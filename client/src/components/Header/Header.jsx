import React, { useState, useEffect, memo, useCallback, useMemo } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Search, User, LogIn, MessageCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import authService from '../../services/authService';
import messagingService from '../../services/messagingService';
import SearchOverlay from '../SearchOverlay/SearchOverlay';
import './Header.css';

// Memoized navigation link component - Using React Router Link for SPA navigation
const NavLink = memo(({ to, isActive, onClick, children }) => (
  <Link
    to={to}
    className={`nav-link ${isActive ? 'active' : ''}`}
    onClick={onClick}
  >
    {children}
  </Link>
));

NavLink.displayName = 'NavLink';

// Memoized mobile navigation link component - Using React Router Link for SPA navigation
const MobileNavLink = memo(({ to, isActive, onClick, children }) => (
  <Link
    to={to}
    className={`mobile-nav-link ${isActive ? 'active' : ''}`}
    onClick={onClick}
  >
    {children}
  </Link>
));

MobileNavLink.displayName = 'MobileNavLink';

const Header = memo(() => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [activeLink, setActiveLink] = useState('/');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [unreadMessagesCount, setUnreadMessagesCount] = useState(0);
  const [searchValue, setSearchValue] = useState('');
  const [mobileSearchValue, setMobileSearchValue] = useState('');
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const location = useLocation();

  // Update active link based on current location
  useEffect(() => {
    setActiveLink(location.pathname);
  }, [location]);

  // Check authentication status
  useEffect(() => {
    const checkAuth = () => {
      const authStatus = authService.isAuthenticated();
      setIsAuthenticated(authStatus);
      
      if (authStatus) {
        const user = authService.getCurrentUser();
        setCurrentUser(user);
      } else {
        setCurrentUser(null);
      }
    };

    checkAuth();
    
    // Listen for storage changes (login/logout in other tabs)
    window.addEventListener('storage', checkAuth);
    
    // Listen for custom auth events
    const handleAuthChange = (event) => {
      if (event.detail && event.detail.type === 'login') {
        checkAuth();
      }
    };
    
    window.addEventListener('authStateChanged', handleAuthChange);
    
    return () => {
      window.removeEventListener('storage', checkAuth);
      window.removeEventListener('authStateChanged', handleAuthChange);
    };
  }, []);

  // Fetch unread messages count
  useEffect(() => {
    let unsubscribe;
    
    const fetchUnreadCount = async () => {
      if (isAuthenticated) {
        try {
          const count = await messagingService.getUnreadCount();
          setUnreadMessagesCount(count);
        } catch (error) {
          console.error('Error fetching unread count:', error);
          setUnreadMessagesCount(0);
        }
      } else {
        setUnreadMessagesCount(0);
      }
    };

    fetchUnreadCount();

    // Subscribe to new messages to update count in real-time
    if (isAuthenticated) {
      messagingService.subscribeToMessages((message) => {
        if (!message.isOwn) {
          setUnreadMessagesCount(prev => prev + 1);
        }
      }).then(unsub => {
        unsubscribe = unsub;
      }).catch(error => {
        console.error('Error subscribing to messages:', error);
      });
    }

    return () => {
      if (unsubscribe && typeof unsubscribe === 'function') {
        unsubscribe();
      }
    };
  }, [isAuthenticated]);

  const toggleMobileMenu = useCallback(() => {
    setIsMobileMenuOpen(prevState => {
      const newState = !prevState;
      
      // Prevent body scroll when menu is open
      if (newState) {
        document.body.classList.add('mobile-menu-open');
      } else {
        document.body.classList.remove('mobile-menu-open');
      }
      
      return newState;
    });
  }, []);

  // Clean up on unmount
  useEffect(() => {
    return () => {
      document.body.classList.remove('mobile-menu-open');
    };
  }, []);

  const handleLinkClick = useCallback((path) => {
    setActiveLink(path);
  }, []);
  
  const handleMobileLinkClick = useCallback((path) => {
    setActiveLink(path);
    toggleMobileMenu();
  }, [toggleMobileMenu]);

  const handleLogout = useCallback(() => {
    authService.logout();
    setIsAuthenticated(false);
    setCurrentUser(null);
    navigate('/');
  }, [navigate]);

  // Handle search submission
  const handleSearchSubmit = useCallback((e, isMobile = false) => {
    e.preventDefault();
    const query = isMobile ? mobileSearchValue : searchValue;
    if (query.trim()) {
      navigate(`/products?q=${encodeURIComponent(query.trim())}`);
      // Clear search input after navigation
      if (isMobile) {
        setMobileSearchValue('');
        toggleMobileMenu();
      } else {
        setSearchValue('');
      }
    }
  }, [navigate, searchValue, mobileSearchValue, toggleMobileMenu]);

  // Handle Enter key in search input
  const handleSearchKeyPress = useCallback((e, isMobile = false) => {
    if (e.key === 'Enter') {
      handleSearchSubmit(e, isMobile);
    }
  }, [handleSearchSubmit]);
  
  // Memoize navigation items - removed per requirement
  const navigationItems = useMemo(() => [], []);

  return (
    <header className="header">
      <div className="header-container">
        {/* Language Switcher - Always on left */}
       

        {/* Mobile Menu Button */}
        {/* <button
          className="mobile-menu-btn"
          onClick={toggleMobileMenu}
          aria-label={t('header.toggleNavigation')}
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button> */}

        {/* Logo - Using React Router Link for SPA navigation */}
        <Link to="/" className="header-logo">
          <img src="/logo.svg" alt="GamersStation" className="logo-icon" loading="eager" />
          <span className="logo-text">GamersStation</span>
          <span className="logo-beta">beta</span>
        </Link>

        {/* Navigation - removed per requirement */}

        {/* Search Bar - removed per requirement */}

        {/* User Actions */}
        <div className="header-actions">
          {/* Search Icon Button */}
          <button
            className="action-btn header-icon-btn"
            onClick={() => setIsSearchOpen(true)}
            aria-label={t('header.search')}
          >
            <Search size={20} />
          </button>

          {/* User Icon Button */}
          {isAuthenticated ? (
            <div className="auth-dropdown">
              <button className="action-btn header-icon-btn" style={{ position: 'relative' }}>
                <User size={20} />
                {unreadMessagesCount > 0 && (
                  <span className="user-unread-badge">{unreadMessagesCount}</span>
                )}
              </button>
              <div className="dropdown-menu">
                <Link to="/profile" className="dropdown-item">
                  <User size={18} />
                  <span>{t('header.myProfile')}</span>
                </Link>
                <Link to="/profile?tab=messages" className="dropdown-item">
                  <MessageCircle size={18} />
                  <span>{t('chat.messages')}</span>
                  {unreadMessagesCount > 0 && (
                    <span className="dropdown-badge">{unreadMessagesCount}</span>
                  )}
                </Link>
                <button onClick={handleLogout} className="dropdown-item logout-item">
                  <LogIn size={18} />
                  <span>{t('header.logout')}</span>
                </button>
              </div>
            </div>
          ) : (
            <button
              className="action-btn header-icon-btn"
              onClick={() => navigate('/login')}
              aria-label={t('header.login')}
            >
              <User size={20} />
            </button>
          )}
        </div>
      </div>

      <SearchOverlay
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
      />

      {/*  
      <div className={`mobile-menu ${isMobileMenuOpen ? 'open' : ''}`}>
        <div className="mobile-menu-content">
          
          <div className="mobile-user-section">
            {isAuthenticated ? (
              <>
                <Link to="/profile" className="mobile-action-btn" onClick={toggleMobileMenu}>
                  <div className="mobile-user-profile-img">
                    <User size={20} />
                  </div>
                  <span>{currentUser?.username || t('header.myAccount')}</span>
                </Link>
                <button onClick={handleLogout} className="mobile-action-btn logout-btn">
                  <LogIn size={20} />
                  <span>{t('header.logout')}</span>
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="mobile-auth-btn mobile-login-btn" onClick={toggleMobileMenu}>
                  <LogIn size={20} />
                  <span>{t('header.login')}</span>
                </Link>
                <Link to="/register" className="mobile-auth-btn mobile-register-btn" onClick={toggleMobileMenu}>
                  <UserPlus size={20} />
                  <span>{t('header.register')}</span>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
      */}
    </header>
  );
});

Header.displayName = 'Header';

export default Header;