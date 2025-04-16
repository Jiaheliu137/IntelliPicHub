package com.jiahe.intellipichub.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * Space update request, for administrators
 */
@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * Space name
     */
    private String spaceName;

    /**
     * Space Level: 0-Regular; 1-Professional; 2-Flagship
     */
    private Integer spaceLevel;

    /**
     * Maximum total size of space images
     */
    private Long maxSize;

    /**
     * Maximum number of space images
     */
    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
