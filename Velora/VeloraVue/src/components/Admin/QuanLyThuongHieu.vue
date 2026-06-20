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
                        <tr v-for="brand in paginatedBrands" :key="brand.maThuongHieu">
                            <td>#{{ brand.maThuongHieu }}</td>
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
                        <tr v-if="brands.length === 0">
                            <td colspan="7" class="empty-state">Đang tải dữ liệu hoặc danh sách thương hiệu trống...</td>
                        </tr>
                    </tbody>
                </table>

                <div v-if="brands.length > 0" class="pagination-bar">
                    <div class="pagination-info">
                        Hiển thị từ <b>{{ fromItem }}</b> đến <b>{{ toItem }}</b> trên tổng số <b>{{ brands.length }}</b> đối tác
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
                    <div class="form-group">
                        <label>Tên file Logo / Đường dẫn ảnh URL</label>
                        <input type="text" v-model="form.logoThuongHieu" placeholder="Ví dụ: rolex-logo.png" />
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
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/thuong-hieu';

const menuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked' },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar' },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem' },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list' }
];

const brands = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const currentBrandId = ref(null);

// --- CẤU HÌNH LOGIC PHÂN TRANG ---
const currentPage = ref(1);    // Trang hiện tại
const itemsPerPage = ref(5);   // Số lượng thương hiệu trên mỗi trang (Bạn có thể tăng lên 10 nếu muốn)

// Tính toán tổng số trang dựa trên mảng tổng dữ liệu trả về từ API
const totalPages = computed(() => {
    return Math.ceil(brands.value.length / itemsPerPage.value) || 1;
});

// Cắt lát (Slice) mảng dữ liệu gốc để chỉ hiển thị dữ liệu của trang hiện tại
const paginatedBrands = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return brands.value.slice(start, end);
});

// Các biến hiển thị nhãn chỉ số (Ví dụ: Hiển thị từ 1 đến 5...)
const fromItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1);
const toItem = computed(() => {
    const calcEnd = currentPage.value * itemsPerPage.value;
    return calcEnd > brands.value.length ? brands.value.length : calcEnd;
});

// Hàm chuyển trang bảo vệ không bị tràn index
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
        
        // Tránh tình trạng sau khi xóa phần tử ở trang cuối bị rỗng giao diện
        if (currentPage.value > totalPages.value) {
            currentPage.value = totalPages.value;
        }
    } catch (error) {
        console.error('Lỗi kết nối API Thương Hiệu:', error);
    }
};

onMounted(() => {
    loadBrands();
});

const getLogoUrl = (img) => {
    if (!img) return '';
    return img.startsWith('http') ? img : `/src/assets/images/brands/${img}`;
};

const handleImageError = (e) => {
    e.target.src = 'https://placehold.co/150x75/3e332e/d1aa68?text=VELORA';
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
        console.error('Lỗi lưu dữ liệu:', error);
        alert(error.response?.data || 'Không thể ghi nhận dữ liệu, vui lòng kiểm tra lại.');
    }
};

const deleteBrand = async (id, name) => {
    if (confirm(`Bạn có chắc chắn muốn xóa thương hiệu "${name}" (#${id})?`)) {
        try {
            await axios.delete(`${API_URL}/${id}`);
            alert('Đã xóa thương hiệu thành công!');
            loadBrands();
        } catch (error) {
            console.error('Lỗi khi xóa:', error);
            alert(error.response?.data || 'Không thể xóa do thương hiệu này đang có sản phẩm ràng buộc!');
        }
    }
};

const toggleBrandStatus = async (brand) => {
    try {
        brand.trangThai = !brand.trangThai;
        await axios.put(`${API_URL}/${brand.maThuongHieu}`, brand);
    } catch (error) {
        console.error('Lỗi đổi trạng thái nhanh:', error);
        loadBrands();
    }
};

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};
</script>

<style scoped>
/* ================= TÁI SỬ DỤNG HỆ THỐNG CSS GỐC CỦA BẠN ================= */
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

.img-wrapper { width: 70px; height: 40px; background: #fff; border: 1px solid #e3ded6; border-radius: 4px; display: flex; align-items: center; justify-content: center; overflow: hidden; padding: 2px; }
.img-wrapper img { max-width: 100%; max-height: 100%; object-fit: contain; }

.product-name { font-weight: bold; color: #3e332e; }
.brand-desc { color: #777; font-style: italic; max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.web-link { color: #3e332e; font-weight: 500; text-decoration: underline !important; }
.web-link:hover { color: #d1aa68; }

.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 11px; font-weight: bold; text-transform: uppercase; user-select: none; }
.in-stock { background-color: #e8f5e9; color: #2e7d32; }
.out-stock { background-color: #ffebee; color: #c62828; }
.actions { display: flex; gap: 10px; }
.btn-action { width: 32px; height: 32px; border: none; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.btn-action.edit { background-color: #f4f1ea; color: #3e332e; }
.btn-action.edit:hover { background-color: #d1aa68; color: #fff; }
.btn-action.delete { background-color: #ffebee; color: #c62828; }
.btn-action.delete:hover { background-color: #c62828; color: #fff; }
.empty-state { text-align: center; padding: 40px !important; color: #888; }

/* CSS MỚI CHO THANH ĐIỀU HƯỚNG PHÂN TRANG ĐỒNG BỘ STYLE CỔ ĐIỂN LUXURY */
.pagination-bar { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; background: #fcfbf9; border-top: 1px solid #e0dcd5; }
.pagination-info { font-size: 13px; color: #666; }
.pagination-info b { color: #3e332e; }
.pagination-controls { display: flex; gap: 6px; align-items: center; }
.btn-page { background: #fff; border: 1px solid #ccbfb5; padding: 6px 14px; font-size: 13px; border-radius: 4px; color: #3e332e; cursor: pointer; display: flex; align-items: center; gap: 5px; transition: 0.2s; }
.btn-page:hover:not(:disabled) { background: #f4f1ea; border-color: #3e332e; }
.btn-page:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-page-number { background: #fff; border: 1px solid #ccbfb5; min-width: 32px; height: 32px; font-size: 13px; border-radius: 4px; color: #3e332e; cursor: pointer; font-weight: 500; transition: 0.2s; }
.btn-page-number:hover { background: #f4f1ea; }
.btn-page-number.active { background: #3e332e; color: #fff; border-color: #3e332e; font-weight: bold; }

/* MODAL BOX */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal-box { background: #fff; padding: 30px; border-radius: 8px; width: 500px; max-width: 90%; box-shadow: 0 10px 25px rgba(0,0,0,0.15); }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e0dcd5; padding-bottom: 15px; margin-bottom: 20px; }
.modal-header h3 { color: #3e332e; margin: 0; font-size: 20px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #888; }
.form-group { margin-bottom: 15px; }
label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: bold; color: #555; }
input, select { width: 100%; padding: 10px; border: 1px solid #ccbfb5; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
input:focus, select:focus { border-color: #d1aa68; outline: none; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; }
.btn-cancel { background: #f4f1ea; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; color: #555; }
.btn-submit { background: #3e332e; color: #fff; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
.btn-submit:hover { background: #52433d; }
</style>