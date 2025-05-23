package com.jiahe.intellipichub.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新头像请求
 */
@Data
public class UserUpdateAvatarRequest implements Serializable {

    /**
     * 头像URL
     */
    private String userAvatar;

    private static final long serialVersionUID = 1L;
} 