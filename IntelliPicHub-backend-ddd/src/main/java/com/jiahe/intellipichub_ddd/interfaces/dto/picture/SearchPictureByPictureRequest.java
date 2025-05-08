package com.jiahe.intellipichub_ddd.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图请求（图片id）
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
