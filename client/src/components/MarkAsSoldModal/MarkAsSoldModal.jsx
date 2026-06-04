import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { X, Loader2 } from 'lucide-react';
import postService from '../../services/postService';
import { showError } from '../ErrorNotification/ErrorNotification';
import './MarkAsSoldModal.css';

const MarkAsSoldModal = ({ postId, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (soldThroughPlatform) => {
    try {
      setLoading(true);
      await postService.markAsSold(postId, soldThroughPlatform);
      if (onSuccess) onSuccess();
      onClose();
    } catch (error) {
      console.error('Error marking as sold:', error);
      showError(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mark-sold-overlay" onClick={onClose}>
      <div className="mark-sold-modal" onClick={(e) => e.stopPropagation()}>
        <button className="mark-sold-close" onClick={onClose} aria-label={t('common.close')}>
          <X size={20} />
        </button>

        <div className="mark-sold-content">
          <h2 className="mark-sold-title">{t('markAsSold.title')}</h2>
          <p className="mark-sold-warning">{t('markAsSold.confirmMessage')}</p>
          <p className="mark-sold-question">{t('markAsSold.question')}</p>

          <div className="mark-sold-actions">
            <button
              className="mark-sold-btn mark-sold-yes"
              onClick={() => handleSubmit(true)}
              disabled={loading}
            >
              {loading ? <Loader2 size={18} className="spinner" /> : t('markAsSold.yes')}
            </button>
            <button
              className="mark-sold-btn mark-sold-no"
              onClick={() => handleSubmit(false)}
              disabled={loading}
            >
              {loading ? <Loader2 size={18} className="spinner" /> : t('markAsSold.no')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MarkAsSoldModal;
