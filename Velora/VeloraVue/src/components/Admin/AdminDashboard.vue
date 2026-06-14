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
    { name: 'Dashboard', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Products', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Users', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Orders', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' }
];

const cards = [
    { title: 'Quản lý sản phẩm', desc: 'Cập nhật kho và giá bán.', icon: 'fa-solid fa-shoe-prints', link: '/admin/products' },
    { title: 'Quản lý đơn hàng', desc: 'Xử lý yêu cầu khách hàng.', icon: 'fa-solid fa-box', link: '/admin/orders' },
    { title: 'Báo cáo doanh thu', desc: 'Thống kê tình hình kinh doanh.', icon: 'fa-solid fa-chart-pie', link: '/admin/statistics' }
];

const activities = ref(['Đơn hàng #V2026 mới được tạo', 'Sản phẩm Velora Noir đã hết hàng']);

const handleLogout = () => {
    localStorage.removeItem('role');
    window.location.href = '/';
};
</script>

<style scoped>
/* Reset & Layout */
.admin-wrapper {
    display: flex;
    min-height: 100vh;
    background: #f4f1ea;
    font-family: sans-serif;
}

/* Sidebar */
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

/* Content */
.content {
    flex: 1;
    padding: 60px;
    min-width: 0;
}

.header h1 {
    color: #3e332e;
    font-size: 32px;
}

.gold {
    color: #d1aa68;
}

/* Grid Cards */
.grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 30px;
    margin: 40px 0;
}

.card {
    background: white;
    padding: 40px;
    border: 1px solid #e0dcd5;
    text-decoration: none !important;
    color: #3e332e;
    transition: 0.3s;
    display: flex;
    flex-direction: column;
}

.card:hover {
    border-color: #d1aa68;
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
}

.card i {
    font-size: 30px;
    margin-bottom: 15px;
    color: #d1aa68;
}

.card h3 {
    margin-bottom: 10px;
}

/* Activity */
.activity-box {
    background: white;
    padding: 30px;
    border: 1px solid #e0dcd5;
}

.activity-box li {
    padding: 10px 0;
    border-bottom: 1px solid #eee;
}
</style>