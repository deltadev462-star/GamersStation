import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Bundle translations inline — eliminates the render-blocking network fetch
import ar from '../public/locales/ar/translation.json';
import en from '../public/locales/en/translation.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      ar: { translation: ar },
      en: { translation: en },
    },
    supportedLngs: ['ar', 'en'],
    fallbackLng: 'ar',
    debug: import.meta.env.DEV,

    // Detection options - check localStorage first
    detection: {
      order: ['localStorage', 'cookie', 'navigator'],
      caches: ['localStorage', 'cookie'],
      lookupLocalStorage: 'i18nextLng',
      lookupCookie: 'i18nextLng',
      checkWhitelist: true,
    },

    interpolation: {
      escapeValue: false,
    },

    defaultNS: 'translation',
    ns: ['translation'],

    // Performance optimizations
    load: 'languageOnly',
    cleanCode: true,
    nonExplicitSupportedLngs: false,

    // React optimizations
    react: {
      useSuspense: false,
      bindI18n: 'languageChanged loaded',
      bindI18nStore: 'added removed',
      transEmptyNodeValue: '',
      transSupportBasicHtmlNodes: true,
      transKeepBasicHtmlNodesFor: ['br', 'strong', 'i'],
    },

    simplifyPluralSuffix: true,
    returnNull: false,
    returnEmptyString: false,
  });

// Set initial direction
const initLang = i18n.language || i18n.resolvedLanguage || 'ar';
document.documentElement.dir = initLang === 'ar' ? 'rtl' : 'ltr';
document.documentElement.lang = initLang;

// Listen for language changes and update direction
i18n.on('languageChanged', (lng) => {
  document.documentElement.dir = lng === 'ar' ? 'rtl' : 'ltr';
  document.documentElement.lang = lng;
});

export default i18n;
