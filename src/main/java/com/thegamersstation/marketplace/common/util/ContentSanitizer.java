package com.thegamersstation.marketplace.common.util;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ContentSanitizer {
    
    // Pattern to detect control characters (except newline, tab, carriage return)
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\n\r\t]]");
    
    // Pattern to detect HTML/Script tags
    private static final Pattern HTML_SCRIPT_PATTERN = Pattern.compile(
        "<\\s*script[^>]*>.*?</\\s*script\\s*>|<\\s*[^>]+\\s*>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    // Basic SQL injection patterns (additional security layer)
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('|(\\-\\-)|(;)|(\\|\\|)|(\\*))",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Sanitizes user content by removing dangerous characters and HTML/script tags
     */
    public String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        
        // Remove HTML and script tags
        String sanitized = HTML_SCRIPT_PATTERN.matcher(content).replaceAll("");
        
        // Remove control characters except newline, tab, and carriage return
        sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll("");
        
        // Trim excessive whitespace
        sanitized = sanitized.trim().replaceAll("\\s+", " ");
        
        return sanitized;
    }
    
    /**
     * Checks if content contains potentially dangerous patterns
     */
    public boolean containsDangerousContent(String content) {
        if (content == null) {
            return false;
        }
        
        return CONTROL_CHARS.matcher(content).find() || 
               HTML_SCRIPT_PATTERN.matcher(content).find();
    }
    
    /**
     * Sanitizes content specifically for comments (stricter rules)
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
     * Escapes special characters for safe display
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
