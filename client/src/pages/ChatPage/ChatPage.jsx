import React, { useState, useEffect, useRef, useCallback } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  ArrowLeft,
  Send,
  Check,
  CheckCheck,
  Clock,
  AlertCircle,
  File,
  Loader2,
  MessageCircle,
} from "lucide-react";
import Header from "../../components/Header/Header";
import messagingService from "../../services/messagingService";
import authService from "../../services/authService";
import { showError } from "../../components/ErrorNotification/ErrorNotification";
import "./ChatPage.css";

const ChatPage = () => {
  const { t, i18n } = useTranslation();
  const { chatId: conversationId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [chatInfo, setChatInfo] = useState(null);
  const [isTyping, setIsTyping] = useState(false);
  const [otherUserTyping, setOtherUserTyping] = useState(false);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);
  const [, setConnectionStatus] = useState(true); // track connection status updates only
  const [error, setError] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);

  // Get current user
  const currentUser = authService.getCurrentUser();

  // Auth guard: ensure user is logged in before accessing chat
  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate("/login", {
        state: { redirectTo: location.pathname + location.search },
      });
    }
  }, [navigate, location, conversationId]);

  // Scroll to bottom function
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Debounce function for marking messages as read
  const markAsReadTimeoutRef = useRef(null);
  const debouncedMarkAsRead = useCallback((convId) => {
    if (markAsReadTimeoutRef.current) {
      clearTimeout(markAsReadTimeoutRef.current);
    }
    markAsReadTimeoutRef.current = setTimeout(() => {
      messagingService.markMessagesAsRead(convId).catch(() => {});
    }, 1000); // Wait 1 second before marking as read
  }, []);

  // Initialize chat data - only run when conversationId changes
  useEffect(() => {
    if (!conversationId || isInitialized) return;
    
    let mounted = true;
    
    const initializeChat = async () => {
      try {
        setLoading(true);
        setError(null);

        // Load conversation data
        const conversation = await messagingService.getConversation(
          conversationId
        );
        
        if (!mounted) return;

        setChatInfo({
          conversationId: conversation.id,
          productId: conversation.post?.id,
          productName:
            conversation.post?.localizedTitle?.[i18n.language] ||
            conversation.post?.title,
          productImage: conversation.post?.images?.[0]?.url,
          sellerName: conversation.otherParticipant?.username,
          sellerId: conversation.otherParticipant?.id,
          sellerAvatar: conversation.otherParticipant?.profileImage,
          lastSeen: conversation.otherParticipant?.lastSeen,
          isOnline: conversation.otherParticipant?.isOnline,
          isCurrentUserSeller: conversation.isCurrentUserSeller,
          post: conversation.post,
          otherParticipant: conversation.otherParticipant,
        });

        // Load initial messages
        await loadMessages();

        // Mark as initialized
        setIsInitialized(true);

        // Mark messages as read with debouncing
        debouncedMarkAsRead(conversationId);

        // Mark conversation as seen once
        await messagingService.markConversationAsSeen(conversationId);
      } catch (err) {
        // Handle specific error cases
        if (err.message.includes("[403]")) {
          // Check if it's a token issue or a business rule violation
          if (
            err.message.includes("token") ||
            err.message.includes("expired")
          ) {
            authService.clearTokens();
            navigate("/login", { state: { redirectTo: location.pathname } });
            return;
          }
        } else if (err.message.includes("[401]")) {
          // Authentication issue - redirect to login
          authService.clearTokens();
          navigate("/login", { state: { redirectTo: location.pathname } });
          return;
        }

        setError(t("chat.loadError"));
        showError(err);
      } finally {
        setLoading(false);
      }
    };

    if (authService.isAuthenticated()) {
      initializeChat();
    }
    
    // Cleanup function
    return () => {
      mounted = false;
    };
  }, [conversationId, i18n.language]); // Only depend on conversationId and language
  
  // Reset initialization when conversation changes
  useEffect(() => {
    setIsInitialized(false);
    setMessages([]);
    setCursor(null);
    setHasMore(true);
  }, [conversationId]);

  // Prepare new chat info when navigating from a product page (no conversation yet)
  useEffect(() => {
    if (conversationId) return;

    if (location.state?.productId) {
      setChatInfo({
        productId: location.state.productId,
        productName: location.state.productName,
        sellerName: location.state.sellerName,
        sellerId: location.state.sellerId,
        isOnline: true,
      });
    }

    // Ensure loading spinner doesn't hang in empty/new chat state
    setLoading(false);
  }, [conversationId, location.state]);

  // Load messages function - not memoized to avoid dependency issues
  const loadMessages = async (loadMore = false, currentCursor = null) => {
    try {
      if (!conversationId) return;
      
      if (loadMore) {
        setLoadingMore(true);
      }

      const response = await messagingService.getMessages(
        conversationId,
        loadMore ? (currentCursor || cursor) : null
      );

      if (loadMore) {
        setMessages((prev) => [...response.messages, ...prev]);
      } else {
        setMessages(response.messages);
      }

      setHasMore(response.hasMore);
      setCursor(response.nextCursor);
    } catch (err) {
      console.error('Error loading messages:', err);
      showError(err);
    } finally {
      setLoadingMore(false);
    }
  };

  // Set up WebSocket subscriptions
  useEffect(() => {
    if (!conversationId) return;

    let unsubscribeConvMessages;
    let unsubscribeReceipts;
    let unsubscribeTyping;
    let unsubscribeStatus;

    const setupSubscriptions = async () => {
      try {
        // Subscribe to per-conversation messages topic to update both sides instantly
        unsubscribeConvMessages =
          await messagingService.subscribeToConversationMessages(
            conversationId,
            (messageData) => {
              // Normalize ownership relative to current user so incoming messages render correctly
              const senderId =
                messageData?.sender?.id ?? messageData?.sender?.userId;
              const myId = currentUser?.userId ?? currentUser?.id;
              const normalized = {
                ...messageData,
                // use loose equality to tolerate string/number mismatch across platforms
                isOwnMessage: senderId == myId,
              };

              // Dedupe by id to avoid double-add when sender also receives topic echo
              setMessages((prev) => {
                if (prev.some((m) => m.id === normalized.id)) return prev;
                return [...prev, normalized];
              });

              // Mark as read with debouncing if not own message
              if (!normalized.isOwnMessage) {
                debouncedMarkAsRead(conversationId);
              }
            }
          );

        // Subscribe to read receipts
        unsubscribeReceipts = await messagingService.subscribeToReadReceipts(
          (receiptData) => {
            if (receiptData.conversationId === parseInt(conversationId)) {
              // Update message statuses
              setMessages((prev) =>
                prev.map((msg) => {
                  if (msg.isOwnMessage && !msg.isRead) {
                    return {
                      ...msg,
                      isRead: true,
                      readAt: receiptData.readAt,
                      status: "read",
                    };
                  }
                  return msg;
                })
              );
            }
          }
        );

        // Subscribe to typing status
        unsubscribeTyping = await messagingService.subscribeToTypingStatus(
          conversationId,
          (typingData) => {
            if (typingData.userId !== currentUser?.userId) {
              setOtherUserTyping(typingData.typing);
            }
          }
        );

        // Connection status
        unsubscribeStatus =
          messagingService.onConnectionStatusChange(setConnectionStatus);
      } catch (error) {
        console.error('Failed to setup subscriptions:', error);
        showError(error);
      }
    };

    setupSubscriptions();

    return () => {
      // Cleanup subscriptions
      if (typeof unsubscribeConvMessages === "function")
        unsubscribeConvMessages();
      if (typeof unsubscribeReceipts === "function") unsubscribeReceipts();
      if (typeof unsubscribeTyping === "function") unsubscribeTyping();
      if (typeof unsubscribeStatus === "function") unsubscribeStatus();
    };
  }, [conversationId, currentUser?.userId, debouncedMarkAsRead]);

  // Cleanup debounced mark as read on unmount
  useEffect(() => {
    return () => {
      if (markAsReadTimeoutRef.current) {
        clearTimeout(markAsReadTimeoutRef.current);
      }
    };
  }, []);

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (e) => {
    e.preventDefault();

    // Require authentication
    if (!authService.isAuthenticated()) {
      navigate("/login", {
        state: { redirectTo: location.pathname + location.search },
      });
      return;
    }

    if (!message.trim()) return;
    if (sending) return;

    setSending(true);
    setError(null);

    try {
      let activeConversationId = conversationId;

      // If no conversation exists, start one
      if (!activeConversationId) {
        const conversation = await messagingService.startConversation(
          chatInfo.productId,
          message
        );

        // Navigate to the new conversation
        navigate(`/chat/${conversation.id}`, { replace: true });
        return;
      }

      // Send message
      const sentMessage = await messagingService.sendMessage(
        activeConversationId,
        message
      );

      // Add to messages if not already received via WebSocket
      setMessages((prev) => {
        const exists = prev.some((msg) => msg.id === sentMessage.id);
        if (!exists) {
          return [...prev, sentMessage];
        }
        return prev;
      });

      setMessage("");

      // Stop typing indicator
      handleTypingStop();
    } catch (err) {
      // Handle specific error cases
      if (err.message.includes("[403]")) {
        // Authentication issue - redirect to login
        authService.clearTokens();
        navigate("/login", { state: { redirectTo: location.pathname } });
        return;
      } else if (
        err.message.includes("[400]") &&
        err.message.includes("yourself")
      ) {
        setError(t("chat.cannotMessageYourself"));
      } else if (err.message.includes("[404]")) {
        setError(t("chat.productNotFound") || "Product not found");
      } else {
        // Show the actual error message from backend if available
        const errorMatch = err.message.match(/\[\d+\]\s*(.+)/);
        setError(errorMatch ? errorMatch[1] : t("chat.sendError"));
      }
    } finally {
      setSending(false);
    }
  };

  const handleTyping = () => {
    if (!isTyping && conversationId) {
      setIsTyping(true);
      messagingService
        .sendTypingStatus(conversationId, true)
        .catch(() => {});
    }

    // Clear existing timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // Set new timeout
    typingTimeoutRef.current = setTimeout(() => {
      handleTypingStop();
    }, 3000);
  };

  const handleTypingStop = () => {
    if (isTyping && conversationId) {
      setIsTyping(false);
      messagingService
        .sendTypingStatus(conversationId, false)
        .catch(() => {});
    }

    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
  };

  const formatTime = (date) => {
    if (!date) return "";

    const messageDate = new Date(date);
    const now = new Date();
    const diffInHours = (now - messageDate) / (1000 * 60 * 60);

    if (diffInHours < 24) {
      return messageDate.toLocaleTimeString(
        i18n.language === "ar" ? "ar-SA" : "en-US",
        {
          hour: "2-digit",
          minute: "2-digit",
          timeZone: "Asia/Riyadh",
        }
      );
    } else {
      return messageDate.toLocaleDateString(
        i18n.language === "ar" ? "ar-SA" : "en-US",
        {
          day: "numeric",
          month: "short",
          timeZone: "Asia/Riyadh",
        }
      );
    }
  };


  const MessageStatus = ({ status, isRead }) => {
    if (status === "sending") {
      return <Clock size={14} className="message-status sending" />;
    } else if (isRead || status === "read") {
      return <CheckCheck size={14} className="message-status read" />;
    } else if (status === "sent") {
      return <Check size={14} className="message-status sent" />;
    } else if (status === "failed") {
      return <AlertCircle size={14} className="message-status failed" />;
    }
    return null;
  };

  if (loading) {
    return (
      <div className="chat-page">
        <Header />
        <div className="loading-container">
          <Loader2 className="spinner" size={48} />
          <p>{t("common.loading")}</p>
        </div>
      </div>
    );
  }

  // Show empty state when no conversation is selected
  if (!conversationId && !chatInfo) {
    return (
      <div className="chat-page">
        <Header />

        <div className="chat-empty-state">
          <div className="empty-state-content">
            <MessageCircle size={64} className="empty-state-icon" />
            <h2>{t("chat.noConversation")}</h2>
            <p>{t("chat.selectConversationOrStartNew")}</p>
            <button
              className="browse-products-btn"
              onClick={() => navigate("/products")}
            >
              {t("chat.browseProducts")}
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-page">
      <Header />

      <div className="chat-container">
        {/* Chat Header */}
        <div className="chat-header">
          <button className="chat-back-btn" onClick={() => navigate(-1)}>
            <ArrowLeft size={24} />
          </button>

          <div className="chat-header-left">
            <div className="chat-header">
              <div className="chat-user-details">
                <h3>{chatInfo?.sellerName}</h3>
              </div>
              <div className="chat-user-avatar">
                {chatInfo?.sellerAvatar ? (
                  <img src={chatInfo.sellerAvatar} alt={chatInfo.sellerName} />
                ) : (
                  <span>{chatInfo?.sellerName?.charAt(0) || "?"}</span>
                )}
              </div>

              
            </div>

            
          </div>
        </div>

        {/* Product Info Bar */}
        {chatInfo?.productName && (
          <div className="chat-product-info">
            <div className="product-info-content">
              {chatInfo?.productImage && (
                <img
                  src={chatInfo.productImage}
                  alt={chatInfo.productName}
                  className="product-thumbnail"
                />
              )}
              <span className="product-label">{t("chat.regarding")}:</span>
              <span className="product-name">{chatInfo.productName}</span>
            </div>
            <button
              className="view-product-btn"
              onClick={() => navigate(`/product/${chatInfo.productId}`)}
            >
              {t("chat.viewProduct")}
            </button>
          </div>
        )}

        {/* Messages Area */}
        <div className="chat-messages">
          <div className="messages-container">
            {/* Load more button */}
            {hasMore && !loadingMore && (
              <button
                className="load-more-btn"
                onClick={() => loadMessages(true, cursor)}
              >
                {t("chat.loadEarlierMessages")}
              </button>
            )}

            {loadingMore && (
              <div className="loading-more">
                <Loader2 className="spinner" size={20} />
              </div>
            )}

            {/* Error message */}
            {error && (
              <div className="chat-error">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {/* Messages */}
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`message ${msg.isOwnMessage ? "sent" : "received"}`}
              >
                <div className="message-content">
                  {msg.content && <p className="message-text">{msg.content}</p>}

                  {msg.attachments && msg.attachments.length > 0 && (
                    <div className="message-attachments">
                      {msg.attachments.map((att) => (
                        <div key={att.id} className="attachment-item">
                          {att.type === "image" ? (
                            <img src={att.preview} alt="Attachment" />
                          ) : (
                            <div className="file-attachment">
                              <File size={24} />
                              <span>{att.file.name}</span>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}

                  <div className="message-meta">
                    <span className="message-time">
                      {formatTime(msg.createdAt)}
                    </span>
                    {msg.isOwnMessage && (
                      <MessageStatus status={msg.status} isRead={msg.isRead} />
                    )}
                  </div>
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {otherUserTyping && (
              <div className="message received typing">
                <div className="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>
        </div>

        {/* Message Input */}
        <form className="chat-input-container" onSubmit={handleSendMessage}>
          <div className="chat-input-wrapper">
            <input
              type="text"
              className="message-input"
              placeholder={
                conversationId
                  ? t("chat.typeMessage")
                  : t("chat.typeFirstMessage")
              }
              value={message}
              onChange={(e) => {
                const value = e.target.value;
                // Limit to 250 characters
                if (value.length <= 250) {
                  setMessage(value);
                  handleTyping();
                }
              }}
              onBlur={handleTypingStop}
              disabled={sending}
              maxLength={250}
            />
            <span className="char-counter" style={{ 
              fontSize: '0.75rem', 
              color: message.length >= 250 ? '#ff4444' : 'rgba(255, 255, 255, 0.5)',
              position: 'absolute',
              bottom: '8px',
              right: '60px'
            }}>
              {message.length}/250
            </span>

            <button
              type="submit"
              className="send-btn"
              disabled={!message.trim() || sending}
            >
              {sending ? (
                <Loader2 size={20} className="spinner" />
              ) : (
                <Send size={20} />
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChatPage;
