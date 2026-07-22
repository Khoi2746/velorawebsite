<template>
    <div class="register-page">
        <Header />

        <div class="register-wrapper">
            <div class="register-container">
                <div class="register-header">
                    <h2>TẠO TÀI KHOẢN</h2>
                    <p>Chào mừng gia nhập thế giới thời gian của Velora Clock</p>
                </div>

                <form @submit.prevent="handleRegister" class="register-form">

                    <div class="input-group">
                        <label for="fullName">Họ và tên</label>
                        <div class="input-wrapper">
                            <i class="fas fa-user input-icon"></i>
                            <input type="text" id="fullName" v-model="fullName" placeholder="Nhập họ tên" required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="email">Email</label>
                        <div class="input-wrapper">
                            <i class="far fa-envelope input-icon"></i>
                            <input type="email" id="email" v-model="email" placeholder="Nhập địa chỉ email" required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="password">Mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fas fa-lock input-icon"></i>
                            <input type="password" id="password" v-model="password" placeholder="Nhập mật khẩu" required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="confirmPassword">Xác nhận mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fas fa-lock input-icon"></i>
                            <input type="password" id="confirmPassword" v-model="confirmPassword"
                                placeholder="Nhập lại mật khẩu" required />
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">ĐĂNG KÝ NGAY</button>

                    <div class="login-redirect">
                        <span>Đã có tài khoản?</span>
                        <router-link to="/dang-nhap" class="login-link">Đăng nhập ngay</router-link>
                    </div>
                </form>
            </div>
        </div>

        <!-- HTML KHỐI POPUP THÔNG BÁO -->
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const router = useRouter()
const fullName = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

// -- STATE QUẢN LÝ POPUP THÔNG BÁO --
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
// ----------------------------------

const handleRegister = async () => {
    if (password.value !== confirmPassword.value) {
        showNotification("Mật khẩu không khớp! Vui lòng kiểm tra lại.", "warning")
        return
    }

    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                hoTen: fullName.value,
                email: email.value,
                matKhauMaHoa: password.value
            })
        })

        if (response.ok) {
            showNotification('Đăng ký thành công! Vui lòng kiểm tra Email để nhận hướng dẫn tiếp theo.', 'success')
            
            // Đợi 2.5 giây cho khách đọc xong rồi mới chuyển trang
            setTimeout(() => {
                router.push('/dang-nhap')
            }, 2500)
        } else {
            const errorMsg = await response.text()
            showNotification('Lỗi đăng ký: ' + errorMsg, 'error')
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error)
        showNotification('Không thể kết nối đến máy chủ! Vui lòng thử lại sau.', 'error')
    }
}
</script>

<style scoped>
@import "./CSS/SignIn.css";

/* --- CSS CHO KHỐI POPUP (Giữ nguyên phong cách Velora) --- */
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
    animation: fadeIn 0.3s ease;
}

.custom-popup-box {
    background: #1e1e1e;
    border: 1px solid #d1aa68;
    border-radius: 8px;
    padding: 30px 40px;
    width: 90%;
    max-width: 420px;
    text-align: center;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
    animation: scaleUp 0.3s ease;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.popup-icon i {
    font-size: 55px;
}

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
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 100%;
}

.popup-close-btn:hover {
    background: #b8955b;
    transform: translateY(-2px);
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}
@keyframes scaleUp {
    from { transform: scale(0.9); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
}
</style>