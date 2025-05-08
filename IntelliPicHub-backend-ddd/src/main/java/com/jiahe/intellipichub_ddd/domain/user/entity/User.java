package com.jiahe.intellipichub_ddd.domain.user.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.jiahe.intellipichub_ddd.domain.user.valueobject.UserRoleEnum;
import com.jiahe.intellipichub_ddd.infrastructure.exception.BusinessException;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * User Table
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * Primary ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Account
     */
    private String userAccount;

    /**
     * password
     */
    private String userPassword;


    /**
     * Username
     */
    private String userName;

    /**
     * User Avatar URL
     */
    private String userAvatar;

    /**
     * User Profile Description
     */
    private String userProfile;

    /**
     * User Role: user/admin
     */
    private String userRole;

    /**
     * Edit Time
     */
    private Date editTime;

    /**
     * Create Time
     */
    private Date createTime;

    /**
     * Update Time
     */
    private Date updateTime;

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer isDelete;

    /**
     * VIP Expiration Time
     */
    private Date vipExpireTime;

    /**
     * VIP Redemption Code
     */
    private String vipCode;

    /**
     * VIP Number
     */
    private Long vipNumber;

    /**
     * Share Code
     */
    private String shareCode;

    /**
     * Inviter User ID
     */
    private Long inviteUser;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 校验用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     */
    public static void validUserRegister(String userAccount, String userPassword, String checkPassword){
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

    }

    /**
     * 校验用户登录
     * @param userAccount
     * @param userPassword
     */
    public static void validUserLogin(String userAccount, String userPassword){
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Params are not allowed to be empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " Account format error");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password format error");
        }
    }

    /**
     * 判断用户是否为管理员
     * @return
     */
    public boolean isAdmin() {
        return UserRoleEnum.ADMIN.getValue().equals(this.getUserRole());
    }

}