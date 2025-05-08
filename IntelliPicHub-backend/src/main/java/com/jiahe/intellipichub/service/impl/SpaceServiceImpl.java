package com.jiahe.intellipichub.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.exception.ThrowUtils;
import com.jiahe.intellipichub.mapper.SpaceMapper;
import com.jiahe.intellipichub.model.dto.space.SpaceAddRequest;
import com.jiahe.intellipichub.model.dto.space.SpaceQueryRequest;
import com.jiahe.intellipichub.model.entity.Space;
import com.jiahe.intellipichub.model.entity.SpaceUser;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.enums.SpaceLevelEnum;
import com.jiahe.intellipichub.model.enums.SpaceRoleEnum;
import com.jiahe.intellipichub.model.enums.SpaceTypeEnum;
import com.jiahe.intellipichub.model.vo.SpaceVO;
import com.jiahe.intellipichub.model.vo.UserVO;
import com.jiahe.intellipichub.service.SpaceService;
import com.jiahe.intellipichub.service.SpaceUserService;
import com.jiahe.intellipichub.service.UserService;
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
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;
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
        this.validSpace(space, true);

        // 3.校验权限，非管理员只能创建普通空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)){
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
                    if(SpaceTypeEnum.TEAM.getValue()==space.getSpaceType()){
                        SpaceUser spaceUser=new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserService.save(spaceUser);
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
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType= space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建时要校验的参数
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space name cannot be empty");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space level cannot be empty");
            }
            if(spaceType==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space type cannot be empty");

            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space level does not exist");
        }
        // 修改数据时，如果要改空间名称
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space name is too long");
        }
        // 修改数据时，如果要改空间类型
        if (spaceType!=null&& spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space type is invalid");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
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
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息，找到每一个空间对应的User
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }


    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            // 如果管理员没有设置其他的空间容量，则用默认级别的最大值
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            // 如果管理员没有设置其他的空间容纳数量，则用默认级别的最大值
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();

        // 拼接查询条件

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);



        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if(!space.getUserId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"No permission to operate");
        }
    }
}




