package com.thegamersstation.marketplace.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketAuthInterceptor authInterceptor;
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "https://thegamersstation.com",
                    "https://www.thegamersstation.com",
                    "https://gamers-station.com",
                    "https://www.gamers-station.com",
                    "http://localhost:[*]",
                    "http://localhost:5173",
                    "http://localhost:5174",
                    "http://localhost:3000"
                )
                .withSockJS()
                .setSessionCookieNeeded(false);
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple memory-based message broker
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Set application destination prefix
        registry.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor
        registration.interceptors(authInterceptor);
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(65536)       // 64KB max message size
            .setSendBufferSizeLimit(524288)    // 512KB send buffer
            .setSendTimeLimit(20000)           // 20 seconds send timeout
            .setTimeToFirstMessage(30000);     // 30 seconds for first message
    }
}