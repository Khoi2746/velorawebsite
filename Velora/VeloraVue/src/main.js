import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// 1. Tạo một cái khuôn ứng dụng và gán nó vào biến 'app'
const app = createApp(App)

// 2. Lắp Router vào ứng dụng (Phải nằm TRƯỚC khi mount)
app.use(router)

// 3. Đưa ứng dụng ra ngoài màn hình (Phải nằm CUỐI CÙNG)
app.mount('#app')