<template>
  <div class="profile-page">
    <Header />
    <main class="profile-main container">
      <div class="page-title-box">
        <h1>THÔNG TIN CÁ NHÂN</h1>
        <div class="title-divider"><span class="diamond"></span></div>
      </div>

      <div class="profile-layout">
        <!-- CỘT TRÁI: SIDEBAR MENU -->
        <aside class="profile-sidebar">
          <div class="user-avatar-box">
            <div class="avatar-circle">
              <i class="fas fa-user"></i>
            </div>
            <h3>{{ userInfo.hoTen || 'Thành Viên' }}</h3>
            <p class="role-text">{{ isAdmin ? 'QUẢN TRỊ VIÊN' : 'THÀNH VIÊN VVIP' }}</p>
          </div>

          <nav class="profile-menu">
            <a href="#" :class="{ active: activeTab === 'profile' }" @click.prevent="switchTab('profile')">
              <i class="fas fa-user-shield"></i> Hồ sơ của tôi
            </a>
            <a href="#" :class="{ active: activeTab === 'history' }" @click.prevent="switchTab('history')">
              <i class="fas fa-box-open"></i> Lịch sử đơn hàng
            </a>
            <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="switchTab('appointments')">
              <i class="fas fa-calendar-check"></i> Lịch sử lịch hẹn
            </a>
            <a href="#" class="logout-link" @click.prevent="handleLogout">
              <i class="fas fa-sign-out-alt"></i> Đăng xuất
            </a>
          </nav>
        </aside>

        <!-- CỘT PHẢI: NỘI DUNG TƯƠNG ỨNG TỪNG TAB -->
        <section class="profile-content">

          <!-- TAB 1: HỒ SƠ CỦA TÔI -->
          <div v-if="activeTab === 'profile'" class="tab-pane">
            <div class="content-header">
              <h2>HỒ SƠ CỦA TÔI</h2>
              <p>Quản lý thông tin bảo mật để nhận các đặc quyền từ Velora.</p>
            </div>

            <form class="profile-form" @submit.prevent="updateProfile">
              <div class="form-group">
                <label>HỌ VÀ TÊN</label>
                <input type="text" v-model="userInfo.hoTen" placeholder="Nhập họ và tên..." required />
              </div>

              <div class="form-group">
                <label>EMAIL (TÀI KHOẢN)</label>
                <input type="email" v-model="userInfo.email" disabled class="disabled-input" title="Không thể thay đổi email" />
              </div>

              <div class="form-group">
                <label>SỐ ĐIỆN THOẠI</label>
                <input type="tel" v-model="userInfo.soDienThoai" placeholder="Nhập số điện thoại liên hệ..." />
              </div>

              <div class="form-group">
                <label>ĐỊA CHỈ GIAO HÀNG MẶC ĐỊNH</label>
                <textarea v-model="userInfo.diaChi" rows="3" placeholder="Nhập địa chỉ nhận hàng chi tiết..."></textarea>
              </div>

              <button type="submit" class="btn-save" :disabled="isUpdating">
                {{ isUpdating ? 'ĐANG CẬP NHẬT...' : 'LƯU THAY ĐỔI' }}
              </button>

              <p v-if="successMsg" class="msg success-msg mt-3"><i class="fas fa-check-circle"></i> {{ successMsg }}</p>
              <p v-if="errorMsg" class="msg error-msg mt-3"><i class="fas fa-exclamation-circle"></i> {{ errorMsg }}</p>
            </form>
          </div>

          <!-- TAB 2: LỊCH SỬ ĐƠN HÀNG -->
          <div v-if="activeTab === 'history'" class="tab-pane">
            <div class="content-header">
              <h2>LỊCH SỬ MUA HÀNG VVIP</h2>
              <p>Danh sách các kiệt tác thời gian bạn đã sở hữu. Hỗ trợ yêu cầu bảo hành nhanh chóng.</p>
            </div>

            <div class="history-list">
              <div v-if="historyOrders.length === 0" class="empty-msg">
                <i class="fas fa-box-open"></i>
                <p>Bạn chưa có lịch sử mua hàng nào hoàn tất.</p>
              </div>

              <div v-else v-for="order in historyOrders" :key="order.maDonHang" class="history-card">
                <div class="hc-header">
                  <span class="hc-code">#{{ order.maDonHangCode }}</span>
                  <span class="hc-date">Hoàn tất ngày: {{ formatDateTime(order.ngayTao) }}</span>
                </div>

                <div class="hc-body">
                  <div class="hc-price">Tổng thanh toán: <strong>{{ formatPrice(order.tongTien) }}</strong></div>
                  <div class="hc-items">
                    <span v-for="(item, idx) in order.chiTietDonHangs" :key="idx" class="item-tag">
                      {{ item.sanPham?.tenSanPham || 'Đồng hồ Velora' }} (x{{ item.soLuong }})
                    </span>
                  </div>
                </div>

                <div class="hc-footer">
                  <span class="hc-status"><i class="fas fa-check-circle"></i> Đã giao hàng thành công</span>
                  <button class="btn-warranty" @click="goToWarrantyPage(order)">
                    <i class="fas fa-shield-alt"></i> YÊU CẦU BẢO HÀNH
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- TAB 3: QUẢN LÝ LỊCH SỬ LỊCH HẸN -->
          <div v-if="activeTab === 'appointments'" class="tab-pane">
            <div class="content-header">
              <h2>LỊCH SỬ LỊCH HẸN</h2>
              <p>Theo dõi trạng thái các lịch hẹn trải nghiệm và dịch vụ tại Velora.</p>
            </div>

            <!-- Bộ lọc trạng thái -->
            <div class="appointment-filters">
              <button 
                v-for="status in ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED']" 
                :key="status"
                :class="['filter-btn', selectedStatus === status ? 'active' : '']"
                @click="selectedStatus = status"
              >
                {{ getStatusText(status) }}
              </button>
            </div>

            <!-- Trạng thái Loading -->
            <div v-if="isLoadingAppointments" class="empty-msg">
              <i class="fas fa-spinner fa-spin"></i>
              <p>Đang tải danh sách lịch hẹn...</p>
            </div>

            <!-- Danh sách lịch hẹn -->
            <div v-else-if="filteredAppointments.length > 0" class="history-list">
              <div v-for="item in filteredAppointments" :key="item.id" class="history-card">
                <div class="hc-header">
                  <span class="hc-code">Mã lịch hẹn: #{{ item.id }}</span>
                  <span :class="['status-badge', getBadgeClass(item.status)]">
                    {{ getStatusText(item.status) }}
                  </span>
                </div>

                <div class="hc-body">
                  <div class="info-row"><i class="far fa-clock"></i> <strong>Thời gian:</strong> {{ item.appointmentTime }}</div>
                  <div class="info-row"><i class="fas fa-map-marker-alt"></i> <strong>Chi nhánh:</strong> {{ item.location }}</div>
                  <div class="info-row"><i class="fas fa-concierge-bell"></i> <strong>Dịch vụ:</strong> {{ item.serviceName }}</div>
                  <div v-if="item.note" class="info-row"><i class="far fa-comment-alt"></i> <strong>Ghi chú:</strong> {{ item.note }}</div>
                </div>

                <div v-if="item.status === 'PENDING'" class="hc-footer">
                  <button class="btn-cancel" @click="cancelAppointment(item.id)">Hủy lịch hẹn</button>
                </div>
              </div>
            </div>

            <!-- Trạng thái chưa có lịch hẹn -->
            <div v-else class="empty-msg">
              <i class="far fa-calendar-times"></i>
              <p>Chưa có lịch hẹn nào được ghi nhận.</p>
            </div>
          </div>

        </section>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()

// Tab hiện tại ('profile', 'history', 'appointments')
const activeTab = ref('profile')

// Thông tin người dùng
const userInfo = ref({ maNguoiDung: '', hoTen: '', email: '', soDienThoai: '', diaChi: '' })
const isAdmin = ref(false)
const isUpdating = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

// Quản lý Đơn hàng
const historyOrders = ref([])

// Quản lý Lịch hẹn
const appointments = ref([])
const selectedStatus = ref('ALL')
const isLoadingAppointments = ref(false)

// Khởi tạo thông tin người dùng khi Mounted
onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    router.push('/dang-nhap')
    return
  }

  const user = JSON.parse(userStr)
  userInfo.value = {
    maNguoiDung: user.maNguoiDung || user.id,
    hoTen: user.hoTen,
    email: user.email,
    soDienThoai: user.soDienThoai || user.sdt || '',
    diaChi: user.diaChi || ''
  }

  isAdmin.value = (user.vaiTro && user.vaiTro.toUpperCase() === 'ROLE_ADMIN') || (user.vaiTro === 'ADMIN')
  loadHistoryOrders(userInfo.value.maNguoiDung)
})

// Chuyển tab
const switchTab = (tab) => {
  activeTab.value = tab
  successMsg.value = ''
  errorMsg.value = ''
  if (tab === 'appointments') {
    fetchAppointments()
  }
}

// Lấy lịch sử mua hàng ĐÃ GIAO
const loadHistoryOrders = async (userId) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/don-hang/nguoi-dung/${userId}`)
    if (res.data) {
      historyOrders.value = res.data.filter(order => order.trangThaiDonHang === 'DA_GIAO' || order.trangThaiDonHang === 'HOAN_TAT')
    }
  } catch (err) {
    console.error("Lỗi tải lịch sử mua hàng:", err)
  }
}

// Cập nhật thông tin Hồ sơ cá nhân
const updateProfile = async () => {
  isUpdating.value = true
  successMsg.value = ''
  errorMsg.value = ''

  try {
    const res = await fetch(`http://localhost:8080/api/admin/cap-nhat/${userInfo.value.maNguoiDung}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
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
      window.dispatchEvent(new Event('user-updated')) // Cập nhật Header
    } else {
      errorMsg.value = `Lỗi Backend (Mã ${res.status})`
    }
  } catch (error) {
    errorMsg.value = 'Không thể kết nối đến máy chủ.'
  } finally {
    isUpdating.value = false
  }
}

// Gọi API lấy lịch sử lịch hẹn
const fetchAppointments = async () => {
  isLoadingAppointments.value = true
  const userEmail = userInfo.value.email

  try {
    const res = await fetch(`http://localhost:8080/api/lich-hen/nguoi-dung/email/${userEmail}`)
    if (res.ok) {
      const data = await res.json()
      appointments.value = data.map(item => ({
        id: item.id || item.maLichHen,
        appointmentTime: `${item.ngayHen || ''} ${item.thoiGian || ''}`.trim(),
        location: item.diaDiem || 'Showroom Velora',
        serviceName: item.sanPham ? item.sanPham.tenSanPham : (item.tenDichVu || 'Tư vấn & Thử đồng hồ'),
        note: item.ghiChu,
        status: parseTrangThai(item.trangThai)
      }))
    }
  } catch (error) {
    console.error('Lỗi kết nối Backend lịch hẹn:', error)
  } finally {
    isLoadingAppointments.value = false
  }
}

// Hàm hủy lịch hẹn
const cancelAppointment = async (id) => {
  if (confirm('Bạn có chắc chắn muốn hủy lịch hẹn này không?')) {
    try {
      const res = await fetch(`http://localhost:8080/api/lich-hen/huy/${id}`, { method: 'PUT' })
      if (res.ok) {
        alert('Đã hủy lịch hẹn thành công!')
        await fetchAppointments()
      } else {
        alert('Không thể hủy lịch hẹn!')
      }
    } catch (error) {
      console.error('Lỗi khi hủy lịch hẹn:', error)
    }
  }
}

// Logout
const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/dang-nhap')
}

// Đẩy sang trang yêu cầu bảo hành
const goToWarrantyPage = (order) => {
  localStorage.setItem('selectedWarrantyOrder', JSON.stringify(order))
  router.push('/bao-hanh')
}

// ================= CÁC HÀM TIỆN ÍCH =================
const parseTrangThai = (trangThaiNum) => {
  switch (trangThaiNum) {
    case 0: return 'PENDING'
    case 1: return 'CONFIRMED'
    case 2: return 'COMPLETED'
    case 3: return 'CANCELLED'
    default: return typeof trangThaiNum === 'string' ? trangThaiNum : 'PENDING'
  }
}

const filteredAppointments = computed(() => {
  if (selectedStatus.value === 'ALL') return appointments.value
  return appointments.value.filter(item => item.status === selectedStatus.value)
})

const getStatusText = (status) => {
  const map = { ALL: 'Tất cả', PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' }
  return map[status] || status
}

const getBadgeClass = (status) => {
  const map = { PENDING: 'badge-pending', CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled' }
  return map[status] || 'badge-default'
}

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' })
}
</script>

<style scoped>
.profile-page { background: #f8f6f0; min-height: 100vh; display: flex; flex-direction: column; font-family: 'Segoe UI', Tahoma, sans-serif; }
.profile-main { flex: 1; padding: 40px 20px; max-width: 1200px; margin: 0 auto; width: 100%; }
.page-title-box { text-align: center; margin-bottom: 40px; }
.page-title-box h1 { font-size: 24px; color: #362921; letter-spacing: 2px; font-weight: 600; margin-bottom: 10px; }
.title-divider { display: flex; align-items: center; justify-content: center; gap: 10px; }
.title-divider::before, .title-divider::after { content: ''; width: 50px; height: 1px; background-color: #cca15e; }
.diamond { width: 8px; height: 8px; background-color: #cca15e; transform: rotate(45deg); }

.profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 30px; align-items: start; }

/* SIDEBAR */
.profile-sidebar { background: #fff; border: 1px solid #eaeaea; padding: 30px 0; border-radius: 8px; }
.user-avatar-box { text-align: center; padding: 0 20px 20px; border-bottom: 1px solid #eaeaea; }
.avatar-circle { width: 70px; height: 70px; background: #cca15e; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 30px; margin: 0 auto 15px; }
.user-avatar-box h3 { font-size: 16px; color: #333; margin-bottom: 5px; font-weight: bold;}
.role-text { font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px; }

.profile-menu { display: flex; flex-direction: column; margin-top: 10px; }
.profile-menu a { padding: 15px 30px; color: #555; text-decoration: none; font-size: 14px; transition: 0.2s; border-left: 3px solid transparent; display: flex; align-items: center; gap: 10px;}
.profile-menu a:hover { color: #cca15e; background: #fdfbf7; }
.profile-menu a.active { color: #cca15e; font-weight: 600; border-left-color: #cca15e; background: #fdfbf7; }
.logout-link { color: #dc2626 !important; }

/* CONTENT PANE */
.profile-content { background: #fff; padding: 40px; border: 1px solid #eaeaea; border-radius: 8px; min-height: 500px; }
.content-header { margin-bottom: 30px; }
.content-header h2 { font-size: 18px; color: #362921; font-weight: 700; margin-bottom: 8px; }
.content-header p { font-size: 13px; color: #777; }

/* FORM CÁ NHÂN */
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #555; margin-bottom: 8px; letter-spacing: 0.5px; }
.profile-form input, .profile-form textarea { width: 100%; padding: 12px 15px; border: 1px solid #ddd; outline: none; font-family: inherit; font-size: 14px; color: #333; transition: 0.2s; border-radius: 4px; box-sizing: border-box;}
.profile-form input:focus, .profile-form textarea:focus { border-color: #cca15e; }
.disabled-input { background: #f5f5f5; color: #999 !important; cursor: not-allowed; }
.btn-save { background: #222; color: #fff; border: none; padding: 12px 30px; font-size: 13px; font-weight: 600; cursor: pointer; letter-spacing: 1px; transition: 0.2s; border-radius: 4px;}
.btn-save:hover { background: #cca15e; }
.btn-save:disabled { background: #999; cursor: not-allowed; }
.success-msg { color: #16a34a; font-size: 14px; }
.error-msg { color: #dc2626; font-size: 14px; }

/* HISTORY LIST & APPOINTMENTS */
.history-list { display: flex; flex-direction: column; gap: 15px; }
.empty-msg { text-align: center; color: #888; padding: 40px; }
.empty-msg i { font-size: 40px; color: #ddd; margin-bottom: 15px; }
.history-card { border: 1px solid #eaeaea; padding: 20px; background: #fafafa; border-radius: 6px;}
.hc-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eaeaea; padding-bottom: 12px; margin-bottom: 12px; }
.hc-code { font-weight: bold; color: #362921; }
.hc-date { font-size: 12px; color: #666; }
.hc-price { font-size: 14px; margin-bottom: 8px; }
.hc-price strong { color: #cca15e; font-size: 16px; }
.hc-items { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 15px; }
.item-tag { background: #eee; font-size: 12px; padding: 4px 8px; border-radius: 4px; color: #555; }
.hc-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 15px;}
.hc-status { font-size: 13px; color: #16a34a; font-weight: 600; }

.btn-warranty { background: #fff; border: 1px solid #cca15e; color: #cca15e; padding: 8px 15px; font-size: 12px; font-weight: bold; cursor: pointer; transition: 0.2s; border-radius: 4px;}
.btn-warranty:hover { background: #cca15e; color: #fff; }

.appointment-filters { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.filter-btn { padding: 8px 16px; border: 1px solid #ddd; background: #fff; border-radius: 20px; cursor: pointer; font-size: 13px; transition: 0.2s; }
.filter-btn.active { background: #362921; color: #fff; border-color: #362921; }
.filter-btn:hover:not(.active) { background: #f5f5f5; }

.info-row { font-size: 14px; color: #444; margin-bottom: 8px; display: flex; align-items: center; gap: 8px;}
.info-row i { color: #cca15e; width: 16px; text-align: center; }

.btn-cancel { background: white; border: 1px solid #dc2626; color: #dc2626; padding: 6px 15px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: bold; transition: 0.2s;}
.btn-cancel:hover { background: #dc2626; color: white; }

.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; color: #fff; }
.badge-pending { background-color: #f59e0b; }
.badge-confirmed { background-color: #3b82f6; }
.badge-completed { background-color: #10b981; }
.badge-cancelled { background-color: #ef4444; }
</style>