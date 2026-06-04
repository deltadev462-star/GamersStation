package com.thegamersstation.marketplace.common.util;

/**
 * General-purpose string utility methods.
 */
public final class StringUtil {

    private StringUtil() {
        // Utility class
    }

    /**
     * Truncate a message to the specified max length, appending "..." if truncated.
     */
    public static String truncate(String message, int maxLength) {
        if (message == null) return null;
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Truncate a message to 200 characters (default for message previews).
     */
    public static String truncatePreview(String message) {
        return truncate(message, 200);
    }
}
