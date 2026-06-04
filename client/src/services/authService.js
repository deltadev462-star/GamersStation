import { API_ENDPOINTS, apiRequest, buildApiUrl } from '../config/api';

class AuthService {
  constructor() {
    // Singleton promise to prevent concurrent refresh calls
    this._refreshPromise = null;
  }

  // Store tokens in local storage
  setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }
  
  // Clear tokens
  clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }
  
  // Get current user from storage
  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
  
  // Check if user has tokens stored (does NOT validate expiry)
  hasTokens() {
    return !!localStorage.getItem('refreshToken');
  }

  // Check if user is authenticated
  isAuthenticated() {
    return !!localStorage.getItem('accessToken');
  }
  
  // Get access token
  getAccessToken() {
    return localStorage.getItem('accessToken');
  }
  
  // Get refresh token
  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }
  
  // Request OTP
  async requestOtp(phoneNumber) {
    try {
      const response = await apiRequest(API_ENDPOINTS.auth.requestOtp, {
        method: 'POST',
        body: JSON.stringify({ phoneNumber }),
      });
      return response;
    } catch (error) {
      console.error('Error requesting OTP:', error);
      throw error;
    }
  }
  
  // Verify OTP and login
  async verifyOtp(phoneNumber, code) {
    try {
      const response = await apiRequest(API_ENDPOINTS.auth.verifyOtp, {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, code }),
      });
      
      // Store tokens and user data
      if (response.accessToken && response.refreshToken) {
        this.setTokens(response.accessToken, response.refreshToken);
        
        // Store user information
        const user = {
          userId: response.userId,
          phoneNumber: response.phoneNumber,
          role: response.role,
          profileCompleted: response.profileCompleted,
          isNewUser: response.isNewUser,
        };
        localStorage.setItem('user', JSON.stringify(user));
      }
      
      return response;
    } catch (error) {
      console.error('Error verifying OTP:', error);
      throw error;
    }
  }
  
  // Refresh access token (single-flight: concurrent callers share one request)
  async refreshToken() {
    // If a refresh is already in-flight, reuse the same promise.
    // This prevents concurrent 401 retries from each firing a separate
    // refresh request, which would trigger server-side token-reuse detection.
    if (this._refreshPromise) {
      return this._refreshPromise;
    }

    this._refreshPromise = this._doRefresh();
    try {
      return await this._refreshPromise;
    } finally {
      this._refreshPromise = null;
    }
  }

  async _doRefresh() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.auth.refresh), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${refreshToken}`,
        },
      });
      
      if (!response.ok) {
        throw new Error('Failed to refresh token');
      }
      
      const data = await response.json();
      
      // Update tokens
      if (data.accessToken && data.refreshToken) {
        this.setTokens(data.accessToken, data.refreshToken);
      }
      
      return data;
    } catch (error) {
      console.error('Error refreshing token:', error);
      // Clear tokens on refresh failure
      this.clearTokens();
      throw error;
    }
  }
  
  // Logout — revoke server-side session, then clear local state
  async logout() {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        await fetch(buildApiUrl(API_ENDPOINTS.auth.logout), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${refreshToken}`,
          },
        });
      }
    } catch {
      // Ignore errors — we're logging out regardless
    } finally {
      this.clearTokens();
    }
  }
  
  // Format phone number to Saudi format
  formatPhoneNumber(phoneNumber) {
    // Remove all non-digits
    const cleaned = phoneNumber.replace(/\D/g, '');
    
    // Check if it starts with country code
    if (cleaned.startsWith('966')) {
      return `+${cleaned}`;
    }
    
    // Check if it starts with 0, remove it
    if (cleaned.startsWith('0')) {
      return `+966${cleaned.substring(1)}`;
    }
    
    // Otherwise add country code
    return `+966${cleaned}`;
  }
  
  // Validate Saudi phone number
  validatePhoneNumber(phoneNumber) {
    const regex = /^\+966[0-9]{9}$/;
    return regex.test(phoneNumber);
  }
}

const authService = new AuthService();

export { authService };
export default authService;