package com.thegamersstation.marketplace.messaging.repository;

import com.thegamersstation.marketplace.messaging.entity.Conversation;
import com.thegamersstation.marketplace.post.Post;
import com.thegamersstation.marketplace.user.repository.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c WHERE c.post = :post AND c.seller = :seller AND c.buyer = :buyer")
    Optional<Conversation> findByPostAndSellerAndBuyer(
        @Param("post") Post post,
        @Param("seller") User seller,
        @Param("buyer") User buyer
    );
    
    @Query(value = "SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.post p " +
           "LEFT JOIN FETCH c.seller s " +
           "LEFT JOIN FETCH c.buyer b " +
           "WHERE (c.seller.id = :userId OR c.buyer.id = :userId) " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST",
           countQuery = "SELECT COUNT(c) FROM Conversation c WHERE c.seller.id = :userId OR c.buyer.id = :userId")
    Page<Conversation> findByParticipantId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.post p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH c.seller s " +
           "LEFT JOIN FETCH c.buyer b " +
           "WHERE c.id = :conversationId AND (c.seller.id = :userId OR c.buyer.id = :userId)")
    Optional<Conversation> findByIdAndParticipantId(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId
    );
    
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.seller.id = :userId OR c.buyer.id = :userId")
    long countByParticipantId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId " +
           "AND m.isRead = false")
    long countUnreadMessages(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId
    );
    
    @Query("SELECT COUNT(DISTINCT m.conversation.id) FROM Message m " +
           "WHERE (m.conversation.seller.id = :userId OR m.conversation.buyer.id = :userId) " +
           "AND m.sender.id != :userId " +
           "AND m.isRead = false")
    long countConversationsWithUnreadMessages(@Param("userId") Long userId);
    
    /**
     * Count unread messages for multiple conversations in a single query.
     * Returns a list of [conversationId, unreadCount] pairs.
     */
    @Query("SELECT m.conversation.id, COUNT(m) FROM Message m " +
           "WHERE m.conversation.id IN :conversationIds " +
           "AND m.sender.id != :userId " +
           "AND m.isRead = false " +
           "GROUP BY m.conversation.id")
    List<Object[]> countUnreadMessagesByConversationIds(
        @Param("conversationIds") List<Long> conversationIds,
        @Param("userId") Long userId
    );

    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessageAt = :lastMessageAt, " +
           "c.lastMessagePreview = :preview WHERE c.id = :conversationId")
    void updateLastMessage(
        @Param("conversationId") Long conversationId,
        @Param("lastMessageAt") LocalDateTime lastMessageAt,
        @Param("preview") String preview
    );
    
    @Query("SELECT c FROM Conversation c WHERE c.post.id = :postId")
    Page<Conversation> findByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Conversation c " +
           "WHERE c.id = :conversationId AND (c.seller.id = :userId OR c.buyer.id = :userId)")
    boolean isParticipant(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
