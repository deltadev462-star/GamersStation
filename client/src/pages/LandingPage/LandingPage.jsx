import React, { useState } from 'react';
import { Plus, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Header from '../../components/Header/Header';
// import Hero from '../../components/Hero/Hero';
import CategoryFilter from '../../components/CategoryFilter/CategoryFilter';
import LocationFilter from '../../components/LocationFilter/LocationFilter';
import PostTypeToggle from '../../components/PostTypeToggle/PostTypeToggle';
import ProductGrid from '../../components/ProductGrid/ProductGrid';
import Footer from '../../components/Footer/Footer';
import SEO from '../../components/SEO/SEO';
import './LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  // State for selected category filter
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [selectedSubcategoryType, setSelectedSubcategoryType] = useState(null);
  const [selectedPostType, setSelectedPostType] = useState('SELL');
  const [selectedRegionId, setSelectedRegionId] = useState(null);
  const [selectedCityId, setSelectedCityId] = useState(null);

  // Handle category filter change
  const handleCategoryChange = (categoryId, categoryIds) => {
    if (categoryIds) {
      // Multiple category IDs for cross-platform subcategory search
      setSelectedCategoryId(null);
      setSelectedSubcategoryType(categoryIds);
    } else {
      // Single category ID for platform-specific search
      setSelectedCategoryId(categoryId);
      setSelectedSubcategoryType(null);
    }
  };

  // Handle location filter change
  const handleLocationChange = ({ regionId, cityId }) => {
    setSelectedRegionId(regionId);
    setSelectedCityId(cityId);
  };
  // Structured data for the home page
  const homeStructuredData = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "WebSite",
        "@id": "https://gamersstation.eg/#website",
        "url": "https://gamersstation.eg/",
        "name": "GamersStation",
        "description": "أكبر سوق للألعاب الإلكترونية في السعودية",
        "publisher": {
          "@id": "https://gamersstation.eg/#organization"
        },
        "potentialAction": {
          "@type": "SearchAction",
          "target": {
            "@type": "EntryPoint",
            "urlTemplate": "https://gamersstation.eg/search?q={search_term_string}"
          },
          "query-input": "required name=search_term_string"
        },
        "inLanguage": "ar-SA"
      },
      {
        "@type": "Organization",
        "@id": "https://gamersstation.eg/#organization",
        "name": "GamersStation",
        "url": "https://gamersstation.eg/",
        "logo": {
          "@type": "ImageObject",
          "@id": "https://gamersstation.eg/#logo",
          "url": "https://gamersstation.eg/logo.svg",
          "contentUrl": "https://gamersstation.eg/logo.svg",
          "caption": "GamersStation"
        },
        "image": {
          "@id": "https://gamersstation.eg/#logo"
        },
        "sameAs": [
          "https://www.facebook.com/GamerStationApp",
          "https://www.twitter.com/GamerStationApp",
          "https://www.instagram.com/GamerStationApp",
          "https://www.youtube.com/@GamerStationApp"
        ],
        "address": {
          "@type": "PostalAddress",
          "addressCountry": "SA",
          "addressLocality": "الرياض"
        }
      },
      {
        "@type": "BreadcrumbList",
        "@id": "https://gamersstation.eg/#breadcrumb",
        "itemListElement": [
          {
            "@type": "ListItem",
            "position": 1,
            "name": "الرئيسية",
            "item": "https://gamersstation.eg/"
          }
        ]
      }
    ]
  };

  return (
    <>
      <SEO
        title=""
        description="تسوق أحدث الألعاب والأجهزة من PlayStation 5، Xbox Series X، Nintendo Switch، وأجهزة الكمبيوتر. أفضل الأسعار والعروض من تجار موثوقين في السعودية. توصيل سريع وضمان أصلي."
        keywords="ألعاب إلكترونية, بلايستيشن 5, إكس بوكس, نينتندو سويتش, ألعاب كمبيوتر, PS5, Xbox Series X, Nintendo Switch, gaming السعودية, متجر ألعاب, أجهزة ألعاب"
        structuredData={homeStructuredData}
        type="website"
      />
      <div className="landing-page">
        <Header />
        {/* <Hero /> */}
        
        {/* Post Type Toggle */}
        <PostTypeToggle
          selectedType={selectedPostType}
          onTypeChange={setSelectedPostType}
        />
        
        <CategoryFilter onFilterChange={handleCategoryChange} />
        <LocationFilter onFilterChange={handleLocationChange} />
        <main className="main-content">
          <ProductGrid
            categoryId={selectedCategoryId}
            subcategoryType={selectedSubcategoryType}
            regionId={selectedRegionId}
            cityId={selectedCityId}
            postType={selectedPostType}
            hideLoadMore={false}
          />
          
          {/*/!* Show All Products Button *!/*/}
          {/*<div className="show-all-products-container">*/}
          {/*  <button*/}
          {/*    className="show-all-products-btn"*/}
          {/*    onClick={() => navigate('/products')}*/}
          {/*  >*/}
          {/*    {t('common.showAllProducts', 'Show All Products')}*/}
          {/*    <ArrowRight size={20} className="arrow-icon" />*/}
          {/*  </button>*/}
          {/*</div>*/}
        </main>
        <Footer />
        
        {/* Floating Add Product Button */}
        <button
          className="floating-add-btn"
          onClick={() => navigate('/add-product')}
          aria-label="Add Product"
        >
          <Plus size={24} />
        </button>
      </div>
    </>
  );
};

export default LandingPage;