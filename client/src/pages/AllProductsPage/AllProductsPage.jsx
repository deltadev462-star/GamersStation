
import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import { Search, Filter, X, ChevronDown, ChevronUp } from 'lucide-react';
import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import ProductGrid from '../../components/ProductGrid/ProductGrid';
import PostTypeToggle from '../../components/PostTypeToggle/PostTypeToggle';
import SEO from '../../components/SEO/SEO';
import categoryService from '../../services/categoryService';
import cityService from '../../services/cityService';
import { showError } from '../../components/ErrorNotification/ErrorNotification';
import './AllProductsPage.css';

const AllProductsPage = () => {
  const { t, i18n } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  
  // State management
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [showFilters, setShowFilters] = useState(window.innerWidth > 768);
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Filter states
  const [filters, setFilters] = useState({
    categoryId: searchParams.get('category') || '',
    cityId: searchParams.get('city') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    condition: searchParams.get('condition') || '',
    sortBy: searchParams.get('sortBy') || 'createdAt',
    direction: searchParams.get('direction') || 'DESC',
    postType: searchParams.get('postType') || ''
  });

  // Expanded sections state
  const [expandedSections, setExpandedSections] = useState({
    category: true,
    price: true,
    city: true,
    condition: true
  });

  // Load categories and cities
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const [categoriesData, citiesData] = await Promise.all([
          categoryService.getCategories(),
          cityService.getCities()
        ]);
        setCategories(categoriesData);
        setCities(citiesData);
      } catch (error) {
        console.error('Error loading data:', error);
        showError(error);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  // Update URL params when filters change
  useEffect(() => {
    const params = new URLSearchParams();
    if (searchQuery) params.set('q', searchQuery);
    if (filters.categoryId) params.set('category', filters.categoryId);
    if (filters.cityId) params.set('city', filters.cityId);
    if (filters.minPrice) params.set('minPrice', filters.minPrice);
    if (filters.maxPrice) params.set('maxPrice', filters.maxPrice);
    if (filters.condition) params.set('condition', filters.condition);
    if (filters.sortBy !== 'createdAt') params.set('sortBy', filters.sortBy);
    if (filters.direction !== 'DESC') params.set('direction', filters.direction);
    if (filters.postType) params.set('postType', filters.postType);
    
    setSearchParams(params);
  }, [searchQuery, filters, setSearchParams]);

  // Handle search
  const handleSearch = useCallback((value) => {
    setSearchQuery(value);
  }, []);

  // Handle filter changes
  const handleFilterChange = (filterType, value) => {
    setFilters(prev => ({
      ...prev,
      [filterType]: value
    }));
  };

  // Clear all filters
  const clearAllFilters = () => {
    setFilters({
      categoryId: '',
      cityId: '',
      minPrice: '',
      maxPrice: '',
      condition: '',
      sortBy: 'createdAt',
      direction: 'DESC',
      postType: ''
    });
    setSearchQuery('');
  };

  // Handle post type toggle
  const handlePostTypeChange = (type) => {
    setFilters(prev => ({
      ...prev,
      postType: type || ''
    }));
  };

  // Toggle section expansion
  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  // Count active filters
  const activeFiltersCount = Object.entries(filters).filter(([key, v]) => v && v !== 'createdAt' && v !== 'DESC' && key !== 'postType').length + (searchQuery ? 1 : 0);

  // Close filters on mobile when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (window.innerWidth <= 768 && showFilters) {
        const sidebar = document.querySelector('.filters-sidebar');
        const toggleBtn = document.querySelector('.mobile-filter-toggle');
        
        if (sidebar && !sidebar.contains(event.target) && !toggleBtn.contains(event.target)) {
          setShowFilters(false);
        }
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showFilters]);

  // Handle window resize
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 768) {
        setShowFilters(true);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <>
      <SEO 
        title={t('allProducts.pageTitle')}
        description={t('allProducts.pageDescription')}
      />
      
      <Header />
      
      <div className="all-products-page">
        <div className="container">
          {/* Search Bar */}
          <div className="search-section">
            <div className="search-container">
              <Search className="search-icon" size={20} />
              <input
                type="text"
                className="search-input"
                placeholder={t('allProducts.searchPlaceholder')}
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
              />
              {searchQuery && (
                <button 
                  className="clear-search"
                  onClick={() => handleSearch('')}
                  aria-label={t('common.clear')}
                >
                  <X size={18} />
                </button>
              )}
            </div>
            
            {/* Mobile Filter Toggle */}
            <button 
              className="mobile-filter-toggle"
              onClick={() => setShowFilters(!showFilters)}
            >
              <Filter size={20} />
              <span>{t('allProducts.filters')}</span>
              {activeFiltersCount > 0 && (
                <span className="filter-count">{activeFiltersCount}</span>
              )}
            </button>
          </div>

          <div className="products-layout">
            {/* Filters Sidebar */}
            <aside className={`filters-sidebar ${showFilters ? 'show' : ''}`}>
              <div className="filters-header">
                <h2>{t('allProducts.filters')}</h2>
                <div className="filters-header-actions">
                  {activeFiltersCount > 0 && (
                    <button
                      className="clear-filters"
                      onClick={clearAllFilters}
                    >
                      {t('allProducts.clearAll')}
                    </button>
                  )}
                  {/* Mobile close button */}
                  <button
                    className="mobile-close-filters"
                    onClick={() => setShowFilters(false)}
                    aria-label={t('common.close')}
                  >
                    <X size={24} />
                  </button>
                </div>
              </div>

              {/* Category Filter */}
              <div className="filter-section">
                <button 
                  className="filter-section-header"
                  onClick={() => toggleSection('category')}
                >
                  <span>{t('allProducts.category')}</span>
                  {expandedSections.category ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                </button>
                {expandedSections.category && (
                  <div className="filter-options">
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="category"
                        value=""
                        checked={!filters.categoryId}
                        onChange={() => handleFilterChange('categoryId', '')}
                      />
                      <span>{t('allProducts.allCategories')}</span>
                    </label>
                    {categories.map(category => (
                      <label key={category.id} className="filter-option">
                        <input
                          type="radio"
                          name="category"
                          value={category.id}
                          checked={filters.categoryId === category.id.toString()}
                          onChange={() => handleFilterChange('categoryId', category.id.toString())}
                        />
                        <span>{i18n.language === 'ar' ? category.nameAr : category.nameEn}</span>
                      </label>
                    ))}
                  </div>
                )}
              </div>

              {/* Price Filter */}
              <div className="filter-section">
                <button 
                  className="filter-section-header"
                  onClick={() => toggleSection('price')}
                >
                  <span>{t('allProducts.price')}</span>
                  {expandedSections.price ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                </button>
                {expandedSections.price && (
                  <div className="filter-options">
                    <div className="price-inputs">
                      <input
                        type="number"
                        className="price-input"
                        placeholder={t('allProducts.minPrice')}
                        value={filters.minPrice}
                        onChange={(e) => handleFilterChange('minPrice', e.target.value)}
                      />
                      <input
                        type="number"
                        className="price-input"
                        placeholder={t('allProducts.maxPrice')}
                        value={filters.maxPrice}
                        onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
                      />
                    </div>
                    <div className="price-presets">
                      <button 
                        className="price-preset"
                        onClick={() => {
                          handleFilterChange('minPrice', '0');
                          handleFilterChange('maxPrice', '100');
                        }}
                      >
                        0 - 100 {t('currency')}
                      </button>
                      <button 
                        className="price-preset"
                        onClick={() => {
                          handleFilterChange('minPrice', '100');
                          handleFilterChange('maxPrice', '500');
                        }}
                      >
                        100 - 500 {t('currency')}
                      </button>
                      <button 
                        className="price-preset"
                        onClick={() => {
                          handleFilterChange('minPrice', '500');
                          handleFilterChange('maxPrice', '1000');
                        }}
                      >
                        500 - 1000 {t('currency')}
                      </button>
                      <button 
                        className="price-preset"
                        onClick={() => {
                          handleFilterChange('minPrice', '1000');
                          handleFilterChange('maxPrice', '');
                        }}
                      >
                        1000+ {t('currency')}
                      </button>
                    </div>
                  </div>
                )}
              </div>

              {/* City Filter */}
              <div className="filter-section">
                <button 
                  className="filter-section-header"
                  onClick={() => toggleSection('city')}
                >
                  <span>{t('allProducts.city')}</span>
                  {expandedSections.city ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                </button>
                {expandedSections.city && (
                  <div className="filter-options scrollable">
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="city"
                        value=""
                        checked={!filters.cityId}
                        onChange={() => handleFilterChange('cityId', '')}
                      />
                      <span>{t('allProducts.allCities')}</span>
                    </label>
                    {cities.map(city => (
                      <label key={city.id} className="filter-option">
                        <input
                          type="radio"
                          name="city"
                          value={city.id}
                          checked={filters.cityId === city.id.toString()}
                          onChange={() => handleFilterChange('cityId', city.id.toString())}
                        />
                        <span>{i18n.language === 'ar' ? city.nameAr : city.nameEn}</span>
                      </label>
                    ))}
                  </div>
                )}
              </div>

              {/* Condition Filter */}
              <div className="filter-section">
                <button 
                  className="filter-section-header"
                  onClick={() => toggleSection('condition')}
                >
                  <span>{t('allProducts.condition')}</span>
                  {expandedSections.condition ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                </button>
                {expandedSections.condition && (
                  <div className="filter-options">
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="condition"
                        value=""
                        checked={!filters.condition}
                        onChange={() => handleFilterChange('condition', '')}
                      />
                      <span>{t('allProducts.allConditions')}</span>
                    </label>
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="condition"
                        value="NEW"
                        checked={filters.condition === 'NEW'}
                        onChange={() => handleFilterChange('condition', 'NEW')}
                      />
                      <span>{t('allProducts.conditionNew')}</span>
                    </label>
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="condition"
                        value="LIKE_NEW"
                        checked={filters.condition === 'LIKE_NEW'}
                        onChange={() => handleFilterChange('condition', 'LIKE_NEW')}
                      />
                      <span>{t('allProducts.conditionLikeNew')}</span>
                    </label>
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="condition"
                        value="GOOD"
                        checked={filters.condition === 'GOOD'}
                        onChange={() => handleFilterChange('condition', 'GOOD')}
                      />
                      <span>{t('allProducts.conditionGood')}</span>
                    </label>
                    <label className="filter-option">
                      <input
                        type="radio"
                        name="condition"
                        value="FAIR"
                        checked={filters.condition === 'FAIR'}
                        onChange={() => handleFilterChange('condition', 'FAIR')}
                      />
                      <span>{t('allProducts.conditionFair')}</span>
                    </label>
                  </div>
                )}
              </div>
            </aside>

            {/* Products Grid */}
            <main className="products-main">
              {/* Sort Options */}
              <div className="sort-section">
                <div className="results-info">
                  {searchQuery && (
                    <span>{t('allProducts.searchingFor', { query: searchQuery })}</span>
                  )}
                </div>
                <div className="sort-options">
                  <label>{t('allProducts.sortBy')}</label>
                  <select 
                    className="sort-select"
                    value={`${filters.sortBy}-${filters.direction}`}
                    onChange={(e) => {
                      const [sortBy, direction] = e.target.value.split('-');
                      handleFilterChange('sortBy', sortBy);
                      handleFilterChange('direction', direction);
                    }}
                  >
                    <option value="createdAt-DESC">{t('allProducts.sortNewest')}</option>
                    <option value="createdAt-ASC">{t('allProducts.sortOldest')}</option>
                    <option value="price-ASC">{t('allProducts.sortPriceLow')}</option>
                    <option value="price-DESC">{t('allProducts.sortPriceHigh')}</option>
                    <option value="views-DESC">{t('allProducts.sortMostViewed')}</option>
                  </select>
                </div>
              </div>

              {/* Products Grid Component */}
              <ProductGrid
                searchQuery={searchQuery}
                categoryId={filters.categoryId}
                cityId={filters.cityId}
                minPrice={filters.minPrice}
                maxPrice={filters.maxPrice}
                condition={filters.condition}
                sortBy={filters.sortBy}
                direction={filters.direction}
                postType={filters.postType || null}
              />
            </main>
          </div>
        </div>
      </div>
      
    
    </>
  );
};

export default AllProductsPage;
