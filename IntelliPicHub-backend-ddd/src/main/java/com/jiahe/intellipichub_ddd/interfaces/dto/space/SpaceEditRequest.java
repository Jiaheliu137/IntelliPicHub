package com.jiahe.intellipichub_ddd.interfaces.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间编辑请求，给用户使用
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * Space id
     */
    private Long id;


    /**
     * Space name
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
