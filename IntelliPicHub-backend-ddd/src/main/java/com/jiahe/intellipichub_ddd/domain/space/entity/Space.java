package com.jiahe.intellipichub_ddd.domain.space.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceLevelEnum;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceTypeEnum;
import com.jiahe.intellipichub_ddd.infrastructure.exception.BusinessException;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ThrowUtils;
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
     * Space-type：0-personal 1-group
     */
    private Integer spaceType;


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

    public void validSpace(boolean add) {
        ThrowUtils.throwIf(this == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = this.getSpaceName();
        Integer spaceLevel = this.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType= this.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建时要校验的参数
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space name cannot be empty");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space level cannot be empty");
            }
            if(spaceType==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space type cannot be empty");

            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space level does not exist");
        }
        // 修改数据时，如果要改空间名称
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space name is too long");
        }
        // 修改数据时，如果要改空间类型
        if (spaceType!=null&& spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space type is invalid");
        }
    }

}