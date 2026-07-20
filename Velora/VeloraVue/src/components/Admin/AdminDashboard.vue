<template>
    <div class="admin-wrapper">
        <nav class="sidebar">
            <h2 class="brand">VELORA ADMIN</h2>
            <!-- Tìm thẻ <ul> class="menu" -->
            <ul class="menu">
                <li v-for="item in filteredMenuItems" :key="item.name">
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
                <h1>Welcome back, <span class="gold">Admin</span></h1>
                <p>Quản trị hệ thống thời gian cao cấp Velora Clock.</p>
            </header>

            <section class="grid">
                <router-link v-for="card in filteredCards" :key="card.title" :to="card.link" class="card">
                    <i :class="card.icon"></i>
                    <h3>{{ card.title }}</h3>
                    <p>{{ card.desc }}</p>
                </router-link>
            </section>

            <div class="activity-box">
                <h3>Recent Activity</h3>
                <ul v-if="activities.length > 0">
                    <li v-for="(msg, i) in activities" :key="i">
                        <i class="fa-solid fa-circle-info"></i> {{ msg }}
                    </li>
                </ul>
                <p v-else>Không có thông báo mới.</p>
            </div>
        </main>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// 1. Lấy role từ localStorage một cách an toàn
const userRole = computed(() => {
    const userStr = localStorage.getItem('user'); // Nhớ kiểm tra đúng key lưu user của em là 'user'
    if (!userStr) return '';
    try {
        const user = JSON.parse(userStr);
        // Trả về vai trò (ví dụ: 'ROLE_ADMIN' hoặc 'ROLE_CHUYEN_VIEN_TU_VAN')
        return user.vaiTro || ''; 
    } catch (e) {
        console.error("Lỗi parse user từ localStorage:", e);
        return '';
    }
});

// 2. Định nghĩa danh sách menu gốc
// 2. Định nghĩa danh sách menu gốc với quyền cụ thể
const allMenuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge', roles: ['ROLE_ADMIN'] },
    { name: 'Tư Vấn Khách Hàng', link: '/admin/tu-van-khach-hang', icon: 'fa-solid fa-comments', roles: ['ROLE_CHUYEN_VIEN_TU_VAN'] },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked', roles: ['ROLE_ADMIN'] },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem', roles: ['ROLE_ADMIN'] },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check', roles: ['ROLE_ADMIN', 'ROLE_CHUYEN_VIEN_TU_VAN'] }, 
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
];
const allCards = [

    { title: 'Quản lý sản phẩm', desc: '...', icon: 'fa-solid fa-box-open', link: '/admin/products' },

    { title: 'Quản lý loại sản phẩm', desc: '...', icon: 'fa-solid fa-layer-group', link: '/admin/categories' },

    { title: 'Quản lý người dùng', desc: '...', icon: 'fa-solid fa-users', link: '/admin/users', requiresAdmin: true },

    { title: 'Quản lý đơn hàng', desc: '...', icon: 'fa-solid fa-file-invoice', link: '/admin/orders' },

    { title: 'Quản lý kho hàng', desc: '...', icon: 'fa-solid fa-boxes-stacked', link: '/admin/inventory' },

    { title: 'Xuất hóa đơn', desc: '...', icon: 'fa-solid fa-file-invoice-dollar', link: '/admin/invoices' },

    { title: 'Quản lý thương hiệu', desc: '...', icon: 'fa-solid fa-gem', link: '/admin/manufacturers' },

    { title: 'Phiếu nhập kho', desc: '...', icon: 'fa-solid fa-clipboard-list', link: '/admin/receipts' }

];


// 3. Logic Lọc Menu
const filteredMenuItems = computed(() => {
    if (!userRole.value) return [];
    
    return allMenuItems.filter(item => item.roles.includes(userRole.value));
});

// 4. Các logic phụ
const activities = ref(['Đơn hàng #V2026 mới được tạo', 'Sản phẩm Velora Noir đã hết hàng']);

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/dang-nhap';
};
</script>

<style scoped>
@import "../CSS/Admin/AdminDashboard.css";
</style>