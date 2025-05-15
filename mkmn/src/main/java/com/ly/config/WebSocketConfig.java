package com.ly.config;

import com.ly.handler.FileWebSocketHandler;
import com.ly.handler.TextWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FileWebSocketHandler fileWebSocketHandler;
    private final TextWebSocketHandler textWebSocketHandler;

    @Autowired
    public WebSocketConfig(FileWebSocketHandler fileWebSocketHandler, TextWebSocketHandler textWebSocketHandler) {
        this.fileWebSocketHandler = fileWebSocketHandler;
        this.textWebSocketHandler = textWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(fileWebSocketHandler, "/ws/file").setAllowedOrigins("*");
        registry.addHandler(textWebSocketHandler, "/ws/text").setAllowedOrigins("*");
    }
}