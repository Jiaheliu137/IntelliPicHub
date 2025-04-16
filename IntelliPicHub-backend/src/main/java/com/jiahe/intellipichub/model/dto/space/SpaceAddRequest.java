package com.jiahe.intellipichub.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间创建请求
 */
@Data
public class SpaceAddRequest implements Serializable {

    /**
     * Space name
     */
    private String spaceName;

    /**
     * Space Level: 0-Common; 1-Professional; 2-Flagship
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
