# 🔌 WebSocket Implementation Guide for Real-Time Messaging

## Overview

This guide explains how to implement real-time messaging using WebSockets in a Spring Boot application. It covers architecture, flow, authentication, and best practices.

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [WebSocket Flow](#websocket-flow)
4. [Implementation Steps](#implementation-steps)
5. [Authentication & Security](#authentication--security)
6. [Message Broadcasting](#message-broadcasting)
7. [Client Integration](#client-integration)
8. [Testing](#testing)
9. [Production Considerations](#production-considerations)

---

## 🏗️ Architecture Overview

```
┌─────────────┐                    ┌─────────────┐
│   Client A  │                    │   Client B  │
│  (Browser)  │                    │  (Browser)  │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       │ WS Connect                       │ WS Connect
       │ (with JWT)                       │ (with JWT)
       ▼                                  ▼
┌────────────────────────────────────────────────┐
│          WebSocket Server (STOMP)              │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │   WebSocket Configuration                │ │
│  │   - Endpoint: /ws                        │ │
│  │   - Protocol: STOMP over SockJS          │ │
│  │   - Authentication: JWT Interceptor      │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │   Message Broker                         │ │
│  │   - /app/* → Application handlers        │ │
│  │   - /topic/* → Broadcast destinations    │ │
│  │   - /user/* → User-specific destinations │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │   Message Controllers                    │ │
│  │   - @MessageMapping("/send")             │ │
│  │   - @SendToUser("/queue/messages")       │ │
│  └──────────────────────────────────────────┘ │
└────────────────┬───────────────────────────────┘
                 │
                 │ Save to DB
                 ▼
         ┌───────────────┐
         │   Database    │
         │  (messages,   │
         │conversations) │
         └───────────────┘
```

---

## 🛠️ Technology Stack

### Backend:
- **Spring WebSocket** - WebSocket support
- **STOMP Protocol** - Simple Text Oriented Messaging Protocol
- **SockJS** - WebSocket fallback for older browsers
- **Spring Security** - JWT authentication
- **Spring Messaging** - Message routing and broadcasting

### Frontend:
- **SockJS Client** - WebSocket client with fallback
- **STOMP.js** - STOMP protocol client
- **JavaScript/TypeScript** - Client implementation

---

## 🔄 WebSocket Flow

### 1. **Connection Flow**

```
Client                          Server
  │                               │
  │  1. HTTP Handshake            │
  │  GET /ws?token=JWT            │
  ├──────────────────────────────>│
  │                               │
  │                   2. Validate JWT
  │                   3. Upgrade to WS
  │                               │
  │  4. WebSocket Established     │
  │<──────────────────────────────┤
  │                               │
  │  5. STOMP CONNECT             │
  ├──────────────────────────────>│
  │                               │
  │                   6. Authenticate
  │                   7. Create Session
  │                               │
  │  8. STOMP CONNECTED           │
  │<──────────────────────────────┤
  │                               │
  │  9. SUBSCRIBE                 │
  │  /user/queue/messages         │
  ├──────────────────────────────>│
  │                               │
  │  10. Subscription Success     │
  │<──────────────────────────────┤
  │                               │
  │  Ready to send/receive        │
```

### 2. **Message Sending Flow**

```
Sender (User A)              Server                Recipient (User B)
     │                         │                         │
     │ 1. Send Message         │                         │
     │ SEND /app/send          │                         │
     │ {"content": "Hello"}    │                         │
     ├────────────────────────>│                         │
     │                         │                         │
     │             2. Authenticate & Authorize           │
     │             3. Save to Database                   │
     │                         │                         │
     │ 4. HTTP Response        │                         │
     │ 201 Created             │                         │
     │<────────────────────────┤                         │
     │                         │                         │
     │             5. Broadcast via WebSocket            │
     │                         │ MESSAGE                 │
     │                         │ /user/{userId}/queue/   │
     │                         │   messages              │
     │                         ├────────────────────────>│
     │                         │                         │
     │                         │              6. Receive │
     │                         │              7. Display │
```

---

## 📝 Implementation Steps

### Step 1: Add Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

---

### Step 2: WebSocket Configuration

**Purpose**: Configure WebSocket endpoints, message broker, and STOMP protocol.

**File**: `WebSocketConfig.java`

**Key Configuration:**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    // 1. Register WebSocket endpoint
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")              // Endpoint URL
                .setAllowedOriginPatterns("*")   // CORS
                .withSockJS();                   // Fallback support
    }
    
    // 2. Configure message broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user");  // Destinations
        registry.setApplicationDestinationPrefixes("/app"); // Client prefix
        registry.setUserDestinationPrefix("/user");        // User prefix
    }
    
    // 3. Add authentication interceptor
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
```

**Message Destinations:**
- `/app/*` → Messages TO server (client sends here)
- `/topic/*` → Broadcast messages (server broadcasts to all)
- `/user/*` → Private messages (server sends to specific user)

---

### Step 3: WebSocket Authentication

**Purpose**: Authenticate WebSocket connections using JWT tokens.

**File**: `WebSocketAuthInterceptor.java`

**Authentication Flow:**

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 1. Extract JWT token from connection
            String token = extractToken(accessor);
            
            // 2. Validate JWT token
            if (jwtUtil.validateAccessToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                
                // 3. Create authentication
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities
                );
                
                // 4. Set user in WebSocket session
                accessor.setUser(auth);
                
                return message;  // Allow connection
            }
            
            return null;  // Reject connection
        }
        
        return message;
    }
}
```

**Token Extraction Methods:**

**Option 1: Query Parameter** (Recommended for SockJS)
```java
// Client connects: ws://localhost:8080/ws?token=JWT_TOKEN
String token = request.getParameter("token");
```

**Option 2: STOMP Header**
```java
// Client sends: CONNECT with Authorization header
String authHeader = accessor.getFirstNativeHeader("Authorization");
String token = authHeader.substring(7); // Remove "Bearer "
```

---

### Step 4: Message Controllers

**Purpose**: Handle incoming WebSocket messages.

**File**: `WebSocketMessagingController.java`

**Example Controller:**

```java
@Controller
public class WebSocketMessagingController {
    
    // Handle incoming messages from clients
    @MessageMapping("/conversations/{conversationId}/send")
    public void handleMessage(
        @DestinationVariable Long conversationId,
        @Payload SendMessageRequest request,
        Principal principal  // Authenticated user
    ) {
        // 1. Get authenticated user ID
        Long userId = extractUserId(principal);
        
        // 2. Validate user is participant
        validateParticipant(conversationId, userId);
        
        // 3. Save message to database
        Message message = messageService.sendMessage(
            conversationId, 
            request, 
            userId
        );
        
        // 4. Broadcasting happens in service layer
        // (See Step 5)
    }
    
    // Handle read receipts
    @MessageMapping("/messages/{messageId}/read")
    public void markAsRead(
        @DestinationVariable Long messageId,
        Principal principal
    ) {
        Long userId = extractUserId(principal);
        messageService.markMessageAsRead(messageId, userId);
    }
}
```

**Annotations:**
- `@MessageMapping` - Maps WebSocket messages to handlers (like @PostMapping for HTTP)
- `@DestinationVariable` - Extract path variables from destination
- `@Payload` - Message body
- `Principal` - Authenticated user

---

### Step 5: Message Broadcasting

**Purpose**: Send messages to specific users via WebSocket.

**File**: `MessageService.java`

**Broadcasting Implementation:**

```java
@Service
public class MessageService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public MessageDto sendMessage(
        Long conversationId, 
        SendMessageRequest request, 
        Long senderId
    ) {
        // 1. Save message to database
        Message message = saveMessageToDatabase(
            conversationId, 
            request, 
            senderId
        );
        
        // 2. Convert to DTO
        MessageDto messageDto = convertToDto(message);
        
        // 3. Determine recipient
        Long recipientId = getOtherParticipant(conversationId, senderId);
        
        // 4. Broadcast to recipient via WebSocket
        messagingTemplate.convertAndSendToUser(
            recipientId.toString(),      // User ID
            "/queue/messages",           // Destination
            messageDto                   // Payload
        );
        
        // Recipient subscribes to: /user/{userId}/queue/messages
        // Spring automatically adds /user/{userId} prefix
        
        return messageDto;
    }
}
```

**SimpMessagingTemplate Methods:**

1. **Send to specific user:**
```java
// Recipient subscribes to: /user/{userId}/queue/messages
messagingTemplate.convertAndSendToUser(
    userId.toString(),
    "/queue/messages",
    message
);
```

2. **Broadcast to all:**
```java
// All subscribers to /topic/announcements receive
messagingTemplate.convertAndSend(
    "/topic/announcements",
    announcement
);
```

3. **Send to session:**
```java
// Send to specific WebSocket session
messagingTemplate.convertAndSendToSession(
    sessionId,
    "/queue/messages",
    message
);
```

---

### Step 6: REST API Fallback

**Purpose**: Provide REST API for when WebSocket is unavailable.

**File**: `MessageController.java`

**REST Endpoints:**

```java
@RestController
@RequestMapping("/conversations/{conversationId}/messages")
public class MessageController {
    
    // Send message via REST (also broadcasts via WebSocket)
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
        @PathVariable Long conversationId,
        @RequestBody SendMessageRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        // Same service method - broadcasts to WebSocket subscribers
        MessageDto message = messageService.sendMessage(
            conversationId, 
            request, 
            userId
        );
        
        return ResponseEntity.ok(message);
    }
    
    // Get messages (for loading history)
    @GetMapping
    public ResponseEntity<MessagesPageDto> getMessages(
        @PathVariable Long conversationId,
        @RequestParam(required = false) Long cursor,
        @AuthenticationPrincipal Long userId
    ) {
        MessagesPageDto messages = messageService.getMessages(
            conversationId, 
            cursor, 
            20, 
            userId
        );
        
        return ResponseEntity.ok(messages);
    }
}
```

**Why REST + WebSocket?**
- WebSocket: Real-time delivery
- REST: Loading history, offline fallback, mobile apps

---

## 🔐 Authentication & Security

### Security Configuration

**Allow WebSocket Endpoint:**

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/ws/**").permitAll()  // Allow WebSocket
            .anyRequest().authenticated()
        );
        
        return http.build();
    }
}
```

### JWT Token Flow

```
Client Login
     │
     ├─> POST /auth/login
     │   { phone, otp }
     │
     ├<─ 200 OK
     │   { accessToken, refreshToken }
     │
     ├─> Store tokens in localStorage
     │
     ├─> Connect WebSocket
     │   ws://server/ws?token={accessToken}
     │
     └─> On 401/403: Refresh token or re-login
```

### Security Best Practices

1. **Validate on every message**
   - Don't trust WebSocket session after initial auth
   - Re-validate user permissions per message

2. **Rate limiting**
   - Limit messages per user per minute
   - Prevent spam/DoS attacks

3. **Content sanitization**
   - Sanitize message content (XSS protection)
   - Validate message length

4. **Authorization checks**
   - Verify user is conversation participant
   - Check if user is blocked

---

## 📡 Message Broadcasting Patterns

### Pattern 1: User-to-User (Private)

```java
// Send to specific user
messagingTemplate.convertAndSendToUser(
    recipientUserId.toString(),
    "/queue/messages",
    message
);

// Client subscribes to:
// /user/{userId}/queue/messages
```

**Use Case**: 1-on-1 chat messages

### Pattern 2: Broadcast (Public)

```java
// Broadcast to all subscribers
messagingTemplate.convertAndSend(
    "/topic/conversation/" + conversationId,
    message
);

// All participants subscribe to:
// /topic/conversation/{conversationId}
```

**Use Case**: Group chats, notifications

### Pattern 3: Session-Specific

```java
// Send to specific session
messagingTemplate.convertAndSendToSession(
    sessionId,
    "/queue/private",
    message
);
```

**Use Case**: Multi-device handling

---

## 💻 Client Integration

### JavaScript/TypeScript Client

```javascript
// 1. Import libraries
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// 2. Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws?token=' + accessToken);
const stompClient = Stomp.over(socket);

// 3. Connect
stompClient.connect({}, 
    // Success callback
    (frame) => {
        console.log('Connected:', frame);
        
        // 4. Subscribe to receive messages
        stompClient.subscribe('/user/queue/messages', (message) => {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
    },
    // Error callback
    (error) => {
        console.error('Connection error:', error);
        
        // Retry connection
        setTimeout(() => reconnect(), 5000);
    }
);

// 5. Send message (via REST API recommended)
async function sendMessage(conversationId, content) {
    const response = await fetch(`/api/conversations/${conversationId}/messages`, {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + accessToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ content })
    });
    
    // Message saved to DB and broadcast via WebSocket
    // Recipient receives via WebSocket subscription
}

// Alternative: Send via WebSocket directly
function sendViaWebSocket(conversationId, content) {
    stompClient.send(
        `/app/conversations/${conversationId}/send`,
        {},
        JSON.stringify({ content })
    );
}
```

### Connection States

```javascript
const ConnectionState = {
    CONNECTING: 'connecting',
    CONNECTED: 'connected',
    DISCONNECTED: 'disconnected',
    RECONNECTING: 'reconnecting'
};

let connectionState = ConnectionState.DISCONNECTED;

function updateConnectionStatus(state) {
    connectionState = state;
    
    // Update UI
    document.getElementById('status').textContent = state;
    
    // Enable/disable send button
    document.getElementById('sendBtn').disabled = 
        state !== ConnectionState.CONNECTED;
}
```

---

## 🧪 Testing

### Testing WebSocket Connections

**Tool Options:**
1. **Browser DevTools** - Network tab shows WebSocket frames
2. **Postman** - Supports WebSocket testing
3. **wscat** - Command-line WebSocket client
4. **Custom HTML page** - Quick test interface

### Test Scenarios

```javascript
// Test 1: Connection
✓ Connect with valid JWT
✗ Connect with invalid JWT
✗ Connect without JWT

// Test 2: Subscription
✓ Subscribe to own messages
✗ Subscribe to other user's private queue

// Test 3: Message Sending
✓ Send message to conversation you're in
✗ Send message to conversation you're not in
✓ Receive message broadcast

// Test 4: Reconnection
✓ Auto-reconnect after disconnect
✓ Re-subscribe after reconnect

// Test 5: Multiple Tabs
✓ Open same user in 2 tabs
✓ Both tabs receive message
```

### Load Testing

```bash
# Test with multiple concurrent connections
npm install -g artillery

# artillery.yml
config:
  target: "ws://localhost:8080"
  phases:
    - duration: 60
      arrivalRate: 10  # 10 connections/sec

scenarios:
  - name: "Connect and send messages"
    engine: "socketio"
    flow:
      - emit:
          channel: "/app/send"
          data:
            conversationId: 1
            content: "Test message"
```

---

## 🚀 Production Considerations

### 1. Scalability

**Problem**: WebSocket connections are stateful (sticky to one server)

**Solutions:**

**Option A: Sticky Sessions**
```
Load Balancer
     │
     ├─> Server 1 (User A connected)
     └─> Server 2 (User B connected)

Problem: If User A sends to User B, 
         message is on Server 1 but
         User B connected to Server 2
```

**Option B: Message Broker (Recommended)**
```
Server 1 ──┐
           ├─> RabbitMQ/Redis ←─┤
Server 2 ──┘                    │
                                │
         All servers subscribe to broker
         Messages distributed to all servers
```

```java
// Use external broker instead of simple broker
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/topic", "/user")
            .setRelayHost("rabbitmq.example.com")
            .setRelayPort(61613);
}
```

### 2. Connection Management

```java
@Component
public class WebSocketEventListener {
    
    // Track connected users
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        String userId = extractUserId(event);
        String sessionId = event.getMessage().getSessionId();
        
        activeSessions.put(sessionId, userId);
        
        // Notify user came online
        updateUserStatus(userId, true);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = activeSessions.remove(sessionId);
        
        // Notify user went offline
        updateUserStatus(userId, false);
    }
}
```

### 3. Monitoring

**Metrics to Track:**
- Active WebSocket connections
- Messages sent/received per second
- Connection errors/timeouts
- Average message latency

```java
@Bean
public WebSocketMessageBrokerStats webSocketMessageBrokerStats() {
    // Spring Boot Actuator endpoint: /actuator/websocket
    return new WebSocketMessageBrokerStats();
}
```

### 4. Heartbeat/Ping-Pong

```java
@Override
public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration.setMessageSizeLimit(64 * 1024)     // 64KB max message
                .setSendBufferSizeLimit(512 * 1024)  // 512KB buffer
                .setSendTimeLimit(20000)             // 20 sec timeout
                .setTimeToFirstMessage(30000);       // 30 sec handshake
}
```

```javascript
// Client-side heartbeat
setInterval(() => {
    if (stompClient.connected) {
        stompClient.send('/app/heartbeat', {}, '');
    }
}, 30000); // Every 30 seconds
```

---

## 📊 Comparison: WebSocket vs REST

| Feature | WebSocket | REST API |
|---------|-----------|----------|
| **Real-time** | ✅ Yes | ❌ No (polling needed) |
| **Bidirectional** | ✅ Yes | ❌ No (request-response) |
| **Connection** | Persistent | Per-request |
| **Overhead** | Low (after handshake) | High (per request) |
| **Scaling** | Complex (stateful) | Easy (stateless) |
| **Mobile Friendly** | ⚠️ Battery drain | ✅ Better for battery |
| **Caching** | ❌ Not supported | ✅ HTTP caching |
| **Load Balancing** | Sticky sessions needed | Easy |

**Recommended Approach**: **Hybrid**
- WebSocket for real-time delivery
- REST for loading history and fallback

---

## 🎯 Best Practices

1. ✅ **Always provide REST fallback** - Mobile, offline, WebSocket failures
2. ✅ **Authenticate every action** - Don't trust WebSocket session
3. ✅ **Use cursor pagination** - Efficient for loading history
4. ✅ **Implement reconnection** - Auto-reconnect with exponential backoff
5. ✅ **Rate limit** - Prevent spam and abuse
6. ✅ **Monitor connections** - Track active connections and errors
7. ✅ **Use external broker** - For multi-server deployments
8. ✅ **Handle offline scenarios** - Queue messages when recipient offline
9. ✅ **Implement heartbeat** - Detect dead connections
10. ✅ **Test thoroughly** - Connection failures, network issues, load

---

## 📚 Resources

- [Spring WebSocket Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol](https://stomp.github.io/)
- [SockJS Client](https://github.com/sockjs/sockjs-client)
- [STOMP.js](https://github.com/stomp-js/stompjs)

---

**Version**: 1.0  
**Last Updated**: 2025-11-22  
**Author**: AI Assistant
