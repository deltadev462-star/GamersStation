import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import './LanguageSwitcher.css';

const LanguageSwitcher = () => {
  const { i18n } = useTranslation();
  const [isChanging, setIsChanging] = useState(false);
  
  const toggleLanguage = () => {
    setIsChanging(true);
    const newLang = i18n.language === 'ar' ? 'en' : 'ar';
    i18n.changeLanguage(newLang);
    // Update HTML dir attribute
    document.documentElement.dir = newLang === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.lang = newLang;
    // Store in localStorage to persist
    localStorage.setItem('i18nextLng', newLang);
    
    // Remove animation class after animation completes
    setTimeout(() => setIsChanging(false), 500);
  };

  useEffect(() => {
    // Set initial direction based on current language
    const currentLang = i18n.language || 'ar';
    document.documentElement.dir = currentLang === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.lang = currentLang;
  }, [i18n.language]);

  return (
    <div className="language-toggle">
      <button
        className={`language-toggle-btn ${isChanging ? 'changing' : ''}`}
        onClick={toggleLanguage}
        aria-label="Toggle language"
      >
        {i18n.language === 'ar' ? (
          <span className="lang-option flag-option">
            <span className="flag-text">EN</span>
          </span>
        ) : (
          <span className="lang-option flag-option">
            <span className="flag-text">AR</span>
          </span>
        )}
      </button>
    </div>
  );
};

export default LanguageSwitcher;