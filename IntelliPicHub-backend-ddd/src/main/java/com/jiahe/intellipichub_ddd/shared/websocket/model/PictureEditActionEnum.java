package com.jiahe.intellipichub_ddd.shared.websocket.model;

import lombok.Getter;

/**
 * 图片编辑:操作类型枚举
 */
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("Zoom in", "ZOOM_IN"),
    ZOOM_OUT("Zoom out", "ZOOM_OUT"),
    ROTATE_LEFT("Rotate left", "ROTATE_LEFT"),
    ROTATE_RIGHT("Rotate right", "ROTATE_RIGHT");

    private final String text;
    private final String value;

    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PictureEditActionEnum actionEnum : PictureEditActionEnum.values()) {
            if (actionEnum.value.equals(value)) {
                return actionEnum;
            }
        }
        return null;
    }
}
