import React, { useState, useEffect, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Gamepad2,
  Monitor,
  Headphones,
  LayoutGrid,
} from 'lucide-react';
import categoryService from '../../services/categoryService';
import './CategoryFilter.css';

// Visual config per platform slug
const PLATFORM_VISUALS = {
  playstation: { icon: <Gamepad2 size={18} />, indicator: '#0050a0' },
  xbox:        { icon: <Gamepad2 size={18} />, indicator: '#52b043' },
  nintendo:    { icon: <Gamepad2 size={18} />, indicator: '#e4000f' },
  pc:          { icon: <Monitor size={18} />,  indicator: '#ff6b35' },
};

// Emoji per subcategory slug keyword
function getSubcategoryIcon(slug) {
  if (slug.includes('device')) return '🖥️';
  if (slug.includes('game'))   return '🎮';
  if (slug.includes('accessor')) return '🎧';
  return '📦';
}

const CategoryFilter = ({ onFilterChange }) => {
  const { i18n } = useTranslation();
  const { t } = useTranslation();
  const [selectedPlatform, setSelectedPlatform] = useState('all');
  const [selectedSubcategory, setSelectedSubcategory] = useState('all');
  const [categories, setCategories] = useState([]);

  // Fetch categories from API on mount
  useEffect(() => {
    categoryService.getCategories()
      .then(data => {
        console.log('Categories from API:', data);
        setCategories(data);
      })
      .catch(err => console.error('Failed to load categories:', err));
  }, []);

  const isArabic = i18n.language === 'ar';

  // Build platforms from API data (level 1 = platforms, level 2 = subcategories)
  const platforms = useMemo(() => {
    const mainCats = categories.filter(c => c.level === 1).sort((a, b) => a.sortOrder - b.sortOrder);
    const subCats = categories.filter(c => c.level === 2);

    // Build "all" platform with cross-platform subcategory groups
    const subcategoryGroups = {};
    for (const sub of subCats) {
      // Group by slug suffix (e.g. "devices", "games", "accessories")
      const suffix = sub.slug.split('-').pop();
      if (!subcategoryGroups[suffix]) subcategoryGroups[suffix] = { ids: [], name: isArabic ? sub.nameAr : sub.nameEn, slug: suffix };
      subcategoryGroups[suffix].ids.push(sub.id);
    }
    const genericSubs = Object.values(subcategoryGroups).map(g => ({
      key: g.slug,
      name: g.name,
      icon: getSubcategoryIcon(g.slug),
      categoryIds: g.ids,
    }));

    // Build each platform entry
    const platformEntries = mainCats.map(cat => {
      const visuals = PLATFORM_VISUALS[cat.slug] || { icon: <Gamepad2 size={18} />, indicator: null };
      const children = subCats.filter(s => s.parentId === cat.id).sort((a, b) => a.sortOrder - b.sortOrder);

      return {
        name: isArabic ? cat.nameAr : cat.nameEn,
        key: cat.slug,
        icon: visuals.icon,
        indicator: visuals.indicator,
        categoryIds: children.map(c => c.id),
        subcategories: children.map(c => ({
          key: c.slug,
          name: isArabic ? c.nameAr : c.nameEn,
          icon: getSubcategoryIcon(c.slug),
          categoryId: c.id,
        })),
      };
    });

    return [
      { name: t('categoryFilter.all'), key: 'all', icon: <LayoutGrid size={18} />, indicator: null, categoryId: null, subcategories: genericSubs },
      ...platformEntries,
    ];
  }, [categories, isArabic, t]);

  const currentPlatform = platforms.find(p => p.key === selectedPlatform);
  const subcategories = currentPlatform?.subcategories || [];

  const handlePlatformChange = (platform) => {
    setSelectedPlatform(platform.key);
    setSelectedSubcategory('all');
    if (onFilterChange) {
      if (platform.key === 'all') {
        onFilterChange(null);
      } else if (platform.categoryIds) {
        onFilterChange(null, platform.categoryIds);
      } else {
        onFilterChange(platform.categoryId);
      }
    }
  };

  const handleSubcategoryChange = (subcategory) => {
    setSelectedSubcategory(subcategory.key);
    if (onFilterChange) {
      if (subcategory.categoryIds) {
        onFilterChange(null, subcategory.categoryIds);
      } else {
        onFilterChange(subcategory.categoryId);
      }
    }
  };

  return (
    <div className="category-filter-modern">
      <div className="filter-container">
        {/* Platform Selection */}
        <div className="platform-section">
          <div className="platform-pills-modern">
            {platforms.map((platform) => (
              <button
                key={platform.key}
                className={`platform-pill-modern ${
                  selectedPlatform === platform.key ? 'active' : ''
                }`}
                onClick={() => handlePlatformChange(platform)}
              >
                <span className="pill-text">{platform.name}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Subcategory Filters */}
        <div className="subcategory-section">
          {subcategories.length > 0 && (
            <div className="subcategory-pills">
              <button
                className={`subcategory-pill ${
                  selectedSubcategory === 'all' ? 'active' : ''
                }`}
                onClick={() => {
                  setSelectedSubcategory('all');
                  if (onFilterChange) {
                    if (currentPlatform.categoryIds) {
                      onFilterChange(null, currentPlatform.categoryIds);
                    } else {
                      onFilterChange(currentPlatform.categoryId);
                    }
                  }
                }}
              >
                <span className="pill-emoji">🏷️</span>
                <span>{t('categoryFilter.all')}</span>
              </button>
              
              {subcategories.map((subcategory) => (
                <button
                  key={subcategory.key}
                  className={`subcategory-pill ${
                    selectedSubcategory === subcategory.key ? 'active' : ''
                  }`}
                  onClick={() => handleSubcategoryChange(subcategory)}
                >
                  <span className="pill-emoji">{subcategory.icon}</span>
                  <span>{subcategory.name}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CategoryFilter;