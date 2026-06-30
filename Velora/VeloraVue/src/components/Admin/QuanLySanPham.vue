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

            <section class="filter-wrapper">
                <div class="search-box">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <!-- Cập nhật placeholder tại đây -->
                    <input type="text" :value="searchQuery" @input="searchQuery = $event.target.value" placeholder="Tìm kiếm theo mã ID hoặc tên sản phẩm..." />
                </div>
                <div class="filter-boxes">
                    <select v-model="filterDanhMuc">
                        <option value="">-- Tất cả danh mục --</option>
                        <option v-for="dm in mainCategories" :key="dm.maDanhMuc" :value="dm.maDanhMuc">
                            {{ dm.tenDanhMuc }}
                        </option>
                    </select>

                    <select v-model="filterTrangThai">
                        <option value="">-- Tất cả trạng thái --</option>
                        <option value="CON_HANG">Còn Hàng</option>
                        <option value="HET_HANG">Hết Hàng</option>
                    </select>
                </div>
            </section>

            <section class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Hình Ảnh</th>
                            <th>Tên Sản Phẩm</th>
                            <th>Danh Mục</th>
                            <th>Loại Sản Phẩm</th>
                            <th>Giá Bán</th>
                            <th>Tồn Kho</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="product in filteredProducts" :key="product.maSanPham">
                            <td>#{{ product.maSanPham }}</td>
                            <td>
                                <div class="img-wrapper">
                                    <img :src="getImageUrl(product.anhDaiDien)" :alt="product.tenSanPham" />
                                </div>
                            </td>
                            <td class="product-name">{{ product.tenSanPham }}</td>

                            <td>
                                {{ product.danhMuc ? product.danhMuc.tenDanhMuc : 'Chưa chọn' }}
                            </td>

                            <td class="category-name">
                                {{ product.loaiSanPham ? product.loaiSanPham.tenLoai : 'Không có' }}
                            </td>

                            <td class="price">{{ formatPrice(product.giaBan) }}</td>

                            <td style="font-weight: bold; color: #3e332e;">
                                {{ product.soLuongTonKho != null ? product.soLuongTonKho : 0 }}
                            </td>

                            <td>
                                <span class="status-badge"
                                    :class="product.trangThai === 'CON_HANG' ? 'in-stock' : 'out-stock'">
                                    {{ product.trangThai === 'CON_HANG' ? 'Còn Hàng' : 'Hết Hàng' }}
                                </span>
                            </td>

                            <td class="actions">
                                <button class="btn-action edit" @click="openEditModal(product)" title="Chỉnh sửa">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button class="btn-action delete" @click="deleteProduct(product.maSanPham)" title="Xóa">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="filteredProducts.length === 0">
                            <td colspan="9" class="empty-state">Không tìm thấy sản phẩm nào phù hợp.</td>
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
                        <label>Danh mục chính *</label>
                        <select v-model="form.maDanhMucSelected" required>
                            <option value="">-- Chọn danh mục chính --</option>
                            <option v-for="dm in mainCategories" :key="dm.maDanhMuc" :value="dm.maDanhMuc">
                                {{ dm.tenDanhMuc }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Loại sản phẩm (Đặc tính cơ khí)</label>
                        <select v-model="form.maLoaiSelected">
                            <option value="">-- Không có / Bỏ chọn đặc tính cơ khí --</option>
                            <option v-for="cat in categories" :key="cat.maLoai" :value="cat.maLoai">
                                {{ cat.tenLoai }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Giá bán (VNĐ) *</label>
                        <input type="number" v-model.number="form.giaBan" required min="0" />
                    </div>

                    <div class="form-group">
                        <label>Hình ảnh sản phẩm *</label>
                        <input type="file" accept="image/*" @change="handleFileChange" :required="!isEditMode" />
                        <div v-if="imagePreview" class="file-preview-wrapper">
                            <p>Xem trước:</p>
                            <img :src="imagePreview" class="file-preview-img" />
                            <small class="file-name-text">Tên file sẽ lưu: <b>{{ form.anhDaiDien }}</b></small>
                        </div>
                    </div>

                    <div v-if="isEditMode" class="form-group">
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
import { ref, onMounted, computed } from 'vue';

const API_URL = 'http://localhost:8080/api/san-pham';
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

const products = ref([]);
const categories = ref([]);
const mainCategories = ref([]);

// State cho bộ tìm kiếm và lọc
const searchQuery = ref('');
const filterDanhMuc = ref('');
const filterTrangThai = ref('');

const showModal = ref(false);
const isEditMode = ref(false);
const currentProductId = ref(null);
const imagePreview = ref(''); // Lưu URL preview ảnh

const defaultForm = {
    tenSanPham: '',
    giaBan: 0,
    anhDaiDien: '',
    trangThai: 'CON_HANG', // Thêm mới mặc định vẫn gửi CON_HANG lên DB
    maDanhMucSelected: '',
    maLoaiSelected: ''
};
const form = ref({ ...defaultForm });

// LOGIC TÌM KIẾM VÀ LỌC SẢN PHẨM (COMPUTED CHẠY REAL-TIME)
// LOGIC TÌM KIẾM VÀ LỌC SẢN PHẨM (TÌM REAL-TIME KHÔNG CẦN ENTER)
const filteredProducts = computed(() => {
    // Chỉ chuyển chữ thường, KHÔNG dùng .trim() ở đây để tránh nghẹn khoảng trắng khi đang gõ
    const query = searchQuery.value.toLowerCase();

    return products.value.filter(product => {
        // Nếu không gõ gì thì bỏ qua, hiện hết
        if (!query.trim()) {
            const matchDanhMuc = !filterDanhMuc.value || (product.danhMuc && product.danhMuc.maDanhMuc === Number(filterDanhMuc.value));
            const matchTrangThai = !filterTrangThai.value || product.trangThai === filterTrangThai.value;
            return matchDanhMuc && matchTrangThai;
        }

        // 1. Kiểm tra khớp Tên sản phẩm (dùng .trim() khi so sánh thực tế)
        const matchName = product.tenSanPham ? product.tenSanPham.toLowerCase().includes(query.trim()) : false;
        
        // 2. Kiểm tra khớp Mã ID (maSanPham)
        const matchId = product.maSanPham ? String(product.maSanPham).includes(query.trim()) : false;

        // Các bộ lọc danh mục và trạng thái
        const matchDanhMuc = !filterDanhMuc.value || (product.danhMuc && product.danhMuc.maDanhMuc === Number(filterDanhMuc.value));
        const matchTrangThai = !filterTrangThai.value || product.trangThai === filterTrangThai.value;

        // Thỏa mãn (Tên hoặc ID) và bộ lọc đi kèm
        return (matchName || matchId) && matchDanhMuc && matchTrangThai;
    });
});

// Xử lý sự kiện khi người dùng chọn file hình ảnh từ máy tính
const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        form.value.anhDaiDien = file.name; // Trích xuất tên file (ví dụ: "dongho.png") đưa vào form gửi đi
        imagePreview.value = URL.createObjectURL(file); // Tạo đường dẫn tạm thời để hiển thị xem trước
    }
};

const getImageUrl = (img) => {
    if (!img) return '/img/default-watch.png';
    return img.startsWith('http') ? img : `/img/${img}`;
};

const formatPrice = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const loadProducts = async () => {
    try {
        const res = await fetch(API_URL);
        if (res.ok) {
            products.value = await res.json();
        }
    } catch (error) {
        console.error('Lỗi kết nối Backend sản phẩm:', error);
    }
};

const loadCategories = async () => {
    try {
        const res = await fetch(CAT_API_URL);
        if (res.ok) {
            categories.value = await res.json();
        }
    } catch (error) {
        console.error('Lỗi kết nối Backend loại sản phẩm:', error);
    }
};

const loadMainCategories = () => {
    mainCategories.value = [
        { maDanhMuc: 1, tenDanhMuc: 'Đồng hồ cơ (Mechanical)' },
        { maDanhMuc: 2, tenDanhMuc: 'Đồng hồ pin (Quartz)' },
        { maDanhMuc: 3, tenDanhMuc: 'Đồng hồ thông minh (Smartwatch)' }
    ];
};

const openAddModal = () => {
    isEditMode.value = false;
    currentProductId.value = null;
    form.value = { ...defaultForm };
    imagePreview.value = ''; // Reset ảnh preview
    showModal.value = true;
};

const openEditModal = (product) => {
    isEditMode.value = true;
    currentProductId.value = product.maSanPham;

    form.value = {
        tenSanPham: product.tenSanPham,
        giaBan: product.giaBan,
        anhDaiDien: product.anhDaiDien,
        trangThai: product.trangThai,
        maDanhMucSelected: product.danhMuc ? product.danhMuc.maDanhMuc : '',
        maLoaiSelected: product.loaiSanPham ? product.loaiSanPham.maLoai : ''
    };

    // Nếu sản phẩm đã có tên ảnh cũ, thực hiện hiển thị preview
    imagePreview.value = product.anhDaiDien ? getImageUrl(product.anhDaiDien) : '';
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
};

const saveProduct = async () => {
    try {
        let url = API_URL;
        let method = 'POST';

        const dataToSend = {
            tenSanPham: form.value.tenSanPham,
            giaBan: form.value.giaBan,
            anhDaiDien: form.value.anhDaiDien,
            trangThai: form.value.trangThai,
            danhMuc: form.value.maDanhMucSelected ? { maDanhMuc: Number(form.value.maDanhMucSelected) } : null
        };

        if (form.value.maLoaiSelected) {
            dataToSend.loaiSanPham = {
                maLoai: Number(form.value.maLoaiSelected)
            };
        } else {
            dataToSend.loaiSanPham = null;
        }

        if (isEditMode.value) {
            url = `${API_URL}/${currentProductId.value}`;
            method = 'PUT';
        }

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dataToSend)
        });

        if (res.ok) {
            alert(isEditMode.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!');
            closeModal();
            loadProducts();
        } else {
            const errorText = await res.text();
            alert(`Có lỗi xảy ra: ${errorText || 'Vui lòng kiểm tra lại dữ liệu.'}`);
        }
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error);
    }
};

const deleteProduct = async (id) => {
    if (confirm(`Bạn chắc chắn muốn xóa sản phẩm #${id}? Hành động này không thể hoàn tác.`)) {
        try {
            const res = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            });
            if (res.ok) {
                alert('Xóa sản phẩm thành công!');
                loadProducts();
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
    loadCategories();
    loadMainCategories();
});
</script>

<style scoped>
/* CSS GỐC + CSS BỔ SUNG CHO THANH LỌC VÀ PREVIEW FILE */
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

/* CSS CHO THANH TÌM KIẾM VÀ BỘ LỌC MỚI THÊM */
.filter-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;
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

.filter-boxes {
    display: flex;
    gap: 15px;
}

.filter-boxes select {
    width: 200px;
    background: #fcfbf9;
}

/* CSS CHỌN FILE VÀ PREVIEW ẢNH */
.file-preview-wrapper {
    margin-top: 10px;
    padding: 10px;
    background: #fdfaf5;
    border: 1px dashed #d1aa68;
    border-radius: 4px;
    text-align: center;
}

.file-preview-wrapper p {
    margin: 0 0 5px 0;
    font-size: 12px;
    color: #666;
}

.file-preview-img {
    max-height: 100px;
    object-fit: contain;
    border-radius: 4px;
    margin-bottom: 5px;
}

.file-name-text {
    display: block;
    font-size: 11px;
    color: #444;
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
    max-width: 200px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.category-name {
    color: #7c6f64;
    font-style: italic;
}

.price {
    font-weight: bold;
    color: #d1aa68;
}

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
    width: 500px;
    max-width: 90%;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
    max-height: 90vh;
    overflow-y: auto;
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
select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccbfb5;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 14px;
}

input:focus,
select:focus {
    border-color: #d1aa68;
    outline: none;
}

/* Kiểu riêng cho ô input file cho đẹp */
input[type="file"] {
    padding: 5px;
    background: #fcfbf9;
    cursor: pointer;
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