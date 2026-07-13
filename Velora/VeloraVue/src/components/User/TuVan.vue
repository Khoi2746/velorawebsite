<template>
  <div class="tuvan-page">
    <Header />
    <main class="container">
      <div class="tuvan-grid">
        <div class="booking-section">
          <h2>ĐẶT LỊCH HẸN VVIP</h2>
          <p>Trải nghiệm dịch vụ tư vấn cá nhân hóa tại showroom.</p>
          <form @submit.prevent="bookAppointment">
            <input v-model="booking.name" placeholder="Họ và tên" required />
            <input v-model="booking.phone" placeholder="Số điện thoại" required />
            <input type="date" v-model="booking.date" required />
            <select v-model="booking.time">
              <option value="">Chọn khung giờ</option>
              <option value="09:00">09:00 - 10:30</option>
              <option value="14:00">14:00 - 15:30</option>
            </select>
            <button type="submit" class="btn-book">XÁC NHẬN HẸN</button>
          </form>
        </div>

        <div class="chat-section">
          <div class="chat-header">
            <h3>HỖ TRỢ TRỰC TUYẾN</h3>
            <span class="status-indicator">AI Đang hoạt động</span>
          </div>
          
          <div class="chat-messages" ref="chatBox">
            <div v-for="msg in messages" :class="['msg', msg.sender]">
              {{ msg.text }}
            </div>
          </div>

          <div class="chat-input">
            <input v-model="userMsg" @keyup.enter="sendMessage" placeholder="Nhập câu hỏi của bạn..." />
            <button @click="sendMessage">Gửi</button>
          </div>
          
          <button class="btn-human" @click="requestHumanSupport">
            <i class="fas fa-headset"></i> Yêu cầu tư vấn trực tiếp
          </button>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Header from '../Header.vue' // Đảm bảo đường dẫn này đúng với project của em
import Footer from '../Footer.vue'

const route = useRoute()

// 1. Dữ liệu form đặt lịch
const booking = ref({
  name: '',
  phone: '',
  date: '',
  time: ''
})

// 2. Dữ liệu chat
const messages = ref([
  { sender: 'bot', text: 'Chào ku em! Velora Clock hỗ trợ gì cho em hôm nay?' }
])
const userMsg = ref('')

// Xử lý khi khách đến từ trang sản phẩm
onMounted(() => {
    if (route.query.productName) {
        messages.value.push({ 
            sender: 'bot', 
            text: `Chào quý khách, tôi rất sẵn lòng tư vấn chi tiết về ${route.query.productName}.` 
        })
    }
})

// Hàm đặt lịch
const bookAppointment = () => {
  alert(`Đã gửi yêu cầu lịch hẹn cho ${booking.value.name}! Nhân viên sẽ liên hệ lại sớm nhất.`)
  // Ở đây sau này em gọi API lưu vào DB
}

// Hàm gửi tin nhắn
const sendMessage = () => {
  if (!userMsg.value.trim()) return
  messages.value.push({ sender: 'user', text: userMsg.value })
  userMsg.value = ''
  // Logic gọi API AI ở đây
}

// Hàm yêu cầu người thật
const requestHumanSupport = () => {
  messages.value.push({ sender: 'bot', text: 'Đang kết nối với nhân viên VVIP, vui lòng đợi trong giây lát...' })
}
</script>

<style scoped>
@import "../CSS/User/TuVan.css";
</style>