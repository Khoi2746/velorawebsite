<template>
  <div class="user-chat-container">
    <div class="chat-box-card">
      <div class="user-chat-header">
        <h3>Hỗ Trợ Trực Tuyến <span class="gold">Velora Clock</span></h3>
        <span :class="['status-pill', trangThaiPhien]">
          {{ trangThaiPhien === 'AI_HANDLING' ? '🤖 Đang trò chuyện cùng Trợ lý AI' : '👨‍💼 Chuyên viên đang hỗ trợ' }}
        </span>
      </div>

      <!-- Lịch sử tin nhắn -->
      <div class="user-chat-messages" ref="messageBox">
        <div v-for="(msg, index) in lichSuChat" :key="index" :class="['msg-row', msg.nguoiGui]">
          <div class="msg-content-bubble">
            <span class="sender-name">
              {{ msg.nguoiGui === 'USER' ? 'Bạn' : msg.nguoiGui === 'AI' ? 'Trợ lý AI' : 'Chuyên viên' }}
            </span>
            <p>{{ msg.noiDungTinNhan }}</p>
            <span class="msg-time">{{ msg.thoiGianGui }}</span>
          </div>
        </div>
      </div>

      <!-- Nút yêu cầu gặp nhân viên nếu đang kẹt ở AI -->
      <div class="escalate-banner" v-if="trangThaiPhien === 'AI_HANDLING'">
        <span>Cần sự hỗ trợ trực tiếp từ nhân viên?</span>
        <button class="btn-request-human" @click="yeuCauGặpNhanVien">
          Yêu cầu gặp Chuyên Viên <i class="fa-solid fa-headset"></i>
        </button>
      </div>

      <!-- Khu vực nhập tin nhắn -->
      <div class="user-chat-input">
        <input 
          type="text" 
          v-model="noiDungMoi" 
          placeholder="Nhập nội dung cần hỗ trợ..." 
          @keyup.enter="guiTinNhan"
        />
        <button @click="guiTinNhan">
          Gửi <i class="fa-solid fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs'
import Stomp from 'stompjs'

const maPhienChat = ref('SESSION_' + Math.floor(Math.random() * 100000))
const tenKhach = ref('Khách hàng Velora')
const trangThaiPhien = ref('AI_HANDLING')
const lichSuChat = ref([
  { nguoiGui: 'AI', noiDungTinNhan: 'Xin chào quý khách! Tôi là trợ lý ảo của Velora Clock. Tôi có thể giúp gì cho bạn?', thoiGianGui: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }
])
const noiDungMoi = ref('')
const messageBox = ref(null)

let stompClient = null

const scrollToBottom = async () => {
  await nextTick()
  if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight
}

const connectSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws-chat')
  stompClient = Stomp.over(socket)
  stompClient.debug = null

  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/chat/${maPhienChat.value}`, (message) => {
      const received = JSON.parse(message.body)
      if (received.sender === 'USER') return

      lichSuChat.value.push({
        nguoiGui: received.sender,
        noiDungTinNhan: received.content,
        thoiGianGui: received.timestamp || new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      })
      scrollToBottom()
    })
  })
}

const guiTinNhan = async () => {
  if (!noiDungMoi.value.trim()) return

  const text = noiDungMoi.value
  const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  lichSuChat.value.push({ nguoiGui: 'USER', noiDungTinNhan: text, thoiGianGui: time })
  noiDungMoi.value = ''
  scrollToBottom()

  try {
    await fetch('http://localhost:8080/api/chatbot/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        maPhienChat: maPhienChat.value,
        message: text,
        sender: 'USER'
      })
    })
  } catch (e) {
    console.error("Lỗi gửi tin nhắn:", e)
  }
}

const yeuCauGặpNhanVien = async () => {
  try {
    await fetch('http://localhost:8080/api/chatbot/request-human', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        maPhienChat: maPhienChat.value,
        tenKhach: tenKhach.value
      })
    })
    trangThaiPhien.value = 'HUMAN_HANDLING'
    lichSuChat.value.push({
      nguoiGui: 'AI',
      noiDungTinNhan: 'Đã chuyển yêu cầu của bạn đến tổng đài viên. Vui lòng đợi trong giây lát!',
      thoiGianGui: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    })
    scrollToBottom()
  } catch (e) {
    console.error("Lỗi kết nối nhân viên:", e)
  }
}

onMounted(() => {
  connectSocket()
})

onUnmounted(() => {
  if (stompClient) stompClient.disconnect()
})
</script>

<style scoped>
.user-chat-container { max-width: 600px; margin: 40px auto; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.chat-box-card { background: #fff; border: 1px solid #e2dcd0; border-radius: 8px; box-shadow: 0 4px 12px rgba(54, 41, 33, 0.08); display: flex; flex-direction: column; height: 550px; }
.user-chat-header { background: #362921; color: #fff; padding: 15px 20px; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: space-between; align-items: center; }
.user-chat-header h3 { margin: 0; font-size: 16px; }
.user-chat-header .gold { color: #cca15e; }
.status-pill { font-size: 11px; background: rgba(204, 161, 94, 0.2); color: #cca15e; padding: 4px 8px; border-radius: 12px; }
.user-chat-messages { flex: 1; padding: 20px; overflow-y: auto; background: #fdfbf7; display: flex; flex-direction: column; gap: 12px; }
.msg-row { display: flex; flex-direction: column; }
.msg-row.USER { align-items: flex-end; }
.msg-row.AI, .msg-row.ADMIN { align-items: flex-start; }
.msg-content-bubble { max-width: 75%; padding: 10px 14px; border-radius: 8px; font-size: 14px; }
.USER .msg-content-bubble { background: #362921; color: #fff; border-bottom-right-radius: 2px; }
.AI .msg-content-bubble, .ADMIN .msg-content-bubble { background: #eee6d8; color: #362921; border-bottom-left-radius: 2px; }
.sender-name { font-size: 11px; font-weight: bold; display: block; margin-bottom: 2px; color: #cca15e; }
.msg-time { font-size: 10px; opacity: 0.6; display: block; margin-top: 4px; text-align: right; }
.escalate-banner { background: #fff3cd; padding: 8px 15px; display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: #856404; border-top: 1px solid #ffeeba; }
.btn-request-human { background: #362921; color: #cca15e; border: none; padding: 5px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; font-weight: 600; }
.user-chat-input { padding: 12px; background: #fff; border-top: 1px solid #e2dcd0; display: flex; gap: 10px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; }
.user-chat-input input { flex: 1; padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; outline: none; }
.user-chat-input button { background: #362921; color: #cca15e; border: none; padding: 0 15px; border-radius: 4px; font-weight: bold; cursor: pointer; }
</style>