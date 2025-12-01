import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { User, MapPin, Star, StarHalf } from 'lucide-react';
import './ProductCard.css';

const ProductCard = ({
  id,
  title,
  description,
  price,
  originalPrice,
  image,
  platforms,
  isHighlighted,
  badge,
  username,
  location,
  rating = 0,
  reviewCount = 0
}) => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  
  const handleCardClick = () => {
    navigate(`/product/${id}`);
  };
  // Function to render rating stars
  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    
    for (let i = 0; i < fullStars; i++) {
      stars.push(
        <Star key={i} className="star-icon star-filled" size={16} />
      );
    }
    
    if (hasHalfStar && fullStars < 5) {
      stars.push(
        <StarHalf key="half" className="star-icon star-half" size={16} />
      );
    }
    
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(
        <Star key={`empty-${i}`} className="star-icon star-empty" size={16} />
      );
    }
    
    return stars;
  };

  return (
    <div className={`product-card ${isHighlighted ? 'highlighted' : ''}`} onClick={handleCardClick}>
      {badge && (
        <div className="badge-container">
          <span className="product-badge">{badge}</span>
        </div>
      )}
      
      <div className="product-image">
        <img src={image || '/placeholder-game.jpg'} alt={title} />
      </div>

      <div className="product-content">
        {/* User Info Section - Enhanced */}
        <div className="user-info-section">
          <div className="user-avatar">
            <User size={16} />
          </div>
          <div className="user-details">
            <div className="user-name">
              <span className="username-text">{username || t('anonymous')}</span>
            </div>
            {location && (
              <div className="user-location">
                <MapPin size={11} />
                <span>{location}</span>
              </div>
            )}
          </div>
        </div>

        {/* Rating and Reviews - Moved Above Title */}
        <div className="rating-section">
          <div className="rating-stars">
            {renderStars(rating)}
          </div>
          <div className="rating-info">
            <span className="rating-value">{rating.toFixed(1)}</span>
            {reviewCount > 0 && (
              <span className="review-count">({reviewCount} {t('reviews')})</span>
            )}
          </div>
        </div>

        {/* Product Info Section */}
        <div className="product-info">
          <h3 className="product-titlee">{title}</h3>
          
          <p className="product-description">
            {description && description.length > 80
              ? `${description.substring(0, 80)}...`
              : description || t('noDescription')}
          </p>
        </div>

        {/* Platforms Tags */}
        {platforms && platforms.length > 0 && (
          <div className="product-platforms">
            {platforms.slice(0, 3).map((platform, index) => (
              <span key={index} className="platform-tag">{platform}</span>
            ))}
            {platforms.length > 3 && (
              <span className="platform-tag more">+{platforms.length - 3}</span>
            )}
          </div>
        )}

        {/* Price Section - At Bottom of Card */}
        
           
          
            <div className="current-price">
              <span className="price-currency">{t('currency')}</span>
              <span className="price-value">{price || '0'}</span>
            </div>
             
          </div>
  

      
    </div>
  );
};

export default ProductCard;
