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
                        <a href="#" class="forgot-password">Quên mật khẩu?</a>
                    </div>

                    <button type="submit" class="btn-submit">
                        ĐĂNG NHẬP
                    </button>

                    <div class="register-redirect">
                        <span>Bạn chưa có tài khoản?</span>
                        <router-link to="/dang-ky" class="register-link">Đăng ký ngay</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <Footer />
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../components/Header.vue' 
import Footer from '../components/Footer.vue'

const router = useRouter()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)

const togglePassword = () => {
    showPassword.value = !showPassword.value
}

const handleLogin = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email.value, password: password.value })
        })

        if (response.ok) {
            const userData = await response.json()
            
            // 1. IN RA ĐỂ KIỂM TRA CHẮC CHẮN CÓ CỘT vaiTro KHÔNG
            console.log("Dữ liệu nhận từ Backend:", userData)

            // 2. Lưu vào Local Storage
            localStorage.setItem('user', JSON.stringify(userData))
            
            alert('Đăng nhập thành công! Chào mừng trở lại Velora Clock.')
            
            // 3. QUAN TRỌNG NHẤT: Ép load lại trang để Header cập nhật quyền Admin
            window.location.href = '/'
            
        } else {
            alert('Sai email hoặc mật khẩu. Vui lòng kiểm tra lại!')
        }
    } catch (error) {
        console.error('Lỗi kết nối Server:', error)
        alert('Không thể kết nối đến máy chủ. Vui lòng thử lại sau!')
    }
}
</script>

<style scoped>
/* ================= KHUNG CHỨA TOÀN TRANG ================= */
.login-page {
    width: 100%;
    min-height: 100vh;
    background-color: #0d0d0d;
    display: flex;
    flex-direction: column;
    /* Xếp Header ở trên, Form ở dưới */
}

/* ================= VÙNG BAO QUANH FORM (CĂN GIỮA) ================= */
.login-wrapper {
    flex: 1;
    /* Tự động chiếm trọn phần không gian còn lại bên dưới Header */
    display: flex;
    justify-content: center;
    align-items: center;
    background-image: radial-gradient(circle at center, #1a1a1a 0%, #0d0d0d 100%);
    padding: 40px 20px;
    font-family: 'Arial', sans-serif;
}

/* ================= KHUNG FORM TRÒN GÓC LUXURY ================= */
.login-container {
    width: 100%;
    max-width: 420px;
    background-color: #1a1714;
    border: 1px solid #332d27;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.5);
}

.login-header {
    text-align: center;
    margin-bottom: 35px;
}

.login-header h2 {
    color: #d1aa68;
    font-size: 26px;
    letter-spacing: 2px;
    margin: 0 0 10px 0;
    font-family: 'Times New Roman', serif;
}

.login-header p {
    color: #888888;
    font-size: 14px;
    margin: 0;
}

.input-group {
    margin-bottom: 25px;
}

.input-group label {
    display: block;
    color: #cccccc;
    font-size: 13px;
    margin-bottom: 8px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.input-icon {
    position: absolute;
    left: 15px;
    color: #888888;
    font-size: 16px;
}

.input-wrapper input {
    width: 100%;
    background-color: #24201D;
    border: 1px solid #332d27;
    border-radius: 6px;
    padding: 14px 15px 14px 45px;
    color: #ffffff;
    font-size: 15px;
    transition: all 0.3s ease;
    outline: none;
}

.input-wrapper input:focus {
    border-color: #d1aa68;
    box-shadow: 0 0 5px rgba(209, 170, 104, 0.2);
}

.input-wrapper input::placeholder {
    color: #555555;
}

.toggle-pw-icon {
    position: absolute;
    right: 15px;
    color: #888888;
    cursor: pointer;
    transition: color 0.3s ease;
}

.toggle-pw-icon:hover {
    color: #d1aa68;
}

.form-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
}

.remember-me {
    display: flex;
    align-items: center;
    color: #aaaaaa;
    font-size: 13px;
    cursor: pointer;
}

.remember-me input {
    margin-right: 8px;
    accent-color: #d1aa68;
    cursor: pointer;
}

.forgot-password {
    color: #d1aa68;
    font-size: 13px;
    text-decoration: none;
    transition: color 0.3s ease;
}

.forgot-password:hover {
    color: #ffffff;
    text-decoration: underline;
}

.btn-submit {
    width: 100%;
    background-color: #d1aa68;
    color: #111111;
    border: none;
    border-radius: 6px;
    padding: 15px;
    font-size: 15px;
    font-weight: bold;
    letter-spacing: 1px;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 25px;
}

.btn-submit:hover {
    background-color: #b8955b;
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(209, 170, 104, 0.3);
}

.register-redirect {
    text-align: center;
    font-size: 14px;
    color: #888888;
}

.register-link {
    color: #d1aa68;
    text-decoration: none;
    font-weight: bold;
    margin-left: 5px;
    transition: color 0.3s ease;
}

.register-link:hover {
    color: #ffffff;
    text-decoration: underline;
}
</style>