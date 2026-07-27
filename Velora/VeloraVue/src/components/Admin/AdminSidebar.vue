<template>
  <aside class="main-sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <div class="sidebar-header">
      <span class="logo-mini"><b>V</b>L</span>
      <span class="logo-lg"><b class="gold-text">VELORA</b> ADMIN</span>
    </div>

    <div class="user-panel">
      <div class="image">
        <img src="https://ui-avatars.com/api/?name=Admin+Velora&background=cca15e&color=fff" alt="User Image">
      </div>
      <div class="info">
        <p>{{ userName }}</p>
        <a href="#"><i class="fa-solid fa-circle text-success"></i> Online</a>
      </div>
    </div>

    <ul class="sidebar-menu">
      <li v-for="item in filteredMenuItems" :key="item.name">
        <router-link :to="item.link" active-class="active">
          <i :class="item.icon"></i>
          <span>{{ item.name }}</span>
        </router-link>
      </li>
    </ul>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue';

// Nhận state thu gọn từ Layout cha
defineProps({
  isCollapsed: {
    type: Boolean,
    default: false
  }
});

const userName = ref('Quản Trị Viên');
const userRole = computed(() => {
  try {
    const userStr = localStorage.getItem('user'); 
    if (userStr && userStr !== 'undefined') {
      const user = JSON.parse(userStr);
      if (user.hoTen) userName.value = user.hoTen;
      return user?.vaiTro || ''; 
    }
  } catch (e) {
    console.error("Lỗi parse user:", e);
  }
  return '';
});

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

const filteredMenuItems = computed(() => {
  if (!userRole.value) return [];
  return allMenuItems.filter(item => item.roles.includes(userRole.value));
});
</script>

<style scoped>
/* Chỉ giữ CSS của Sidebar ở đây */
.main-sidebar {
  width: 260px;
  background-color: var(--wood-dark);
  color: #fff;
  transition: width 0.3s ease-in-out;
  display: flex;
  flex-direction: column;
  z-index: 10;
}
.sidebar-header { height: 60px; background-color: var(--wood-dark); color: var(--gold-matte); display: flex; align-items: center; justify-content: center; font-size: 20px; letter-spacing: 1.5px; padding-top: 10px; }
.logo-mini { display: none; font-size: 18px; }
.gold-text { color: var(--gold-matte); }
.user-panel { display: flex; padding: 20px 15px; align-items: center; }
.user-panel .image img { width: 40px; height: 40px; border-radius: 50%; margin-right: 15px; }
.user-panel .info p { font-weight: normal; margin-bottom: 5px; color: #fff; font-size: 14px; }
.user-panel .info a { color: #aaa; text-decoration: none; font-size: 12px; }
.text-success { color: #2ecc71; }
.sidebar-menu { list-style: none; padding: 10px 15px; flex-grow: 1; overflow-y: auto; }
.sidebar-menu::-webkit-scrollbar { width: 5px; }
.sidebar-menu::-webkit-scrollbar-thumb { background: var(--wood-active); border-radius: 10px; }
.sidebar-menu li { margin-bottom: 5px; }
.sidebar-menu li a { display: flex; align-items: center; padding: 12px 15px; color: #dedede; text-decoration: none; transition: 0.2s; border-radius: 6px; font-size: 14px; }
.sidebar-menu li a:hover, .sidebar-menu li a.active { color: var(--gold-matte); background: var(--wood-active); }
.sidebar-menu li a i { width: 30px; font-size: 16px; color: inherit; }

/* COLLAPSED STATE */
.sidebar-collapsed { width: 70px; }
.sidebar-collapsed .logo-lg, .sidebar-collapsed .user-panel .info, .sidebar-collapsed .sidebar-menu span { display: none; }
.sidebar-collapsed .logo-mini { display: block; color: var(--gold-matte); font-weight: bold; font-size: 22px; text-align: center; width: 100%; }
.sidebar-collapsed .user-panel { padding: 15px 0; justify-content: center; }
.sidebar-collapsed .user-panel .image img { margin-right: 0; width: 38px; height: 38px; }
.sidebar-collapsed .sidebar-menu { padding: 10px 8px; }
.sidebar-collapsed .sidebar-menu li a { padding: 12px 0; justify-content: center; border-radius: 8px; }
.sidebar-collapsed .sidebar-menu li a i { width: auto; margin: 0; font-size: 20px; }
</style>