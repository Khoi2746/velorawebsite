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
                    <h1>Quản Lý <span class="gold">Loại Sản Phẩm</span></h1>
                    <p>Danh sách các phân loại đặc tính cơ khí của đồng hồ.</p>
                </div>
                <div class="header-right">
                    <button class="btn-add" @click="openAddModal">
                        <i class="fa-solid fa-plus"></i> Thêm Loại Mới
                    </button>
                </div>
            </header>

            <section class="filter-wrapper">
                <div class="search-box">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên loại sản phẩm..." />
                </div>
            </section>

            <section class="table-container">
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
                        <tr v-for="category in filteredCategories" :key="category.maLoai">
                            <td>#{{ category.maLoai }}</td>
                            <td class="category-title">{{ category.tenLoai }}</td>
                            <td class="category-desc">{{ category.moTa || 'Chưa có mô tả' }}</td>
                            <td class="actions">
                                <button class="btn-action edit" @click="openEditModal(category)" title="Chỉnh sửa">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button class="btn-action delete" @click="deleteCategory(category.maLoai)" title="Xóa">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="filteredCategories.length === 0">
                            <td colspan="4" class="empty-state">Không tìm thấy loại sản phẩm nào phù hợp.</td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </main>

        <div v-if="showModal" class="modal-overlay">
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
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const CAT_API_URL = 'http://localhost:8080/api/loai-san-pham'; 

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

const categories = ref([]);
const searchQuery = ref('');
const showModal = ref(false);
const isEditMode = ref(false);
const currentCategoryId = ref(null);

const defaultForm = {
    tenLoai: '',
    moTa: ''
};
const form = ref({ ...defaultForm });

// Tính toán tìm kiếm thời gian thực
const filteredCategories = computed(() => {
    return categories.value.filter(cat => {
        return cat.tenLoai.toLowerCase().includes(searchQuery.value.toLowerCase());
    });
});

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
    // Nhận diện mã loại (gốc tương đương Long id bên Java)
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

        // Tạo cục JSON thuần khớp cấu trúc Entity LoaiSanPham trong Spring Boot
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
            alert(isEditMode.value ? 'Cập nhật loại sản phẩm thành công!' : 'Thêm loại sản phẩm mới thành công!');
            closeModal();
            loadCategories();
        } else {
            const errorText = await res.text();
            alert(`Lỗi hệ thống: ${errorText || 'Không thể lưu, vui lòng kiểm tra lại dữ liệu.'}`);
        }
    } catch (error) {
        console.error('Lỗi khi gửi dữ liệu loại sản phẩm:', error);
    }
};

const deleteCategory = async (id) => {
    if (confirm(`Bạn chắc chắn muốn xóa loại sản phẩm #${id}? Thao tác này sẽ mất dữ liệu liên kết!`)) {
        try {
            const res = await fetch(`${CAT_API_URL}/${id}`, {
                method: 'DELETE'
            });
            if (res.ok) {
                alert('Xóa loại sản phẩm thành công!');
                loadCategories();
            } else {
                alert('Xóa thất bại! Loại sản phẩm này có thể đang được sử dụng ở bảng Sản Phẩm.');
            }
        } catch (error) {
            console.error('Lỗi xóa loại sản phẩm:', error);
        }
    }
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

onMounted(() => {
    loadCategories();
});
</script>

<style scoped>
@import "../CSS/Admin/LoaiSanPham.css";
</style>