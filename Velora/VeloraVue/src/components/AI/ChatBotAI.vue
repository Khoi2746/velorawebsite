<template>
    <div class="chatbot-container">
        <!-- Nút bấm mở chat -->
        <button class="chat-toggle-btn" @click="toggleChat">
            <i :class="isOpen ? 'fas fa-times' : 'fas fa-comment-dots'"></i>
        </button>

        <div class="chat-window" :class="{ 'open': isOpen }">
            <!-- Header Chat -->
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

            <!-- Khung hiển thị nội dung chat -->
            <div class="chat-body" ref="chatBody">
                <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender]">
                    <div class="msg-content" v-html="msg.text"></div>
                </div>
                <div v-if="isLoading" class="message bot">
                    <div class="msg-content typing">
                        {{ isConnecting ? 'Đang kết nối chuyên viên...' : 'Đang suy nghĩ...' }}
                    </div>
                </div>
            </div>

            <!-- Footer nhập chat -->
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
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs'
import Stomp from 'stompjs'

const isOpen = ref(false)
const currentInput = ref('')
const isLoading = ref(false)
const isConnecting = ref(false)
const chatBody = ref(null)

// 1. TẠO HOẶC LẤY MÃ PHIÊN CHAT (KHÔNG DÙNG SESSIONSTORAGE CHO LỊCH SỬ NỮA)
const localUser = JSON.parse(localStorage.getItem('user'));
let maPhienChat = '';

if (localUser && localUser.maNguoiDung) {
    maPhienChat = 'USER_' + localUser.maNguoiDung;
} else {
    // Với khách vãng lai (chưa đăng nhập), dùng localStorage để giữ ID không bị đổi khi F5
    maPhienChat = localStorage.getItem('velora_guest_roomId');
    if (!maPhienChat) {
        maPhienChat = 'GUEST_' + Date.now().toString().slice(-5);
        localStorage.setItem('velora_guest_roomId', maPhienChat);
    }
}

const messages = ref([]);
const isHumanMode = ref(sessionStorage.getItem('velora_humanMode') === 'true');
const headerTitle = ref(isHumanMode.value ? 'CVTV Khách Hàng Velora' : 'Velora AI Assistant');

let stompClient = null;

// 2. KẾT NỐI WEBSOCKET ĐỂ NGHE ADMIN REPLIES
const connectWebSocket = () => {
    if (stompClient && stompClient.connected) return;
    const socket = new SockJS('http://localhost:8080/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/chat/${maPhienChat}`, (message) => {
            const body = JSON.parse(message.body);
            // Bỏ qua tin nhắn AI qua socket vì Frontend đã push khi gọi REST API /tu-van thành công
            if (body.sender === 'ADMIN' || body.sender === 'SYSTEM') {
                messages.value.push({ sender: 'bot', text: body.content });
                scrollToBottom();
                
                if (body.sender === 'SYSTEM' && body.content.includes('kết thúc')) {
                    isHumanMode.value = false;
                    sessionStorage.removeItem('velora_humanMode');
                    headerTitle.value = 'Velora AI Assistant';
                }
            }
        });
    });
};

// 3. TẢI LỊCH SỬ TỪ DATABASE (ĐỒNG BỘ MỌI THIẾT BỊ)
const loadHistoryFromDB = async () => {
    try {
        const res = await fetch(`http://localhost:8080/api/chatbot/history/${maPhienChat}`);
        const data = await res.json();
        
        if (data && data.length > 0) {
            messages.value = data.map(m => ({
                sender: m.nguoiGui === 'USER' ? 'user' : 'bot',
                text: m.noiDungTinNhan
            }));
        } else {
            messages.value = [{ sender: 'bot', text: 'Kính chào quý khách! Tôi là trợ lý AI của Velora Clock.' }];
        }
    } catch (error) {
        console.error("Lỗi tải lịch sử chat:", error);
        messages.value = [{ sender: 'bot', text: 'Kính chào quý khách! Tôi là trợ lý AI của Velora Clock.' }];
    }
    scrollToBottom();
};

const toggleChat = async () => {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
        await scrollToBottom();
    }
}

const scrollToBottom = async () => {
    await nextTick()
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}

onMounted(() => {
    loadHistoryFromDB();
    connectWebSocket(); 
});

// ================= CHUYỂN SANG CHUYÊN VIÊN TƯ VẤN =================
const switchToHuman = async () => {
    messages.value.push({ sender: 'user', text: 'Tôi muốn liên hệ trực tiếp với nhân viên tư vấn.' })
    scrollToBottom()

    isLoading.value = true
    isConnecting.value = true

    const freshUser = JSON.parse(localStorage.getItem('user'));
    const tenKhachHang = freshUser ? (freshUser.hoTen || 'Khách') : 'Khách';

    try {
        const response = await fetch('http://localhost:8080/api/chatbot/request-human', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                maPhienChat: maPhienChat,
                tenKhach: tenKhachHang
            })
        });

        if (response.ok) {
            isHumanMode.value = true;
            sessionStorage.setItem('velora_humanMode', 'true');
            headerTitle.value = 'CVTV Khách Hàng Velora';
        }
    } catch (error) {
        messages.value.push({ sender: 'bot', text: 'Lỗi kết nối chuyên viên.' })
    } finally {
        isLoading.value = false
        isConnecting.value = false
        scrollToBottom();
    }
}

// ================= GỬI TIN NHẮN =================
const sendMessage = async () => {
    const userText = currentInput.value.trim()
    if (!userText) return
    currentInput.value = ''

    messages.value.push({ sender: 'user', text: userText })
    scrollToBottom()

    if (isHumanMode.value && stompClient?.connected) {
        stompClient.send(`/app/chat/${maPhienChat}`, {}, JSON.stringify({
            sender: 'USER', content: userText, timestamp: new Date().toLocaleTimeString()
        }));
    } else {
        isLoading.value = true
        try {
            const res = await fetch('http://localhost:8080/api/chatbot/tu-van', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: userText, maPhienChat: maPhienChat })
            })
            const data = await res.json()
            if (data.reply && data.reply !== 'SILENT') {
                messages.value.push({ sender: 'bot', text: data.reply.replace(/\n/g, '<br>') })
            }
        } catch (error) {
            messages.value.push({ sender: 'bot', text: 'Lỗi kết nối máy chủ.' })
        } finally {
            isLoading.value = false
            scrollToBottom()
        }
    }
}

onUnmounted(() => { if (stompClient) stompClient.disconnect(); })
</script>

<style scoped>
.chatbot-container {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 9999;
}

.chat-toggle-btn {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #d1aa68;
    color: #fff;
    border: none;
    cursor: pointer;
}

.chat-window {
    position: absolute;
    bottom: 80px;
    right: 0;
    width: 350px;
    height: 480px;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    opacity: 0;
    pointer-events: none;
    transition: all 0.3s;
}

.chat-window.open {
    opacity: 1;
    pointer-events: all;
}

.chat-header {
    background-color: #24201D;
    color: #d1aa68;
    padding: 12px 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.chat-body {
    flex: 1;
    padding: 15px;
    overflow-y: auto;
    background-color: #f9f9f9;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.message {
    display: flex;
    max-width: 85%;
}

.message.user {
    align-self: flex-end;
}

.message.bot {
    align-self: flex-start;
}

.msg-content {
    padding: 10px 15px;
    border-radius: 15px;
    font-size: 13px;
}

.message.user .msg-content {
    background-color: #d1aa68;
    color: #fff;
}

.message.bot .msg-content {
    background-color: #fff;
    border: 1px solid #e0e0e0;
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
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 20px;
    outline: none;
}

.chat-footer button {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #24201D;
    color: #d1aa68;
    border: none;
    cursor: pointer;
}
</style>