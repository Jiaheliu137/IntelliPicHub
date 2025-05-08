package com.jiahe.intellipichub_ddd.domain.picture.entity;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ThrowUtils;
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


    public void validPicture() {
        // 从对象中取值
        Long id = this.getId();
        String url = this.getUrl();
        String introduction = this.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id can not be empty");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url is too long");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "introduce is too long");
        }
    }


}