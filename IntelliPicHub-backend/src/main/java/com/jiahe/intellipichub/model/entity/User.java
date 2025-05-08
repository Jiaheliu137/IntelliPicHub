package com.jiahe.intellipichub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
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
     * VIP Number，会员编号
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
}