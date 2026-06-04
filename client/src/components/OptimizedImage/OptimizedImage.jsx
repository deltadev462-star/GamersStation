import React, { useState, useEffect, useRef, memo, useCallback } from 'react';
import {
  checkFormatSupport,
  generateOptimizedUrl,
  generateSizesAttribute,
  createLazyImageObserver,
  preloadImage,
  generateBlurDataURL,
  imageLoadingQueue,
  getNetworkAwareQuality
} from './imageOptimizer';
import './OptimizedImage.css';

// Cache for format support and blur placeholders
const formatSupportCache = { checked: false, support: {} };
const blurCache = new Map();

const OptimizedImage = memo(({
  src,
  alt,
  className = '',
  placeholder,
  loading = 'lazy',
  decoding = 'async',
  onLoad,
  onError,
  width,
  height,
  srcSet,
  sizes,
  priority = false,
  objectFit = 'cover',
  enableBlur = true,
  quality,
  ...rest
}) => {
  const [imageSrc, setImageSrc] = useState(placeholder || 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1 1"%3E%3Crect width="1" height="1" fill="%23f0f0f0"/%3E%3C/svg%3E');
  const [blurPlaceholder, setBlurPlaceholder] = useState(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const [error, setError] = useState(false);
  const [formatSupport, setFormatSupport] = useState(formatSupportCache.support);
  const imgRef = useRef(null);
  const observerRef = useRef(null);
  const loadTaskRef = useRef(null);

  // Check format support on mount
  useEffect(() => {
    if (!formatSupportCache.checked) {
      checkFormatSupport().then(support => {
        formatSupportCache.support = support;
        formatSupportCache.checked = true;
        setFormatSupport(support);
      });
    }
  }, []);

  // Generate blur placeholder
  useEffect(() => {
    let isActive = true;
    if (!src || !enableBlur || error) return;

    const schedule = (cb) => {
      if (typeof queueMicrotask === 'function') {
        queueMicrotask(cb);
      } else {
        Promise.resolve().then(cb);
      }
    };

    // Check cache first (async schedule to avoid sync setState in effect)
    const cached = blurCache.get(src);
    if (cached) {
      schedule(() => {
        if (isActive) setBlurPlaceholder(cached);
      });
    } else if (src.includes('unsplash.com')) {
      const blurUrl = generateOptimizedUrl(src, {
        width: 20,
        quality: 10,
        blur: true
      });
      schedule(() => {
        if (isActive) setBlurPlaceholder(blurUrl);
      });
      blurCache.set(src, blurUrl);
    } else {
      generateBlurDataURL(src)
        .then((dataUrl) => {
          if (!isActive) return;
          setBlurPlaceholder(dataUrl);
          blurCache.set(src, dataUrl);
        })
        .catch(() => {
          // Silently fail for blur generation
        });
    }

    return () => {
      isActive = false;
    };
  }, [src, enableBlur, error]);

  // Network-aware quality
  const imageQuality = quality || getNetworkAwareQuality();

  // Load image function
  const loadImage = useCallback(async () => {
    if (!src) return;

    // Cancel any pending load task
    if (loadTaskRef.current) {
      loadTaskRef.current.cancel = true;
    }

    const loadTask = { cancel: false };
    loadTaskRef.current = loadTask;

    try {
      // Use image loading queue for better performance
      await imageLoadingQueue.add(async () => {
        if (loadTask.cancel) return;

        const img = new Image();
        
        // Generate optimized URL
        const optimizedSrc = generateOptimizedUrl(src, {
          width: width || (imgRef.current?.clientWidth * (window.devicePixelRatio || 1)),
          quality: imageQuality,
          format: formatSupport.avif ? 'avif' : formatSupport.webp ? 'webp' : 'jpeg'
        });

        // Handle srcSet if provided or generate responsive sources
        if (srcSet) {
          img.srcset = srcSet;
        } else if (src.includes('unsplash.com')) {
          // Generate responsive srcSet for Unsplash images
          const srcSetArray = [
            generateOptimizedUrl(src, { width: 400, quality: imageQuality }) + ' 400w',
            generateOptimizedUrl(src, { width: 800, quality: imageQuality }) + ' 800w',
            generateOptimizedUrl(src, { width: 1200, quality: imageQuality }) + ' 1200w',
            generateOptimizedUrl(src, { width: 1600, quality: imageQuality }) + ' 1600w'
          ];
          img.srcset = srcSetArray.join(', ');
        }
        
        // Handle sizes if provided
        if (sizes) {
          img.sizes = sizes;
        } else {
          img.sizes = generateSizesAttribute();
        }

        return new Promise((resolve, reject) => {
          img.onload = () => {
            if (!loadTask.cancel) {
              setImageSrc(optimizedSrc);
              setImageLoaded(true);
              setError(false);
              if (onLoad) onLoad();
              resolve();
            }
          };

          img.onerror = (err) => {
            if (!loadTask.cancel) {
              // Silently handle CORS and network errors
              console.debug('Image load failed (CORS or network):', src);
              setError(true);
              setImageLoaded(false);
              if (onError) onError(err);
              reject();
            }
          };

          img.src = optimizedSrc;
        });
      });
    } catch (err) {
      if (!loadTask.cancel) {
        setError(true);
        setImageLoaded(false);
        if (onError) onError(err);
      }
    }
  }, [src, width, srcSet, sizes, formatSupport, imageQuality, onLoad, onError]);

  useEffect(() => {
    if (!src) {
      // No source provided; keep placeholder without marking error to avoid sync state updates
      return;
    }

    // Preload priority images
    if (priority) {
      preloadImage(src, {
        type: formatSupport.avif ? 'image/avif' : formatSupport.webp ? 'image/webp' : 'image/jpeg'
      });
    }

    // If priority image or eager loading, load immediately
    if (priority || loading === 'eager') {
      // Schedule async to avoid sync setState in effect
      Promise.resolve().then(() => loadImage());
      return;
    }

    // Setup Intersection Observer for lazy loading
    if ('IntersectionObserver' in window && imgRef.current) {
      observerRef.current = createLazyImageObserver({
        rootMargin: priority ? '200px' : '50px 0px',
        threshold: 0.01
      });

      // Create a wrapper element for observation
      const observeTarget = imgRef.current;
      observeTarget.dataset.src = src;
      
      const handleIntersection = () => {
        loadImage();
        if (observerRef.current) {
          observerRef.current.unobserve(observeTarget);
        }
      };

      // Custom intersection handling
      const customObserver = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              handleIntersection();
              customObserver.disconnect();
            }
          });
        },
        {
          rootMargin: priority ? '200px' : '50px 0px',
          threshold: 0.01
        }
      );

      customObserver.observe(observeTarget);

      return () => {
        customObserver.disconnect();
      };
    } else {
      // Fallback for browsers without IntersectionObserver
      Promise.resolve().then(() => loadImage());
    }

    return () => {
      if (observerRef.current && imgRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [src, priority, loading, loadImage, formatSupport]);

  // Handle native image error (fallback)
  const handleError = (err) => {
    if (!error) {
      // Silently handle CORS and network errors
      console.debug('Image error caught:', err);
      setImageSrc(placeholder || '/placeholder-game.jpg');
      setError(true);
      setImageLoaded(false);
      if (onError) onError(err);
    }
  };

  // Handle native image load
  const handleLoad = () => {
    setImageLoaded(true);
    if (onLoad) onLoad();
  };

  // Generate responsive srcSet if not provided
  const generateSrcSet = () => {
    if (srcSet) return srcSet;
    if (!src || error) return undefined;
    
    // For Unsplash images, we already set srcset during load
    if (imageSrc !== src && imageSrc.includes('?')) {
      return undefined; // Let the browser use the srcset we already set
    }
    
    return undefined;
  };

  return (
    <div 
      className={`optimized-image-container ${className}`}
      style={{
        width: width || '100%',
        height: height || 'auto',
        position: 'relative',
        overflow: 'hidden',
        backgroundColor: error ? '#f0f0f0' : 'transparent'
      }}
    >
      {/* Blur placeholder while loading */}
      {!imageLoaded && !error && enableBlur && blurPlaceholder && (
        <div 
          className="image-placeholder blur-placeholder"
          style={{
            backgroundImage: `url(${blurPlaceholder})`,
            filter: 'blur(20px)',
            transform: 'scale(1.2)',
            opacity: imageLoaded ? 0 : 1
          }}
        />
      )}
      
      {/* Loading skeleton */}
      {!imageLoaded && !error && !blurPlaceholder && (
        <div className="image-skeleton">
          <div className="skeleton-shimmer"></div>
        </div>
      )}

      {/* Picture element for modern format support */}
      {formatSupport.avif || formatSupport.webp ? (
        <picture>
          {/* AVIF source */}
          {formatSupport.avif && src && !error && (
            <source
              type="image/avif"
              srcSet={generateOptimizedUrl(src, { format: 'avif', quality: imageQuality })}
              sizes={sizes || generateSizesAttribute()}
            />
          )}
          
          {/* WebP source */}
          {formatSupport.webp && src && !error && (
            <source
              type="image/webp"
              srcSet={generateOptimizedUrl(src, { format: 'webp', quality: imageQuality })}
              sizes={sizes || generateSizesAttribute()}
            />
          )}
          
          {/* Fallback image */}
          <img
            ref={imgRef}
            src={imageSrc}
            alt={alt}
            className={`optimized-image ${imageLoaded ? 'loaded' : ''} ${error ? 'error' : ''}`}
            loading={priority ? 'eager' : loading}
            decoding={decoding}
            fetchpriority={priority ? 'high' : 'auto'}
            onLoad={handleLoad}
            onError={handleError}
            width={width}
            height={height}
            srcSet={generateSrcSet()}
            sizes={sizes || generateSizesAttribute()}
            style={{
              objectFit,
              opacity: imageLoaded ? 1 : 0
            }}
            {...rest}
          />
        </picture>
      ) : (
        // Regular img for browsers without modern format support
        <img
          ref={imgRef}
          src={imageSrc}
          alt={alt}
          className={`optimized-image ${imageLoaded ? 'loaded' : ''} ${error ? 'error' : ''}`}
          loading={priority ? 'eager' : loading}
          decoding={decoding}
          fetchpriority={priority ? 'high' : 'auto'}
          onLoad={handleLoad}
          onError={handleError}
          width={width}
          height={height}
          srcSet={generateSrcSet()}
          sizes={sizes || generateSizesAttribute()}
          style={{
            objectFit,
            opacity: imageLoaded ? 1 : 0
          }}
          {...rest}
        />
      )}

      {/* Error state */}
      {error && (
        <div className="image-error">
          <span className="error-icon">🖼️</span>
          <span className="error-text">Image unavailable</span>
        </div>
      )}
    </div>
  );
});

OptimizedImage.displayName = 'OptimizedImage';

export default OptimizedImage;