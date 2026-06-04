package com.thegamersstation.marketplace.notification;

import com.thegamersstation.marketplace.user.repository.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String fromAddress;

    @Value("${notification.mail.sender-name}")
    private String senderName;

    @Value("${notification.frontend-url}")
    private String frontendUrl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Sends an email notification for a new chat message.
     * Runs asynchronously so the message-send API response is not delayed.
     *
     * @param recipient       the user who should receive the email
     * @param senderUser      the user who sent the chat message
     * @param messagePreview  a truncated preview of the message content
     * @param conversationId  the conversation ID for deep-linking
     */
    @Async("emailTaskExecutor")
    public void sendNewMessageNotification(User recipient, User senderUser,
                                           String messagePreview, Long conversationId) {
        String recipientEmail = recipient.getEmail();

        // ── Mandatory email validation ──────────────────────────────────
        if (!isValidEmail(recipientEmail)) {
            log.debug("Skipping email notification for user {} — no valid email address",
                    recipient.getId());
            return;
        }

        String senderDisplayName = senderUser.getUsername() != null
                ? senderUser.getUsername()
                : "Someone";

        String conversationLink = frontendUrl + "/profile?tab=messages";

        String subject = "New message | رسالة جديدة";
        String body = buildEmailBody(senderDisplayName, messagePreview, conversationLink);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress, senderName);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML enabled

            mailSender.send(mimeMessage);
            log.info("Email notification sent to user {} for conversation {}",
                    recipient.getId(), conversationId);

        } catch (MailException e) {
            log.error("Failed to send email notification to user {} — mail server error: {}",
                    recipient.getId(), e.getMessage(), e);
        } catch (MessagingException e) {
            log.error("Failed to compose email notification for user {}: {}",
                    recipient.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email notification to user {}: {}",
                    recipient.getId(), e.getMessage(), e);
        }
    }

    /**
     * Returns {@code true} only when the supplied string is a non-null,
     * non-blank, structurally valid email address.
     */
    private boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }

    private String buildEmailBody(String senderName, String messagePreview, String conversationLink) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <!-- English -->
                    <div style="direction: ltr; text-align: left;">
                        <h2 style="color: #333;"> New message | رسالة جديدة</h2>
                        <div style="background-color: #f5f5f5; padding: 16px; border-radius: 8px; margin: 16px 0;">
                            <p style="color: #555; margin: 0;">%1$s : %2$s</p>
                        </div>
                    </div>
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 24px 0;">
        
                    <div style="text-align: center; margin-top: 24px;">
                        <a href="%3$s"
                           style="display: inline-block; padding: 12px 24px; background-color: #007bff;
                                  color: #fff; text-decoration: none; border-radius: 6px;">
                            View Conversation | عرض المحادثة
                        </a>
                    </div>
                    <p style="color: #999; font-size: 12px; margin-top: 24px; text-align: center;">
                        You are receiving this email because you have a Gamers Station account.<br>
                        تصلك هذه الرسالة لأن لديك حساب في منصة محطة اللاعبين
                    </p>
                </div>
                """.formatted(escapeHtml(senderName), escapeHtml(messagePreview), conversationLink);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
