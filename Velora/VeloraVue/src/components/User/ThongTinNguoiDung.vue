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
@import "../CSS/User/ThongTinNguoiDung.css";
</style>