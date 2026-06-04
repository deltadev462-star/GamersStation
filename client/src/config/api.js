// API Configuration
// Use relative URL in development to leverage Vite proxy
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

// API Endpoints
export const API_ENDPOINTS = {
  // Authentication
  auth: {
    requestOtp: '/auth/otp/request',
    verifyOtp: '/auth/otp/verify',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
  },
  
  // Posts/Products
  posts: {
    list: '/posts',
    search: '/posts/search',
    getById: (id) => `/posts/${id}`,
    create: '/posts',
    update: (id) => `/posts/${id}`,
    delete: (id) => `/posts/${id}`,
    myPosts: '/posts/my-ads',
    markAsSold: (id) => `/posts/${id}/mark-sold`,
  },
  
  // Categories
  categories: {
    list: '/categories/tree',
    getById: (id) => `/categories/${id}`,
  },
  
  // Cities
  cities: {
    list: '/cities',
    getById: (id) => `/cities/${id}`,
  },
  
  // Regions
  regions: {
    list: '/regions',
    getById: (id) => `/regions/${id}`,
  },
  
  // Users
  users: {
    profile: '/users/profile',
    updateProfile: '/users/profile',
    publicProfile: (id) => `/users/${id}/public`,
  },
  
  // Comments
  comments: {
    create: '/comments',
    list: '/comments',
    update: (id) => `/comments/${id}`,
    delete: (id) => `/comments/${id}`,
  },
  
  // Media
  media: {
    uploadImage: '/media/upload',
  },
};

// Helper function to build full API URL
export const buildApiUrl = (endpoint) => `${API_BASE_URL}${endpoint}`;

// Default headers
export const getDefaultHeaders = () => {
  const token = localStorage.getItem('accessToken');
  const currentLang = localStorage.getItem('i18nextLng') || 'ar';
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept-Language': currentLang,
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
};

// Import error handler
import { ApiError, parseErrorResponse } from '../utils/errorHandler.js';

// ---------------------------------------------------------------------------
// Refresh-token queue: centralises 401 handling so that concurrent requests
// that all receive 401 share a single token-refresh round-trip instead of
// each independently triggering one (which would cause the server's
// token-reuse detection to revoke the entire session).
// ---------------------------------------------------------------------------
let _isRefreshing = false;
let _failedQueue = []; // { resolve, reject } for each queued caller

const _processQueue = (error) => {
  _failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(); // signal caller to retry with the fresh token
    }
  });
  _failedQueue = [];
};

/**
 * Wait for the in-flight refresh to finish.
 * Returns a promise that resolves when the queue is drained successfully,
 * or rejects if the refresh failed.
 */
const _enqueue = () =>
  new Promise((resolve, reject) => {
    _failedQueue.push({ resolve, reject });
  });

/**
 * Trigger a token refresh (if one isn't already running) and replay
 * the original request once the new token is available.
 *
 * @param {Function} replayFn  – a zero-arg function that re-executes the
 *                               original request with the fresh token.
 * @returns {Promise<*>}       – result of the replayed request, or throws.
 */
const _handleUnauthorised = async (replayFn) => {
  // No stored token at all → user was never logged in; don't try to refresh
  if (!localStorage.getItem('accessToken')) {
    return null; // let the caller fall through to normal error handling
  }

  if (_isRefreshing) {
    // Another request already kicked off a refresh → just wait in line
    await _enqueue();
    return replayFn();
  }

  _isRefreshing = true;

  try {
    const { authService } = await import('../services/authService.js');
    await authService.refreshToken();

    // Refresh succeeded → drain the queue so all waiters can retry
    _processQueue(null);

    // Replay this request (the one that triggered the refresh)
    return replayFn();
  } catch (refreshError) {
    // Refresh failed → reject every queued caller
    _processQueue(refreshError);

    // Clear local auth state
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
    return undefined;
  } finally {
    _isRefreshing = false;
  }
};

// ---------------------------------------------------------------------------
// Core helpers
// ---------------------------------------------------------------------------

/** Safely parse a response body as JSON; returns null on failure. */
const _tryParseJson = (text) => {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
};

/** Build an ApiError from a non-OK response. */
const _buildApiError = (status, bodyText, statusText) => {
  const errorJson = _tryParseJson(bodyText);

  let messageAr = null;
  let messageEn = null;
  if (errorJson) {
    messageAr = errorJson.messageAr || errorJson.message_ar || null;
    messageEn = errorJson.messageEn || errorJson.message_en || null;
  }

  const message =
    (errorJson && (errorJson.message || errorJson.detail || errorJson.title)) ||
    bodyText ||
    statusText ||
    'API request failed';

  const error = new ApiError(`[${status}] ${message}`, status, messageAr, messageEn);
  error.errorJson = errorJson;
  return error;
};

// ---------------------------------------------------------------------------
// apiRequest
// ---------------------------------------------------------------------------

// API request helper with robust error handling and safe JSON parsing
export const apiRequest = async (url, options = {}, _isRetry = false) => {
  try {
    const response = await fetch(buildApiUrl(url), {
      ...options,
      headers: {
        ...getDefaultHeaders(),
        ...(options.headers || {}),
      },
    });

    // 204 No Content → nothing to parse
    if (response.status === 204) return null;

    // Read body once (stream is consumable only once)
    let bodyText = '';
    try {
      bodyText = await response.text();
    } catch {
      bodyText = '';
    }

    // ---- 401 / 403 → queue-based refresh --------------------------------
    if ((response.status === 401 || response.status === 403) && !_isRetry) {
      const result = await _handleUnauthorised(
        () => apiRequest(url, options, true),
      );
      // If _handleUnauthorised returned null the user was never logged in;
      // fall through to the normal error path below.
      if (result !== null && result !== undefined) return result;
    }

    // ---- Success --------------------------------------------------------
    if (response.ok) {
      const json = _tryParseJson(bodyText);
      if (json !== null) return json;
      return bodyText || null;
    }

    // ---- Error ----------------------------------------------------------
    throw _buildApiError(response.status, bodyText, response.statusText);
  } catch (error) {
    if (error instanceof ApiError) throw error;

    // Network error or other fetch failure
    throw new ApiError(
      error.message || 'Network request failed',
      0,
      'خطأ في الاتصال بالشبكة. يرجى التحقق من اتصالك بالإنترنت.',
      'Network connection error. Please check your internet connection.',
    );
  }
};

// ---------------------------------------------------------------------------
// File upload helper (also uses the shared refresh queue)
// ---------------------------------------------------------------------------

export const uploadFile = async (file, folder = 'posts', _isRetry = false) => {
  const response = await fetch(buildApiUrl('/media/upload'), {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
      // Don't set Content-Type – let browser set it with boundary for multipart
    },
    body: (() => {
      const fd = new FormData();
      fd.append('file', file);
      fd.append('folder', folder);
      return fd;
    })(),
  });

  // ---- 401 / 403 → same queue as apiRequest -----------------------------
  if ((response.status === 401 || response.status === 403) && !_isRetry) {
    const result = await _handleUnauthorised(
      () => uploadFile(file, folder, true),
    );
    if (result !== null && result !== undefined) return result;
  }

  if (!response.ok) {
    let errorText = '';
    try {
      errorText = await response.text();
    } catch {
      errorText = '';
    }

    const errorJson = _tryParseJson(errorText);

    let messageAr = null;
    let messageEn = null;
    if (errorJson) {
      messageAr = errorJson.messageAr || errorJson.message_ar || null;
      messageEn = errorJson.messageEn || errorJson.message_en || null;
    }

    throw new ApiError(
      `[${response.status}] Upload failed`,
      response.status,
      messageAr || 'فشل رفع الملف. يرجى المحاولة مرة أخرى.',
      messageEn || 'Upload failed. Please try again.',
    );
  }

  return response.json();
};

export default API_BASE_URL;
