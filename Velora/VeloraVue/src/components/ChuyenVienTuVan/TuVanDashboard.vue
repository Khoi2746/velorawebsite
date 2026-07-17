<template>
    <div class="tu-van-container">
        <!-- Sidebar Danh sách cuộc hội thoại -->
        <aside class="phien-sidebar">
            <h3>Hội Thoại Trực Tuyến</h3>
            <div class="phien-list">
                <div v-for="phien in danhSachPhien" :key="phien.maPhienChat" 
                     :class="['phien-item', { active: phienDangChon?.maPhienChat === phien.maPhienChat }]"
                     @click="chonPhien(phien)">
                    <div class="phien-info">
                        <span class="phien-title">{{ phien.tieuDePhien }}</span>
                        <span :class="['status-badge', phien.trangThai]">
                            {{ phien.trangThai === 'AI_HANDLING' ? 'Trợ lý AI' : 'Nhân viên tiếp quản' }}
                        </span>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Khung chat chi tiết -->
        <main class="chat-area">
            <div v-if="phienDangChon" class="chat-wrapper">
                <header class="chat-header">
                    <h4>{{ phienDangChon.tieuDePhien }}</h4>
                    <div class="header-actions">
                        <button v-if="phienDangChon.trangThai === 'AI_HANDLING'" class="btn-takeover" @click="xuLyTiepQuan">
                            <i class="fa-solid fa-handshake"></i> Tiếp quản cuộc chat
                        </button>
                        <button class="btn-close" @click="dongPhienChat">Đóng</button>
                    </div>
                </header>

                <!-- Lịch sử hội thoại -->
                <div class="chat-messages" ref="messageBox">
                    <div v-for="(msg, index) in lichSuChat" :key="index" :class="['msg-line', msg.nguoiGui]">
                        <span class="sender-label">{{ msg.nguoiGui === 'USER' ? 'Khách' : msg.nguoiGui === 'AI' ? 'Robot AI' : 'Bạn' }}</span>
                        <div class="msg-box">
                            <p>{{ msg.noiDungTinNhan }}</p>
                            <span class="msg-time">{{ msg.thoiGianGui }}</span>
                        </div>
                    </div>
                </div>

                <!-- Ô nhập tin nhắn -->
                <div class="chat-input-area">
                    <input v-model="noiDungMoi" type="text" placeholder="Gõ câu trả lời..." @keyup.enter="guiTinNhan"
                           :disabled="phienDangChon.trangThai === 'AI_HANDLING'" />
                    <button @click="guiTinNhan" :disabled="phienDangChon.trangThai === 'AI_HANDLING' || !noiDungMoi.trim()">
                        Gửi <i class="fa-solid fa-paper-plane"></i>
                    </button>
                </div>
                <div v-if="phienDangChon.trangThai === 'AI_HANDLING'" class="warning-overlay">
                    <span>⚠️ Nhấn "Tiếp quản cuộc chat" phía trên để trò chuyện trực tiếp với khách hàng.</span>
                </div>
            </div>
            <div v-else class="chat-empty">
                <i class="fa-solid fa-comments"></i>
                <p>Hãy chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu hỗ trợ.</p>
            </div>
        </main>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue';
import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stompjs';

// Khôi phục dữ liệu từ SessionStorage (Nếu có) để F5 không bị mất
const danhSachPhien = ref(JSON.parse(sessionStorage.getItem('admin_sessions')) || []);
const mapLichSuChat = ref(JSON.parse(sessionStorage.getItem('admin_chat_history')) || {});

const phienDangChon = ref(null);
const lichSuChat = ref([]);
const noiDungMoi = ref('');
const messageBox = ref(null);

let stompClient = null;
let currentChatSub = null;

// TỰ ĐỘNG LƯU DỮ LIỆU KHI CÓ THAY ĐỔI
watch(danhSachPhien, (val) => sessionStorage.setItem('admin_sessions', JSON.stringify(val)), { deep: true });
watch(mapLichSuChat, (val) => sessionStorage.setItem('admin_chat_history', JSON.stringify(val)), { deep: true });

const scrollToBottom = async () => {
    await nextTick();
    if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight;
}

// --- HÀM 1: KẾT NỐI TỔNG ĐÀI ---
const connectTongDai = () => {
    const socket = new SockJS('http://localhost:8080/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        console.log("✅ [ADMIN] Đã cắm giắc trực tổng đài!");
        
        stompClient.subscribe('/topic/cvtv/requests', (message) => {
            const yeuCauMoi = JSON.parse(message.body);
            const tonTai = danhSachPhien.value.find(p => p.maPhienChat == yeuCauMoi.maPhienChat);
            if (!tonTai) {
                danhSachPhien.value.push({ 
                    maPhienChat: yeuCauMoi.maPhienChat, 
                    tieuDePhien: yeuCauMoi.tenKhach, 
                    trangThai: 'AI_HANDLING' 
                });
            }
        });
    });
};

onMounted(() => {
    connectTongDai();
});

// --- HÀM 2: CHỌN PHIÊN ---
const chonPhien = (phien) => {
    phienDangChon.value = phien;
    
    // Khởi tạo trang sổ nếu chưa có
    if (!mapLichSuChat.value[phien.maPhienChat]) {
        mapLichSuChat.value[phien.maPhienChat] = [];
    }
    lichSuChat.value = mapLichSuChat.value[phien.maPhienChat];
    
    if (currentChatSub) currentChatSub.unsubscribe();

    if (stompClient && stompClient.connected) {
        currentChatSub = stompClient.subscribe(`/topic/chat/${phien.maPhienChat}`, (message) => {
            const received = JSON.parse(message.body);
            
            // Chống lặp tin nhắn Admin
            if (received.sender === 'ADMIN' && lichSuChat.value.some(m => m.noiDungTinNhan === received.content && m.nguoiGui === 'ADMIN')) return; 

            lichSuChat.value.push({
                nguoiGui: received.sender,
                noiDungTinNhan: received.content,
                thoiGianGui: received.timestamp || new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            });
            scrollToBottom();
        });
    }
};

// --- HÀM 3: GỬI TIN ---
const guiTinNhan = async () => {
    if (!noiDungMoi.value || !phienDangChon.value) return;

    const userText = noiDungMoi.value;
    const msgTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    lichSuChat.value.push({ nguoiGui: 'ADMIN', noiDungTinNhan: userText, thoiGianGui: msgTime });
    noiDungMoi.value = '';
    scrollToBottom();

    await fetch('http://localhost:8080/api/chatbot/admin-reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userText, maPhienChat: phienDangChon.value.maPhienChat })
    });
};

// --- HÀM 4: TIẾP QUẢN ---
const xuLyTiepQuan = () => {
    phienDangChon.value.trangThai = 'HUMAN_HANDLING';
    const loiChao = 'Chào Quý khách, tôi là Chuyên viên tư vấn của Velora. Xin phép hỗ trợ Quý khách trực tiếp từ bây giờ ạ!';
    
    lichSuChat.value.push({ nguoiGui: 'ADMIN', noiDungTinNhan: loiChao, thoiGianGui: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
    scrollToBottom();

    fetch('http://localhost:8080/api/chatbot/admin-reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: loiChao, maPhienChat: phienDangChon.value.maPhienChat })
    });
};

// --- HÀM 5: ĐÓNG PHIÊN (LƯU DB & DỌN DẸP) ---
const dongPhienChat = async () => {
    if (!phienDangChon.value) return;

    const maPhienNgat = phienDangChon.value.maPhienChat;
    const doanChat = mapLichSuChat.value[maPhienNgat] || [];

    // Lưu DB
    try {
        await fetch('http://localhost:8080/api/chatbot/luu-lich-su', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ maPhienChat: maPhienNgat, tieuDePhien: phienDangChon.value.tieuDePhien, tinNhanList: doanChat })
        });
    } catch (e) { console.error("Lỗi lưu DB"); }

    // Xóa sạch dấu vết
    if (currentChatSub) currentChatSub.unsubscribe();
    danhSachPhien.value = danhSachPhien.value.filter(p => p.maPhienChat !== maPhienNgat);
    delete mapLichSuChat.value[maPhienNgat];
    phienDangChon.value = null;
    lichSuChat.value = [];
};

onUnmounted(() => { if (stompClient) stompClient.disconnect(); });
</script>

<style scoped>
/* CSS Giao Diện Tư Vấn Chuyên Nghiệp */
.tu-van-container {
    display: flex;
    height: 100vh;
    background-color: #1e1e1e;
    color: #fff;
    font-family: Arial, sans-serif;
}
.phien-sidebar {
    width: 300px;
    background-color: #2a2a2a;
    border-right: 1px solid #3a3a3a;
    padding: 15px;
    display: flex;
    flex-direction: column;
}
.phien-sidebar h3 {
    color: #d1aa68;
    font-size: 18px;
    margin-bottom: 20px;
}
.phien-list {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.phien-item {
    background-color: #333;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    border: 1px solid transparent;
    transition: 0.3s;
}
.phien-item.active, .phien-item:hover {
    border-color: #d1aa68;
    background-color: #3e3e3e;
}
.phien-title {
    font-weight: bold;
    display: block;
    margin-bottom: 5px;
}
.status-badge {
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 4px;
}
.status-badge.AI_HANDLING {
    background-color: #3b5998;
    color: #fff;
}
.status-badge.HUMAN_HANDLING {
    background-color: #2ecc71;
    color: #fff;
}
.chat-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: #1a1a1a;
    position: relative;
}
.chat-wrapper {
    display: flex;
    flex-direction: column;
    height: 100%;
}
.chat-header {
    background-color: #222;
    padding: 15px 20px;
    border-bottom: 1px solid #333;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.chat-header h4 {
    color: #d1aa68;
    margin: 0;
}
.header-actions {
    display: flex;
    gap: 10px;
}
.btn-takeover {
    background-color: #d1aa68;
    color: #111;
    border: none;
    padding: 8px 15px;
    border-radius: 5px;
    font-weight: bold;
    cursor: pointer;
}
.btn-close {
    background-color: #e74c3c;
    color: #fff;
    border: none;
    padding: 8px 15px;
    border-radius: 5px;
    cursor: pointer;
}
.chat-messages {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 15px;
}
.msg-line {
    display: flex;
    flex-direction: column;
    max-width: 70%;
}
.msg-line.USER {
    align-self: flex-start;
}
.msg-line.AI {
    align-self: flex-start;
}
.msg-line.ADMIN {
    align-self: flex-end;
    align-items: flex-end;
}
.sender-label {
    font-size: 11px;
    color: #888;
    margin-bottom: 3px;
}
.msg-box {
    padding: 10px 15px;
    border-radius: 8px;
    color: #fff;
    position: relative;
}
.USER .msg-box {
    background-color: #2c3e50;
}
.AI .msg-box {
    background-color: #4f5f6f;
    border-left: 3px solid #d1aa68;
}
.ADMIN .msg-box {
    background-color: #d1aa68;
    color: #111;
}
.msg-time {
    font-size: 9px;
    color: #ccc;
    display: block;
    margin-top: 5px;
    text-align: right;
}
.chat-input-area {
    padding: 15px 20px;
    background-color: #222;
    display: flex;
    gap: 10px;
}
.chat-input-area input {
    flex: 1;
    background-color: #333;
    border: 1px solid #444;
    padding: 12px;
    border-radius: 5px;
    color: #fff;
    outline: none;
}
.chat-input-area button {
    background-color: #d1aa68;
    color: #111;
    border: none;
    padding: 0 20px;
    border-radius: 5px;
    font-weight: bold;
    cursor: pointer;
}
.warning-overlay {
    position: absolute;
    bottom: 80px;
    left: 0;
    right: 0;
    background-color: rgba(243, 156, 18, 0.9);
    color: #111;
    text-align: center;
    padding: 10px;
    font-weight: bold;
}
.chat-empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #666;
}
.chat-empty i {
    font-size: 50px;
    margin-bottom: 15px;
}
</style>    