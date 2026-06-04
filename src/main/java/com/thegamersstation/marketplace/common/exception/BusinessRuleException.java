package com.thegamersstation.marketplace.common.exception;

import lombok.Getter;

@Getter
public class BusinessRuleException extends RuntimeException {
    private final String messageAr;
    private final String messageEn;
    
    public BusinessRuleException(String message) {
        super(message);
        this.messageEn = message;
        this.messageAr = null;
    }
    
    public BusinessRuleException(String messageEn, String messageAr) {
        super(messageEn);
        this.messageEn = messageEn;
        this.messageAr = messageAr;
    }
}
