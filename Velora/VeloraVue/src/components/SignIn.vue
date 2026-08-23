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
                            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password" placeholder="Nhập mật khẩu" required />
                            <i class="fas toggle-pw-icon" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="togglePassword"></i>
                        </div>

                        <!-- 🔥 PREMIUM PASSWORD STRENGTH UI -->
                        <div class="password-strength-container" v-if="password.length > 0">
                            <div class="strength-header">
                                <span>Độ mạnh mật khẩu</span>
                                <span class="strength-text" :style="{ color: strengthColor }">{{ strengthText }}</span>
                            </div>
                            <div class="strength-bar-bg">
                                <div class="strength-bar-fill" :style="{ width: (strengthScore / 5 * 100) + '%', backgroundColor: strengthColor }"></div>
                            </div>
                            <div class="strength-criteria">
                                <div class="criteria-item" :class="{ 'valid': hasLength }">
                                    <i :class="hasLength ? 'fas fa-check-circle' : 'far fa-circle'"></i> 8+ Ký tự
                                </div>
                                <div class="criteria-item" :class="{ 'valid': hasUpper }">
                                    <i :class="hasUpper ? 'fas fa-check-circle' : 'far fa-circle'"></i> A-Z
                                </div>
                                <div class="criteria-item" :class="{ 'valid': hasLower }">
                                    <i :class="hasLower ? 'fas fa-check-circle' : 'far fa-circle'"></i> a-z
                                </div>
                                <div class="criteria-item" :class="{ 'valid': hasNumber }">
                                    <i :class="hasNumber ? 'fas fa-check-circle' : 'far fa-circle'"></i> Số
                                </div>
                                <div class="criteria-item" :class="{ 'valid': hasSpecial }">
                                    <i :class="hasSpecial ? 'fas fa-check-circle' : 'far fa-circle'"></i> @#$
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="confirmPassword">Xác nhận mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fas fa-lock input-icon"></i>
                            <input :type="showConfirmPassword ? 'text' : 'password'" id="confirmPassword" v-model="confirmPassword"
                                placeholder="Nhập lại mật khẩu" required />
                            <i class="fas toggle-pw-icon" :class="showConfirmPassword ? 'fa-eye-slash' : 'fa-eye'" @click="toggleConfirmPassword"></i>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">ĐĂNG KÝ NGAY</button>

                    <div class="social-login-separator">
                        <span>Hoặc tiếp tục với</span>
                    </div>

                    <div class="social-login-buttons">
                        <button type="button" class="btn-social btn-google" @click="loginWithGoogle">
                            <i class="fab fa-google"></i>
                            Google
                        </button>
                        <button type="button" class="btn-social btn-facebook" @click="loginWithFacebook">
                            <i class="fab fa-facebook-f"></i>
                            Facebook
                        </button>
                    </div>

                    <div class="login-redirect">
                        <span>Đã có tài khoản?</span>
                        <router-link to="/dang-nhap" class="login-link">Đăng nhập ngay</router-link>
                    </div>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const router = useRouter()
const route = useRoute()

const fullName = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

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

// 🔥 Logic Ẩn/Hiện mật khẩu
const togglePassword = () => showPassword.value = !showPassword.value
const toggleConfirmPassword = () => showConfirmPassword.value = !showConfirmPassword.value

// 🔥 Tính toán độ mạnh mật khẩu (Premium Password UI Logic)
const hasLength = computed(() => password.value.length >= 8)
const hasUpper = computed(() => /[A-Z]/.test(password.value))
const hasLower = computed(() => /[a-z]/.test(password.value))
const hasNumber = computed(() => /[0-9]/.test(password.value))
const hasSpecial = computed(() => /[^A-Za-z0-9]/.test(password.value))

const strengthScore = computed(() => {
    let score = 0
    if (hasLength.value) score++
    if (hasUpper.value) score++
    if (hasLower.value) score++
    if (hasNumber.value) score++
    if (hasSpecial.value) score++
    return score
})

const strengthText = computed(() => {
    if (strengthScore.value <= 2) return 'Yếu'
    if (strengthScore.value <= 4) return 'Khá'
    return 'Mạnh'
})

const strengthColor = computed(() => {
    if (strengthScore.value <= 2) return '#e74c3c' // Đỏ (Yếu)
    if (strengthScore.value <= 4) return '#f39c12' // Cam (Khá)
    return '#2ecc71' // Xanh lá (Mạnh)
})

// Tự động bắt lỗi từ Backend
onMounted(() => {
    const errorMsg = route.query.error;
    if (errorMsg) {
        showNotification(decodeURIComponent(errorMsg), 'error');
    }
})

const handleRegister = async () => {
    if (password.value !== confirmPassword.value) {
        showNotification("Mật khẩu không khớp! Vui lòng kiểm tra lại.", "warning")
        return
    }

    if (strengthScore.value < 3) {
        showNotification("Mật khẩu quá yếu! Vui lòng đặt mật khẩu mạnh hơn.", "warning")
        return
    }

    try {
        const response = await fetch(`${API_BASE}/api/auth/register`, {
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

// LUỒNG ĐĂNG KÝ BẰNG MẠNG XÃ HỘI
const loginWithGoogle = () => {
    window.location.href = `${API_BASE}/api/auth/oauth2/prepare/register?provider=google`;
}

const loginWithFacebook = () => {
    window.location.href = `${API_BASE}/api/auth/oauth2/prepare/register?provider=facebook`;
}
</script>

<style scoped>
@import "./CSS/SignIn.css";

.register-container {
    width: 100%;
    max-width: 480px !important; 
    padding: 50px 40px !important;
    border-radius: 0 !important; 
}

.register-header {
    margin-bottom: 28px !important;
}

.input-group {
    margin-bottom: 20px !important;
}

.input-group label {
    margin-bottom: 6px !important;
    font-size: 12px !important;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.input-wrapper {
    position: relative; /* Cần thiết để icon con mắt đặt position absolute chuẩn */
}

.input-wrapper input {
    border-radius: 0 !important; 
    padding: 12px 40px 12px 42px !important; /* Dành khoảng trống bên phải cho icon mắt */
    font-size: 14px !important;
    width: 100%;
}

/* =========================================
   🔥 PREMIUM PASSWORD STRENGTH UI 
========================================= */
.password-strength-container {
    margin-top: 10px;
    background: #170d08; /* Màu nền tiệp với form */
    padding: 12px 15px;
    border: 1px solid #333;
    animation: fadeIn 0.3s ease;
}

.strength-header {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: #888;
    margin-bottom: 8px;
}

.strength-text {
    transition: color 0.3s ease;
}

.strength-bar-bg {
    width: 100%;
    height: 4px;
    background: #2a2a2a;
    margin-bottom: 12px;
    overflow: hidden;
}

.strength-bar-fill {
    height: 100%;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.strength-criteria {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 12px;
}

.criteria-item {
    font-size: 11px;
    color: #555;
    display: flex;
    align-items: center;
    gap: 5px;
    transition: color 0.3s ease;
}

.criteria-item i {
    font-size: 12px;
    transition: all 0.3s ease;
}

.criteria-item.valid {
    color: #d1aa68; /* Khi đạt tiêu chí sẽ sáng màu Vàng Gold */
}

/* Eye Toggle Icons */
.toggle-pw-icon {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #777;
    cursor: pointer;
    font-size: 14px;
    transition: color 0.2s ease;
}

.toggle-pw-icon:hover {
    color: #d1aa68;
}

/* ========================================= */

.btn-submit {
    margin-top: 10px !important;
    padding: 12px !important;
    font-size: 14px !important;
    border-radius: 0 !important; 
}

.social-login-separator {
    display: flex;
    align-items: center;
    text-align: center;
    margin: 22px 0 16px 0 !important;
    color: #888;
    font-size: 12px;
    font-weight: 500;
}

.social-login-separator::before,
.social-login-separator::after {
    content: '';
    flex: 1;
    border-bottom: 1px solid #333;
}

.social-login-separator span {
    padding: 0 15px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.social-login-buttons {
    display: flex;
    gap: 15px;
    margin-bottom: 22px !important;
}

.btn-social {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 11px;
    border-radius: 0 !important; 
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    background: transparent;
    border: 1px solid #444;
    color: #e0e0e0;
    transition: all 0.3s ease;
}

.btn-social:hover {
    background: #1a1a1a;
    border-color: #d1aa68;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.btn-google i { color: #ea4335; font-size: 16px; }
.btn-facebook i { color: #1877f2; font-size: 16px; }

/* CSS Popup */
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
    border-radius: 0 !important; 
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

@media (max-width: 550px) {
    .register-container {
        padding: 30px 20px !important;
    }
}
</style>