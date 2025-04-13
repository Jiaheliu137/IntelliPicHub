package com.jiahe.intellipichub.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）
     * 用户上传土图片的时候会立刻获得一个id，此时如果用户选择重新上传图片，则是更新图片，id不变
     */  
    private Long id;

    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    private static final long serialVersionUID = 1L;  
}
