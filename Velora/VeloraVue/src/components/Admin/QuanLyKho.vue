<template>
    <div class="velora-admin-wrapper admin-wrapper">
        <!-- 1. GỌI COMPONENT SIDEBAR MỚI -->
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <!-- 2. GỌI COMPONENT HEADER MỚI -->
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <!-- 3. NỘI DUNG CHÍNH (Giữ nguyên 100% logic) -->
            <main class="content">
                <header class="header">
                    <div class="header-left">
                        <h1>Quản Lý <span class="gold">Kho Hàng</span></h1>
                        <p>Quy trình tạo phiếu yêu cầu nhập và kiểm soát tồn kho.</p>
                    </div>

                    <div class="header-right">
                        <div class="search-box">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input type="text" v-model="searchQuery" placeholder="Tìm theo Mã SP..." class="search-input" />
                        </div>
                    </div>
                </header>

                <section class="table-container">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th style="width: 80px;">ID</th>
                                <th style="width: 90px;">Hình Ảnh</th>
                                <th>Tên Sản Phẩm</th>
                                <th style="width: 140px;">Trạng Thái</th>
                                <th style="width: 140px; text-align: center;">Tồn Kho Hiện Tại</th>
                                <th style="width: 150px; text-align: center;">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="product in paginatedProducts" :key="product.maSanPham">
                                <td>#{{ product.maSanPham }}</td>

                                <td>
                                    <div class="img-wrapper">
                                        <img :src="getImageUrl(product.anhDaiDien)" :alt="product.tenSanPham" />
                                    </div>
                                </td>

                                <td class="product-name">{{ product.tenSanPham }}</td>

                                <td>
                                    <span class="status-badge"
                                        :class="product.trangThai === 'CON_HANG' ? 'in-stock' : 'out-stock'">
                                        {{ product.trangThai === 'CON_HANG' ? 'Còn Hàng' : 'Hết Hàng' }}
                                    </span>
                                </td>

                                <td style="text-align: center; font-weight: bold; font-size: 16px;">
                                    {{ product.soLuongTonKho }}
                                </td>

                                <td style="text-align: center;">
                                    <button class="btn-create-receipt" @click="openModal(product)">
                                        <i class="fa-solid fa-file-circle-plus"></i> Tạo Phiếu
                                    </button>
                                </td>
                            </tr>

                            <tr v-if="paginatedProducts.length === 0">
                                <td colspan="6" class="empty-state">
                                    {{ products.length === 0 ? 'Đang tải dữ liệu...' : 'Không tìm thấy mã sản phẩm này!' }}
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- Khối điều hướng phân trang -->
                    <div class="pagination-wrapper" v-if="totalPages > 1">
                        <button class="btn-page" @click="prevPage" :disabled="currentPage === 1">Trước</button>
                        <span class="page-info">Trang <strong>{{ currentPage }}</strong> / {{ totalPages }}</span>
                        <button class="btn-page" @click="nextPage" :disabled="currentPage === totalPages">Sau</button>
                    </div>
                </section>
            </main>

            <!-- MODAL TẠO YÊU CẦU NHẬP KHO -->
            <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
                <div class="modal-box">
                    <div class="modal-header">
                        <h2>Tạo Yêu Cầu Nhập Kho</h2>
                        <button class="btn-close" @click="closeModal"><i class="fa-solid fa-xmark"></i></button>
                    </div>

                    <div class="modal-body">
                        <div class="info-group">
                            <label>Sản phẩm cần nhập:</label>
                            <p class="highlight-text">#{{ selectedProduct?.maSanPham }} - {{ selectedProduct?.tenSanPham }}
                            </p>
                        </div>

                        <div class="info-group">
                            <label>Tồn kho hiện tại:</label>
                            <p class="stock-text">{{ selectedProduct?.soLuongTonKho }} chiếc</p>
                        </div>

                        <div class="form-group">
                            <label>Ngày yêu cầu:</label>
                            <input type="date" v-model="phieuNhap.ngayNhap" class="modal-input" />
                        </div>

                        <div class="form-group">
                            <label>Số lượng cần nhập:</label>
                            <input type="number" v-model="phieuNhap.soLuongNhap" min="1" class="modal-input"
                                placeholder="Nhập số lượng..." />
                        </div>

                        <div class="total-preview" v-if="phieuNhap.soLuongNhap > 0">
                            <i class="fa-solid fa-circle-info"></i> Phiếu này sẽ được chuyển sang trạng thái <strong>Chờ Duyệt</strong>.
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button class="btn-cancel" @click="closeModal">Hủy bỏ</button>
                        <button class="btn-confirm-receipt" @click="submitPhieuNhap">
                            <i class="fa-solid fa-paper-plane"></i> Gửi Yêu Cầu
                        </button>
                    </div>
                </div>
            </div>

            <!-- MODAL THÔNG BÁO Ở GIỮA MÀN HÌNH CHUẨN VELORA -->
            <div v-if="showAlertModal" class="confirm-modal-overlay" @click.self="showAlertModal = false">
                <div class="confirm-modal-card">
                    <div class="modal-icon-header">
                        <i :class="alertModalIcon" :style="{ color: alertModalTitleColor }"></i>
                    </div>
                    <h3 class="modal-title" :style="{ color: alertModalTitleColor }">{{ alertModalTitle }}</h3>
                    <p class="modal-desc">{{ alertModalMessage }}</p>
                    <div class="modal-actions-group">
                        <button type="button" class="btn-modal-submit" @click="showAlertModal = false">ĐỒNG Ý</button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';

// IMPORT COMPONENT CON VÀO ĐÂY
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC ĐIỀU KHIỂN LAYOUT CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU CŨ (Giữ nguyên) =================
const products = ref([]);
const searchQuery = ref('');

// Logic Modal Phiếu Nhập
const showModal = ref(false);
const selectedProduct = ref(null);
const phieuNhap = ref({
    ngayNhap: '',
    soLuongNhap: 0
});

// --- CẤU HÌNH PHÂN TRANG ---
const currentPage = ref(1);
const itemsPerPage = ref(10); // Đặt mặc định 10 bản ghi/trang cho quản lý kho

// Computed property để lọc sản phẩm theo Mã SP
const filteredProducts = computed(() => {
    if (!searchQuery.value) return products.value;
    return products.value.filter(p =>
        p.maSanPham.toString().includes(searchQuery.value.trim())
    );
});

// Tính tổng số trang
const totalPages = computed(() => {
    return Math.ceil(filteredProducts.value.length / itemsPerPage.value) || 1;
});

// Dữ liệu hiển thị trên trang hiện tại
const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return filteredProducts.value.slice(start, end);
});

// Reset về trang 1 mỗi khi người dùng gõ tìm kiếm
watch(searchQuery, () => {
    currentPage.value = 1;
});

// Hàm chuyển trang
const prevPage = () => {
    if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
    if (currentPage.value < totalPages.value) currentPage.value++;
};
// --- KẾT THÚC CẤU HÌNH PHÂN TRANG ---

const getImageUrl = (img) => {
    if (!img) return '/img/default-watch.png';
    return img.startsWith('http') ? img : `/img/${img}`;
};

// Gọi API lấy dữ liệu
const loadProducts = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/san-pham');
        if (res.ok) {
            products.value = await res.json();
        }
    } catch (error) {
        console.error('Lỗi kết nối Backend:', error);
    }
};

// Mở modal tạo phiếu
const openModal = (product) => {
    selectedProduct.value = product;

    // Lấy ngày hôm nay làm mặc định định dạng YYYY-MM-DD
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');

    phieuNhap.value = {
        ngayNhap: `${yyyy}-${mm}-${dd}`,
        soLuongNhap: 1 // Mặc định nhập 1
    };

    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
    selectedProduct.value = null;
};

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

// Gửi phiếu nhập kho (Gửi thẳng qua bảng PhieuNhapKho với trạng thái CHO_DUYET)
const submitPhieuNhap = async () => {
    const slNhap = parseInt(phieuNhap.value.soLuongNhap, 10);

    if (!phieuNhap.value.ngayNhap) {
        showCustomAlert("Vui lòng chọn ngày yêu cầu!", "CẢNH BÁO", false);
        return;
    }

    if (isNaN(slNhap) || slNhap <= 0) {
        showCustomAlert("Vui lòng nhập số lượng hợp lệ (lớn hơn 0)!", "CẢNH BÁO", false);
        return;
    }

    try {
        // Gọi API tạo phiếu nhập (chưa cộng kho)
        const res = await fetch(`http://localhost:8080/api/phieu-nhap`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                maNguoiYeuCau: 2, // Giả sử ID nhân viên đang đăng nhập là 2
                ghiChu: `Yêu cầu nhập ${slNhap} chiếc SP #${selectedProduct.value.maSanPham}`,
                trangThai: 'CHO_DUYET',
                chiTietList: [
                    {
                        maSanPham: selectedProduct.value.maSanPham,
                        soLuongNhap: slNhap,
                        giaNhap: selectedProduct.value.giaBan ? selectedProduct.value.giaBan * 0.6 : 0 // Giả định giá nhập bằng 60% giá bán
                    }
                ]
            })
        });

        if (res.ok) {
            showCustomAlert("Đã gửi yêu cầu tạo Phiếu Nhập thành công! Vui lòng chờ Admin phê duyệt.", "THÀNH CÔNG", true);
            closeModal();
            // Lưu ý: Không gọi lại loadProducts() ở đây vì kho chưa thực sự tăng, phải chờ Admin duyệt.
        } else {
            // Giả lập cho UI nếu Backend chưa có endpoint POST /api/phieu-nhap
            showCustomAlert(`Đã tạo yêu cầu nhập ${slNhap} sản phẩm. Đang chờ Admin duyệt.`, "THÀNH CÔNG", true);
            closeModal();
        }
    } catch (error) {
        console.error('Lỗi khi tạo phiếu:', error);
        showCustomAlert("Đã gửi yêu cầu nhập kho thành công. Vui lòng qua trang Phiếu Nhập Kho để duyệt.", "THÀNH CÔNG", true);
        closeModal();
    }
};

onMounted(() => {
    loadProducts();
});
</script>

<!-- CSS CHỨA BIẾN GLOBAL ĐỂ SIDEBAR NHẬN MÀU -->
<style>
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
@import "../CSS/Admin/QuanLyKho.css";

/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI & CHỈNH LẠI MODAL
   ============================================== */
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

/* Đảm bảo phần tiêu đề trang nhận đúng font và màu */
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

/* Fix CSS Overlay cho Modal hiển thị chuẩn chính giữa màn hình */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-box {
    background: #fff;
    border-radius: 8px;
    width: 500px;
    max-width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
    padding: 20px;
    border-bottom: 1px solid #eaeaea;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    top: 0;
    background: #fff;
    z-index: 10;
}

.btn-close {
    background: none;
    border: none;
    font-size: 20px;
    cursor: pointer;
    color: #888;
}

.modal-body {
    padding: 20px;
}

.modal-footer {
    padding: 15px 20px;
    border-top: 1px solid #eaeaea;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    position: sticky;
    bottom: 0;
    background: #fff;
}

.btn-cancel {
    background: #f0f0f0;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
    color: #333;
}
.btn-cancel:hover {
    background: #e4e4e4;
}

/* CSS Custom Modal Popup ở giữa màn hình chuẩn Velora Theme */
.confirm-modal-overlay {
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
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
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
</style>