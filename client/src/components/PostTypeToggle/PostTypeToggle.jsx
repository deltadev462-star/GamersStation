import React from 'react';
import { useTranslation } from 'react-i18next';
import './PostTypeToggle.css';

const PostTypeToggle = ({ selectedType, onTypeChange }) => {
  const { t } = useTranslation();

  const handleToggle = (type) => {
    if (selectedType === type) {
      onTypeChange(null);
    } else {
      onTypeChange(type);
    }
  };

  return (
    <div className="post-type-toggle">
      <div className="toggle-container">
        <button
          className={`toggle-btn ${selectedType === 'SELL' ? 'active' : ''}`}
          onClick={() => handleToggle('SELL')}
          aria-pressed={selectedType === 'SELL'}
        >
          <span className="toggle-icon-emoji">📦</span>
          <div className="toggle-btn-text">
            <span className="toggle-btn-title">{t('postTypeToggle.forSale')}</span>
            <span className="toggle-btn-subtitle">{t('postTypeToggle.forSaleDesc')}</span>
          </div>
        </button>

        <button
          className={`toggle-btn ${selectedType === 'ASK' ? 'active' : ''}`}
          onClick={() => handleToggle('ASK')}
          aria-pressed={selectedType === 'ASK'}
        >
          <span className="toggle-icon-emoji">🔍</span>
          <div className="toggle-btn-text">
            <span className="toggle-btn-title">{t('postTypeToggle.wanted')}</span>
            <span className="toggle-btn-subtitle">{t('postTypeToggle.wantedDesc')}</span>
          </div>
        </button>
      </div>
    </div>
  );
};

export default PostTypeToggle;
