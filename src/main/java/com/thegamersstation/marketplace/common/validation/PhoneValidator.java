package com.thegamersstation.marketplace.common.validation;

import java.util.regex.Pattern;

public class PhoneValidator {
    
    private static final Pattern SAUDI_PHONE_PATTERN = Pattern.compile("^\\+966[0-9]{9}$");
    
    /**
     * Validates if the phone number is in E.164 format and restricted to +966 (Saudi Arabia)
     */
    public static boolean isValid(String phoneNumber) {
        return phoneNumber != null && SAUDI_PHONE_PATTERN.matcher(phoneNumber).matches();
    }
    
    /**
     * Normalizes a phone number to E.164 format (+966...)
     * Handles inputs like: 0501234567, 501234567, +966501234567
     */
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        
        // Remove all whitespace and dashes
        phoneNumber = phoneNumber.replaceAll("[\\s-]", "");
        
        // If starts with +966, validate and return
        if (phoneNumber.startsWith("+966")) {
            return isValid(phoneNumber) ? phoneNumber : null;
        }
        
        // If starts with 966, add +
        if (phoneNumber.startsWith("966")) {
            String normalized = "+" + phoneNumber;
            return isValid(normalized) ? normalized : null;
        }
        
        // If starts with 0, replace with +966
        if (phoneNumber.startsWith("0")) {
            String normalized = "+966" + phoneNumber.substring(1);
            return isValid(normalized) ? normalized : null;
        }
        
        // If just 9 digits, prepend +966
        if (phoneNumber.matches("^[0-9]{9}$")) {
            String normalized = "+966" + phoneNumber;
            return isValid(normalized) ? normalized : null;
        }
        
        return null;
    }
    
    /**
     * Formats phone number for display (e.g., +966 50 123 4567)
     */
    public static String format(String phoneNumber) {
        if (!isValid(phoneNumber)) {
            return phoneNumber;
        }
        // +966 50 123 4567
        return phoneNumber.substring(0, 4) + " " + 
               phoneNumber.substring(4, 6) + " " + 
               phoneNumber.substring(6, 9) + " " + 
               phoneNumber.substring(9);
    }
}
