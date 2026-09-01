<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- Sidebar -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- Header -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- Nội dung chính -->
      <main class="content">
        <!-- Tiêu đề trang -->
        <header class="header">
          <div class="header-left">
            <h1>Quản Lý <span class="gold">Bảo Hành</span></h1>
            <p>Danh sách yêu cầu bảo hành và điều phối lịch hẹn trực tiếp với khách hàng.</p>
          </div>
        </header>

        <!-- Modal thông báo chuẩn phong cách Velora Dark -->
<div v-if="message" class="velora-modal-overlay" @click.self="message = null">
  <div class="velora-modal-card">
    <!-- Icon trạng thái -->
    <div class="modal-icon-wrapper" :class="message.type">
      <span v-if="message.type === 'success'" class="icon-check">✓</span>
      <span v-else class="icon-error">✕</span>
    </div>

    <!-- Tiêu đề & Nội dung -->
    <h3 class="modal-title">{{ message.type === 'success' ? 'THÀNH CÔNG' : 'LỖI' }}</h3>
    <p class="modal-desc">{{ message.text }}</p>

    <!-- Nút đóng -->
    <button class="modal-btn-close" @click="message = null">ĐÓNG</button>
  </div>
</div>

        <section class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Mã yêu cầu</th>
                <th>Mã đơn hàng</th>
                <th>Khách hàng</th>
                <th>Sản phẩm & Lỗi</th>
                <th>Yêu cầu đổi lịch</th>
                <th>Ngày gửi</th>
                <th>Trạng thái</th>
                <th>Hành động & Lịch hẹn</th>
              </tr>
            </thead>
            <tbody>
  <tr v-for="item in paginatedRequests" :key="item.maBaoHanh">
    <td>#{{ item.maBaoHanh }}</td>
    <td>{{ item.maDonHangCode || 'Chưa có' }}</td>
    <td>
      <strong>{{ item.hoTen || 'Khách lẻ' }}</strong><br/>
      <small class="text-muted">{{ item.soDienThoai || 'Không có SĐT' }}</small>
    </td>
    <td>
      <span class="product-badge">{{ item.loaiSanPham || 'Chưa rõ' }}</span>
      <p class="error-description">{{ item.moTaLoi || 'Không có mô tả' }}</p>
    </td>

    <!-- 🌟 Thêm nội dung hiển thị cho cột mới -->
    <td>
  <div v-if="item.thoiGianKhachMongMuon" class="customer-note-box">
    <span class="text-orange" style="font-weight: 600; font-size: 0.85rem;">
      🕒 {{ formatDisplayTime(item.thoiGianKhachMongMuon) }}
    </span>
  </div>
  <span v-else class="text-muted" style="font-size: 0.85rem;">Không có</span>
</td>

    <td>{{ formatDate(item.ngayGui) }}</td>
    <td>
  <span
    class="status-badge"
    :class="{
      pending: item.trangThai === 'CHO_XU_LY',
      proposed: item.trangThai === 'DA_DE_XUAT_LICH',
      received: item.trangThai === 'DA_TIEP_NHAN',
      reschedule: item.trangThai === 'YEU_CAU_DOI_LICH',
      processing: item.trangThai === 'DANG_SUA_CHUA',
      completed: item.trangThai === 'HOAN_TAT',
      cancelled: item.trangThai === 'DA_HUY',
      rejected: item.trangThai === 'TU_CHOI'
    }"
  >
    {{ getStatusText(item.trangThai) }}
  </span>
</td>
<td>
  <div class="action-cell">
    <div class="datetime-wrapper" v-if="canProposeSchedule(item.trangThai)">
      <label class="input-label">Lịch hẹn đề xuất:</label>
      <input type="datetime-local" v-model="item.thoiGianHenInput" class="input-datetime" />
    </div>

    <select v-model="item.trangThai" class="status-select">
      <option :value="item.trangThai" disabled>{{ getStatusText(item.trangThai) }} (hiện tại)</option>
      <option v-for="opt in nextOptions(item.trangThai)" :key="opt" :value="opt">
        {{ getStatusText(opt) }}
      </option>
    </select>

    <button 
  class="btn-confirm" 
  @click="updateStatus(item)" 
  :disabled="submittingIds.has(item.maBaoHanh) || item.trangThai === item.originalTrangThai"
>
  {{ submittingIds.has(item.maBaoHanh) ? 'Đang xử lý...' : 'Cập nhật & Gửi Email' }}
</button>
  </div>
</td>
  </tr>
              <tr v-if="warrantyRequests.length === 0">
                <td colspan="7" class="empty-state">Không có yêu cầu bảo hành nào trong hệ thống.</td>
              </tr>
            </tbody>
          </table>

          <!-- Phân trang -->
          <div class="pagination-bar" v-if="totalPages > 1">
            <button class="btn-page" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">Trước</button>

            <template v-for="page in visiblePages" :key="page.value + page.type">
              <span v-if="page.type === 'ellipsis'" class="page-ellipsis">...</span>
              <button
                v-else
                class="btn-page-number"
                :class="{ active: currentPage === page.value }"
                @click="changePage(page.value)"
              >
                {{ page.value }}
              </button>
            </template>

            <button class="btn-page" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">Sau</button>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'

import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// Sửa lại đường dẫn API cho đúng với Controller Admin
// Đổi lại thành endpoint cũ đang hoạt động
const API = "http://localhost:8080/api/bao-hanh"

const warrantyRequests = ref([])
const loading = ref(false)
const message = ref(null)

const currentPage = ref(1)
const itemsPerPage = ref(5)
// Thêm vào trong script setup
const submittingIds = ref(new Set())
const totalPages = computed(() => {
  return Math.max(Math.ceil(warrantyRequests.value.length / itemsPerPage.value), 1)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5

  if (totalPages.value <= maxVisible) {
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push({ type: 'page', value: i })
    }
    return pages
  }

  if (currentPage.value <= 3) {
    for (let i = 1; i <= 4; i++) {
      pages.push({ type: 'page', value: i })
    }
    pages.push({ type: 'ellipsis', value: '...' })
    pages.push({ type: 'page', value: totalPages.value })
    return pages
  }

  if (currentPage.value >= totalPages.value - 2) {
    pages.push({ type: 'page', value: 1 })
    pages.push({ type: 'ellipsis', value: '...' })
    for (let i = totalPages.value - 3; i <= totalPages.value; i++) {
      pages.push({ type: 'page', value: i })
    }
    return pages
  }

  pages.push({ type: 'page', value: 1 })
  pages.push({ type: 'ellipsis', value: '...' })
  pages.push({ type: 'page', value: currentPage.value - 1 })
  pages.push({ type: 'page', value: currentPage.value })
  pages.push({ type: 'page', value: currentPage.value + 1 })
  pages.push({ type: 'ellipsis', value: '...' })
  pages.push({ type: 'page', value: totalPages.value })
  return pages
})

const paginatedRequests = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return warrantyRequests.value.slice(start, end)
})

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

watch([warrantyRequests, totalPages], () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})

const formatDate = (date) => {
  if (!date) return "---"
  return new Date(date).toLocaleString("vi-VN")
}

// Hàm định dạng ngày giờ khách muốn đổi
const formatDisplayTime = (val) => {
  if (!val) return ''
  try {
    const d = new Date(val)
    if (!isNaN(d.getTime())) {
      return d.toLocaleString('vi-VN')
    }
  } catch (e) {}
  return val
}

const fetchWarrantyRequests = async () => {
  loading.value = true
  try {
    const response = await axios.get(API)
    const data = response.data
    
warrantyRequests.value = data.map(item => ({
  ...item,
  originalTrangThai: item.trangThai,
  thoiGianHenInput: item.thoiGianHen ? item.thoiGianHen.substring(0, 16) : ''
}))
  } catch (e) {
    console.error(e)
    message.value = {
      type: "error",
      text: "Không tải được danh sách bảo hành từ máy chủ."
    }
  }
  loading.value = false
}

const updateStatus = async (item) => {
  if (item.trangThai === 'DA_DE_XUAT_LICH' && !item.thoiGianHenInput) {
    message.value = { type: "error", text: "Vui lòng chọn thời gian hẹn trước khi đề xuất lịch." }
    return
  }
  
  submittingIds.value.add(item.maBaoHanh)
  try {
    const payload = {
      trangThai: item.trangThai,
      thoiGianHen: item.thoiGianHenInput || null
    }
    await axios.put(`http://localhost:8080/api/bao-hanh/${item.maBaoHanh}/status`, payload)
    message.value = { type: "success", text: `Đã cập nhật đơn #${item.maBaoHanh} và gửi thông báo thành công!` }
    await fetchWarrantyRequests()
  } catch (err) {
    const msg = err.response?.data?.message || "Cập nhật thất bại, vui lòng thử lại."
    message.value = { type: "error", text: msg }
  } finally {
    submittingIds.value.delete(item.maBaoHanh)
  }
}

const ADMIN_TRANSITIONS = {
  CHO_XU_LY: ['DA_DE_XUAT_LICH', 'TU_CHOI'],
  DA_DE_XUAT_LICH: ['TU_CHOI'],
  YEU_CAU_DOI_LICH: ['DA_DE_XUAT_LICH', 'TU_CHOI'],
  DA_TIEP_NHAN: ['DANG_SUA_CHUA'],
  DANG_SUA_CHUA: ['HOAN_TAT'],
  HOAN_TAT: [],
  DA_HUY: [],
  TU_CHOI: []
}

const nextOptions = (current) => ADMIN_TRANSITIONS[current] || []

const canProposeSchedule = (current) =>
  ['CHO_XU_LY', 'YEU_CAU_DOI_LICH'].includes(current)

const getStatusText = (status) => {
  switch (status) {
    case "CHO_XU_LY": return "Chờ xử lý"
    case "DA_DE_XUAT_LICH": return "Đã đề xuất lịch"
    case "DA_TIEP_NHAN": return "Khách đã xác nhận"
    case "YEU_CAU_DOI_LICH": return "Yêu cầu đổi lịch"
    case "DANG_SUA_CHUA": return "Đang xử lý kỹ thuật"
    case "HOAN_TAT": return "Hoàn tất"
    case "DA_HUY": return "Khách đã hủy"
    case "TU_CHOI": return "Đã từ chối"
    default: return status
  }
}

onMounted(() => {
  fetchWarrantyRequests()
})
</script>

<style>
.status-badge {
  display: inline-block;
  padding: 5px 12px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  border-radius: 20px;
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-badge.pending {
  color: #cca15e;
  border-color: #cca15e;
  background: rgba(204, 161, 94, 0.1);
}

.status-badge.proposed {
  color: #63b3ed;
  border-color: #63b3ed;
  background: rgba(99, 179, 237, 0.1);
}

.status-badge.received {
  color: #48bb78;
  border-color: #48bb78;
  background: rgba(72, 187, 120, 0.1);
}

.status-badge.reschedule {
  color: #f6ad55;
  border-color: #f6ad55;
  background: rgba(246, 173, 85, 0.1);
}

.status-badge.processing {
  color: #b794f4;
  border-color: #b794f4;
  background: rgba(183, 148, 244, 0.1);
}

.status-badge.completed {
  color: #1a1614;
  border-color: #48bb78;
  background: #48bb78;
}

.status-badge.cancelled {
  color: #a0aec0;
  border-color: #a0aec0;
  background: rgba(160, 174, 192, 0.08);
}

.status-badge.rejected {
  color: #fc8181;
  border-color: #fc8181;
  background: rgba(252, 129, 129, 0.1);
}
:root {
  --wood-dark: #362921;
  --wood-active: #47372c;
  --wood-medium: #544438;
  --wood-light: #7a6352;
  --gold-matte: #cca15e;
  --bg-page: #f8f6f0;
  --border-light: #eaeaea;
  --text-main: #333333;
  --text-muted: #888888;
}
</style>

<style scoped>
@import '../CSS/Admin/QuanLyBaoHanh.css';

.velora-admin-wrapper {
  display: flex;
  height: 100vh;
  background-color: var(--bg-page);
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.content-wrapper {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.header-left h1 {
  font-size: 26px;
  font-weight: bold;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
}
.header-left .gold {
  color: var(--gold-matte);
}
.header-left p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.action-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.datetime-wrapper {
  display: flex;
  flex-direction: column;
}

.input-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 2px;
}

.input-datetime {
  font-size: 11px;
  padding: 4px;
  border: 1px solid var(--border-light);
  border-radius: 4px;
}

.error-description {
  font-size: 12px;
  color: var(--text-muted);
  margin: 4px 0 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* --- CSS THÔNG BÁO NỔI GIỮA MÀN HÌNH --- */
/* --- VELORA DARK MODAL POPUP --- */
.velora-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.65); /* Phủ mờ nền tối */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease-in-out;
}

.velora-modal-card {
  background-color: #1a1614; /* Nền tối sang trọng */
  border: 1px solid #cca15e; /* Viền màu vàng gold đặc trưng */
  border-radius: 14px;
  padding: 35px 40px;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
  animation: scaleUp 0.2s ease-in-out;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Khung icon tròn */
.modal-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.modal-icon-wrapper.success {
  background-color: #2ebd59; /* Xanh lá đặc trưng */
  color: #fff;
}

.modal-icon-wrapper.error {
  background-color: #e74c3c; /* Đỏ báo lỗi */
  color: #fff;
}

.icon-check, .icon-error {
  font-size: 2rem;
  font-weight: bold;
}

/* Chữ tiêu đề */
.modal-title {
  color: #cca15e; /* Màu vàng gold */
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 10px;
  text-transform: uppercase;
}

/* Mô tả ngắn */
.modal-desc {
  color: #e0d8cc;
  font-size: 0.95rem;
  margin-bottom: 25px;
  line-height: 1.5;
}

/* Nút Đóng */
.modal-btn-close {
  background-color: #cca15e;
  color: #1a1614;
  border: none;
  border-radius: 8px;
  width: 100%;
  padding: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  letter-spacing: 0.5px;
  transition: background-color 0.2s, transform 0.1s;
}

.modal-btn-close:hover {
  background-color: #b88f4e;
}

.modal-btn-close:active {
  transform: scale(0.98);
}

/* Hiệu ứng chuyển động */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
