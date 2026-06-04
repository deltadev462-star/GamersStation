import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  ChevronLeft,
  ChevronRight,
  Heart,
  Share2,
  Shield,
  MapPin,
  Calendar,
  Eye,
  Loader2,
  MessageCircle,
  User,
  Check,
  Package,
  Gamepad2,
  Maximize2,
  Camera,
  Tag
} from 'lucide-react';
import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import SEO from '../../components/SEO/SEO';
import { postService } from '../../services/postService';
import messagingService from '../../services/messagingService';
import authService from '../../services/authService';
import OptimizedImage from '../../components/OptimizedImage/OptimizedImage';
import userService from '../../services/userService';
import { getTranslatedCityName } from '../../utils/cityTranslations';
import { showError } from '../../components/ErrorNotification/ErrorNotification';
import MarkAsSoldModal from '../../components/MarkAsSoldModal/MarkAsSoldModal';
import './ProductDetailsPage.css';

const ProductDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const currentLang = i18n.language;
  const [selectedImage, setSelectedImage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [product, setProduct] = useState(null);
  const [relatedProducts, setRelatedProducts] = useState([]);
  const [showShareMenu, setShowShareMenu] = useState(false);
  const [postType, setPostType] = useState(null);
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [sellerDetails, setSellerDetails] = useState(null);
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [fullscreenImage, setFullscreenImage] = useState(false);
  const [sellerAdCount, setSellerAdCount] = useState(0);
  const [showMarkSoldModal, setShowMarkSoldModal] = useState(false);

  useEffect(() => {
    fetchProductDetails();
  }, [id, currentLang]);

  const fetchProductDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch product details
      const postData = await postService.getPostById(id);
      
      // Save post type
      setPostType(postData.type);
      
      // Transform Post data to Product format
      const transformedProduct = {
        id: postData.id,
        name: postData.localizedTitle?.[currentLang] || postData.title || 'Untitled Product',
        arabicName: postData.localizedTitle?.ar || postData.title || 'منتج بدون اسم',
        price: postData.price || 0,
        sold: Math.floor(Math.random() * 1000) + 100,
        availability: postData.status === 'ACTIVE' ? t('pages.productDetails.available') : t('pages.productDetails.unavailable'),
        brand: 'GamersStation',
        category: postData.categoryName || 'Gaming',
        categoryId: postData.categoryId,
        sku: `GS-${postData.id}-${new Date().getFullYear()}`,
        images: postData.images?.length > 0
          ? postData.images.map(img => img.url || '/placeholder-game.svg')
          : ['/placeholder-game.svg', '/placeholder-game.svg', '/placeholder-game.svg', '/placeholder-game.svg'], // Add multiple placeholders for testing
        description: postData.localizedDescription?.[currentLang] || postData.description || 'منتج عالي الجودة من GamersStation',
        type: postData.type, // Add type to product data
        specifications: {
          [t('pages.productDetails.condition.label')]: (() => {
            switch (postData.condition) {
              case 'NEW':
                return t('addProduct.conditions.new');
              case 'LIKE_NEW':
                return t('addProduct.conditions.likeNew');
              case 'USED_GOOD':
                return t('addProduct.conditions.good');
              case 'USED_FAIR':
                return t('addProduct.conditions.fair');
              default:
                return postData.condition === 'NEW' ? t('pages.productDetails.condition.new') : t('pages.productDetails.condition.used');
            }
          })(),
          [t('pages.productDetails.city')]: getTranslatedCityName(postData.cityName, t),
          [t('pages.productDetails.publishDate')]: new Date(postData.createdAt).toLocaleDateString(currentLang === 'ar' ? 'ar-SA' : 'en-US', { timeZone: 'Asia/Riyadh' }),
          [t('pages.productDetails.adNumber')]: postData.id,
          [t('pages.productDetails.views')]: postData.viewCount || 0,
        },
        seller: {
          id: postData.ownerId,
          name: postData.store?.nameEn || postData.ownerUsername || (currentLang === 'ar' ? 'مجهول' : 'Anonymous'),
          verified: postData.store?.isVerified || false,
          city: getTranslatedCityName(postData.cityName, t)
        }
      };
      
      setProduct(transformedProduct);
      
      // Try to fetch seller details for avatar
      if (postData.ownerId) {
        try {
          const userData = await userService.getUserById(postData.ownerId);
          setSellerDetails({
            avatar: userData.profileImageUrl,
            storeName: userData.storeName,
            username: userData.username,
            name: userData.name || userData.username || (currentLang === 'ar' ? 'مجهول' : 'Anonymous'),
            city: getTranslatedCityName(userData.cityName || postData.cityName, t),
            memberSince: userData.createdAt
          });
          
          // Try to fetch seller's ad count
          try {
            const sellerPosts = await postService.getPosts({
              ownerId: postData.ownerId,
              page: 0,
              size: 1
            });
            setSellerAdCount(sellerPosts?.totalElements || 0);
          } catch {
            // Could not fetch ad count
          }
        } catch {
          // Could not fetch seller details
        }
      }
      
      // Fetch related products from same category
      let transformedRelated = [];
      
      // First try to get products from the same category
      if (postData.categoryId) {
        try {
          const relatedData = await postService.getPosts({
            categoryId: postData.categoryId,
            page: 0, // API uses 0-based pagination
            size: 8,
            sortBy: 'createdAt',
            direction: 'DESC'
          });
          
          if (relatedData && relatedData.content) {
            transformedRelated = relatedData.content
              .filter(post => post.id !== postData.id)
              .map(post => ({
                id: post.id,
                name: post.localizedTitle?.[currentLang] || post.title || 'Untitled',
                price: post.price || 0,
                image: post.images?.[0]?.url || '/placeholder-game.svg'
              }));
          }
        } catch {
          // Error fetching category products
        }
      }
      
      // If we don't have enough related products, fetch random products
      if (transformedRelated.length < 4) {
        try {
          const randomData = await postService.getPosts({
            page: 0, // API uses 0-based pagination
            size: 10,
            sortBy: 'createdAt',
            direction: 'DESC'
          });
          
          if (randomData && randomData.content) {
            const additionalProducts = randomData.content
              .filter(post => post.id !== postData.id && !transformedRelated.find(p => p.id === post.id))
              .map(post => ({
                id: post.id,
                name: post.localizedTitle?.[currentLang] || post.title || 'Untitled',
                price: post.price || 0,
                image: post.images?.[0]?.url || '/placeholder-game.svg'
              }));
            
            transformedRelated = [...transformedRelated, ...additionalProducts].slice(0, 4);
          }
        } catch {
          // Error fetching random products
        }
      } else {
        transformedRelated = transformedRelated.slice(0, 4);
      }
        
      setRelatedProducts(transformedRelated);
      
    } catch (error) {
      console.error('Error fetching product details:', error);
      setError(t('common.error'));
      showError(error);
    } finally {
      setLoading(false);
    }
  };

  const handleShare = (platform) => {
    const url = window.location.href;
    const title = product.name;
    
    switch (platform) {
      case 'whatsapp':
        window.open(`https://wa.me/?text=${encodeURIComponent(title + ' ' + url)}`, '_blank');
        break;
      case 'facebook':
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`, '_blank');
        break;
      case 'twitter':
        window.open(`https://twitter.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`, '_blank');
        break;
      case 'copy':
        navigator.clipboard.writeText(url);
        setShowCopyNotification(true);
        setTimeout(() => setShowCopyNotification(false), 3000);
        break;
    }
    setShowShareMenu(false);
  };

  const handleImageSelect = (index) => {
    setSelectedImage(index);
  };

  const getImageCountText = (count) => {
    if (currentLang === 'ar') {
      if (count === 1) return t('pages.productDetails.onePhoto');
      if (count === 2) return t('pages.productDetails.twoPhotos');
      if (count >= 3 && count <= 10) return t('pages.productDetails.multiplePhotos', { count });
      return t('pages.productDetails.manyPhotos', { count });
    }
    return count === 1 ? t('pages.productDetails.onePhoto') : t('pages.productDetails.multiplePhotos', { count });
  };

  if (loading) {
    return (
      <div className="product-details-page gaming-theme">
        <Header />
        <div className="loading-container">
          <Loader2 className="spinner" size={48} />
          <p>{t('common.loading')}</p>
        </div>
        <Footer />
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="product-details-page gaming-theme">
        <Header />
        <div className="error-container">
          <p>{error || t('common.error')}</p>
          <button onClick={() => navigate('/')} className="btn-primary">
            {t('common.back')}
          </button>
        </div>
        <Footer />
      </div>
    );
  }

  // Structured data
  const productStructuredData = {
    "@context": "https://schema.org",
    "@type": "Product",
    "name": product.name,
    "image": Array.isArray(product.images) && product.images.length > 0 ? product.images : [],
    "description": product.description,
    "sku": product.sku,
    "mpn": product.sku,
    "brand": {
      "@type": "Brand",
      "name": product.brand
    },
    "category": product.category,
    "offers": {
      "@type": "Offer",
      "url": `${window.location.origin}/product/${product.id}`,
      "priceCurrency": "SAR",
      "price": product.price,
      "priceValidUntil": new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      "availability": "https://schema.org/InStock",
      "seller": {
        "@type": "Organization",
        "name": product.seller.name
      }
    }
  };

  return (
    <>
      <SEO
        title={`${product.name} - ${product.arabicName}`}
        description={`${t('pages.productDetails.buyNow')} ${product.name} ${t('productCard.sar')} ${product.price}. ${product.description}`}
        keywords={`${product.name}, ${product.arabicName}, ${product.brand}, ${product.category}, GamersStation`}
        type="product"
        image={Array.isArray(product.images) && product.images.length > 0 ? product.images[0] : undefined}
        structuredData={productStructuredData}
      />
      <div className="product-details-page gaming-theme">
        <Header />
        
        {/* Gaming Background Pattern */}
        <div className="gaming-bg-pattern"></div>
        
        <div className="container">
          {/* Product Content - Responsive Layout */}
          <div className="product-content-wrapper">
            
            {/* Left Column - Images & Description */}
            <div className="product-left-column">
              {/* Image Section */}
              <div className="product-image-section">
              {/* Type Badge */}
              <div className={`image-badge type-badge ${postType === 'ASK' ? 'badge-ask' : 'badge-sell'}`}>
                {postType === 'ASK' ? (
                  <>
                    <Package size={18} />
                    <span>{currentLang === 'ar' ? 'مطلوب' : 'Wanted'}</span>
                  </>
                ) : (
                  <>
                    <Check size={18} />
                    <span>{currentLang === 'ar' ? 'للبيع' : 'For Sale'}</span>
                  </>
                )}
              </div>

              {/* Main Image */}
              <div className="main-product-image">
                {product.images[selectedImage].startsWith('http') ? (
                  <OptimizedImage
                    src={product.images[selectedImage]}
                    alt={product.name}
                    className="product-img"
                    priority={true}
                    objectFit="contain"
                  />
                ) : (
                  <div className="product-icon-wrapper">
                    <Gamepad2 size={120} className="product-icon" />
                  </div>
                )}
              </div>

              {/* Image Count Badge */}
              <div className="image-count-badge">
                <Camera size={16} />
                <span>{getImageCountText(product.images.filter(img => img.startsWith('http')).length || product.images.length)}</span>
              </div>

              {/* Expand Button */}
              <button
                className="expand-image-btn"
                onClick={() => setFullscreenImage(true)}
                aria-label="Expand image"
              >
                <Maximize2 size={18} />
              </button>
            </div>

            {/* Thumbnails - Horizontal */}
            <div className="thumbnails-horizontal">
              {product.images.map((image, index) => (
                <button
                  key={index}
                  className={`thumbnail-button ${selectedImage === index ? 'active' : ''}`}
                  onClick={() => handleImageSelect(index)}
                  aria-label={`View image ${index + 1}`}
                >
                  {image.startsWith('http') ? (
                    <img
                      src={image}
                      alt={`${product.name} ${index + 1}`}
                      className="thumbnail-img"
                    />
                  ) : (
                    <Gamepad2 size={28} />
                  )}
                </button>
              ))}
            </div>

            {/* Product Title & Description */}
            <div className="product-header-info">
              <h1 className="product-title">{product.name}</h1>
              <p className="product-brief">{product.description}</p>
            </div>
            </div>
            {/* End Left Column */}
            
            {/* Right Sidebar - Price, Info, Seller, Action */}
            <div className="product-right-sidebar">
            
            {/* Price Card */}
            <div className="price-card">
              <div className="price-card-actions">
                <button
                  className="price-action-btn"
                  onClick={() => setShowShareMenu(!showShareMenu)}
                  aria-label={t('pages.productDetails.share')}
                >
                  <Share2 size={20} />
                </button>
                {showShareMenu && (
                  <div className="share-menu price-share-menu">
                    <button onClick={() => handleShare('whatsapp')}>WhatsApp</button>
                    <button onClick={() => handleShare('facebook')}>Facebook</button>
                    <button onClick={() => handleShare('copy')}>{t('pages.productDetails.copyLink')}</button>
                  </div>
                )}
                <button
                  className={`price-action-btn ${isWishlisted ? 'active' : ''}`}
                  onClick={() => setIsWishlisted(!isWishlisted)}
                  aria-label={t('pages.productDetails.addToWishlist')}
                >
                  <Heart size={20} fill={isWishlisted ? '#ff6b35' : 'none'} />
                </button>
              </div>
              <div className="price-card-info">
                <span className="price-label">{t('pages.productDetails.askingPrice')}</span>
                <div className="price-value-row">
                  <span className="price-amount">{product.price.toLocaleString()}</span>
                  <span className="currency-symbol">{t('currency')}</span>
                </div>
              </div>
            </div>

            {/* Info Cards Row - 2x2 Grid */}
            <div className="info-cards-row">
              <div className="info-card">
                <div className="info-card-icon condition-icon">
                  <Shield size={24} />
                </div>
                <span className="info-card-label">{t('pages.productDetails.condition.label')}</span>
                <span className="info-card-value">{product.specifications[t('pages.productDetails.condition.label')]}</span>
              </div>
              <div className="info-card">
                <div className="info-card-icon location-icon">
                  <MapPin size={24} />
                </div>
                <span className="info-card-label">{t('pages.productDetails.location')}</span>
                <span className="info-card-value">{product.specifications[t('pages.productDetails.city')]}</span>
              </div>
              <div className="info-card">
                <div className="info-card-icon category-icon">
                  <Tag size={24} />
                </div>
                <span className="info-card-label">{currentLang === 'ar' ? 'الفئة' : 'Category'}</span>
                <span className="info-card-value">{product.category}</span>
              </div>
              <div className="info-card">
                <div className="info-card-icon date-icon">
                  <Calendar size={24} />
                </div>
                <span className="info-card-label">{t('pages.productDetails.publishDate')}</span>
                <span className="info-card-value">{product.specifications[t('pages.productDetails.publishDate')]}</span>
              </div>
            </div>

            {/* Seller Information Card */}
            <div className="seller-info-card">
              <div className="seller-header">
                <div className="seller-avatar">
                  {(sellerDetails?.avatar || product.seller.verified) ? (
                    sellerDetails?.avatar ? (
                      <OptimizedImage
                        src={sellerDetails.avatar}
                        alt={product.seller.name}
                        className="seller-avatar-img"
                        objectFit="cover"
                      />
                    ) : (
                      <User size={32} className="seller-avatar-icon" />
                    )
                  ) : (
                    <User size={32} className="seller-avatar-icon" />
                  )}
                  {product.seller.verified && (
                    <div className="verified-badge" title={t('pages.productDetails.verifiedSeller')}>
                      <Check size={12} />
                    </div>
                  )}
                </div>
                <div className="seller-details">
                  <h3 className="seller-name">{sellerDetails?.name || product.seller.name}</h3>
                  <div className="seller-location">
                    <MapPin size={14} />
                    <span>{product.seller.city}</span>
                  </div>
                </div>
              </div>
              
              {/* Seller Info Badges */}
              <div className="seller-badges">
                {sellerDetails?.memberSince && (
                  <div className="seller-badge">
                    <Calendar size={14} />
                    <span>
                      {currentLang === 'ar' ? 'عضو منذ' : 'Member since'} {new Date(sellerDetails.memberSince).getFullYear()}
                    </span>
                  </div>
                )}
                {sellerAdCount > 0 && (
                  <div className="seller-badge seller-badge-count">
                    <Package size={14} />
                    <span>
                      <strong>{sellerAdCount}</strong> {currentLang === 'ar' ? 'إعلان' : 'Ads'}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="action-section">
              {(() => {
                const currentUser = authService.getCurrentUser();
                const isOwner = currentUser && currentUser.userId === product.seller.id;
                if (isOwner && postType !== 'SOLD' && product.availability !== t('pages.productDetails.unavailable')) {
                  return (
                    <>
                      <button
                        className="btn-contact-seller btn-edit-product"
                        onClick={() => navigate(`/edit-product/${product.id}`)}
                      >
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <span>{t('common.edit')}</span>
                      </button>
                      <button
                        className="btn-contact-seller btn-mark-sold"
                        onClick={() => setShowMarkSoldModal(true)}
                      >
                        <Check size={20} />
                        <span>{t('markAsSold.button')}</span>
                      </button>
                    </>
                  );
                }
                return null;
              })()}

              {(() => {
                const currentUser = authService.getCurrentUser();
                const isOwner = currentUser && currentUser.userId === product.seller.id;
                return !isOwner;
              })() && (
                <button
                  className="btn-contact-seller"
                  onClick={async () => {
                    if (!authService.isAuthenticated()) {
                      navigate('/login', {
                        state: { redirectTo: `/product/${product.id}` }
                      });
                      return;
                    }
                    try {
                      const conversations = await messagingService.getConversations(0, 100);
                      const existingConversation = conversations?.content?.find(
                        conv => conv.post?.id === product.id
                      );
                      if (existingConversation) {
                        navigate(`/chat/${existingConversation.id}`);
                        return;
                      }
                      const initialMessage = t('chat.interestedInProduct', { productName: product.name }) || `Hi, I'm interested in ${product.name}`;
                      const conversation = await messagingService.startConversation(
                        product.id,
                        initialMessage
                      );
                      if (!conversation || !conversation.id) {
                        throw new Error('Invalid conversation response');
                      }
                      navigate(`/chat/${conversation.id}`, {
                        state: { initialMessage, justCreated: true }
                      });
                    } catch (error) {
                      let errorMessage = t('chat.errorStartingConversation') || 'لا يمكن بدء المحادثة. يرجى المحاولة مرة أخرى';
                      if (error.message.includes('[400]')) {
                        if (error.message.includes('yourself')) {
                          errorMessage = t('chat.cannotMessageYourself');
                        } else if (error.message.includes('inactive')) {
                          errorMessage = t('chat.productInactive') || 'Cannot start conversation on inactive product';
                        } else {
                          errorMessage = t('chat.invalidRequest') || 'Cannot start conversation. Please check the product details.';
                        }
                      } else if (error.message.includes('[401]') || error.message.includes('[403]')) {
                        errorMessage = t('chat.authenticationError') || 'Authentication error. Please login again.';
                        authService.clearTokens();
                        navigate('/login', {
                          state: { redirectTo: `/product/${product.id}` }
                        });
                        return;
                      } else if (error.message.includes('[404]')) {
                        errorMessage = t('chat.productNotFound') || 'Product not found.';
                      }
                      showError({ messageAr: errorMessage, messageEn: errorMessage });
                    }
                  }}
                >
                  <MessageCircle size={20} />
                  <span>{t('pages.productDetails.contactSeller')}</span>
                </button>
              )}
            </div>
            
            </div>
            {/* End Right Sidebar */}

            {/* Copy Link Notification */}
            {showCopyNotification && (
              <div className="copy-notification">
                <Check size={20} />
                <span>{t('pages.productDetails.linkCopied')}</span>
              </div>
            )}

          </div>


          {/* Similar Products Section - Modern Design */}
          <div className="related-products modern">
            <div className="section-header">
              <div className="section-title-wrapper">
                <div className="title-icon">
                  <Gamepad2 size={28} />
                </div>
                <h2 className="section-title">
                  {t('pages.productDetails.similarProducts')}
                </h2>
              </div>
              <div className="section-line"></div>
            </div>
            
            <div className="related-grid modern-grid">
              {relatedProducts.length > 0 ? (
                relatedProducts.map((item, index) => (
                  <div
                    key={item.id}
                    className="related-card modern-card"
                    style={{ animationDelay: `${index * 0.1}s`, cursor: 'pointer' }}
                    onClick={() => navigate(`/product/${item.id}`)}
                  >
                    <div className="card-glow-effect"></div>
                    
                    {/* Image Section */}
                    <div className="related-image-wrapper">
                      <div className="image-overlay"></div>
                      {item.image.startsWith('http') ? (
                        <OptimizedImage
                          src={item.image}
                          alt={item.name}
                          className="related-img"
                          objectFit="cover"
                        />
                      ) : (
                        <div className="placeholder-image">
                          <Gamepad2 size={80} className="related-icon" />
                        </div>
                      )}
                      
                      {/* Quick View Button */}
                      <button
                        className="quick-view-btn"
                        onClick={(e) => {
                          e.preventDefault();
                          navigate(`/product/${item.id}`);
                        }}
                        aria-label={t('pages.productDetails.viewProduct')}
                      >
                        <Eye size={20} />
                      </button>
                    </div>
                    
                    {/* Info Section */}
                    <div className="related-content">
                      <h3 className="related-title">{item.name}</h3>
                      
                      <div className="price-section">
                        <div className="price-wrapper">
                          <span className="currency-prefix">{t('currency')}</span>
                          <span className="price-value">{item.price.toLocaleString()}</span>
                        </div>
                      </div>
                      
                      {/* Action Button */}
                      <button
                        className="view-product-btn"
                        onClick={(e) => {
                          e.preventDefault();
                          navigate(`/product/${item.id}`);
                        }}
                      >
                        <span>{t('pages.productDetails.viewDetails') || 'View Details'}</span>
                        <ChevronRight size={16} />
                      </button>
                    </div>
                  </div>
                ))
              ) : (
                // Modern Skeleton Loaders
                [...Array(4)].map((_, index) => (
                  <div key={`skeleton-${index}`} className="related-card modern-card skeleton-card">
                    <div className="skeleton-image"></div>
                    <div className="related-content">
                      <div className="skeleton-title"></div>
                      <div className="skeleton-price"></div>
                      <div className="skeleton-button"></div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        <Footer />
      </div>

      {/* Mark as Sold Modal */}
      {showMarkSoldModal && (
        <MarkAsSoldModal
          postId={product.id}
          onClose={() => setShowMarkSoldModal(false)}
          onSuccess={() => fetchProductDetails()}
        />
      )}

      {/* Fullscreen Image Viewer */}
      {fullscreenImage && (
        <div className="fullscreen-overlay" onClick={() => setFullscreenImage(false)}>
          <button
            className="fullscreen-close"
            onClick={() => setFullscreenImage(false)}
            aria-label="Close fullscreen"
          >
            &times;
          </button>

          {product.images.length > 1 && (
            <button
              className="fullscreen-nav fullscreen-prev"
              onClick={(e) => {
                e.stopPropagation();
                setSelectedImage((prev) => (prev - 1 + product.images.length) % product.images.length);
              }}
              aria-label="Previous image"
            >
              <ChevronLeft size={32} />
            </button>
          )}

          <div className="fullscreen-image-container" onClick={(e) => e.stopPropagation()}>
            {product.images[selectedImage].startsWith('http') ? (
              <img
                src={product.images[selectedImage]}
                alt={product.name}
                className="fullscreen-img"
              />
            ) : (
              <Gamepad2 size={200} className="fullscreen-placeholder-icon" />
            )}
          </div>

          {product.images.length > 1 && (
            <button
              className="fullscreen-nav fullscreen-next"
              onClick={(e) => {
                e.stopPropagation();
                setSelectedImage((prev) => (prev + 1) % product.images.length);
              }}
              aria-label="Next image"
            >
              <ChevronRight size={32} />
            </button>
          )}

          {product.images.length > 1 && (
            <div className="fullscreen-counter">
              {selectedImage + 1} / {product.images.length}
            </div>
          )}
        </div>
      )}
    </>
  );
};

export default ProductDetailsPage;