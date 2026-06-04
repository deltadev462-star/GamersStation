import React, { useState } from 'react';
import {
  Mail,
  Phone,
  Send,
  MessageSquare,
  CheckCircle,
  User,
  MessageCircle,
  ArrowLeft
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import SEO from '../../components/SEO/SEO';
import './ContactPage.css';

const ContactPage = () => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    subject: '',
    message: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.name.trim()) {
      newErrors.name = t('pages.contact.nameRequired');
    }
    
    if (!formData.email.trim()) {
      newErrors.email = t('pages.contact.emailRequired');
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = t('pages.contact.emailInvalid');
    }
    
    if (!formData.subject.trim()) {
      newErrors.subject = t('pages.contact.subjectRequired');
    }
    
    if (!formData.message.trim()) {
      newErrors.message = t('pages.contact.messageRequired');
    } else if (formData.message.length < 10) {
      newErrors.message = t('pages.contact.messageTooShort');
    }
    
    return newErrors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validateForm();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    
    setIsSubmitting(true);
    
    // Simulate API call
    setTimeout(() => {
      setIsSubmitting(false);
      setSubmitSuccess(true);
      setFormData({
        name: '',
        email: '',
        phone: '',
        subject: '',
        message: ''
      });
      
      // Reset success message after 5 seconds
      setTimeout(() => {
        setSubmitSuccess(false);
      }, 5000);
    }, 2000);
  };

  return (
    <div className="contact-page">
      <SEO
        title={t('pages.contact.title', 'تواصل معنا')}
        description={t('pages.contact.subtitle', 'نحن هنا لمساعدتك')}
        keywords="اتصل بنا, دعم العملاء, خدمة العملاء, GamersStation, تواصل معنا"
        url="https://gamersstation.eg/contact"
      />
      <Header />
      
      {/* Hero Section */}
      <section className="contact-hero">
        <div className="hero-bg">
          <div className="hero-gradient"></div>
          <div className="hero-pattern"></div>
        </div>
        <div className="container">
          <div className="contact-content">
            <h1 className="contact-title">
               
              {t('pages.contact.title', 'تواصل معنا')}
            </h1>
            <p className="hero-subtitle">
              {t('pages.contact.subtitle', 'نحن هنا لمساعدتك. تواصل معنا في أي وقت وسنرد عليك في أسرع وقت ممكن')}
            </p>
          </div>
        </div>
      </section>

      {/* Contact Content */}
      <section className="contact-content">
        <div className="container">
          <div className="contact-grid">
            {/* Contact Form */}
            <div className="contact-form-wrapper">
              <div className="form-header">
                <h2>{t('pages.contact.sendMessage')}</h2>
                <p>{t('pages.contact.welcomeInquiries')}</p>
              </div>

              {submitSuccess && (
                <div className="success-message">
                  <CheckCircle size={20} />
                  <span>{t('pages.contact.successMessage')} {t('pages.contact.contactSoon')}</span>
                </div>
              )}

              <form className="contact-form" onSubmit={handleSubmit}>
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="name">
                      <User size={16} />
                      {t('pages.contact.fullName')}
                    </label>
                    <input
                      type="text"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      placeholder={t('pages.contact.fullName')}
                      className={errors.name ? 'error' : ''}
                    />
                    {errors.name && <span className="error-message">{errors.name}</span>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="email">
                      <Mail size={16} />
                      {t('pages.contact.email')}
                    </label>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder={t('pages.contact.emailPlaceholder')}
                      className={errors.email ? 'error' : ''}
                      dir="ltr"
                    />
                    {errors.email && <span className="error-message">{errors.email}</span>}
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="phone">
                      <Phone size={16} />
                      {t('pages.contact.phone')} ({t('common.optional')})
                    </label>
                    <input
                      type="tel"
                      id="phone"
                      name="phone"
                      value={formData.phone}
                      onChange={handleChange}
                      placeholder={t('pages.contact.phonePlaceholder')}
                      dir="ltr"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="subject">
                      <MessageCircle size={16} />
                      {t('pages.contact.subject')}
                    </label>
                    <select
                      id="subject"
                      name="subject"
                      value={formData.subject}
                      onChange={handleChange}
                      className={errors.subject ? 'error' : ''}
                    >
                      <option value="">{t('pages.contact.chooseSubject')}</option>
                      <option value="general">{t('pages.contact.generalInquiry')}</option>
                      <option value="order">{t('pages.contact.orders')}</option>
                      <option value="technical">{t('pages.contact.technicalSupport')}</option>
                      <option value="complaint">{t('pages.contact.complaint')}</option>
                      <option value="suggestion">{t('pages.contact.suggestion')}</option>
                      <option value="partnership">{t('pages.contact.partnership')}</option>
                    </select>
                    {errors.subject && <span className="error-message">{errors.subject}</span>}
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="message">
                    <MessageSquare size={16} />
                    {t('pages.contact.message')}
                  </label>
                  <textarea
                    id="message"
                    name="message"
                    value={formData.message}
                    onChange={handleChange}
                    placeholder={t('pages.contact.writeMessage')}
                    rows="6"
                    className={errors.message ? 'error' : ''}
                  />
                  {errors.message && <span className="error-message">{errors.message}</span>}
                  <span className="char-count">{formData.message.length} / 500</span>
                </div>

                <button 
                  type="submit" 
                  className="submit-btn"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <span className="spinner"></span>
                      {t('pages.contact.sending')}
                    </>
                  ) : (
                    <>
                      <Send size={18} />
                      {t('pages.contact.send')}
                      {/* <ArrowLeft size={18} className="arrow-icon" /> */}
                    </>
                  )}
                </button>
              </form>
            </div>

            {/* Contact Info */}
            <div className="contact-info-wrapper">
              {/* Quick Contact */}
              <div className="info-card">
                <h3>{t('pages.contact.contactInfo')}</h3>
                <div className="info-items">
                  <div className="info-item">
                    <div className="info-icon-box">
                      <Mail size={18} />
                    </div>
                    <div className="info-content">
                      <span className="info-label">{t('pages.contact.email')}</span>
                      <a href="mailto:contact@thegamersstation.com" dir="ltr">contact@thegamersstation.com</a>
                    </div>
                  </div>

                  <div className="info-item">
                    <div className="info-icon-box">
                      <Phone size={18} />
                    </div>
                    <div className="info-content">
                      <span className="info-label">{t('pages.contact.phone')}</span>
                      <a href="tel:+966509551004" dir="ltr">+966 50 955 1004</a>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default ContactPage;