package com.jiahe.intellipichub.manager.websocket.model;

import lombok.Getter;

/**
 * 图片编辑:消息类型枚举，便于后续根据消息类型进行相应的处理
 */
@Getter
public enum PictureEditMessageTypeEnum {

    INFO("Send notification", "INFO"),
    ERROR("Send error", "ERROR"),
    ENTER_EDIT("Enter edit status", "ENTER_EDIT"),
    EXIT_EDIT("Exit edit status", "EXIT_EDIT"),
    EDIT_ACTION("Execute edit action", "EDIT_ACTION");

    private final String text;
    private final String value;

    PictureEditMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditMessageTypeEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PictureEditMessageTypeEnum typeEnum : PictureEditMessageTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
