import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Search, ChevronRight, X } from 'lucide-react';
import './SearchOverlay.css';

const RECENT_SEARCHES_KEY = 'gs_recent_searches';
const MAX_RECENT = 10;
const DISMISS_THRESHOLD = 100;

const SearchOverlay = ({ isOpen, onClose }) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState('');
  const [recentSearches, setRecentSearches] = useState([]);
  const inputRef = useRef(null);
  const sheetRef = useRef(null);
  const dragRef = useRef({ startY: 0, isDragging: false, deltaY: 0 });

  // Load recent searches and reset input when opened
  useEffect(() => {
    if (isOpen) {
      try {
        const saved = JSON.parse(localStorage.getItem(RECENT_SEARCHES_KEY) || '[]');
        setRecentSearches(Array.isArray(saved) ? saved : []);
      } catch {
        setRecentSearches([]);
      }
      setSearchValue('');
      setTimeout(() => inputRef.current?.focus(), 350);
    }
  }, [isOpen]);

  // Lock body scroll while open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  const saveRecent = useCallback((list) => {
    setRecentSearches(list);
    localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(list));
  }, []);

  const addRecentSearch = useCallback(
    (query) => {
      const trimmed = query.trim();
      if (!trimmed) return;
      const updated = [trimmed, ...recentSearches.filter((s) => s !== trimmed)].slice(
        0,
        MAX_RECENT
      );
      saveRecent(updated);
    },
    [recentSearches, saveRecent]
  );

  const handleSearch = useCallback(
    (query) => {
      const trimmed = (query ?? searchValue).trim();
      if (!trimmed) return;
      addRecentSearch(trimmed);
      onClose();
      navigate(`/products?q=${encodeURIComponent(trimmed)}`);
    },
    [searchValue, addRecentSearch, navigate, onClose]
  );

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter') handleSearch(searchValue);
      if (e.key === 'Escape') onClose();
    },
    [handleSearch, searchValue, onClose]
  );

  const removeRecentSearch = useCallback(
    (term, e) => {
      e.stopPropagation();
      saveRecent(recentSearches.filter((s) => s !== term));
    },
    [recentSearches, saveRecent]
  );

  const clearAllRecent = useCallback(() => {
    setRecentSearches([]);
    localStorage.removeItem(RECENT_SEARCHES_KEY);
  }, []);

  // ── Pull-to-dismiss touch handlers ──────────────────────────────────────
  const handleTouchStart = useCallback((e) => {
    dragRef.current = { startY: e.touches[0].clientY, isDragging: true, deltaY: 0 };
  }, []);

  const handleTouchMove = useCallback((e) => {
    if (!dragRef.current.isDragging) return;
    const delta = e.touches[0].clientY - dragRef.current.startY;
    if (delta <= 0) return; // prevent upward over-drag
    dragRef.current.deltaY = delta;
    if (sheetRef.current) {
      sheetRef.current.style.transition = 'none';
      sheetRef.current.style.transform = `translateY(${delta}px)`;
    }
  }, []);

  const handleTouchEnd = useCallback(() => {
    if (!dragRef.current.isDragging) return;
    dragRef.current.isDragging = false;
    const delta = dragRef.current.deltaY;
    if (sheetRef.current) {
      sheetRef.current.style.transition = '';
      sheetRef.current.style.transform = '';
    }
    if (delta > DISMISS_THRESHOLD) onClose();
  }, [onClose]);

  return (
    <div
      className={`search-overlay${isOpen ? ' search-overlay--open' : ''}`}
      onClick={onClose}
      aria-modal="true"
      role="dialog"
    >
      <div
        className="search-sheet"
        ref={sheetRef}
        onClick={(e) => e.stopPropagation()}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
      >
        {/* Drag handle */}
        <div className="sheet-drag-handle" />

        {/* Search bar row */}
        <div className="sheet-search-bar">
          <div className="sheet-search-input-wrapper">
            <Search className="sheet-search-icon" size={20} aria-hidden="true" />
            <input
              ref={inputRef}
              type="search"
              className="sheet-search-input"
              placeholder={t('searchOverlay.placeholder')}
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              onKeyDown={handleKeyDown}
              autoComplete="off"
              dir="auto"
            />
          </div>
          <button
            className="sheet-back-btn"
            onClick={onClose}
            aria-label={t('common.back')}
          >
            <ChevronRight size={22} />
          </button>
        </div>

        {/* Scrollable content */}
        <div className="sheet-content">
          {/* Recent searches */}
          {recentSearches.length > 0 && (
            <section className="sheet-section">
              <div className="sheet-section-header">
                <button className="clear-all-btn" onClick={clearAllRecent}>
                  {t('searchOverlay.deleteAll')}
                </button>
                <h3 className="sheet-section-title">
                  {t('searchOverlay.recentKeywords')}
                </h3>
              </div>
              <ul className="recent-searches" role="list">
                {recentSearches.map((term) => (
                  <li
                    key={term}
                    className="recent-search-item"
                    onClick={() => handleSearch(term)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch(term)}
                  >
                    <button
                      className="remove-recent-btn"
                      onClick={(e) => removeRecentSearch(term, e)}
                      aria-label={t('common.delete')}
                    >
                      <X size={14} />
                    </button>
                    <span className="recent-search-text">{term}</span>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>
      </div>
    </div>
  );
};

export default SearchOverlay;
