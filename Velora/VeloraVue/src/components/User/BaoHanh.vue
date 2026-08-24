<template>
  <div class="bao-hanh-page">
    <Header />

    <main class="bao-hanh-main">
      <!-- HERO BANNER VVIP SANG TRỌNG (GÓC CẠNH) -->
      <section class="hero-banner">
        <div class="hero-content">
          <h1>TRUNG TÂM BẢO HÀNH</h1>
          <p>Gửi yêu cầu bảo hành dễ dàng, chủ động sắp xếp và xác nhận lịch hẹn trực tiếp với kỹ thuật viên của Velora.
          </p>
        </div>
        <div class="hero-badge">
          <strong>TIÊU CHUẨN VELORA</strong>
          <div class="badge-divider"></div>
          <p>Linh kiện chính hãng • Kỹ thuật chuyên sâu</p>
        </div>
      </section>

      <!-- MAIN GRID -->
      <div class="warranty-container">

        <!-- CỘT TRÁI: FORM GỬI YÊU CẦU MỚI -->
        <section class="form-section card-box">
          <div class="card-header-luxury">
            <h2>GỬI YÊU CẦU HỖ TRỢ MỚI</h2>
            <div class="title-line"></div>
            <p class="section-desc">Cung cấp thông tin sản phẩm và mô tả lỗi để chúng tôi chuẩn bị linh kiện thay thế.
            </p>
          </div>

          <form class="bao-hanh-form" @submit.prevent="submitForm">
            <div class="form-group">
              <label>HỌ VÀ TÊN KHÁCH HÀNG</label>
              <input v-model="form.hoTen" type="text" placeholder="Nhập họ và tên..." required />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>SỐ ĐIỆN THOẠI LIÊN HỆ</label>
                <input v-model="form.sdt" type="tel" placeholder="Nhập số điện thoại..." required />
              </div>
              <div class="form-group">
                <label>MÃ ĐƠN HÀNG <span class="auto-fill-text" v-if="isAutoFilled">(ĐÃ TỰ ĐỘNG ĐIỀN)</span></label>
                <input v-model="form.maDonHang" type="text" placeholder="Ví dụ: VELORA-..." required
                  :readonly="isAutoFilled" :class="{ 'disabled-input': isAutoFilled }" />
              </div>
            </div>

            <!-- LOGIC SẢN PHẨM -->
            <div class="form-group" v-if="isAutoFilled && availableProductsInOrder.length === 1">
              <label>SẢN PHẨM CẦN BẢO HÀNH <span class="auto-fill-text">(SẢN PHẨM DUY NHẤT TRONG ĐƠN)</span></label>
              <input type="text" v-model="form.loaiSanPham" readonly class="disabled-input input-bold" required />
            </div>

            <div class="form-group" v-else-if="isAutoFilled && availableProductsInOrder.length > 1">
              <label>SẢN PHẨM CẦN BẢO HÀNH <span class="warning-text">(VUI LÒNG CHỌN SẢN PHẨM BỊ LỖI)</span></label>
              <select v-model="form.loaiSanPham" required>
                <option value="" disabled selected>-- Chọn sản phẩm trong đơn hàng này --</option>
                <option v-for="(sp, idx) in availableProductsInOrder" :key="idx"
                  :value="sp.ten + ' (Mã SP: ' + (sp.maSanPham || 'N/A') + ')'">
                  {{ sp.ten }} (Mã SP: {{ sp.maSanPham || 'N/A' }}) - Số lượng: {{ sp.soLuong }}
                </option>
              </select>
            </div>

            <div class="form-group" v-else>
              <label>DÒNG SẢN PHẨM BẢO HÀNH</label>
              <select v-model="form.loaiSanPham" required>
                <option value="">-- Chọn dòng sản phẩm --</option>
                <option value="Đồng hồ cơ (Mechanical)">Đồng hồ cơ (Mechanical)</option>
                <option value="Đồng hồ pin (Quartz)">Đồng hồ pin (Quartz)</option>
                <option value="Đồng hồ thông minh (Smartwatch)">Đồng hồ thông minh (Smartwatch)</option>
                <option value="Đồng hồ cao cấp / Edition">Đồng hồ cao cấp / Edition</option>
              </select>
            </div>

            <div class="form-group">
              <label>HÌNH THỨC GỬI ĐỒNG HỒ CHO TRUNG TÂM</label>
              <div class="delivery-methods">
                <label class="radio-card" :class="{ 'active': form.hinhThucGiaoNhan === 'GUI_BUU_DIEN' }">
                  <input type="radio" value="GUI_BUU_DIEN" v-model="form.hinhThucGiaoNhan">
                  <span class="text">GỬI QUA ĐƠN VỊ VẬN CHUYỂN</span>
                </label>
                <label class="radio-card" :class="{ 'active': form.hinhThucGiaoNhan === 'MANG_TRUC_TIEP' }">
                  <input type="radio" value="MANG_TRUC_TIEP" v-model="form.hinhThucGiaoNhan">
                  <span class="text">TRỰC TIẾP TẠI SHOP</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label>MÔ TẢ TÌNH TRẠNG LỖI CHI TIẾT</label>
              <textarea v-model="form.moTa" rows="3"
                placeholder="Ví dụ: Đồng hồ chạy chậm, hấp hơi nước mặt kính, lệch kim" required></textarea>
            </div>

            <button class="btn-submit" type="submit">
              GỬI YÊU CẦU BẢO HÀNH
            </button>

            <div v-if="message" :class="['alert-box', message.type]">
              {{ message.text }}
            </div>
          </form>
        </section>

        <!-- CỘT PHẢI: LỊCH SỬ BẢO HÀNH -->
        <section class="history-section card-box">
          <div class="card-header-luxury">
            <h2>LỊCH SỬ & LỊCH HẸN CỦA BẠN</h2>
            <div class="title-line"></div>
            <p class="section-desc">Theo dõi tiến trình hoặc xác nhận lịch hẹn bảo hành.</p>
          </div>

          <div v-if="requestList.length === 0" class="empty-history">
            <p>CHƯA CÓ YÊU CẦU BẢO HÀNH NÀO ĐƯỢC GHI NHẬN.</p>
          </div>

          <div v-else class="history-list">
            <div v-for="item in requestList" :key="item.maBaoHanh" class="history-card">
              <div class="history-header">
                <span class="order-code">ĐƠN HÀNG: <span class="gold-text">{{ item.maDonHangCode }}</span></span>
                <span class="status-badge" :class="getStatusClass(item.trangThai)">
                  {{ getStatusText(item.trangThai) }}
                </span>
              </div>

              <div class="history-body">
                <div class="info-row"><span class="lbl">SẢN PHẨM:</span> <span class="val">{{ item.loaiSanPham }}</span>
                </div>
                <div class="info-row"><span class="lbl">HÌNH THỨC:</span> <span class="val">{{ item.hinhThucGiaoNhan ===
                  'MANG_TRUC_TIEP' ? 'KHÁCH MANG TỚI SHOP' : 'GỬI QUA VẬN CHUYỂN' }}</span></div>
                <div class="info-row"><span class="lbl">LỖI GHI NHẬN:</span> <span class="val">{{ item.moTaLoi }}</span>
                </div>

                <div v-if="item.thoiGianHen" class="appointment-proposal-box">
                  <div class="appointment-title">LỊCH HẸN TRUNG TÂM ĐỀ XUẤT:</div>
                  <div class="appointment-time">{{ new Date(item.thoiGianHen).toLocaleString('vi-VN') }}</div>

                  <div class="appointment-actions">
                    <button class="btn-confirm-schedule" @click="confirmAppointment(item.maBaoHanh)">XÁC NHẬN LỊCH
                      HẸN</button>
                    <button class="btn-reschedule" @click="openRescheduleModal(item.maBaoHanh)">YÊU CẦU ĐỔI GIỜ</button>
                  </div>
                </div>

                <div v-if="item.thoiGianKhachMongMuon" class="info-row appointment-wait">
                  <span class="lbl">YÊU CẦU ĐỔI GIỜ:</span> <span class="val">{{
                    formatDisplayTime(item.thoiGianKhachMongMuon) }}</span>
                </div>
              </div>

              <div class="history-actions-bottom">
                <div class="history-footer-info">
                  GỬI LÚC: {{ item.ngayGui ? new Date(item.ngayGui).toLocaleString('vi-VN') : '' }}
                </div>
                <button v-if="item.trangThai === 'CHO_XU_LY'" class="btn-cancel"
                  @click="cancelWarranty(item.maBaoHanh)">
                  HỦY YÊU CẦU
                </button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- Modal chọn lịch hẹn mới -->
    <div v-if="showRescheduleModal" class="modal-overlay">
      <div class="modal-content card-box">
        <div class="card-header-luxury">
          <h2>ĐỀ XUẤT LỊCH HẸN MỚI</h2>
          <div class="title-line"></div>
          <p class="section-desc">Vui lòng chọn ngày và giờ bạn có thể đến trung tâm bảo hành.</p>
        </div>
        <div class="form-group" style="margin: 20px 0;">
          <input type="datetime-local" v-model="selectedNewTime" class="input-datetime-custom" />
        </div>
        <div class="modal-actions">
          <button class="btn-cancel-modal" @click="showRescheduleModal = false">HỦY BỎ</button>
          <button class="btn-confirm-modal" @click="submitReschedule">XÁC NHẬN ĐỔI LỊCH</button>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import axios from 'axios'
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API = "http://localhost:8080/api/bao-hanh"

const form = reactive({ hoTen: '', sdt: '', maDonHang: '', loaiSanPham: '', moTa: '', hinhThucGiaoNhan: 'GUI_BUU_DIEN' })
const message = ref(null)
const requestList = ref([])

const isAutoFilled = ref(false)
const availableProductsInOrder = ref([])

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
  if (!currentUser || !currentUser.maNguoiDung) return;
  try {
    const res = await axios.get(`${API}/my-history/${currentUser.maNguoiDung}`)
    requestList.value = res.data
  } catch (err) { console.error("Lỗi tải lịch sử bảo hành:", err) }
}

const connectWebSocket = () => {
  const stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/warranty', (message) => {
        if (message.body === 'RELOAD_WARRANTY') fetchWarrantyRequests();
      });
    }
  });
  stompClient.activate();
};

const submitForm = async () => {
  const currentUser = getUser()
  if (!currentUser || !currentUser.maNguoiDung) {
    message.value = { type: "error", text: "VUI LÒNG ĐĂNG NHẬP ĐỂ THỰC HIỆN CHỨC NĂNG NÀY." }
    return
  }
  try {
    const res = await axios.post(`${API}/send`, {
      maNguoiDung: currentUser.maNguoiDung,
      hoTen: form.hoTen, sdt: form.sdt,
      maDonHangCode: form.maDonHang, loaiSanPham: form.loaiSanPham, moTaLoi: form.moTa,
      hinhThucGiaoNhan: form.hinhThucGiaoNhan
    })
    message.value = { type: "success", text: "GỬI YÊU CẦU BẢO HÀNH THÀNH CÔNG." }
    form.maDonHang = ""; form.loaiSanPham = ""; form.moTa = ""; isAutoFilled.value = false; availableProductsInOrder.value = [];
    await fetchWarrantyRequests()
  } catch (err) {
    message.value = { type: "error", text: "ĐÃ XẢY RA LỖI, VUI LÒNG THỬ LẠI." }
  }
}

const confirmAppointment = async (id) => {
  try {
    await axios.put(`${API}/${id}/confirm-schedule`)
    alert("XÁC NHẬN LỊCH HẸN THÀNH CÔNG. HẸN GẶP QUÝ KHÁCH TẠI TRUNG TÂM.")
    await fetchWarrantyRequests()
  } catch (err) { }
}

const openRescheduleModal = (id) => {
  currentRescheduleId.value = id; selectedNewTime.value = ''; showRescheduleModal.value = true;
}

const submitReschedule = async () => {
  if (!selectedNewTime.value) { alert("VUI LÒNG CHỌN NGÀY VÀ GIỜ MỚI."); return }
  try {
    await axios.put(`${API}/${currentRescheduleId.value}/reschedule-request`, { thoiGianMongMuon: selectedNewTime.value })
    alert("YÊU CẦU ĐỔI LỊCH ĐÃ ĐƯỢC GỬI.")
    showRescheduleModal.value = false
    await fetchWarrantyRequests()
  } catch (err) { }
}

const cancelWarranty = async (id) => {
  if (!confirm("BẠN CÓ CHẮC CHẮN MUỐN HỦY YÊU CẦU NÀY?")) return
  try { await axios.put(`${API}/${id}/cancel`); await fetchWarrantyRequests(); } catch (err) { }
}

const formatDisplayTime = (val) => {
  if (!val) return ''
  try { const d = new Date(val); if (!isNaN(d.getTime())) return d.toLocaleString('vi-VN') } catch (e) { }
  return val
}

// Xóa Emoji, trả về text in hoa sang trọng
const getStatusText = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "ĐANG CHỜ XỬ LÝ"
    case "CHO_NHAN_HANG": return "ĐANG CHỜ NHẬN MÁY"
    case "DA_TIEP_NHAN": return "ĐÃ XÁC NHẬN LỊCH"
    case "YEU_CAU_DOI_LICH": return "YÊU CẦU ĐỔI LỊCH"
    case "DANG_SUA_CHUA": return "ĐANG XỬ LÝ KỸ THUẬT"
    case "HOAN_TAT": return "HOÀN TẤT BẢO HÀNH"
    case "DA_HUY": return "ĐÃ HỦY YÊU CẦU"
    default: return status
  }
}

// Style cho các trạng thái dạng chữ (chỉ đổi màu viền/chữ, thiết kế vuông)
const getStatusClass = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "status-pending"
    case "CHO_NHAN_HANG": return "status-waiting"
    case "DA_TIEP_NHAN": return "status-accepted"
    case "YEU_CAU_DOI_LICH": return "status-reschedule"
    case "DANG_SUA_CHUA": return "status-processing"
    case "HOAN_TAT": return "status-completed"
    case "DA_HUY": return "status-cancelled"
    default: return ""
  }
}

onMounted(async () => {
  const currentUser = getUser()
  if (currentUser) {
    if (currentUser.hoTen) form.hoTen = currentUser.hoTen
    if (currentUser.sdt) form.sdt = currentUser.sdt
  }

  const savedOrderStr = localStorage.getItem('selectedWarrantyOrder')
  if (savedOrderStr) {
    isAutoFilled.value = true;
    try {
      const savedOrder = JSON.parse(savedOrderStr)
      form.maDonHang = savedOrder.maDonHangCode || savedOrder.maDonHang || ''

      let itemsList = []

      if (Array.isArray(savedOrder.items) && savedOrder.items.length > 0) {
        itemsList = savedOrder.items.map(i => ({
          ten: i.ten || 'Sản phẩm Velora',
          maSanPham: i.sanPhamId || i.maSanPham || 'N/A',
          soLuong: i.soLuong || 1
        }))
      } else if (Array.isArray(savedOrder.chiTietDonHangs) && savedOrder.chiTietDonHangs.length > 0) {
        itemsList = savedOrder.chiTietDonHangs.map(ct => ({
          ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Sản phẩm Velora',
          maSanPham: ct.sanPham ? ct.sanPham.maSanPham : 'N/A',
          soLuong: ct.soLuong || 1
        }))
      }

      if (itemsList.length === 0 && currentUser && currentUser.maNguoiDung) {
        const res = await axios.get(`http://localhost:8080/api/don-hang/nguoi-dung/${currentUser.maNguoiDung}`);
        const allOrders = res.data;
        const targetOrder = allOrders.find(o => o.maDonHangCode === form.maDonHang || o.maDonHang === form.maDonHang);

        if (targetOrder && targetOrder.chiTietDonHangs) {
          itemsList = targetOrder.chiTietDonHangs.map(ct => ({
            ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Sản phẩm Velora',
            maSanPham: ct.sanPham ? ct.sanPham.maSanPham : 'N/A',
            soLuong: ct.soLuong || 1
          }));
        }
      }

      availableProductsInOrder.value = itemsList;

      if (itemsList.length === 1) {
        form.loaiSanPham = `${itemsList[0].ten} (Mã SP: ${itemsList[0].maSanPham || 'N/A'})`;
      }

    } catch (e) {
      console.error("Lỗi parse data:", e)
    } finally {
      localStorage.removeItem('selectedWarrantyOrder')
    }
  }

  fetchWarrantyRequests()
  connectWebSocket()
})
</script>

<style scoped>
/* ==========================================================================
   CSS VVIP SANG TRỌNG - THIẾT KẾ GÓC CẠNH (SQUARE), KHÔNG EMOJI
========================================================================== */

.bao-hanh-page {
  background-color: #f8f6f0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Segoe UI', Tahoma, sans-serif;
}

.bao-hanh-main {
  max-width: 1240px;
  margin: 40px auto;
  padding: 0 20px;
  width: 100%;
  flex: 1;
}

/* HERO BANNER - VUÔNG, KHÔNG BO GÓC */
.hero-banner {
  background-color: #362921;
  color: #fff;
  padding: 40px 50px;
  border-radius: 0;
  /* KHÔNG BO GÓC */
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 3px solid #cca15e;
}

.hero-content h1 {
  font-size: 1.8rem;
  margin-bottom: 10px;
  font-weight: 600;
  color: #cca15e;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.hero-content p {
  color: #d4d0c7;
  max-width: 650px;
  line-height: 1.6;
  font-size: 0.95rem;
}

.hero-badge {
  background: transparent;
  border: 1px solid #cca15e;
  padding: 20px 25px;
  border-radius: 0;
  /* KHÔNG BO GÓC */
  text-align: center;
  min-width: 250px;
}

.hero-badge strong {
  font-size: 1rem;
  color: #cca15e;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  display: block;
  margin-bottom: 10px;
}

.badge-divider {
  width: 40px;
  height: 1px;
  background-color: #cca15e;
  margin: 0 auto 10px;
}

.hero-badge p {
  font-size: 0.8rem;
  color: #b5b0a5;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* LAYOUT */
.warranty-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

@media(max-width: 992px) {
  .warranty-container {
    grid-template-columns: 1fr;
  }

  .hero-banner {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .badge-divider {
    margin: 0 auto 10px;
  }
}

/* CARD BOX - VUÔNG */
.card-box {
  background: #ffffff;
  padding: 35px;
  border-radius: 0;
  border: 1px solid #eaeaea;
}

/* TIÊU ĐỀ LUXURY (K CÓ ICON) */
.card-header-luxury {
  margin-bottom: 30px;
}

.card-header-luxury h2 {
  font-size: 1.3rem;
  color: #362921;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.title-line {
  width: 60px;
  height: 2px;
  background-color: #cca15e;
  margin-bottom: 10px;
}

.section-desc {
  color: #777;
  font-size: 0.88rem;
  margin: 0;
}

/* FORM ELEMENTS - VUÔNG */
.bao-hanh-form .form-group {
  margin-bottom: 20px;
}

.bao-hanh-form label {
  display: block;
  font-weight: 600;
  font-size: 0.8rem;
  color: #362921;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.auto-fill-text {
  color: #cca15e !important;
  font-weight: 400;
}

.warning-text {
  color: #d97706 !important;
  font-weight: 400;
}

.bao-hanh-form input,
.bao-hanh-form select,
.bao-hanh-form textarea,
.input-datetime-custom {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 0;
  /* KHÔNG BO GÓC */
  font-size: 0.92rem;
  background-color: #fafafa;
  color: #333;
  font-family: inherit;
  transition: all 0.2s ease;
}

.bao-hanh-form input:focus,
.bao-hanh-form select:focus,
.bao-hanh-form textarea:focus,
.input-datetime-custom:focus {
  outline: none;
  border-color: #cca15e;
  background-color: #fff;
  box-shadow: 0 0 0 1px rgba(204, 161, 94, 0.2);
}

.disabled-input {
  background-color: #f5f5f5 !important;
  color: #888 !important;
  border-color: #eee !important;
  cursor: not-allowed;
}

.input-bold {
  color: #362921 !important;
  font-weight: 600 !important;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* RADIO CARDS (CHỌN GIAO NHẬN) - VUÔNG */
.delivery-methods {
  display: flex;
  gap: 15px;
  margin-top: 5px;
}

.radio-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  border-radius: 0;
  padding: 15px;
  cursor: pointer;
  transition: 0.2s;
  background: #fafafa;
  text-align: center;
}

.radio-card input {
  display: none;
}

.radio-card .text {
  font-size: 0.8rem;
  font-weight: 600;
  color: #777;
  letter-spacing: 0.5px;
}

.radio-card:hover {
  border-color: #cca15e;
}

.radio-card.active {
  border-color: #cca15e;
  background: #fff;
  border-width: 2px;
}

.radio-card.active .text {
  color: #362921;
}

/* BUTTONS - VUÔNG */
.btn-submit {
  width: 100%;
  background: #362921;
  color: #cca15e;
  padding: 15px;
  border: none;
  border-radius: 0;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.3s;
  margin-top: 10px;
}

.btn-submit:hover {
  background: #cca15e;
  color: #362921;
}

/* HISTORY LIST */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 600px;
  overflow-y: auto;
  padding-right: 5px;
}

.history-card {
  background: #fff;
  border: 1px solid #eaeaea;
  border-radius: 0;
  padding: 20px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f4f4f4;
}

.order-code {
  font-size: 0.85rem;
  color: #555;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.gold-text {
  color: #cca15e;
  font-weight: 700;
}

/* BADGES - KHÔNG ICON, BO GÓC = 0, DẠNG VIỀN SANG TRỌNG */
.status-badge {
  font-size: 0.7rem;
  padding: 6px 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-radius: 0;
  border: 1px solid transparent;
  background: transparent;
}

.status-pending {
  color: #b45309;
  border-color: #fde68a;
}

.status-waiting {
  color: #0369a1;
  border-color: #bae6fd;
}

.status-accepted {
  color: #15803d;
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.status-reschedule {
  color: #b91c1c;
  border-color: #fecaca;
}

.status-processing {
  color: #7e22ce;
  border-color: #f5d0fe;
}

.status-completed {
  color: #166534;
  border-color: #22c55e;
  background: #dcfce7;
}

.status-cancelled {
  color: #4b5563;
  border-color: #e5e7eb;
}

/* INFO ROWS */
.history-body .info-row {
  font-size: 0.85rem;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.5;
}

.lbl {
  font-weight: 600;
  color: #777;
  margin-right: 5px;
  font-size: 0.75rem;
}

.val {
  color: #362921;
  font-weight: 500;
}

/* APPOINTMENT BOX (GÓC CẠNH) */
.appointment-proposal-box {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-left: 3px solid #cca15e;
  padding: 15px;
  border-radius: 0;
  margin: 15px 0;
}

.appointment-title {
  font-weight: 600;
  font-size: 0.75rem;
  color: #cca15e;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.appointment-time {
  font-size: 1.1rem;
  font-weight: 700;
  color: #362921;
  margin-bottom: 15px;
  letter-spacing: 1px;
}

.appointment-actions {
  display: flex;
  gap: 10px;
}

.btn-confirm-schedule {
  background-color: #362921;
  color: #fff;
  border: none;
  padding: 8px 15px;
  border-radius: 0;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-confirm-schedule:hover {
  background-color: #cca15e;
}

.btn-reschedule {
  background-color: transparent;
  color: #362921;
  border: 1px solid #362921;
  padding: 8px 15px;
  border-radius: 0;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-reschedule:hover {
  background-color: #f4f4f4;
}

.appointment-wait {
  background: #fffbeb;
  border: 1px solid #fde68a;
  padding: 10px;
  margin-top: 10px;
}

.history-actions-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f4f4f4;
}

.history-footer-info {
  color: #aaa;
  font-size: 0.7rem;
  letter-spacing: 0.5px;
}

.btn-cancel {
  background: transparent;
  color: #dc2626;
  border: 1px solid transparent;
  padding: 5px 0;
  border-radius: 0;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: 0.2s;
  text-decoration: underline;
}

.btn-cancel:hover {
  color: #b91c1c;
}

.empty-history {
  text-align: center;
  padding: 40px 0;
  color: #888;
  font-size: 0.85rem;
  letter-spacing: 1px;
}

.alert-box {
  margin-top: 15px;
  padding: 12px;
  border-radius: 0;
  font-size: 0.85rem;
  text-align: center;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.alert-box.success {
  border: 1px solid #bbf7d0;
  color: #166534;
  background: #f0fdf4;
}

.alert-box.error {
  border: 1px solid #fecaca;
  color: #991b1b;
  background: #fef2f2;
}

/* CSS MODAL CHỌN LỊCH SANG TRỌNG */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(20, 20, 20, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  padding: 40px;
  border-radius: 0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: fadeUp 0.3s ease-out;
}

@keyframes fadeUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.btn-confirm-modal {
  background-color: #362921;
  color: #cca15e;
  border: none;
  padding: 12px 20px;
  border-radius: 0;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-confirm-modal:hover {
  background-color: #cca15e;
  color: #362921;
}

.btn-cancel-modal {
  background-color: transparent;
  color: #555;
  border: 1px solid #ddd;
  padding: 12px 20px;
  border-radius: 0;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-cancel-modal:hover {
  background-color: #f5f5f5;
  color: #333;
}
</style>