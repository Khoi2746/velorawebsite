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
/* Reset form và chatbox */
.tuvan-page { background: #fdfdfd; min-height: 100vh; }
.container { max-width: 1400px; margin: 50px auto; }

.tuvan-grid { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 60px; 
  padding: 40px; 
}

/* Style cho khung Đặt lịch & Chatbox */
.booking-section, .chat-section { 
  background: #ffffff; 
  padding: 40px; 
  border: 1px solid #e0d0b0; /* Viền màu vàng kim nhạt sang trọng */
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
}

h2, h3 { 
  font-family: 'Arial', sans-serif; 
  font-weight: 300; 
  letter-spacing: 3px; 
  color: #24201D; 
  margin-bottom: 20px;
}

/* Input và Select */
input, select {
  width: 100%;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid #eeeeee;
  background: #fcfcfc;
  font-size: 13px;
  outline: none;
  transition: border 0.3s;
}

input:focus { border-color: #d1aa68; }

.btn-book { 
  width: 100%; 
  padding: 18px; 
  background: #24201D; 
  color: #d1aa68; 
  border: none; 
  text-transform: uppercase; 
  letter-spacing: 2px; 
  cursor: pointer;
  transition: 0.3s;
}

.btn-book:hover { background: #d1aa68; color: #ffffff; }

/* Chatbox */
.chat-messages { 
  height: 350px; 
  overflow-y: auto; 
  background: #f9f9f9; 
  padding: 20px; 
  margin-bottom: 20px; 
  border: 1px solid #eee;
}

.msg { 
  padding: 12px 18px; 
  margin-bottom: 12px; 
  border-radius: 4px; 
  font-size: 13px;
  max-width: 80%;
}

.msg.user { background: #24201D; color: #fff; margin-left: auto; }
.msg.bot { background: #eee; color: #333; }

.chat-input { display: flex; gap: 10px; }
.chat-input input { margin-bottom: 0; flex: 1; }
.chat-input button { background: #d1aa68; color: #fff; border: none; padding: 0 20px; cursor: pointer; }

.btn-human { 
  width: 100%; 
  margin-top: 20px; 
  padding: 15px; 
  background: transparent; 
  border: 1px solid #d1aa68; 
  color: #d1aa68; 
  cursor: pointer;
  transition: 0.3s;
}

.btn-human:hover { background: #d1aa68; color: #fff; }

/* Responsive cho mobile */
@media (max-width: 768px) {
  .tuvan-grid { grid-template-columns: 1fr; gap: 30px; }
}
</style>