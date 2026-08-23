<template>
    <div class="update-page">
        <Header />

        <div class="update-wrapper">
            <div class="update-container">
                <div class="update-header">
                    <h2>HOÀN TẤT HỒ SƠ</h2>
                    <p>Vui lòng cập nhật bổ sung thông tin để trải nghiệm Velora Clock</p>
                </div>

                <form @submit.prevent="handleUpdateInfo" class="update-form">
                    
                    <div class="input-group">
                        <label for="email">Email tài khoản</label>
                        <div class="input-wrapper">
                            <i class="far fa-envelope input-icon"></i>
                            <!-- Nếu là email giả lập từ Facebook thì mở khóa cho khách tự nhập email thật -->
                            <input 
                                type="email" 
                                id="email" 
                                v-model="email" 
                                :disabled="!isFakeFacebookEmail(originalEmail)" 
                                :class="{'disabled-input': !isFakeFacebookEmail(originalEmail)}" 
                                placeholder="Nhập địa chỉ email chính xác của bạn" 
                                required 
                            />
                        </div>
                        <small v-if="isFakeFacebookEmail(originalEmail)" class="email-note">
                            * Tài khoản Facebook không cung cấp email công khai. Vui lòng nhập email thật để nhận thông báo đơn hàng!
                        </small>
                    </div>

                    <div class="input-group">
                        <label for="fullName">Họ và tên</label>
                        <div class="input-wrapper">
                            <i class="fas fa-user input-icon"></i>
                            <input type="text" id="fullName" v-model="fullName" placeholder="Nhập họ và tên" required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="phone">Số điện thoại</label>
                        <div class="input-wrapper">
                            <i class="fas fa-phone input-icon"></i>
                            <input type="tel" id="phone" v-model="phone" placeholder="Nhập số điện thoại nhận hàng" required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="address">Địa chỉ giao hàng</label>
                        <div class="input-wrapper">
                            <i class="fas fa-map-marker-alt input-icon"></i>
                            <input type="text" id="address" v-model="address" placeholder="Nhập số nhà, đường, phường/xã, tỉnh/TP" required />
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">LƯU VÀ TIẾP TỤC</button>
                </form>
            </div>
        </div>

        <div class="custom-popup-overlay" v-if="popup.show" @click="closePopup">
            <div class="custom-popup-box" :class="popup.type" @click.stop>
                <div class="popup-icon">
                    <i v-if="popup.type === 'success'" class="fas fa-check-circle"></i>
                    <i v-else-if="popup.type === 'warning'" class="fas fa-exclamation-triangle"></i>
                    <i v-else class="fas fa-times-circle"></i>
                </div>
                <div class="popup-content">
                    <h3>{{ popup.type === 'success' ? 'THÀNH CÔNG' : popup.type === 'warning' ? 'CHÚ Ý' : 'LỖI' }}</h3>
                    <p>{{ popup.message }}</p>
                </div>
                <button class="popup-close-btn" @click="closePopup">ĐÓNG</button>
            </div>
        </div>

        <Footer />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()
const route = useRoute()

const email = ref('')
const originalEmail = ref('') // Lưu lại email gốc để Backend đối chiếu
const fullName = ref('')
const phone = ref('')
const address = ref('')

const popup = ref({
    show: false,
    message: '',
    type: 'success'
})

let popupTimeout = null;

const showNotification = (message, type = 'success') => {
    popup.value = { show: true, message, type }
    if (popupTimeout) clearTimeout(popupTimeout)
    popupTimeout = setTimeout(() => {
        closePopup()
    }, 3000)
}

const closePopup = () => {
    popup.value.show = false
    if (popupTimeout) clearTimeout(popupTimeout)
}

// Kiểm tra xem có phải email giả lập từ Facebook không (chứa @facebook.com hoặc toàn số)
const isFakeFacebookEmail = (emailStr) => {
    if (!emailStr) return false;
    return emailStr.includes('@facebook.com') || /^\d+@/.test(emailStr);
}

onMounted(() => {
    if (route.query.email) {
        email.value = route.query.email
        originalEmail.value = route.query.email // Lưu lại email ban đầu
    }
    if (route.query.name) {
        fullName.value = route.query.name
    }
})

const handleUpdateInfo = async () => {
    if (!phone.value || !address.value || !email.value) {
        showNotification("Vui lòng điền đầy đủ thông tin bắt buộc!", "warning")
        return
    }

    try {
        const response = await fetch('http://localhost:8080/api/auth/cap-nhat-thong-tin', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                originalEmail: originalEmail.value, // Gửi email gốc để tìm user trong DB
                email: email.value,                 // Gửi email mới (nếu khách đã sửa lại)
                hoTen: fullName.value,
                soDienThoai: phone.value,
                diaChi: address.value
            })
        })

        if (response.ok) {
            const userData = await response.json()
            localStorage.setItem('user', JSON.stringify(userData))
            
            showNotification('Cập nhật hồ sơ thành công! Đang chuyển hướng...', 'success')
            
            setTimeout(() => {
                window.location.href = '/'
            }, 1500)
        } else {
            const errorMsg = await response.text()
            showNotification('Lỗi cập nhật: ' + errorMsg, 'error')
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error)
        showNotification('Không thể kết nối đến máy chủ! Vui lòng thử lại sau.', 'error')
    }
}
</script>

<style scoped>
@import "../CSS/SignIn.css";

.update-container {
    width: 100%;
    max-width: 480px !important; 
    padding: 50px 40px !important;
    border-radius: 0 !important; 
    background: #161616;
    border: 1px solid #333;
    box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    margin: 50px auto;
}

.update-header {
    text-align: center;
    margin-bottom: 28px !important;
}

.update-header h2 {
    color: #d1aa68;
    font-size: 22px;
    letter-spacing: 2px;
    margin-bottom: 8px;
}

.update-header p {
    color: #888;
    font-size: 13px;
}

.input-group {
    margin-bottom: 20px !important;
}

.input-group label {
    display: block;
    margin-bottom: 6px !important;
    font-size: 12px !important;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: #bbb;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.input-icon {
    position: absolute;
    left: 15px;
    color: #888;
}

.input-wrapper input {
    width: 100%;
    padding: 12px 15px 12px 42px !important;
    font-size: 14px !important;
    background: #1e1e1e;
    border: 1px solid #333;
    color: #fff;
    border-radius: 0 !important; 
    outline: none;
    transition: border-color 0.3s;
}

.input-wrapper input:focus {
    border-color: #d1aa68;
}

.disabled-input {
    background: #111 !important;
    color: #777 !important;
    cursor: not-allowed;
}

.email-note {
    color: #d1aa68;
    font-size: 11px;
    margin-top: 6px;
    display: block;
    line-height: 1.4;
}

.btn-submit {
    width: 100%;
    margin-top: 10px !important;
    padding: 13px !important;
    font-size: 14px !important;
    font-weight: bold;
    letter-spacing: 1px;
    background: #d1aa68;
    color: #111;
    border: none;
    border-radius: 0 !important; 
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-submit:hover {
    background: #b8955b;
    transform: translateY(-2px);
}

.custom-popup-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.7);
    backdrop-filter: blur(5px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

.custom-popup-box {
    background: #1e1e1e;
    border: 1px solid #d1aa68;
    border-radius: 0 !important; 
    padding: 30px 40px;
    width: 90%;
    max-width: 420px;
    text-align: center;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.popup-icon i { font-size: 55px; }
.custom-popup-box.success .popup-icon i { color: #2ecc71; }
.custom-popup-box.warning .popup-icon i { color: #f39c12; }
.custom-popup-box.error .popup-icon i { color: #e74c3c; }

.popup-content h3 {
    color: #d1aa68;
    margin: 0 0 10px 0;
    font-size: 20px;
    letter-spacing: 2px;
    font-weight: bold;
}

.popup-content p {
    color: #e0e0e0;
    margin: 0;
    font-size: 15px;
    line-height: 1.6;
}

.popup-close-btn {
    margin-top: 15px;
    background: #d1aa68;
    color: #111;
    border: none;
    padding: 12px 30px;
    font-size: 14px;
    font-weight: bold;
    letter-spacing: 1px;
    border-radius: 0 !important; 
    cursor: pointer;
    transition: all 0.3s ease;
    width: 100%;
}

.popup-close-btn:hover {
    background: #b8955b;
}
</style>