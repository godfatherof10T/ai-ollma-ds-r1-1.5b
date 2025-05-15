package com.ly.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.manager.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TextWebSocketHandler extends AbstractWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public TextWebSocketHandler(WebSocketSessionManager sessionManager, WebClient webClient, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public TextWebSocketHandler fileWebSocketHandler() {
        return this;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = UUID.randomUUID().toString();
        sessionManager.addSession(sessionId, session);
        session.sendMessage(new TextMessage("Your session ID is: " + sessionId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String inputText = message.getPayload();
        // 调用AI接口获取流式结果
        callAIInterface(inputText)
               .subscribe(
                        partialResult -> {
                            try {
                                session.sendMessage(new TextMessage(partialResult));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            try {
                                session.sendMessage(new TextMessage("Error: " + error.getMessage()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        },
                        () -> {
                            try {
                                session.sendMessage(new TextMessage("[DONE]")); // 表示流式传输结束
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }

    private Flux<String> callAIInterface(String inputText) {
        // 构建Ollama请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-r1:1.5b");
        requestBody.put("prompt", inputText);
        requestBody.put("stream", true); // 启用流式响应

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            return webClient.post()
                   .uri("/api/generate")
                   .header("Content-Type", "application/json")
                   .bodyValue(jsonBody)
                   .retrieve()
                   .bodyToFlux(String.class)
                   .map(response -> {
                        try {
                            // 解析JSON响应，提取文本内容
                            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
                            if (jsonResponse.containsKey("response")) {
                                return (String) jsonResponse.get("response");
                            }
                            return "";
                        } catch (Exception e) {
                            return "";
                        }
                    });
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionManager.removeSession(sessionId);
    }
}