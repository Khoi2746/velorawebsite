<template>
    <div class="velora-admin-wrapper admin-wrapper">
        <!-- 1. GỌI COMPONENT SIDEBAR MỚI -->
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <!-- 2. GỌI COMPONENT HEADER MỚI -->
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <!-- 3. NỘI DUNG CHÍNH -->
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
                        <input type="text" :value="searchQuery"
                            @input="searchQuery = $event.target.value; currentPage = 1"
                            placeholder="Tìm kiếm theo mã ID hoặc tên sản phẩm..." />
                    </div>
                    <div class="filter-boxes">
                        <select v-model="filterDanhMuc" @change="currentPage = 1">
                            <option value="">-- Tất cả danh mục --</option>
                            <option v-for="dm in mainCategories" :key="dm.maDanhMuc" :value="dm.maDanhMuc">
                                {{ dm.tenDanhMuc }}
                            </option>
                        </select>
                        <select v-model="filterGioiTinh" @change="currentPage = 1">
                            <option value="">-- Tất cả giới tính --</option>
                            <option value="Nam">Nam</option>
                            <option value="Nữ">Nữ</option>
                            <option value="Unisex">Unisex</option>
                        </select>
                        <select v-model="filterTrangThai" @change="currentPage = 1">
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
                                <th>Giới tính</th>
                                <th>Bảo Hành</th> <!-- Thêm cột Bảo Hành -->
                                <th>Trạng Thái</th>
                                <th>Hành Động</th>
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
                                    {{ product.danhMuc ? product.danhMuc.tenDanhMuc : 'Chưa chọn' }}
                                </td>

                                <td class="category-name">
                                    {{ product.loaiSanPham ? product.loaiSanPham.tenLoai : 'Không có' }}
                                </td>

                                <td class="price">{{ formatPrice(product.giaBan) }}</td>

                                <td style="font-weight: bold; color: #3e332e;">
                                    {{ product.soLuongTonKho != null ? product.soLuongTonKho : 0 }}
                                </td>
                                <td>{{ product.gioiTinh }}</td>

                                <!-- Hiển thị thông tin bảo hành -->
                                <td>{{ product.thoiGianBaoHanh ? `${product.thoiGianBaoHanh} tháng` : 'Không có' }}</td>

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
                                    <!-- Thay thế gọi deleteProduct(product.maSanPham) thành: -->
                                    <button class="btn-action delete" @click="confirmDeleteProduct(product.maSanPham)"
                                        title="Xóa">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                            <tr v-if="filteredProducts.length === 0">
                                <td colspan="11" class="empty-state">Không tìm thấy sản phẩm nào phù hợp.</td>
                            </tr>
                        </tbody>
                    </table>
                </section>

                <div class="pagination-controls"
                    style="margin-top: 20px; display: flex; justify-content: center; gap: 10px; align-items: center;">
                    <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1"
                        class="btn-page">Trước</button>

                    <span style="font-weight: bold;">Trang {{ currentPage }} / {{ totalPages }}</span>

                    <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages"
                        class="btn-page">Sau</button>
                </div>
            </main>

            <!-- Modal Form Thêm/Sửa Sản Phẩm -->
            <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
                <div class="modal-box">
                    <div class="modal-header">
                        <h3>{{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}</h3>
                        <button class="close-btn" @click="closeModal">&times;</button>
                    </div>
                    <form @submit.prevent="saveProduct">
                        <div class="form-group">
                            <label>Tên sản phẩm *</label>
                            <input type="text" v-model="form.tenSanPham" required
                                placeholder="Ví dụ: Rolex Cosmograph" />
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
                            <label>Giới tính *</label>
                            <select v-model="form.gioiTinh" required>
                                <option value="">-- Chọn giới tính --</option>
                                <option value="Nam">Nam</option>
                                <option value="Nữ">Nữ</option>
                                <option value="Unisex">Unisex</option>
                            </select>
                        </div>

                        <!-- Input thêm thời gian bảo hành (tháng) -->
                        <div class="form-group">
                            <label>Thời gian bảo hành (tháng)</label>
                            <input type="number" v-model.number="form.thoiGianBaoHanh" min="0"
                                placeholder="Ví dụ: 24" />
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
    </div>
    <!-- Popup Thông Báo VVIP Dark Mode -->
    <div v-if="messageModal.show" class="velora-modal-overlay" @click.self="messageModal.show = false">
        <div class="velora-modal-card">
            <div class="modal-icon-wrapper" :class="messageModal.type">
                <span class="icon-symbol">{{ messageModal.type === 'success' ? '✓' : '✕' }}</span>
            </div>
            <h3 class="modal-title">{{ messageModal.type === 'success' ? 'THÀNH CÔNG' : 'THÔNG BÁO LỖI' }}</h3>
            <p class="modal-desc">{{ messageModal.text }}</p>
            <button class="modal-btn-close" @click="messageModal.show = false">ĐÓNG</button>
        </div>
    </div>
    <!-- Modal Xác Nhận Xóa VVIP Dark Mode -->
    <div v-if="showDeleteConfirmModal" class="velora-modal-overlay" @click.self="showDeleteConfirmModal = false">
        <div class="velora-modal-card">
            <div class="modal-icon-wrapper error">
                <span class="icon-symbol">✕</span>
            </div>
            <h3 class="modal-title">XÁC NHẬN XÓA</h3>
            <p class="modal-desc">BẠN CÓ CHẮC CHẮN MUỐN XÓA SẢN PHẨM #{{ productToDeleteId }} KHÔNG? HÀNH ĐỘNG NÀY KHÔNG
                THỂ HOÀN TÁC.</p>
            <div style="display: flex; gap: 10px; justify-content: center;">
                <button class="modal-btn-close"
                    style="background: transparent; border: 1px solid #cca15e; color: #cca15e;"
                    @click="showDeleteConfirmModal = false">GIỮ LẠI</button>
                <button class="modal-btn-close" style="background: #d9534f; color: white;"
                    @click="executeDeleteProduct">XÁC NHẬN XÓA</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

// IMPORT COMPONENT CON
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const isCollapsed = ref(false);

const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};

const currentPage = ref(1);
const itemsPerPage = 10;

const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return filteredProducts.value.slice(start, end);
});

const totalPages = computed(() => {
    return Math.ceil(filteredProducts.value.length / itemsPerPage) || 1;
});

const changePage = (page) => {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
};

const API_URL = 'http://localhost:8080/api/san-pham';
const CAT_API_URL = 'http://localhost:8080/api/loai-san-pham';

const products = ref([]);
const categories = ref([]);
const mainCategories = ref([]);

const searchQuery = ref('');
const filterDanhMuc = ref('');
const filterTrangThai = ref('');
const filterGioiTinh = ref('');
const showModal = ref(false);
const isEditMode = ref(false);
const currentProductId = ref(null);
const imagePreview = ref('');

// Bổ sung thoiGianBaoHanh vào form mặc định
const defaultForm = {
    tenSanPham: '',
    giaBan: 0,
    anhDaiDien: '',
    trangThai: 'CON_HANG',
    maDanhMucSelected: '',
    maLoaiSelected: '',
    gioiTinh: '',
    thoiGianBaoHanh: 12 // Mặc định 12 tháng hoặc để trống
};
const form = ref({ ...defaultForm });

const filteredProducts = computed(() => {
    const query = searchQuery.value.toLowerCase().trim();

    return products.value.filter(product => {
        const cleanQuery = query.startsWith('#') ? query.slice(1) : query;
        const matchId = product.maSanPham != null && String(product.maSanPham) === cleanQuery;
        const matchName = product.tenSanPham ? product.tenSanPham.toLowerCase().includes(query) : false;

        const matchSearch = !query || matchId || matchName;
        const matchDanhMuc = !filterDanhMuc.value || (product.danhMuc && product.danhMuc.maDanhMuc === Number(filterDanhMuc.value));
        const matchTrangThai = !filterTrangThai.value || product.trangThai === filterTrangThai.value;
        const matchGioiTinh = !filterGioiTinh.value || product.gioiTinh === filterGioiTinh.value;

        return matchSearch && matchDanhMuc && matchTrangThai && matchGioiTinh;
    });
});
// Thêm biến quản lý popup thông báo VVIP
const messageModal = ref({
    show: false,
    type: 'success', // 'success' hoặc 'error'
    text: ''
});

const showPopup = (text, type = 'success') => {
    messageModal.value = { show: true, type, text };
};
const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        form.value.anhDaiDien = file.name;
        imagePreview.value = URL.createObjectURL(file);
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
    imagePreview.value = '';
    showModal.value = true;
};

const openEditModal = (product) => {
    isEditMode.value = true;
    currentProductId.value = product.maSanPham;

    // Gán dữ liệu bảo hành vào form khi sửa
    form.value = {
        tenSanPham: product.tenSanPham,
        giaBan: product.giaBan,
        anhDaiDien: product.anhDaiDien,
        trangThai: product.trangThai,
        maDanhMucSelected: product.danhMuc ? product.danhMuc.maDanhMuc : '',
        maLoaiSelected: product.loaiSanPham ? product.loaiSanPham.maLoai : '',
        gioiTinh: product.gioiTinh,
        thoiGianBaoHanh: product.thoiGianBaoHanh || 0
    };

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

        // Đẩy thêm thuộc tính thoiGianBaoHanh vào DTO gửi lên Backend
        const dataToSend = {
            tenSanPham: form.value.tenSanPham,
            gioiTinh: form.value.gioiTinh,
            giaBan: form.value.giaBan,
            anhDaiDien: form.value.anhDaiDien,
            trangThai: form.value.trangThai,
            thoiGianBaoHanh: form.value.thoiGianBaoHanh,
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
            showPopup(isEditMode.value ? 'CẬP NHẬT SẢN PHẨM THÀNH CÔNG!' : 'THÊM SẢN PHẨM THÀNH CÔNG!', 'success');
            closeModal();
            loadProducts();
        } else {
            const errorText = await res.text();
            showPopup(`CÓ LỖI XẢY RA: ${errorText || 'Vui lòng kiểm tra lại dữ liệu.'}`, 'error');
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
                showPopup('XÓA SẢN PHẨM THÀNH CÔNG!', 'success');
                loadProducts();
            } else {
                showPopup('XÓA THẤT BẠI. SẢN PHẨM CÓ THỂ ĐANG VƯỚNG ĐƠN HÀNG!', 'error');
            }
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
            showPopup('ĐÃ XẢY RA LỖI KẾT NỐI KHI XÓA SẢN PHẨM.', 'error');
        }
    }
};
// Biến quản lý modal xác nhận xóa
const showDeleteConfirmModal = ref(false);
const productToDeleteId = ref(null);

const confirmDeleteProduct = (id) => {
    productToDeleteId.value = id;
    showDeleteConfirmModal.value = true;
};

const executeDeleteProduct = async () => {
    if (!productToDeleteId.value) return;
    try {
        const res = await fetch(`${API_URL}/${productToDeleteId.value}`, {
            method: 'DELETE'
        });
        showDeleteConfirmModal.value = false;
        if (res.ok) {
            showPopup('XÓA SẢN PHẨM THÀNH CÔNG!', 'success');
            loadProducts();
        } else {
            showPopup('XÓA THẤT BẠI. SẢN PHẨM CÓ THỂ ĐANG VƯỚNG ĐƠN HÀNG!', 'error');
        }
    } catch (error) {
        showDeleteConfirmModal.value = false;
        console.error('Lỗi khi xóa sản phẩm:', error);
        showPopup('ĐÃ XẢY RA LỖI KẾT NỐI KHI XÓA SẢN PHẨM.', 'error');
    }
};
onMounted(() => {
    loadProducts();
    loadCategories();
    loadMainCategories();
});
</script>

<!-- CSS CHỨA BIẾN GLOBAL ĐỂ SIDEBAR NHẬN MÀU -->
<style>
:root {
    --wood-dark: #362921;
    --wood-active: #47372c;
    --wood-medium: #544438;
    --wood-light: #7a6352;
    --gold-matte: #cca15e;
    --bg-page: #f8f6f0;
    --border-light: #eaeaea;
    --text-main: #333333;
    --text-muted: #888888;
}
</style>

<style scoped>
@import "../CSS/Admin/QuanLySanPham.css";

/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI & CHỈNH LẠI MODAL
   ============================================== */
.velora-admin-wrapper {
    display: flex;
    height: 100vh;
    background-color: var(--bg-page);
    overflow: hidden;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.content-wrapper {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
}

.content {
    flex: 1;
    padding: 30px;
}

/* Đảm bảo phần tiêu đề trang nhận đúng font và màu */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
}

.header-left h1 {
    font-size: 26px;
    font-weight: bold;
    color: var(--wood-dark);
    margin: 0 0 5px 0;
}

.header-left .gold {
    color: var(--gold-matte);
}

.header-left p {
    font-size: 14px;
    color: var(--text-muted);
    margin: 0;
}

/* Fix CSS Overlay cho Modal hiển thị chuẩn chính giữa màn hình */
.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    backdrop-filter: blur(2px);
}

.modal-box {
    background: #fff;
    border-radius: 8px;
    width: 600px;
    /* Cho form to ra một chút để chứa hình ảnh đẹp hơn */
    max-width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    padding: 30px;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    border-bottom: 1px solid #eaeaea;
    padding-bottom: 10px;
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

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: bold;
    color: var(--wood-dark);
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-family: inherit;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: var(--gold-matte);
}

.file-preview-wrapper {
    margin-top: 10px;
    text-align: center;
    background: #f9f9f9;
    padding: 10px;
    border-radius: 4px;
}

.file-preview-img {
    max-width: 150px;
    max-height: 150px;
    border-radius: 4px;
    object-fit: cover;
    margin-bottom: 5px;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 25px;
}

.btn-cancel {
    background: #f0f0f0;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    color: #333;
    font-weight: bold;
}

.btn-cancel:hover {
    background: #e4e4e4;
}

.btn-submit {
    background: var(--wood-dark);
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}

.btn-submit:hover {
    background: var(--gold-matte);
}

/* ==============================================
   CSS POPUP VVIP DARK MODE CHO QUẢN LÝ SẢN PHẨM
   ============================================== */
.velora-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.75);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    backdrop-filter: blur(4px);
}

.velora-modal-card {
    background: #231c18;
    border: 1px solid #cca15e;
    width: 420px;
    max-width: 90%;
    padding: 35px 25px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    color: #f8f6f0;
}

.modal-icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    margin: 0 auto 20px auto;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    font-weight: bold;
}

.modal-icon-wrapper.success {
    background: rgba(204, 161, 94, 0.15);
    border: 2px solid #cca15e;
    color: #cca15e;
}

.modal-icon-wrapper.error {
    background: rgba(217, 83, 79, 0.15);
    border: 2px solid #d9534f;
    color: #d9534f;
}

.modal-title {
    font-size: 18px;
    letter-spacing: 1.5px;
    color: #cca15e;
    margin-bottom: 12px;
    font-weight: 700;
}

.modal-desc {
    font-size: 14px;
    color: #dcd6cd;
    line-height: 1.6;
    margin-bottom: 25px;
    word-break: break-word;
}

.modal-btn-close {
    background: #cca15e;
    color: #1a1412;
    border: none;
    padding: 12px 35px;
    font-weight: bold;
    border-radius: 4px;
    cursor: pointer;
    letter-spacing: 1px;
    transition: all 0.3s ease;
}

.modal-btn-close:hover {
    background: #dfb775;
    box-shadow: 0 0 10px rgba(204, 161, 94, 0.4);
}
</style>