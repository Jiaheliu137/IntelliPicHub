package com.jiahe.intellipichub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Space
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Space Name
     */
    private String spaceName;

    /**
     * Space Level: 0-Regular; 1-Professional; 2-Flagship
     */
    private Integer spaceLevel;

    /**
     * Maximum total size of images in the space
     */
    private Long maxSize;

    /**
     * Maximum number of images in the space
     */
    private Long maxCount;

    /**
     * Total size of images in the space
     */
    private Long totalSize;

    /**
     * Total number of images in the space
     */
    private Long totalCount;

    /**
     * Creator User ID
     */
    private Long userId;

    /**
     * Creation Time
     */
    private Date createTime;

    /**
     * Edit Time
     */
    private Date editTime;

    /**
     * Update Time
     */
    private Date updateTime;

    /**
     * Is Deleted
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private  static final long serialVersionUID = 1L;

}