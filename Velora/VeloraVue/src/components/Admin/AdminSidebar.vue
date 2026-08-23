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
      <!-- Trang chủ / Dashboard -->
      <li>
        <router-link to="/admin/dashboard" active-class="active">
          <i class="fa-solid fa-gauge"></i>
          <span>Trang Quản Trị</span>
        </router-link>
      </li>

      <!-- NHÓM 1: KHO & SẢN PHẨM -->
      <li class="menu-group" :class="{ 'menu-open': openGroups.inventory }">
        <div class="menu-group-header" @click="toggleGroup('inventory')">
          <div class="group-title">
            <i class="fa-solid fa-boxes-stacked"></i>
            <span>Kho & Sản Phẩm</span>
          </div>
          <i class="fa-solid fa-chevron-down arrow-icon"></i>
        </div>
        <div class="sub-menu-collapse">
          <ul class="sub-menu">
            <template
              v-for="item in getGroupItems(['Quản Lý Sản Phẩm', 'Quản Lý Loại Sản Phẩm', 'Quản Lý Thương Hiệu', 'Quản Lý Kho', 'Phiếu Nhập Kho'])"
              :key="item.name">
              <li>
                <router-link :to="item.link" active-class="active">
                  <i :class="item.icon"></i>
                  <span>{{ item.name }}</span>
                </router-link>
              </li>
            </template>
          </ul>
        </div>
      </li>

      <!-- NHÓM 2: BÁN HÀNG & TÀI CHÍNH -->
      <li class="menu-group" :class="{ 'menu-open': openGroups.sales }">
        <div class="menu-group-header" @click="toggleGroup('sales')">
          <div class="group-title">
            <i class="fa-solid fa-cash-register"></i>
            <span>Bán Hàng & Tài Chính</span>
          </div>
          <i class="fa-solid fa-chevron-down arrow-icon"></i>
        </div>
        <div class="sub-menu-collapse">
          <ul class="sub-menu">
            <template
              v-for="item in getGroupItems(['Quản Lý Đơn Đặt', 'Quản Lý Hoàn Tiền', 'Xuất Hóa Đơn', 'Quản Lý Mã Giảm Giá', 'Thống Kê Doanh Thu'])"
              :key="item.name">
              <li>
                <router-link :to="item.link" active-class="active">
                  <i :class="item.icon"></i>
                  <span>{{ item.name }}</span>
                </router-link>
              </li>
            </template>
          </ul>
        </div>
      </li>

      <!-- NHÓM 3: VẬN HÀNH & KHÁCH HÀNG -->
      <li class="menu-group" :class="{ 'menu-open': openGroups.operations }">
        <div class="menu-group-header" @click="toggleGroup('operations')">
          <div class="group-title">
            <i class="fa-solid fa-user-gear"></i>
            <span>Vận Hành & Khách Hàng</span>
          </div>
          <i class="fa-solid fa-chevron-down arrow-icon"></i>
        </div>
        <div class="sub-menu-collapse">
          <ul class="sub-menu">
            <template
              v-for="item in getGroupItems(['Quản Lý Người Dùng', 'Quản Lý Lịch Hẹn', 'Quản Lý Bảo Hành', 'Tư Vấn Khách Hàng'])"
              :key="item.name">
              <li>
                <router-link :to="item.link" active-class="active">
                  <i :class="item.icon"></i>
                  <span>{{ item.name }}</span>
                </router-link>
              </li>
            </template>
          </ul>
        </div>
      </li>

      <!-- ============================================== -->
      <!-- TAB ĐỘC LẬP: TRUNG TÂM GIÁM SÁT SOC (MỚI THÊM) -->
      <!-- ============================================== -->
      <li>
        <router-link to="/admin/soc" active-class="active">
          <i class="fa-solid fa-shield-halved"></i>
          <span>Trung Tâm SOC</span>
        </router-link>
      </li>

    </ul>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue';

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

// Trạng thái mặc định ban đầu
const openGroups = ref({
  inventory: true,
  sales: false,
  operations: false
});

// THẦN CHÚ KHÔI PHỤC TRẠNG THÁI: Lấy dữ liệu đã lưu trong Local Storage khi load trang
const savedState = localStorage.getItem('velora_sidebar_state');
if (savedState) {
  try {
    openGroups.value = JSON.parse(savedState);
  } catch (error) {
    console.error("Lỗi đọc trạng thái Sidebar", error);
  }
}

const toggleGroup = (groupName) => {
  // Đảo trạng thái đóng/mở
  openGroups.value[groupName] = !openGroups.value[groupName];
  // THẦN CHÚ LƯU TRẠNG THÁI: Cập nhật thẳng vào Local Storage mỗi khi click
  localStorage.setItem('velora_sidebar_state', JSON.stringify(openGroups.value));
};

const allMenuItems = [
  { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge', roles: ['ROLE_ADMIN'] },
  { name: 'Tư Vấn Khách Hàng', link: '/admin/tu-van-khach-hang', icon: 'fa-solid fa-comments', roles: ['ROLE_ADMIN', 'ROLE_CHUYEN_VIEN_TU_VAN'] },
  { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Hoàn Tiền', link: '/admin/quan-ly-hoan-tien', icon: 'fa-solid fa-rotate-left', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked', roles: ['ROLE_ADMIN'] },
  { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem', roles: ['ROLE_ADMIN'] },
  { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check', roles: ['ROLE_ADMIN', 'ROLE_CHUYEN_VIEN_TU_VAN'] },
  { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
];

const getGroupItems = (namesArray) => {
  if (!userRole.value) return allMenuItems.filter(item => namesArray.includes(item.name));
  return allMenuItems.filter(item =>
    namesArray.includes(item.name) && item.roles.includes(userRole.value)
  );
};
</script>

<style scoped>
.main-sidebar {
  width: 260px;
  background-color: var(--wood-dark);
  color: #fff;
  transition: width 0.3s ease-in-out;
  display: flex;
  flex-direction: column;
  z-index: 10;
  flex-shrink: 0;
}

.sidebar-header {
  height: 60px;
  background-color: var(--wood-dark);
  color: var(--gold-matte);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  letter-spacing: 1.5px;
  padding-top: 10px;
}

.logo-mini {
  display: none;
  font-size: 18px;
}

.gold-text {
  color: var(--gold-matte);
}

.user-panel {
  display: flex;
  padding: 20px 15px;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.user-panel .image img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 15px;
}

.user-panel .info p {
  font-weight: normal;
  margin-bottom: 5px;
  color: #fff;
  font-size: 14px;
}

.user-panel .info a {
  color: #aaa;
  text-decoration: none;
  font-size: 12px;
}

.text-success {
  color: #2ecc71;
}

.sidebar-menu {
  list-style: none;
  padding: 10px 15px;
  flex-grow: 1;
  overflow-y: auto;
}

.sidebar-menu::-webkit-scrollbar {
  width: 5px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: var(--wood-active);
  border-radius: 10px;
}

.sidebar-menu li {
  margin-bottom: 5px;
}

.sidebar-menu a {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  color: #dedede;
  text-decoration: none;
  transition: 0.2s;
  border-radius: 6px;
  font-size: 13.5px;
}

.sidebar-menu a:hover,
.sidebar-menu a.active {
  color: var(--gold-matte);
  background: var(--wood-active);
}

.sidebar-menu i {
  width: 28px;
  font-size: 15px;
  color: inherit;
}

/* --- HEADER NHÓM --- */
.menu-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 15px;
  color: #c2c7d0;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s, color 0.2s;
  font-size: 14px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.15);
  margin-top: 8px;
  user-select: none;
}

.menu-group-header:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.05);
}

.group-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-title i {
  color: var(--gold-matte);
}

/* HIỆU ỨNG MŨI TÊN LẬT 3D LÊN/XUỐNG */
.arrow-icon {
  font-size: 11px;
  transform-origin: center;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-block;
  /* BẮT BUỘC PHẢI CÓ DÒNG NÀY */
  opacity: 0.5;
}

.menu-open .arrow-icon {
  transform: rotateX(180deg);
  /* Lật 3D theo trục X */
  opacity: 1;
}

/* ========================================= */
/* CSS GRID TRICK - CHUẨN NHẤT CHO ACCORDION */
/* ========================================= */
.sub-menu-collapse {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.35s ease-in-out;
}

.menu-open .sub-menu-collapse {
  grid-template-rows: 1fr;
}

.sub-menu {
  min-height: 0;
  overflow: hidden;
  /* Bắt buộc để ẩn khi grid 0fr */
  list-style: none;
  padding-left: 15px;
  margin-left: 15px;
  border-left: 2px solid rgba(209, 170, 104, 0.2);
  opacity: 0;
  transition: opacity 0.35s ease-in-out, padding 0.35s ease-in-out;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  margin-bottom: 0;
}

.menu-open .sub-menu {
  opacity: 1;
  padding-top: 6px;
  padding-bottom: 6px;
  margin-bottom: 4px;
}

.sub-menu li {
  margin-bottom: 3px;
}

.sub-menu a {
  padding: 8px 12px;
  font-size: 13px;
}

/* COLLAPSED STATE (Khi thu gọn Sidebar) */
.sidebar-collapsed {
  width: 80px;
}

.sidebar-collapsed .logo-lg,
.sidebar-collapsed .user-panel .info,
.sidebar-collapsed .menu-group-header span,
.sidebar-collapsed .sidebar-menu span,
/* <-- THÊM DÒNG NÀY ĐỂ ẨN CHỮ CỦA TRANG QUẢN TRỊ */
.sidebar-collapsed .arrow-icon,
.sidebar-collapsed .sub-menu-collapse {
  display: none !important;
}

.sidebar-collapsed .logo-mini {
  display: block;
  color: var(--gold-matte);
  font-weight: 900;
  font-size: 26px;
  text-align: center;
  width: 100%;
  text-shadow: 0 0 10px rgba(209, 170, 104, 0.3);
}

.sidebar-collapsed .user-panel {
  padding: 15px 0;
  justify-content: center;
  background: transparent;
  border: none;
}

.sidebar-collapsed .user-panel .image img {
  margin-right: 0;
  width: 45px;
  height: 45px;
}

.sidebar-collapsed .sidebar-menu {
  padding: 10px;
}

.sidebar-collapsed .menu-group-header {
  justify-content: center;
  padding: 15px 0;
}

.sidebar-collapsed .group-title span {
  display: none;
}

.sidebar-collapsed .group-title i {
  margin: 0;
  font-size: 20px;
}

/* =======================================================
   ĐỒNG BỘ CĂN GIỮA TOÀN BỘ KHỐI VÀ ICON KHI THU GỌN 
======================================================= */
/* 1. Ép tất cả các khối (lẻ & nhóm) dùng chung 1 kiểu căn giữa và padding */
.sidebar-collapsed .sidebar-menu a,
.sidebar-collapsed .menu-group-header {
  justify-content: center !important;
  padding: 15px 0 !important;
}

/* 2. Đập bỏ cái width: 28px cũ của tất cả icon, ép về auto để nằm ngay tâm */
.sidebar-collapsed .sidebar-menu a i,
.sidebar-collapsed .group-title i {
  width: auto !important;
  margin: 0 !important;
  font-size: 20px !important;
}
</style>