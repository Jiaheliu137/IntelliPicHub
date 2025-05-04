package com.jiahe.intellipichub.manager.websocket.disruptor;

import com.jiahe.intellipichub.manager.websocket.model.PictureEditRequestMessage;
import com.jiahe.intellipichub.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件
 * 里面的成员和handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);接受的参数一模一样
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;
    
    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
