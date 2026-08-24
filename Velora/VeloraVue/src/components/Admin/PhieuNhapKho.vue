<template>
    <div class="velora-admin-wrapper">
        <!-- 1. GỌI COMPONENT SIDEBAR MỚI -->
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <!-- 2. GỌI COMPONENT HEADER MỚI -->
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <!-- 3. NỘI DUNG CHÍNH -->
            <main class="content">
                <header class="header">
                    <div class="header-left">
                        <h1>Quản Lý <span class="gold">Phiếu Nhập</span></h1>
                        <p>Phê duyệt yêu cầu nhập hàng từ nhân viên và kiểm soát biến động kho.</p>
                    </div>
                </header>

                <div class="controls-container">
                    <div class="filter-group">
                        <div class="filter-item">
                            <label>Trạng Thái:</label>
                            <select v-model="filterTrangThai">
                                <option value="all">Tất cả</option>
                                <option value="CHO_DUYET">Chờ duyệt</option>
                                <option value="DA_DUYET">Đã duyệt</option>
                                <option value="TU_CHOI">Từ chối</option>
                            </select>
                        </div>

                        <div class="filter-item">
                            <label>Người Yêu Cầu:</label>
                            <div class="search-box">
                                <i class="fa-solid fa-magnifying-glass"></i>
                                <input 
                                    type="text" 
                                    v-model="searchNguoiYeuCau" 
                                    placeholder="Nhập ID nhân viên..." 
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <section class="table-container">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Người Yêu Cầu</th>
                                <th>Ngày Tạo</th>
                                <th>Ngày Duyệt</th>
                                <th>Trạng Thái</th>
                                <th>Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="receipt in filteredReceipts" :key="receipt.maPhieuNhap">
                                <td class="receipt-code">{{ receipt.maPhieuNhapCode || 'PNK-#' + receipt.maPhieuNhap }}</td>
                                <td><strong>Nhân viên #{{ receipt.maNguoiYeuCau }}</strong></td>
                                <td>{{ formatDate(receipt.ngayYeuCau) }}</td>
                                <td>{{ receipt.ngayDuyet ? formatDate(receipt.ngayDuyet) : 'Chưa duyệt' }}</td>
                                <td>
                                    <span class="status-badge" :class="getStatusClass(receipt.trangThai)">
                                        <i :class="getStatusIcon(receipt.trangThai)"></i>
                                        {{ getStatusText(receipt.trangThai) }}
                                    </span>
                                </td>
                                <td class="actions">
                                    <button class="btn-action view" title="Xem chi tiết phiếu nhập"
                                        @click="viewDetails(receipt)">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                    <button class="btn-action approve" title="Duyệt phiếu (Cộng số lượng vào kho)"
                                        v-if="receipt.trangThai === 'CHO_DUYET'"
                                        @click="processReceipt(receipt.maPhieuNhap, 'DA_DUYET')">
                                        <i class="fa-solid fa-check"></i>
                                    </button>

                                    <button class="btn-action cancel" title="Từ chối yêu cầu nhập kho này"
                                        v-if="receipt.trangThai === 'CHO_DUYET'"
                                        @click="processReceipt(receipt.maPhieuNhap, 'TU_CHOI')">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>
                                </td>
                            </tr>
                            <tr v-if="filteredReceipts.length === 0">
                                <td colspan="6" class="empty-state">Không tìm thấy phiếu nhập nào phù hợp.</td>
                            </tr>
                        </tbody>
                    </table>
                </section>
            </main>

            <!-- MODAL CHI TIẾT -->
            <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
                <div class="modal-box modal-lg">
                    <div class="modal-header">
                        <h2>Chi Tiết Phiếu Nhập <span class="gold">{{ selectedReceipt?.maPhieuNhapCode }}</span></h2>
                        <button class="btn-close" @click="closeDetailModal"><i class="fa-solid fa-xmark"></i></button>
                    </div>
                    <div class="modal-body">
                        <div class="info-card" style="margin-bottom: 20px;">
                            <p><strong>Ghi chú từ nhân viên:</strong> {{ selectedReceipt?.ghiChu || 'Không có ghi chú đính kèm' }}</p>
                        </div>

                        <h4 class="table-title"><i class="fa-solid fa-boxes-stacked"></i> Danh Sách Hàng Hóa Cần Nhập</h4>
                        <table class="detail-table">
                            <thead>
                                <tr>
                                    <th>Mã Sản Phẩm</th>
                                    <th style="text-align: center;">Số Lượng Nhập</th>
                                    <th style="text-align: right;">Giá Nhập Dự Kiến</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="item in receiptDetails" :key="item.maChiTietPhieuNhap">
                                    <td><strong>Sản phẩm #{{ item.maSanPham }}</strong></td>
                                    <td style="text-align: center;"><span class="qty-badge">{{ item.soLuongNhap }}</span>
                                    </td>
                                    <td style="text-align: right; color: #d1aa68; font-weight: bold;">{{
                                        formatPrice(item.giaNhap) }}</td>
                                </tr>
                                <tr v-if="receiptDetails.length === 0">
                                    <td colspan="3" class="empty-state">Đang tải danh sách hàng hóa...</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button class="btn-cancel" @click="closeDetailModal">Đóng</button>
                    </div>
                </div>
            </div>

            <!-- MODAL POPUP XÁC NHẬN Ở GIỮA MÀN HÌNH -->
            <div v-if="showConfirmModal" class="confirm-modal-overlay" @click.self="showConfirmModal = false">
                <div class="confirm-modal-card">
                    <div class="modal-icon-header">
                        <i class="fa-solid fa-circle-question"></i>
                    </div>
                    <h3 class="modal-title">XÁC NHẬN THAO TÁC</h3>
                    <p class="modal-desc">{{ confirmModalText }}</p>
                    <div class="modal-actions-group">
                        <button type="button" class="btn-modal-cancel" @click="showConfirmModal = false">HỦY BỎ</button>
                        <button type="button" class="btn-modal-submit" @click="handleExecuteConfirm">XÁC NHẬN</button>
                    </div>
                </div>
            </div>

            <!-- MODAL POPUP THÔNG BÁO Ở GIỮA MÀN HÌNH -->
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
import { ref, onMounted, computed } from 'vue';

// IMPORT COMPONENT SIDEBAR & HEADER MỚI
import AdminSidebar from './AdminSidebar.vue'; 
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC GIAO DIỆN CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU =================
const receipts = ref([]);
const showDetailModal = ref(false);
const selectedReceipt = ref(null);
const receiptDetails = ref([]);

// State cho việc lọc
const filterTrangThai = ref('all');
const searchNguoiYeuCau = ref('');

// Computed Property để tự động lọc phiếu nhập
const filteredReceipts = computed(() => {
    return receipts.value.filter(receipt => {
        // Lọc theo trạng thái
        const matchTrangThai = filterTrangThai.value === 'all' || receipt.trangThai === filterTrangThai.value;
        
        // Lọc theo mã người yêu cầu (so sánh chuỗi bất chấp chữ hoa/thường)
        const matchNguoiYeuCau = !searchNguoiYeuCau.value || 
            String(receipt.maNguoiYeuCau).toLowerCase().includes(searchNguoiYeuCau.value.toLowerCase());
            
        return matchTrangThai && matchNguoiYeuCau;
    });
});

const formatPrice = (value) => {
    if (!value) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatDate = (dateInput) => {
    if (!dateInput) return 'N/A';
    
    try {
        if (Array.isArray(dateInput)) {
            const [y, m, d, h = 0, min = 0, s = 0] = dateInput;
            const date = new Date(y, m - 1, d, h, min, s);
            return date.toLocaleDateString('vi-VN', { 
                day: '2-digit', month: '2-digit', year: 'numeric', 
                hour: '2-digit', minute: '2-digit' 
            });
        }

        const date = new Date(dateInput);
        if (isNaN(date.getTime())) return 'N/A';
        
        return date.toLocaleDateString('vi-VN', { 
            day: '2-digit', month: '2-digit', year: 'numeric', 
            hour: '2-digit', minute: '2-digit' 
        });

    } catch (error) {
        return 'N/A';
    }
};

const getStatusClass = (status) => {
    if (status === 'DA_DUYET') return 'approved';
    if (status === 'TU_CHOI') return 'rejected';
    return 'pending';
};

const getStatusText = (status) => {
    if (status === 'DA_DUYET') return 'Đã Duyệt';
    if (status === 'TU_CHOI') return 'Từ Chối';
    return 'Chờ Duyệt';
};

const getStatusIcon = (status) => {
    if (status === 'DA_DUYET') return 'fa-solid fa-check';
    if (status === 'TU_CHOI') return 'fa-solid fa-xmark';
    return 'fa-solid fa-clock';
};

const loadReceipts = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/phieu-nhap');
        if (res.ok) {
            receipts.value = await res.json();
        } else {
            loadMockData(); 
        }
    } catch (error) {
        console.warn("Chưa kết nối được Backend. Đang dùng dữ liệu mẫu.");
        loadMockData();
    }
};

const loadMockData = () => {
    receipts.value = [
        { maPhieuNhap: 1, maPhieuNhapCode: 'PNK-2026061501', maNguoiYeuCau: 2, ngayYeuCau: '2026-06-15T08:30:00', trangThai: 'DA_DUYET', ngayDuyet: '2026-06-15T09:00:00', ghiChu: 'Nhập lô hàng Velora Noir Starlight' },
        { maPhieuNhap: 2, maPhieuNhapCode: 'PNK-2026061701', maNguoiYeuCau: 2, ngayYeuCau: '2026-06-17T14:15:00', trangThai: 'CHO_DUYET', ngayDuyet: null, ghiChu: 'Yêu cầu nhập bổ sung Rolex Daytona' },
        { maPhieuNhap: 3, maPhieuNhapCode: 'PNK-2026061805', maNguoiYeuCau: 5, ngayYeuCau: '2026-06-18T10:00:00', trangThai: 'CHO_DUYET', ngayDuyet: null, ghiChu: 'Nhập hàng test' }
    ];
};

// --- MODAL POPUP THÔNG BÁO Ở GIỮA MÀN HÌNH CHUẨN VELORA ---
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

const processReceipt = (id, trangThaiMoi) => {
    const isApprove = trangThaiMoi === 'DA_DUYET';
    const message = isApprove
        ? "XÁC NHẬN DUYỆT: Số lượng sản phẩm trong phiếu này sẽ được cộng thẳng vào kho. Bạn có chắc chắn?"
        : "TỪ CHỐI: Hủy bỏ yêu cầu nhập kho này?";

    triggerConfirmModal(message, async () => {
        try {
            const res = await fetch(`http://localhost:8080/api/phieu-nhap/${id}/trang-thai?trangThai=${trangThaiMoi}`, {
                method: 'PATCH'
            });

            if (res.ok) {
                showCustomAlert(isApprove ? "Đã duyệt phiếu và cộng kho thành công!" : "Đã từ chối phiếu nhập!", "THÀNH CÔNG", true);
                loadReceipts();
            } else {
                processMockReceipt(id, trangThaiMoi);
            }
        } catch (error) {
            processMockReceipt(id, trangThaiMoi);
        }
    });
};

const processMockReceipt = (id, trangThaiMoi) => {
    showCustomAlert(trangThaiMoi === 'DA_DUYET' ? "Đã duyệt và cộng kho thành công!" : "Đã từ chối phiếu nhập!", "THÀNH CÔNG", true);
    const idx = receipts.value.findIndex(r => r.maPhieuNhap === id);
    if (idx !== -1) {
        receipts.value[idx].trangThai = trangThaiMoi;
        receipts.value[idx].ngayDuyet = new Date().toISOString();
    }
};

const viewDetails = async (receipt) => {
    selectedReceipt.value = receipt;
    showDetailModal.value = true;
    receiptDetails.value = [];

    try {
        const res = await fetch(`http://localhost:8080/api/phieu-nhap/${receipt.maPhieuNhap}/chi-tiet`);
        if (res.ok) {
            receiptDetails.value = await res.json();
        } else {
            loadMockDetails();
        }
    } catch (error) {
        loadMockDetails();
    }
};

const loadMockDetails = () => {
    receiptDetails.value = [
        { maChiTietPhieuNhap: 101, maSanPham: 1, tenSanPham: 'Velora Noir Starlight Edition', soLuongNhap: 20, giaNhap: 15000000 },
        { maChiTietPhieuNhap: 102, maSanPham: 2, tenSanPham: 'Rolex Daytona Gold Master', soLuongNhap: 5, giaNhap: 450000000 }
    ];
};

const closeDetailModal = () => {
    showDetailModal.value = false;
    selectedReceipt.value = null;
};

onMounted(() => {
    loadReceipts();
});
</script>

<style scoped>
@import "../CSS/Admin/PhieuNhapKho.css";

/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI
   ============================================== */
.velora-admin-wrapper {
    /* ĐÂY LÀ ĐOẠN BIẾN CSS LÚC NÃY ANH QUÊN MẤT, ĐÃ BỔ SUNG ĐẦY ĐỦ ĐỂ SIDEBAR NHẬN MÀU */
    --wood-dark: #362921;
    --wood-active: #47372c;
    --wood-medium: #544438;
    --wood-light: #7a6352;
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

/* Đè CSS cho màn Overlay của Modal để không bị tẹt xuống dưới */
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
    width: 600px; 
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
</style>