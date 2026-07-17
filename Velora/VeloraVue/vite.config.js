import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
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
  // Đây là đoạn fix lỗi WebSocket cho ku em
  define: {
    global: 'window', 
  },
})