package com.thegamersstation.marketplace.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * Utility class to hold bilingual content (English and Arabic)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedContent {
    private String en;
    private String ar;
    
    /**
     * Get localized value based on locale
     */
    public String get(Locale locale) {
        if (locale != null && "ar".equals(locale.getLanguage())) {
            return ar != null ? ar : en;
        }
        return en != null ? en : ar;
    }
    
    /**
     * Get localized value based on language code
     */
    public String get(String languageCode) {
        if ("ar".equalsIgnoreCase(languageCode)) {
            return ar != null ? ar : en;
        }
        return en != null ? en : ar;
    }
    
    /**
     * Create LocalizedContent from EN and AR strings
     */
    public static LocalizedContent of(String en, String ar) {
        return new LocalizedContent(en, ar);
    }
}
