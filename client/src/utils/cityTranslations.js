// Utility function to get translated city name
export const getTranslatedCityName = (cityName, t) => {
  if (!cityName) return t('cities.Riyadh') || 'Riyadh';
  
  // Try to translate the city name, fall back to original if translation not found
  const translatedCity = t(`cities.${cityName}`, { defaultValue: cityName });
  return translatedCity;
};


