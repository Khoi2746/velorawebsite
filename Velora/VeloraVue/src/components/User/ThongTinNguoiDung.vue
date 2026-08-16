<template>
  <div class="profile-page">
    <Header />

        <main class="profile-content">
            <div class="container">
                <div class="title-wrapper">
                    <h1 class="page-title">THÔNG TIN CÁ NHÂN</h1>
                    <div class="title-divider">
                        <span class="diamond"></span>
                    </div>
                </div>

                <div class="profile-layout">
                    <!-- Cột Menu bên trái (Tùy chọn) -->
                    <div class="profile-sidebar">
                        <div class="user-avatar">
                            <i class="fas fa-user-circle"></i>
                            <h3>{{ userInfo.hoTen || 'Thành Viên' }}</h3>
                            <p class="role-tag">{{ isAdmin ? 'Quản Trị Viên' : 'Khách Hàng VIP' }}</p>
                        </div>
                        <ul class="sidebar-menu">
                            <li><router-link to="/thong-tin-ca-nhan" class="active">Hồ sơ của tôi</router-link></li>
                            <li><router-link to="/don-hang">Lịch sử đơn hàng</router-link></li>
                            <li><a href="#" @click.prevent="logout" class="text-danger">Đăng xuất</a></li>
                        </ul>
                    </div>

                    <!-- Cột Form bên phải -->
                    <div class="profile-form-section">
                        <h2 class="section-title">HỒ SƠ CỦA TÔI</h2>
                        <p class="section-desc">Quản lý thông tin bảo mật để nhận các đặc quyền từ Velora.</p>

                        <form @submit.prevent="updateProfile" class="velora-form">
                            <div class="form-group">
                                <label>Họ và tên</label>
                                <input type="text" v-model="userInfo.hoTen" placeholder="Nhập họ và tên..." required />
                            </div>

                            <div class="form-group">
                                <label>Email (Tài khoản)</label>
                                <input type="email" v-model="userInfo.email" class="readonly-input" readonly
                                    title="Không thể thay đổi email" />
                            </div>

                            <div class="form-group">
                                <label>Số điện thoại</label>
                                <input type="tel" v-model="userInfo.soDienThoai"
                                    placeholder="Nhập số điện thoại liên hệ..." />
                            </div>

                            <div class="form-group">
                                <label>Địa chỉ giao hàng mặc định</label>
                                <textarea v-model="userInfo.diaChi" rows="3"
                                    placeholder="Nhập địa chỉ nhận hàng chi tiết..."></textarea>
                            </div>

                            <div class="form-actions">
                                <button type="submit" class="btn-primary" :disabled="isUpdating">
                                    {{ isUpdating ? 'ĐANG CẬP NHẬT...' : 'LƯU THAY ĐỔI' }}
                                </button>
                            </div>

                            <p v-if="successMsg" class="msg success"><i class="fas fa-check-circle"></i> {{ successMsg
                                }}</p>
                            <p v-if="errorMsg" class="msg error"><i class="fas fa-exclamation-circle"></i> {{ errorMsg
                                }}</p>
                        </form>
                    </div>
                </div>
            </div>
        </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue' // Chỉnh lại đường dẫn cho đúng với project của em
import Footer from '../Footer.vue'

const router = useRouter()

// Biến lưu thông tin user
const userInfo = ref({
    maNguoiDung: '',
    hoTen: '',
    email: '',
    soDienThoai: '',
    diaChi: ''
})

const isAdmin = ref(false)
const isUpdating = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

// Load dữ liệu khi vào trang
onMounted(() => {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
        alert('Vui lòng đăng nhập để xem thông tin!')
        router.push('/dang-nhap')
        return
    }

    const user = JSON.parse(userStr)
    userInfo.value.maNguoiDung = user.maNguoiDung
    userInfo.value.hoTen = user.hoTen
    userInfo.value.email = user.email
    userInfo.value.soDienThoai = user.soDienThoai || ''
    userInfo.value.diaChi = user.diaChi || ''

    isAdmin.value = (user.vaiTro && user.vaiTro.toUpperCase() === 'ROLE_ADMIN')
})

// Xóa thông báo khi gõ lại
const clearMessages = () => {
    successMsg.value = ''
    errorMsg.value = ''
}

// Hàm gọi API Cập nhật
const updateProfile = async () => {
  isUpdating.value = true
  clearMessages()

  console.log("Đang gửi lệnh cập nhật cho ID:", userInfo.value.maNguoiDung);

  try {
    const res = await fetch(`http://localhost:8080/api/admin/cap-nhat/${userInfo.value.maNguoiDung}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        hoTen: userInfo.value.hoTen,
        soDienThoai: userInfo.value.soDienThoai,
        diaChi: userInfo.value.diaChi
      })
    })

    if (res.ok) {
      const updatedUser = await res.json()
      
      let currentUser = JSON.parse(localStorage.getItem('user'))
      currentUser.hoTen = updatedUser.hoTen
      currentUser.soDienThoai = updatedUser.soDienThoai
      currentUser.diaChi = updatedUser.diaChi
      
      localStorage.setItem('user', JSON.stringify(currentUser))
      successMsg.value = 'Cập nhật thông tin thành công!'
      window.dispatchEvent(new Event('user-updated'))
      
    } else {
      // ĐỌC THẲNG LỖI TỪ BACKEND ĐỂ CHẨN ĐOÁN
      const errorText = await res.text();
      console.error("Java báo lỗi:", errorText);
      errorMsg.value = `Lỗi Backend (Mã ${res.status}): Nhấn F12 chọn tab Console để xem chi tiết!`;
    }
  } catch (error) {
    console.error('Lỗi kết nối:', error)
    errorMsg.value = 'Không thể kết nối đến máy chủ. Hãy chắc chắn Server Java đang chạy!'
  } finally {
    isUpdating.value = false
  }
}

const logout = () => {
    localStorage.removeItem('user')
    alert('Đã đăng xuất!')
    window.location.href = '/'
}
</script>

<style scoped>
.profile-page {
    background: #f8f6f0;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    font-family: 'Segoe UI', Tahoma, sans-serif;
}

.profile-main {
    flex: 1;
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
}

.page-title-box {
    text-align: center;
    margin-bottom: 40px;
}

.page-title-box h1 {
    font-size: 24px;
    color: #362921;
    letter-spacing: 2px;
    font-weight: 600;
    margin-bottom: 10px;
}

.title-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.title-divider::before,
.title-divider::after {
    content: '';
    width: 50px;
    height: 1px;
    background-color: #cca15e;
}

.diamond {
    width: 8px;
    height: 8px;
    background-color: #cca15e;
    transform: rotate(45deg);
}

.profile-layout {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 30px;
    align-items: start;
}

/* SIDEBAR */
.profile-sidebar {
    background: #fff;
    border: 1px solid #eaeaea;
    padding: 30px 0;
}

.user-avatar-box {
    text-align: center;
    padding: 0 20px 20px;
    border-bottom: 1px solid #eaeaea;
}

.avatar-circle {
    width: 70px;
    height: 70px;
    background: #cca15e;
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin: 0 auto 15px;
}

.user-avatar-box h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 5px;
}

.role-text {
    font-size: 11px;
    color: #888;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.profile-menu {
    display: flex;
    flex-direction: column;
    margin-top: 10px;
}

.profile-menu a {
    padding: 15px 30px;
    color: #555;
    text-decoration: none;
    font-size: 14px;
    transition: 0.2s;
    border-left: 3px solid transparent;
}

.profile-menu a:hover {
    color: #cca15e;
    background: #fdfbf7;
}

.profile-menu a.active {
    color: #cca15e;
    font-weight: 600;
    border-left-color: #cca15e;
    background: #fdfbf7;
}

.logout-link {
    color: #dc2626 !important;
}

/* CONTENT PANE */
.profile-content {
    background: #fff;
    padding: 40px;
    border: 1px solid #eaeaea;
    min-height: 500px;
}

.content-header {
    margin-bottom: 30px;
}

.content-header h2 {
    font-size: 18px;
    color: #362921;
    font-weight: 700;
    margin-bottom: 8px;
}

.content-header p {
    font-size: 13px;
    color: #777;
}

/* FORM CÁ NHÂN */
.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    font-size: 12px;
    font-weight: 600;
    color: #555;
    margin-bottom: 8px;
    letter-spacing: 0.5px;
}

.profile-form input,
.profile-form textarea {
    width: 100%;
    padding: 12px 15px;
    border: 1px solid #ddd;
    outline: none;
    font-family: inherit;
    font-size: 14px;
    color: #333;
    transition: 0.2s;
}

.profile-form input:focus,
.profile-form textarea:focus {
    border-color: #cca15e;
}

.disabled-input {
    background: #f5f5f5;
    color: #999 !important;
    cursor: not-allowed;
}

.btn-save {
    background: #222;
    color: #fff;
    border: none;
    padding: 12px 30px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    letter-spacing: 1px;
    transition: 0.2s;
}

.btn-save:hover {
    background: #cca15e;
}

/* HISTORY LIST */
.history-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.empty-msg {
    text-align: center;
    color: #888;
    padding: 40px;
}

.empty-msg i {
    font-size: 40px;
    color: #ddd;
    margin-bottom: 15px;
}

.history-card {
    border: 1px solid #eaeaea;
    padding: 20px;
    background: #fafafa;
}

.hc-header {
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid #eaeaea;
    padding-bottom: 12px;
    margin-bottom: 12px;
}

.hc-code {
    font-weight: bold;
    color: #362921;
}

.hc-date {
    font-size: 12px;
    color: #666;
}

.hc-price {
    font-size: 14px;
    margin-bottom: 8px;
}

.hc-price strong {
    color: #cca15e;
    font-size: 16px;
}

.hc-items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 15px;
}

.item-tag {
    background: #eee;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
    color: #555;
}

.hc-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.hc-status {
    font-size: 13px;
    color: #16a34a;
    font-weight: 600;
}

.btn-warranty {
    background: #fff;
    border: 1px solid #cca15e;
    color: #cca15e;
    padding: 8px 15px;
    font-size: 12px;
    font-weight: bold;
    cursor: pointer;
    transition: 0.2s;
}

.btn-warranty:hover {
    background: #cca15e;
    color: #fff;
}
</style>