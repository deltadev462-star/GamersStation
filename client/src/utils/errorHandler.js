// Global Error Handler with Bilingual Messages

// Default fallback error message (Arabic/English)
export const DEFAULT_ERROR_MESSAGE = {
  ar: 'عذراً، حدث خطأ غير متوقع. يرجى محاولة تحديث الصفحة أو التواصل مع الدعم الفني إذا استمرت المشكلة.',
  en: 'An unexpected error occurred. Please try refreshing the page or contact support if the issue persists.'
};

// Common error messages mapping
export const ERROR_MESSAGES = {
  // Network errors
  NETWORK_ERROR: {
    ar: 'خطأ في الاتصال بالشبكة. يرجى التحقق من اتصالك بالإنترنت.',
    en: 'Network connection error. Please check your internet connection.'
  },
  
  // Authentication errors
  UNAUTHORIZED: {
    ar: 'انتهت صلاحية جلستك. يرجى تسجيل الدخول مرة أخرى.',
    en: 'Your session has expired. Please log in again.'
  },
  FORBIDDEN: {
    ar: 'ليس لديك صلاحية للوصول إلى هذا المحتوى.',
    en: 'You do not have permission to access this content.'
  },
  
  // Client errors
  BAD_REQUEST: {
    ar: 'البيانات المدخلة غير صحيحة. يرجى التحقق من المعلومات المدخلة.',
    en: 'Invalid data provided. Please check your input.'
  },
  NOT_FOUND: {
    ar: 'المحتوى المطلوب غير موجود.',
    en: 'The requested content was not found.'
  },
  VALIDATION_ERROR: {
    ar: 'يرجى التحقق من صحة البيانات المدخلة.',
    en: 'Please check the validity of your input.'
  },
  
  // Server errors
  INTERNAL_SERVER_ERROR: {
    ar: 'حدث خطأ في الخادم. يرجى المحاولة لاحقاً.',
    en: 'A server error occurred. Please try again later.'
  },
  SERVICE_UNAVAILABLE: {
    ar: 'الخدمة غير متاحة حالياً. يرجى المحاولة لاحقاً.',
    en: 'Service is currently unavailable. Please try again later.'
  },
  TIMEOUT: {
    ar: 'انتهت مهلة الاتصال. يرجى المحاولة مرة أخرى.',
    en: 'Connection timeout. Please try again.'
  },
  
  // Specific business errors
  DUPLICATE_ENTRY: {
    ar: 'هذا المحتوى موجود بالفعل.',
    en: 'This content already exists.'
  },
  RESOURCE_LOCKED: {
    ar: 'هذا المحتوى مقفل حالياً. يرجى المحاولة لاحقاً.',
    en: 'This content is currently locked. Please try again later.'
  },
  RATE_LIMIT_EXCEEDED: {
    ar: 'عدد كبير من الطلبات. يرجى المحاولة لاحقاً.',
    en: 'Too many requests. Please try again later.'
  }
};

// Map HTTP status codes to error messages
export const getErrorMessageByStatus = (status) => {
  const errorMap = {
    400: ERROR_MESSAGES.BAD_REQUEST,
    401: ERROR_MESSAGES.UNAUTHORIZED,
    403: ERROR_MESSAGES.FORBIDDEN,
    404: ERROR_MESSAGES.NOT_FOUND,
    422: ERROR_MESSAGES.VALIDATION_ERROR,
    429: ERROR_MESSAGES.RATE_LIMIT_EXCEEDED,
    500: ERROR_MESSAGES.INTERNAL_SERVER_ERROR,
    502: ERROR_MESSAGES.SERVICE_UNAVAILABLE,
    503: ERROR_MESSAGES.SERVICE_UNAVAILABLE,
    504: ERROR_MESSAGES.TIMEOUT
  };
  
  return errorMap[status] || DEFAULT_ERROR_MESSAGE;
};

// Parse error response and extract bilingual message
export const parseErrorResponse = (error, errorJson = null) => {
  // If error has both ar and en fields, return them
  if (errorJson) {
    if (errorJson.messageAr && errorJson.messageEn) {
      return {
        ar: errorJson.messageAr,
        en: errorJson.messageEn
      };
    }
    
    if (errorJson.message_ar && errorJson.message_en) {
      return {
        ar: errorJson.message_ar,
        en: errorJson.message_en
      };
    }
    
    // If only one language is provided, try to detect which one
    if (errorJson.message) {
      const message = errorJson.message;
      // Simple Arabic detection (contains Arabic characters)
      const hasArabic = /[\u0600-\u06FF]/.test(message);
      
      if (hasArabic) {
        return {
          ar: message,
          en: DEFAULT_ERROR_MESSAGE.en
        };
      } else {
        return {
          ar: DEFAULT_ERROR_MESSAGE.ar,
          en: message
        };
      }
    }
    
    if (errorJson.detail) {
      const detail = errorJson.detail;
      const hasArabic = /[\u0600-\u06FF]/.test(detail);
      
      if (hasArabic) {
        return {
          ar: detail,
          en: DEFAULT_ERROR_MESSAGE.en
        };
      } else {
        return {
          ar: DEFAULT_ERROR_MESSAGE.ar,
          en: detail
        };
      }
    }
  }
  
  // Extract status code from error message if available
  const statusMatch = error.message?.match(/\[(\d+)\]/);
  const status = statusMatch ? parseInt(statusMatch[1]) : null;
  
  if (status) {
    return getErrorMessageByStatus(status);
  }
  
  // Check for network errors
  if (error.message?.includes('Failed to fetch') || 
      error.message?.includes('NetworkError') ||
      error.message?.includes('Network request failed')) {
    return ERROR_MESSAGES.NETWORK_ERROR;
  }
  
  // Default fallback
  return DEFAULT_ERROR_MESSAGE;
};

// Format error for display based on current language
export const getLocalizedError = (error, errorJson = null, language = 'ar') => {
  const messages = parseErrorResponse(error, errorJson);
  return messages[language] || messages.en || DEFAULT_ERROR_MESSAGE[language];
};

// Create a standardized error object
export class ApiError extends Error {
  constructor(message, status, messageAr = null, messageEn = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.messageAr = messageAr;
    this.messageEn = messageEn;
    this.timestamp = new Date().toISOString();
  }
  
  getLocalizedMessage(language = 'ar') {
    // First priority: Use the actual API response messages
    if (language === 'ar' && this.messageAr) {
      return this.messageAr;
    }
    if (language === 'en' && this.messageEn) {
      return this.messageEn;
    }
    
    // Second priority: Try to extract from errorJson if available
    if (this.errorJson) {
      const messageAr = this.errorJson.messageAr || this.errorJson.message_ar;
      const messageEn = this.errorJson.messageEn || this.errorJson.message_en;
      
      if (language === 'ar' && messageAr) {
        return messageAr;
      }
      if (language === 'en' && messageEn) {
        return messageEn;
      }
    }
    
    // Third priority: Use status code fallback messages
    const messages = getErrorMessageByStatus(this.status);
    return messages[language] || messages.en || this.message;
  }
  
  toJSON() {
    return {
      name: this.name,
      message: this.message,
      status: this.status,
      messageAr: this.messageAr,
      messageEn: this.messageEn,
      timestamp: this.timestamp
    };
  }
}

// Global error handler that can be used in components
export const handleApiError = (error, options = {}) => {
  const {
    showNotification = false,
    notificationHandler = null,
    language = 'ar',
    logToConsole = true
  } = options;
  
  // Log to console in development
  if (logToConsole && import.meta.env.DEV) {
    console.error('[API Error]', error);
  }
  
  // Get localized error message
  const errorJson = error.errorJson || null;
  const localizedMessage = getLocalizedError(error, errorJson, language);
  
  // Show notification if handler is provided
  if (showNotification && notificationHandler) {
    notificationHandler({
      type: 'error',
      message: localizedMessage,
      duration: 5000
    });
  }
  
  return {
    message: localizedMessage,
    messages: parseErrorResponse(error, errorJson),
    status: error.status,
    originalError: error
  };
};

// Export default error message for convenience
export default {
  DEFAULT_ERROR_MESSAGE,
  ERROR_MESSAGES,
  parseErrorResponse,
  getLocalizedError,
  handleApiError,
  ApiError
};
