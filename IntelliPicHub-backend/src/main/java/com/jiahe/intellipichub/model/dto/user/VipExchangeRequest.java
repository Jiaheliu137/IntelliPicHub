package com.jiahe.intellipichub.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * VIP会员兑换请求
 */
@Data
public class VipExchangeRequest implements Serializable {

    /**
     * VIP兑换码
     */
    private String vipCode;

    private static final long serialVersionUID = 1L;
} 