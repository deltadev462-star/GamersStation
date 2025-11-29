package com.thegamersstation.marketplace.common.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final Long retryAfterSeconds;
    
    public RateLimitExceededException(String message, Long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
