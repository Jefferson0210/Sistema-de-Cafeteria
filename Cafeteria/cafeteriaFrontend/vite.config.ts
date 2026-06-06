import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg'],
      manifest: {
        name: 'Cafetería UIDE',
        short_name: 'CaféUIDE',
        description: 'Sistema de gestión de cafetería UIDE',
        theme_color: '#910048',
        background_color: '#0f172a',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png', purpose: 'any maskable' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        runtimeCaching: [
          { urlPattern: /\/api\//i, handler: 'NetworkFirst', options: { cacheName: 'api', expiration: { maxEntries: 100, maxAgeSeconds: 300 } } },
          { urlPattern: /\/uploads\//i, handler: 'CacheFirst', options: { cacheName: 'imgs', expiration: { maxEntries: 50, maxAgeSeconds: 86400 } } }
        ]
      }
    })
  ],
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
});
