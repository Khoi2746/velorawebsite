<template>
    <div class="velora-admin-wrapper admin-wrapper">
        <!-- 1. GỌI COMPONENT SIDEBAR MỚI -->
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <!-- 2. GỌI COMPONENT HEADER MỚI -->
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <!-- 3. NỘI DUNG CHÍNH (Giữ nguyên 100% logic của ku em) -->
            <main class="content">
                <header class="header">
                    <div class="header-title">
                        <h1>Quản Lý <span class="gold">Mã Giảm Giá</span></h1>
                        <p>Tạo và cấu hình các mã khuyến mãi, giới hạn lượt sử dụng cho khách hàng.</p>
                    </div>
                </header>

                <!-- KHỐI CÔNG CỤ (TÌM KIẾM + LỌC + THÊM MỚI) TRÊN 1 HÀNG -->
                <div class="controls-container">
                    <!-- Ô Tìm kiếm -->
                    <div class="search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input 
                            type="text" 
                            v-model="searchQuery" 
                            placeholder="Tìm theo mã code..."
                        >
                    </div>

                    <!-- Cụm Bộ lọc -->
                    <div class="filter-group">
                        <div class="filter-item">
                            <label>Trạng thái:</label>
                            <select v-model="filterTrangThai">
                                <option value="all">Tất cả</option>
                                <option value="active">Đang hoạt động</option>
                                <option value="expired">Đã hết hạn</option>
                                <option value="empty">Đã hết lượt</option>
                            </select>
                        </div>
                        <div class="filter-item">
                            <label>Mức giảm:</label>
                            <select v-model="filterMucGiam">
                                <option value="all">Tất cả</option>
                                <option value="under_10">Dưới 10%</option>
                                <option value="10_to_20">Từ 10% - 20%</option>
                                <option value="over_20">Trên 20%</option>
                            </select>
                        </div>
                        <div class="filter-item">
                            <label>Thời hạn:</label>
                            <select v-model="filterHanSuDung">
                                <option value="all">Tất cả</option>
                                <option value="permanent">Vĩnh viễn</option>
                                <option value="limited">Có thời hạn</option>
                            </select>
                        </div>
                    </div>

                    <!-- Nút Thêm Mới (Được đẩy sang phải) -->
                    <button @click="moModal()" class="btn-add">
                        <i class="fa-solid fa-plus"></i> Thêm mã mới
                    </button>
                </div>

                <!-- BẢNG DỮ LIỆU -->
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>MÃ PHIẾU / CODE</th>
                                <th>MỨC GIẢM (%)</th>
                                <th>ĐÃ DÙNG / GIỚI HẠN</th>
                                <th>HẠN SỬ DỤNG</th>
                                <th>TRẠNG THÁI</th>
                                <th>HÀNH ĐỘNG</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in paginatedDanhSach" :key="item.id">
                                <td><strong>{{ item.maCode }}</strong></td>
                                <td>Giảm {{ item.phanTramGiam }}%</td>
                                <td>{{ item.soLuotDaDung }} / {{ item.gioiHanSuDung }}</td>
                                <td>
                                    <span :class="{'permanent': !item.ngayHetHan}">
                                        {{ formatDate(item.ngayHetHan) }}
                                    </span>
                                </td>
                                <td>
                                    <span class="status-badge" :class="tinhTrangThai(item).class">
                                        {{ tinhTrangThai(item).text }}
                                    </span>
                                </td>
                                <td class="action-buttons">
                                    <button @click="moModal(item)" class="btn-icon btn-edit" title="Sửa">
                                        <i class="fa-solid fa-pen"></i>
                                    </button>
                                    <button @click="xoaMa(item.id)" class="btn-icon btn-delete" title="Xóa">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>
                                </td>
                            </tr>
                            <tr v-if="paginatedDanhSach.length === 0">
                                <td colspan="6" class="empty-msg">Không tìm thấy mã giảm giá nào phù hợp.</td>
                            </tr>
                        </tbody>
                    </table>
                    
                    <!-- Khối điều hướng phân trang -->
                    <div class="pagination-wrapper" v-if="totalPages > 1">
                        <button class="btn-page" @click="prevPage" :disabled="currentPage === 1">Trước</button>
                        <span class="page-info">Trang <strong>{{ currentPage }}</strong> / {{ totalPages }}</span>
                        <button class="btn-page" @click="nextPage" :disabled="currentPage === totalPages">Sau</button>
                    </div>
                </div>
            </main>

            <!-- MODAL THÊM / SỬA -->
            <div v-if="hienThiModal" class="modal-overlay" @click.self="hienThiModal = false">
                <div class="modal-content">
                    <h3>{{ dangSua ? 'Sửa Mã Giảm Giá' : 'Thêm Mã Mới' }}</h3>
                    <form @submit.prevent="luuMaGiamGia">
                        <div class="form-group">
                            <label>Mã Code (VD: SALE10)</label>
                            <input v-model="formData.maCode" :disabled="dangSua" type="text" required>
                        </div>
                        <div class="form-group">
                            <label>Mức giảm (%)</label>
                            <input v-model="formData.phanTramGiam" type="number" step="0.1" required>
                        </div>
                        <div class="form-group">
                            <label>Giới hạn số lượt dùng</label>
                            <input v-model="formData.gioiHanSuDung" type="number" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Thời hạn sử dụng</label>
                            <select v-model="loaiHanSuDung">
                                <option value="none">Vĩnh viễn (Không hết hạn)</option>
                                <option value="1">Hết hạn sau 1 ngày</option>
                                <option value="5">Hết hạn sau 5 ngày</option>
                                <option value="15">Hết hạn sau 15 ngày</option>
                                <option value="30">Hết hạn sau 30 ngày</option>
                                <option value="custom">Chọn ngày khác...</option>
                            </select>
                        </div>

                        <div v-if="loaiHanSuDung === 'custom'" class="form-group">
                            <label>Chọn ngày hết hạn (Mặc định đến 23:59 ngày đó)</label>
                            <input v-model="formData.ngayHetHan" type="date" required>
                        </div>

                        <div class="modal-actions">
                            <button type="button" @click="hienThiModal = false" class="btn-cancel">Hủy</button>
                            <button type="submit" class="btn-save">Lưu lại</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';

// IMPORT COMPONENT CON VÀO ĐÂY
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC ĐIỀU KHIỂN LAYOUT CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU CŨ (Giữ nguyên 100%) =================
const danhSachMa = ref([]);
const searchQuery = ref(''); 
const hienThiModal = ref(false);
const dangSua = ref(false);
const formData = ref({ id: null, maCode: '', phanTramGiam: 0, gioiHanSuDung: 100, ngayHetHan: '' });
const loaiHanSuDung = ref('none');

const filterTrangThai = ref('all');
const filterMucGiam = ref('all');
const filterHanSuDung = ref('all');

// --- CẤU HÌNH PHÂN TRANG ---
const currentPage = ref(1);
const itemsPerPage = ref(7); // Số lượng bản ghi mã giảm giá trên một trang (có thể tùy chỉnh)

const tinhTrangThai = (item) => {
    if (item.soLuotDaDung >= item.gioiHanSuDung) {
        return { text: 'Hết lượt', class: 'status-empty', isExpired: false, isEmpty: true };
    }
    if (item.ngayHetHan) {
        const now = new Date();
        const expiry = new Date(item.ngayHetHan);
        if (now > expiry) {
            return { text: 'Đã hết hạn', class: 'status-expired', isExpired: true, isEmpty: false };
        }
    }
    return { text: 'Đang hoạt động', class: 'status-active', isExpired: false, isEmpty: false };
};

const danhSachMaLoc = computed(() => {
    return danhSachMa.value.filter(item => {
        const lowerCaseQuery = searchQuery.value.toLowerCase();
        const matchSearch = !searchQuery.value || item.maCode.toLowerCase().includes(lowerCaseQuery);

        const statusObj = tinhTrangThai(item);
        let matchTrangThai = true;
        if (filterTrangThai.value === 'active') matchTrangThai = statusObj.class === 'status-active';
        else if (filterTrangThai.value === 'expired') matchTrangThai = statusObj.class === 'status-expired';
        else if (filterTrangThai.value === 'empty') matchTrangThai = statusObj.class === 'status-empty';

        let matchMucGiam = true;
        if (filterMucGiam.value === 'under_10') matchMucGiam = item.phanTramGiam < 10;
        else if (filterMucGiam.value === '10_to_20') matchMucGiam = item.phanTramGiam >= 10 && item.phanTramGiam <= 20;
        else if (filterMucGiam.value === 'over_20') matchMucGiam = item.phanTramGiam > 20;

        let matchHanSuDung = true;
        if (filterHanSuDung.value === 'permanent') matchHanSuDung = !item.ngayHetHan;
        else if (filterHanSuDung.value === 'limited') matchHanSuDung = !!item.ngayHetHan;

        return matchSearch && matchTrangThai && matchMucGiam && matchHanSuDung;
    });
});

// Tính tổng số trang
const totalPages = computed(() => {
    return Math.ceil(danhSachMaLoc.value.length / itemsPerPage.value) || 1;
});

// Dữ liệu hiển thị trên trang hiện tại
const paginatedDanhSach = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return danhSachMaLoc.value.slice(start, end);
});

// Reset về trang 1 mỗi khi người dùng tìm kiếm hoặc thay đổi bộ lọc
watch([searchQuery, filterTrangThai, filterMucGiam, filterHanSuDung], () => {
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

const formatDate = (dateString) => {
    if (!dateString) return 'Vĩnh viễn';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { 
        day: '2-digit', month: '2-digit', year: 'numeric' 
    });
};

const layDanhSach = async () => {
  try {
    const res = await axios.get('/api/admin/ma-giam-gia');
    if (res.data && Array.isArray(res.data.content)) {
        danhSachMa.value = res.data.content; 
    } else if (Array.isArray(res.data)) {
        danhSachMa.value = res.data;        
    } else {
        danhSachMa.value = [];              
    }
  } catch (error) {
    console.error("Lỗi lấy dữ liệu:", error);
  }
};

const moModal = (item = null) => {
    if (item) {
        dangSua.value = true;
        let formattedDate = '';
        if (item.ngayHetHan) {
            formattedDate = item.ngayHetHan.substring(0, 10);
            loaiHanSuDung.value = 'custom'; 
        } else {
            loaiHanSuDung.value = 'none';   
        }
        formData.value = { ...item, ngayHetHan: formattedDate };
    } else {
        dangSua.value = false;
        loaiHanSuDung.value = 'none'; 
        formData.value = { id: null, maCode: '', phanTramGiam: 0, gioiHanSuDung: 100, ngayHetHan: '' };
    }
    hienThiModal.value = true;
};

const luuMaGiamGia = async () => {
    try {
        const payload = { ...formData.value };

        if (loaiHanSuDung.value === 'none') {
            payload.ngayHetHan = null;
        } else if (loaiHanSuDung.value === 'custom') {
            if (!payload.ngayHetHan) {
                alert("Vui lòng chọn ngày hết hạn cụ thể!");
                return;
            }
            if (payload.ngayHetHan.length === 10) {
                payload.ngayHetHan = `${payload.ngayHetHan}T23:59:59`;
            }
        } else {
            const soNgay = parseInt(loaiHanSuDung.value);
            const dateHienTai = new Date();
            dateHienTai.setDate(dateHienTai.getDate() + soNgay);
            
            const timezoneOffset = dateHienTai.getTimezoneOffset() * 60000;
            const targetDate = new Date(dateHienTai.getTime() - timezoneOffset).toISOString().slice(0, 10);
            payload.ngayHetHan = `${targetDate}T23:59:59`;
        }

        if (dangSua.value) {
            await axios.put(`/api/admin/ma-giam-gia/${payload.id}`, payload);
        } else {
            await axios.post('/api/admin/ma-giam-gia', payload);
        }
        hienThiModal.value = false;
        layDanhSach();
    } catch (error) {
        if (error.response && error.response.data) {
            alert("Lỗi: " + (typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data)));
        } else {
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        }
    }
};

const xoaMa = async (id) => {
    if (confirm("Bạn có chắc chắn muốn xóa mã giảm giá này?")) {
        try {
            await axios.delete(`/api/admin/ma-giam-gia/${id}`);
            // Lùi về trang trước nếu xóa phần tử cuối cùng của trang hiện tại
            if (paginatedDanhSach.value.length === 1 && currentPage.value > 1) {
                currentPage.value--;
            }
            layDanhSach();
        } catch (error) {
            if (error.response && error.response.data) {
                alert("Lỗi: " + error.response.data);
            }
        }
    }
};

onMounted(() => {
    layDanhSach();
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
/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI
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

.content {
    flex: 1;
    padding: 40px 60px;
    min-width: 0;
}

/* ==============================================
   CSS CỦA TRANG MÃ GIẢM GIÁ (Giữ nguyên)
   ============================================== */
.header {
    margin-bottom: 25px;
}

.header h1 {
    color: var(--wood-dark);
    font-size: 28px;
    margin-bottom: 8px;
    font-weight: bold;
}

.gold {
    color: var(--gold-matte);
}

.header p {
    color: var(--text-muted);
    font-size: 14px;
    margin: 0;
}

/* ================= KHỐI CONTROLS ĐỒNG BỘ 1 HÀNG ================= */
.controls-container {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.03);
    margin-bottom: 25px;
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap; /* Cho phép rớt dòng nếu màn hình quá nhỏ */
}

/* Tìm kiếm */
.search-box {
    position: relative;
    display: flex;
    align-items: center;
}

.search-box i {
    position: absolute;
    left: 12px;
    color: #999;
}

.search-box input {
    padding: 0 10px 0 35px;
    border: 1px solid #ddd;
    width: 220px;
    transition: 0.3s;
}

.search-box input:focus {
    outline: none;
    border-color: #d1aa68;
}

/* Nhóm Filter */
.filter-group {
    display: flex;
    align-items: center;
    gap: 15px;
}

.filter-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.filter-item label {
    font-size: 13px;
    font-weight: 600;
    color: #555;
    white-space: nowrap;
}

.filter-item select {
    padding: 0 10px;
    border: 1px solid #ddd;
    color: #333;
    outline: none;
    cursor: pointer;
    min-width: 140px;
}

.filter-item select:focus {
    border-color: #d1aa68;
}

/* Nút thêm mới - Được đẩy sang góc phải */
.btn-add {
    margin-left: auto;
    background-color: #3e332e;
    color: white;
    padding: 0 20px;
    border: none;
    cursor: pointer;
    transition: 0.3s;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    white-space: nowrap;
}

.btn-add:hover {
    background-color: #d1aa68;
}

/* Chiều cao đồng bộ cho tất cả control */
.search-box input, 
.filter-item select,
.btn-add {
    height: 40px;
    box-sizing: border-box;
    border-radius: 4px;
    font-size: 14px;
}

/* ================= BẢNG & TRẠNG THÁI ================= */
.status-badge {
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
}

.status-active {
    background-color: #e6f9ec;
    color: #27ae60;
}

.status-expired {
    background-color: #f2f2f2;
    color: #7f8c8d;
}

.status-empty {
    background-color: #fce4e4;
    color: #e74c3c;
}

.table-container {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.03);
    overflow: hidden;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th, td {
    padding: 16px 20px;
    text-align: left;
    border-bottom: 1px solid #f0f0f0;
}

th {
    font-size: 12px;
    font-weight: bold;
    color: #555;
}

td {
    font-size: 14px;
    color: #333;
}

td strong {
    color: #3e332e;
}

.permanent {
    color: #27ae60;
    font-weight: 500;
}

.empty-msg {
    text-align: center;
    padding: 30px;
    color: #999;
}

.action-buttons {
    display: flex;
    gap: 8px;
}

.btn-icon {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: 0.2s;
}

.btn-edit {
    background-color: #f0f0f0;
    color: #333;
}
.btn-edit:hover {
    background-color: #e0e0e0;
}

.btn-delete {
    background-color: #fce4e4;
    color: #e74c3c;
}
.btn-delete:hover {
    background-color: #f5caca;
}

/* ================= PHÂN TRANG ================= */
.pagination-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    padding: 20px 0;
    margin-top: 10px;
}

.btn-page {
    padding: 8px 16px;
    border: 1px solid #ccc;
    background-color: #fff;
    color: #333;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-page:hover:not(:disabled) {
    background-color: #f0f0f0;
    border-color: #bbb;
}

.btn-page:disabled {
    background-color: #f9f9f9;
    color: #aaa;
    cursor: not-allowed;
}

.page-info {
    font-size: 14px;
    color: #555;
}

/* ================= MODAL ================= */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    padding: 30px;
    border-radius: 8px;
    width: 100%;
    max-width: 450px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-content h3 {
    margin-top: 0;
    margin-bottom: 20px;
    color: #3e332e;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #5a4b44;
    font-size: 14px;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 15px;
    background-color: #fff;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: #d1aa68;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    margin-top: 30px;
}

.btn-cancel {
    padding: 10px 20px;
    background: #eee;
    color: #333;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: 0.2s;
}
.btn-cancel:hover { background: #ddd; }

.btn-save {
    padding: 10px 20px;
    background: #3e332e;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: 0.2s;
}
.btn-save:hover { background: #d1aa68; }
</style>