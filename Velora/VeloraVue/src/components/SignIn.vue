<template>
    <div class="register-page">
        <Header />

        <div class="register-wrapper">
            <div class="register-container">
                <div class="register-header">
                    <h2>TẠO TÀI KHOẢN</h2>
                    <p>Chào mừng bạn gia nhập thế giới thời gian của Velora</p>
                </div>

                <form @submit.prevent="handleRegister" class="register-form">

                    <div class="input-group">
                        <label for="fullName">Họ và tên</label>
                        <div class="input-wrapper">
                            <i class="fas fa-user input-icon"></i>
                            <input type="text" id="fullName" v-model="fullName" placeholder="Nhập họ tên của bạn"
                                required />
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
                            <input type="password" id="password" v-model="password" placeholder="Nhập mật khẩu"
                                required />
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
    </div>
    <Footer />
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

const handleRegister = async () => {
    if (password.value !== confirmPassword.value) {
        alert("Mật khẩu không khớp! Bạn vui lòng kiểm tra lại nhé.")
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
            alert('Đăng ký thành công! Chào mừng bạn gia nhập Velora Clock.')
            router.push('/dang-nhap')
        } else {
            const errorMsg = await response.text()
            alert('Lỗi đăng ký: ' + errorMsg)
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error)
        alert('Không thể kết nối đến máy chủ! Vui lòng thử lại sau.')
    }
}
</script>

<style scoped>
.register-page {
    width: 100%;
    min-height: 100vh;
    background-color: #0d0d0d;
    display: flex;
    flex-direction: column;
}

.register-wrapper {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background-image: radial-gradient(circle at center, #1a1714 0%, #0d0d0d 100%);
    padding: 40px 20px;
    font-family: 'Arial', sans-serif;
}

.register-container {
    width: 100%;
    max-width: 420px;
    background-color: #1a1714;
    border: 1px solid #332d27;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.5);
}

.register-header {
    text-align: center;
    margin-bottom: 35px;
}

.register-header h2 {
    color: #d1aa68;
    font-size: 26px;
    letter-spacing: 2px;
    margin: 0 0 10px 0;
    font-family: 'Times New Roman', serif;
}

.register-header p {
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

.btn-submit {
    width: 100%;
    background-color: #d1aa68;
    color: #111111;
    border: none;
    border-radius: 6px;
    padding: 15px;
    font-size: 15px;
    font-weight: bold;
    cursor: pointer;
    margin-top: 25px;
    transition: 0.3s;
}

.btn-submit:hover {
    background-color: #b8955b;
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(209, 170, 104, 0.3);
}

.login-redirect {
    text-align: center;
    margin-top: 20px;
    color: #888;
    font-size: 14px;
}

.login-link {
    color: #d1aa68;
    text-decoration: none;
    font-weight: bold;
    margin-left: 5px;
    transition: color 0.3s;
}

.login-link:hover {
    color: #ffffff;
    text-decoration: underline;
}
</style>