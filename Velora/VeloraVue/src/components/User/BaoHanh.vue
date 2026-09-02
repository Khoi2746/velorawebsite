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
                <label>MÃ ĐƠN HÀNG <span class="required-star">*</span></label>
                <div style="display: flex; gap: 10px;">
                  <input v-model="form.maDonHang" type="text" placeholder="Ví dụ: VELORA-..." required 
                         @blur="verifyOrderCode" />
                  <button type="button" class="btn-verify-order" @click="verifyOrderCode">KIỂM TRA</button>
                </div>
              </div>
            </div>
            <div class="form-group" v-if="orderCheckMessage">
              <small :style="{ color: isOrderValid ? '#276749' : '#e53e3e', fontWeight: '600' }">
                {{ orderCheckMessage }}
              </small>
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
          </form>
        </section>

        <!-- KHUNG TÌM KIẾM TRA CỨU BẢO HÀNH NẰM Ở GIỮA -->
        <section class="lookup-section card-box">
          <div class="card-header-luxury">
            <h2>TRA CỨU THỜI HẠN BẢO HÀNH</h2>
            <div class="title-line"></div>
            <p class="section-desc">Nhập mã đơn hàng hoặc mã sản phẩm để kiểm tra thông tin bảo hành trực tuyến.</p>
          </div>

          <div class="lookup-form-group">
            <div class="form-group" style="flex: 1; margin-bottom: 0;">
              <input type="text" v-model="lookupQuery" placeholder="Nhập mã đơn hàng (Ví dụ: VELORA-...)"
                class="bao-hanh-input lookup-custom-input" @keyup.enter="handleLookupWarranty" />
            </div>
            <button class="btn-lookup" @click="handleLookupWarranty">
              KIỂM TRA
            </button>
          </div>
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
                <div class="info-row"><span class="lbl">MÃ YÊU CẦU:</span> <span class="val">#{{ item.maBaoHanh }}</span></div>
                <div class="info-row"><span class="lbl">SẢN PHẨM:</span> <span class="val">{{ item.loaiSanPham }}</span></div>
                <div class="info-row"><span class="lbl">HÌNH THỨC:</span> <span class="val">{{ item.hinhThucGiaoNhan === 'MANG_TRUC_TIEP' ? 'KHÁCH MANG TỚI SHOP' : 'GỬI QUA VẬN CHUYỂN' }}</span></div>
                <div class="info-row"><span class="lbl">LIÊN HỆ:</span> <span class="val">{{ item.hoTen }} - {{ item.soDienThoai }}</span></div>
                <div class="info-row"><span class="lbl">LỖI GHI NHẬN:</span> <span class="val">{{ item.moTaLoi }}</span></div>

                <!-- Giờ hẹn ĐÃ CHỐT -->
                <div v-if="['DA_TIEP_NHAN', 'DANG_SUA_CHUA', 'HOAN_TAT'].includes(item.trangThai) && item.thoiGianHen"
                  class="info-row appointment-wait" style="border-left: 4px solid #276749;">
                  <span class="lbl">LỊCH HẸN ĐÃ CHỐT:</span>
                  <span class="val" style="color:#276749; font-weight:700;">{{ formatDisplayTime(item.thoiGianHen) }}</span>
                </div>

                <div v-if="item.trangThai === 'DA_DE_XUAT_LICH' && item.thoiGianHen" class="appointment-proposal-box">
                  <div class="appointment-title">LỊCH HẸN TRUNG TÂM ĐỀ XUẤT:</div>
                  <div class="appointment-time">{{ formatDisplayTime(item.thoiGianHen) }}</div>

                  <div class="appointment-actions">
                    <button class="btn-confirm-schedule" @click="confirmAppointment(item.maBaoHanh)">XÁC NHẬN LỊCH HẸN</button>
                    <button class="btn-reschedule" @click="openRescheduleModal(item.maBaoHanh)">YÊU CẦU ĐỔI GIỜ</button>
                  </div>
                </div>

                <div v-if="item.thoiGianKhachMongMuon" class="info-row appointment-wait">
                  <span class="lbl">YÊU CẦU ĐỔI GIỜ:</span> <span class="val">{{ formatDisplayTime(item.thoiGianKhachMongMuon) }}</span>
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

    <!-- MODAL POPUP TRA CỨU BẢO HÀNH (VVIP DARK MODE) -->
    <div v-if="lookupResultModal" class="velora-modal-overlay" @click.self="lookupResultModal = null">
      <div class="velora-modal-card">
        <div class="modal-icon-wrapper" :class="lookupResultModal.isValid ? 'success' : 'error'">
          <span class="icon-symbol">{{ lookupResultModal.isValid ? '✓' : '✕' }}</span>
        </div>
        <h3 class="modal-title">KẾT QUẢ TRA CỨU BẢO HÀNH</h3>
        <p class="modal-desc" style="text-align: left; margin-top: 15px; line-height: 1.6;">
          <strong>Mã đơn hàng:</strong> {{ lookupResultModal.maDonHang }}<br>
          <strong>Sản phẩm:</strong> {{ lookupResultModal.tenSanPham }}<br>
          <strong>Ngày mua hàng:</strong> {{ lookupResultModal.ngayMua || 'N/A' }}<br>
          <strong>Hạn bảo hành:</strong> {{ lookupResultModal.hanBaoHanh || 'N/A' }}<br>
          <strong>Trạng thái:</strong> <span
            :style="{ color: lookupResultModal.isValid ? '#4ade80' : '#f87171', fontWeight: 'bold' }">{{
              lookupResultModal.trangThaiBaoHanh }}</span>
        </p>
        <button class="modal-btn-close" @click="lookupResultModal = null" style="margin-top: 20px;">ĐÓNG</button>
      </div>
    </div>

    <!-- CÁC MODAL HỆ THỐNG KHÁC -->
    <div v-if="message" class="velora-modal-overlay" @click.self="message = null">
      <div class="velora-modal-card">
        <div class="modal-icon-wrapper" :class="message.type">
          <span v-if="message.type === 'success'" class="icon-symbol">✓</span>
          <span v-else class="icon-symbol">✕</span>
        </div>
        <h3 class="modal-title">{{ message.type === 'success' ? 'THÀNH CÔNG' : 'LỖI HỆ THỐNG' }}</h3>
        <p class="modal-desc">{{ message.text }}</p>
        <button class="modal-btn-close" @click="message = null">ĐÓNG</button>
      </div>
    </div>

    <div v-if="showCancelConfirmModal" class="velora-modal-overlay" @click.self="showCancelConfirmModal = false">
      <div class="velora-modal-card">
        <div class="modal-icon-wrapper error">
          <span class="icon-symbol">✕</span>
        </div>
        <h3 class="modal-title">XÁC NHẬN HỦY</h3>
        <p class="modal-desc">BẠN CÓ CHẮC CHẮN MUỐN HỦY YÊU CẦU BẢO HÀNH NÀY KHÔNG?</p>
        <div style="display: flex; gap: 10px; width: 100%;">
          <button class="modal-btn-close" style="background: transparent; border: 1px solid #cca15e; color: #cca15e;"
            @click="showCancelConfirmModal = false">GIỮ LẠI</button>
          <button class="modal-btn-close" @click="executeCancelWarranty">XÁC NHẬN HỦY</button>
        </div>
      </div>
    </div>

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

// Thêm logic cho Tra cứu bảo hành
const lookupQuery = ref('')
const lookupResultModal = ref(null)

const handleLookupWarranty = async () => {
  const query = lookupQuery.value.trim()
  if (!query) {
    message.value = { type: "error", text: "VUI LÒNG NHẬP MÃ ĐƠN HÀNG HOẶC MÃ SẢN PHẨM ĐỂ TRA CỨU." }
    return
  }

  const currentUser = getUser()
  const rawUserId = currentUser ? (currentUser.maNguoiDung || currentUser.id) : null
  const userId = rawUserId ? parseInt(String(rawUserId).split(':')[0]) : null

  try {
    const res = await axios.get(`${API}/lookup`, {
      params: { code: query, userId: userId }
    })

    if (res && res.data) {
      lookupResultModal.value = res.data
    }
  } catch (err) {
    const errorMsg = err.response?.data?.message || "Mã đơn hàng không tồn tại hoặc quý khách chưa sở hữu sản phẩm này."
    lookupResultModal.value = {
      maDonHang: query,
      tenSanPham: "Không xác định",
      isValid: false,
      trangThaiBaoHanh: "Tra cứu thất bại",
      hanBaoHanh: "N/A"
    }
    message.value = { type: "error", text: errorMsg }
  }
}

const getUser = () => {
  try {
    const userStr = localStorage.getItem("user")
    if (!userStr) return null
    return JSON.parse(userStr)
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

const orderCheckMessage = ref('')
const isOrderValid = ref(false)

const verifyOrderCode = async () => {
  const code = form.maDonHang.trim()
  if (!code) {
    orderCheckMessage.value = "Vui lòng nhập mã đơn hàng."
    isOrderValid.value = false
    return
  }

  const currentUser = getUser()
  const userId = currentUser ? (currentUser.maNguoiDung || currentUser.id) : null

  try {
    const res = await axios.get(`${API}/kiem-tra`, {
      params: { code: code, userId: userId }
    })
    
    if (res.data && res.data.isValid) {
      isOrderValid.value = true
      orderCheckMessage.value = "✓ Mã đơn hàng hợp lệ."
      
      if (res.data.items && res.data.items.length > 0) {
        availableProductsInOrder.value = res.data.items
        if (res.data.items.length === 1) {
          form.loaiSanPham = `${res.data.items[0].ten} (Mã SP: ${res.data.items[0].maSanPham || 'N/A'})`
        }
      }
    } else {
      isOrderValid.value = false
      orderCheckMessage.value = "✕ Mã đơn hàng không tồn tại hoặc không thuộc sở hữu của bạn."
      availableProductsInOrder.value = []
    }
  } catch (err) {
    isOrderValid.value = false
    orderCheckMessage.value = "✕ Không tìm thấy thông tin đơn hàng này. Vui lòng kiểm tra lại."
    availableProductsInOrder.value = []
  }
}

const submitForm = async () => {
  if (!isOrderValid.value) {
    message.value = { type: "error", text: "VUI LÒNG NHẬP VÀ XÁC THỰC MÃ ĐƠN HÀNG HỢP LỆ TRƯỚC KHI GỬI." }
    return
  }
  
  const currentUser = getUser()
  if (!currentUser || !currentUser.maNguoiDung) {
    message.value = { type: "error", text: "VUI LÒNG ĐĂNG NHẬP ĐỂ THỰC HIỆN CHỨC NĂNG NÀY." }
    return
  }

  try {
    await axios.post(`${API}/send`, {
      maNguoiDung: currentUser.maNguoiDung,
      hoTen: form.hoTen, 
      sdt: form.sdt,
      maDonHangCode: form.maDonHang,
      loaiSanPham: form.loaiSanPham, 
      moTaLoi: form.moTa,
      hinhThucGiaoNhan: form.hinhThucGiaoNhan
    })

    message.value = { type: "success", text: "GỬI YÊU CẦU BẢO HÀNH THÀNH CÔNG." }
    form.maDonHang = ""
    form.loaiSanPham = ""
    form.moTa = ""
    isOrderValid.value = false
    orderCheckMessage.value = ""
    availableProductsInOrder.value = []
    await fetchWarrantyRequests()
  } catch (err) {
    console.error("Lỗi gửi bảo hành:", err.response?.data || err)
    message.value = { type: "error", text: err.response?.data?.message || "ĐÃ XẢY RA LỖI, VUI LÒNG THỬ LẠI." }
  }
}

const confirmAppointment = async (id) => {
  try {
    await axios.put(`${API}/${id}/confirm-schedule`)
    message.value = { type: "success", text: "XÁC NHẬN LỊCH HẸN THÀNH CÔNG. HẸN GẶP QUÝ KHÁCH TẠI TRUNG TÂM." }
    await fetchWarrantyRequests()
  } catch (err) {
    message.value = { type: "error", text: "ĐÃ XẢY RA LỖI KHI XÁC NHẬN LỊCH HẸN." }
  }
}

const openRescheduleModal = (id) => {
  currentRescheduleId.value = id; selectedNewTime.value = ''; showRescheduleModal.value = true;
}

const submitReschedule = async () => {
  if (!selectedNewTime.value) {
    message.value = { type: "error", text: "VUI LÒNG CHỌN NGÀY VÀ GIỜ MỚI." }
    return
  }
  try {
    await axios.put(`${API}/${currentRescheduleId.value}/reschedule-request`, { thoiGianMongMuon: selectedNewTime.value })
    showRescheduleModal.value = false
    message.value = { type: "success", text: "YÊU CẦU ĐỔI LỊCH ĐÃ ĐƯỢC GỬI THÀNH CÔNG." }
    await fetchWarrantyRequests()
  } catch (err) {
    message.value = { type: "error", text: "ĐÃ XẢY RA LỖI KHI GỬI YÊU CẦU ĐỔI LỊCH." }
  }
}

const formatDisplayTime = (val) => {
  if (!val) return ''
  try { const d = new Date(val); if (!isNaN(d.getTime())) return d.toLocaleString('vi-VN') } catch (e) { }
  return val
}

const showCancelConfirmModal = ref(false)
const currentCancelId = ref(null)

const cancelWarranty = (id) => {
  currentCancelId.value = id
  showCancelConfirmModal.value = true
}

const executeCancelWarranty = async () => {
  if (!currentCancelId.value) return
  try {
    await axios.put(`${API}/${currentCancelId.value}/cancel`)
    showCancelConfirmModal.value = false
    message.value = { type: "success", text: "ĐÃ HỦY YÊU CẦU BẢO HÀNH THÀNH CÔNG." }
    await fetchWarrantyRequests()
  } catch (err) {
    showCancelConfirmModal.value = false
    message.value = { type: "error", text: "ĐÃ XẢY RA LỖI KHI HỦY YÊU CẦU." }
  }
}

const getStatusText = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "ĐANG CHỜ XỬ LÝ"
    case "DA_DE_XUAT_LICH": return "ĐÃ ĐỀ XUẤT LỊCH HẸN"
    case "DA_TIEP_NHAN": return "ĐÃ XÁC NHẬN LỊCH"
    case "YEU_CAU_DOI_LICH": return "YÊU CẦU ĐỔI LỊCH"
    case "DANG_SUA_CHUA": return "ĐANG XỬ LÝ KỸ THUẬT"
    case "HOAN_TAT": return "HOÀN TẤT BẢO HÀNH"
    case "DA_HUY": return "ĐÃ HỦY YÊU CẦU"
    case "TU_CHOI": return "TRUNG TÂM TỪ CHỐI"
    default: return status
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "status-pending"
    case "DA_DE_XUAT_LICH": return "status-waiting"
    case "DA_TIEP_NHAN": return "status-accepted"
    case "YEU_CAU_DOI_LICH": return "status-reschedule"
    case "DANG_SUA_CHUA": return "status-processing"
    case "HOAN_TAT": return "status-completed"
    case "DA_HUY": return "status-cancelled"
    case "TU_CHOI": return "status-cancelled"
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
      
      // Tự động điền mã đơn vào ô tra cứu bảo hành để tiện kiểm tra
      lookupQuery.value = form.maDonHang

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

      // Tự động gọi xác thực mã đơn hàng
      if (form.maDonHang) {
        verifyOrderCode()
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
   VELORA VVIP CSS - LUXURY SQUARE DESIGN
   Không bo góc, tối giản, dứt khoát, mảng màu tương phản
========================================================================== */

/* 1. BIẾN MÀU SẮC & CƠ BẢN */
.bao-hanh-page {
  background-color: #fcfbf9;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1a1614;
}

.bao-hanh-main {
  max-width: 1280px;
  margin: 40px auto;
  padding: 0 20px;
  width: 100%;
  flex: 1;
}

/* 2. HERO BANNER */
.hero-banner {
  background-color: #1a1614;
  color: #ffffff;
  padding: 50px 60px;
  border-radius: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  border-bottom: 4px solid #cca15e;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

.hero-content h1 {
  font-size: 2.2rem;
  margin-bottom: 15px;
  font-weight: 700;
  color: #cca15e;
  letter-spacing: 4px;
  text-transform: uppercase;
}

.hero-content p {
  color: #d4d0c7;
  max-width: 600px;
  line-height: 1.8;
  font-size: 1rem;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.hero-badge {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid #cca15e;
  padding: 25px 35px;
  text-align: center;
  min-width: 280px;
  backdrop-filter: blur(5px);
}

.hero-badge strong {
  font-size: 1.1rem;
  color: #cca15e;
  letter-spacing: 2px;
  text-transform: uppercase;
  display: block;
  margin-bottom: 15px;
}

.badge-divider {
  width: 50px;
  height: 1px;
  background-color: #cca15e;
  margin: 0 auto 15px;
}

.hero-badge p {
  font-size: 0.85rem;
  color: #b5b0a5;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 3. BỐ CỤC GRID */
.warranty-container {
  display: grid;
  grid-template-columns: 5fr 4fr;
  gap: 30px;
  align-items: start;
}

.form-section {
  grid-column: 1 / 2;
  grid-row: 1 / 3;
}

.lookup-section {
  grid-column: 2 / 3;
  grid-row: 1 / 2;
}

.history-section {
  grid-column: 2 / 3;
  grid-row: 2 / 3;
}

/* 4. CARD BOX */
.card-box {
  background: #ffffff;
  padding: 40px;
  border-radius: 0;
  border: 1px solid #e5e3dd;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02);
  transition: border-color 0.3s ease;
}

.card-box:hover {
  border-color: #cca15e;
}

.card-header-luxury {
  margin-bottom: 35px;
}

.card-header-luxury h2 {
  font-size: 1.25rem;
  color: #1a1614;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 12px;
}

.title-line {
  width: 80px;
  height: 2px;
  background-color: #cca15e;
  margin-bottom: 15px;
}

.section-desc {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
  font-weight: 400;
}

/* 5. FORM & INPUTS */
.form-group {
  margin-bottom: 25px;
}

.bao-hanh-form label {
  display: block;
  font-weight: 600;
  font-size: 0.8rem;
  color: #1a1614;
  margin-bottom: 10px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.auto-fill-text,
.warning-text {
  font-weight: 600;
  letter-spacing: 0;
  margin-left: 5px;
}

.auto-fill-text {
  color: #cca15e;
}

.warning-text {
  color: #d97706;
}

.bao-hanh-form input,
.bao-hanh-form select,
.bao-hanh-form textarea,
.input-datetime-custom,
.bao-hanh-input {
  width: 100%;
  padding: 14px 18px;
  border: 1px solid #d4d0c7;
  border-radius: 0;
  font-size: 0.95rem;
  background-color: #fdfcfb;
  color: #1a1614;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.bao-hanh-form input:focus,
.bao-hanh-form select:focus,
.bao-hanh-form textarea:focus,
.bao-hanh-input:focus {
  outline: none;
  border-color: #cca15e;
  background-color: #ffffff;
  box-shadow: inset 0 0 0 1px #cca15e;
}

.disabled-input {
  background-color: #f3f2ef !important;
  color: #888 !important;
  border-color: #e5e3dd !important;
  cursor: not-allowed;
}

.input-bold {
  color: #1a1614 !important;
  font-weight: 700 !important;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* RADIO CARDS */
.delivery-methods {
  display: flex;
  gap: 15px;
}

.radio-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d4d0c7;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fdfcfb;
  text-align: center;
}

.radio-card:hover {
  border-color: #1a1614;
}

.radio-card.active {
  border-color: #cca15e;
  background: #fff;
  border-width: 2px;
  padding: 15px;
}

.radio-card .text {
  font-size: 0.85rem;
  font-weight: 700;
  color: #777;
  letter-spacing: 1px;
}

.radio-card.active .text {
  color: #cca15e;
}

/* 6. BUTTONS */
.btn-submit,
.btn-lookup {
  width: 100%;
  background: #1a1614;
  color: #ffffff;
  padding: 16px;
  border: 1px solid #1a1614;
  border-radius: 0;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-submit:hover,
.btn-lookup:hover {
  background: #cca15e;
  border-color: #cca15e;
  color: #1a1614;
}

/* 7. PHẦN TRA CỨU */
.lookup-form-group {
  display: flex;
  gap: 15px;
}

.lookup-custom-input {
  height: 100%;
}

.btn-lookup {
  width: 160px;
  padding: 0;
}

/* 8. LỊCH SỬ BẢO HÀNH */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 700px;
  overflow-y: auto;
  padding-right: 10px;
}

.history-list::-webkit-scrollbar { width: 6px; }
.history-list::-webkit-scrollbar-track { background: #f1f1f1; }
.history-list::-webkit-scrollbar-thumb { background: #d4d0c7; }
.history-list::-webkit-scrollbar-thumb:hover { background: #cca15e; }

.history-card {
  background: #ffffff;
  border: 1px solid #e5e3dd;
  padding: 25px;
  transition: transform 0.3s ease;
}

.history-card:hover {
  border-color: #cca15e;
  transform: translateX(5px);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0eee9;
}

.order-code {
  font-size: 0.9rem;
  color: #1a1614;
  font-weight: 700;
  letter-spacing: 1px;
}

.gold-text {
  color: #cca15e;
}

.status-badge {
  font-size: 0.7rem;
  padding: 6px 14px;
  font-weight: 700;
  letter-spacing: 1px;
  border: 1px solid transparent;
  text-transform: uppercase;
}

.status-pending { color: #8a5a19; border-color: #8a5a19; }
.status-waiting { color: #2c5282; border-color: #2c5282; }
.status-accepted { color: #276749; border-color: #276749; background: rgba(39, 103, 73, 0.05); }
.status-reschedule { color: #9b2c2c; border-color: #9b2c2c; }
.status-processing { color: #553c9a; border-color: #553c9a; }
.status-completed { color: #ffffff; border-color: #276749; background: #276749; }
.status-cancelled { color: #718096; border-color: #cbd5e0; background: #f7fafc; }

.info-row {
  font-size: 0.9rem;
  margin-bottom: 12px;
  line-height: 1.6;
}

.lbl {
  font-weight: 700;
  color: #888;
  margin-right: 8px;
  font-size: 0.75rem;
  letter-spacing: 1px;
}

.val {
  color: #1a1614;
  font-weight: 600;
}

.appointment-proposal-box {
  background: #fdfcfb;
  border: 1px solid #e5e3dd;
  border-left: 4px solid #cca15e;
  padding: 20px;
  margin: 20px 0;
}

.appointment-title {
  font-weight: 700;
  font-size: 0.8rem;
  color: #cca15e;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.appointment-time {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1614;
  margin-bottom: 15px;
  letter-spacing: 1px;
}

.appointment-actions {
  display: flex;
  gap: 12px;
}

.btn-confirm-schedule {
  background-color: #cca15e;
  color: #1a1614;
  border: 1px solid #cca15e;
  padding: 10px 20px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
}

.btn-confirm-schedule:hover {
  background-color: #1a1614;
  color: #cca15e;
  border-color: #1a1614;
}

.btn-reschedule {
  background-color: transparent;
  color: #1a1614;
  border: 1px solid #1a1614;
  padding: 10px 20px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
}

.btn-reschedule:hover {
  background-color: #1a1614;
  color: #ffffff;
}

.appointment-wait {
  background: #fdfcfb;
  border: 1px solid #cca15e;
  padding: 15px;
  margin-top: 15px;
}

.history-actions-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0eee9;
}

.history-footer-info {
  color: #999;
  font-size: 0.75rem;
  letter-spacing: 1px;
  font-weight: 600;
}

.btn-cancel {
  background: transparent;
  color: #9b2c2c;
  border: none;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
  text-decoration: underline;
}

.btn-cancel:hover {
  color: #1a1614;
}

.empty-history {
  text-align: center;
  padding: 60px 0;
  color: #888;
  font-size: 0.9rem;
  letter-spacing: 2px;
  font-weight: 600;
}

/* 9. MODALS DARK VVIP */
.velora-modal-overlay,
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(15, 12, 10, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(8px);
  animation: fadeInModal 0.3s ease-out;
}

.velora-modal-card,
.modal-content {
  background-color: #14110f;
  border: 1px solid #cca15e;
  padding: 50px 40px;
  width: 100%;
  max-width: 480px;
  text-align: center;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.5);
  animation: scaleUpModal 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  color: #fff;
}

.modal-content.card-box {
  background: #14110f;
  border-color: #cca15e;
}

.modal-content .card-header-luxury h2 {
  color: #cca15e;
}

.modal-content .section-desc {
  color: #d4d0c7;
}

.modal-icon-wrapper {
  width: 70px;
  height: 70px;
  border: 1px solid #cca15e;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 25px;
  background: rgba(204, 161, 94, 0.1);
}

.modal-icon-wrapper.success {
  border-color: #cca15e;
  color: #cca15e;
}

.modal-icon-wrapper.error {
  border-color: #e53e3e;
  color: #e53e3e;
  background: rgba(229, 62, 62, 0.1);
}

.icon-symbol {
  font-size: 2rem;
  font-weight: 300;
}

.modal-title {
  font-size: 1.3rem;
  letter-spacing: 3px;
  color: #cca15e;
  margin-bottom: 15px;
  text-transform: uppercase;
}

.modal-desc {
  color: #d4d0c7;
  font-size: 0.95rem;
  line-height: 1.6;
  font-weight: 300;
  margin-bottom: 30px;
}

.modal-btn-close,
.btn-confirm-modal,
.btn-cancel-modal {
  background: #cca15e;
  color: #14110f;
  border: 1px solid #cca15e;
  padding: 14px 25px;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  width: 100%;
  transition: all 0.3s ease;
  text-transform: uppercase;
}

.modal-btn-close:hover,
.btn-confirm-modal:hover {
  background: #ffffff;
  border-color: #ffffff;
}

.modal-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.btn-cancel-modal {
  background: transparent;
  color: #d4d0c7;
  border-color: #555;
}

.btn-cancel-modal:hover {
  border-color: #cca15e;
  color: #cca15e;
}

@keyframes fadeInModal { from { opacity: 0; } to { opacity: 1; } }
@keyframes scaleUpModal { from { transform: scale(0.95) translateY(20px); opacity: 0; } to { transform: scale(1) translateY(0); opacity: 1; } }

@media(max-width: 992px) {
  .warranty-container { grid-template-columns: 1fr; }
  .form-section { grid-column: 1; grid-row: 1; }
  .lookup-section { grid-column: 1; grid-row: 2; }
  .history-section { grid-column: 1; grid-row: 3; }
  .hero-banner { flex-direction: column; text-align: center; gap: 30px; padding: 40px 20px; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
  .lookup-form-group { flex-direction: column; }
  .btn-lookup { width: 100%; padding: 16px; }
  .delivery-methods { flex-direction: column; }
}

.btn-verify-order {
  background: #1a1614;
  color: #cca15e;
  border: 1px solid #cca15e;
  padding: 0 15px;
  font-weight: 700;
  font-size: 0.75rem;
  letter-spacing: 1px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.btn-verify-order:hover {
  background: #cca15e;
  color: #1a1614;
}
</style>