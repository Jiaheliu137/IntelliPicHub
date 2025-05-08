package com.jiahe.intellipichub.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * User Table 已登录用户视图(脱敏)
 */

@Data
public class UserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建时间
     */
    private Date updateTime;

        /**
     * VIP 过期时间
     */
    private Date vipExpireTime;

    /**
     * VIP 兑换码
     */
    private String vipCode;

    /**
     * VIP Number，会员编号
     */
    private Long vipNumber;



    private static final long serialVersionUID = 1L;
}
