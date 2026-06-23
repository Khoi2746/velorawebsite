<template>
    <div class="admin-wrapper">
        <!-- SIDEBAR -->
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
                <button class="logout" @click="handleLogout"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
            </div>
        </nav>

        <!-- MAIN CONTENT -->
        <main class="content">
            <header class="header">
                <div class="header-title">
                    <h1>Quản Lý <span class="gold">Mã Giảm Giá</span></h1>
                    <p>Tạo và cấu hình các mã khuyến mãi, giới hạn lượt sử dụng cho khách hàng.</p>
                </div>
                <button @click="moModal()" class="btn-add">
                    <i class="fa-solid fa-plus"></i> Thêm mã mới
                </button>
            </header>

            <!-- BẢNG DỮ LIỆU -->
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>MÃ PHIẾU / CODE</th>
                            <th>NGƯỜI TẠO (GIẢM %)</th>
                            <th>ĐÃ DÙNG / GIỚI HẠN</th>
                            <th>HÀNH ĐỘNG</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in danhSachMa" :key="item.id">
                            <td><strong>{{ item.maCode }}</strong></td>
                            <td>Giảm {{ item.phanTramGiam }}%</td>
                            <td>{{ item.soLuotDaDung }} / {{ item.gioiHanSuDung }}</td>
                            <td class="action-buttons">
                                <button @click="moModal(item)" class="btn-icon btn-edit" title="Sửa">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button @click="xoaMa(item.id)" class="btn-icon btn-delete" title="Xóa">
                                    <i class="fa-solid fa-xmark"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="danhSachMa.length === 0">
                            <td colspan="4" class="empty-msg">Chưa có mã giảm giá nào.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </main>

        <!-- MODAL THÊM / SỬA -->
        <div v-if="hienThiModal" class="modal-overlay">
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
                    <div class="modal-actions">
                        <button type="button" @click="hienThiModal = false" class="btn-cancel">Hủy</button>
                        <button type="submit" class="btn-save">Lưu lại</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// Dữ liệu Sidebar
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
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie' }
];

const handleLogout = () => {
    localStorage.removeItem('role');
    window.location.href = '/';
};

// Dữ liệu quản lý mã
const danhSachMa = ref([]);
const hienThiModal = ref(false);
const dangSua = ref(false);
const formData = ref({ id: null, maCode: '', phanTramGiam: 0, gioiHanSuDung: 100 });

// Gọi API Lấy danh sách
const layDanhSach = async () => {
  try {
    const res = await axios.get('/api/admin/ma-giam-gia');
    
    // Xử lý ép kiểu dữ liệu: Chỉ lấy mảng danh sách thật, bỏ qua các thông số phân trang thừa
    if (res.data && Array.isArray(res.data.content)) {
        danhSachMa.value = res.data.content; // Trường hợp Backend trả về Page
    } else if (Array.isArray(res.data)) {
        danhSachMa.value = res.data;         // Trường hợp Backend trả về List trực tiếp
    } else {
        danhSachMa.value = [];               // Nếu không có gì thì cho mảng rỗng để bảng báo "Chưa có mã nào"
    }
    
  } catch (error) {
    console.error("Lỗi lấy dữ liệu:", error);
  }
};
const moModal = (item = null) => {
    if (item) {
        dangSua.value = true;
        formData.value = { ...item };
    } else {
        dangSua.value = false;
        formData.value = { id: null, maCode: '', phanTramGiam: 0, gioiHanSuDung: 100 };
    }
    hienThiModal.value = true;
};

const luuMaGiamGia = async () => {
    try {
        if (dangSua.value) {
            await axios.put(`/api/admin/ma-giam-gia/${formData.value.id}`, formData.value);
        } else {
            await axios.post('/api/admin/ma-giam-gia', formData.value);
        }
        hienThiModal.value = false;
        layDanhSach();
    } catch (error) {
        if (error.response && error.response.data) {
            alert("Lỗi: " + error.response.data);
        } else {
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        }
    }
};

const xoaMa = async (id) => {
    if (confirm("Bạn có chắc chắn muốn xóa mã giảm giá này?")) {
        try {
            await axios.delete(`/api/admin/ma-giam-gia/${id}`);
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

<style scoped>
/* ================= BỐ CỤC CHUNG ================= */
.admin-wrapper {
    display: flex;
    min-height: 100vh;
    background: #f4f1ea;
    font-family: sans-serif;
}

/* ================= SIDEBAR ================= */
.sidebar {
    width: 260px;
    background: #3e332e;
    color: #fff;
    padding: 40px 20px;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
}

.brand {
    font-size: 18px;
    color: #d1aa68;
    margin-bottom: 50px;
    text-align: center;
    letter-spacing: 2px;
}

.menu {
    padding: 0;
}

.menu li {
    margin-bottom: 20px;
    list-style: none;
}

.menu a {
    color: #ccc;
    text-decoration: none !important;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: 0.3s;
    padding: 10px;
    border-radius: 6px;
}

.menu a:hover,
.menu a.active {
    color: #d1aa68;
    background-color: rgba(209, 170, 104, 0.1);
}

.sidebar-bottom {
    margin-top: auto;
    border-top: 1px solid #5a4b44;
    padding-top: 20px;
}

.exit,
.logout {
    color: #aaa;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;
    text-decoration: none !important;
}

/* ================= MAIN CONTENT ================= */
.content {
    flex: 1;
    padding: 40px 60px;
    min-width: 0;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 30px;
}

.header h1 {
    color: #3e332e;
    font-size: 28px;
    margin-bottom: 8px;
}

.gold {
    color: #d1aa68;
}

.header p {
    color: #777;
    font-size: 14px;
    margin: 0;
}

.btn-add {
    background-color: #3e332e;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: 0.3s;
    display: flex;
    align-items: center;
    gap: 8px;
}

.btn-add:hover {
    background-color: #d1aa68;
}

/* ================= BẢNG DỮ LIỆU ================= */
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
    padding: 16px 24px;
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

.empty-msg {
    text-align: center;
    padding: 30px;
    color: #999;
}

/* Các nút hành động dạng khối */
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

.form-group input {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 15px;
}

.form-group input:focus {
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