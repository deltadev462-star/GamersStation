import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, ChevronRight, ChevronDown, ArrowUpDown, Check } from 'lucide-react';
import ProductCard from '../ProductCard/ProductCard';
import postService from '../../services/postService';
import { SkeletonLoader } from '../Loading/Loading';
import './ProductGrid.css';

const ProductGrid = ({ categoryId, subcategoryType, searchQuery, regionId, cityId, minPrice, maxPrice, condition, sortBy: externalSortBy, direction: externalDirection, postType, hideLoadMore = false }) => {
  const { t, i18n } = useTranslation();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Sort state
  const [sortBy, setSortBy] = useState(externalSortBy || 'createdAt');
  const [direction, setDirection] = useState(externalDirection || 'DESC');
  const [showSortModal, setShowSortModal] = useState(false);

  // Fetch products from backend
  const fetchProducts = async (pageNumber = 0, append = false) => {
    try {
      if (!append) {
        setLoading(true);
      }
      setError(null);

      const params = {
        page: pageNumber,
        size: 10, // Reduced from 20 to 10 to show pagination with fewer products
        sortBy: sortBy || 'createdAt',
        direction: direction || 'DESC',
      };

      // Add filters if provided
      if (categoryId) {
        params.categoryId = categoryId;
      } else if (subcategoryType && Array.isArray(subcategoryType)) {
        // Handle multiple category IDs for cross-platform subcategory search
        params.categoryIds = subcategoryType.join(',');
      }
      if (regionId) {
        params.regionId = regionId;
      }
      if (cityId) {
        params.cityId = cityId;
      }
      if (minPrice) {
        params.minPrice = minPrice;
      }
      if (maxPrice) {
        params.maxPrice = maxPrice;
      }
      if (condition) {
        params.condition = condition;
      }
      if (postType) {
        params.type = postType;
      }

      // Use search endpoint if search query is provided
      const response = searchQuery
        ? await postService.searchPosts({ ...params, q: searchQuery })
        : await postService.getPosts(params);

      const transformedProducts = postService.transformPosts(response.content || []);
      
      if (append) {
        setProducts(prev => [...prev, ...transformedProducts]);
      } else {
        setProducts(transformedProducts);
      }

      setPage(pageNumber);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      
      // Debug logging
      // console.log('Pagination Debug:', {
      //   pageNumber,
      //   totalPages: response.totalPages,
      //   totalElements: response.totalElements,
      //   productCount: transformedProducts.length,
      //   hideLoadMore
      // });
    } catch (err) {
      console.error('Error fetching products:', err);
      setError(err.message || t('common.error'));
    } finally {
      setLoading(false);
    }
  };

  // Handle sort change
  const handleSortChange = (newSortBy, newDirection) => {
    setSortBy(newSortBy);
    setDirection(newDirection);
    setShowSortModal(false);
  };

  // Get current sort option label
  const getCurrentSortLabel = () => {
    if (sortBy === 'createdAt' && direction === 'DESC') return t('allProducts.sortNewest');
    if (sortBy === 'createdAt' && direction === 'ASC') return t('allProducts.sortOldest');
    if (sortBy === 'price' && direction === 'ASC') return t('allProducts.sortPriceLow');
    if (sortBy === 'price' && direction === 'DESC') return t('allProducts.sortPriceHigh');
    return t('allProducts.sortNewest');
  };

  // Fetch products on mount and when filters change
  useEffect(() => {
    fetchProducts(0, false);
  }, [categoryId, subcategoryType, searchQuery, regionId, cityId, minPrice, maxPrice, condition, sortBy, direction, postType]);


  // Loading state
  if (loading) {
    return (
      <div className="product-grid">
        <SkeletonLoader type="card" count={6} />
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="product-grid">
        <div className="error-container">
          <p className="error-message">{error}</p>
          <button onClick={() => fetchProducts(0, false)} className="retry-button">
            {t('common.retry')}
          </button>
        </div>
      </div>
    );
  }

  // Empty state
  if (products.length === 0) {
    return (
      <div className="product-grid">
        <div className="empty-state">
          <svg className="empty-icon" width="100" height="100" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 2L3 9H8V22H10V9H15L9 2Z" fill="currentColor" opacity="0.3"/>
            <path d="M15 22L21 15H16V2H14V15H9L15 22Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <h3>{t('common.noResults')}</h3>
          <p>{searchQuery 
            ? t('productGrid.noSearchResults', { query: searchQuery })
            : t('productGrid.noProducts')
          }</p>
        </div>
      </div>
    );
  }

  return (
    <div className="product-grid">
      {/* Section Header with Sort */}
      <div className="grid-section-header">
        <h2 className="grid-section-title">{t('common.products', 'المنتجات')}</h2>
        

        {/* Sort Button - Mobile */}
        <button 
          className="sort-mobile-btn mobile-sort"
          onClick={() => setShowSortModal(true)}
        >
          <ArrowUpDown size={16} />
          <span>{getCurrentSortLabel()}</span>
        </button>
      </div>
      
      {/* Mobile Sort Modal */}
      {showSortModal && (
        <div 
          className="sort-modal-overlay mobile-sort" 
          onClick={(e) => {
            if (e.target.classList.contains('sort-modal-overlay')) {
              setShowSortModal(false);
            }
          }}
          onTouchStart={(e) => {
            const touchStartY = e.touches[0].clientY;
            const handleTouchMove = (moveEvent) => {
              const touchEndY = moveEvent.touches[0].clientY;
              const deltaY = touchEndY - touchStartY;
              if (deltaY > 100) {
                setShowSortModal(false);
                document.removeEventListener('touchmove', handleTouchMove);
              }
            };
            document.addEventListener('touchmove', handleTouchMove);
            document.addEventListener('touchend', () => {
              document.removeEventListener('touchmove', handleTouchMove);
            }, { once: true });
          }}
        >
          <div className="sort-modal" onClick={(e) => e.stopPropagation()}>
            <div className="sort-modal-header">
              <div className="sort-modal-handle" />
              <h3>{t('allProducts.sortBy')}</h3>
            </div>
            
            <div className="sort-modal-options">
              <label className="sort-modal-option">
                <input
                  type="radio"
                  name="sort"
                  checked={sortBy === 'createdAt' && direction === 'DESC'}
                  onChange={() => handleSortChange('createdAt', 'DESC')}
                />
                <span>{t('allProducts.sortNewest')}</span>
                {sortBy === 'createdAt' && direction === 'DESC' && <Check size={20} className="check-icon" />}
              </label>
              
              <label className="sort-modal-option">
                <input
                  type="radio"
                  name="sort"
                  checked={sortBy === 'createdAt' && direction === 'ASC'}
                  onChange={() => handleSortChange('createdAt', 'ASC')}
                />
                <span>{t('allProducts.sortOldest')}</span>
                {sortBy === 'createdAt' && direction === 'ASC' && <Check size={20} className="check-icon" />}
              </label>
              
              <label className="sort-modal-option">
                <input
                  type="radio"
                  name="sort"
                  checked={sortBy === 'price' && direction === 'ASC'}
                  onChange={() => handleSortChange('price', 'ASC')}
                />
                <span>{t('allProducts.sortPriceLow')}</span>
                {sortBy === 'price' && direction === 'ASC' && <Check size={20} className="check-icon" />}
              </label>
              
              <label className="sort-modal-option">
                <input
                  type="radio"
                  name="sort"
                  checked={sortBy === 'price' && direction === 'DESC'}
                  onChange={() => handleSortChange('price', 'DESC')}
                />
                <span>{t('allProducts.sortPriceHigh')}</span>
                {sortBy === 'price' && direction === 'DESC' && <Check size={20} className="check-icon" />}
              </label>
            </div>
            
            <div className="sort-modal-actions">
              <button className="sort-modal-apply" onClick={() => setShowSortModal(false)}>
                {t('common.apply', 'Apply')}
              </button>
              <button 
                className="sort-modal-reset" 
                onClick={() => {
                  handleSortChange('createdAt', 'DESC');
                  setShowSortModal(false);
                }}
              >
                {t('common.reset', 'Reset')}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="grid-container">
        {products.map(product => (
          <ProductCard
            key={product.id}
            id={product.id}
            title={product.title}
            price={product.price}
            image={product.image}
            thumbnailUrl={product.thumbnailUrl}
            isHighlighted={product.isNew}
            badge={product.onSale ? (i18n.language === 'ar' ? 'عرض' : 'Sale') : null}
            username={product.ownerUsername}
            location={product.cityName}
            originalPrice={product.originalPrice}
            condition={product.condition}
            type={product.type}
            status={product.status}
          />
        ))}
      </div>
      
      {/* Pagination */}
      {!hideLoadMore && (
        <div className="pagination-container">
          <div className="pagination-info">
            <span>{t('pagination.showing')} {page * 10 + 1} - {Math.min((page + 1) * 10, totalElements)} {t('pagination.of')} {totalElements} {t('pagination.products')}</span>
          </div>
          
          <div className="pagination-controls">
            <button
              className="pagination-btn prev"
              onClick={() => fetchProducts(page - 1, false)}
              disabled={page === 0}
              aria-label={t('common.previous')}
            >
              <ChevronLeft size={18} />
            </button>
            
            <div className="page-numbers">
              {/* First page */}
              {totalPages > 5 && page > 2 && (
                <>
                  <button
                    className="page-number"
                    onClick={() => fetchProducts(0, false)}
                  >
                    1
                  </button>
                  {page > 3 && <span className="page-dots">...</span>}
                </>
              )}
              
              {/* Pages around current page */}
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let pageNum;
                if (totalPages <= 5) {
                  pageNum = i;
                } else if (page < 3) {
                  pageNum = i;
                } else if (page > totalPages - 4) {
                  pageNum = totalPages - 5 + i;
                } else {
                  pageNum = page - 2 + i;
                }
                
                if (pageNum >= 0 && pageNum < totalPages) {
                  return (
                    <button
                      key={pageNum}
                      className={`page-number ${pageNum === page ? 'active' : ''}`}
                      onClick={() => pageNum !== page && fetchProducts(pageNum, false)}
                    >
                      {pageNum + 1}
                    </button>
                  );
                }
                return null;
              })}
              
              {/* Last page */}
              {totalPages > 5 && page < totalPages - 3 && (
                <>
                  {page < totalPages - 4 && <span className="page-dots">...</span>}
                  <button
                    className="page-number"
                    onClick={() => fetchProducts(totalPages - 1, false)}
                  >
                    {totalPages}
                  </button>
                </>
              )}
            </div>
            
            <button
              className="pagination-btn next"
              onClick={() => fetchProducts(page + 1, false)}
              disabled={page >= totalPages - 1 || totalPages === 0}
              aria-label={t('common.next')}
            >
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductGrid;