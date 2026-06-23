import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174, // Giữ nguyên port hiện tại của bạn
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Trỏ tới Backend Spring Boot
        changeOrigin: true,
        secure: false,
      }
    }
  }
})