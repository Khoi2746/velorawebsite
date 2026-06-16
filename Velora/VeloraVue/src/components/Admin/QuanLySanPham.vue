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
                    <h1>Quản Lý <span class="gold">Sản Phẩm</span></h1>
                    <p>Danh sách các cỗ máy thời gian hiện có trong hệ thống.</p>
                </div>
                <div class="header-right">
                    <button class="btn-add" @click="openAddModal">
                        <i class="fa-solid fa-plus"></i> Thêm Sản Phẩm Mới
                    </button>
                </div>
            </header>

            <section class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Hình Ảnh</th>
                            <th>Tên Sản Phẩm</th>
                            <th>Giá Bán</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="product in products" :key="product.maSanPham">
                            <td>#{{ product.maSanPham }}</td>
                            <td>
                                <div class="img-wrapper">
                                    <img :src="getImageUrl(product.anhDaiDien)" :alt="product.tenSanPham" />
                                </div>
                            </td>
                            <td class="product-name">{{ product.tenSanPham }}</td>
                            <td class="price">{{ formatPrice(product.giaBan) }}</td>
                            <td>
                                <span class="status-badge"
                                    :class="product.trangThai === 'CON_HANG' ? 'in-stock' : 'out-stock'">
                                    {{ product.trangThai === 'CON_HANG' ? 'Còn Hàng' : 'Hết Hàng' }}
                                </span>
                            </td>
                            <td class="actions">
                                <button class="btn-action edit" title="Chỉnh sửa" @click="openEditModal(product)">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button class="btn-action delete" title="Xóa" @click="deleteProduct(product.maSanPham)">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="products.length === 0">
                            <td colspan="6" class="empty-state">Đang tải dữ liệu hoặc kho hàng trống...</td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </main>

        <div v-if="showModal" class="modal-overlay">
            <div class="modal-box">
                <div class="modal-header">
                    <h3>{{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}</h3>
                    <button class="close-btn" @click="closeModal">&times;</button>
                </div>
                <form @submit.prevent="saveProduct">
                    <div class="form-group">
                        <label>Tên sản phẩm *</label>
                        <input type="text" v-model="form.tenSanPham" required placeholder="Ví dụ: Rolex Cosmograph" />
                    </div>
                    <div class="form-group">
                        <label>Giá bán (VNĐ) *</label>
                        <input type="number" v-model.number="form.giaBan" required min="0" />
                    </div>
                    <div class="form-group">
                        <label>Tên file hình ảnh</label>
                        <input type="text" v-model="form.anhDaiDien" placeholder="Ví dụ: rolex.png hoặc đường dẫn URL" />
                    </div>
                    <div class="form-group">
                        <label>Trạng thái</label>
                        <select v-model="form.trangThai">
                            <option value="CON_HANG">Còn Hàng</option>
                            <option value="HET_HANG">Hết Hàng</option>
                        </select>
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

const API_URL = 'http://localhost:8080/api/san-pham';

// Menu Sidebar
const menuItems = [
    { name: 'Dashboard', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Products', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Users', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Orders', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' },
    { name: 'Inventory', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked' },
];

const products = ref([]);

// State quản lý Modal và Dữ liệu Form
const showModal = ref(false);
const isEditMode = ref(false);
const currentProductId = ref(null);

const defaultForm = {
    tenSanPham: '',
    giaBan: 0,
    anhDaiDien: '',
    trangThai: 'CON_HANG'
};
const form = ref({ ...defaultForm });

// Xử lý link ảnh
const getImageUrl = (img) => {
    if (!img) return '/img/default-watch.png';
    return img.startsWith('http') ? img : `/img/${img}`;
};

// Định dạng tiền tệ VNĐ
const formatPrice = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// [READ] Tải danh sách sản phẩm
const loadProducts = async () => {
    try {
        const res = await fetch(API_URL);
        if (res.ok) {
            products.value = await res.json();
        }
    } catch (error) {
        console.error('Lỗi kết nối Backend:', error);
    }
};

// Mở modal để Thêm mới
const openAddModal = () => {
    isEditMode.value = false;
    currentProductId.value = null;
    form.value = { ...defaultForm };
    showModal.value = true;
};

// Mở modal để Chỉnh sửa (Gán data cũ vào form)
const openEditModal = (product) => {
    isEditMode.value = true;
    currentProductId.value = product.maSanPham;
    form.value = { ...product }; // clone object tránh thay đổi trực tiếp trên table trước khi bấm lưu
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
};

// [CREATE & UPDATE] Lưu dữ liệu từ form
const saveProduct = async () => {
    try {
        let url = API_URL;
        let method = 'POST';

        // Tạo bản sao dữ liệu gửi đi
        const dataToSend = { ...form.value };

        // Nếu ở chế độ edit, đổi method thành PUT và chỉ định ID cụ thể
        if (isEditMode.value) {
            url = `${API_URL}/${currentProductId.value}`;
            method = 'PUT';
            delete dataToSend.maSanPham; // Xóa ID khỏi body khi cập nhật
        }

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dataToSend)
        });

        if (res.ok) {
            alert(isEditMode.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!');
            closeModal();
            loadProducts(); // Load lại bảng sau khi thay đổi dữ liệu thành công
        } else {
            const errorText = await res.text();
            alert(`Có lỗi xảy ra: ${errorText || 'Vui lòng kiểm tra lại dữ liệu.'}`);
        }
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error);
    }
};

// [DELETE] Xóa sản phẩm theo ID
const deleteProduct = async (id) => {
    if (confirm(`Bạn chắc chắn muốn xóa sản phẩm #${id}? Hành động này không thể hoàn tác.`)) {
        try {
            const res = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            });
            if (res.ok) {
                alert('Xóa sản phẩm thành công!');
                loadProducts(); // Cập nhật lại danh sách trực quan
            } else {
                alert('Xóa thất bại. Sản phẩm có thể đang vướng đơn hàng!');
            }
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
        }
    }
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

onMounted(() => {
    loadProducts();
});
</script>

<style scoped>
/* ================= CSS CŨ CỦA BẠN (GIỮ NGUYÊN) ================= */
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }
.sidebar { width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; display: flex; flex-direction: column; flex-shrink: 0; }
.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { color: #ccc; text-decoration: none !important; display: flex; align-items: center; gap: 10px; transition: 0.3s; padding: 10px; border-radius: 6px; }
.menu a:hover, .menu a.active { color: #d1aa68; background-color: rgba(209, 170, 104, 0.1); }
.sidebar-bottom { margin-top: auto; border-top: 1px solid #5a4b44; padding-top: 20px; }
.exit, .logout { color: #aaa; background: none; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 10px; margin-bottom: 15px; text-decoration: none !important; }
.content { flex: 1; padding: 60px; min-width: 0; }
.gold { color: #d1aa68; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.header h1 { color: #3e332e; font-size: 32px; margin-bottom: 5px; }
.header p { color: #888; font-size: 14px; }
.btn-add { background-color: #d1aa68; color: #111; border: none; padding: 12px 24px; font-weight: bold; border-radius: 6px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 8px; }
.btn-add:hover { background-color: #b8955b; transform: translateY(-2px); }
.table-container { background: #ffffff; border: 1px solid #e0dcd5; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.02); }
.admin-table { width: 100%; border-collapse: collapse; text-align: left; }
.admin-table th { background-color: #fcfbf9; color: #3e332e; padding: 18px 20px; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 2px solid #e0dcd5; }
.admin-table td { padding: 15px 20px; border-bottom: 1px solid #f0efeb; vertical-align: middle; color: #555; font-size: 14px; }
.admin-table tbody tr:hover { background-color: #fdfaf5; }
.img-wrapper { width: 50px; height: 50px; background: #f4f1ea; border-radius: 4px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.img-wrapper img { max-width: 100%; max-height: 100%; object-fit: contain; }
.product-name { font-weight: bold; color: #3e332e; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price { font-weight: bold; color: #d1aa68; }
.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 11px; font-weight: bold; text-transform: uppercase; }
.in-stock { background-color: #e8f5e9; color: #2e7d32; }
.out-stock { background-color: #ffebee; color: #c62828; }
.actions { display: flex; gap: 10px; }
.btn-action { width: 32px; height: 32px; border: none; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.btn-action.edit { background-color: #f4f1ea; color: #3e332e; }
.btn-action.edit:hover { background-color: #d1aa68; color: #fff; }
.btn-action.delete { background-color: #ffebee; color: #c62828; }
.btn-action.delete:hover { background-color: #c62828; color: #fff; }
.empty-state { text-align: center; padding: 40px !important; color: #888; }

/* ================= BỔ SUNG CSS CHO PHẦN DIALOG MODAL CRUD ================= */
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
    width: 500px;
    max-width: 90%;
    box-shadow: 0 10px 25px rgba(0,0,0,0.15);
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

input, select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccbfb5;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 14px;
}

input:focus, select:focus {
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