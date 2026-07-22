import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Trỏ tới Backend Spring Boot
        changeOrigin: true,
        secure: false,
      }
    }
  },
  // Fix lỗi global cho WebSocket / SockJS / Stomp client
  define: {
    global: 'window',
  },
})