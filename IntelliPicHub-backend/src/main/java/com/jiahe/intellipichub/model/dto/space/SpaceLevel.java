package com.jiahe.intellipichub.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @AllArgsConstructor注解用于自动生成一个包含所有字段的构造函数
 * 空间级别
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
