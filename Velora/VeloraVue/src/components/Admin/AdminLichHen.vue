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
            <div class="filter-wrapper custom-filter-layout">
                <!-- Tìm kiếm Text -->
                <div class="search-box custom-search">
                    <i class="fa-solid fa-magnifying-glass"></i>
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

                <button class="btn-add custom-btn" @click="resetFilters">
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
                            
                            <!-- CỘT HIỂN THỊ SẢN PHẨM -->
                            <td>
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <!-- Ảnh sản phẩm -->
                                    <img 
                                        v-if="item.sanPham?.hinhAnh || item.hinhAnhSanPham" 
                                        :src="item.sanPham?.hinhAnh || item.hinhAnhSanPham" 
                                        alt="Product" 
                                        style="width: 45px; height: 45px; object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb;"
                                    />
                                    <!-- Hiển thị icon hộp quà nếu sản phẩm không có ảnh -->
                                    <div v-else style="width: 45px; height: 45px; background: #f3f4f6; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: #9ca3af;">
                                        <i class="fa-solid fa-box"></i>
                                    </div>
                                    
                                    <!-- Tên sản phẩm & Giá -->
                                    <div style="display: flex; flex-direction: column; text-align: left;">
                                        <span style="font-weight: 600; color: #374151; font-size: 14px;">
                                            {{ item.sanPham?.tenSanPham || item.tenSanPham || 'Không xác định' }}
                                        </span>
                                        <small v-if="item.sanPham?.gia || item.giaSanPham" style="color: #d4af37; font-weight: 500;">
                                            {{ (item.sanPham?.gia || item.giaSanPham).toLocaleString('vi-VN') }} đ
                                        </small>
                                    </div>
                                </div>
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
                                <div class="actions" style="display: flex; gap: 8px; justify-content: center;">
                                    <!-- Nút Xác nhận -->
                                    <button 
                                        v-if="item.trangThai === 0" 
                                        class="btn-action confirm-btn" 
                                        title="Xác nhận lịch hẹn" 
                                        @click="quickUpdateStatus(item.id, 1)"
                                    >
                                        <i class="fa-solid fa-check"></i>
                                    </button>

                                    <!-- Nút Hoàn thành -->
                                    <button 
                                        v-if="item.trangThai === 1" 
                                        class="btn-action complete-btn" 
                                        title="Đã hoàn thành" 
                                        @click="quickUpdateStatus(item.id, 2)"
                                    >
                                        <i class="fa-solid fa-check-double"></i>
                                    </button>

                                    <!-- Nút Từ chối/Hủy -->
                                    <button 
                                        v-if="item.trangThai === 0 || item.trangThai === 1" 
                                        class="btn-action reject-btn" 
                                        title="Từ chối lịch hẹn" 
                                        @click="quickUpdateStatus(item.id, 3)"
                                    >
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>

                                    <!-- Nút Chỉnh sửa -->
                                    <button class="btn-action edit" title="Chi tiết / Cập nhật" @click="openEditModal(item)">
                                        <i class="fa-solid fa-pen"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="filteredLichHen.length === 0">
                            <td colspan="7" class="empty-state">Không tìm thấy dữ liệu phù hợp với bộ lọc.</td>
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
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
];

const filteredMenuItems = computed(() => {
    if (!userRole.value) return [];
    return allMenuItems.filter(item => item.roles.includes(userRole.value));
});

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/dang-nhap';
};

// ================= LOGIC DỮ LIỆU LỊCH HẸN & BỘ LỌC =================
const danhSachLichHen = ref([]);
const searchQuery = ref('');
const filterStatus = ref('');
const filterDate = ref('');
const filterTime = ref('');

const fetchLichHen = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/lich-hen/admin/danh-sach');
        if (response.ok) {
            const data = await response.json();
            
            // THÊM DÒNG NÀY VÀO ĐỂ XEM DATA BACKEND TRẢ VỀ LÀ GÌ
            console.log("Dữ liệu Lịch Hẹn từ API:", data[0]); 
            
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

const resetFilters = () => {
    searchQuery.value = '';
    filterStatus.value = '';
    filterDate.value = '';
    filterTime.value = '';
    fetchLichHen();
};

const filteredLichHen = computed(() => {
    if (!Array.isArray(danhSachLichHen.value)) return [];
    
    return danhSachLichHen.value.filter(item => {
        // 1. Lọc theo Từ khóa
        const lowerQuery = searchQuery.value.toLowerCase();
        const ten = item.tenKhachHang ? item.tenKhachHang.toLowerCase() : '';
        const sdt = item.soDienThoai ? item.soDienThoai : '';
        const matchSearch = !searchQuery.value || ten.includes(lowerQuery) || sdt.includes(lowerQuery);

        // 2. Lọc theo Trạng thái
        const matchStatus = filterStatus.value === '' || item.trangThai === parseInt(filterStatus.value);

        // 3. Lọc theo Ngày
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

        // 4. Lọc theo Khung giờ
        let matchTime = true;
        if (filterTime.value) {
            matchTime = item.thoiGian === filterTime.value;
        }

        return matchSearch && matchStatus && matchDate && matchTime;
    });
});

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