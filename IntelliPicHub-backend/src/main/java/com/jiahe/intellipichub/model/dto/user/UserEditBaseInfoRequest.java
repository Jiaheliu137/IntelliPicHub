package com.jiahe.intellipichub.model.dto.user;

import lombok.Data;

import java.io.Serializable;


/**
 * 编辑用户基本信息（用户名和简介）
 */
@Data
public class UserEditBaseInfoRequest implements Serializable {


     /**
     * 用户名
     */
    private String userName;

    /**
     * 用户简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
