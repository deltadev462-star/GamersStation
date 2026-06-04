import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { MapPin, ArrowLeft } from 'lucide-react';
import { getTranslatedCityName } from '../../utils/cityTranslations';
import OptimizedImage from '../OptimizedImage/OptimizedImage';
import './ProductCard.css';

const AVATAR_COLORS = [
  'linear-gradient(135deg, #ff6b35, #ff8c42)',
  'linear-gradient(135deg, #6366f1, #8b5cf6)',
  'linear-gradient(135deg, #10b981, #059669)',
  'linear-gradient(135deg, #f43f5e, #e11d48)',
  'linear-gradient(135deg, #3b82f6, #2563eb)',
  'linear-gradient(135deg, #f59e0b, #d97706)',
  'linear-gradient(135deg, #ec4899, #db2777)',
  'linear-gradient(135deg, #14b8a6, #0d9488)',
];

const getInitial = (name) => {
  if (!name) return '?';
  return name.charAt(0).toUpperCase();
};

const getAvatarColor = (name) => {
  if (!name) return AVATAR_COLORS[0];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
};

const ProductCard = ({
  id,
  title,
  price,
  image,
  thumbnailUrl,
  isHighlighted,
  badge,
  username,
  location,
  condition,
  status,
}) => {
  const { t, i18n } = useTranslation();
  const isArabic = i18n.language === 'ar';
  const avatarColor = useMemo(() => getAvatarColor(username), [username]);
  const initial = useMemo(() => getInitial(username), [username]);

  const getConditionLabel = (cond) => {
    const map = {
      NEW: t('condition.new', 'جديد'),
      LIKE_NEW: t('condition.likeNew', 'شبه جديد'),
      USED: t('condition.used', 'مستعمل'),
      USED_GOOD: t('condition.usedGood', 'مستعمل - جيد'),
      USED_FAIR: t('condition.usedFair', 'مستعمل - مقبول'),
      FOR_PARTS: t('condition.forParts', 'قطع غيار'),
    };
    return map[cond] || cond;
  };

  const isSold = status === 'SOLD';

  const handleCardClick = (e) => {
    if (isSold) {
      e.preventDefault();
      return;
    }
    e.preventDefault();
    window.location.href = `/product/${id}`;
  };

  return (
    <a href={isSold ? undefined : `/product/${id}`} className={`product-card ${isHighlighted ? 'highlighted' : ''} ${isSold ? 'product-card-sold' : ''}`} onClick={handleCardClick} style={{ textDecoration: 'none', color: 'inherit' }}>
      {isSold && (
        <div className="sold-overlay">
          <span className="sold-overlay-text">{t('markAsSold.soldBadge')}</span>
        </div>
      )}
      {badge && (
        <div className="badge-container">
          <span className="product-badge">{badge}</span>
        </div>
      )}

      {/* Condition Badge */}
      {condition && (
        <div className="condition-badge">
          {getConditionLabel(condition)}
        </div>
      )}
      
      <div className="product-image">
        <OptimizedImage
          src={thumbnailUrl || image || '/placeholder-game.svg'}
          alt={t('imageAlt.productImage', { productName: title }) || title}
          className="product-img"
          width="100%"
          height="220px"
          objectFit="cover"
          placeholder="/placeholder-game.svg"
          enableBlur={false}
        />
      </div>

      <div className="product-content">
        {/* User Info Section - Enhanced */}
        <div className="user-info-section">
          <div className="user-avatar" style={{ background: avatarColor }}>
            <span className="avatar-initial">{initial}</span>
          </div>
          <div className="user-details">
            <div className="user-name">
              <span className="username-text">{username || t('anonymous')}</span>
            </div>
            {location && (
              <div className="user-location">
                <MapPin size={11} />
                <span>{getTranslatedCityName(location, t)}</span>
              </div>
            )}
          </div>
        </div>

        {/* Product Info Section */}
         <div className="product-info">
          <h3 className="product-titlee">{title}</h3>
        </div>


        {/* Price Section - At Bottom of Card */}
        <div className="current-price">
          <button className="card-nav-btn" aria-label="View product">
            <span className="rtl-flip">
              <ArrowLeft size={15} />
            </span>
          </button>
          {isArabic ? (
            <>
              <img src="/sar.svg" alt="SAR" className="price-currency-icon" />
              <span className="price-valuee">{Number(price || 0).toLocaleString()}</span>
            </>
          ) : (
            <>
              <span className="price-valuee">{Number(price || 0).toLocaleString()}</span>
              <img src="/sar.svg" alt="SAR" className="price-currency-icon" />
            </>
          )}
        </div>
      </div>
    </a>
  );
};

export default ProductCard;
