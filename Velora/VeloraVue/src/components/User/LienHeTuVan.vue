<template>
  <div class="profile-page">
    <Header />

    <main class="profile-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">ĐĂNG KÝ TƯ VẤN</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <div class="profile-layout">
          <!-- Cột Hướng dẫn bên trái -->
          <div class="profile-sidebar">
            <div class="user-avatar">
              <i class="fas fa-calendar-alt"></i>
              <h3>Đặt Lịch Hẹn</h3>
              <p class="role-tag">Velora Services</p>
            </div>
            <div class="booking-notes">
              <p><strong>Lưu ý:</strong></p>
              <ul>
                <li>Vui lòng đăng ký trước ít nhất 1 ngày.</li>
                <li>Đội ngũ chuyên viên sẽ liên hệ xác nhận qua Số điện thoại / Email trong vòng 2 giờ làm việc.</li>
              </ul>
            </div>
          </div>

          <!-- Cột Form Đăng Ký bên phải -->
          <div class="profile-form-section">
            <h2 class="section-title">THÔNG TIN ĐẶT LỊCH</h2>
            <p class="section-desc">Hãy để lại thông tin, Velora sẽ chuẩn bị không gian chu đáo nhất dành cho bạn.</p>

            <form @submit.prevent="bookAppointment" class="velora-form">
              <!-- Họ và tên -->
              <div class="form-group">
                <label>Họ và tên <span class="required">*</span></label>
                <input type="text" v-model="appointment.tenKhachHang" placeholder="Nhập họ và tên của bạn..." required />
              </div>

              <div class="form-row">
                <!-- Số điện thoại -->
                <div class="form-group flex-1">
                  <label>Số điện thoại <span class="required">*</span></label>
                  <input type="tel" v-model="appointment.soDienThoai" placeholder="Nhập số điện thoại..." pattern="[0-9]{10,11}" title="Số điện thoại phải chứa 10-11 chữ số" required />
                </div>

                <!-- Email -->
                <div class="form-group flex-1">
                  <label>Email <span class="required">*</span></label>
                  <input type="email" v-model="appointment.email" placeholder="Nhập địa chỉ email..." required />
                </div>
              </div>

              <div class="form-row">
                <!-- Ngày hẹn -->
                <div class="form-group flex-1">
                  <label>Ngày hẹn tư vấn <span class="required">*</span></label>
                  <input type="date" v-model="appointment.ngayHen" :min="minDate" required />
                </div>

                <!-- Khung giờ -->
                <div class="form-group flex-1">
                  <label>Khung giờ <span class="required">*</span></label>
                  <select v-model="appointment.thoiGian" required>
                    <option value="" disabled selected>Chọn khung giờ...</option>
                    <option value="09:00 - 10:00">09:00 - 10:00</option>
                    <option value="10:30 - 11:30">10:30 - 11:30</option>
                    <option value="14:00 - 15:00">14:00 - 15:00</option>
                    <option value="15:30 - 16:30">15:30 - 16:30</option>
                    <option value="17:00 - 18:00">17:00 - 18:00</option>
                    <option value="19:30 - 20:30">19:30 - 20:30</option>
                  </select>
                </div>
              </div>

              <!-- Sản phẩm cần tư vấn -->
              <div class="form-group">
                <label>Sản phẩm bạn quan tâm</label>
                <select v-model="appointment.idSanPham">
                  <option :value="null">-- Chọn sản phẩm cụ thể (Tùy chọn) --</option>
                  <option v-for="sp in danhSachSanPham" :key="sp.maSanPham" :value="sp.maSanPham">
                    {{ sp.tenSanPham }}
                  </option>
                </select>
              </div>

              <!-- Ghi chú -->
              <div class="form-group">
                <label>Yêu cầu đặc biệt / Ghi chú</label>
                <textarea v-model="appointment.ghiChu" rows="4" placeholder="Ví dụ: Cần tư vấn quà tặng, yêu cầu không gian riêng tư..."></textarea>
              </div>

              <!-- Khu vực nút bấm -->
              <div class="form-actions">
                <button type="submit" class="btn-primary" :disabled="isSubmitting">
                  {{ isSubmitting ? 'ĐANG GỬI YÊU CẦU...' : 'ĐĂNG KÝ HẸN TƯ VẤN' }}
                </button>
              </div>

              <p v-if="errorMsg" class="msg error"><i class="fas fa-exclamation-circle"></i> {{ errorMsg }}</p>
            </form>
          </div>
        </div>
      </div>
    </main>

    <Footer />

    <!-- Modal Thông Báo Đặt Lịch Thành Công Ở Giữa Màn Hình -->
    <div v-if="showSuccessModal" class="booking-modal-overlay" @click.self="showSuccessModal = false">
      <div class="booking-modal-card">
        <div class="modal-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h3 class="modal-title">ĐĂNG KÝ THÀNH CÔNG</h3>
        <p class="modal-desc">
          Đăng ký lịch hẹn xem đồng hồ thành công!<br />
          Mã lịch hẹn của bạn là: <strong>#{{ lastCreatedId || 'VELORA' }}</strong>
        </p>
        <div class="modal-subdesc">
          <p><i class="fas fa-envelope"></i> Email xác nhận kèm <strong>tệp tin PDF xác nhận</strong> đã được gửi tới <strong>{{ bookedDetails.email }}</strong>.</p>
          <p style="margin-top: 6px; color: #aaa; font-size: 12px;">Vui lòng kiểm tra hộp thư (bao gồm cả Hòm thư rác/Spam) của bạn.</p>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-modal-confirm" @click="showSuccessModal = false">
            ĐỒNG Ý
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router' // 1. Bổ sung useRoute

import Header from '../Header.vue'
import Footer from '../Footer.vue'

const route = useRoute() // 2. Khai báo route

const appointment = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  ngayHen: '',
  thoiGian: '',
  idSanPham: null,
  ghiChu: '',
  trangThai: 0
})

const bookedDetails = ref({})
const lastCreatedId = ref(null)
const danhSachSanPham = ref([])
const isSubmitting = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const minDate = ref('')
const showSuccessModal = ref(false)

onMounted(async () => {
  const today = new Date()
  minDate.value = today.toISOString().split('T')[0]

  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    appointment.value.tenKhachHang = user.hoTen || user.tenKhachHang || ''
    appointment.value.soDienThoai = user.soDienThoai || ''
    appointment.value.email = user.email || ''
  }

  // Tải danh sách sản phẩm trước
  await fetchProducts()

  // 3. Đọc productId từ URL và tự động gán vào ô select
  if (route.query.productId) {
    appointment.value.idSanPham = Number(route.query.productId)
  }
})

const fetchProducts = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/san-pham')
    if (res.ok) danhSachSanPham.value = await res.json()
  } catch (error) {
    console.error('Không thể tải danh sách sản phẩm:', error)
  }
}

// Hàm so sánh dùng == để an toàn khi so sánh giữa String và Number
const getTenSanPham = (id) => {
  if (!id) return 'Tư vấn chung (Không chọn sản phẩm cụ thể)'
  const sp = danhSachSanPham.value.find(item => item.maSanPham == id)
  return sp ? sp.tenSanPham : 'Tư vấn chung'
}

const bookAppointment = async () => {
  isSubmitting.value = true
  successMsg.value = ''
  errorMsg.value = ''

  const payload = {
    tenKhachHang: appointment.value.tenKhachHang,
    soDienThoai: appointment.value.soDienThoai,
    email: appointment.value.email,
    ngayHen: appointment.value.ngayHen,
    thoiGian: appointment.value.thoiGian,
    ghiChu: appointment.value.ghiChu || '',
    trangThai: appointment.value.trangThai,
    idSanPham: appointment.value.idSanPham ? Number(appointment.value.idSanPham) : null
  }

  try {
    const res = await fetch('http://localhost:8080/api/lich-hen/dat-lich', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      const rawText = await res.text()
      bookedDetails.value = { ...payload }

      try {
        const data = JSON.parse(rawText)
        const appointmentId = data.id || (data.data && data.data.id) || null

        if (appointmentId !== null && appointmentId !== undefined) {
          lastCreatedId.value = appointmentId
          successMsg.value = `Đăng ký lịch hẹn thành công! Mã lịch hẹn của bạn là: #${appointmentId}`
        } else {
          successMsg.value = `Đăng ký lịch hẹn thành công! Yêu cầu của bạn đã được ghi nhận.`
        }
        showSuccessModal.value = true
      } catch (parseError) {
        successMsg.value = `Đăng ký lịch hẹn thành công!`
        showSuccessModal.value = true
      }
      resetForm()
    } else {
      const errText = await res.text()
      errorMsg.value = errText.includes("Lỗi") || errText.includes("không") ? errText : `Đăng ký thất bại. Lỗi máy chủ (${res.status}).`
    }
  } catch (error) {
    errorMsg.value = 'Không thể kết nối tới server Spring Boot. Vui lòng thử lại!'
  } finally {
    isSubmitting.value = false
  }
}

const resetForm = () => {
  appointment.value.ngayHen = ''
  appointment.value.thoiGian = ''
  appointment.value.idSanPham = null
  appointment.value.ghiChu = ''
}
</script>
<style scoped>
/* Trạng thái trang chủ */
.profile-page { width: 100%; min-height: 100vh; display: flex; flex-direction: column; background-color: #fdfdfd; }
.profile-content { flex: 1; padding: 60px 20px 100px; }
.container { max-width: 1200px; margin: 0 auto; }
.title-wrapper { text-align: center; margin-bottom: 60px; }
.page-title { color: #24201D; font-size: 26px; font-weight: 400; letter-spacing: 4px; margin-bottom: 15px; }
.title-divider { display: flex; align-items: center; justify-content: center; }
.title-divider::before, .title-divider::after { content: ''; display: block; width: 60px; height: 1px; background-color: #d1aa68; }
.diamond { width: 6px; height: 6px; background-color: #d1aa68; transform: rotate(45deg); margin: 0 15px; }
.profile-layout { display: flex; gap: 50px; align-items: flex-start; }
.profile-sidebar { flex: 1; background-color: #ffffff; border: 1px solid #eeeeee; padding: 40px 25px; }
.user-avatar { text-align: center; }
.user-avatar i { font-size: 50px; color: #d1aa68; margin-bottom: 15px; }
.user-avatar h3 { font-size: 18px; color: #24201D; margin-bottom: 5px; font-weight: 600; }
.role-tag { font-size: 11px; color: #888888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 25px; }
.booking-notes { font-size: 13px; color: #555555; line-height: 1.6; border-top: 1px dashed #e0e0e0; padding-top: 20px; }
.booking-notes ul { padding-left: 18px; margin-top: 8px; }
.booking-notes li { margin-bottom: 8px; color: #666; }
.profile-form-section { flex: 3; background-color: #ffffff; border: 1px solid #eeeeee; padding: 50px; }
.section-title { font-size: 18px; color: #24201D; margin-bottom: 10px; font-weight: 600; letter-spacing: 1px; }
.section-desc { font-size: 13px; color: #888888; margin-bottom: 35px; }
.velora-form .form-group { margin-bottom: 25px; }
.form-row { display: flex; gap: 20px; }
.flex-1 { flex: 1; }
.required { color: #ff4444; }
.velora-form label { display: block; font-size: 12px; color: #555555; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 10px; }
.velora-form input, .velora-form select, .velora-form textarea { width: 100%; border: 1px solid #e0e0e0; padding: 15px; font-size: 14px; color: #24201D; outline: none; transition: border-color 0.3s ease; background-color: #fff; border-radius: 0; font-family: inherit; }
.velora-form input:focus, .velora-form select:focus, .velora-form textarea:focus { border-color: #d1aa68; }
.form-actions { margin-top: 40px; }
.btn-primary { background-color: #24201D; color: #ffffff; border: none; padding: 15px 40px; font-size: 12px; font-weight: 600; letter-spacing: 2px; cursor: pointer; transition: all 0.3s ease; width: 100%; }
.btn-primary:hover { background-color: #d1aa68; }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }
.msg { margin-top: 0; font-size: 13px; font-weight: 500; }
.msg.success { color: #2b7a0b; }
.msg.error { color: #ff4444; margin-top: 20px; }

/* CSS Nút Xuất PDF */
.msg-container.success-box {
  margin-top: 20px;
  padding: 15px;
  background-color: #f4fdf8;
  border: 1px solid #c6f6d5;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.email-note {
  font-size: 13px;
  color: #1b4332;
  margin: 0;
  line-height: 1.5;
}

.btn-pdf {
  background-color: #24201D;
  color: #d1aa68;
  border: 1px solid #d1aa68;
  padding: 10px 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-pdf:hover {
  background-color: #d1aa68;
  color: #ffffff;
}

/* CSS Modal Popup Thông báo Đặt Lịch Thành Công ở Giữa Màn Hình */
.booking-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.booking-modal-card {
  background-color: #1a1918;
  border: 1px solid #d1aa68;
  border-radius: 8px;
  padding: 35px 30px;
  max-width: 460px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.85);
  animation: modalPopIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes modalPopIn {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

.modal-icon i {
  font-size: 48px;
  color: #4CAF50;
  margin-bottom: 15px;
}

.modal-title {
  color: #d1aa68;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 15px;
  text-transform: uppercase;
}

.modal-desc {
  color: #ffffff;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.modal-subdesc {
  color: #dddddd;
  font-size: 13px;
  line-height: 1.5;
  background-color: #11100f;
  padding: 12px 15px;
  border-radius: 4px;
  border: 1px dashed #d1aa68;
  margin-bottom: 25px;
  text-align: left;
}

.modal-subdesc i {
  color: #d1aa68;
  margin-right: 6px;
}

.modal-actions {
  display: flex;
  justify-content: center;
}

.btn-modal-confirm {
  background-color: #d1aa68;
  color: #1a1918;
  border: none;
  padding: 12px 45px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(209, 170, 104, 0.3);
}

.btn-modal-confirm:hover {
  background-color: #e5be7a;
  color: #000000;
  transform: translateY(-1px);
}

/* CSS Template PDF xuất ra */
.pdf-wrapper {
  padding: 30px;
  background-color: #faf7f5;
  font-family: Arial, sans-serif;
  color: #2c2c2c;
  width: 190mm;
}

.pdf-header {
  background-color: #1a2a3a;
  color: #ffffff;
  text-align: center;
  padding: 20px;
  border-bottom: 3px solid #c5a059;
}

.pdf-brand {
  color: #c5a059;
  font-size: 24pt;
  letter-spacing: 3px;
  margin: 0;
}

.pdf-subbrand {
  font-size: 9pt;
  letter-spacing: 2px;
  margin: 5px 0 0 0;
}

.pdf-title {
  text-align: center;
  margin: 25px 0;
}

.pdf-title h2 {
  font-size: 16pt;
  color: #1a2a3a;
  margin: 0;
}

.pdf-divider {
  text-align: center;
  margin: 8px 0;
}

.pdf-diamond {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #c5a059;
  transform: rotate(45deg);
}

.pdf-meta {
  font-size: 10pt;
  color: #666;
}

.pdf-section {
  background: #ffffff;
  border: 1px solid #e0d8d0;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
}

.pdf-section-title {
  font-weight: bold;
  color: #1a2a3a;
  border-left: 3px solid #c5a059;
  padding-left: 8px;
  margin-bottom: 10px;
}

.pdf-table {
  width: 100%;
  border-collapse: collapse;
}

.pdf-table td {
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
  font-size: 10pt;
}

.pdf-table td.lbl {
  width: 35%;
  color: #666;
  font-weight: bold;
}

.pdf-notes {
  background: #f7f3ee;
  border-left: 3px solid #8a6d3b;
  padding: 10px 15px;
  font-size: 9pt;
  margin-top: 15px;
}

.pdf-footer {
  text-align: center;
  font-size: 8.5pt;
  color: #888;
  margin-top: 30px;
  border-top: 1px solid #ddd;
  padding-top: 10px;
}

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; }
  .form-row { flex-direction: column; gap: 0; }
  .profile-sidebar, .profile-form-section { width: 100%; }
}
</style>