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
                    <h1>Quản Lý <span class="gold">Thương Hiệu</span></h1>
                    <p>Danh sách các đối tác và nhà chế tác đồng hồ trong hệ thống.</p>
                </div>
                <div class="header-right">
                    <button class="btn-add" @click="openAddModal">
                        <i class="fa-solid fa-plus"></i> Thêm Thương Hiệu Mới
                    </button>
                </div>
            </header>

            <!-- THANH BỘ LỌC VÀ TÌM KIẾM -->
            <section class="filter-bar" style="display: flex; gap: 15px; margin-bottom: 20px;">
                <div class="search-box" style="flex: 1; position: relative;">
                    <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên thương hiệu..." 
                           style="width: 100%; padding: 10px 12px; border: 1px solid #4a3f35;  border-radius: 4px;" />
                </div>
                <div class="filter-box" style="width: 200px;">
                    <select v-model="statusFilter" 
                            style="width: 100%; padding: 10px 12px; border: 1px solid #4a3f35; border-radius: 4px; cursor: pointer;">
                        <option value="all">Tất cả trạng thái</option>
                        <option value="active">Đang hợp tác</option>
                        <option value="inactive">Tạm ngưng</option>
                    </select>
                </div>
            </section>

            <section class="table-container">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Logo</th>
                            <th>Tên Thương Hiệu</th>
                            <th>Mô Tả Ngắn</th>
                            <th>Website</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(brand, index) in paginatedBrands" :key="brand.maThuongHieu">
                        <td>#{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
                            <td>
                                <div class="img-wrapper">
                                    <img :src="getLogoUrl(brand.logoThuongHieu)" :alt="brand.tenThuongHieu" @error="handleImageError" />
                                </div>
                            </td>
                            <td class="product-name">{{ brand.tenThuongHieu }}</td>
                            <td class="brand-desc" :title="brand.moTaNgan">{{ brand.moTaNgan || '---' }}</td>
                            <td>
                                <a v-if="brand.websiteThuongHieu" :href="brand.websiteThuongHieu" target="_blank" class="web-link">
                                    {{ cleanUrl(brand.websiteThuongHieu) }} <i class="fa-solid fa-arrow-up-right-from-square" style="font-size: 11px;"></i>
                                </a>
                                <span v-else class="text-muted">---</span>
                            </td>
                            <td>
                                <span class="status-badge"
                                    @click="toggleBrandStatus(brand)"
                                    :class="brand.trangThai ? 'in-stock' : 'out-stock'"
                                    style="cursor: pointer;" title="Nhấp chuột để đổi nhanh trạng thái">
                                    {{ brand.trangThai ? 'Đang hợp tác' : 'Tạm ngưng' }}
                                </span>
                            </td>
                            <td class="actions">
                                <button class="btn-action edit" title="Chỉnh sửa" @click="openEditModal(brand)">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button class="btn-action delete" title="Xóa" @click="deleteBrand(brand.maThuongHieu, brand.tenThuongHieu)">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="filteredBrands.length === 0">
                            <td colspan="7" class="empty-state">Không tìm thấy thương hiệu phù hợp hoặc danh sách trống...</td>
                        </tr>
                    </tbody>
                </table>

                <div v-if="filteredBrands.length > 0" class="pagination-bar">
                    <div class="pagination-info">
                        Hiển thị từ <b>{{ fromItem }}</b> đến <b>{{ toItem }}</b> trên tổng số <b>{{ filteredBrands.length }}</b> đối tác
                    </div>
                    <div class="pagination-controls">
                        <button class="btn-page" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
                            <i class="fa-solid fa-chevron-left"></i> Trước
                        </button>
                        
                        <button v-for="page in totalPages" :key="page" 
                                class="btn-page-number" 
                                :class="{ active: currentPage === page }"
                                @click="changePage(page)">
                            {{ page }}
                        </button>

                        <button class="btn-page" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
                            Sau <i class="fa-solid fa-chevron-right"></i>
                        </button>
                    </div>
                </div>
            </section>
        </main>

        <!-- FORM MODAL THÊM / SỬA -->
        <div v-if="showModal" class="modal-overlay">
            <div class="modal-box">
                <div class="modal-header">
                    <h3>{{ isEditMode ? 'Cập Nhật Thương Hiệu' : 'Thêm Thương Hiệu Mới' }}</h3>
                    <button class="close-btn" @click="closeModal">&times;</button>
                </div>
                <form @submit.prevent="saveBrand">
                    <div class="form-group">
                        <label>Tên thương hiệu *</label>
                        <input type="text" v-model="form.tenThuongHieu" required placeholder="Ví dụ: Rolex, Hublot..." />
                    </div>
                    
                    <!-- CHỌN FILE ẢNH TRỰC TIẾP -->
                    <div class="form-group">
                        <label>Logo thương hiệu *</label>
                        <input type="file" accept="image/*" @change="handleFileUpload" style="background: transparent; color: #fff;" />
                        <div v-if="form.logoThuongHieu" class="img-preview-wrapper" style="margin-top: 10px;">
                            <p style="font-size: 12px; color: #aaa; margin-bottom: 4px;">Xem trước ảnh:</p>
                            <img :src="getLogoUrl(form.logoThuongHieu)" style="max-height: 60px; border-radius: 4px; border: 1px solid #4a3f35; object-fit: contain;" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Website chính thức</label>
                        <input type="text" v-model="form.websiteThuongHieu" placeholder="Ví dụ: https://www.rolex.com" />
                    </div>
                    <div class="form-group">
                        <label>Mô tả ngắn về hãng</label>
                        <input type="text" v-model="form.moTaNgan" placeholder="Nhập một vài dòng giới thiệu ngắn..." />
                    </div>
                    <div class="form-group">
                        <label>Trạng thái hợp tác</label>
                        <select v-model="form.trangThai">
                            <option :value="true">Đang hợp tác</option>
                            <option :value="false">Tạm ngưng</option>
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
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/thuong-hieu';

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
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', requiresAdmin: true }
];

const brands = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const currentBrandId = ref(null);

// --- TÌM KIẾM & BỘ LỌC ---
const searchQuery = ref('');
const statusFilter = ref('all'); 

// --- PHÂN TRANG LOGIC ---
const currentPage = ref(1);    
const itemsPerPage = ref(5);   

// 1. Tầng lọc dữ liệu tìm kiếm và trạng thái
const filteredBrands = computed(() => {
    return brands.value.filter(brand => {
        const matchesSearch = brand.tenThuongHieu
            ? brand.tenThuongHieu.toLowerCase().includes(searchQuery.value.toLowerCase())
            : false;
        
        let matchesStatus = true;
        if (statusFilter.value === 'active') matchesStatus = brand.trangThai === true;
        if (statusFilter.value === 'inactive') matchesStatus = brand.trangThai === false;

        return matchesSearch && matchesStatus;
    });
});

// Tự động nhảy về trang 1 khi gõ tìm kiếm hoặc đổi bộ lọc
watch([searchQuery, statusFilter], () => {
    currentPage.value = 1;
});

const totalPages = computed(() => {
    return Math.ceil(filteredBrands.value.length / itemsPerPage.value) || 1;
});

const paginatedBrands = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return filteredBrands.value.slice(start, end);
});

const fromItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1);
const toItem = computed(() => {
    const calcEnd = currentPage.value * itemsPerPage.value;
    return calcEnd > filteredBrands.value.length ? filteredBrands.value.length : calcEnd;
});

const changePage = (page) => {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
};

const defaultForm = {
    tenThuongHieu: '',
    logoThuongHieu: '',
    moTaNgan: '',
    websiteThuongHieu: '',
    trangThai: true
};
const form = ref({ ...defaultForm });

const loadBrands = async () => {
    try {
        const res = await axios.get(API_URL);
        brands.value = res.data; 
        if (currentPage.value > totalPages.value) {
            currentPage.value = totalPages.value;
        }
    } catch (error) {
        console.error('Lỗi API:', error);
    }
};

onMounted(() => {
    loadBrands();
});

// --- PHƯƠNG THỨC XỬ LÝ ẢNH CHUẨN ---
const getLogoUrl = (img) => {
    if (!img) return '';
    // Nếu là Base64 hoặc link Web ngoài hệ thống, render trực tiếp
    if (img.startsWith('data:image') || img.startsWith('http')) {
        return img;
    }
    // Dành cho ảnh tĩnh mẫu cục bộ cũ
    return `/src/assets/images/brands/${img}`;
};

const handleImageError = (e) => {
    e.target.src = 'https://placehold.co/150x75/3e332e/d1aa68?text=VELORA';
};

const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        alert('Vui lòng chọn file hình ảnh hợp lệ!');
        event.target.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        form.value.logoThuongHieu = e.target.result; // Chuyển trọn vẹn thành Base64 gán vào form
    };
    reader.readAsDataURL(file);
};

const cleanUrl = (url) => {
    if (!url) return '';
    return url.replace('https://', '').replace('http://', '').replace('www.', '');
};

const openAddModal = () => {
    isEditMode.value = false;
    currentBrandId.value = null;
    form.value = { ...defaultForm };
    showModal.value = true;
};

const openEditModal = (brand) => {
    isEditMode.value = true;
    currentBrandId.value = brand.maThuongHieu;
    form.value = { ...brand };
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
};

const saveBrand = async () => {
    try {
        const dataToSend = { ...form.value };
        if (isEditMode.value) {
            await axios.put(`${API_URL}/${currentBrandId.value}`, dataToSend);
            alert('Cập nhật thông tin thương hiệu thành công!');
        } else {
            await axios.post(API_URL, dataToSend);
            alert('Thêm mới đối tác thương hiệu thành công!');
        }
        closeModal();
        loadBrands();
    } catch (error) {
        console.error('Lỗi lưu:', error);
        alert('Không thể ghi nhận dữ liệu, vui lòng kiểm tra lại cấu hình DB.');
    }
};

const deleteBrand = async (id, name) => {
    if (confirm(`Bạn có chắc chắn muốn xóa thương hiệu "${name}" (#${id})?`)) {
        try {
            await axios.delete(`${API_URL}/${id}`);
            alert('Đã xóa thương hiệu thành công!');
            loadBrands();
        } catch (error) {
            alert('Không thể xóa do thương hiệu này đang có sản phẩm ràng buộc!');
        }
    }
};

const toggleBrandStatus = async (brand) => {
    try {
        brand.trangThai = !brand.trangThai;
        await axios.put(`${API_URL}/${brand.maThuongHieu}`, brand);
    } catch (error) {
        loadBrands();
    }
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};
</script>
<style scoped>
@import "../CSS/Admin/QuanLyThuongHieu.css";
</style>