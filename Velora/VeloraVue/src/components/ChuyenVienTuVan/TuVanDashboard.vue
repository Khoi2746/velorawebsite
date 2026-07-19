<template>
    <div class="tu-van-container">
        <!-- Sidebar Danh sách cuộc hội thoại -->
        <aside class="phien-sidebar">
            <div class="sidebar-header">
                <h3>Hội Thoại Trực Tuyến</h3>
            </div>

            <!-- PHẦN DANH SÁCH KHÁCH HÀNG -->
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

            <!-- PHẦN NÚT RETURN -->
            <div class="sidebar-footer">
                <router-link to="/admin/dashboard" class="return-btn">
                    <i class="fas fa-home"></i> Return
                </router-link>
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
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stompjs';

// Đã gỡ bỏ hoàn toàn sessionStorage, dùng mảng trống lúc ban đầu
const danhSachPhien = ref([]);
const mapLichSuChat = ref({});

const phienDangChon = ref(null);
const noiDungMoi = ref('');
const messageBox = ref(null);

let stompClient = null;
const chatSubscriptions = {};

const lichSuChat = computed(() => {
    if (!phienDangChon.value) return [];
    return mapLichSuChat.value[phienDangChon.value.maPhienChat] || [];
});

const scrollToBottom = async () => {
    await nextTick();
    if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight;
}

// Hàm format tên của em
const formatTenVaMaPhong = (hoTenDayDu, maPhienChat) => {
    let ten = 'Khách';
    if (hoTenDayDu && hoTenDayDu.trim() !== '' && !hoTenDayDu.includes('ROOM')) {
        const mangTu = hoTenDayDu.trim().split(' ');
        ten = mangTu[mangTu.length - 1]; 
    }
    let maNgan = maPhienChat || 'Unknown';
    if (maPhienChat && maPhienChat.includes('ROOM_')) {
        maNgan = maPhienChat.split('ROOM_')[1]; 
    } else if (maPhienChat) {
        maNgan = maPhienChat.substring(0, 5); 
    }
    return `${ten}_${maNgan}`;
};

// Đăng ký nhận tin nhắn qua WebSocket
const subscribeToChat = (maPhienChat) => {
    if (chatSubscriptions[maPhienChat]) return;

    chatSubscriptions[maPhienChat] = stompClient.subscribe(`/topic/chat/${maPhienChat}`, (message) => {
        const received = JSON.parse(message.body);
        
        if (!mapLichSuChat.value[maPhienChat]) {
            mapLichSuChat.value[maPhienChat] = [];
        }

        // Tránh lặp tin nhắn do Admin tự gửi (Frontend đã push sẵn vào mảng)
        if (received.sender === 'ADMIN' && mapLichSuChat.value[maPhienChat].some(m => m.noiDungTinNhan === received.content && m.nguoiGui === 'ADMIN')) return; 

        mapLichSuChat.value[maPhienChat].push({
            nguoiGui: received.sender || 'USER',
            noiDungTinNhan: received.content,
            thoiGianGui: received.timestamp || new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });

        if (phienDangChon.value && phienDangChon.value.maPhienChat === maPhienChat) {
            scrollToBottom();
        }
    });
};

// ========================================================
// ĐỒNG BỘ: KẾT NỐI VÀ TẢI DANH SÁCH TỪ DATABASE
// ========================================================
const loadActiveSessions = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/chatbot/active-sessions');
        const data = await res.json();
        
        danhSachPhien.value = data.map(p => ({
            maPhienChat: p.maPhienChat,
            tieuDePhien: p.tieuDePhien,
            trangThai: 'AI_HANDLING' // Mặc định AI, hoặc em có thể lấy từ DB
        }));

        // Đăng ký lắng nghe WebSocket cho các phòng đang mở
        danhSachPhien.value.forEach(phien => {
            subscribeToChat(phien.maPhienChat);
        });
    } catch (e) {
        console.error("Lỗi load danh sách phiên đang mở:", e);
    }
};

const connectTongDai = () => {
    const socket = new SockJS('http://localhost:8080/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        // 1. Lắng nghe khách mới bấm nút tai nghe
        stompClient.subscribe('/topic/cvtv/requests', (message) => {
            const yeuCauMoi = JSON.parse(message.body);
            const tonTai = danhSachPhien.value.find(p => p.maPhienChat == yeuCauMoi.maPhienChat);
            
            if (!tonTai) {
                danhSachPhien.value.push({ 
                    maPhienChat: yeuCauMoi.maPhienChat, 
                    tieuDePhien: formatTenVaMaPhong(yeuCauMoi.tenKhach, yeuCauMoi.maPhienChat),
                    trangThai: 'AI_HANDLING' 
                });
            }
            subscribeToChat(yeuCauMoi.maPhienChat);
        });

        // 2. Load danh sách các phiên đang mở từ DB (Dùng cho Admin mở máy mới)
        loadActiveSessions();
    });
};

onMounted(() => {
    connectTongDai();
});

// ========================================================
// XỬ LÝ SỰ KIỆN: CHỌN PHIÊN, GỬI TIN, TIẾP QUẢN, ĐÓNG
// ========================================================
const chonPhien = async (phien) => {
    phienDangChon.value = phien;
    
    // Nếu chưa có lịch sử trong map, tải từ DB về
    if (!mapLichSuChat.value[phien.maPhienChat] || mapLichSuChat.value[phien.maPhienChat].length === 0) {
        try {
            const res = await fetch(`http://localhost:8080/api/chatbot/history/${phien.maPhienChat}`);
            const data = await res.json();
            mapLichSuChat.value[phien.maPhienChat] = data.map(m => ({
                nguoiGui: m.nguoiGui,
                noiDungTinNhan: m.noiDungTinNhan,
                thoiGianGui: m.thoiGianGui
            }));
        } catch (e) {
            mapLichSuChat.value[phien.maPhienChat] = [];
        }
    }
    
    scrollToBottom();
};

const guiTinNhan = async () => {
    if (!noiDungMoi.value || !phienDangChon.value) return;

    const userText = noiDungMoi.value;
    const msgTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const maPhienHienTai = phienDangChon.value.maPhienChat;

    if (!mapLichSuChat.value[maPhienHienTai]) {
        mapLichSuChat.value[maPhienHienTai] = [];
    }

    mapLichSuChat.value[maPhienHienTai].push({ nguoiGui: 'ADMIN', noiDungTinNhan: userText, thoiGianGui: msgTime });
    noiDungMoi.value = '';
    scrollToBottom();

    await fetch('http://localhost:8080/api/chatbot/admin-reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userText, maPhienChat: maPhienHienTai })
    });
};

const xuLyTiepQuan = () => {
    phienDangChon.value.trangThai = 'HUMAN_HANDLING';
    const loiChao = 'Chào Quý khách, tôi là Chuyên viên tư vấn của Velora. Xin phép hỗ trợ Quý khách trực tiếp từ bây giờ ạ!';
    const maPhienHienTai = phienDangChon.value.maPhienChat;

    if (!mapLichSuChat.value[maPhienHienTai]) {
        mapLichSuChat.value[maPhienHienTai] = [];
    }
    
    mapLichSuChat.value[maPhienHienTai].push({ nguoiGui: 'ADMIN', noiDungTinNhan: loiChao, thoiGianGui: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
    scrollToBottom();

    fetch('http://localhost:8080/api/chatbot/admin-reply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: loiChao, maPhienChat: maPhienHienTai })
    });
};

const dongPhienChat = async () => {
    if (!phienDangChon.value) return;
    const maPhienNgat = phienDangChon.value.maPhienChat;

    try {
        await fetch('http://localhost:8080/api/chatbot/luu-lich-su', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ maPhienChat: maPhienNgat, tieuDePhien: phienDangChon.value.tieuDePhien })
        });
    } catch (e) { console.error("Lỗi đóng phiên"); }

    if (chatSubscriptions[maPhienNgat]) {
        chatSubscriptions[maPhienNgat].unsubscribe();
        delete chatSubscriptions[maPhienNgat];
    }
    
    danhSachPhien.value = danhSachPhien.value.filter(p => p.maPhienChat !== maPhienNgat);
    delete mapLichSuChat.value[maPhienNgat];
    phienDangChon.value = null;
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
    display: flex;
    flex-direction: column;
}

.sidebar-header {
    padding: 20px 15px;
}

.sidebar-header h3 {
    color: #d1aa68;
    font-size: 18px;
    margin: 0;
}

.phien-list {
    flex: 1; 
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 0 15px;
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

.sidebar-footer {
    padding: 15px 20px;
    border-top: 1px solid #3a3a3a;
    background-color: #2a2a2a;
}

.return-btn {
    display: flex;
    align-items: center;
    gap: 10px;
    background: none;
    border: none;
    color: #a0a0a0;
    font-size: 15px;
    cursor: pointer;
    padding: 10px 0;
    width: 100%;
    transition: color 0.3s;
    text-decoration: none;
}

.return-btn:hover {
    color: #d1aa68;
}

.return-btn i {
    font-size: 18px;
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