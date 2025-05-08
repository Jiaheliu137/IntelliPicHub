package com.jiahe.intellipichub_ddd.shared.websocket.model;

import com.jiahe.intellipichub_ddd.interfaces.vo.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片编辑响应消息，后端要发送给前端的信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {

    /**
     * 消息类型，例如 "INFO", "ERROR", "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVO user;
}
