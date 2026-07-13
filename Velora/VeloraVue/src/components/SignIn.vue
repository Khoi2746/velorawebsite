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
@import "./CSS/SignIn.css";
</style>