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
            <!-- ĐÃ BỔ SUNG: Chân trang Sidebar chứa nút Return và Logout -->
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
                    <p>Phân loại các tuyệt tác thời gian và phụ kiện cao cấp.</p>
                </div>
                <div class="header-right">
                    <button class="btn-add" @click="openAddModal">
                        <i class="fa-solid fa-plus"></i> Thêm Loại Mới
                    </button>
                </div>
            </header>

            <section class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên Loại Sản Phẩm</th>
                            <th>Mô Tả Chi Tiết</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="cat in categories" :key="cat.maLoai">
                            <td>#{{ cat.maLoai }}</td>
                            <td class="cat-name">{{ cat.tenLoai }}</td>
                            <td>{{ cat.moTa || 'Chưa có mô tả' }}</td>
                            <td class="actions">
                                <button class="btn-action edit" @click="openEditModal(cat)">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button class="btn-action delete" @click="deleteCategory(cat.maLoai)">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="categories.length === 0">
                            <td colspan="4" class="empty-state">Đang tải dữ liệu hoặc chưa có loại sản phẩm nào...</td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </main>

        <div v-if="showModal" class="modal-overlay">
            <div class="modal-box">
                <div class="modal-header">
                    <h3>{{ isEditMode ? 'Cập Nhật Loại' : 'Thêm Loại Sản Phẩm' }}</h3>
                    <button class="close-btn" @click="closeModal">&times;</button>
                </div>
                <form @submit.prevent="saveCategory">
                    <div class="form-group">
                        <label>Tên loại sản phẩm *</label>
                        <input type="text" v-model="form.tenLoai" required placeholder="Ví dụ: Chronograph, Moonphase..." />
                    </div>
                    <div class="form-group">
                        <label>Mô tả ngắn</label>
                        <textarea v-model="form.moTa" rows="3" placeholder="Nhập mô tả về đặc điểm của loại này..."></textarea>
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
import { ref, onMounted } from 'vue';

const API_URL = 'http://localhost:8080/api/loai-san-pham';

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
];

const categories = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const currentId = ref(null);

const form = ref({
    tenLoai: '',
    moTa: ''
});

const loadCategories = async () => {
    try {
        const res = await fetch(API_URL);
        if (res.ok) categories.value = await res.json();
    } catch (err) { console.error(err); }
};

const openAddModal = () => {
    isEditMode.value = false;
    form.value = { tenLoai: '', moTa: '' };
    showModal.value = true;
};

const openEditModal = (cat) => {
    isEditMode.value = true;
    currentId.value = cat.maLoai;
    form.value = { ...cat };
    showModal.value = true;
};

const closeModal = () => showModal.value = false;

const saveCategory = async () => {
    const method = isEditMode.value ? 'PUT' : 'POST';
    const url = isEditMode.value ? `${API_URL}/${currentId.value}` : API_URL;

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(form.value)
        });
        if (res.ok) {
            alert('Thành công!');
            closeModal();
            loadCategories();
        }
    } catch (err) { alert('Lỗi lưu dữ liệu'); }
};

const deleteCategory = async (id) => {
    if (!confirm('Xóa loại này có thể ảnh hưởng đến sản phẩm thuộc loại này. Bạn chắc chứ?')) return;
    try {
        const res = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        if (res.ok) {
            alert('Đã xóa!');
            loadCategories();
        }
    } catch (err) { console.error(err); }
};

// ĐÃ BỔ SUNG: Logic xử lý sự kiện đăng xuất đồng bộ
const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

onMounted(loadCategories);
</script>

<style scoped>
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }

/* ĐÃ SỬA: Thêm thuộc tính flex-direction để dồn nút xuống đáy */
.sidebar { width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; flex-shrink: 0; display: flex; flex-direction: column; }

.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { color: #ccc; text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 10px; transition: 0.3s; border-radius: 6px; }
.menu a:hover, .menu a.active { color: #d1aa68; background: rgba(209, 170, 104, 0.1); }

/* ĐÃ BỔ SUNG: Toàn bộ style cho chân sidebar */
.sidebar-bottom {
    margin-top: auto;
    border-top: 1px solid #5a4b44;
    padding-top: 20px;
}
.exit, .logout {
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
.exit:hover, .logout:hover {
    color: #d1aa68;
}

.content { flex: 1; padding: 60px; }
.gold { color: #d1aa68; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.btn-add { background: #d1aa68; color: #111; border: none; padding: 12px 24px; font-weight: bold; cursor: pointer; border-radius: 4px; }
.table-container { background: #fff; border: 1px solid #e0dcd5; border-radius: 8px; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th { background: #fcfbf9; padding: 18px 20px; text-align: left; border-bottom: 2px solid #e0dcd5; font-size: 13px; }
.admin-table td { padding: 15px 20px; border-bottom: 1px solid #f0efeb; font-size: 14px; }
.cat-name { font-weight: bold; color: #3e332e; }
.actions { display: flex; gap: 10px; }
.btn-action { border: none; width: 32px; height: 32px; cursor: pointer; border-radius: 4px; }
.btn-action.edit { background: #f4f1ea; color: #3e332e; }
.btn-action.delete { background: #ffebee; color: #c62828; }

/* Modal Style */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-box { background: #fff; padding: 30px; border-radius: 8px; width: 450px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 13px; }
.form-group input, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ccbfb5; border-radius: 4px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-cancel { background: #eee; border: none; padding: 10px 20px; cursor: pointer; }
.btn-submit { background: #3e332e; color: #fff; border: none; padding: 10px 20px; cursor: pointer; }
</style>