package com.jiahe.intellipichub_ddd.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub_ddd.application.service.SpaceApplicationService;
import com.jiahe.intellipichub_ddd.application.service.SpaceUserApplicationService;
import com.jiahe.intellipichub_ddd.application.service.UserApplicationService;
import com.jiahe.intellipichub_ddd.domain.space.entity.Space;
import com.jiahe.intellipichub_ddd.domain.space.entity.SpaceUser;
import com.jiahe.intellipichub_ddd.domain.space.service.SpaceDomainService;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceLevelEnum;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceRoleEnum;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceTypeEnum;
import com.jiahe.intellipichub_ddd.domain.user.entity.User;
import com.jiahe.intellipichub_ddd.infrastructure.exception.BusinessException;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ThrowUtils;
import com.jiahe.intellipichub_ddd.infrastructure.mapper.SpaceMapper;
import com.jiahe.intellipichub_ddd.interfaces.dto.space.SpaceAddRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.space.SpaceQueryRequest;
import com.jiahe.intellipichub_ddd.interfaces.vo.space.SpaceVO;
import com.jiahe.intellipichub_ddd.interfaces.vo.user.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author jiahe
 * @description 针对表【space(Space)】的数据库操作Service实现
 * @createDate 2025-04-15 14:48:07
 */
@Service
public class SpaceApplicationServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceApplicationService {

    @Resource
    private SpaceDomainService spaceDomainService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;
    // 为了方便部署，注释掉分库分表
//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    /**
     * 用于存储用户级别的锁对象
     */
    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 填充参数值
        // 转换实体类和DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("Basic space");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量和大小
        this.fillSpaceBySpaceLevel(space);

        // 2. 校验参数
        space.validSpace(true);

        // 3.校验权限，非管理员只能创建普通空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !loginUser.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "No auth to create a space of the specified level.");
        }

        // 4. 控制同一用户只能创建一个私有空间,以及一个团队空间
        // 使用ConcurrentHashMap存储锁对象，避免内存泄漏问题
        // 为每个用户（userId）动态创建并管理一个唯一的锁对象
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
        try {
            synchronized (lock) {
                // transactionTemplate.execute 的作用是：在方法中执行一段包含事务管理的代码。
                // 代码会在一个事务中执行，Spring 会确保在执行过程中如果发生任何异常，可以回滚事务；如果执行成功，事务将提交。
                Long newSpaceId = transactionTemplate.execute(status -> {
                    // 判断是否已有空间，有的话则不能创建
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, space.getSpaceType())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "Each user can create only one space per space type.");
                    // 创建
                    boolean result = this.save((space));
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Space creation failed");
                    // 创建成功后，如果是团队空间，关联新增团队成员记录(space_user数据库)
                    if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserApplicationService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Failed to create team member record");
                    }
                    // 创建图片分表，仅对团队空间生效
                    // 为了方便部署，注释掉分库分表

//                    dynamicShardingManager.createSpacePictureTable(space);
                    // 成功则返回新写入的id
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            }
        } finally {
            // 操作完成后移除锁对象，防止内存泄漏
            lockMap.remove(userId);
        }
    }


    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 从空间列表中提取所有用户ID，并收集到一个Set集合中（无重复元素）
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 根据这些用户ID查询用户信息，并按用户ID分组存储到Map中，实际上就是一个userId对应一个列表，该列表只有一个元素那就是User
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息，找到每一个空间对应的User
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userApplicationService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }


    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        spaceDomainService.fillSpaceBySpaceLevel(space);
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {

        return spaceDomainService.getQueryWrapper(spaceQueryRequest);
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        spaceDomainService.checkSpaceAuth(loginUser, space);
    }
}




