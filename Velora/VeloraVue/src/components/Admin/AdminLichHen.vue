<template>
  <div class="velora-admin-wrapper">
    <!-- 1. GỌI COMPONENT SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. GỌI COMPONENT HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH (CONTENT) -->
      <main class="content">
        <!-- HEADER CỦA TRANG -->
        <header class="page-header">
          <div class="header-left">
            <h1>Quản Lý <span class="gold-text">Lịch Hẹn</span></h1>
            <p class="subtitle">Theo dõi và phản hồi yêu cầu xem sản phẩm từ khách hàng.</p>
          </div>
        </header>

        <!-- BỘ LỌC TÌM KIẾM (Dạng Card trắng) -->
        <section class="card-panel search-panel filter-layout">
          <!-- Tìm kiếm Text -->
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input type="text" v-model="searchQuery" placeholder="Tìm khách hàng, SĐT..." />
          </div>

          <!-- Lọc theo Trạng thái -->
          <select v-model="filterStatus" class="filter-input">
            <option value="">Tất cả trạng thái</option>
            <option value="0">Chờ xác nhận</option>
            <option value="1">Đã xác nhận</option>
            <option value="2">Hoàn thành</option>
            <option value="3">Đã hủy</option>
          </select>

          <!-- Lọc theo Ngày -->
          <input type="date" v-model="filterDate" class="filter-input" title="Lọc theo ngày" />

          <!-- Lọc theo Giờ -->
          <select v-model="filterTime" class="filter-input" title="Lọc theo khung giờ">
            <option value="">Tất cả khung giờ</option>
            <option value="09:00 - 11:00">09:00 - 11:00</option>
            <option value="13:00 - 15:00">13:00 - 15:00</option>
            <option value="15:00 - 17:00">15:00 - 17:00</option>
          </select>

          <button class="btn-action reset-btn" @click="resetFilters" title="Làm mới bộ lọc">
            <i class="fa-solid fa-rotate-right"></i>
          </button>
        </section>

        <!-- BẢNG DỮ LIỆU (Dạng Card trắng) -->
        <section class="card-panel table-panel">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Mã</th>
                <th>Khách Hàng & SĐT</th>
                <th>Sản Phẩm Yêu Cầu</th>
                <th>Thời Gian Hẹn</th>
                <th>Trạng Thái</th>
                <th style="text-align: right;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in paginatedLichHen" :key="item.id">
                <td class="col-id">#{{ item.id }}</td>
                
                <td>
                  <div class="customer-info">
                    <span class="customer-name">{{ item.tenKhachHang || 'N/A' }}</span>
                    <span class="customer-phone">{{ item.soDienThoai || 'N/A' }}</span>
                  </div>
                </td>
                
                <!-- CỘT HIỂN THỊ SẢN PHẨM -->
                <td>
                  <div class="product-info">
                    <img 
                      v-if="item.sanPham?.hinhAnh || item.hinhAnhSanPham" 
                      :src="item.sanPham?.hinhAnh || item.hinhAnhSanPham" 
                      alt="Product" 
                      class="product-thumb"
                    />
                    <div v-else class="product-thumb-placeholder">
                      <i class="fa-solid fa-box"></i>
                    </div>
                    
                    <div class="product-details">
                      <span class="product-name">
                        {{ item.sanPham?.tenSanPham || item.tenSanPham || 'Không xác định' }}
                      </span>
                      <small v-if="item.sanPham?.gia || item.giaSanPham" class="product-price">
                        {{ (item.sanPham?.gia || item.giaSanPham).toLocaleString('vi-VN') }} đ
                      </small>
                    </div>
                  </div>
                </td>

                <td>
                  <strong class="date-text">{{ formatDate(item.ngayHen) }}</strong><br />
                  <small class="time-text">{{ item.thoiGian || '' }}</small>
                </td>
                
                <td>
                  <span :class="['badge', getStatusClass(item.trangThai)]">
                    {{ getStatusText(item.trangThai) }}
                  </span>
                </td>
                
                <td>
                  <div class="col-actions" style="justify-content: flex-end;">
                    <!-- Nút Xác Nhận -->
                    <button 
                      v-if="item.trangThai === 0" 
                      class="btn-action confirm" 
                      title="Xác nhận lịch hẹn" 
                      @click="quickUpdateStatus(item.id, 1)"
                    >
                      <i class="fa-solid fa-check"></i>
                    </button>

                    <!-- Nút Hoàn Thành -->
                    <button 
                      v-if="item.trangThai === 1" 
                      class="btn-action complete" 
                      title="Đã hoàn thành" 
                      @click="quickUpdateStatus(item.id, 2)"
                    >
                      <i class="fa-solid fa-check-double"></i>
                    </button>

                    <!-- Nút Từ Chối/Hủy -->
                    <button 
                      v-if="item.trangThai === 0 || item.trangThai === 1" 
                      class="btn-action delete" 
                      title="Từ chối lịch hẹn" 
                      @click="quickUpdateStatus(item.id, 3)"
                    >
                      <i class="fa-solid fa-xmark"></i>
                    </button>

                    <!-- Nút Chỉnh sửa chi tiết -->
                    <button class="btn-action edit" title="Chi tiết / Cập nhật" @click="openEditModal(item)">
                      <i class="fa-solid fa-pen"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredLichHen.length === 0">
                <td colspan="6" class="empty-state">
                  Không tìm thấy dữ liệu phù hợp với bộ lọc.
                </td>
              </tr>
            </tbody>
          </table>

          <!-- THANH PHÂN TRANG -->
          <div class="pagination-wrapper" v-if="totalPages > 1">
            <span class="pagination-info">
              Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - 
              {{ Math.min(currentPage * itemsPerPage, filteredLichHen.length) }} / {{ filteredLichHen.length }}
            </span>
            <div class="pagination-buttons">
              <button class="btn-page" @click="changePage(currentPage - 1)" :disabled="currentPage === 1">
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <button 
                v-for="page in totalPages" 
                :key="page" 
                :class="['btn-page', { active: currentPage === page }]"
                @click="changePage(page)"
              >
                {{ page }}
              </button>
              <button class="btn-page" @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- MODAL CẬP NHẬT TRẠNG THÁI -->
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal-box">
          <div class="modal-header">
            <h3>Cập Nhật Lịch Hẹn #{{ selectedLichHen.id }}</h3>
            <button class="close-btn" @click="showModal = false">&times;</button>
          </div>
          
          <form @submit.prevent="submitUpdateStatus">
            <div class="form-group">
              <label>Tên khách hàng</label>
              <input type="text" :value="selectedLichHen.tenKhachHang" disabled style="background: #f9f9f9;" />
            </div>
            
            <div class="form-group">
              <label>Ghi chú của khách hàng</label>
              <textarea rows="3" disabled style="background: #f9f9f9;">{{ selectedLichHen.ghiChu || 'Không có ghi chú' }}</textarea>
            </div>

            <div class="form-group">
              <label>Cập nhật trạng thái</label>
              <select v-model="selectedLichHen.trangThai" class="filter-input" style="width: 100%;">
                <option :value="0">Chờ xác nhận</option>
                <option :value="1">Đã xác nhận</option>
                <option :value="2">Hoàn thành</option>
                <option :value="3">Đã hủy</option>
              </select>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="showModal = false">Hủy Bỏ</button>
              <button type="submit" class="btn-submit">Lưu Thay Đổi</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import AdminSidebar from './AdminSidebar.vue'; 
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC GIAO DIỆN CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU LỊCH HẸN & BỘ LỌC =================
const danhSachLichHen = ref([]);
const searchQuery = ref('');
const filterStatus = ref('');
const filterDate = ref('');
const filterTime = ref('');

// ================= LOGIC PHÂN TRANG =================
const currentPage = ref(1);
const itemsPerPage = ref(10); // Hiển thị 10 dòng mỗi trang

const fetchLichHen = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/lich-hen/admin/danh-sach');
    if (response.ok) {
      const data = await response.json();
      if (Array.isArray(data)) {
        danhSachLichHen.value = data.sort((a, b) => a.id - b.id);
      } else {
        danhSachLichHen.value = [];
      }
    }
  } catch (error) {
    console.error("Lỗi khi lấy dữ liệu lịch hẹn:", error);
  }
};

onMounted(() => {
  fetchLichHen();
});

const resetFilters = () => {
  searchQuery.value = '';
  filterStatus.value = '';
  filterDate.value = '';
  filterTime.value = '';
  currentPage.value = 1;
  fetchLichHen();
};

const filteredLichHen = computed(() => {
  if (!Array.isArray(danhSachLichHen.value)) return [];
  
  return danhSachLichHen.value.filter(item => {
    const lowerQuery = searchQuery.value.toLowerCase();
    const ten = item.tenKhachHang ? item.tenKhachHang.toLowerCase() : '';
    const sdt = item.soDienThoai ? item.soDienThoai : '';
    const matchSearch = !searchQuery.value || ten.includes(lowerQuery) || sdt.includes(lowerQuery);

    const matchStatus = filterStatus.value === '' || item.trangThai === parseInt(filterStatus.value);

    let matchDate = true;
    if (filterDate.value) {
      let itemDateStr = '';
      if (Array.isArray(item.ngayHen) && item.ngayHen.length >= 3) {
        const year = item.ngayHen[0];
        const month = String(item.ngayHen[1]).padStart(2, '0');
        const day = String(item.ngayHen[2]).padStart(2, '0');
        itemDateStr = `${year}-${month}-${day}`;
      } else if (typeof item.ngayHen === 'string') {
        itemDateStr = item.ngayHen.split('T')[0];
      }
      matchDate = (itemDateStr === filterDate.value);
    }

    let matchTime = true;
    if (filterTime.value) {
      matchTime = item.thoiGian === filterTime.value;
    }

    return matchSearch && matchStatus && matchDate && matchTime;
  });
});

watch([searchQuery, filterStatus, filterDate, filterTime], () => {
  currentPage.value = 1;
});

const totalPages = computed(() => {
  return Math.ceil(filteredLichHen.value.length / itemsPerPage.value) || 1;
});

const paginatedLichHen = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredLichHen.value.slice(start, end);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// ================= LOGIC CẬP NHẬT NHANH =================
const quickUpdateStatus = async (id, newTrangThai) => {
  const confirmMessage = newTrangThai === 3 ? "Bạn có chắc chắn muốn TỪ CHỐI lịch hẹn này?" : "Bạn có chắc chắn muốn cập nhật trạng thái?";
  if (!confirm(confirmMessage)) return;

  try {
    const response = await fetch(`http://localhost:8080/api/lich-hen/admin/cap-nhat-trang-thai/${id}?trangThai=${newTrangThai}`, {
      method: 'PUT'
    });
    
    if (response.ok) {
      fetchLichHen();
    } else {
      alert('Cập nhật trạng thái thất bại từ Server.');
    }
  } catch (error) {
    console.error("Lỗi cập nhật nhanh:", error);
    alert('Không thể kết nối đến server.');
  }
};

// ================= LOGIC MODAL =================
const showModal = ref(false);
const selectedLichHen = ref({});

const openEditModal = (item) => {
  selectedLichHen.value = { ...item };
  showModal.value = true;
};

const submitUpdateStatus = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/lich-hen/admin/cap-nhat-trang-thai/${selectedLichHen.value.id}?trangThai=${selectedLichHen.value.trangThai}`, {
      method: 'PUT'
    });
    
    if (response.ok) {
      alert('Cập nhật trạng thái thành công!');
      showModal.value = false;
      fetchLichHen();
    } else {
      alert('Cập nhật thất bại từ Server.');
    }
  } catch (error) {
    console.error("Lỗi cập nhật:", error);
    alert('Không thể kết nối đến server.');
  }
};

// ================= TIỆN ÍCH =================
const formatDate = (dateData) => {
  if (!dateData) return 'Chưa xác định';
  if (Array.isArray(dateData) && dateData.length >= 3) {
    const year = dateData[0];
    const month = String(dateData[1]).padStart(2, '0');
    const day = String(dateData[2]).padStart(2, '0');
    return `${day}/${month}/${year}`;
  }
  if (typeof dateData === 'string') {
    const parts = dateData.split('-');
    if (parts.length >= 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }
  return dateData;
};

const getStatusText = (status) => {
  switch(status) {
    case 0: return 'Chờ xác nhận';
    case 1: return 'Đã xác nhận';
    case 2: return 'Hoàn thành';
    case 3: return 'Đã hủy';
    default: return 'Không rõ';
  }
};

const getStatusClass = (status) => {
  switch(status) {
    case 0: return 'badge-warning';
    case 1: return 'badge-info';
    case 2: return 'badge-success';
    case 3: return 'badge-danger';
    default: return '';
  }
};
</script>

<style scoped>
/* Xóa hoặc comment dòng import cũ nếu có xung đột */
/* @import "../CSS/Admin/AdminLichHen.css"; */

/* ==============================================
   CSS LAYOUT CHUNG & KẾT HỢP TỪ ẢNH THIẾT KẾ
   ============================================== */
.velora-admin-wrapper {
  --wood-dark: #362921;
  --gold-matte: #cca15e;
  --bg-page: #f8f6f0;
  --border-light: #eaeaea;
  --text-main: #333333;
  --text-muted: #888888;

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

.content {
  padding: 30px;
}

/* --- Page Header --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 26px;
  font-weight: bold;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
}

.gold-text {
  color: var(--gold-matte);
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* --- Cards (White Background Panels) --- */
.card-panel {
  background-color: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  margin-bottom: 20px;
}

/* --- Bộ Lọc (Filters) --- */
.filter-layout {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  padding: 8px 15px;
  background-color: #fafafa;
  flex: 1; /* Mở rộng tối đa */
  min-width: 250px;
}

.search-icon {
  color: #aaa;
  margin-right: 10px;
}

.search-box input {
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  font-size: 14px;
  color: var(--text-main);
}

.filter-input {
  border: 1px solid var(--border-light);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--text-main);
  background-color: #fff;
  outline: none;
}

.filter-input:focus {
  border-color: var(--gold-matte);
}

/* --- Table --- */
.table-panel {
  padding: 0; 
  overflow: hidden;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.admin-table th {
  padding: 16px 24px;
  font-size: 12px;
  text-transform: uppercase;
  color: var(--text-muted);
  font-weight: 700;
  border-bottom: 1px solid var(--border-light);
  background-color: #fff;
}

.admin-table td {
  padding: 16px 24px;
  font-size: 14px;
  color: var(--text-main);
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
}

.col-id {
  font-weight: 500;
  color: var(--text-muted);
}

/* Cột Thông tin khách */
.customer-info {
  display: flex;
  flex-direction: column;
}
.customer-name {
  font-weight: 600;
  color: var(--wood-dark);
}
.customer-phone {
  font-size: 12px;
  color: var(--text-muted);
}

/* Cột Sản phẩm */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-thumb {
  width: 45px; 
  height: 45px; 
  object-fit: cover; 
  border-radius: 6px; 
  border: 1px solid var(--border-light);
}
.product-thumb-placeholder {
  width: 45px; 
  height: 45px; 
  background: #f3f4f6; 
  border-radius: 6px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  color: #9ca3af;
}
.product-details {
  display: flex;
  flex-direction: column;
}
.product-name {
  font-weight: 600;
  color: var(--text-main);
  font-size: 13px;
}
.product-price {
  color: var(--gold-matte);
  font-weight: 500;
}

/* Cột Thời gian */
.date-text { color: var(--wood-dark); }
.time-text { color: var(--text-muted); }

/* --- Badges Trạng thái --- */
.badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}
.badge-warning { background-color: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.badge-info { background-color: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.badge-success { background-color: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.badge-danger { background-color: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

/* --- Action Buttons --- */
.col-actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-action.reset-btn { background-color: #f0f0f0; color: #555; }
.btn-action.reset-btn:hover { background-color: #e4e4e4; color: #333; }

.btn-action.edit { background-color: #f0f0f0; color: #555; }
.btn-action.edit:hover { background-color: #e4e4e4; color: #333; }

.btn-action.delete { background-color: #ffeef0; color: #e74c3c; }
.btn-action.delete:hover { background-color: #ffdce0; color: #c0392b; }

.btn-action.confirm { background-color: #f6ffed; color: #52c41a; }
.btn-action.confirm:hover { background-color: #d9f7be; }

.btn-action.complete { background-color: #e6f7ff; color: #1890ff; }
.btn-action.complete:hover { background-color: #bae0ff; }

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

/* --- Pagination --- */
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 24px;
  border-top: 1px solid var(--border-light);
}

.pagination-info {
  font-size: 13px;
  color: var(--text-muted);
}

.pagination-buttons {
  display: flex;
  gap: 6px;
}

.btn-page {
  padding: 6px 12px;
  border: 1px solid var(--border-light);
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-main);
  font-size: 13px;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  background-color: #f5f5f5;
}

.btn-page.active {
  background-color: var(--gold-matte);
  color: #fff;
  border-color: var(--gold-matte);
  font-weight: bold;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* --- Modal Ghi đè nhanh (dùng chung phong cách card) --- */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff; border-radius: 8px; width: 450px; max-width: 90%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden;
}
.modal-header {
  padding: 20px; border-bottom: 1px solid var(--border-light);
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h3 { margin: 0; font-size: 18px; color: var(--wood-dark); }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: var(--text-muted); }
.form-group { padding: 15px 20px 0; display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; font-weight: 600; color: var(--text-main); }
.form-group input, .form-group textarea {
  border: 1px solid var(--border-light); border-radius: 6px; padding: 10px; font-family: inherit; font-size: 14px;
}
.modal-actions {
  padding: 20px; display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;
}
.btn-cancel {
  background: #f0f0f0; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; color: var(--text-main);
}
.btn-submit {
  background: var(--gold-matte); border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; color: #fff; font-weight: bold;
}
</style>