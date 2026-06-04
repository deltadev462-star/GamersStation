import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import FormInput from '../../components/FormInput/FormInput';
import LanguageSwitcher from '../../components/LanguageSwitcher/LanguageSwitcher';
import authService from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import { showError } from '../../components/ErrorNotification/ErrorNotification';
import './LoginPage.css';

const LoginPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, login } = useAuth();
  const [step, setStep] = useState('phone'); // 'phone' or 'otp'
  const [formData, setFormData] = useState({
    phoneNumber: '',
    otp: ''
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [resendTimer, setResendTimer] = useState(0);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      const redirectTo = location.state?.redirectTo || location.state?.from || '/';
      navigate(redirectTo, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  // Start resend timer
  const startResendTimer = () => {
    setResendTimer(60);
    const interval = setInterval(() => {
      setResendTimer((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validatePhone = () => {
    const newErrors = {};
    
    if (!formData.phoneNumber) {
      newErrors.phoneNumber = t('auth.errors.phoneRequired');
    } else {
      const formattedPhone = authService.formatPhoneNumber(formData.phoneNumber);
      if (!authService.validatePhoneNumber(formattedPhone)) {
        newErrors.phoneNumber = t('auth.errors.phoneInvalid');
      }
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateOtp = () => {
    const newErrors = {};
    
    if (!formData.otp) {
      newErrors.otp = t('auth.errors.otpRequired');
    } else if (!/^\d{4}$/.test(formData.otp)) {
      newErrors.otp = t('auth.errors.otpInvalid');
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRequestOtp = async (e) => {
    e.preventDefault();
    
    if (!validatePhone()) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      const formattedPhone = authService.formatPhoneNumber(formData.phoneNumber);
      await authService.requestOtp(formattedPhone);
      
      setStep('otp');
      startResendTimer();
      setIsLoading(false);
    } catch (error) {
      setIsLoading(false);
      // Show error notification instead of inline error
      showError(error);
      
      // If rate limited (429), set resend timer to retryAfter value
      if (error.status === 429 && error.errorJson?.retryAfter) {
        setResendTimer(error.errorJson.retryAfter);
        const interval = setInterval(() => {
          setResendTimer((prev) => {
            if (prev <= 1) {
              clearInterval(interval);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      }
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    
    if (!validateOtp()) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      const formattedPhone = authService.formatPhoneNumber(formData.phoneNumber);
      const response = await authService.verifyOtp(formattedPhone, formData.otp);

      // Update AuthContext state so the rest of the app knows we're logged in
      login(response);

      setIsLoading(false);
      setShowSuccess(true);
      
      // Determine redirect target after successful login
      const searchParams = new URLSearchParams(location.search);
      const qsRedirect = searchParams.get('redirectTo');
      const redirectTo = (location.state && (location.state.redirectTo || location.state.from)) || qsRedirect || '/';
      
      // Check if user needs to complete profile
      if (response.isNewUser || !response.profileCompleted) {
        setTimeout(() => {
          navigate('/profile/complete');
        }, 1500);
      } else {
        setTimeout(() => {
          navigate(redirectTo);
        }, 1500);
      }
    } catch (error) {
      setIsLoading(false);
      // Show error notification instead of inline error
      showError(error);
    }
  };

  const handleResendOtp = async () => {
    if (resendTimer > 0) return;
    
    setIsLoading(true);
    
    try {
      const formattedPhone = authService.formatPhoneNumber(formData.phoneNumber);
      await authService.requestOtp(formattedPhone);
      startResendTimer();
      setIsLoading(false);
    } catch (error) {
      setIsLoading(false);
      // Show error notification instead of inline error
      showError(error);
      
      // If rate limited (429), set resend timer to retryAfter value
      if (error.status === 429 && error.errorJson?.retryAfter) {
        setResendTimer(error.errorJson.retryAfter);
        const interval = setInterval(() => {
          setResendTimer((prev) => {
            if (prev <= 1) {
              clearInterval(interval);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      }
    }
  };

  const handleBack = () => {
    setStep('phone');
    setFormData(prev => ({ ...prev, otp: '' }));
    setErrors({});
  };

  // Icon components
  const PhoneIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M22 16.92V19.92C22.0011 20.1985 21.9441 20.4742 21.8325 20.7293C21.7209 20.9845 21.5573 21.2136 21.3521 21.4019C21.1468 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0974 21.9451 19.82 21.92C16.7428 21.5856 13.787 20.5341 11.19 18.85C8.77382 17.3147 6.72533 15.2662 5.18999 12.85C3.49997 10.2412 2.44824 7.27099 2.11999 4.18C2.095 3.90347 2.12787 3.62476 2.21649 3.36162C2.30512 3.09849 2.44756 2.85669 2.63476 2.65162C2.82196 2.44655 3.0498 2.28271 3.30379 2.17052C3.55777 2.05833 3.83233 2.00026 4.10999 2H7.10999C7.5953 1.99522 8.06579 2.16708 8.43376 2.48353C8.80173 2.79999 9.04207 3.23945 9.10999 3.72C9.23662 4.68007 9.47144 5.62273 9.80999 6.53C9.94454 6.88792 9.97366 7.27691 9.89391 7.65088C9.81415 8.02485 9.62886 8.36811 9.35999 8.64L8.08999 9.91C9.51355 12.4135 11.5864 14.4864 14.09 15.91L15.36 14.64C15.6319 14.3711 15.9751 14.1858 16.3491 14.1061C16.7231 14.0263 17.1121 14.0555 17.47 14.19C18.3773 14.5286 19.3199 14.7634 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );

  const OtpIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 17C11.45 17 11 16.55 11 16V12C11 11.45 11.45 11 12 11C12.55 11 13 11.45 13 12V16C13 16.55 12.55 17 12 17ZM13 9H11V7H13V9Z" fill="currentColor"/>
    </svg>
  );

  return (
    <>
      <Helmet>
        <title>{t('auth.login.pageTitle')} - GamersStation</title>
        <meta name="description" content={t('auth.login.pageDescription')} />
      </Helmet>
      
      <div className="login-page">
        <div className="login-background">
          <div className="gradient-orb gradient-orb-1"></div>
          <div className="gradient-orb gradient-orb-2"></div>
          <div className="gradient-orb gradient-orb-3"></div>
        </div>
        
        <div className="login-container">
          <div className="login-card">
            {/* Language Switcher */}
            <div className="auth-language-switcher">
              <LanguageSwitcher />
            </div>
            
            {/* Logo and Title */}
            <div className="login-header">
              <div className="logo-containers">
                <img src="/logo.svg" alt={t('imageAlt.logo')} className="logo-image" />
              </div>
              <h1 className="login-title">{t('auth.login.welcomeBack')}</h1>
              <p className="login-subtitle">
                {step === 'phone'
                  ? t('auth.login.enterPhone')
                  : t('auth.login.enterOtp')
                }
              </p>
            </div>

            {/* Success Animation */}
            {showSuccess && (
              <div className="success-overlay">
                <div className="success-animation">
                  <svg className="success-checkmark" viewBox="0 0 52 52">
                    <circle className="success-checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
                    <path className="success-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
                  </svg>
                  <p className="success-text">{t('auth.login.successMessage')}</p>
                </div>
              </div>
            )}

            {/* Phone Number Step */}
            {step === 'phone' && (
              <form onSubmit={handleRequestOtp} className="login-form">
                <FormInput
                  label={t('auth.fields.phoneNumber')}
                  type="tel"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  placeholder={t('auth.placeholders.phoneNumber')}
                  error={errors.phoneNumber}
                  required
                  icon={<PhoneIcon />}
                  autoComplete="tel"
                  disabled={isLoading}
                />

                <button
                  type="submit"
                  className={`login-button ${isLoading ? 'loading' : ''}`}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <span className="button-loader"></span>
                  ) : (
                    t('auth.login.requestOtp')
                  )}
                </button>
              </form>
            )}

            {/* OTP Verification Step */}
            {step === 'otp' && (
              <form onSubmit={handleVerifyOtp} className="login-form">
                <div className="otp-info">
                  <p className="otp-sent-message">
                    {t('auth.login.otpSentTo')} <strong>{formData.phoneNumber}</strong>
                  </p>
                  <button
                    type="button"
                    className="change-phone-btn"
                    onClick={handleBack}
                  >
                    {t('auth.login.changePhone')}
                  </button>
                </div>

                <FormInput
                  label={t('auth.fields.otpCode')}
                  type="text"
                  name="otp"
                  value={formData.otp}
                  onChange={handleChange}
                  placeholder={t('auth.placeholders.otpCode')}
                  error={errors.otp}
                  required
                  icon={<OtpIcon />}
                  autoComplete="one-time-code"
                  disabled={isLoading}
                  maxLength="4"
                />

                <div className="resend-otp-container">
                  {resendTimer > 0 ? (
                    <p className="resend-timer">
                      {t('auth.login.resendIn')} {resendTimer}s
                    </p>
                  ) : (
                    <button
                      type="button"
                      className="resend-otp-btn"
                      onClick={handleResendOtp}
                      disabled={isLoading}
                    >
                      {t('auth.login.resendOtp')}
                    </button>
                  )}
                </div>

                <button
                  type="submit"
                  className={`login-button ${isLoading ? 'loading' : ''}`}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <span className="button-loader"></span>
                  ) : (
                    t('auth.login.verifyAndSignIn')
                  )}
                </button>
              </form>
            )}

            {/* Sign Up Link */}
            <div className="auth-footer">
              <p>
                {t('auth.login.noAccount')}{' '}
                <Link to="/register" className="auth-link">
                  {t('auth.login.signUp')}
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default LoginPage;