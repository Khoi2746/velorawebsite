<template>
    <div class="velora-admin-wrapper admin-wrapper">
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <main class="content">
                <!-- 🔥 HEADER MỚI CÓ NÚT LIÊN KẾT CHÉO -->
                <header class="header" style="display: flex; justify-content: space-between; align-items: flex-end;">
                    <div class="header-title">
                        <h1>Quản Lý <span class="gold">Mã Giảm Giá</span></h1>
                        <p>Tạo và cấu hình các mã khuyến mãi, giới hạn lượt sử dụng cho khách hàng.</p>
                    </div>
                    <button @click="$router.push('/bai-viet')" class="btn-link-cross">
                        <i class="fa-solid fa-bullhorn"></i> TỚI TRANG CHIẾN DỊCH MARKETING
                    </button>
                </header>

                <!-- KHỐI CÔNG CỤ -->
                <div class="controls-container">
                    <div class="search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input type="text" v-model="searchQuery" placeholder="Tìm theo mã code...">
                    </div>

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
                                    <span :class="{ 'permanent': !item.ngayHetHan }">
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

            <!-- MODAL POPUP XÁC NHẬN CHUẨN VELORA -->
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

            <!-- MODAL POPUP THÔNG BÁO CHUẨN VELORA -->
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
import axios from 'axios';
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const isCollapsed = ref(false);
const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

const danhSachMa = ref([]);
const searchQuery = ref('');
const hienThiModal = ref(false);
const dangSua = ref(false);
const formData = ref({ id: null, maCode: '', phanTramGiam: 0, gioiHanSuDung: 100, ngayHetHan: '' });
const loaiHanSuDung = ref('none');

const filterTrangThai = ref('all');
const filterMucGiam = ref('all');
const filterHanSuDung = ref('all');
const currentPage = ref(1);
const itemsPerPage = ref(7);

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

const totalPages = computed(() => Math.ceil(danhSachMaLoc.value.length / itemsPerPage.value) || 1);
const paginatedDanhSach = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    return danhSachMaLoc.value.slice(start, start + itemsPerPage.value);
});

watch([searchQuery, filterTrangThai, filterMucGiam, filterHanSuDung], () => { currentPage.value = 1; });
const prevPage = () => { if (currentPage.value > 1) currentPage.value--; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };

const formatDate = (dateString) => {
    if (!dateString) return 'Vĩnh viễn';
    return new Date(dateString).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const layDanhSach = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/admin/ma-giam-gia');
        if (res.data && Array.isArray(res.data.content)) danhSachMa.value = res.data.content;
        else if (Array.isArray(res.data)) danhSachMa.value = res.data;
        else danhSachMa.value = [];
    } catch (error) { console.error("Lỗi lấy dữ liệu:", error); }
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

const luuMaGiamGia = async () => {
    try {
        const payload = { ...formData.value };
        if (loaiHanSuDung.value === 'none') { payload.ngayHetHan = null; }
        else if (loaiHanSuDung.value === 'custom') {
            if (!payload.ngayHetHan) { showCustomAlert("Vui lòng chọn ngày hết hạn cụ thể!", "CẢNH BÁO", false); return; }
            if (payload.ngayHetHan.length === 10) payload.ngayHetHan = `${payload.ngayHetHan}T23:59:59`;
        } else {
            const soNgay = parseInt(loaiHanSuDung.value);
            const dateHienTai = new Date();
            dateHienTai.setDate(dateHienTai.getDate() + soNgay);
            const timezoneOffset = dateHienTai.getTimezoneOffset() * 60000;
            const targetDate = new Date(dateHienTai.getTime() - timezoneOffset).toISOString().slice(0, 10);
            payload.ngayHetHan = `${targetDate}T23:59:59`;
        }

        if (dangSua.value) {
            await axios.put(`http://localhost:8080/api/admin/ma-giam-gia/${payload.id}`, payload);
            showCustomAlert("Đã cập nhật thông tin mã giảm giá thành công!", "THÀNH CÔNG", true);
        } else {
            await axios.post('http://localhost:8080/api/admin/ma-giam-gia', payload);
            showCustomAlert("Đã tạo mới mã giảm giá thành công!", "THÀNH CÔNG", true);
        }
        hienThiModal.value = false;
        layDanhSach();
    } catch (error) {
        if (error.response && error.response.data) {
            const err = typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data);
            showCustomAlert("Lỗi: " + err, "LỖI TẠO MÃ", false);
        } else {
            showCustomAlert("Có lỗi xảy ra, vui lòng thử lại!", "LỖI MÁY CHỦ", false);
        }
    }
};

const xoaMa = (id) => {
    triggerConfirmModal("Bạn có chắc chắn muốn xóa mã giảm giá này?", async () => {
        try {
            await axios.delete(`http://localhost:8080/api/admin/ma-giam-gia/${id}`);
            if (paginatedDanhSach.value.length === 1 && currentPage.value > 1) currentPage.value--;
            showCustomAlert("Đã xóa mã giảm giá thành công!", "THÀNH CÔNG", true);
            layDanhSach();
        } catch (error) {
            if (error.response && error.response.data) showCustomAlert("Lỗi: " + error.response.data, "LỖI XÓA MÃ", false);
        }
    });
};

onMounted(() => { layDanhSach(); });
</script>

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

/* NÚT LIÊN KẾT MARKETING ĐỘC QUYỀN */
.btn-link-cross {
    background-color: transparent;
    color: #362921;
    border: 2px solid #362921;
    padding: 10px 20px;
    border-radius: 6px;
    font-weight: 700;
    font-size: 12px;
    cursor: pointer;
    transition: 0.3s;
    letter-spacing: 1px;
}

.btn-link-cross:hover {
    background-color: #362921;
    color: #fff;
}

.controls-container {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
    margin-bottom: 25px;
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;
}

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
    height: 40px;
    border-radius: 4px;
}

.search-box input:focus {
    outline: none;
    border-color: #d1aa68;
}

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
    height: 40px;
    border-radius: 4px;
}

.filter-item select:focus {
    border-color: #d1aa68;
}

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
    height: 40px;
    border-radius: 4px;
}

.btn-add:hover {
    background-color: #d1aa68;
}

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

th,
td {
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
    background: #f5caca;
}

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

/* MODAL */
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

.btn-cancel:hover {
    background: #ddd;
}

.btn-save {
    padding: 10px 20px;
    background: #3e332e;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: 0.2s;
}

.btn-save:hover {
    background: #d1aa68;
}

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
</style>