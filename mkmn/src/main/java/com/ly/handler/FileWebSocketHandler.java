package com.ly.handler;

import com.ly.manager.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

@Component
public class FileWebSocketHandler extends AbstractWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public FileWebSocketHandler fileWebSocketHandler() {
        return this;
    }

    @Autowired
    public FileWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = UUID.randomUUID().toString();
        sessionManager.addSession(sessionId, session);
        session.sendMessage(new TextMessage("Your session ID is: " + sessionId));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        ByteBuffer buffer = message.getPayload();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        byteArrayOutputStream.write(bytes);

        if (message.isLast()) {
            String fileContent = byteArrayOutputStream.toString("UTF-8");
            // 返回文件内容给前端
            session.sendMessage(new TextMessage(fileContent));
            byteArrayOutputStream.reset();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionManager.removeSession(sessionId);
    }
}