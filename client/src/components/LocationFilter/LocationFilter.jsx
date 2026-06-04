import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { MapPin, X } from 'lucide-react';
import regionService from '../../services/regionService';
import cityService from '../../services/cityService';
import './LocationFilter.css';

const LocationFilter = ({ onFilterChange }) => {
  const { t, i18n } = useTranslation();
  const isArabic = i18n.language === 'ar';

  const [regions, setRegions] = useState([]);
  const [cities, setCities] = useState([]);
  const [selectedRegionId, setSelectedRegionId] = useState(null);
  const [selectedCityId, setSelectedCityId] = useState(null);
  const [loadingCities, setLoadingCities] = useState(false);

  // Fetch regions on mount
  useEffect(() => {
    regionService.getRegions()
      .then(data => setRegions(data))
      .catch(err => console.error('Failed to load regions:', err));
  }, []);

  // Fetch cities when region changes
  useEffect(() => {
    if (selectedRegionId) {
      setLoadingCities(true);
      cityService.getCities(selectedRegionId)
        .then(data => {
          setCities(data);
          setLoadingCities(false);
        })
        .catch(err => {
          console.error('Failed to load cities:', err);
          setLoadingCities(false);
        });
    } else {
      setCities([]);
    }
  }, [selectedRegionId]);

  const handleRegionChange = (regionId) => {
    const newRegionId = regionId === selectedRegionId ? null : regionId;
    setSelectedRegionId(newRegionId);
    setSelectedCityId(null);
    onFilterChange({ regionId: newRegionId, cityId: null });
  };

  const handleCityChange = (cityId) => {
    const newCityId = cityId === selectedCityId ? null : cityId;
    setSelectedCityId(newCityId);
    onFilterChange({ regionId: selectedRegionId, cityId: newCityId });
  };

  const handleClear = () => {
    setSelectedRegionId(null);
    setSelectedCityId(null);
    setCities([]);
    onFilterChange({ regionId: null, cityId: null });
  };

  const getRegionName = (region) => isArabic ? region.nameAr : region.nameEn;
  const getCityName = (city) => isArabic ? city.nameAr : city.nameEn;

  const hasSelection = selectedRegionId || selectedCityId;

  return (
    <div className="location-filter">
      <div className="location-filter-container">
        <div className="location-filter-header">
          <div className="location-filter-label">
            <MapPin size={16} />
            <span>{t('locationFilter.title', 'Location')}</span>
          </div>
          {hasSelection && (
            <button className="location-filter-clear" onClick={handleClear}>
              <X size={14} />
              <span>{t('common.clear', 'Clear')}</span>
            </button>
          )}
        </div>

        {/* Region Pills */}
        <div className="location-pills-row">
          {regions.map((region) => (
            <button
              key={region.id}
              className={`location-pill ${selectedRegionId === region.id ? 'active' : ''}`}
              onClick={() => handleRegionChange(region.id)}
            >
              {getRegionName(region)}
            </button>
          ))}
        </div>

        {/* City Pills */}
        {selectedRegionId && (
          <div className="location-pills-row city-pills">
            {loadingCities ? (
              <span className="location-loading">{t('common.loading', 'Loading...')}</span>
            ) : (
              cities.map((city) => (
                <button
                  key={city.id}
                  className={`location-pill city-pill ${selectedCityId === city.id ? 'active' : ''}`}
                  onClick={() => handleCityChange(city.id)}
                >
                  {getCityName(city)}
                </button>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default LocationFilter;
