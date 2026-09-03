<template>
    <div class="chatbot-container">
        <button class="chat-toggle-btn" @click="toggleChat">
            <i :class="isOpen ? 'fas fa-times' : 'fas fa-comment-dots'"></i>
        </button>

        <div class="chat-window" :class="{ 'open': isOpen }">
            <div class="chat-header">
                <div class="header-title">
                    <i v-if="isConnecting" class="fas fa-spinner fa-spin"></i>
                    <span>{{ headerTitle }}</span>
                </div>
                <button v-if="!isConnecting && headerTitle === 'Velora AI Assistant'" class="switch-human-btn"
                    @click="switchToHuman" title="Kết nối nhân viên tư vấn">
                    <i class="fas fa-headset"></i>
                </button>
            </div>

            <div class="chat-body" ref="chatBody">
                <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender]">
                    <div class="msg-content">
                        <div v-html="msg.text"></div>

                        <div class="product-suggestions" v-if="msg.products && msg.products.length > 0">
                            <div v-for="p in msg.products" :key="p.id || p.maSanPham || p.slug"
                                class="suggested-product-card">
                                <img :src="anhSanPham(p)" :alt="p.ten" />

                                <div class="suggested-info">
                                    <h5 class="suggested-name">{{ p.ten }}</h5>
                                    <small v-if="p.thuongHieu" class="suggested-brand">{{ p.thuongHieu }}</small>

                                    <span class="suggested-price"
                                        :class="{ 'consult-price': soGia(p) <= 0 || soGia(p) > 100000000 }">
                                        {{ soGia(p) > 0 && soGia(p) <= 100000000 ?
                                            Number(soGia(p)).toLocaleString('vi-VN') + ' ₫' : (p.giaHienThi
                                                || 'Giá cần được tư vấn') }} </span>

                                            <router-link :to="`/san-pham/${p.id || p.maSanPham}`"
                                                class="btn-detail-link" @click="isOpen = false">
                                                Xem chi tiết <i class="fas fa-arrow-right"></i>
                                            </router-link>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="isLoading" class="message bot">
                    <div class="msg-content typing">
                        {{ isConnecting ? 'Đang kết nối chuyên viên...' : 'Velora AI đang soạn tin...' }}
                    </div>
                </div>
            </div>

            <div class="chat-footer">
                <input v-model="currentInput"
                    :placeholder="headerTitle === 'CVTV Khách Hàng Velora' ? 'Nhắn tin cho chuyên viên Velora...' : 'Hỏi AI về đồng hồ Velora...'"
                    type="text" @keyup.enter="sendMessage" />
                <button @click="sendMessage" :disabled="isLoading || !currentInput.trim()">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs'
import Stomp from 'stompjs'

const isOpen = ref(false)
const currentInput = ref('')
const isLoading = ref(false)
const isConnecting = ref(false)
const chatBody = ref(null)

const userInfo = JSON.parse(localStorage.getItem('user'))
const currentEmail = userInfo ? userInfo.email : 'GUEST'
const lastEmail = sessionStorage.getItem('velora_logged_email')

if (lastEmail !== currentEmail) {
    Object.keys(sessionStorage).forEach(key => {
        if (key.startsWith('velora_chat_') || key === 'velora_roomId' || key === 'velora_humanMode') {
            sessionStorage.removeItem(key)
        }
    })
    sessionStorage.setItem('velora_logged_email', currentEmail)
}

let maPhienChat = sessionStorage.getItem('velora_roomId')
if (!maPhienChat) {
    maPhienChat = userInfo
        ? 'USER_' + (userInfo.id || userInfo.maNguoiDung)
        : 'ROOM_' + Date.now().toString().slice(-5) + '_' + Math.floor(Math.random() * 1000)
    sessionStorage.setItem('velora_roomId', maPhienChat)
}

const savedMessages = sessionStorage.getItem('velora_chat_' + maPhienChat)
const messages = ref(savedMessages ? JSON.parse(savedMessages) : [
    {
        sender: 'bot',
        text: 'Kính chào quý khách! Tôi là trợ lý AI của Velora Clock. Quý khách đang tìm kiếm mẫu đồng hồ hoặc thương hiệu nào ạ?',
        products: []
    }
])

watch(messages, (newVal) => {
    sessionStorage.setItem('velora_chat_' + maPhienChat, JSON.stringify(newVal))
}, { deep: true })

const isHumanMode = ref(sessionStorage.getItem('velora_humanMode') === 'true')
const headerTitle = ref(isHumanMode.value ? 'CVTV Khách Hàng Velora' : 'Velora AI Assistant')

let stompClient = null

const soGia = (p) => Number(p?.gia ?? p?.giaSo ?? 0)

const anhSanPham = (p) => {
  const raw = p?.anh ? String(p.anh).trim() : ''
  if (!raw) return '/img/VeloraIcon.png'

  if (raw.startsWith('http://') || raw.startsWith('https://')) return raw

  const fileName = raw.split('/').pop()
  return '/img/' + fileName
}

const toggleChat = () => {
    isOpen.value = !isOpen.value
    if (isOpen.value) scrollToBottom()
}

const scrollToBottom = async () => {
    await nextTick()
    if (chatBody.value) {
        chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
}

const connectWebSocket = () => {
    if (stompClient && stompClient.connected) return

    const socket = new SockJS('http://localhost:8080/ws-chat')
    stompClient = Stomp.over(socket)
    stompClient.debug = null

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/chat/${maPhienChat}`, (message) => {
            const body = JSON.parse(message.body)

            if (body.sender === 'ADMIN' || body.sender === 'SYSTEM') {
                messages.value.push({
                    sender: 'bot',
                    text: body.content,
                    products: body.products || []
                })
                scrollToBottom()

                if (body.sender === 'SYSTEM' && body.content && body.content.includes('kết thúc')) {
                    isHumanMode.value = false
                    sessionStorage.removeItem('velora_humanMode')
                    headerTitle.value = 'Velora AI Assistant'
                    if (stompClient) stompClient.disconnect()
                }
            }
        })
    }, (error) => {
        console.error('Lỗi kết nối WebSocket:', error)
    })
}

onMounted(() => {
    connectWebSocket()
})

const switchToHuman = async () => {
    messages.value.push({ sender: 'user', text: 'Tôi muốn liên hệ trực tiếp với nhân viên tư vấn.', products: [] })
    scrollToBottom()

    isLoading.value = true
    isConnecting.value = true

    try {
        const response = await fetch('http://localhost:8080/api/chatbot/request-human', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ maPhienChat: maPhienChat })
        })

        if (response.ok) {
            isHumanMode.value = true
            sessionStorage.setItem('velora_humanMode', 'true')
            headerTitle.value = 'CVTV Khách Hàng Velora'
        }
    } catch (error) {
        messages.value.push({ sender: 'bot', text: 'Lỗi mạng: Không thể gọi chuyên viên lúc này.', products: [] })
    } finally {
        isConnecting.value = false
        isLoading.value = false
        scrollToBottom()
    }
}

const sendMessage = async () => {
    const userText = currentInput.value.trim()
    if (!userText) return

    currentInput.value = ''
    messages.value.push({ sender: 'user', text: userText, products: [] })
    scrollToBottom()

    if (isHumanMode.value) {
        if (stompClient && stompClient.connected) {
            const msgPayload = {
                sender: 'USER',
                content: userText,
                timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            }
            stompClient.send(`/app/chat/${maPhienChat}`, {}, JSON.stringify(msgPayload))
        } else {
            messages.value.push({ sender: 'bot', text: 'Mất kết nối với máy chủ. Đang thử lại...', products: [] })
        }
        scrollToBottom()
        return
    }

    isLoading.value = true
    try {
        const res = await fetch('http://localhost:8080/api/chatbot/tu-van', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: userText, maPhienChat: maPhienChat })
        })

        if (res.ok) {
            const data = await res.json()
            if (data.reply && data.reply !== 'SILENT') {
                const formattedReply = String(data.reply).replace(/\n/g, '<br>')
                messages.value.push({
                    sender: 'bot',
                    text: formattedReply,
                    products: data.products || []
                })
            }
        } else {
            messages.value.push({ sender: 'bot', text: 'Xin lỗi, kết nối bị gián đoạn.', products: [] })
        }
    } catch (error) {
        messages.value.push({ sender: 'bot', text: 'Không thể kết nối đến máy chủ.', products: [] })
    } finally {
        isLoading.value = false
        scrollToBottom()
    }
}

onUnmounted(() => {
    if (stompClient) stompClient.disconnect()
})
</script>

<style scoped>
.chatbot-container {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 9999;
    font-family: 'Arial', sans-serif;
}

.chat-toggle-btn {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #d1aa68;
    color: #fff;
    border: none;
    font-size: 24px;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(209, 170, 104, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.3s ease;
}

.chat-toggle-btn:hover {
    transform: scale(1.1);
}

.chat-window {
    position: absolute;
    bottom: 80px;
    right: 0;
    width: 360px;
    height: 520px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    opacity: 0;
    pointer-events: none;
    transform: translateY(20px);
    transition: all 0.3s ease;
    border: 1px solid #eee;
}

.chat-window.open {
    opacity: 1;
    pointer-events: all;
    transform: translateY(0);
}

.chat-header {
    background-color: #24201D;
    color: #d1aa68;
    padding: 12px 15px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.header-title {
    display: flex;
    align-items: center;
    gap: 10px;
}

.switch-human-btn {
    background: none;
    border: 1px solid #d1aa68;
    color: #d1aa68;
    border-radius: 50%;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
}

.switch-human-btn:hover {
    background-color: #d1aa68;
    color: #24201D;
}

.chat-body {
    flex: 1;
    padding: 15px;
    overflow-y: auto;
    background-color: #f9f9f9;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.message {
    display: flex;
    max-width: 90%;
}

.message.user {
    align-self: flex-end;
}

.message.bot {
    align-self: flex-start;
}

.msg-content {
    padding: 10px 14px;
    border-radius: 15px;
    font-size: 13px;
    line-height: 1.5;
    width: 100%;
}

.message.user .msg-content {
    background-color: #d1aa68;
    color: #fff;
    border-bottom-right-radius: 3px;
}

.message.bot .msg-content {
    background-color: #fff;
    color: #333;
    border: 1px solid #e0e0e0;
    border-bottom-left-radius: 3px;
}

.typing {
    font-style: italic;
    color: #888;
}

.product-suggestions {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
}

.suggested-product-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px;
    background: #fffcf5;
    border: 1px solid #d1aa68;
    border-radius: 8px;
    width: 100%;
    box-sizing: border-box;
}

.suggested-product-card img {
    width: 55px;
    height: 55px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #ddd;
    flex-shrink: 0;
}

.suggested-info {
    display: flex;
    flex-direction: column;
    flex: 1;
    overflow: hidden;
}

.suggested-name {
    font-size: 13px;
    font-weight: bold;
    color: #24201D;
    margin: 0 0 2px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.suggested-brand {
    font-size: 11px;
    color: #888;
    margin-bottom: 4px;
}

.suggested-price {
    font-size: 12px;
    color: #c5a880;
    font-weight: bold;
    margin-bottom: 6px;
}

.suggested-price.consult-price {
    color: #e67e22;
    font-style: italic;
}

.btn-detail-link {
    font-size: 11px;
    color: #fff;
    background: #24201D;
    padding: 4px 10px;
    border-radius: 4px;
    text-decoration: none;
    width: fit-content;
    transition: 0.2s;
    display: inline-flex;
    align-items: center;
    gap: 5px;
}

.btn-detail-link:hover {
    background: #d1aa68;
    color: #fff;
}

.chat-footer {
    padding: 12px;
    background-color: #fff;
    border-top: 1px solid #eee;
    display: flex;
    gap: 10px;
}

.chat-footer input {
    flex: 1;
    padding: 10px 15px;
    border: 1px solid #ddd;
    border-radius: 20px;
    outline: none;
    font-size: 13px;
}

.chat-footer input:focus {
    border-color: #d1aa68;
}

.chat-footer button {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #24201D;
    color: #d1aa68;
    border: none;
    cursor: pointer;
    transition: 0.2s;
}

.chat-footer button:disabled {
    background-color: #ccc;
    color: #fff;
    cursor: not-allowed;
}
</style>