package com.thegamersstation.marketplace.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATEHYPHEN = Pattern.compile("-{2,}");
    
    /**
     * Generates a URL-friendly slug from the given text
     * Example: "Gaming Console - PS5!" becomes "gaming-console-ps5"
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.toLowerCase(Locale.ENGLISH);
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = DUPLICATEHYPHEN.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");
        
        return slug;
    }
    
    /**
     * Generates a unique slug by appending a number if needed
     */
    public static String toUniqueSlug(String input, int attempt) {
        String baseSlug = toSlug(input);
        return attempt > 0 ? baseSlug + "-" + attempt : baseSlug;
    }
}
