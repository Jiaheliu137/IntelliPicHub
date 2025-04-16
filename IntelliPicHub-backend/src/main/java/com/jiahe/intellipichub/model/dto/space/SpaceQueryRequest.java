package com.jiahe.intellipichub.model.dto.space;

import com.jiahe.intellipichub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * User id
     */
    private Long userId;

    /**
     * Space name
     */
    private String spaceName;

    /**
     * Space Level: 0-Regular; 1-Professional; 2-Flagship
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
