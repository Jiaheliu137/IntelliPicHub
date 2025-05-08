package com.jiahe.intellipichub_ddd.shared.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.jiahe.intellipichub_ddd.shared.auth.SpaceUserAuthManager;
import com.jiahe.intellipichub_ddd.shared.auth.model.SpaceUserPermissionConstant;
import com.jiahe.intellipichub_ddd.domain.picture.entity.Picture;
import com.jiahe.intellipichub_ddd.domain.space.entity.Space;
import com.jiahe.intellipichub_ddd.domain.user.entity.User;
import com.jiahe.intellipichub_ddd.domain.space.valueobject.SpaceTypeEnum;
import com.jiahe.intellipichub_ddd.application.service.PictureApplicatioinService;
import com.jiahe.intellipichub_ddd.application.service.SpaceApplicationService;
import com.jiahe.intellipichub_ddd.application.service.UserApplicationService;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Websocket 拦截器，建立连接前要先校验
 */
@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private PictureApplicatioinService pictureApplicatioinService;

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 建立连接前先校验
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebsocketSession 会话设置属性
     * @return
     */
    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        // ServerHttpRequest 是 Spring 抽象出来的“通用的 HTTP 请求”接口，不依赖于 Servlet，可以兼容不同服务器环境（Servlet环境、Reactive环境）。
        // ServletServerHttpRequest 是 ServerHttpRequest 的一个实现类，用于 Servlet 环境下的 HTTP 请求。
        // HttpServletRequest 是 标准的 Servlet API，表示 HTTP 请求，常规 Web 应用开发中最常用的原生请求对象。
        // if (request instanceof ServletServerHttpRequest) 这个检查，是运行时的逻辑， Java 编译器在编译阶段，不知道这个 if 判断以后 request 的类型变了
        // 在编译阶段，request 还是原来的静态类型 —— ServerHttpRequest（接口），所以需要手动转换 ((ServletServerHttpRequest) request)
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 获取请求参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("Lack of picture parameter, reject handshake");
                return false;
            }
            User loginUser = userApplicationService.getLoginUser(servletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("User not logged in, reject handshake");
                return false;
            }
            // 校验用户是否有该图片的权限
            Picture picture = pictureApplicatioinService.getById(pictureId);
            if (picture == null) {
                log.error("Picture does not exist, reject handshake");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceApplicationService.getById(spaceId);
                if (space == null) {
                    log.error("Space does not exist, reject handshake");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.info("Not a team space, reject handshake");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("No picture edit permission, reject handshake");
                return false;
            }
            // 设置 attributes
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId)); // 记得转换为long类型
        }
        return true;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
