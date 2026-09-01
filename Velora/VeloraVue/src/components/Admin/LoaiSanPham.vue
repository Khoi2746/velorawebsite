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
            <h1>Quản Lý <span class="gold-text">Loại Sản Phẩm</span></h1>
            <p class="subtitle">Danh sách các phân loại đặc tính cơ khí của đồng hồ.</p>
          </div>
          <div class="header-right">
            <button class="btn-add" @click="openAddModal">
              <i class="fa-solid fa-plus"></i> Thêm Loại Mới
            </button>
          </div>
        </header>

        <!-- BỘ LỌC TÌM KIẾM (Dạng Card trắng) -->
        <section class="card-panel search-panel">
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên loại sản phẩm..." />
          </div>
        </section>

        <!-- BẢNG DỮ LIỆU (Dạng Card trắng) -->
        <section class="card-panel table-panel">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Mã Loại</th>
                <th>Tên Loại Sản Phẩm</th>
                <th>Mô Tả Chi Tiết</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="category in paginatedCategories" :key="category.maLoai">
                <td class="col-id">#{{ category.maLoai }}</td>
                <td class="col-name">{{ category.tenLoai }}</td>
                <td class="col-desc">{{ category.moTa || 'Chưa có mô tả' }}</td>
                <td class="col-actions">
                  <button class="btn-action edit" @click="openEditModal(category)" title="Chỉnh sửa">
                    <i class="fa-solid fa-pen"></i>
                  </button>
                  <button class="btn-action delete" @click="confirmDeleteCategory(category.maLoai)" title="Xóa">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="paginatedCategories.length === 0">
                <td colspan="4" class="empty-state">
                  Không tìm thấy loại sản phẩm nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>

          <!-- PHÂN TRANG -->
          <div class="pagination-wrapper" v-if="totalPages > 1">
            <button class="btn-page" @click="prevPage" :disabled="currentPage === 1">Trước</button>
            <span class="page-info">Trang <strong>{{ currentPage }}</strong> / {{ totalPages }}</span>
            <button class="btn-page" @click="nextPage" :disabled="currentPage === totalPages">Sau</button>
          </div>
        </section>
      </main>

      <!-- MODAL THÊM / SỬA -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-box">
          <div class="modal-header">
            <h3>{{ isEditMode ? 'Cập Nhật Loại Sản Phẩm' : 'Thêm Loại Sản Phẩm Mới' }}</h3>
            <button class="close-btn" @click="closeModal">&times;</button>
          </div>
          <form @submit.prevent="saveCategory">
            <div class="form-group">
              <label>Tên loại sản phẩm *</label>
              <input type="text" v-model="form.tenLoai" required placeholder="Ví dụ: Lộ máy (Skeleton), Chronograph..." />
            </div>

            <div class="form-group">
              <label>Mô tả đặc tính</label>
              <textarea v-model="form.moTa" rows="4" placeholder="Nhập mô tả chi tiết cho loại đặc tính này..."></textarea>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="closeModal">Hủy bỏ</button>
              <button type="submit" class="btn-submit">Lưu lại</button>
            </div>
          </form>
        </div>
      </div>

      <!-- POPUP THÔNG BÁO VVIP DARK MODE -->
      <div v-if="messageModal.show" class="velora-modal-overlay" @click.self="messageModal.show = false">
        <div class="velora-modal-card">
          <div class="modal-icon-wrapper" :class="messageModal.type">
            <span class="icon-symbol">{{ messageModal.type === 'success' ? '✓' : '✕' }}</span>
          </div>
          <h3 class="modal-title">{{ messageModal.type === 'success' ? 'THÀNH CÔNG' : 'THÔNG BÁO LỖI' }}</h3>
          <p class="modal-desc">{{ messageModal.text }}</p>
          <button class="modal-btn-close" @click="messageModal.show = false">ĐÓNG</button>
        </div>
      </div>

      <!-- MODAL XÁC NHẬN XÓA VVIP DARK MODE -->
      <div v-if="showDeleteConfirmModal" class="velora-modal-overlay" @click.self="showDeleteConfirmModal = false">
        <div class="velora-modal-card">
          <div class="modal-icon-wrapper error">
            <span class="icon-symbol">✕</span>
          </div>
          <h3 class="modal-title">XÁC NHẬN XÓA</h3>
          <p class="modal-desc">BẠN CÓ CHẮC CHẮN MUỐN XÓA LOẠI SẢN PHẨM #{{ categoryToDeleteId }} KHÔNG? THAO TÁC NÀY SẼ ẢNH HƯỞNG ĐẾN DỮ LIỆU LIÊN KẾT!</p>
          <div style="display: flex; gap: 10px; justify-content: center;">
            <button class="modal-btn-close" style="background: transparent; border: 1px solid #cca15e; color: #cca15e;" @click="showDeleteConfirmModal = false">GIỮ LẠI</button>
            <button class="modal-btn-close" style="background: #d9534f; color: white;" @click="executeDeleteCategory">XÁC NHẬN XÓA</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';

// IMPORT COMPONENT CON VÀO ĐÂY 
import AdminSidebar from './AdminSidebar.vue'; 
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC ĐIỀU KHIỂN LAYOUT CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU LOẠI SẢN PHẨM =================
const CAT_API_URL = 'http://localhost:8080/api/loai-san-pham'; 

const categories = ref([]);
const searchQuery = ref('');
const showModal = ref(false);
const isEditMode = ref(false);
const currentCategoryId = ref(null);

// Biến quản lý popup thông báo VVIP
const messageModal = ref({
    show: false,
    type: 'success', 
    text: ''
});

const showPopup = (text, type = 'success') => {
    messageModal.value = { show: true, type, text };
};

// Biến quản lý modal xác nhận xóa VVIP
const showDeleteConfirmModal = ref(false);
const categoryToDeleteId = ref(null);

const defaultForm = {
  tenLoai: '',
  moTa: ''
};
const form = ref({ ...defaultForm });

// --- CẤU HÌNH PHÂN TRANG ---
const currentPage = ref(1);
const itemsPerPage = ref(5);

// Tính toán tìm kiếm thời gian thực
const filteredCategories = computed(() => {
  return categories.value.filter(cat => {
    return cat.tenLoai.toLowerCase().includes(searchQuery.value.toLowerCase());
  });
});

const totalPages = computed(() => {
  return Math.ceil(filteredCategories.value.length / itemsPerPage.value) || 1;
});

const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredCategories.value.slice(start, end);
});

watch(searchQuery, () => {
  currentPage.value = 1;
});

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};

const loadCategories = async () => {
  try {
    const res = await fetch(CAT_API_URL);
    if (res.ok) {
      categories.value = await res.json();
    }
  } catch (error) {
    console.error('Lỗi kết nối API danh sách loại:', error);
  }
};

const openAddModal = () => {
  isEditMode.value = false;
  currentCategoryId.value = null;
  form.value = { ...defaultForm };
  showModal.value = true;
};

const openEditModal = (category) => {
  isEditMode.value = true;
  currentCategoryId.value = category.maLoai;
  form.value = {
    tenLoai: category.tenLoai,
    moTa: category.moTa
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const saveCategory = async () => {
  try {
    let url = CAT_API_URL;
    let method = 'POST';

    const dataToSend = {
      tenLoai: form.value.tenLoai,
      moTa: form.value.moTa
    };

    if (isEditMode.value) {
      url = `${CAT_API_URL}/${currentCategoryId.value}`;
      method = 'PUT';
    }

    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dataToSend)
    });

    if (res.ok) {
      showPopup(isEditMode.value ? 'CẬP NHẬT LOẠI SẢN PHẨM THÀNH CÔNG!' : 'THÊM LOẠI SẢN PHẨM MỚI THÀNH CÔNG!', 'success');
      closeModal();
      loadCategories();
    } else {
      const errorText = await res.text();
      let displayMsg = errorText || 'Không thể lưu, vui lòng kiểm tra lại dữ liệu.';
      if (errorText.includes('Violation of UNIQUE KEY constraint') || errorText.includes('duplicate key')) {
        displayMsg = 'TÊN LOẠI SẢN PHẨM ĐÃ TỒN TẠI TRONG HỆ THỐNG!';
      }
      showPopup(displayMsg, 'error');
    }
  } catch (error) {
    console.error('Lỗi khi gửi dữ liệu loại sản phẩm:', error);
    showPopup('ĐÃ XẢY RA LỖI KẾT NỐI HỆ THỐNG.', 'error');
  }
};

const confirmDeleteCategory = (id) => {
  categoryToDeleteId.value = id;
  showDeleteConfirmModal.value = true;
};

const executeDeleteCategory = async () => {
  if (!categoryToDeleteId.value) return;
  try {
    const res = await fetch(`${CAT_API_URL}/${categoryToDeleteId.value}`, {
      method: 'DELETE'
    });
    showDeleteConfirmModal.value = false;
    if (res.ok) {
      showPopup('XÓA LOẠI SẢN PHẨM THÀNH CÔNG!', 'success');
      if (paginatedCategories.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }
      loadCategories();
    } else {
      showPopup('XÓA THẤT BẠI! LOẠI SẢN PHẨM NÀY CÓ THỂ ĐANG ĐƯỢC SỬ DỤNG Ở BẢNG SẢN PHẨM.', 'error');
    }
  } catch (error) {
    showDeleteConfirmModal.value = false;
    console.error('Lỗi xóa loại sản phẩm:', error);
    showPopup('ĐÃ XẢY RA LỖI KẾT NỐI KHI XÓA.', 'error');
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
/* --- Modal Override (If needed) --- */
@import "../CSS/Admin/LoaiSanPham.css";

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

.btn-add {
  background-color: var(--gold-matte);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: opacity 0.2s;
}

.btn-add:hover {
  opacity: 0.9;
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

/* --- Search Box --- */
.search-panel {
  padding: 15px 20px;
}

.search-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  padding: 8px 15px;
  background-color: #fafafa;
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
  padding: 20px 24px;
  font-size: 14px;
  color: var(--text-main);
  border-bottom: 1px solid var(--border-light);
}

.admin-table tbody tr:last-child td {
  border-bottom: none;
}

.col-id {
  font-weight: 500;
  color: var(--text-muted);
  width: 10%;
}

.col-name {
  font-weight: 600;
  color: var(--wood-dark);
  width: 30%;
}

.col-desc {
  color: #666;
  width: 45%;
}

/* --- Action Buttons --- */
.col-actions {
  display: flex;
  gap: 10px;
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

.btn-action.edit {
  background-color: #f0f0f0;
  color: #555;
}

.btn-action.edit:hover {
  background-color: #e4e4e4;
  color: #333;
}

.btn-action.delete {
  background-color: #ffeef0;
  color: #e74c3c;
}

.btn-action.delete:hover {
  background-color: #ffdce0;
  color: #c0392b;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

/* --- Pagination --- */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-top: 1px solid var(--border-light);
}

.btn-page {
  padding: 6px 12px;
  border: 1px solid var(--border-light);
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ==============================================
   MODAL NHẬP LIỆU CHÍNH
   ============================================== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: #fff;
  border-radius: 8px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--wood-dark);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
}

.form-group {
  padding: 15px 20px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
}

.form-group input, 
.form-group textarea {
  border: 1px solid var(--border-light);
  border-radius: 6px;
  padding: 10px;
  font-family: inherit;
  font-size: 14px;
}

.form-group input:focus, 
.form-group textarea:focus {
  outline: none;
  border-color: var(--gold-matte);
}

.modal-actions {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.btn-cancel {
  background: #f0f0f0;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-main);
}

.btn-cancel:hover {
  background: #e4e4e4;
}

.btn-submit {
  background: var(--gold-matte);
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: #fff;
  font-weight: bold;
}

.btn-submit:hover {
  opacity: 0.9;
}

/* ==============================================
   CSS POPUP VVIP DARK MODE (THÔNG BÁO & XÁC NHẬN XÓA)
   ============================================== */
.velora-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.75);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    backdrop-filter: blur(4px);
}

.velora-modal-card {
    background: #231c18;
    border: 1px solid #cca15e;
    width: 420px;
    max-width: 90%;
    padding: 35px 25px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    color: #f8f6f0;
}

.modal-icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    margin: 0 auto 20px auto;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    font-weight: bold;
}

.modal-icon-wrapper.success {
    background: rgba(204, 161, 94, 0.15);
    border: 2px solid #cca15e;
    color: #cca15e;
}

.modal-icon-wrapper.error {
    background: rgba(217, 83, 79, 0.15);
    border: 2px solid #d9534f;
    color: #d9534f;
}

.modal-title {
    font-size: 18px;
    letter-spacing: 1.5px;
    color: #cca15e;
    margin-bottom: 12px;
    font-weight: 700;
}

.modal-desc {
    font-size: 14px;
    color: #dcd6cd;
    line-height: 1.6;
    margin-bottom: 25px;
    word-break: break-word;
}

.modal-btn-close {
    background: #cca15e;
    color: #1a1412;
    border: none;
    padding: 12px 35px;
    font-weight: bold;
    border-radius: 4px;
    cursor: pointer;
    letter-spacing: 1px;
    transition: all 0.3s ease;
}

.modal-btn-close:hover {
    background: #dfb775;
    box-shadow: 0 0 10px rgba(204, 161, 94, 0.4);
}
</style>