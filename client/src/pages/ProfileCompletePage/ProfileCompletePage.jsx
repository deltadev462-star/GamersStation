import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import FormInput from '../../components/FormInput/FormInput';
import SuccessPopup from '../../components/SuccessPopup/SuccessPopup';
import userService from '../../services/userService';
import cityService from '../../services/cityService';
import regionService from '../../services/regionService';
import SearchableSelect from '../../components/SearchableSelect/SearchableSelect';
import { showError } from '../../components/ErrorNotification/ErrorNotification';
import './ProfileCompletePage.css';

const ProfileCompletePage = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    cityId: ''
  });
  const [regions, setRegions] = useState([]);
  const [cities, setCities] = useState([]);
  const [selectedRegionId, setSelectedRegionId] = useState('');
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingRegions, setIsLoadingRegions] = useState(true);
  const [isLoadingCities, setIsLoadingCities] = useState(false);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);

  // Determine if this is an existing user who only needs to add email
  const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
  const isEmailOnlyMode = storedUser.profileCompleted && !storedUser.hasEmail;

  // Fetch regions on mount
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const regionsData = await regionService.getRegions();
        setRegions(regionsData);
      } catch (error) {
        console.error('Error fetching regions:', error);
        showError(error);
      } finally {
        setIsLoadingRegions(false);
      }
    };

    fetchRegions();
  }, []);

  // Fetch cities when region changes
  useEffect(() => {
    if (selectedRegionId) {
      const fetchCities = async () => {
        setIsLoadingCities(true);
        try {
          const citiesData = await cityService.getCities(selectedRegionId);
          setCities(citiesData);
        } catch (error) {
          console.error('Error fetching cities:', error);
          showError(error);
        } finally {
          setIsLoadingCities(false);
        }
      };
      fetchCities();
    } else {
      setCities([]);
    }
  }, [selectedRegionId]);

  // Pre-fill form for existing users and fetch their profile
  useEffect(() => {
    if (isEmailOnlyMode) {
      const fetchProfile = async () => {
        try {
          const profile = await userService.getCurrentUserProfile();
          setFormData(prev => ({
            ...prev,
            username: profile.username || '',
            cityId: profile.cityId ? String(profile.cityId) : '',
          }));
        } catch (error) {
          console.error('Error fetching profile:', error);
        }
      };
      fetchProfile();
    }
  }, [isEmailOnlyMode]);

  // Check if user needs to complete profile
  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user.profileCompleted && user.hasEmail) {
      navigate('/');
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Real-time validation
    validateField(name, value);
  };

  const validateField = (name, value) => {
    const newErrors = { ...errors };
    const newSuccess = { ...success };
    
    // Clear previous messages
    delete newErrors[name];
    delete newSuccess[name];
    
    if (name === 'username') {
      if (!value.trim()) {
        newErrors.username = t('auth.errors.usernameRequired');
      } else if (value.trim().length < 3) {
        newErrors.username = t('auth.errors.usernameTooShort', { min: 3 });
      } else if (value.trim().length > 20) {
        newErrors.username = t('auth.errors.usernameTooLong', { max: 20 });
      } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        newErrors.username = t('auth.errors.usernameInvalid');
      } else {
        newSuccess.username = t('auth.success.usernameValid');
      }
    }
    
    if (name === 'email') {
      if (!value || !value.trim()) {
        newErrors.email = t('auth.errors.emailRequired') || 'Email is required';
      } else if (!/\S+@\S+\.\S+/.test(value)) {
        newErrors.email = t('auth.errors.emailInvalid');
      } else {
        newSuccess.email = t('auth.success.emailValid');
      }
    }
    
    setErrors(newErrors);
    setSuccess(newSuccess);
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!isEmailOnlyMode) {
      if (!formData.username.trim()) {
        newErrors.username = t('auth.errors.usernameRequired');
      } else if (formData.username.trim().length < 3) {
        newErrors.username = t('auth.errors.usernameShort');
      } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
        newErrors.username = t('auth.errors.usernameInvalid');
      }
    }
    
    if (!formData.email || !formData.email.trim()) {
      newErrors.email = t('auth.errors.emailRequired') || 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = t('auth.errors.emailInvalid');
    }
    
    if (!isEmailOnlyMode && !formData.cityId) {
      newErrors.cityId = t('auth.errors.cityRequired');
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      const profileData = {
        email: formData.email.trim(),
      };
      
      if (!isEmailOnlyMode) {
        profileData.username = formData.username.trim();
        profileData.cityId = parseInt(formData.cityId);
      }
      
        await userService.updateProfile(profileData);
      
      // Update hasEmail in localStorage so ProtectedRoute stops redirecting
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.hasEmail = true;
      if (!currentUser.profileCompleted) currentUser.profileCompleted = true;
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      // Show success popup then navigate
      setShowSuccessPopup(true);
      
      // Navigate after a delay
      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch (error) {
      console.error('Error updating profile:', error);
      showError(error);
    } finally {
      setIsLoading(false);
    }
  };


  // Icon components
  const UserIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21M16 7C16 9.20914 14.2091 11 12 11C9.79086 11 8 9.20914 8 7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );

  const EmailIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M3 8L10.89 13.26C11.2187 13.4793 11.6049 13.5963 12 13.5963C12.3951 13.5963 12.7813 13.4793 13.11 13.26L21 8M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );

  const CityIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );

  return (
    <>
      <Helmet>
        <title>{t('profileComplete.pageTitle')} - GamersStation</title>
        <meta name="description" content={t('profileComplete.pageDescription')} />
      </Helmet>
      
      <div className="profile-complete-page">
        <div className="profile-background">
          <div className="gradient-orb gradient-orb-1"></div>
          <div className="gradient-orb gradient-orb-2"></div>
        </div>
        
        <div className="profile-container">
          <div className="profile-card">
            {/* Header */}
            <div className="profile-header">
              <div className="logo-container">
                <img src="/logo.svg" alt={t('imageAlt.logo')} className="logo-image" />
              </div>
              <h1 className="profile-title">
                {isEmailOnlyMode
                  ? (t('profileComplete.addEmailTitle') || 'Add Your Email')
                  : t('profileComplete.title')}
              </h1>
              <p className="profile-subtitle">
                {isEmailOnlyMode
                  ? (t('profileComplete.addEmailSubtitle') || 'Please add your email address to continue receiving important notifications.')
                  : t('profileComplete.subtitle')}
              </p>
            </div>

            {/* Error Message */}
            {errors.general && (
              <div className="error-message">
                {errors.general}
              </div>
            )}

            {/* Profile Form */}
            <form onSubmit={handleSubmit} className="profile-form">
              {!isEmailOnlyMode && (
                <FormInput
                  label={t('profileComplete.fields.username')}
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder={t('profileComplete.placeholders.username')}
                  error={errors.username}
                  success={success.username}
                  required
                  icon={<UserIcon />}
                  disabled={isLoading}
                  minLength={3}
                  maxLength={20}
                  pattern="[a-zA-Z0-9_]+"
                />
              )}

              <FormInput
                label={t('profileComplete.fields.email')}
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder={t('profileComplete.placeholders.email')}
                error={errors.email}
                success={success.email}
                required
                icon={<EmailIcon />}
                disabled={isLoading}
              />

              {!isEmailOnlyMode && (
                <>
                  <div className="form-group">
                    <label className="form-label">
                      {t('profileComplete.fields.region')} <span className="required">*</span>
                    </label>
                    <SearchableSelect
                      options={regions}
                      value={selectedRegionId}
                      onChange={(val) => {
                        setSelectedRegionId(val);
                        setFormData((prev) => ({ ...prev, cityId: '' }));
                      }}
                      placeholder={t('profileComplete.placeholders.region')}
                      searchPlaceholder={t('profileComplete.placeholders.searchRegion')}
                      disabled={isLoading || isLoadingRegions}
                      hasError={!!errors.regionId}
                      getOptionLabel={(r) => i18n.language === 'ar' ? r.nameAr : r.nameEn}
                      getOptionValue={(r) => r.id}
                    />
                    {errors.regionId && (
                      <span className="error-text">{errors.regionId}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      {t('profileComplete.fields.city')} <span className="required">*</span>
                    </label>
                    <SearchableSelect
                      options={cities}
                      value={formData.cityId}
                      onChange={(val) => {
                        setFormData((prev) => ({ ...prev, cityId: val }));
                        if (errors.cityId) {
                          setErrors((prev) => ({ ...prev, cityId: '' }));
                        }
                      }}
                      placeholder={t('profileComplete.placeholders.city')}
                      searchPlaceholder={t('profileComplete.placeholders.searchCity')}
                      disabled={isLoading || !selectedRegionId || isLoadingCities}
                      hasError={!!errors.cityId}
                      getOptionLabel={(c) => i18n.language === 'ar' ? c.nameAr : c.nameEn}
                      getOptionValue={(c) => c.id}
                    />
                    {errors.cityId && (
                      <span className="error-text">{errors.cityId}</span>
                    )}
                  </div>
                </>
              )}

              <div className="form-actions">
                <button
                  type="submit"
                  className={`btn-submit btn-submit-full ${isLoading ? 'loading' : ''}`}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <span className="button-loader"></span>
                      {t('profileComplete.saving')}
                    </>
                  ) : (
                    t('profileComplete.complete')
                  )}
                </button>
              </div>
            </form>

            {/* Info Text */}
            <div className="info-text">
              <p>{t('profileComplete.infoText')}</p>
            </div>
          </div>
        </div>
      </div>
      
      {/* Success Popup */}
      <SuccessPopup
        isOpen={showSuccessPopup}
        onClose={() => {
          setShowSuccessPopup(false);
          navigate('/');
        }}
        title={t('profileComplete.successTitle') || 'Welcome to GamersStation!'}
        message={t('profileComplete.successMessage') || 'Your profile has been completed successfully. Enjoy browsing!'}
        autoClose={true}
        autoCloseDelay={2000}
      />
    </>
  );
};

export default ProfileCompletePage;