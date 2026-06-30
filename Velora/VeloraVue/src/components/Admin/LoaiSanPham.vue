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
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags' }
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
/* Đồng bộ hoàn toàn UI với trang quản lý sản phẩm */
.admin-wrapper {
    display: flex;
    min-height: 100vh;
    background: #f4f1ea;
    font-family: sans-serif;
}

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

.content {
    flex: 1;
    padding: 40px 60px;
    min-width: 0;
}

.gold {
    color: #d1aa68;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
}

.header h1 {
    color: #3e332e;
    font-size: 32px;
    margin-bottom: 5px;
}

.header p {
    color: #888;
    font-size: 14px;
}

.filter-wrapper {
    display: flex;
    margin-bottom: 25px;
    background: #fff;
    padding: 15px 20px;
    border: 1px solid #e0dcd5;
    border-radius: 8px;
}

.search-box {
    display: flex;
    align-items: center;
    gap: 10px;
    background: #fcfbf9;
    border: 1px solid #ccbfb5;
    padding: 10px 15px;
    border-radius: 6px;
    flex: 1;
    max-width: 400px;
}

.search-box input {
    width: 100%;
    border: none;
    padding: 0;
    background: transparent;
}

.search-box input:focus {
    outline: none;
}

.search-box i {
    color: #888;
}

.btn-add {
    background-color: #d1aa68;
    color: #111;
    border: none;
    padding: 12px 24px;
    font-weight: bold;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    gap: 8px;
}

.btn-add:hover {
    background-color: #b8955b;
    transform: translateY(-2px);
}

.table-container {
    background: #ffffff;
    border: 1px solid #e0dcd5;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.02);
}

.admin-table {
    width: 100%;
    border-collapse: collapse;
    text-align: left;
}

.admin-table th {
    background-color: #fcfbf9;
    color: #3e332e;
    padding: 18px 20px;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 2px solid #e0dcd5;
}

.admin-table td {
    padding: 15px 20px;
    border-bottom: 1px solid #f0efeb;
    vertical-align: middle;
    color: #555;
    font-size: 14px;
}

.admin-table tbody tr:hover {
    background-color: #fdfaf5;
}

.category-title {
    font-weight: bold;
    color: #3e332e;
}

.category-desc {
    color: #666;
    max-width: 400px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.actions {
    display: flex;
    gap: 10px;
}

.btn-action {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.btn-action.edit {
    background-color: #f4f1ea;
    color: #3e332e;
}

.btn-action.edit:hover {
    background-color: #d1aa68;
    color: #fff;
}

.btn-action.delete {
    background-color: #ffebee;
    color: #c62828;
}

.btn-action.delete:hover {
    background-color: #c62828;
    color: #fff;
}

.empty-state {
    text-align: center;
    padding: 40px !important;
    color: #888;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
}

.modal-box {
    background: #fff;
    padding: 30px;
    border-radius: 8px;
    width: 450px;
    max-width: 90%;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #e0dcd5;
    padding-bottom: 15px;
    margin-bottom: 20px;
}

.modal-header h3 {
    color: #3e332e;
    margin: 0;
    font-size: 20px;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #888;
}

.form-group {
    margin-bottom: 15px;
}

label {
    display: block;
    margin-bottom: 6px;
    font-size: 13px;
    font-weight: bold;
    color: #555;
}

input,
textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccbfb5;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 14px;
    font-family: sans-serif;
}

input:focus,
textarea:focus {
    border-color: #d1aa68;
    outline: none;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 25px;
}

.btn-cancel {
    background: #f4f1ea;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
    color: #555;
}

.btn-submit {
    background: #3e332e;
    color: #fff;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
}

.btn-submit:hover {
    background: #52433d;
}
</style>