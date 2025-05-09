package com.jiahe.intellipichub_ddd.infrastructure.manager.upload.model.dto.file;

import lombok.Data;

// 上传图片的结果
@Data
public class UploadPictureResult {  
  
    /**  
     * 图片地址  
     */  
    private String url;  
  
    /**  
     * 原始图片地址  
     */  
    private String originalUrl;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;
  
    /**  
     * 图片名称  
     */  
    private String picName;  
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private int picWidth;  
  
    /**  
     * 图片高度  
     */  
    private int picHeight;  
  
    /**  
     * 图片宽高比  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;

    /**
     * Picture main color tone
     */
    private String picColor;


}
