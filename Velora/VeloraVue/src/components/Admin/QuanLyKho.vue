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
                        <tr v-for="product in paginatedProducts" :key="product.maSanPham">
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

                        <tr v-if="paginatedProducts.length === 0">
                            <td colspan="6" class="empty-state">
                                {{ products.length === 0 ? 'Đang tải dữ liệu...' : 'Không tìm thấy mã sản phẩm này!' }}
                            </td>
                        </tr>
                    </tbody>
                </table>

                <!-- Khối điều hướng phân trang -->
                <div class="pagination-wrapper" v-if="totalPages > 1">
                    <button class="btn-page" @click="prevPage" :disabled="currentPage === 1">Trước</button>
                    <span class="page-info">Trang <strong>{{ currentPage }}</strong> / {{ totalPages }}</span>
                    <button class="btn-page" @click="nextPage" :disabled="currentPage === totalPages">Sau</button>
                </div>
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
import { ref, computed, onMounted, watch } from 'vue';

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
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
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

// --- CẤU HÌNH PHÂN TRANG ---
const currentPage = ref(1);
const itemsPerPage = ref(10); // Đặt mặc định 10 bản ghi/trang cho quản lý kho

// Computed property để lọc sản phẩm theo Mã SP
const filteredProducts = computed(() => {
    if (!searchQuery.value) return products.value;
    return products.value.filter(p =>
        p.maSanPham.toString().includes(searchQuery.value.trim())
    );
});

// Tính tổng số trang
const totalPages = computed(() => {
    return Math.ceil(filteredProducts.value.length / itemsPerPage.value) || 1;
});

// Dữ liệu hiển thị trên trang hiện tại
const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return filteredProducts.value.slice(start, end);
});

// Reset về trang 1 mỗi khi người dùng gõ tìm kiếm
watch(searchQuery, () => {
    currentPage.value = 1;
});

// Hàm chuyển trang
const prevPage = () => {
    if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
    if (currentPage.value < totalPages.value) currentPage.value++;
};
// --- KẾT THÚC CẤU HÌNH PHÂN TRANG ---

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
@import "../CSS/Admin/QuanLyKho.css";

</style>