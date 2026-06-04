import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { MessageSquare, Clock, Check, CheckCheck, User, Search } from 'lucide-react';
import messagingService from '../../services/messagingService';
import './MessagesTab.css';

const MessagesTab = ({ conversations, setConversations, loading, setLoading }) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  // Fetch conversations when component mounts
  useEffect(() => {
    let unsubscribe;
    
    const setup = async () => {
      fetchConversations();

      // Subscribe to new messages
      try {
        unsubscribe = await messagingService.subscribeToMessages((message) => {
          // Update the conversation list when a new message arrives
          setConversations(prevConversations => {
            return prevConversations.map(conv => {
              if (conv.id === message.conversationId) {
                return {
                  ...conv,
                  lastMessage: message,
                  lastMessageAt: message.createdAt,
                  unreadCount: conv.unreadCount + (message.isOwn ? 0 : 1)
                };
              }
              return conv;
            }).sort((a, b) => new Date(b.lastMessageAt) - new Date(a.lastMessageAt));
          });
        });
      } catch (error) {
        console.error('Failed to subscribe to messages:', error);
      }
    };
    
    setup();

    return () => {
      if (unsubscribe && typeof unsubscribe === 'function') {
        unsubscribe();
      }
    };
  }, []);

  const fetchConversations = async () => {
    try {
      setLoading(true);
      const response = await messagingService.getConversations();
      setConversations(response.content || []);
    } catch (error) {
      console.error('Error fetching conversations:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    
    // If less than 24 hours, show time
    if (diff < 24 * 60 * 60 * 1000) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Riyadh' });
    }
    
    // If less than a week, show day
    if (diff < 7 * 24 * 60 * 60 * 1000) {
      return date.toLocaleDateString([], { weekday: 'short', timeZone: 'Asia/Riyadh' });
    }
    
    // Otherwise show date
    return date.toLocaleDateString([], { month: 'short', day: 'numeric', timeZone: 'Asia/Riyadh' });
  };

  const handleConversationClick = (conversation) => {
    // Navigate to chat page with conversation ID
    navigate(`/chat/${conversation.id}`);
  };

  if (loading) {
    return (
      <div className="messages-loading">
        <div className="spinner"></div>
        <p>{t('chat.loadingConversations')}</p>
      </div>
    );
  }

  if (conversations.length === 0) {
    return (
      <div className="messages-empty">
        <MessageSquare size={48} />
        <p>{t('chat.noConversations')}</p>
      </div>
    );
  }

  return (
    <div className="messages-tab">
      <div className="messages-header">
        <div className="search-bar">
          <Search size={20} />
          <input 
            type="text" 
            placeholder={t('chat.searchMessages')}
            className="search-input"
          />
        </div>
      </div>

      <div className="conversations-list">
      {conversations.map(conversation => {
          // Determine who the other participant is
          const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
          const isUserSeller = conversation.seller?.id === currentUser?.userId;
          const otherParticipant = isUserSeller ? conversation.buyer : conversation.seller;

          return (
            <div 
              key={conversation.id} 
              className={`conversation-item ${conversation.unreadCount > 0 ? 'unread' : ''}`}
              onClick={() => handleConversationClick(conversation)}
            >
              <div className="conversation-avatar">
                {otherParticipant?.profileImage ? (
                  <img 
                    src={otherParticipant.profileImage} 
                    alt={otherParticipant.username}
                  />
                ) : (
                  <div className="avatar-placeholder">
                    <User size={24} />
                  </div>
                )}
              </div>

              <div className="conversation-content">
                <div className="conversation-header">
                  <h4 className="conversation-username">{otherParticipant?.username || t('chat.unknownUser')}</h4>
                  <div className="conversation-header-end">
                    <span className="conversation-time">
                      {formatTime(conversation.lastMessageAt)}
                    </span>
                    {conversation.unreadCount > 0 && (
                      <span className="unread-badge">{conversation.unreadCount}</span>
                    )}
                  </div>
                </div>

                <div className="conversation-preview">
                  <p className="last-message">
                    {conversation.lastMessage?.isOwn && (
                      <span className="message-prefix">{t('chat.you')}: </span>
                    )}
                    {conversation.lastMessage?.content || ''}
                  </p>

                  <div className="conversation-status">
                    {conversation.lastMessage?.isOwn && (
                      conversation.lastMessage?.readAt ? (
                        <CheckCheck size={16} className="read-icon" />
                      ) : (
                        <Check size={16} className="sent-icon" />
                      )
                    )}
                  </div>
                </div>

                {conversation.post && (
                  <div className="conversation-product">
                    <img 
                      src={conversation.post.images?.[0]?.url || '/placeholder.jpg'} 
                      alt={conversation.post.title}
                    />
                    <span>{conversation.post.title}</span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default MessagesTab;