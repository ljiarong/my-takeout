package com.sky.server;/**
 * ClassName: WebSocketServer
 * Package: com.sky.server
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: my-takeout
 *
 * @description:
 *
 * @author: ljr
 *
 * @create: 2023-10-10 14:32
 **/
@Component
@Slf4j
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {
    public static Map<String, Session> sessionMap=new HashMap<>();
    @OnOpen
    public void connectLink(Session session, @PathParam(value = "sid") String sid){
        log.info("WebSocketServer的connectLink方法执行中，参数为{客户端建立连接}"+sid);
        sessionMap.put(sid,session);
    }

    @OnError
    public void connectError(Throwable throwable){
        log.info("WebSocketServer的connectError方法执行中，参数为{连接建立失败}");
        throwable.printStackTrace();
    }

    @OnMessage
    public void getMessage(@PathParam(value = "sid") String sid, String message){
        log.info("WebSocketServer的getMessage方法执行中，参数为{接收到来自}"+sid+"的消息"+message);
    }
    @OnClose
    public void connect(@PathParam(value = "sid") String sid){
        log.info("WebSocketServer的connect方法执行中，参数为{连接断开}"+sid);
        sessionMap.remove(sid);
    }

    public void sendToAllClient(String message){
        Collection<Session> values = sessionMap.values();
        for (Session value : values) {
            try {
                value.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
