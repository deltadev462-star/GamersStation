// Advanced Image Optimization Utilities
export const IMAGE_FORMATS = {
  AVIF: 'avif',
  WEBP: 'webp',
  JPEG: 'jpeg',
  PNG: 'png'
};

export const IMAGE_SIZES = {
  thumbnail: { width: 150, height: 150 },
  small: { width: 320, height: 240 },
  medium: { width: 640, height: 480 },
  large: { width: 1024, height: 768 },
  xlarge: { width: 1920, height: 1080 }
};

// Check browser support for modern formats
export const checkFormatSupport = () => {
  const canvas = document.createElement('canvas');
  canvas.width = 1;
  canvas.height = 1;
  
  const supports = {
    webp: canvas.toDataURL('image/webp').indexOf('image/webp') === 5,
    avif: false // Will be checked via feature detection
  };

  // Check AVIF support via Image API
  return new Promise((resolve) => {
    const avifTest = new Image();
    avifTest.onload = avifTest.onerror = () => {
      supports.avif = avifTest.width === 2 && avifTest.height === 2;
      resolve(supports);
    };
    avifTest.src = 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAABcAAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAEAAAABAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQAMAAAAABNjb2xybmNseAACAAIABoAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAAB9tZGF0EgAKCBgABogQEDQgMgkQAAAAB8dSLfI=';
  });
};

// Generate optimized image URL with modern format support
export const generateOptimizedUrl = (originalUrl, options = {}) => {
  if (!originalUrl) return null;

  const {
    width,
    height,
    format,
    quality = 85,
    blur = false
  } = options;

  // If it's already an optimized URL or a placeholder, return as-is
  if (originalUrl.includes('placeholder') || originalUrl.includes('data:')) {
    return originalUrl;
  }

  // Handle external image services (Unsplash, Cloudinary, etc.)
  if (originalUrl.includes('unsplash.com')) {
    const params = new URLSearchParams({
      ...(width && { w: width }),
      ...(height && { h: height }),
      ...(format && { fm: format }),
      ...(quality && { q: quality }),
      ...(blur && { blur: 20 }),
      fit: 'crop',
      auto: 'format'
    });
    
    const baseUrl = originalUrl.split('?')[0];
    return `${baseUrl}?${params.toString()}`;
  }

  // CloudFront URLs — already optimized via server-side thumbnail generation.
  // Return as-is since thumbnails are separate S3 objects (uuid-thumb.jpg).
  if (originalUrl.includes('cloudfront.net') || originalUrl.includes('amazonaws.com')) {
    return originalUrl;
  }

  // For local images, return as-is
  return originalUrl;
};

// Generate responsive image sources
export const generateImageSources = (baseUrl, alt, formatSupport = {}) => {
  const sources = [];
  const formats = [];

  // Prioritize modern formats
  if (formatSupport.avif) formats.push(IMAGE_FORMATS.AVIF);
  if (formatSupport.webp) formats.push(IMAGE_FORMATS.WEBP);
  formats.push(IMAGE_FORMATS.JPEG); // Fallback

  // Generate sources for each format and size
  formats.forEach(format => {
    const srcSet = Object.entries(IMAGE_SIZES)
      .map(([size, dimensions]) => {
        const url = generateOptimizedUrl(baseUrl, {
          width: dimensions.width,
          format: format,
          quality: size === 'thumbnail' ? 70 : 85
        });
        return `${url} ${dimensions.width}w`;
      })
      .join(', ');

    sources.push({
      type: `image/${format}`,
      srcSet,
      sizes: generateSizesAttribute()
    });
  });

  return sources;
};

// Generate sizes attribute based on viewport
export const generateSizesAttribute = () => {
  return `
    (max-width: 320px) 280px,
    (max-width: 640px) 100vw,
    (max-width: 1024px) 50vw,
    (max-width: 1440px) 33vw,
    400px
  `.trim();
};

// Preload critical images
export const preloadImage = (url, options = {}) => {
  if (!url || typeof window === 'undefined') return;

  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'image';
  link.href = generateOptimizedUrl(url, options);
  
  if (options.type) {
    link.type = options.type;
  }

  if (options.media) {
    link.media = options.media;
  }

  document.head.appendChild(link);
};

// Lazy loading with Intersection Observer
export const createLazyImageObserver = (options = {}) => {
  const defaultOptions = {
    rootMargin: '50px 0px',
    threshold: 0.01,
    ...options
  };

  return new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        const src = img.dataset.src;
        const srcset = img.dataset.srcset;
        const sizes = img.dataset.sizes;

        if (src) {
          // Create a new image to preload
          const tempImg = new Image();
          
          tempImg.onload = () => {
            img.src = src;
            if (srcset) img.srcset = srcset;
            if (sizes) img.sizes = sizes;
            img.classList.add('loaded');
            img.removeAttribute('data-src');
            img.removeAttribute('data-srcset');
            img.removeAttribute('data-sizes');
          };

          tempImg.onerror = () => {
            img.classList.add('error');
          };

          // Start loading
          tempImg.src = src;
          if (srcset) tempImg.srcset = srcset;
          if (sizes) tempImg.sizes = sizes;
        }

        observer.unobserve(img);
      }
    });
  }, defaultOptions);
};

// Calculate optimal image dimensions based on container
export const calculateOptimalDimensions = (container) => {
  if (!container) return { width: 640, height: 480 };

  const rect = container.getBoundingClientRect();
  const pixelRatio = window.devicePixelRatio || 1;
  
  // Account for high DPI displays
  const width = Math.round(rect.width * pixelRatio);
  

  // Find the closest preset size
  const presetSizes = Object.values(IMAGE_SIZES);
  const closest = presetSizes.reduce((prev, curr) => {
    const prevDiff = Math.abs(prev.width - width);
    const currDiff = Math.abs(curr.width - width);
    return currDiff < prevDiff ? curr : prev;
  });

  return closest;
};

// Generate blur hash placeholder
export const generateBlurDataURL = async (imageUrl) => {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    
    img.onload = () => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      
      // Create a small canvas for the blur
      canvas.width = 20;
      canvas.height = Math.round(20 / (img.width / img.height));
      
      // Draw and blur the image
      ctx.filter = 'blur(5px)';
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      
      // Convert to data URL
      resolve(canvas.toDataURL('image/webp', 0.1));
    };
    
    img.onerror = () => {
      // Return a default blur placeholder on error
      resolve('data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1 1"%3E%3Crect width="1" height="1" fill="%23f0f0f0"/%3E%3C/svg%3E');
    };
    
    img.src = imageUrl;
  });
};

// Image loading priority queue
class ImageLoadingQueue {
  constructor(concurrency = 6) {
    this.concurrency = concurrency;
    this.running = 0;
    this.queue = [];
  }

  add(task) {
    return new Promise((resolve, reject) => {
      this.queue.push({ task, resolve, reject });
      this.process();
    });
  }

  async process() {
    if (this.running >= this.concurrency || this.queue.length === 0) {
      return;
    }

    this.running++;
    const { task, resolve, reject } = this.queue.shift();

    try {
      const result = await task();
      resolve(result);
    } catch (error) {
      reject(error);
    } finally {
      this.running--;
      this.process();
    }
  }
}

export const imageLoadingQueue = new ImageLoadingQueue();

// Network-aware image loading
export const getNetworkAwareQuality = () => {
  if (!('connection' in navigator)) return 85;

  const connection = navigator.connection;
  const effectiveType = connection.effectiveType;

  switch (effectiveType) {
    case '4g':
      return 85;
    case '3g':
      return 70;
    case '2g':
      return 50;
    case 'slow-2g':
      return 30;
    default:
      return 85;
  }
};

// Export utilities
export default {
  checkFormatSupport,
  generateOptimizedUrl,
  generateImageSources,
  generateSizesAttribute,
  preloadImage,
  createLazyImageObserver,
  calculateOptimalDimensions,
  generateBlurDataURL,
  imageLoadingQueue,
  getNetworkAwareQuality,
  IMAGE_FORMATS,
  IMAGE_SIZES
};