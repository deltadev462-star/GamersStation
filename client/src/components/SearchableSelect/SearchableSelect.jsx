import React, { useState, useRef, useEffect } from 'react';
import './SearchableSelect.css';

const SearchableSelect = ({
  options,
  value,
  onChange,
  placeholder,
  searchPlaceholder,
  disabled,
  hasError,
  icon,
  getOptionLabel,
  getOptionValue,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const containerRef = useRef(null);
  const searchInputRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
        setSearchTerm('');
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Focus search input when dropdown opens
  useEffect(() => {
    if (isOpen && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isOpen]);

  const filteredOptions = options.filter((opt) => {
    const label = getOptionLabel(opt);
    return label.toLowerCase().includes(searchTerm.toLowerCase());
  });

  const selectedOption = options.find(
    (opt) => String(getOptionValue(opt)) === String(value)
  );

  const handleSelect = (opt) => {
    onChange(String(getOptionValue(opt)));
    setIsOpen(false);
    setSearchTerm('');
  };

  const handleToggle = () => {
    if (!disabled) {
      setIsOpen((prev) => !prev);
      setSearchTerm('');
    }
  };

  return (
    <div
      className={`searchable-select ${isOpen ? 'open' : ''} ${disabled ? 'disabled' : ''} ${hasError ? 'has-error' : ''}`}
      ref={containerRef}
    >
      <button
        type="button"
        className="searchable-select-trigger"
        onClick={handleToggle}
        disabled={disabled}
      >
        <span className={`searchable-select-value ${!selectedOption ? 'placeholder' : ''}`}>
          {selectedOption ? getOptionLabel(selectedOption) : placeholder}
        </span>
        {icon && <span className="searchable-select-icon">{icon}</span>}
        <svg
          className={`searchable-select-chevron ${isOpen ? 'rotated' : ''}`}
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M6 9l6 6 6-6"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>

      {isOpen && (
        <div className="searchable-select-dropdown">
          <div className="searchable-select-search-wrapper">
            <svg
              className="searchable-select-search-icon"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2" />
              <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <input
              ref={searchInputRef}
              type="text"
              className="searchable-select-search"
              placeholder={searchPlaceholder || '...'}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="searchable-select-options">
            {filteredOptions.length === 0 ? (
              <div className="searchable-select-no-results">—</div>
            ) : (
              filteredOptions.map((opt) => {
                const optValue = String(getOptionValue(opt));
                const isSelected = optValue === String(value);
                return (
                  <button
                    type="button"
                    key={optValue}
                    className={`searchable-select-option ${isSelected ? 'selected' : ''}`}
                    onClick={() => handleSelect(opt)}
                  >
                    {getOptionLabel(opt)}
                    {isSelected && (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                        <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    )}
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchableSelect;
