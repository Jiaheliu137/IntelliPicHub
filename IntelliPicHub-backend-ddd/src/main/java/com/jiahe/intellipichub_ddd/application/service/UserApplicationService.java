package com.jiahe.intellipichub_ddd.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiahe.intellipichub_ddd.domain.user.entity.User;
import com.jiahe.intellipichub_ddd.infrastructure.common.DeleteRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.user.UserLoginRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.user.UserQueryRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.user.UserRegisterRequest;
import com.jiahe.intellipichub_ddd.interfaces.vo.user.LoginUserVO;
import com.jiahe.intellipichub_ddd.interfaces.vo.user.UserVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;


/**
 * @author jiahe
 * @description 针对表【user(User Table)】的数据库操作Service
 * @createDate 2025-03-29 12:17:20
 */
public interface UserApplicationService {

    /**
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    long addUser(User user);

    User getUserById(long id);

    UserVO getUserVOById(long id);

    boolean deleteUser(DeleteRequest deleteRequest);

    void updateUser(User user);

    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    List<User> listByIds(Set<Long> userIdSet);

    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest);

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

    @Transactional
    long userRegister(UserRegisterRequest userRegisterRequest);

    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

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

    long saveUser(User userEntity);
}
