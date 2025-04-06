package com.jiahe.intellipichub.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User Register request
 */
@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -4190454383168078627L;

    private String userAccount;

    private String userPassword;


}
