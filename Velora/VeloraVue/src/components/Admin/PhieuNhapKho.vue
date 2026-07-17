<template>
    <div class="admin-wrapper">
        <nav class="sidebar">
            <h2 class="brand">VELORA ADMIN</h2>
            <ul class="menu">
                <li v-for="item in menuItems" :key="item.name">
                    <router-link :to="item.link" active-class="active">
                        <i :class="item.icon"></i> {{ item.name }}
                    </router-link>
                </li>
            </ul>
            <div class="sidebar-bottom">
                <router-link to="/" class="exit"><i class="fa-solid fa-house"></i> Return</router-link>
                <button class="logout" @click="handleLogout"><i class="fa-solid fa-right-from-bracket"></i>
                    Logout</button>
            </div>
        </nav>

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
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const menuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group' },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked' },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar' },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem' },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list' },
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags' },
    { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check' }, 
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', requiresAdmin: true }
];

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

const processReceipt = async (id, trangThaiMoi) => {
    const isApprove = trangThaiMoi === 'DA_DUYET';
    const message = isApprove
        ? "XÁC NHẬN DUYỆT: Số lượng sản phẩm trong phiếu này sẽ được cộng thẳng vào kho. Bạn có chắc chắn?"
        : "TỪ CHỐI: Hủy bỏ yêu cầu nhập kho này?";

    if (!confirm(message)) return;

    try {
        const res = await fetch(`http://localhost:8080/api/phieu-nhap/${id}/trang-thai?trangThai=${trangThaiMoi}`, {
            method: 'PATCH'
        });

        if (res.ok) {
            alert(isApprove ? "Đã duyệt phiếu và cộng kho thành công!" : "Đã từ chối phiếu nhập!");
            loadReceipts();
        } else {
            processMockReceipt(id, trangThaiMoi);
        }
    } catch (error) {
        processMockReceipt(id, trangThaiMoi);
    }
};

const processMockReceipt = (id, trangThaiMoi) => {
    alert(trangThaiMoi === 'DA_DUYET' ? "[Demo] Đã duyệt và cộng kho thành công!" : "[Demo] Đã từ chối phiếu nhập!");
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
        { maChiTietPhieuNhap: 1, maSanPham: 1, soLuongNhap: 10, giaNhap: 10000000 },
        { maChiTietPhieuNhap: 2, maSanPham: 2, soLuongNhap: 5, giaNhap: 8000000 }
    ];
};

const closeDetailModal = () => {
    showDetailModal.value = false;
    selectedReceipt.value = null;
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

onMounted(() => {
    loadReceipts();
});
</script>

<style scoped>
@import "../CSS/Admin/PhieuNhapKho.css";
</style>