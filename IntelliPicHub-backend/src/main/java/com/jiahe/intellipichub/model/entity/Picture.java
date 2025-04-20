package com.jiahe.intellipichub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Image
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Image URL
     */
    private String url;

    /**
     * Thumbnail Image URL
     */
    private String thumbnailUrl;

    /**
     * Original Image URL
     */
    private String originalUrl;

    /**
     * Image Name
     */
    private String name;

    /**
     * Introduction
     */
    private String introduction;

    /**
     * Category
     */
    private String category;

    /**
     * Tags (JSON array)
     */
    private String tags;

    /**
     * Image Size
     */
    private Long picSize;

    /**
     * Image Width
     */
    private Integer picWidth;

    /**
     * Image Height
     */
    private Integer picHeight;

    /**
     * Image Aspect Ratio
     */
    private Double picScale;

    /**
     * Image Format
     */
    private String picFormat;

    /**
     * Creator User ID
     */
    private Long userId;

    /**
     * Space ID (null indicates public space)
     */
    private Long spaceId;

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

    /**
     * Review Status: 0-Pending; 1-Approved; 2-Rejected
     */
    private Integer reviewStatus;

    /**
     * Review Message
     */
    private String reviewMessage;

    /**
     * Reviewer ID
     */
    private Long reviewerId;

    /**
     * Review Time
     */
    private Date reviewTime;

    /**
     * Picture main color tone
     */
    private String picColor;



    @TableField(exist = false)
    private  static final long serialVersionUID = 1L;

}