package com.jiahe.intellipichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub.model.dto.user.UserEditBaseInfoRequest;
import com.jiahe.intellipichub.model.dto.user.UserQueryRequest;
import com.jiahe.intellipichub.model.dto.user.UserUpdateAvatarRequest;
import com.jiahe.intellipichub.model.dto.user.UserUpdatePasswordRequest;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.vo.LoginUserVO;
import com.jiahe.intellipichub.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author jiahe
 * @description 针对表【user(User Table)】的数据库操作Service
 * @createDate 2025-03-29 12:17:20
 */
public interface UserService extends IService<User> {

    /**
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获得脱敏后的登陆用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获得脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);


    /**
     * 返回脱敏后的用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取但当前登录用户(还未返回给前端)
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);

    /**
     * 用户会员码兑换会员
     * @param user
     * @param vipCode
     * @return
     */
    boolean exchangeVip(User user,String vipCode);

    /**
     * 更新用户基本信息（用户名和简介）
     * 
     * @param user 当前登录用户
     * @param userEditBaseInfoRequest 用户基本信息编辑请求
     * @return 是否更新成功
     */
    boolean updateUserInfo(User user, UserEditBaseInfoRequest userEditBaseInfoRequest);
    
    /**
     * 更新用户头像
     * 
     * @param user 当前登录用户
     * @param userUpdateAvatarRequest 用户头像更新请求
     * @return 是否更新成功 
     */
    boolean updateUserAvatar(User user, UserUpdateAvatarRequest userUpdateAvatarRequest);
    
    /**
     * 用户上传头像
     * 
     * @param user 当前登录用户
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(User user, MultipartFile file);
    
    /**
     * 修改用户密码
     * 
     * @param user 当前登录用户
     * @param updatePasswordRequest 修改密码请求
     * @return 是否修改成功
     */
    boolean updateUserPassword(User user, UserUpdatePasswordRequest updatePasswordRequest);
}
