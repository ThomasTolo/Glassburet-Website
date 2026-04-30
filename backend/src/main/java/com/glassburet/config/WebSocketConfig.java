package com.glassburet.config;

import com.glassburet.realtime.SiteUpdateWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SiteUpdateWebSocketHandler siteUpdateWebSocketHandler;

    public WebSocketConfig(SiteUpdateWebSocketHandler siteUpdateWebSocketHandler) {
        this.siteUpdateWebSocketHandler = siteUpdateWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(siteUpdateWebSocketHandler, "/ws/updates")
                .setAllowedOriginPatterns("*");
    }
}
