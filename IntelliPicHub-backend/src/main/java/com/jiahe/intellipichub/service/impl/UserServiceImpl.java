package com.jiahe.intellipichub.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub.constant.UserConstant;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.manager.auth.StpKit;
import com.jiahe.intellipichub.mapper.UserMapper;
import com.jiahe.intellipichub.model.dto.user.UserQueryRequest;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.enums.UserRoleEnum;
import com.jiahe.intellipichub.model.vo.LoginUserVO;
import com.jiahe.intellipichub.model.vo.UserVO;
import com.jiahe.intellipichub.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiahe
 * @description 针对表【user(User Table)】的数据库操作Service实现
 * @createDate 2025-03-29 12:17:20
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "intellipichub";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Params are not allowed to be empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Account format error");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password format error");
        }

        // 2.对用户传递的密码加密,写在最底下了
        String encryptPassword = getEncryptPassword(userPassword);

        // 3.查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //不存在抛异常
        if (user == null) {
            log.info("user login failed,userAccount can not match password");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "User not exist or password wrong");
        }


        // 4.保存用户的登录态到自定义的
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    /**
     * // 获得脱敏用户信息；老对象赋值给新对象，新对象没有的字段就不会被赋值
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
//        普通写法
//        List<UserVO> userVOList = new ArrayList<>();
//        for (User user : userList) {
//            UserVO userVO = getUserVO(user);  // 调用转换方法
//            userVOList.add(userVO);  // 添加到结果列表中
//        }
//        return userVOList;

        //高级写法
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询，追求性能的话可以直接返回上面成果(上面的内容如果用户更改名称的话，那网页session和数据库中的昵称就不一样了)
        // 能不用缓存就不用缓存
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, " Not login");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    // QueryWrapper<User> 返回的数据实际上是构建的数据库查询条件，并且在 MyBatis Plus 中，它会自动将这些条件转化为 SQL 查询语句。
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Request param is null");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 一系列查询条件是且的关系，同时要知道参数中的userQueryRequest对象里不一定每个对象都写值了
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user!=null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    // region --用户会员兑换功能--
    /**
     * 兑换会员
     * @param user
     * @param vipCode
     * @return
     */
    @Override
    public boolean exchangeVip(User user, String vipCode) {
        // 使用工具类检查VIP兑换码是否有效
        if (!com.jiahe.intellipichub.utils.VipCodeUtil.isValidVipCode(vipCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid vip code or vip code has been used");
        }
        
        // 标记VIP兑换码为已使用
        boolean markResult = com.jiahe.intellipichub.utils.VipCodeUtil.markVipCodeAsUsed(vipCode);
        if (!markResult) {
            log.warn("标记VIP兑换码为已使用失败，但会继续处理会员开通逻辑: {}", vipCode);
        }
        
        // 修改数据库用户表，给当前登录用户开通一年会员
        // 1. 查询VIP会员数量，生成递增的会员编号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("vipNumber");
        long vipCount = this.count(queryWrapper);
        long vipNumber = vipCount + 1; // 会员编号递增
        
        // 2. 更新用户信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserRole(UserRoleEnum.VIP.getValue());
        updateUser.setVipCode(vipCode);
        updateUser.setVipNumber(vipNumber);
        
        // 使用Hutool的DateUtil计算会员到期时间：当前时间加一年
        Date vipExpireTime = DateUtil.offset(new Date(), DateField.YEAR, 1);
        updateUser.setVipExpireTime(vipExpireTime);
        
        boolean result = this.updateById(updateUser);
        return result;
    }

    // endregion --用户会员兑换功能--


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Parameters cannot be empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " User account is too short");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " User password is too short");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " The two passwords are inconsistent");
        }

        // 2. 检查账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Account already exists");
        }

        // 3. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);

        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, " Request failed,database operation failed");
        }
        return user.getId();
    }


}




