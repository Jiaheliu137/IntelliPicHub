package com.jiahe.intellipichub_ddd.domain.space.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Space-User Association
 * @TableName space_user
 */
@TableName(value ="space_user")
@Data
public class SpaceUser {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Space id
     */
    private Long spaceId;

    /**
     * User id
     */
    private Long userId;

    /**
     * Space role：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * Create time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;

    @TableField(exist = false)
    private  static final long serialVersionUID = 1L;

}