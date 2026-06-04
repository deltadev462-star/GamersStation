// Dynamic Sitemap Generator Utility
// This utility generates sitemaps dynamically based on actual data from the API

const API_BASE = import.meta.env.VITE_API_URL || 'https://gamers-station.com/api/v1';
const SITE_URL = 'https://gamers-station.com';

// Priority mapping for different page types
const PRIORITY_MAP = {
  home: 1.0,
  category: 0.9,
  product: 0.8,
  merchant: 0.7,
  static: 0.6
};

// Change frequency mapping
const CHANGEFREQ_MAP = {
  home: 'daily',
  category: 'weekly',
  product: 'weekly',
  merchant: 'weekly',
  static: 'monthly'
};

/**
 * Generates XML sitemap entry
 */
function generateUrlEntry(loc, lastmod, changefreq, priority) {
  return `
    <url>
        <loc>${loc}</loc>
        <lastmod>${lastmod}</lastmod>
        <changefreq>${changefreq}</changefreq>
        <priority>${priority}</priority>
    </url>`;
}

/**
 * Generates sitemap XML wrapper
 */
function generateSitemapXml(urls) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9
        http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
    ${urls.join('')}
</urlset>`;
}

/**
 * Fetches products from API and generates product URLs
 */
async function fetchProductUrls(limit = 1000) {
  try {
    const response = await fetch(`${API_BASE}/api/v1/posts?page=0&size=${limit}&sortBy=createdAt&direction=DESC`);
    const data = await response.json();
    
    if (!data || !data.content) return [];
    
    return data.content.map(product => ({
      loc: `${SITE_URL}/product/${product.id}`,
      lastmod: new Date(product.updatedAt || product.createdAt).toISOString().split('T')[0],
      changefreq: CHANGEFREQ_MAP.product,
      priority: PRIORITY_MAP.product
    }));
  } catch (error) {
    console.error('Error fetching products:', error);
    return [];
  }
}

/**
 * Fetches categories from API and generates category URLs
 */
async function fetchCategoryUrls() {
  try {
    const response = await fetch(`${API_BASE}/api/v1/categories/tree`);
    const categories = await response.json();
    
    if (!Array.isArray(categories)) return [];
    
    const urls = [];
    const processCategory = (category, path = '') => {
      const categoryPath = path ? `${path}/${category.slug || category.id}` : (category.slug || category.id);
      urls.push({
        loc: `${SITE_URL}/category/${categoryPath}`,
        lastmod: new Date().toISOString().split('T')[0],
        changefreq: CHANGEFREQ_MAP.category,
        priority: PRIORITY_MAP.category
      });
      
      if (category.children && category.children.length > 0) {
        category.children.forEach(child => processCategory(child, categoryPath));
      }
    };
    
    categories.forEach(cat => processCategory(cat));
    return urls;
  } catch (error) {
    console.error('Error fetching categories:', error);
    return [];
  }
}

/**
 * Generates static page URLs
 */
function getStaticUrls() {
  const pages = [
    { path: '/', priority: PRIORITY_MAP.home, changefreq: CHANGEFREQ_MAP.home },
    { path: '/products', priority: 0.9, changefreq: 'daily' },
    { path: '/merchants', priority: PRIORITY_MAP.merchant, changefreq: CHANGEFREQ_MAP.merchant },
    { path: '/faq', priority: PRIORITY_MAP.static, changefreq: CHANGEFREQ_MAP.static },
    { path: '/contact', priority: PRIORITY_MAP.static, changefreq: CHANGEFREQ_MAP.static },
    { path: '/login', priority: 0.5, changefreq: 'yearly' },
    { path: '/register', priority: 0.5, changefreq: 'yearly' }
  ];
  
  const lastmod = new Date().toISOString().split('T')[0];
  const urls = [];
  
  // Generate URLs for both Arabic and English versions
  pages.forEach(page => {
    // Arabic version (default)
    urls.push({
      loc: `${SITE_URL}${page.path}`,
      lastmod,
      changefreq: page.changefreq,
      priority: page.priority
    });
    
    // English version
    urls.push({
      loc: `${SITE_URL}/en${page.path}`,
      lastmod,
      changefreq: page.changefreq,
      priority: page.priority * 0.9 // Slightly lower priority for alternate language
    });
  });
  
  return urls;
}

/**
 * Main function to generate complete sitemap
 */
export async function generateCompleteSitemap() {
  try {
    // Fetch all URLs in parallel
    const [staticUrls, categoryUrls, productUrls] = await Promise.all([
      Promise.resolve(getStaticUrls()),
      fetchCategoryUrls(),
      fetchProductUrls()
    ]);
    
    // Combine all URLs
    const allUrls = [...staticUrls, ...categoryUrls, ...productUrls];
    
    // Generate XML entries
    const xmlEntries = allUrls.map(url => 
      generateUrlEntry(url.loc, url.lastmod, url.changefreq, url.priority)
    );
    
    // Generate complete sitemap
    return generateSitemapXml(xmlEntries);
  } catch (error) {
    console.error('Error generating sitemap:', error);
    // Return basic sitemap on error
    return generateSitemapXml(
      getStaticUrls().map(url => 
        generateUrlEntry(url.loc, url.lastmod, url.changefreq, url.priority)
      )
    );
  }
}

/**
 * Generates sitemap index for multiple sitemaps
 */
export function generateSitemapIndex(sitemaps) {
  const lastmod = new Date().toISOString().split('T')[0];
  const sitemapEntries = sitemaps.map(sitemap => `
    <sitemap>
        <loc>${SITE_URL}/${sitemap}</loc>
        <lastmod>${lastmod}</lastmod>
    </sitemap>`).join('');
    
  return `<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    ${sitemapEntries}
</sitemapindex>`;
}

/**
 * Saves sitemap to file (for build process or download)
 */
export async function saveSitemapToFile(content, filename = 'sitemap.xml') {
  // Browser environment only - download file
  if (typeof window !== 'undefined') {
    const blob = new Blob([content], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  } else {
    // For Node.js environments, this should be handled in a separate build script
    console.warn('saveSitemapToFile: File system operations should be handled in build scripts');
  }
}

// Export for use in build scripts
export default {
  generateCompleteSitemap,
  generateSitemapIndex,
  saveSitemapToFile
};