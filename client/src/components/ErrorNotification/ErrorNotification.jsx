import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { X, AlertCircle } from 'lucide-react';
import './ErrorNotification.css';

// Global error notification state
let showErrorCallback = null;

export const showError = (error) => {
  if (showErrorCallback) {
    showErrorCallback(error);
  }
};

const ErrorNotification = () => {
  const { i18n } = useTranslation();
  const [errors, setErrors] = useState([]);
  const [nextId, setNextId] = useState(0);

  useEffect(() => {
    showErrorCallback = (error) => {
      const id = nextId;
      setNextId(prev => prev + 1);
      
      const currentLang = i18n.language || 'ar';
      
      // Get the appropriate message based on language
      let message = '';
      
      // Try to get message from errorJson first (most accurate)
      if (error.errorJson) {
        const messageAr = error.errorJson.messageAr || error.errorJson.message_ar;
        const messageEn = error.errorJson.messageEn || error.errorJson.message_en;
        
        if (currentLang === 'ar' && messageAr) {
          message = messageAr;
        } else if (currentLang === 'en' && messageEn) {
          message = messageEn;
        }
      }
      
      // If not found, try getLocalizedMessage method
      if (!message && error.getLocalizedMessage) {
        message = error.getLocalizedMessage(currentLang);
      }
      
      // Try direct properties
      if (!message) {
        if (error.messageAr && currentLang === 'ar') {
          message = error.messageAr;
        } else if (error.messageEn && currentLang === 'en') {
          message = error.messageEn;
        }
      }
      
      // Try error.message as fallback
      if (!message && error.message) {
        message = error.message;
      }
      
      // Final fallback message
      if (!message) {
        message = currentLang === 'ar' 
          ? 'عذراً، حدث خطأ غير متوقع. يرجى محاولة تحديث الصفحة أو التواصل مع الدعم الفني إذا استمرت المشكلة.'
          : 'An unexpected error occurred. Please try refreshing the page or contact support if the issue persists.';
      }
      
      const newError = {
        id,
        message,
        timestamp: Date.now()
      };
      
      setErrors(prev => [...prev, newError]);
      
      // Auto-dismiss after 7 seconds
      setTimeout(() => {
        dismissError(id);
      }, 7000);
    };
    
    return () => {
      showErrorCallback = null;
    };
  }, [nextId, i18n.language]);

  const dismissError = (id) => {
    setErrors(prev => prev.filter(err => err.id !== id));
  };

  if (errors.length === 0) return null;

  return (
    <div className="error-notification-container">
      {errors.map((error) => (
        <div key={error.id} className="error-notification" role="alert">
          <div className="error-notification-content">
            <div className="error-icon-wrapper">
              <AlertCircle size={24} />
            </div>
            <div className="error-message">
              {error.message}
            </div>
            <button
              className="error-close-btn"
              onClick={() => dismissError(error.id)}
              aria-label="Close"
            >
              <X size={20} />
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ErrorNotification;
