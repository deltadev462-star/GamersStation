package com.thegamersstation.marketplace.websocket;

import com.thegamersstation.marketplace.messaging.repository.ConversationRepository;
import com.thegamersstation.marketplace.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final JwtUtil jwtUtil;
    private final ConversationRepository conversationRepository;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern CONVERSATION_TOPIC_PATTERN = 
        Pattern.compile("/topic/conversation\\.(\\d+)\\.");
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(accessor, message);
        }
        
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return handleSubscribe(accessor, message);
        }
        
        return message;
    }
    
    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String token = extractToken(accessor);
        
        if (token != null) {
            try {
                if (jwtUtil.validateAccessToken(token)) {
                    Long userId = jwtUtil.extractUserId(token);
                    String role = jwtUtil.extractRole(token);
                    
                    String springRole = (role != null) ? "ROLE_" + role : "ROLE_USER";
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(springRole)
                    );
                    
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                    );
                    
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    log.debug("WebSocket authentication successful for user: {}", userId);
                    return message;
                }
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
            }
        }
        
        log.warn("WebSocket connection rejected - invalid or missing token");
        return null;
    }
    
    /**
     * Authorize SUBSCRIBE commands: verify the user is a participant of the conversation
     * before allowing subscription to conversation-specific topics.
     */
    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }
        
        Matcher matcher = CONVERSATION_TOPIC_PATTERN.matcher(destination);
        if (matcher.find()) {
            Long conversationId = Long.parseLong(matcher.group(1));
            Long userId = extractUserIdFromAccessor(accessor);
            
            if (userId == null) {
                log.warn("Unauthenticated subscription attempt to: {}", destination);
                throw new AccessDeniedException("Authentication required");
            }
            
            if (!conversationRepository.isParticipant(conversationId, userId)) {
                log.warn("User {} unauthorized subscription to conversation {}", userId, conversationId);
                throw new AccessDeniedException("Not a participant in this conversation");
            }
        }
        
        return message;
    }
    
    private Long extractUserIdFromAccessor(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
        }
        return null;
    }
    
    private String extractToken(StompHeaderAccessor accessor) {
        // Try to extract from Authorization header
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        // Try to extract from query parameter (for SockJS)
        String token = accessor.getFirstNativeHeader("token");
        if (token != null) {
            return token;
        }
        
        // Try to extract from connection URL (for browsers that don't support headers)
        // This would be passed as ws://localhost:8080/ws?token=JWT_TOKEN
        Object rawMessage = accessor.getHeader("simpConnectMessage");
        if (rawMessage != null) {
            String messageStr = rawMessage.toString();
            int tokenIndex = messageStr.indexOf("token=");
            if (tokenIndex != -1) {
                int tokenStart = tokenIndex + 6;
                int tokenEnd = messageStr.indexOf("&", tokenStart);
                if (tokenEnd == -1) {
                    tokenEnd = messageStr.indexOf(" ", tokenStart);
                }
                if (tokenEnd == -1) {
                    tokenEnd = messageStr.length();
                }
                return messageStr.substring(tokenStart, tokenEnd);
            }
        }
        
        return null;
    }
}