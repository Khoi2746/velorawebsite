<template>
    <div class="velora-admin-wrapper admin-wrapper">
        <!-- 1. GỌI COMPONENT SIDEBAR -->
        <AdminSidebar :isCollapsed="isCollapsed" />

        <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
            <!-- 2. GỌI COMPONENT HEADER -->
            <AdminHeader @toggle-sidebar="toggleSidebar" />

            <!-- 3. NỘI DUNG CHÍNH -->
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
                               style="width: 100%; padding: 10px 12px; border: 1px solid #4a3f35; border-radius: 4px;" />
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
                                    <button class="btn-action delete" title="Xóa" @click="confirmDeleteBrand(brand.maThuongHieu, brand.tenThuongHieu)">
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

            <!-- FORM MODAL THÊM / SỬA TRUYỀN THỐNG -->
            <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
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
                        
                        <div class="form-group">
                            <label>Logo thương hiệu *</label>
                            <input type="file" accept="image/*" @change="handleFileUpload" style="background: transparent; color: inherit; padding: 5px 0;" />
                            <div v-if="form.logoThuongHieu" class="img-preview-wrapper" style="margin-top: 10px;">
                                <p style="font-size: 12px; color: #888; margin-bottom: 4px;">Xem trước ảnh:</p>
                                <img :src="getLogoUrl(form.logoThuongHieu)" style="max-height: 60px; border-radius: 4px; border: 1px solid #ddd; object-fit: contain; padding: 5px;" />
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

            <!-- POPUP THÔNG BÁO VVIP DARK MODE -->
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

            <!-- MODAL XÁC NHẬN XÓA VVIP DARK MODE -->
            <div v-if="showDeleteConfirmModal" class="velora-modal-overlay" @click.self="showDeleteConfirmModal = false">
                <div class="velora-modal-card">
                    <div class="modal-icon-wrapper error">
                        <span class="icon-symbol">✕</span>
                    </div>
                    <h3 class="modal-title">XÁC NHẬN XÓA</h3>
                    <p class="modal-desc">BẠN CÓ CHẮC CHẮN MUỐN XÓA THƯƠNG HIỆU "{{ brandToDeleteName }}" (#{{ brandToDeleteId }}) KHÔNG? THAO TÁC NÀY KHÔNG THỂ HOÀN TÁC.</p>
                    <div style="display: flex; gap: 10px; justify-content: center;">
                        <button class="modal-btn-close" style="background: transparent; border: 1px solid #cca15e; color: #cca15e;" @click="showDeleteConfirmModal = false">GIỮ LẠI</button>
                        <button class="modal-btn-close" style="background: #d9534f; color: white;" @click="executeDeleteBrand">XÁC NHẬN XÓA</button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';

// IMPORT COMPONENT CON
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC ĐIỀU KHIỂN LAYOUT CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
    isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU THƯƠNG HIỆU =================
const API_URL = 'http://localhost:8080/api/thuong-hieu';

const brands = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const currentBrandId = ref(null);

// Biến quản lý popup thông báo VVIP
const messageModal = ref({
    show: false,
    type: 'success',
    text: ''
});

const showPopup = (text, type = 'success') => {
    messageModal.value = { show: true, type, text };
};

// Biến quản lý modal xác nhận xóa VVIP
const showDeleteConfirmModal = ref(false);
const brandToDeleteId = ref(null);
const brandToDeleteName = ref('');

// --- TÌM KIẾM & BỘ LỌC ---
const searchQuery = ref('');
const statusFilter = ref('all'); 

// --- PHÂN TRANG LOGIC ---
const currentPage = ref(1);    
const itemsPerPage = ref(5);   

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
        if (currentPage.value > totalPages.value && totalPages.value > 0) {
            currentPage.value = totalPages.value;
        }
    } catch (error) {
        console.error('Lỗi API:', error);
    }
};

onMounted(() => {
    loadBrands();
});

const getLogoUrl = (img) => {
    if (!img) return '';
    if (img.startsWith('data:image') || img.startsWith('http')) {
        return img;
    }
    return `/src/assets/images/brands/${img}`;
};

const handleImageError = (e) => {
    e.target.src = 'https://placehold.co/150x75/3e332e/d1aa68?text=VELORA';
};

const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showPopup('VUI LÒNG CHỌN FILE HÌNH ẢNH HỢP LỆ!', 'error');
        event.target.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        form.value.logoThuongHieu = e.target.result; 
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
            showPopup('CẬP NHẬT THÔNG TIN THƯƠNG HIỆU THÀNH CÔNG!', 'success');
        } else {
            await axios.post(API_URL, dataToSend);
            showPopup('THÊM MỚI ĐỐI TÁC THƯƠNG HIỆU THÀNH CÔNG!', 'success');
        }
        closeModal();
        loadBrands();
    } catch (error) {
        console.error('Lỗi lưu:', error);
        let errorMsg = 'KHÔNG THỂ GHI NHẬN DỮ LIỆU, VUI LÒNG KIỂM TRA LẠI CẤU HÌNH.';
        if (error.response && error.response.data) {
            errorMsg = typeof error.response.data === 'string' ? error.response.data.toUpperCase() : JSON.stringify(error.response.data);
        }
        showPopup(errorMsg, 'error');
    }
};

const confirmDeleteBrand = (id, name) => {
    brandToDeleteId.value = id;
    brandToDeleteName.value = name;
    showDeleteConfirmModal.value = true;
};

const executeDeleteBrand = async () => {
    if (!brandToDeleteId.value) return;
    try {
        await axios.delete(`${API_URL}/${brandToDeleteId.value}`);
        showDeleteConfirmModal.value = false;
        showPopup('ĐÃ XÓA THƯƠNG HIỆU THÀNH CÔNG!', 'success');
        loadBrands();
    } catch (error) {
        showDeleteConfirmModal.value = false;
        showPopup('KHÔNG THỂ XÓA DO THƯƠNG HIỆU NÀY ĐANG CÓ SẢN PHẨM RÀNG BUỘC!', 'error');
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
</script>

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
@import "../CSS/Admin/QuanLyThuongHieu.css";

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

/* Modal form nhập liệu */
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
    width: 500px;
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

.form-group input[type="text"], 
.form-group select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-family: inherit;
}

.form-group input[type="text"]:focus, 
.form-group select:focus {
    outline: none;
    border-color: var(--gold-matte);
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
   CSS POPUP & MODAL XÁC NHẬN VVIP DARK MODE
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