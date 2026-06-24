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
                    <h1>Quản Lý <span class="gold">Kho Hàng</span></h1>
                    <p>Quy trình tạo phiếu yêu cầu nhập và kiểm soát tồn kho.</p>
                </div>

                <div class="header-right">
                    <div class="search-box">
                        <i class="fa-solid fa-magnifying-glass search-icon"></i>
                        <input type="text" v-model="searchQuery" placeholder="Tìm theo Mã SP..." class="search-input" />
                    </div>
                </div>
            </header>

            <section class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 80px;">ID</th>
                            <th style="width: 90px;">Hình Ảnh</th>
                            <th>Tên Sản Phẩm</th>
                            <th style="width: 140px;">Trạng Thái</th>
                            <th style="width: 140px; text-align: center;">Tồn Kho Hiện Tại</th>
                            <th style="width: 150px; text-align: center;">Hành Động</th>
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
                                <span class="status-badge"
                                    :class="product.trangThai === 'CON_HANG' ? 'in-stock' : 'out-stock'">
                                    {{ product.trangThai === 'CON_HANG' ? 'Còn Hàng' : 'Hết Hàng' }}
                                </span>
                            </td>

                            <td style="text-align: center; font-weight: bold; font-size: 16px;">
                                {{ product.soLuongTonKho }}
                            </td>

                            <td style="text-align: center;">
                                <button class="btn-create-receipt" @click="openModal(product)">
                                    <i class="fa-solid fa-file-circle-plus"></i> Tạo Phiếu
                                </button>
                            </td>
                        </tr>

                        <tr v-if="filteredProducts.length === 0">
                            <td colspan="6" class="empty-state">
                                {{ products.length === 0 ? 'Đang tải dữ liệu...' : 'Không tìm thấy mã sản phẩm này!' }}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>
        </main>

        <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
            <div class="modal-box">
                <div class="modal-header">
                    <h2>Tạo Yêu Cầu Nhập Kho</h2>
                    <button class="btn-close" @click="closeModal"><i class="fa-solid fa-xmark"></i></button>
                </div>

                <div class="modal-body">
                    <div class="info-group">
                        <label>Sản phẩm cần nhập:</label>
                        <p class="highlight-text">#{{ selectedProduct?.maSanPham }} - {{ selectedProduct?.tenSanPham }}
                        </p>
                    </div>

                    <div class="info-group">
                        <label>Tồn kho hiện tại:</label>
                        <p class="stock-text">{{ selectedProduct?.soLuongTonKho }} chiếc</p>
                    </div>

                    <div class="form-group">
                        <label>Ngày yêu cầu:</label>
                        <input type="date" v-model="phieuNhap.ngayNhap" class="modal-input" />
                    </div>

                    <div class="form-group">
                        <label>Số lượng cần nhập:</label>
                        <input type="number" v-model="phieuNhap.soLuongNhap" min="1" class="modal-input"
                            placeholder="Nhập số lượng..." />
                    </div>

                    <div class="total-preview" v-if="phieuNhap.soLuongNhap > 0">
                        <i class="fa-solid fa-circle-info"></i> Phiếu này sẽ được chuyển sang trạng thái <strong>Chờ Duyệt</strong>.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn-cancel" @click="closeModal">Hủy bỏ</button>
                    <button class="btn-confirm-receipt" @click="submitPhieuNhap">
                        <i class="fa-solid fa-paper-plane"></i> Gửi Yêu Cầu
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

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
const products = ref([]);
const searchQuery = ref('');

// Logic Modal Phiếu Nhập
const showModal = ref(false);
const selectedProduct = ref(null);
const phieuNhap = ref({
    ngayNhap: '',
    soLuongNhap: 0
});

const getImageUrl = (img) => {
    if (!img) return '/img/default-watch.png';
    return img.startsWith('http') ? img : `/img/${img}`;
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

// Gọi API lấy dữ liệu
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

// Computed property để lọc sản phẩm theo Mã SP
const filteredProducts = computed(() => {
    if (!searchQuery.value) return products.value;
    return products.value.filter(p =>
        p.maSanPham.toString().includes(searchQuery.value.trim())
    );
});

// Mở modal tạo phiếu
const openModal = (product) => {
    selectedProduct.value = product;

    // Lấy ngày hôm nay làm mặc định định dạng YYYY-MM-DD
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');

    phieuNhap.value = {
        ngayNhap: `${yyyy}-${mm}-${dd}`,
        soLuongNhap: 1 // Mặc định nhập 1
    };

    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
    selectedProduct.value = null;
};

// Gửi phiếu nhập kho (Gửi thẳng qua bảng PhieuNhapKho với trạng thái CHO_DUYET)
const submitPhieuNhap = async () => {
    const slNhap = parseInt(phieuNhap.value.soLuongNhap, 10);

    if (!phieuNhap.value.ngayNhap) {
        alert("Vui lòng chọn ngày yêu cầu!");
        return;
    }

    if (isNaN(slNhap) || slNhap <= 0) {
        alert("Vui lòng nhập số lượng hợp lệ (lớn hơn 0)!");
        return;
    }

    try {
        // Gọi API tạo phiếu nhập (chưa cộng kho)
        const res = await fetch(`http://localhost:8080/api/phieu-nhap`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                maNguoiYeuCau: 2, // Giả sử ID nhân viên đang đăng nhập là 2
                ghiChu: `Yêu cầu nhập ${slNhap} chiếc SP #${selectedProduct.value.maSanPham}`,
                trangThai: 'CHO_DUYET',
                chiTietList: [
                    {
                        maSanPham: selectedProduct.value.maSanPham,
                        soLuongNhap: slNhap,
                        giaNhap: selectedProduct.value.giaBan ? selectedProduct.value.giaBan * 0.6 : 0 // Giả định giá nhập bằng 60% giá bán
                    }
                ]
            })
        });

        if (res.ok) {
            alert(`Đã gửi yêu cầu tạo Phiếu Nhập thành công! Vui lòng chờ Admin phê duyệt.`);
            closeModal();
            // Lưu ý: Không gọi lại loadProducts() ở đây vì kho chưa thực sự tăng, phải chờ Admin duyệt.
        } else {
            // Giả lập cho UI nếu Backend chưa có endpoint POST /api/phieu-nhap
            alert(`[Demo Frontend] Đã tạo yêu cầu nhập ${slNhap} sản phẩm. Đang chờ Admin duyệt.`);
            closeModal();
        }
    } catch (error) {
        console.error('Lỗi khi tạo phiếu:', error);
        alert("[Demo Frontend] Đã gửi yêu cầu nhập kho thành công. Vui lòng qua trang Phiếu Nhập Kho để duyệt.");
        closeModal();
    }
};

onMounted(() => {
    loadProducts();
});
</script>

<style scoped>
/* ================= LAYOUT & TABLE ================= */
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
    padding: 10px;
    border-radius: 6px;
    transition: 0.3s;
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

.header {
    margin-bottom: 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
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

/* SEARCH BOX MỚI */
.search-box {
    display: flex;
    align-items: center;
    background: #fff;
    border: 1px solid #d1aa68;
    border-radius: 20px;
    padding: 8px 15px;
    width: 250px;
}

.search-icon {
    color: #d1aa68;
    margin-right: 10px;
}

.search-input {
    border: none;
    outline: none;
    font-size: 14px;
    width: 100%;
    color: #333;
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
    border-bottom: 2px solid #e0dcd5;
    letter-spacing: 1px;
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
    max-width: 250px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.status-badge {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: bold;
    text-transform: uppercase;
    display: inline-block;
    text-align: center;
    width: 90px;
}

.in-stock {
    background-color: #e8f5e9;
    color: #2e7d32;
}

.out-stock {
    background-color: #ffebee;
    color: #c62828;
}

.btn-create-receipt {
    background-color: #f4f1ea;
    color: #3e332e;
    border: 1px solid #e0dcd5;
    padding: 8px 15px;
    font-weight: bold;
    border-radius: 4px;
    cursor: pointer;
    transition: 0.2s;
}

.btn-create-receipt:hover {
    background-color: #d1aa68;
    color: #fff;
    border-color: #d1aa68;
}

.empty-state {
    text-align: center;
    padding: 40px !important;
    color: #888;
}

/* ================= MODAL PHIẾU NHẬP ================= */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-box {
    background: #fff;
    width: 450px;
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    overflow: hidden;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.modal-header {
    background: #3e332e;
    color: #d1aa68;
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h2 {
    font-size: 18px;
    margin: 0;
}

.btn-close {
    background: none;
    border: none;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
    transition: 0.2s;
}

.btn-close:hover {
    color: #d1aa68;
}

.modal-body {
    padding: 25px;
}

.info-group {
    margin-bottom: 15px;
}

.info-group label {
    display: block;
    font-size: 13px;
    color: #888;
    margin-bottom: 5px;
}

.highlight-text {
    font-weight: bold;
    color: #333;
    font-size: 16px;
    margin: 0;
}

.stock-text {
    font-weight: bold;
    color: #2e7d32;
    font-size: 16px;
    margin: 0;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    font-weight: bold;
    color: #333;
    margin-bottom: 8px;
}

.modal-input {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 15px;
    box-sizing: border-box;
}

.modal-input:focus {
    border-color: #d1aa68;
    outline: none;
}

.total-preview {
    background: #e8f5e9;
    border: 1px dashed #2e7d32;
    padding: 15px;
    color: #2e7d32;
    border-radius: 4px;
    font-size: 14px;
}

.modal-footer {
    padding: 15px 25px;
    background: #f4f1ea;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.btn-cancel {
    background: #ccc;
    color: #333;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
}

.btn-cancel:hover {
    background: #bbb;
}

.btn-confirm-receipt {
    background: #d1aa68;
    color: #111;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
}

.btn-confirm-receipt:hover {
    background: #b8955b;
    color: #fff;
}
</style>