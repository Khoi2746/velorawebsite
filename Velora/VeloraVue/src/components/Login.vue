<template>
    <div class="login-page">
        <Header />

        <div class="login-wrapper">
            <div class="login-container">
                <div class="login-header">
                    <h2>ĐĂNG NHẬP</h2>
                    <p>Chào mừng trở lại với thế giới thời gian của Velora</p>
                </div>

                <form @submit.prevent="handleLogin" class="login-form">

                    <div class="input-group">
                        <label for="email">Email</label>
                        <div class="input-wrapper">
                            <i class="far fa-envelope input-icon"></i>
                            <input type="email" id="email" v-model="email" placeholder="Nhập địa chỉ email của bạn"
                                required />
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="password">Mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fas fa-lock input-icon"></i>
                            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password"
                                placeholder="Nhập mật khẩu" required />
                            <i class="fas toggle-pw-icon" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"
                                @click="togglePassword"></i>
                        </div>
                    </div>

                    <div class="form-actions">
                        <label class="remember-me">
                            <input type="checkbox" v-model="rememberMe" />
                            Ghi nhớ đăng nhập
                        </label>
                        <router-link to="/quen-mat-khau" class="forgot-password">Quên mật khẩu?</router-link>
                    </div>

                    <button type="submit" class="btn-submit">
                        ĐĂNG NHẬP
                    </button>

                    <!-- 🔥 KHU VỰC ĐĂNG NHẬP MÃ HÓA MẠNG XÃ HỘI -->
                    <div class="social-login-separator">
                        <span>Hoặc đăng nhập bằng</span>
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
                    <!-- 🔥 KẾT THÚC -->

                    <div class="register-redirect">
                        <span>Bạn chưa có tài khoản?</span>
                        <router-link to="/dang-ky" class="register-link">Đăng ký ngay</router-link>
                    </div>
                </form>
            </div>
        </div>
        <Footer />
    </div>
    <ToastPopup 
        :visible="showToast" 
        :message="toastMsg" 
        :type="toastType"
        :loading="loading" 
    />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../components/Header.vue' 
import Footer from '../components/Footer.vue'
import ToastPopup from '../components/ToastPopup.vue'

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const showToast = ref(false)
const toastMsg = ref('')
const toastType = ref('success')
const loading = ref(false)

// 🔥 Tự động bắt lỗi từ Backend nếu OAuth2 trả về luồng không hợp lệ
onMounted(() => {
    const errorMsg = route.query.error;
    if (errorMsg) {
        showToast.value = true;
        toastMsg.value = decodeURIComponent(errorMsg);
        toastType.value = 'error';

        setTimeout(() => {
            showToast.value = false;
        }, 4500);
    }
})

const togglePassword = () => {
    showPassword.value = !showPassword.value
}

const handleLogin = async () => {
    loading.value = true;
    showToast.value = true;
    toastMsg.value = 'Đang xác thực thông tin...';
    toastType.value = 'loading';

    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', // 🔥 BẮT BUỘC CÓ ĐỂ NHẬN VÀ LƯU COOKIE SESSION TỪ SERVER
            body: JSON.stringify({ email: email.value, password: password.value })
        });

        await new Promise(resolve => setTimeout(resolve, 1000));

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Lỗi xác thực hệ thống!');
        }

        // 🔥 TUYỆT ĐỐI KHÔNG LƯU LOCALSTORAGE NỮA, SESSION SERVER SẼ LO TRỌN GÓI
        loading.value = false;
        toastMsg.value = 'Đăng nhập thành công!';
        toastType.value = 'success';

        setTimeout(() => {
            window.location.href = '/'; // Về trang chủ, Header sẽ tự động fetch /me từ Database
        }, 1200);

    } catch (error) {
        loading.value = false;
        toastMsg.value = error.message;
        toastType.value = 'error';

        setTimeout(() => { 
            showToast.value = false; 
        }, 4000);
    }
}

// 🔥 Chuyển hướng sang endpoint chuẩn bị mode=login của Backend
const loginWithGoogle = () => {
    window.location.href = `${API_BASE}/api/auth/oauth2/prepare/login?provider=google`;
}

const loginWithFacebook = () => {
    window.location.href = `${API_BASE}/api/auth/oauth2/prepare/login?provider=facebook`;
}
</script>

<style scoped> 
@import "./CSS/Login.css";

/* --- GỌT VUÔNG TẤT CẢ KHUNG VÀ NÚT CHO ĐỒNG BỘ LUXURY --- */
.login-container {
    border-radius: 0 !important;
}

.input-wrapper,
.input-wrapper input {
    border-radius: 0 !important;
}

.btn-submit {
    border-radius: 0 !important;
}

/* --- CSS CHO KHU VỰC MẠNG XÃ HỘI --- */
.social-login-separator {
    display: flex;
    align-items: center;
    text-align: center;
    margin: 22px 0 18px 0;
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
    margin-bottom: 25px;
}

.btn-social {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 11px;
    border-radius: 0 !important; /* Vuông vức góc cạnh */
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
</style>