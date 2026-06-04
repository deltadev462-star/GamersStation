import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Custom plugin to remove unused CSS
const removeUnusedCSS = () => {
  return {
    name: 'remove-unused-css',
    apply: 'build',
    generateBundle(options, bundle) {
      const usedClasses = new Set()
      const classRegex = /className\s*=\s*["'`]([^"'`]+)["'`]/g
      const dynamicClassRegex = /className\s*=\s*\{[^}]*["'`]([^"'`]+)["'`]/g

      Object.keys(bundle).forEach(fileName => {
        if (fileName.endsWith('.js')) {
          const chunk = bundle[fileName]
          if (chunk.type === 'chunk') {
            let match
            while ((match = classRegex.exec(chunk.code)) !== null) {
              match[1].split(/\s+/).forEach(cls => usedClasses.add(cls))
            }
            while ((match = dynamicClassRegex.exec(chunk.code)) !== null) {
              match[1].split(/\s+/).forEach(cls => usedClasses.add(cls))
            }
          }
        }
      })

      const essentialClasses = [
        'active', 'disabled', 'error', 'loading', 'success', 'show', 'hide',
        'open', 'closed', 'expanded', 'collapsed', 'animate-in', 'fade-in',
        'slide-in', 'skeleton', 'shimmer', 'mobile-only', 'desktop-only',
        'rtl', 'dark'
      ]
      essentialClasses.forEach(cls => usedClasses.add(cls))
    }
  }
}

export default defineConfig({
  plugins: [
    react({
      fastRefresh: true,
      jsxRuntime: 'automatic'
    }),
    removeUnusedCSS()
  ],

  publicDir: 'public',

  server: {
    host: true,
    port: 5173,
    strictPort: false,
    // FIX: Allow the Nginx upstream host name
    allowedHosts: [
      'client-frontend',
      'gamers-station.com',
      'www.gamers-station.com',
      'thegamersstation.com',
      'www.thegamersstation.com'
    ],
    hmr: {
      overlay: true,
      protocol: 'ws',
      host: 'localhost',
      port: 5174,
      clientPort: 5174
    },
    watch: {
      usePolling: true,
      interval: 100
    },
    middlewareMode: false,
    proxy: {
      '/api': {
        target: 'http://app:8080', // Use Docker service name 'app'
        changeOrigin: true,
        rewrite: (path) => path,
      },
      '/api/v1/ws': {
        target: 'ws://app:8080', // Use Docker service name 'app'
        ws: true,
        changeOrigin: true,
      }
    }
  },

  optimizeDeps: {
    include: [
      'react', 'react-dom', 'react-router-dom', 'react-helmet-async',
      'react-i18next', 'i18next', 'i18next-browser-languagedetector',
      'lucide-react', 'react/jsx-runtime', 'react/jsx-dev-runtime'
    ],
    esbuildOptions: {
      define: {
        global: 'globalThis'
      },
      target: 'es2020'
    }
  },

  build: {
    target: 'es2018',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.trace'],
        passes: 2
      },
      mangle: { safari10: true },
      format: { comments: false }
    },
    chunkSizeWarningLimit: 500,
    sourcemap: false,
    cssCodeSplit: true,
    assetsInlineLimit: 4096,
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'i18n-vendor': ['i18next', 'react-i18next', 'i18next-browser-languagedetector'],
          'ui-vendor': ['lucide-react'],
        },
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.');
          const extType = info[info.length - 1];
          if (/\.(png|jpe?g|svg|gif|tiff|bmp|ico|webp|avif)$/i.test(assetInfo.name)) {
            return `assets/images/[name]-[hash][extname]`;
          }
          if (extType === 'css') {
            return `assets/css/[name]-[hash][extname]`;
          }
          return `assets/[name]-[hash][extname]`;
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js'
      },
    },
    reportCompressedSize: true,
    modulePreload: { polyfill: true },
    cssMinify: true,
    cssTarget: 'chrome80'
  },

  css: {
    devSourcemap: true,
    modules: {
      localsConvention: 'camelCaseOnly',
      generateScopedName: '[name]__[local]___[hash:base64:5]'
    },
    preprocessorOptions: {
      css: { charset: false }
    }
  },

  resolve: {
    alias: {
      '@': '/src',
      '@components': '/src/components',
      '@pages': '/src/pages',
      '@services': '/src/services',
      '@utils': '/src/utils',
      '@assets': '/src/assets'
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json'],
    dedupe: ['react', 'react-dom', 'react-router-dom']
  },

  preview: {
    port: 4173,
    strictPort: false,
    host: true
  }
})