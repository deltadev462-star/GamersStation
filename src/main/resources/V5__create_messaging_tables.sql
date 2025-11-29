-- =====================================================
-- Messaging System Database Schema
-- =====================================================
-- Supports 1-on-1 conversations between buyers and sellers
-- Features: real-time messaging, read receipts, typing indicators
-- =====================================================

-- -----------------------------------------------------
-- Table: conversations
-- -----------------------------------------------------
-- Represents a chat thread between two users about a specific post
-- Business Rules:
-- - One conversation per (post_id, buyer_id, seller_id) combination
-- - Seller is always the post owner
-- - Buyer is the user interested in the post
-- -----------------------------------------------------
CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Relationship to post being discussed
    post_id BIGINT NOT NULL,
    
    -- Participants (seller = post owner, buyer = interested user)
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    
    -- Conversation metadata
    last_message_at TIMESTAMP NULL,
    last_message_preview VARCHAR(200) NULL COMMENT 'Cached preview of last message for listing performance',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_conversations_post 
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_seller 
        FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_buyer 
        FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Ensure one conversation per post + buyer + seller combination
    CONSTRAINT uk_conversation_unique 
        UNIQUE KEY (post_id, seller_id, buyer_id),
    
    -- Performance indexes
    INDEX idx_conversations_seller (seller_id, updated_at DESC),
    INDEX idx_conversations_buyer (buyer_id, updated_at DESC),
    INDEX idx_conversations_post (post_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chat conversations between buyers and sellers about posts';


-- -----------------------------------------------------
-- Table: messages
-- -----------------------------------------------------
-- Individual messages within conversations
-- Features: text content, read receipts, soft delete
-- -----------------------------------------------------
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Relationship to conversation
    conversation_id BIGINT NOT NULL,
    
    -- Message sender
    sender_id BIGINT NOT NULL,
    
    -- Message content
    content TEXT NOT NULL COMMENT 'Message text content (max ~65KB)',
    
    -- Message metadata
    message_type ENUM('TEXT', 'SYSTEM') DEFAULT 'TEXT' 
        COMMENT 'TEXT=normal message, SYSTEM=automated message',
    
    -- Read receipt
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    
    -- Soft delete (allow users to delete messages from their view)
    deleted_by_sender BOOLEAN DEFAULT FALSE,
    deleted_by_recipient BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_messages_conversation 
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Performance indexes
    INDEX idx_messages_conversation_created (conversation_id, created_at DESC),
    INDEX idx_messages_unread (conversation_id, is_read, sender_id),
    INDEX idx_messages_sender (sender_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Individual messages within conversations';



-- -----------------------------------------------------
-- Table: conversation_participants_status
-- -----------------------------------------------------
-- Tracks per-user conversation state (typing, last seen, muted, etc.)
-- Enables features like "User is typing..." and last read position
-- -----------------------------------------------------
CREATE TABLE conversation_participants_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Relationship
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- User status in this conversation
    is_muted BOOLEAN DEFAULT FALSE COMMENT 'User has muted notifications for this conversation',
    is_archived BOOLEAN DEFAULT FALSE COMMENT 'User has archived this conversation',
    is_blocked BOOLEAN DEFAULT FALSE COMMENT 'User has blocked the other participant',
    
    -- Last activity tracking
    last_read_message_id BIGINT NULL COMMENT 'Last message read by this user',
    last_seen_at TIMESTAMP NULL COMMENT 'Last time user viewed this conversation',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_participant_status_conversation 
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_status_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_status_last_message 
        FOREIGN KEY (last_read_message_id) REFERENCES messages(id) ON DELETE SET NULL,
    
    -- Ensure one status record per user per conversation
    CONSTRAINT uk_participant_status 
        UNIQUE KEY (conversation_id, user_id),
    
    -- Performance indexes
    INDEX idx_participant_unread (user_id, last_read_message_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Per-user status and preferences for conversations';


-- -----------------------------------------------------
-- Table: message_reports (Optional - for moderation)
-- -----------------------------------------------------
-- Allows users to report inappropriate messages
-- -----------------------------------------------------
CREATE TABLE message_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- What is being reported
    message_id BIGINT NOT NULL,
    
    -- Who is reporting
    reported_by_user_id BIGINT NOT NULL,
    
    -- Report details
    reason ENUM('SPAM', 'HARASSMENT', 'INAPPROPRIATE', 'SCAM', 'OTHER') NOT NULL,
    description TEXT NULL COMMENT 'Optional additional details',
    
    -- Moderation status
    status ENUM('PENDING', 'REVIEWED', 'ACTION_TAKEN', 'DISMISSED') DEFAULT 'PENDING',
    reviewed_by_admin_id BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    admin_notes TEXT NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_report_message 
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_user 
        FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_admin 
        FOREIGN KEY (reviewed_by_admin_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Prevent duplicate reports
    CONSTRAINT uk_report_unique 
        UNIQUE KEY (message_id, reported_by_user_id),
    
    -- Performance indexes
    INDEX idx_reports_status (status, created_at DESC),
    INDEX idx_reports_user (reported_by_user_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User reports for inappropriate messages';


-- =====================================================
-- Performance Optimization Views (Optional)
-- =====================================================

-- View: unread_messages_count per user
CREATE OR REPLACE VIEW v_unread_message_counts AS
SELECT 
    c.id AS conversation_id,
    u.id AS user_id,
    COUNT(m.id) AS unread_count
FROM conversations c
CROSS JOIN (SELECT DISTINCT seller_id AS id FROM conversations 
            UNION SELECT DISTINCT buyer_id FROM conversations) u
LEFT JOIN messages m ON m.conversation_id = c.id 
    AND m.is_read = FALSE 
    AND m.sender_id != u.id
WHERE (c.seller_id = u.id OR c.buyer_id = u.id)
GROUP BY c.id, u.id;


-- =====================================================
-- Sample Queries for Common Operations
-- =====================================================

-- Get all conversations for a user (with unread count)
-- SELECT c.*, 
--        COUNT(CASE WHEN m.is_read = FALSE AND m.sender_id != ? THEN 1 END) as unread_count,
--        c.last_message_preview
-- FROM conversations c
-- LEFT JOIN messages m ON m.conversation_id = c.id
-- WHERE c.seller_id = ? OR c.buyer_id = ?
-- GROUP BY c.id
-- ORDER BY c.updated_at DESC;

-- Get messages in a conversation (with pagination)
-- SELECT m.*, u.username as sender_username
-- FROM messages m
-- JOIN users u ON u.id = m.sender_id
-- WHERE m.conversation_id = ?
--   AND m.id < ? -- cursor for pagination
-- ORDER BY m.created_at DESC
-- LIMIT 20;

-- Mark all messages in conversation as read
-- UPDATE messages 
-- SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
-- WHERE conversation_id = ? 
--   AND sender_id != ? 
--   AND is_read = FALSE;

-- Get or create conversation
-- INSERT INTO conversations (post_id, seller_id, buyer_id)
-- VALUES (?, ?, ?)
-- ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id);
