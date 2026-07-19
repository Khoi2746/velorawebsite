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
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs'
import Stomp from 'stompjs'

const isOpen = ref(false)
const currentInput = ref('')
const isLoading = ref(false)
const isConnecting = ref(false)
const chatBody = ref(null)

// 1. TỰ ĐỘNG KHÔI PHỤC HOẶC TẠO MÃ PHIÊN CHAT MỚI (Lưu vào SessionStorage để F5 không mất)
const userInfo = JSON.parse(localStorage.getItem('user'));
let maPhienChat = sessionStorage.getItem('velora_roomId');
if (!maPhienChat) {
    maPhienChat = userInfo ? 'USER_' + userInfo.id : 'ROOM_' + Date.now().toString().slice(-5) + '_' + Math.floor(Math.random() * 1000);
    sessionStorage.setItem('velora_roomId', maPhienChat);
}

// 2. KHÔI PHỤC LỊCH SỬ CHAT KHI F5
const savedMessages = sessionStorage.getItem('velora_chat_' + maPhienChat);
const messages = ref(savedMessages ? JSON.parse(savedMessages) : [
    { sender: 'bot', text: 'Kính chào quý khách! Tôi là trợ lý AI của Velora Clock. Quý khách đang tìm kiếm mẫu đồng hồ nào ạ?' }
]);

// Theo dõi mọi sự thay đổi của tin nhắn -> Tự động lưu đè vào trình duyệt
watch(messages, (newVal) => {
    sessionStorage.setItem('velora_chat_' + maPhienChat, JSON.stringify(newVal));
}, { deep: true });

// 3. KHÔI PHỤC TRẠNG THÁI NGƯỜI THẬT (Nếu trước lúc F5 đang chat với chuyên viên)
const isHumanMode = ref(sessionStorage.getItem('velora_humanMode') === 'true');
const headerTitle = ref(isHumanMode.value ? 'CVTV Khách Hàng Velora' : 'Velora AI Assistant');

let stompClient = null;

const toggleChat = () => {
    isOpen.value = !isOpen.value
    if (isOpen.value) scrollToBottom();
}

const scrollToBottom = async () => {
    await nextTick()
    if (chatBody.value) {
        chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
}

// --- KẾT NỐI WEBSOCKET ---
const connectWebSocket = () => {
    if (stompClient && stompClient.connected) return;

    const socket = new SockJS('http://localhost:8080/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, () => {
        console.log("WebSocket connected!");
        
        stompClient.subscribe(`/topic/chat/${maPhienChat}`, (message) => {
            const body = JSON.parse(message.body);
            
            if (body.sender === 'ADMIN' || body.sender === 'SYSTEM') {
                messages.value.push({ sender: 'bot', text: body.content });
                scrollToBottom();

                // Lắng nghe lệnh "Đóng" từ Admin để trả lại phòng cho con AI
                if (body.sender === 'SYSTEM' && body.content.includes('kết thúc')) {
                    isHumanMode.value = false;
                    sessionStorage.removeItem('velora_humanMode');
                    headerTitle.value = 'Velora AI Assistant';
                    if (stompClient) stompClient.disconnect();
                }
            }
        });
    }, (error) => {
        console.error("Lỗi kết nối WebSocket:", error);
    });
};

// Vừa vào web, nếu đang ở chế độ nhân viên thì tự cắm lại WebSocket
onMounted(() => {
    if (isHumanMode.value) {
        connectWebSocket();
    }
});

// ================= CHUYỂN SANG CHUYÊN VIÊN TƯ VẤN =================
const switchToHuman = async () => {
    messages.value.push({ sender: 'user', text: 'Tôi muốn liên hệ trực tiếp với nhân viên tư vấn.' })
    scrollToBottom()

    isLoading.value = true
    isConnecting.value = true

    const callRequestApi = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/chatbot/request-human', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ maPhienChat: maPhienChat })
            });
            
            if (response.ok) {
                // Bật công tắc và lưu vào bộ nhớ trình duyệt
                isHumanMode.value = true;
                sessionStorage.setItem('velora_humanMode', 'true');
                headerTitle.value = 'CVTV Khách Hàng Velora';
            }
        } catch (error) {
            messages.value.push({ sender: 'bot', text: 'Lỗi mạng: Không thể gọi chuyên viên lúc này.' })
        } finally {
            isConnecting.value = false
            isLoading.value = false
            scrollToBottom()
        }
    }

    if (!stompClient || !stompClient.connected) {
        const socket = new SockJS('http://localhost:8080/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; 

        stompClient.connect({}, () => {
            stompClient.subscribe(`/topic/chat/${maPhienChat}`, (message) => {
                const body = JSON.parse(message.body);
                if (body.sender === 'ADMIN' || body.sender === 'SYSTEM') {
                    messages.value.push({ sender: 'bot', text: body.content });
                    scrollToBottom();

                    if (body.sender === 'SYSTEM' && body.content.includes('kết thúc')) {
                        isHumanMode.value = false;
                        sessionStorage.removeItem('velora_humanMode');
                        headerTitle.value = 'Velora AI Assistant';
                        if (stompClient) stompClient.disconnect();
                    }
                }
            });
            callRequestApi();
        }, (error) => {
            callRequestApi();
        });
    } else {
        callRequestApi();
    }
}

// ================= GỬI TIN NHẮN =================
const sendMessage = async () => {
    const userText = currentInput.value.trim()
    if (!userText) return

    currentInput.value = ''
    scrollToBottom()

    // TRƯỜNG HỢP 1: Đang chat với Chuyên viên
    if (isHumanMode.value) {
        messages.value.push({ sender: 'user', text: userText })
        
        if (stompClient && stompClient.connected) {
            const msgPayload = {
                sender: 'USER',
                content: userText,
                timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            };
            stompClient.send(`/app/chat/${maPhienChat}`, {}, JSON.stringify(msgPayload));
        } else {
            messages.value.push({ sender: 'bot', text: 'Mất kết nối với máy chủ. Đang thử lại...' });
        }
        scrollToBottom();
    } 
    // TRƯỜNG HỢP 2: Đang chat với Robot AI
    else {
        messages.value.push({ sender: 'user', text: userText })
        isLoading.value = true
        
        try {
            const res = await fetch('http://localhost:8080/api/chatbot/tu-van', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: userText, maPhienChat: maPhienChat })
            })

            if (res.ok) {
                const data = await res.json()
                // CHẶN HOÀN TOÀN TỪ "SILENT" HIỂN THỊ RA UI
                if (data.reply && data.reply !== 'SILENT') {
                    const formattedReply = data.reply.replace(/\n/g, '<br>')
                    messages.value.push({ sender: 'bot', text: formattedReply })
                }
            } else {
                messages.value.push({ sender: 'bot', text: 'Xin lỗi, kết nối bị gián đoạn.' })
            }
        } catch (error) {
            messages.value.push({ sender: 'bot', text: 'Không thể kết nối đến máy chủ.' })
        } finally {
            isLoading.value = false
            scrollToBottom()
        }
    }
}

onUnmounted(() => {
    if (stompClient) stompClient.disconnect();
})
</script>
<style scoped>
/* Giữ nguyên toàn bộ CSS phong cách sang xịn mịn của em */
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
    transition: transform 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}
.chat-toggle-btn:hover {
    transform: scale(1.1);
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
    line-height: 1.5;
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