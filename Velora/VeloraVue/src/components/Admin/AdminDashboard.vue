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
                <h1>Welcome back, <span class="gold">Admin</span></h1>
                <p>Quản trị hệ thống thời gian cao cấp Velora Clock.</p>
            </header>

            <section class="grid">
                <router-link v-for="card in cards" :key="card.title" :to="card.link" class="card">
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
import { ref } from 'vue';

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

const cards = [
    {
        title: 'Quản lý sản phẩm',
        desc: 'Xem danh sách, thêm mới và chỉnh sửa các cỗ máy thời gian.',
        icon: 'fa-solid fa-box-open',
        link: '/admin/products'
    },
    {
        title: 'Quản lý loại sản phẩm',
        desc: 'Phân loại đồng hồ theo tính năng (Moonphase, Chronograph) hoặc phụ kiện.',
        icon: 'fa-solid fa-layer-group', // Biểu tượng xếp lớp rất hợp cho danh mục
        link: '/admin/categories'
    },
    {
        title: 'Quản lý người dùng',
        desc: 'Quản lý tài khoản, phân quyền quản trị và khách hàng.',
        icon: 'fa-solid fa-users',
        link: '/admin/users'
    },
    {
        title: 'Quản lý đơn hàng',
        desc: 'Theo dõi, luân chuyển trạng thái và duyệt hủy đơn đặt.',
        icon: 'fa-solid fa-file-invoice',
        link: '/admin/orders'
    },
    {
        title: 'Quản lý kho hàng',
        desc: 'Quy trình lập phiếu nhập kho, xuất kho và kiểm tồn.',
        icon: 'fa-solid fa-boxes-stacked',
        link: '/admin/inventory'
    },
    {
        title: 'Xuất hóa đơn',
        desc: 'Xem danh sách đơn hàng đã thanh toán và in xuất file PDF.',
        icon: 'fa-solid fa-file-invoice-dollar',
        link: '/admin/invoices'
    },
    {
        title: 'Quản lý thương hiệu',
        desc: 'Thiết lập danh mục nhà chế tác và đối tác đồng hồ cao cấp.',
        icon: 'fa-solid fa-gem',
        link: '/admin/manufacturers'
    },
    {
        title: 'Phiếu nhập kho',
        desc: 'Theo dõi lịch sử nhập hàng và duyệt phiếu yêu cầu.',
        icon: 'fa-solid fa-clipboard-list',
        link: '/admin/receipts'
    }
];

const activities = ref(['Đơn hàng #V2026 mới được tạo', 'Sản phẩm Velora Noir đã hết hàng']);

const handleLogout = () => {
    localStorage.removeItem('role');
    window.location.href = '/';
};
</script>

<style scoped>
@import "../CSS/Admin/AdminDashboard.css";
</style>