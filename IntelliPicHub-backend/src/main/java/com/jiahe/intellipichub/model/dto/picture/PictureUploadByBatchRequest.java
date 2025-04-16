package com.jiahe.intellipichub.model.dto.picture;

import lombok.Data;

import java.util.List;

@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 从第几张图片开始
     */
    private Integer first;

    /**
     * 一次加载几张，对于bing的图片来说最大是35，等于抓取数量
     */
    private Integer count;


    /**
     * 自定义图片名称前缀
     */
    private String namePrefix;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
