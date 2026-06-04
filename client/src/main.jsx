import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './rtl.css' // RTL/LTR foundation - MUST be imported first
import './index.css'
import './i18n' // Initialize i18n
import App from './App.jsx'
import * as serviceWorkerRegistration from './utils/serviceWorkerRegistration'
import { reportWebVitals } from './utils/resourceHints'
import performanceMonitor from './utils/performanceMonitor'
 

createRoot(document.getElementById('root')).render(
  <App />
)

// Register service worker for offline functionality and performance
serviceWorkerRegistration.register({
  onSuccess: () => {
    // Service Worker registration successful
  },
  onUpdate: () => {
    // New content is available; please refresh
  }
});

// Setup network status monitoring
serviceWorkerRegistration.setupNetworkStatusMonitoring();

// Preload critical resources after initial load
serviceWorkerRegistration.preloadCriticalResources();

// Monitor service worker performance
if (import.meta.env.DEV) {
  serviceWorkerRegistration.monitorServiceWorkerPerformance();
}

// Report Web Vitals
reportWebVitals((metric) => {
  try { performanceMonitor.logMetric(metric.name, metric.value); } catch (e) { void e; }
  if (import.meta.env.DEV) {
    console.log('Web Vital:', metric.name, metric.value);
  }
  // Send to analytics in production
  if (import.meta.env.PROD && window.gtag) {
    window.gtag('event', metric.name, {
      value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
      metric_id: metric.id,
      metric_value: metric.value,
      metric_delta: metric.delta,
    });
  }
});
