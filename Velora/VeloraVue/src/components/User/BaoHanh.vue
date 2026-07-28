<template>
  <div class="bao-hanh-page">
    <Header />

    <main class="bao-hanh-main">
      <!-- Hero Banner -->
      <section class="hero-banner">
        <div class="hero-content">
          <h1>TRUNG TÂM BẢO HÀNH & HẬU MÃI VVIP</h1>
          <p>
            Gửi yêu cầu bảo hành dễ dàng, chủ động sắp xếp và xác nhận lịch hẹn trực tiếp với kỹ thuật viên chỉ với một cú click.
          </p>
        </div>
        <div class="hero-badge">
          <div class="badge-icon">🛡️</div>
          <strong>TIÊU CHUẨN VELORA</strong>
          <p>Linh kiện chính hãng • Kỹ thuật chuyên sâu</p>
        </div>
      </section>

      <!-- Main Grid Layout -->
      <div class="warranty-container">
        
        <!-- Cột trái: Form gửi yêu cầu mới -->
        <section class="form-section card-box">
          <div class="card-header-icon">
            <span class="icon-wrap">📝</span>
            <div>
              <h2>Gửi Yêu Cầu Hỗ Trợ Mới</h2>
              <p class="section-desc">Cung cấp thông tin sản phẩm và mô tả lỗi để chúng tôi chuẩn bị linh kiện.</p>
            </div>
          </div>

          <form class="bao-hanh-form" @submit.prevent="submitForm">
            <div class="form-group">
              <label>Họ và tên khách hàng</label>
              <input v-model="form.hoTen" type="text" placeholder="Nhập họ và tên..." required />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Số điện thoại liên hệ</label>
                <input v-model="form.sdt" type="tel" placeholder="Nhập số điện thoại..." required />
              </div>
              <div class="form-group">
                <label>Mã đơn hàng</label>
                <input v-model="form.maDonHang" type="text" placeholder="Ví dụ: VELORA-..." required />
              </div>
            </div>

            <div class="form-group">
              <label>Dòng sản phẩm bảo hành</label>
              <select v-model="form.loaiSanPham" required>
                <option value="">-- Chọn dòng sản phẩm --</option>
                <option value="Đồng hồ cơ (Mechanical)">Đồng hồ cơ (Mechanical)</option>
                <option value="Đồng hồ pin (Quartz)">Đồng hồ pin (Quartz)</option>
                <option value="Đồng hồ thông minh (Smartwatch)">Đồng hồ thông minh (Smartwatch)</option>
                <option value="Đồng hồ cao cấp / Edition">Đồng hồ cao cấp / Edition</option>
              </select>
            </div>

            <div class="form-group">
              <label>Mô tả tình trạng lỗi chi tiết</label>
              <textarea v-model="form.moTa" rows="4" placeholder="Ví dụ: Đồng hồ chạy chậm, hấp hơi nước mặt kính, lệch kim..." required></textarea>
            </div>

            <button class="btn-submit" type="submit">
              <span>🚀 Gửi Yêu Cầu Bảo Hành</span>
            </button>

            <div v-if="message" :class="['alert-box', message.type]">
              {{ message.text }}
            </div>
          </form>
        </section>

        <!-- Cột phải: Lịch sử & Tương tác lịch hẹn real-time -->
        <section class="history-section card-box">
          <div class="card-header-icon">
            <span class="icon-wrap">📋</span>
            <div>
              <h2>Lịch Sử & Lịch Hẹn Của Bạn</h2>
              <p class="section-desc">Xác nhận lịch hẹn do trung tâm đề xuất hoặc chủ động yêu cầu đổi giờ.</p>
            </div>
          </div>

          <div v-if="requestList.length === 0" class="empty-history">
            <div class="empty-icon">📭</div>
            <p>Chưa có yêu cầu bảo hành nào được ghi nhận.</p>
          </div>

          <div v-else class="history-list">
            <div v-for="item in requestList" :key="item.maBaoHanh" class="history-card">
              <div class="history-header">
                <span class="order-code">Đơn hàng: <strong>{{ item.maDonHangCode }}</strong></span>
                <span class="status-badge" :class="getStatusClass(item.trangThai)">
                  {{ getStatusText(item.trangThai) }}
                </span>
              </div>

              <div class="history-body">
                <div class="info-row"><strong>Sản phẩm:</strong> {{ item.loaiSanPham }}</div>
                <div class="info-row"><strong>Lỗi:</strong> {{ item.moTaLoi }}</div>
                
                <!-- HÔP TƯƠNG TÁC LỊCH HẸN DO ADMIN ĐỀ XUẤT -->
                <div v-if="item.thoiGianHen" class="appointment-proposal-box">
                  <div class="appointment-title">
                    <span class="pulse-dot"></span> 📅 Lịch hẹn trung tâm đề xuất đến trực tiếp:
                  </div>
                  <div class="appointment-time">{{ new Date(item.thoiGianHen).toLocaleString('vi-VN') }}</div>
                  
                  <div class="appointment-actions">
                    <button class="btn-confirm-schedule" @click="confirmAppointment(item.maBaoHanh)">
                      ✓ Xác nhận lịch này
                    </button>
                    <!-- Mở Modal chọn ngày giờ thay cho prompt cũ -->
                    <button class="btn-reschedule" @click="openRescheduleModal(item.maBaoHanh)">
                      🔄 Đổi lịch khác
                    </button>
                  </div>
                </div>

                <div v-if="item.thoiGianKhachMongMuon" class="info-row text-warning">
                  <strong>Khách yêu cầu đổi giờ:</strong> {{ formatDisplayTime(item.thoiGianKhachMongMuon) }}
                </div>

                <div class="history-footer-info">
                  <small>Gửi lúc: {{ item.ngayGui ? new Date(item.ngayGui).toLocaleString('vi-VN') : '' }}</small>
                </div>
              </div>

              <div class="history-actions-bottom" v-if="item.trangThai === 'CHO_XU_LY'">
                <button class="btn-cancel" @click="cancelWarranty(item.maBaoHanh)">
                  ✕ Hủy yêu cầu
                </button>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>

    <!-- MODAL CHỌN LẠI THỜI GIAN MANG MUỐN (Giống cách Admin chọn) -->
    <div v-if="showRescheduleModal" class="modal-overlay">
      <div class="modal-content card-box">
        <div class="card-header-icon">
          <span class="icon-wrap">📅</span>
          <div>
            <h2>Chọn Thời Gian Mới Mong Muốn</h2>
            <p class="section-desc">Vui lòng chọn ngày và giờ bạn muốn đến trung tâm bảo hành.</p>
          </div>
        </div>

        <div class="form-group" style="margin: 20px 0;">
          <label style="display:block; font-weight:600; margin-bottom:6px; color:#362921;">Thời gian hẹn mới:</label>
          <input type="datetime-local" v-model="selectedNewTime" class="input-datetime-custom" />
        </div>

        <div class="modal-actions">
          <button class="btn-cancel-modal" @click="showRescheduleModal = false">Hủy bỏ</button>
          <button class="btn-confirm-modal" @click="submitReschedule">Xác nhận gửi đổi lịch</button>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const API = "http://localhost:8080/api/bao-hanh"

const form = reactive({ hoTen: '', sdt: '', maDonHang: '', loaiSanPham: '', moTa: '' })
const message = ref(null)
const requestList = ref([])
let timer = null

// State quản lý Modal chọn lịch hẹn mới
const showRescheduleModal = ref(false)
const currentRescheduleId = ref(null)
const selectedNewTime = ref('')

const getUser = () => {
  try {
    const userStr = localStorage.getItem("user")
    return userStr ? JSON.parse(userStr) : null
  } catch (err) { return null }
}

const fetchWarrantyRequests = async () => {
  const currentUser = getUser()
  if (!currentUser || !currentUser.maNguoiDung) { requestList.value = []; return; }
  try {
    const res = await axios.get(`${API}/my-history/${currentUser.maNguoiDung}`)
    requestList.value = res.data
  } catch (err) { console.error("Lỗi tải lịch sử bảo hành:", err) }
}

const submitForm = async () => {
  const currentUser = getUser()
  if (!currentUser || !currentUser.maNguoiDung) {
    message.value = { type: "error", text: "Vui lòng đăng nhập để gửi yêu cầu bảo hành!" }
    return
  }
  try {
    const res = await axios.post(`${API}/send`, {
      maNguoiDung: currentUser.maNguoiDung,
      hoTen: form.hoTen, sdt: form.sdt,
      maDonHangCode: form.maDonHang, loaiSanPham: form.loaiSanPham, moTaLoi: form.moTa
    })
    message.value = { type: "success", text: res.data.message || "Gửi yêu cầu thành công!" }
    form.maDonHang = ""; form.loaiSanPham = ""; form.moTa = ""
    await fetchWarrantyRequests()
  } catch (err) {
    message.value = { type: "error", text: err.response?.data?.message || "Không thể gửi yêu cầu." }
  }
}

const confirmAppointment = async (id) => {
  try {
    await axios.put(`${API}/${id}/confirm-schedule`)
    alert("Đã xác nhận lịch hẹn thành công! Hẹn gặp quý khách tại showroom.")
    await fetchWarrantyRequests()
  } catch (err) { alert("Không thể xác nhận lịch hẹn.") }
}

// Mở Modal chọn giờ
const openRescheduleModal = (id) => {
  currentRescheduleId.value = id
  selectedNewTime.value = ''
  showRescheduleModal.value = true
}

// Gửi thời gian mới từ Modal lên server
const submitReschedule = async () => {
  if (!selectedNewTime.value) {
    alert("Vui lòng chọn ngày và giờ mới!")
    return
  }
  
  try {
    await axios.put(`${API}/${currentRescheduleId.value}/reschedule-request`, { 
      thoiGianMongMuon: selectedNewTime.value 
    })
    
    alert("Đã gửi yêu cầu đổi lịch đến hệ thống thành công.")
    showRescheduleModal.value = false
    await fetchWarrantyRequests()
  } catch (err) {
    alert("Không thể gửi yêu cầu đổi lịch.")
  }
}

const cancelWarranty = async (id) => {
  if (!confirm("Bạn có chắc chắn muốn hủy yêu cầu này?")) return
  try {
    await axios.put(`${API}/${id}/cancel`)
    await fetchWarrantyRequests()
  } catch (err) { alert("Không thể hủy yêu cầu.") }
}

const formatDisplayTime = (val) => {
  if (!val) return ''
  // Nếu chuỗi là định dạng datetime-local (YYYY-MM-DDTHH:mm), chuyển thành định dạng hiển thị đẹp
  try {
    const d = new Date(val)
    if (!isNaN(d.getTime())) {
      return d.toLocaleString('vi-VN')
    }
  } catch (e) {}
  return val
}

const getStatusClass = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "badge-waiting"
    case "DA_TIEP_NHAN": return "badge-accepted"
    case "YEU_CAU_DOI_LICH": return "badge-reschedule"
    case "DANG_XU_LY": return "badge-processing"
    case "HOAN_TAT": return "badge-completed"
    case "DA_HUY": return "badge-cancelled"
    default: return ""
  }
}

const getStatusText = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "🟡 Đang chờ xử lý"
    case "DA_TIEP_NHAN": return "🔵 Đã xác nhận lịch hẹn"
    case "YEU_CAU_DOI_LICH": return "🟠 Khách yêu cầu đổi lịch"
    case "DANG_XU_LY": return "⚙️ Đang xử lý kỹ thuật"
    case "HOAN_TAT": return "🟢 Hoàn tất"
    case "DA_HUY": return "⚫ Đã hủy"
    default: return status
  }
}

onMounted(() => {
  const currentUser = getUser()
  if (currentUser) {
    if (currentUser.hoTen) form.hoTen = currentUser.hoTen
    if (currentUser.sdt) form.sdt = currentUser.sdt
  }
  fetchWarrantyRequests()
  timer = setInterval(fetchWarrantyRequests, 5000)
})

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
/* CSS đồng bộ giao diện Velora */
.bao-hanh-page { background-color: #f8f6f0; min-height: 100vh; display: flex; flex-direction: column; font-family: 'Segoe UI', Tahoma, sans-serif; }
.bao-hanh-main { max-width: 1240px; margin: 30px auto; padding: 0 20px; width: 100%; flex: 1; }
.hero-banner { background: linear-gradient(135deg, #362921 0%, #47372c 100%); color: #fff; padding: 36px 40px; border-radius: 16px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; box-shadow: 0 8px 24px rgba(54, 41, 33, 0.15); }
.hero-content h1 { font-size: 1.7rem; margin-bottom: 8px; font-weight: 700; color: #cca15e; }
.hero-content p { color: #d4d0c7; max-width: 650px; line-height: 1.5; font-size: 0.95rem; }
.hero-badge { background: rgba(255, 255, 255, 0.08); border: 1px solid rgba(204, 161, 94, 0.3); padding: 16px 20px; border-radius: 12px; text-align: center; min-width: 220px; }
.badge-icon { font-size: 1.8rem; margin-bottom: 4px; }
.hero-badge strong { font-size: 0.85rem; color: #cca15e; letter-spacing: 0.5px; }
.hero-badge p { font-size: 0.75px; color: #b5b0a5; margin-top: 4px; }
.warranty-container { display: grid; grid-template-columns: 1fr 1fr; gap: 28px; }
@media(max-width: 992px) { .warranty-container { grid-template-columns: 1fr; } .hero-banner { flex-direction: column; gap: 15px; } }
.card-box { background: #ffffff; padding: 28px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); border: 1px solid #eaeaea; }
.card-header-icon { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 22px; }
.icon-wrap { font-size: 1.5rem; background: #f8f6f0; padding: 10px; border-radius: 10px; }
.card-box h2 { font-size: 1.25rem; color: #362921; font-weight: 700; margin-bottom: 4px; }
.section-desc { color: #888; font-size: 0.88rem; margin: 0; }
.bao-hanh-form .form-group { margin-bottom: 16px; }
.bao-hanh-form label { display: block; font-weight: 600; font-size: 0.85rem; color: #362921; margin-bottom: 6px; }
.bao-hanh-form input, .bao-hanh-form select, .bao-hanh-form textarea { width: 100%; padding: 11px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.92rem; background-color: #fbfbfb; }
.bao-hanh-form input:focus, .bao-hanh-form select:focus, .bao-hanh-form textarea:focus { outline: none; border-color: #cca15e; background-color: #fff; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.btn-submit { width: 100%; background: #362921; color: #cca15e; padding: 13px; border: none; border-radius: 8px; font-weight: 700; font-size: 0.95rem; cursor: pointer; transition: 0.2s; margin-top: 6px; }
.btn-submit:hover { background: #47372c; }
.history-list { display: flex; flex-direction: column; gap: 16px; max-height: 520px; overflow-y: auto; padding-right: 4px; }
.history-card { background: #fff; border: 1px solid #eaeaea; border-radius: 12px; padding: 18px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #f4f4f4; }
.order-code { font-size: 0.88rem; color: #555; }
.status-badge { font-size: 0.72rem; padding: 5px 12px; border-radius: 20px; font-weight: 600; }
.badge-waiting { background: #fef9c3; color: #a16207; }
.badge-accepted { background: #e0f2fe; color: #0369a1; }
.badge-reschedule { background: #ffedd5; color: #c2410c; }
.badge-processing { background: #fae8ff; color: #a21caf; }
.badge-completed { background: #dcfce7; color: #15803d; }
.badge-cancelled { background: #f1f5f9; color: #475569; }
.history-body .info-row { font-size: 0.88rem; color: #444; margin-bottom: 6px; }
.appointment-proposal-box { background: #f0fdf4; border: 1px solid #bbf7d0; border-left: 4px solid #22c55e; padding: 14px; border-radius: 8px; margin: 14px 0; }
.appointment-title { font-weight: 600; font-size: 0.82rem; color: #166534; margin-bottom: 6px; display: flex; align-items: center; gap: 6px; }
.pulse-dot { width: 8px; height: 8px; background-color: #22c55e; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34,197,94,0.7); } 70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(34,197,94,0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34,197,94,0); } }
.appointment-time { font-size: 1.05rem; font-weight: 700; color: #14532d; margin-bottom: 12px; }
.appointment-actions { display: flex; gap: 8px; }
.btn-confirm-schedule { background-color: #16a34a; color: white; border: none; padding: 7px 14px; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; }
.btn-confirm-schedule:hover { background-color: #15803d; }
.btn-reschedule { background-color: #47372c; color: #cca15e; border: none; padding: 7px 14px; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; }
.btn-reschedule:hover { background-color: #362921; }
.history-footer-info { margin-top: 10px; color: #888; font-size: 0.78rem; }
.btn-cancel { background: transparent; color: #dc2626; border: 1px solid #fecaca; padding: 5px 12px; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; }
.btn-cancel:hover { background: #fee2e2; }
.alert-box { margin-top: 14px; padding: 11px; border-radius: 8px; font-size: 0.88rem; text-align: center; font-weight: 500; }
.alert-box.success { background: #dcfce7; color: #166534; }
.alert-box.error { background: #fee2e2; color: #991b1b; }
.text-warning { color: #d97706; font-weight: 600; font-size: 0.85rem; margin-top: 6px; }

/* CSS Modal chọn lịch */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}
.modal-content {
  width: 100%; max-width: 480px;
  background: #ffffff;
  animation: scaleUp 0.2s ease-in-out;
}
@keyframes scaleUp {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.input-datetime-custom {
  width: 100%; padding: 12px 14px;
  border: 1px solid #ddd; border-radius: 8px;
  font-size: 1rem; background-color: #fbfbfb;
  font-family: inherit;
}
.input-datetime-custom:focus {
  outline: none; border-color: #cca15e; background-color: #fff;
}
.modal-actions {
  display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;
}
.btn-confirm-modal {
  background-color: #362921; color: #cca15e;
  border: none; padding: 10px 18px; border-radius: 8px;
  font-weight: 700; cursor: pointer;
}
.btn-confirm-modal:hover { background-color: #47372c; }
.btn-cancel-modal {
  background-color: #e5e7eb; color: #374151;
  border: none; padding: 10px 18px; border-radius: 8px;
  font-weight: 600; cursor: pointer;
}
.btn-cancel-modal:hover { background-color: #d1d5db; }
</style>