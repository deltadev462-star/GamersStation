package com.thegamersstation.marketplace.common.util;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ContentSanitizer {

    // Pattern to detect control characters (except newline, tab, carriage return)
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\n\r\t]]");

    /**
     * Pattern to detect Saudi phone numbers in various formats:
     * +966XXXXXXXXX, 966XXXXXXXXX, 05XXXXXXXX — with optional spaces or dashes.
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?966[\\s\\-]?|0)([5-9])\\d[\\s\\-]?\\d{3}[\\s\\-]?\\d{4}"
    );

    /**
     * OWASP HTML Sanitizer policy: strips all HTML tags and attributes.
     * This is far more robust than regex-based stripping.
     */
    private static final PolicyFactory STRIP_ALL_HTML = new org.owasp.html.HtmlPolicyBuilder().toFactory();

    /**
     * Sanitizes user content using the OWASP HTML Sanitizer and removes control characters.
     */
    public String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        // Use OWASP sanitizer to strip all HTML (robust against encoding tricks)
        String sanitized = STRIP_ALL_HTML.sanitize(content);

        // Remove control characters except newline, tab, and carriage return
        sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll("");

        // Trim excessive whitespace
        sanitized = sanitized.trim().replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * Checks if content contains potentially dangerous patterns.
     */
    public boolean containsDangerousContent(String content) {
        if (content == null) {
            return false;
        }

        // Compare OWASP-sanitized output with original to detect stripped content
        String sanitized = STRIP_ALL_HTML.sanitize(content);
        return !sanitized.equals(content) || CONTROL_CHARS.matcher(content).find();
    }

    /**
     * Sanitizes content specifically for comments (stricter rules).
     */
    public String sanitizeComment(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        String sanitized = sanitize(content);

        // Additional restriction: limit consecutive special characters
        sanitized = sanitized.replaceAll("[!@#$%^&*()]{4,}", "***");

        return sanitized;
    }

    /**
     * Replaces any phone numbers found in the content with *******.
     * Covers Saudi formats: +966XXXXXXXXX, 966XXXXXXXXX, 05XXXXXXXX
     * with optional spaces or dashes between digit groups.
     */
    public String maskPhoneNumbers(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        return PHONE_PATTERN.matcher(content).replaceAll("*******");
    }

    /**
     * Escapes special characters for safe display.
     */
    public String escape(String content) {
        if (content == null) {
            return null;
        }

        return content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}
