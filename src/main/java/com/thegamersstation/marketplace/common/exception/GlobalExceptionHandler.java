package com.thegamersstation.marketplace.common.exception;

import com.thegamersstation.marketplace.common.util.LocalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final LocalizationService localizationService;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", "المحتوى المطلوب غير موجود");
        problemDetail.setProperty("messageEn", "The requested resource was not found");
        return problemDetail;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        // Use exception's bilingual messages if available, otherwise use defaults
        String messageEn = ex.getMessageEn() != null ? ex.getMessageEn() : ex.getMessage();
        String messageAr = ex.getMessageAr() != null ? ex.getMessageAr() : "حدث خطأ في معالجة الطلب";
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                localizationService.get(messageEn, messageAr)
        );
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/business-rule"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", messageAr);
        problemDetail.setProperty("messageEn", messageEn);
        return problemDetail;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(RateLimitExceededException ex) {
        Long retryAfter = ex.getRetryAfterSeconds();
        String detailEn = retryAfter != null 
            ? String.format("Please wait %d seconds before requesting another OTP", retryAfter)
            : "Too many requests. Please try again later";
        String detailAr = retryAfter != null
            ? String.format("يرجى الانتظار %d ثانية قبل طلب رمز تحقق آخر", retryAfter)
            : "عدد كبير من الطلبات. يرجى المحاولة لاحقاً";
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                localizationService.get(detailEn, detailAr)
        );
        problemDetail.setTitle("Rate Limit Exceeded");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/rate-limit"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", detailAr);
        problemDetail.setProperty("messageEn", detailEn);
        if (retryAfter != null) {
            problemDetail.setProperty("retryAfter", retryAfter);
        }
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                localizationService.get(
                    "Validation failed for one or more fields",
                    "فشل التحقق من صحة حقل أو أكثر"
                )
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", "فشل التحقق من صحة حقل أو أكثر");
        problemDetail.setProperty("messageEn", "Validation failed for one or more fields");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                localizationService.get(
                    "You don't have permission to access this resource",
                    "ليس لديك صلاحية للوصول إلى هذا المحتوى"
                )
        );
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/forbidden"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", "ليس لديك صلاحية للوصول إلى هذا المحتوى");
        problemDetail.setProperty("messageEn", "You don't have permission to access this resource");
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                localizationService.get(
                    "Invalid credentials",
                    "بيانات الاعتماد غير صحيحة"
                )
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/unauthorized"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", "بيانات الاعتماد غير صحيحة");
        problemDetail.setProperty("messageEn", "Invalid credentials");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                localizationService.get(
                    "An unexpected error occurred",
                    "حدث خطأ غير متوقع"
                )
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.gamersstation.com/errors/internal"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("messageAr", "حدث خطأ غير متوقع. يرجى المحاولة لاحقاً");
        problemDetail.setProperty("messageEn", "An unexpected error occurred. Please try again later");
        return problemDetail;
    }
}
