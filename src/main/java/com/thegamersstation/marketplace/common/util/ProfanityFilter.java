package com.thegamersstation.marketplace.common.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ProfanityFilter {
    
    // Common profanity words (English and Arabic - sample list, expand as needed)
    private static final Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
        // English samples (add more as needed)
        "damn", "hell", "crap", "stupid", "idiot", "moron", "fool",
        
        // Arabic samples (transliterated - add actual Arabic words as needed)
        "kalb", "hmar", "khanzeer"
        
        // Note: In production, load from a configuration file or database
        // to make it easier to update without code changes
    ));
    
    private static final String MASK = "***";
    
    /**
     * Checks if the content contains profanity
     */
    public static boolean containsProfanity(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        
        String normalized = content.toLowerCase();
        
        for (String word : PROFANITY_WORDS) {
            // Word boundary pattern to match whole words only
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Filters profanity by replacing bad words with asterisks
     */
    public static String filter(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        
        String filtered = content;
        
        for (String word : PROFANITY_WORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            filtered = pattern.matcher(filtered).replaceAll(MASK);
        }
        
        return filtered;
    }
    
    /**
     * Validates content and throws exception if profanity is found
     */
    public static void validateNoProfanity(String content) {
        if (containsProfanity(content)) {
            throw new IllegalArgumentException("Content contains inappropriate language");
        }
    }
}
