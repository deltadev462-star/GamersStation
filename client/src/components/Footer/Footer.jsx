import React, { useState, useEffect } from 'react';
import {
  Twitter,
  Instagram,
  Gamepad2,
  Shield,
  Truck,
  CreditCard,
  ChevronDown
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import LanguageSwitcher from '../LanguageSwitcher/LanguageSwitcher';
import './Footer.css';

const Footer = () => {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();
  const [isMobile, setIsMobile] = useState(false);
  const [collapsedSections, setCollapsedSections] = useState({
    customerService: false
  });

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 768);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);

    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  const toggleSection = (section) => {
    if (isMobile) {
      setCollapsedSections(prev => ({
        ...prev,
        [section]: !prev[section]
      }));
    }
  };

  return (
    <footer className="footer">
      {/*/!* Features Section *!/*/}
      {/*<div className="footer-features">*/}
      {/*  <div className="footer-container">*/}
      {/*    <div className="features-grid">*/}
      {/*      <div className="feature-item">*/}
      {/*        <Gamepad2 className="feature-icon" size={24} />*/}
      {/*        <h4>{t('footer.features.latestGames')}</h4>*/}
      {/*        <p>{t('footer.features.latestGamesDesc')}</p>*/}
      {/*      </div>*/}
      {/*      <div className="feature-item">*/}
      {/*        <Shield className="feature-icon" size={24} />*/}
      {/*        <h4>{t('footer.features.securePayment')}</h4>*/}
      {/*        <p>{t('footer.features.securePaymentDesc')}</p>*/}
      {/*      </div>*/}
      {/*      /!*<div className="feature-item">*!/*/}
      {/*      /!*  <Truck className="feature-icon" size={24} />*!/*/}
      {/*      /!*  <h4>{t('footer.features.fastShipping')}</h4>*!/*/}
      {/*      /!*  <p>{t('footer.features.fastShippingDesc')}</p>*!/*/}
      {/*      /!*</div>*!/*/}
      {/*      /!*<div className="feature-item">*!/*/}
      {/*      /!*  <CreditCard className="feature-icon" size={24} />*!/*/}
      {/*      /!*  <h4>{t('footer.features.multiplePayment')}</h4>*!/*/}
      {/*      /!*  <p>{t('footer.features.multiplePaymentDesc')}</p>*!/*/}
      {/*      /!*</div>*!/*/}
      {/*    </div>*/}
      {/*  </div>*/}
      {/*</div>*/}

      {/* Main Footer Content */}
      <div className="footer-main">
        <div className="footer-container">
          <div className="footer-content">
            {/* Company Info */}
            <div className="footer-section footer-about">
              <a href="/" className="footer-logo">
                <img src="/logo.svg" alt="GamersStation" className="footer-logo-icon" />
                <span className="footer-logo-text">GamersStation</span>
              </a>
              <p className="footer-description">
                {t('footer.about.description')}
              </p>
              <div className="social-links">
                <a href="https://www.tiktok.com/@gstationsa" className="social-link" aria-label="TikTok" target="_blank" rel="noopener noreferrer">
                  <svg width={18} height={18} viewBox="0 0 24 24" fill="currentColor">
                    <path d="M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-5.2 1.74 2.89 2.89 0 0 1 2.31-4.64 2.93 2.93 0 0 1 .88.13V9.4a6.37 6.37 0 0 0-1-.05A6.34 6.34 0 0 0 3 15.7a6.34 6.34 0 0 0 6.34 6.34 6.33 6.33 0 0 0 6.34-6.34V8.44a8.19 8.19 0 0 0 4.91 1.62V6.61a4.85 4.85 0 0 1-1-.08z"/>
                  </svg>
                </a>
                <a href="https://www.x.com/GstationSA" className="social-link" aria-label="Twitter" target="_blank" rel="noopener noreferrer">
                  <Twitter size={18} />
                </a>
                <a href="https://www.instagram.com/GstationSA" className="social-link" aria-label="Instagram" target="_blank" rel="noopener noreferrer">
                  <Instagram size={18} />
                </a>
              </div>
            </div>

            {/* Customer Service */}
            <div className="footer-section">
              <h3
                className={`footer-title ${isMobile ? 'mobile-toggle' : ''} ${collapsedSections.customerService ? 'collapsed' : ''}`}
                onClick={() => toggleSection('customerService')}
                style={isMobile ? { cursor: 'pointer' } : {}}
              >
                {t('footer.customerService.title')}
                {/* {isMobile && (
                  <ChevronDown
                    size={14}
                    style={{
                      transform: collapsedSections.customerService ? 'rotate(-90deg)' : 'rotate(0)',
                      transition: 'transform 0.3s ease'
                    }}
                  />
                )} */}
              </h3>
              <ul className={`footer-links ${isMobile && collapsedSections.customerService ? 'mobile-collapsed' : ''}`}>
                {/*<li><a href="/faq">{t('footer.customerService.faq')}</a></li>*/}
                <li><a href="/contact">{t('footer.customerService.contact')}</a></li>
                <li><a href="/privacy-policy">{t('footer.legal.privacy')}</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="footer-bottom">
        <div className="footer-container">
          <div className="footer-bottom-content">
            <p className="copyright">
              {t('footer.bottom.copyright', { year: currentYear })}
            </p>
            <LanguageSwitcher />
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;