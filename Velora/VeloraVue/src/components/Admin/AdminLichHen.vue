<template>
    <div class="admin-wrapper">
        <!-- SIDEBAR -->
        <nav class="sidebar">
            <h2 class="brand">VELORA ADMIN</h2>
            <ul class="menu">
                <li v-for="item in filteredMenuItems" :key="item.name">
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

        <!-- CONTENT -->
        <main class="content">
            <header class="header">
                <div>
                    <h1>Quản lý <span class="gold">Lịch hẹn</span></h1>
                    <p>Theo dõi và phản hồi yêu cầu xem sản phẩm từ khách hàng.</p>
                </div>
            </header>

            <!-- BỘ LỌC & TÌM KIẾM -->
            <div class="filter-wrapper">
                <div class="search-box">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên khách hàng hoặc SĐT..." />
                </div>
                <button style="margin-left: auto;" class="btn-add" @click="fetchLichHen">
                    <i class="fa-solid fa-rotate-right"></i> Làm mới
                </button>
            </div>

            <!-- BẢNG DỮ LIỆU -->
            <div class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Khách Hàng</th>
                            <th>SĐT</th>
                            <th>Sản Phẩm Yêu Cầu</th>
                            <th>Thời Gian Hẹn</th>
                            <th>Trạng Thái</th>
                            <th>Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in filteredLichHen" :key="item.id">
                            <td>#{{ item.id }}</td>
                            <td class="category-title">{{ item.tenKhachHang || 'N/A' }}</td>
                            <td>{{ item.soDienThoai || 'N/A' }}</td>
                            <td class="category-desc">
                                {{ item.sanPham ? item.sanPham.tenSanPham : 'Sản phẩm không xác định' }}
                            </td>
                            <td>
                                <strong>{{ formatDate(item.ngayHen) }}</strong><br />
                                <small style="color: #888">{{ item.thoiGian || '' }}</small>
                            </td>
                            <td>
                                <span :class="['status-badge', getStatusClass(item.trangThai)]">
                                    {{ getStatusText(item.trangThai) }}
                                </span>
                            </td>
                            <td>
                                <div class="actions">
                                    <button class="btn-action edit" title="Cập nhật trạng thái" @click="openEditModal(item)">
                                        <i class="fa-solid fa-pen"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="filteredLichHen.length === 0">
                            <td colspan="7" class="empty-state">Không tìm thấy dữ liệu lịch hẹn nào.</td>
                        </tr>
                    </tbody>
                </table>
            </div>
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
                        <select v-model="selectedLichHen.trangThai" class="status-select">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

// ================= LOGIC SIDEBAR & QUYỀN =================
const userRole = computed(() => {
    try {
        const userStr = localStorage.getItem('user'); 
        if (userStr && userStr !== 'undefined') {
            const user = JSON.parse(userStr);
            return user?.vaiTro || ''; 
        }
    } catch (e) {
        console.error("Lỗi parse user:", e);
    }
    return '';
});

const allMenuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge', roles: ['ROLE_ADMIN'] },
    { name: 'Tư Vấn Khách Hàng', link: '/admin/tu-van-khach-hang', icon: 'fa-solid fa-comments', roles: ['ROLE_CHUYEN_VIEN_TU_VAN'] },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked', roles: ['ROLE_ADMIN'] },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem', roles: ['ROLE_ADMIN'] },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check', roles: ['ROLE_ADMIN', 'ROLE_CHUYEN_VIEN_TU_VAN'] }, 
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] }
];

const filteredMenuItems = computed(() => {
    if (!userRole.value) return [];
    
    return allMenuItems.filter(item => item.roles.includes(userRole.value));
});

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/dang-nhap';
};

// ================= LOGIC DỮ LIỆU LỊCH HẸN =================
const danhSachLichHen = ref([]);
const searchQuery = ref('');

const fetchLichHen = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/lich-hen/admin/danh-sach');
        if (response.ok) {
            const data = await response.json();
            // An toàn: Kiểm tra nếu data là mảng mới gọi hàm sort
            if (Array.isArray(data)) {
                danhSachLichHen.value = data.sort((a, b) => b.id - a.id);
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

const filteredLichHen = computed(() => {
    if (!Array.isArray(danhSachLichHen.value)) return [];
    if (!searchQuery.value) return danhSachLichHen.value;
    
    const lowerQuery = searchQuery.value.toLowerCase();
    return danhSachLichHen.value.filter(item => {
        const ten = item.tenKhachHang ? item.tenKhachHang.toLowerCase() : '';
        const sdt = item.soDienThoai ? item.soDienThoai : '';
        return ten.includes(lowerQuery) || sdt.includes(lowerQuery);
    });
});

// ================= LOGIC MODAL & TRẠNG THÁI =================
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

// ================= TIỆN ÍCH HIỂN THỊ =================
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
        case 0: return 'status-warning';
        case 1: return 'status-info';
        case 2: return 'status-success';
        case 3: return 'status-danger';
        default: return '';
    }
};
</script>

<style scoped>
@import "../CSS/Admin/AdminLichHen.css";
</style>