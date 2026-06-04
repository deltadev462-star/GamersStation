import React from 'react';
import { Helmet } from 'react-helmet-async';

const SEO = ({ 
  title = '',
  description = 'منصتك الأولى للألعاب والمنتجات الرقمية في المملكة العربية السعودية.',
  keywords = 'ألعاب إلكترونية, بلايستيشن, إكس بوكس, نينتندو, ألعاب كمبيوتر, PS5, Xbox Series X, gaming, السعودية',
  image = 'https://gamers-station.com/og-image.jpg',
  url = 'https://gamers-station.com',
  type = 'website',
  author = 'GamersStation',
  structuredData = null,
  canonicalUrl = null,
  alternateLinks = [],
  noindex = false,
  nofollow = false
}) => {
  const siteTitle = 'GamersStation - أكبر سوق للألعاب الإلكترونية في السعودية';
  const fullTitle = title ? `${title} | ${siteTitle}` : siteTitle;
  const currentUrl = canonicalUrl || `${url}${typeof window !== 'undefined' ? window.location.pathname : ''}`;
  
  // Default structured data for Organization
  const defaultStructuredData = {
    "@context": "https://schema.org",
    "@type": "Organization",
    "name": "GamersStation",
    "url": "https://gamers-station.com",
    "logo": "https://gamers-station.com/logo.svg",
    "description": "أكبر سوق للألعاب الإلكترونية في السعودية",
    "address": {
      "@type": "PostalAddress",
      "addressCountry": "SA",
      "addressRegion": "الرياض"
    },
    "contactPoint": {
      "@type": "ContactPoint",
      "email": "contact@thegamersstation.com",
      "contactType": "customer service",
      "availableLanguage": ["Arabic", "English"]
    },
    "sameAs": [
      "https://www.facebook.com/GamersStationApp",
      "https://www.twitter.com/GamersStationApp",
      "https://www.instagram.com/GamersStationApp",
      "https://www.youtube.com/GamersStationApp"
    ]
  };

  const finalStructuredData = structuredData || defaultStructuredData;

  return (
    <Helmet>
      {/* Basic Meta Tags */}
      <title>{fullTitle}</title>
      <meta name="description" content={description} />
      <meta name="keywords" content={keywords} />
      <meta name="author" content={author} />
      
      {/* Robots Meta Tags */}
      <meta 
        name="robots" 
        content={`${noindex ? 'noindex' : 'index'}, ${nofollow ? 'nofollow' : 'follow'}, max-image-preview:large, max-snippet:-1, max-video-preview:-1`} 
      />
      <meta 
        name="googlebot" 
        content={`${noindex ? 'noindex' : 'index'}, ${nofollow ? 'nofollow' : 'follow'}, max-image-preview:large, max-snippet:-1, max-video-preview:-1`} 
      />
      
      {/* Canonical URL */}
      <link rel="canonical" href={currentUrl} />
      
      {/* Alternate Language Links */}
      <link rel="alternate" hreflang="ar-SA" href={currentUrl} />
      <link rel="alternate" hreflang="en-SA" href={`${url}/en${typeof window !== 'undefined' ? window.location.pathname : ''}`} />
      {alternateLinks.map((link, index) => (
        <link key={index} rel="alternate" hreflang={link.hreflang} href={link.href} />
      ))}
      
      {/* Open Graph Meta Tags */}
      <meta property="og:type" content={type} />
      <meta property="og:url" content={currentUrl} />
      <meta property="og:title" content={fullTitle} />
      <meta property="og:description" content={description} />
      <meta property="og:image" content={image} />
      <meta property="og:image:width" content="1200" />
      <meta property="og:image:height" content="630" />
      <meta property="og:locale" content="ar_SA" />
      <meta property="og:locale:alternate" content="en_SA" />
      <meta property="og:site_name" content="GamersStation" />
      
      {/* Twitter Card Meta Tags */}
      <meta name="twitter:card" content="summary_large_image" />
      <meta name="twitter:site" content="@GamersStationApp" />
      <meta name="twitter:creator" content="@GamersStationApp" />
      <meta name="twitter:url" content={currentUrl} />
      <meta name="twitter:title" content={fullTitle} />
      <meta name="twitter:description" content={description} />
      <meta name="twitter:image" content={image} />
      <meta name="twitter:image:alt" content={title || 'GamersStation'} />
      
      {/* Article Meta Tags (for blog posts or product pages) */}
      {type === 'article' && (
        <>
          <meta property="article:author" content={author} />
          <meta property="article:published_time" content={new Date().toISOString()} />
        </>
      )}
      
      {/* Product Meta Tags */}
      {type === 'product' && (
        <>
          <meta property="product:availability" content="in stock" />
          <meta property="product:condition" content="new" />
          <meta property="product:price:currency" content="SAR" />
        </>
      )}
      
      {/* Additional SEO Meta Tags */}
      <meta name="theme-color" content="#0a1628" />
      <meta name="apple-mobile-web-app-capable" content="yes" />
      <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
      <meta name="apple-mobile-web-app-title" content="GamersStation" />
      <meta name="application-name" content="GamersStation" />
      <meta name="msapplication-TileColor" content="#0a1628" />
      
      {/* Structured Data */}
      {finalStructuredData && (
        <script type="application/ld+json">
          {JSON.stringify(finalStructuredData)}
        </script>
      )}
    </Helmet>
  );
};

export default SEO;