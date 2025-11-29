package com.thegamersstation.marketplace.common.util;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocalizationService {
    
    /**
     * Get localized content based on current locale
     */
    public String get(String enValue, String arValue) {
        Locale locale = LocaleContextHolder.getLocale();
        
        if (locale != null && "ar".equals(locale.getLanguage())) {
            return arValue != null ? arValue : enValue;
        }
        
        return enValue != null ? enValue : arValue;
    }
    
    /**
     * Get current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    
    /**
     * Check if current locale is Arabic
     */
    public boolean isArabic() {
        return "ar".equals(getCurrentLocale().getLanguage());
    }
}
