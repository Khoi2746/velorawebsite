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
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #fdfdfd;
}

.profile-content {
    flex: 1;
    padding: 60px 20px 100px;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
}

.title-wrapper {
    text-align: center;
    margin-bottom: 60px;
}

.page-title {
    color: #24201D;
    font-size: 26px;
    font-weight: 400;
    letter-spacing: 4px;
    margin-bottom: 15px;
}

.title-divider {
    display: flex;
    align-items: center;
    justify-content: center;
}

.title-divider::before,
.title-divider::after {
    content: '';
    display: block;
    width: 60px;
    height: 1px;
    background-color: #d1aa68;
}

.diamond {
    width: 6px;
    height: 6px;
    background-color: #d1aa68;
    transform: rotate(45deg);
    margin: 0 15px;
}

.profile-layout {
    display: flex;
    gap: 50px;
    align-items: flex-start;
}

/* SIDEBAR */
.profile-sidebar {
    flex: 1;
    background-color: #ffffff;
    border: 1px solid #eeeeee;
    padding: 40px 30px;
    text-align: center;
}

.user-avatar i {
    font-size: 60px;
    color: #d1aa68;
    margin-bottom: 15px;
}

.user-avatar h3 {
    font-size: 18px;
    color: #24201D;
    margin-bottom: 5px;
    font-weight: 600;
}

.role-tag {
    font-size: 11px;
    color: #888888;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 30px;
}

.sidebar-menu {
    list-style: none;
    padding: 0;
    text-align: left;
}

.sidebar-menu li {
    border-bottom: 1px solid #f5f5f5;
}

.sidebar-menu li:last-child {
    border-bottom: none;
}

.sidebar-menu a {
    display: block;
    padding: 15px 0;
    color: #555555;
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s ease;
}

.sidebar-menu a:hover,
.sidebar-menu a.active {
    color: #d1aa68;
    padding-left: 10px;
    font-weight: 500;
}

.sidebar-menu a.text-danger {
    color: #ff4444;
}

/* FORM SECTION */
.profile-form-section {
    flex: 3;
    background-color: #ffffff;
    border: 1px solid #eeeeee;
    padding: 50px;
}

.section-title {
    font-size: 18px;
    color: #24201D;
    margin-bottom: 10px;
    font-weight: 600;
    letter-spacing: 1px;
}

.section-desc {
    font-size: 13px;
    color: #888888;
    margin-bottom: 35px;
}

.velora-form .form-group {
    margin-bottom: 25px;
}

.velora-form label {
    display: block;
    font-size: 12px;
    color: #555555;
    font-weight: 600;
    letter-spacing: 1px;
    text-transform: uppercase;
    margin-bottom: 10px;
}

.velora-form input,
.velora-form textarea {
    width: 100%;
    border: 1px solid #e0e0e0;
    padding: 15px;
    font-size: 14px;
    color: #24201D;
    outline: none;
    transition: border-color 0.3s ease;
    background-color: #fff;
}

.velora-form input:focus,
.velora-form textarea:focus {
    border-color: #d1aa68;
}

.velora-form .readonly-input {
    background-color: #f9f9f9;
    color: #999999;
    cursor: not-allowed;
}

.form-actions {
    margin-top: 40px;
}

.btn-primary {
    background-color: #24201D;
    color: #ffffff;
    border: none;
    padding: 15px 40px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 2px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-primary:hover {
    background-color: #d1aa68;
}

.btn-primary:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.msg {
    margin-top: 20px;
    font-size: 13px;
    font-weight: 500;
}

.msg.success {
    color: #2b7a0b;
}

.msg.error {
    color: #ff4444;
}

/* RESPONSIVE */
@media (max-width: 768px) {
    .profile-layout {
        flex-direction: column;
    }

    .profile-sidebar,
    .profile-form-section {
        width: 100%;
    }
}
</style>