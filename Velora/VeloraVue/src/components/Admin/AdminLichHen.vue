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

        <!-- BỘ LỌC TÌM KIẾM -->
        <section class="card-panel search-panel filter-layout">
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input type="text" v-model="searchQuery" placeholder="Tìm khách hàng, SĐT..." />
          </div>

          <select v-model="filterStatus" class="filter-input">
            <option value="">Tất cả trạng thái</option>
            <option value="0">Chờ xác nhận</option>
            <option value="1">Đã xác nhận</option>
            <option value="2">Hoàn thành</option>
            <option value="3">Đã hủy</option>
          </select>

          <input type="date" v-model="filterDate" class="filter-input" title="Lọc theo ngày" />

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

        <!-- BẢNG DỮ LIỆU -->
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

                <td>
                  <div class="product-info">
                    <img v-if="item.sanPham?.hinhAnh || item.hinhAnhSanPham"
                      :src="item.sanPham?.hinhAnh || item.hinhAnhSanPham" alt="Product" class="product-thumb" />
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
                    <button v-if="item.trangThai === 0" class="btn-action confirm" title="Xác nhận lịch hẹn"
                      @click="quickUpdateStatus(item.id, 1)">
                      <i class="fa-solid fa-check"></i>
                    </button>

                    <!-- Nút Hoàn Thành -->
                    <button v-if="item.trangThai === 1" class="btn-action complete" title="Đã hoàn thành"
                      @click="quickUpdateStatus(item.id, 2)">
                      <i class="fa-solid fa-check-double"></i>
                    </button>

                    <!-- Nút Từ Chối/Hủy (ĐÃ ĐỔI SANG MỞ MODAL LÝ DO HỦY) -->
                    <button v-if="item.trangThai === 0 || item.trangThai === 1" class="btn-action delete"
                      title="Hủy lịch hẹn & Gửi Email" @click="openCancelModal(item)">
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
              <button v-for="page in totalPages" :key="page" :class="['btn-page', { active: currentPage === page }]"
                @click="changePage(page)">
                {{ page }}
              </button>
              <button class="btn-page" @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- MODAL 1: CẬP NHẬT TRẠNG THÁI CHUNG -->
      <div v-if="showModal" class="confirm-modal-overlay" @click.self="showModal = false">
        <div class="confirm-modal-card" style="text-align: left; max-width: 480px;">
          <div class="modal-header-luxury">
            <h3 class="modal-title" style="margin: 0; text-align: left;">CẬP NHẬT LỊCH HẸN #{{ selectedLichHen.id }}
            </h3>
            <button type="button" class="close-btn-luxury" @click="showModal = false">&times;</button>
          </div>

          <form @submit.prevent="submitUpdateStatus" style="margin-top: 15px;">
            <div class="form-group-dark">
              <label>Tên khách hàng</label>
              <input type="text" :value="selectedLichHen.tenKhachHang" disabled class="input-dark-disabled" />
            </div>

            <div class="form-group-dark">
              <label>Ghi chú của khách hàng</label>
              <textarea rows="3" disabled
                class="input-dark-disabled">{{ selectedLichHen.ghiChu || 'Không có ghi chú' }}</textarea>
            </div>

            <div class="form-group-dark">
              <label>Cập nhật trạng thái</label>
              <select v-model="selectedLichHen.trangThai" class="select-dark">
                <option :value="0">Chờ xác nhận</option>
                <option :value="1">Đã xác nhận</option>
                <option :value="2">Hoàn thành</option>
                <option :value="3">Đã hủy</option>
              </select>
            </div>

            <div class="modal-actions-group" style="margin-top: 20px; justify-content: flex-end;">
              <button type="button" class="btn-modal-cancel" @click="showModal = false">HỦY BỎ</button>
              <button type="submit" class="btn-modal-submit">LƯU THAY ĐỔI</button>
            </div>
          </form>
        </div>
      </div>

      <!-- MODAL 2: NHẬP LÝ DO HỦY LỊCH HẸN VÀ GỬI EMAIL KHÁCH HÀNG -->
      <div v-if="showCancelModal" class="confirm-modal-overlay" @click.self="closeCancelModal">
        <div class="confirm-modal-card" style="text-align: left; max-width: 480px;">
          <div class="modal-header-luxury">
            <h3 class="modal-title" style="margin: 0; text-align: left; color: #ff4444;">HỦY LỊCH HẸN #{{
              selectedCancelItem?.id }}</h3>
            <button type="button" class="close-btn-luxury" @click="closeCancelModal">&times;</button>
          </div>

          <div class="modal-body-dark" style="margin-top: 15px;">
            <div class="form-group-dark" style="margin-bottom: 15px;">
              <label style="color: #aaa; font-weight: normal;">Khách hàng:</label>
              <strong style="color: #ffffff; font-size: 15px;">{{ selectedCancelItem?.tenKhachHang }}</strong>
              <span style="color: #d1aa68; font-size: 13px;"> ({{ selectedCancelItem?.email || 'Chưa đăng ký email'
                }})</span>
            </div>

            <div class="form-group-dark">
              <label>Lý do hủy lịch <span style="color: #ff4444;">*</span></label>
              <textarea v-model="cancelReason" rows="4"
                placeholder="Nhập chi tiết lý do hủy lịch hẹn (Sẽ tự động gửi Gmail cho khách)..."
                class="textarea-dark"></textarea>
            </div>
          </div>

          <div class="modal-actions-group" style="margin-top: 20px; justify-content: flex-end;">
            <button type="button" class="btn-modal-cancel" @click="closeCancelModal" :disabled="isSubmittingCancel">HỦY
              BỎ</button>
            <button type="button" class="btn-modal-submit" style="background-color: #ff4444; color: #ffffff;"
              @click="submitCancelAppointment" :disabled="isSubmittingCancel">
              {{ isSubmittingCancel ? 'ĐANG GỬI EMAIL...' : 'XÁC NHẬN HỦY & GỬI EMAIL' }}
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL 3: XÁC NHẬN THAO TÁC Ở GIỮA MÀN HÌNH -->
      <div v-if="showConfirmModal" class="confirm-modal-overlay" @click.self="showConfirmModal = false">
        <div class="confirm-modal-card">
          <div class="modal-icon-header">
            <i class="fa-solid fa-circle-question"></i>
          </div>
          <h3 class="modal-title">XÁC NHẬN THAO TÁC</h3>
          <p class="modal-desc">
            {{ confirmModalText || 'Bạn có chắc chắn muốn cập nhật trạng thái lịch hẹn này?' }}
          </p>
          <div class="modal-actions-group">
            <button type="button" class="btn-modal-cancel" @click="showConfirmModal = false">
              HỦY BỎ
            </button>
            <button type="button" class="btn-modal-submit" @click="handleExecuteConfirm">
              XÁC NHẬN
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL 4: THÔNG BÁO KẾT QUẢ Ở GIỮA MÀN HÌNH -->
      <div v-if="showAlertModal" class="confirm-modal-overlay" @click.self="showAlertModal = false">
        <div class="confirm-modal-card">
          <div class="modal-icon-header">
            <i :class="alertModalIcon" :style="{ color: alertModalTitleColor }"></i>
          </div>
          <h3 class="modal-title" :style="{ color: alertModalTitleColor }">{{ alertModalTitle }}</h3>
          <p class="modal-desc">
            {{ alertModalMessage }}
          </p>
          <div class="modal-actions-group">
            <button type="button" class="btn-modal-submit" @click="showAlertModal = false">
              ĐỒNG Ý
            </button>
          </div>
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
        danhSachLichHen.value = data.sort((a, b) => b.id - a.id); // Sắp xếp mới nhất lên đầu
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

// ================= LOGIC MODAL XÁC NHẬN & THÔNG BÁO Ở GIỮA MÀN HÌNH =================
const showConfirmModal = ref(false);
const confirmModalText = ref('');
const pendingConfirmAction = ref(null);

const showAlertModal = ref(false);
const alertModalTitle = ref('THÔNG BÁO');
const alertModalMessage = ref('');
const alertModalIcon = ref('fa-solid fa-circle-check');
const alertModalTitleColor = ref('#4CAF50');

const showCustomAlert = (message, title = 'THÔNG BÁO', isSuccess = true) => {
  alertModalMessage.value = message;
  alertModalTitle.value = title;
  alertModalIcon.value = isSuccess ? 'fa-solid fa-circle-check' : 'fa-solid fa-triangle-exclamation';
  alertModalTitleColor.value = isSuccess ? '#4CAF50' : '#ff4444';
  showAlertModal.value = true;
};

const triggerConfirmModal = (text, actionFn) => {
  confirmModalText.value = text;
  pendingConfirmAction.value = actionFn;
  showConfirmModal.value = true;
};

const handleExecuteConfirm = async () => {
  showConfirmModal.value = false;
  if (pendingConfirmAction.value) {
    await pendingConfirmAction.value();
    pendingConfirmAction.value = null;
  }
};

// ================= LOGIC CẬP NHẬT NHANH (XÁC NHẬN / HOÀN THÀNH) =================
const quickUpdateStatus = (id, newTrangThai) => {
  const statusTitle = newTrangThai === 1 ? 'XÁC NHẬN' : (newTrangThai === 2 ? 'HOÀN THÀNH' : 'CẬP NHẬT');
  const confirmMsg = `Bạn có chắc chắn muốn chuyển trạng thái lịch hẹn #${id} sang "${statusTitle}"?`;

  triggerConfirmModal(confirmMsg, async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/lich-hen/admin/cap-nhat-trang-thai/${id}?trangThai=${newTrangThai}`, {
        method: 'PUT'
      });

      if (response.ok) {
        showCustomAlert(`Đã cập nhật trạng thái lịch hẹn #${id} sang "${statusTitle}" thành công!`, 'CẬP NHẬT THÀNH CÔNG', true);
        fetchLichHen();
      } else {
        showCustomAlert('Cập nhật trạng thái thất bại từ Server.', 'LỖI MÁY CHỦ', false);
      }
    } catch (error) {
      console.error("Lỗi cập nhật nhanh:", error);
      showCustomAlert('Không thể kết nối đến server.', 'LỖI KẾT NỐI', false);
    }
  });
};

// ================= LOGIC MODAL HỦY LỊCH HẸN KÈM LÝ DO & GỬI EMAIL =================
const showCancelModal = ref(false);
const selectedCancelItem = ref(null);
const cancelReason = ref('');
const isSubmittingCancel = ref(false);

const openCancelModal = (item) => {
  selectedCancelItem.value = item;
  cancelReason.value = '';
  showCancelModal.value = true;
};

const closeCancelModal = () => {
  showCancelModal.value = false;
  selectedCancelItem.value = null;
  cancelReason.value = '';
};

const submitCancelAppointment = async () => {
  if (!cancelReason.value.trim()) {
    showCustomAlert('Vui lòng nhập lý do hủy lịch hẹn!', 'CẢNH BÁO', false);
    return;
  }

  isSubmittingCancel.value = true;
  try {
    const response = await fetch(`http://localhost:8080/api/lich-hen/admin/huy-lich-hen/${selectedCancelItem.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        lyDoHuy: cancelReason.value
      })
    });

    if (response.ok) {
      closeCancelModal();
      showCustomAlert('Đã hủy lịch hẹn và gửi Email thông báo thành công tới khách hàng!', 'HỦY LỊCH THÀNH CÔNG', true);
      fetchLichHen();
    } else {
      const errText = await response.text();
      showCustomAlert('Lỗi: ' + errText, 'LỖI HỦY LỊCH', false);
    }
  } catch (error) {
    console.error("Lỗi khi gửi yêu cầu hủy lịch:", error);
    showCustomAlert('Không thể kết nối đến máy chủ!', 'LỖI KẾT NỐI', false);
  } finally {
    isSubmittingCancel.value = false;
  }
};

// ================= LOGIC MODAL CHỈNH SỬA =================
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
      showModal.value = false;
      showCustomAlert('Cập nhật trạng thái thành công!', 'THÀNH CÔNG', true);
      fetchLichHen();
    } else {
      showCustomAlert('Cập nhật thất bại từ Server.', 'LỖI MÁY CHỦ', false);
    }
  } catch (error) {
    console.error("Lỗi cập nhật:", error);
    showCustomAlert('Không thể kết nối đến server.', 'LỖI KẾT NỐI', false);
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
  switch (status) {
    case 0: return 'Chờ xác nhận';
    case 1: return 'Đã xác nhận';
    case 2: return 'Hoàn thành';
    case 3: return 'Đã hủy';
    default: return 'Không rõ';
  }
};

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'badge-warning';
    case 1: return 'badge-info';
    case 2: return 'badge-success';
    case 3: return 'badge-danger';
    default: return '';
  }
};
</script>

<style scoped>
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
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
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
  flex: 1;
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
.date-text {
  color: var(--wood-dark);
}

.time-text {
  color: var(--text-muted);
}

/* --- Badges Trạng thái --- */
.badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.badge-warning {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.badge-info {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.badge-success {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.badge-danger {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
}

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

.btn-action.reset-btn {
  background-color: #f0f0f0;
  color: #555;
}

.btn-action.reset-btn:hover {
  background-color: #e4e4e4;
  color: #333;
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

.btn-action.confirm {
  background-color: #f6ffed;
  color: #52c41a;
}

.btn-action.confirm:hover {
  background-color: #d9f7be;
}

.btn-action.complete {
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-action.complete:hover {
  background-color: #bae0ff;
}

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

/* --- Modal Style --- */
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

.modal-body {
  padding: 20px;
}

.form-group {
  padding: 0 0 15px 0;
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

.modal-actions {
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background: #fafafa;
  border-top: 1px solid var(--border-light);
}

.btn-cancel {
  background: #f0f0f0;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-main);
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

.btn-submit:disabled,
.btn-cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* CSS Modal Popup Xác Nhận ở Giữa Màn Hình (Theme Velora) */
.confirm-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.confirm-modal-card {
  background-color: #1a1918;
  border: 1px solid #d1aa68;
  border-radius: 8px;
  padding: 30px 25px;
  max-width: 440px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.85);
  animation: modalPopIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes modalPopIn {
  from {
    opacity: 0;
    transform: scale(0.85);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-icon-header i {
  font-size: 44px;
  color: #d1aa68;
  margin-bottom: 12px;
}

.modal-title {
  color: #d1aa68;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.modal-desc {
  color: #ffffff;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 25px;
}

.modal-actions-group {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-modal-cancel {
  background-color: #2e2b27;
  color: #cccccc;
  border: 1px solid #444444;
  padding: 10px 25px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-modal-cancel:hover {
  background-color: #444444;
  color: #ffffff;
}

.btn-modal-submit {
  background-color: #d1aa68;
  color: #1a1918;
  border: none;
  padding: 10px 30px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(209, 170, 104, 0.3);
}

.btn-modal-submit:hover {
  background-color: #e5be7a;
  color: #000000;
}

/* CSS Giao diện Modal Hủy/Cập Nhật Lịch Hẹn Tone Đen Viền Vàng Kim Velora */
.modal-header-luxury {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #333333;
  padding-bottom: 12px;
}

.close-btn-luxury {
  background: none;
  border: none;
  color: #aaaaaa;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn-luxury:hover {
  color: #d1aa68;
}

.form-group-dark {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 15px;
}

.form-group-dark label {
  color: #cccccc;
  font-size: 13px;
  font-weight: 600;
}

.input-dark-disabled {
  background-color: #11100f;
  border: 1px solid #333333;
  color: #888888;
  padding: 10px 12px;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}

.select-dark,
.textarea-dark {
  background-color: #11100f;
  border: 1px solid #d1aa68;
  color: #ffffff;
  padding: 10px 12px;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
}

.select-dark option {
  background-color: #1a1918;
  color: #ffffff;
}

.textarea-dark:focus,
.select-dark:focus {
  border-color: #e5be7a;
  box-shadow: 0 0 5px rgba(209, 170, 104, 0.4);
}
</style>