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
                    <p>Danh sách các cỗ máy thời gian hiện có trong kho.</p>
                </div>
                <div class="header-right">
                    <button class="btn-add"><i class="fa-solid fa-plus"></i> Thêm Sản Phẩm Mới</button>
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
                            <th>Tồn Kho</th>
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
                            <td>{{ product.soLuongTonKho }}</td>
                            <td>
                                <span class="status-badge"
                                    :class="product.trangThai === 'CON_HANG' ? 'in-stock' : 'out-stock'">
                                    {{ product.trangThai === 'CON_HANG' ? 'Còn Hàng' : 'Hết Hàng' }}
                                </span>
                            </td>
                            <td class="actions">
                                <button class="btn-action edit" title="Chỉnh sửa"><i
                                        class="fa-solid fa-pen"></i></button>
                                <button class="btn-action delete" title="Xóa"><i class="fa-solid fa-trash"></i></button>
                            </td>
                        </tr>
                        <tr v-if="products.length === 0">
                            <td colspan="7" class="empty-state">Đang tải dữ liệu hoặc kho hàng trống...</td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </main>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// Menu Sidebar
const menuItems = [
    { name: 'Dashboard', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Products', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Users', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Orders', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' }
];

const products = ref([]);

// Xử lý link ảnh (hỗ trợ cả link ngoài HTTP và ảnh Local)
const getImageUrl = (img) => {
    if (!img) return '/img/default-watch.png';
    return img.startsWith('http') ? img : `/img/${img}`;
};

// Định dạng tiền tệ VNĐ
const formatPrice = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Gọi API lấy dữ liệu sản phẩm từ Spring Boot
const loadProducts = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/san-pham');
        if (res.ok) {
            products.value = await res.json();
        }
    } catch (error) {
        console.error('Lỗi kết nối Backend:', error);
    }
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

// Chạy tự động khi trang vừa load
onMounted(() => {
    loadProducts();
});
</script>

<style scoped>
/* ================= LAYOUT GỐC (GIỮ NGUYÊN) ================= */
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
    padding: 60px;
    min-width: 0;
}

.gold {
    color: #d1aa68;
}

/* ================= TRANG QUẢN LÝ SẢN PHẨM ================= */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40px;
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

/* ================= BẢNG DỮ LIỆU (TABLE) ================= */
.table-container {
    background: #ffffff;
    border: 1px solid #e0dcd5;
    border-radius: 8px;
    overflow: hidden;
    /* Bo góc mượt mà */
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
    /* Sáng lên nhẹ khi rê chuột */
}

/* Cột hình ảnh */
.img-wrapper {
    width: 50px;
    height: 50px;
    background: #f4f1ea;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.img-wrapper img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.product-name {
    font-weight: bold;
    color: #3e332e;
    max-width: 250px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    /* Cắt chữ quá dài thành dấu ... */
}

.price {
    font-weight: bold;
    color: #d1aa68;
}

/* Badge Trạng thái */
.status-badge {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: bold;
    text-transform: uppercase;
}

.in-stock {
    background-color: #e8f5e9;
    color: #2e7d32;
}

.out-stock {
    background-color: #ffebee;
    color: #c62828;
}

/* Các nút hành động */
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
</style>