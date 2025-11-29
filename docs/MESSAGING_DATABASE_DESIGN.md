# 💬 Messaging System - Database Design

## Overview

Simplified database schema for marketplace chat/messaging. Supports real-time 1-on-1 conversations between buyers and sellers about specific posts.

---

## 📊 Schema Summary

### **4 Core Tables:**

1. **`conversations`** - Chat threads between buyers/sellers
2. **`messages`** - Individual text messages
3. **`conversation_participants_status`** - User preferences (mute/block/archive)
4. **`message_reports`** - Message moderation system

### **Key Features:**
- ✅ 1-on-1 chats about specific posts
- ✅ Read receipts with timestamps
- ✅ Soft delete (per-user message deletion)
- ✅ Mute/Archive/Block functionality
- ✅ Message reporting for moderation
- ✅ Optimized indexes for performance
- ✅ Cursor-based pagination

---

## 📋 Table Details

### 1. **conversations**

Chat thread between buyer and seller about a post.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `post_id` | BIGINT | Post being discussed |
| `seller_id` | BIGINT | Post owner |
| `buyer_id` | BIGINT | Interested user |
| `last_message_at` | TIMESTAMP | Last message time |
| `last_message_preview` | VARCHAR(200) | Cached preview |
| `created_at` | TIMESTAMP | Created |
| `updated_at` | TIMESTAMP | Updated |

**Constraints:**
- `UNIQUE (post_id, seller_id, buyer_id)` - One conversation per combination

**Indexes:**
- `(seller_id, updated_at DESC)` - Seller's conversations
- `(buyer_id, updated_at DESC)` - Buyer's conversations
- `post_id` - Post's conversations

---

### 2. **messages**

Individual messages within conversations.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `conversation_id` | BIGINT | Parent conversation |
| `sender_id` | BIGINT | Message sender |
| `content` | TEXT | Message content (~65KB max) |
| `message_type` | ENUM | TEXT or SYSTEM |
| `is_read` | BOOLEAN | Read status |
| `read_at` | TIMESTAMP | Read timestamp |
| `deleted_by_sender` | BOOLEAN | Sender deleted |
| `deleted_by_recipient` | BOOLEAN | Recipient deleted |
| `created_at` | TIMESTAMP | Sent time |
| `updated_at` | TIMESTAMP | Updated |

**Message Types:**
- `TEXT` - Normal chat message
- `SYSTEM` - Automated message (e.g., "Post sold")

**Indexes:**
- `(conversation_id, created_at DESC)` - Pagination
- `(conversation_id, is_read, sender_id)` - Unread count
- `sender_id` - User's messages

---

### 3. **conversation_participants_status**

Per-user conversation preferences.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `conversation_id` | BIGINT | Conversation |
| `user_id` | BIGINT | User |
| `is_muted` | BOOLEAN | Notifications muted |
| `is_archived` | BOOLEAN | Archived |
| `is_blocked` | BOOLEAN | Other user blocked |
| `last_read_message_id` | BIGINT | Last read |
| `last_seen_at` | TIMESTAMP | Last viewed |
| `created_at` | TIMESTAMP | Created |
| `updated_at` | TIMESTAMP | Updated |

**Constraints:**
- `UNIQUE (conversation_id, user_id)`

**Use Cases:**
- Mute conversations
- Block users
- Track unread position

---

### 4. **message_reports**

Report inappropriate messages.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `message_id` | BIGINT | Reported message |
| `reported_by_user_id` | BIGINT | Reporter |
| `reason` | ENUM | SPAM/HARASSMENT/etc |
| `description` | TEXT | Details |
| `status` | ENUM | PENDING/REVIEWED/etc |
| `reviewed_by_admin_id` | BIGINT | Admin reviewer |
| `reviewed_at` | TIMESTAMP | Review time |
| `admin_notes` | TEXT | Admin notes |
| `created_at` | TIMESTAMP | Reported |
| `updated_at` | TIMESTAMP | Updated |

**Report Reasons:**
- SPAM, HARASSMENT, INAPPROPRIATE, SCAM, OTHER

**Report Statuses:**
- PENDING, REVIEWED, ACTION_TAKEN, DISMISSED

---

## 🔍 Common Queries

### Get User's Conversations

```sql
SELECT 
    c.*,
    CASE WHEN c.seller_id = ? THEN u_buyer.username ELSE u_seller.username END as other_user,
    p.title as post_title,
    COUNT(CASE WHEN m.is_read = FALSE AND m.sender_id != ? THEN 1 END) as unread_count
FROM conversations c
JOIN posts p ON p.id = c.post_id
JOIN users u_seller ON u_seller.id = c.seller_id
JOIN users u_buyer ON u_buyer.id = c.buyer_id
LEFT JOIN messages m ON m.conversation_id = c.id
WHERE c.seller_id = ? OR c.buyer_id = ?
GROUP BY c.id
ORDER BY c.last_message_at DESC NULLS LAST;
```

### Get Messages (Paginated)

```sql
SELECT m.*, u.username as sender_username
FROM messages m
JOIN users u ON u.id = m.sender_id
WHERE m.conversation_id = ?
  AND m.deleted_by_sender = FALSE
  AND m.deleted_by_recipient = FALSE
  AND (? IS NULL OR m.id < ?)  -- cursor
ORDER BY m.created_at DESC
LIMIT 20;
```

### Mark as Read

```sql
UPDATE messages 
SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
WHERE conversation_id = ? 
  AND sender_id != ?
  AND is_read = FALSE;
```

### Get or Create Conversation

```sql
INSERT INTO conversations (post_id, seller_id, buyer_id, last_message_at)
VALUES (?, ?, ?, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE 
    id = LAST_INSERT_ID(id),
    updated_at = CURRENT_TIMESTAMP;
```

---

## 🚀 Performance Tips

1. **Indexes** - All foreign keys indexed
2. **Caching** - `last_message_preview` avoids JOINs
3. **Pagination** - Cursor-based (more efficient than OFFSET)
4. **Soft Deletes** - Preserves data for other participant

---

## 🔐 Security

- Always verify user is participant (check `seller_id` OR `buyer_id`)
- Sanitize message content (XSS protection)
- Rate limit message sending
- Enable blocking to prevent harassment

---

## 🔄 Apply Migration

```bash
./mvnw flyway:migrate
```

Verify:
```sql
SHOW TABLES LIKE '%message%';
DESC conversations;
DESC messages;
```

---

## 📝 Future Enhancements

1. Group chats
2. Message reactions/emojis
3. Image attachments
4. Typing indicators
5. Message editing
6. Voice messages
7. Push notifications (FCM/APNS)

---

**Version**: 1.0  
**Last Updated**: 2025-11-22
