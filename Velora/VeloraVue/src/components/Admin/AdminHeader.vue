<template>
  <header class="main-header">
    <nav class="navbar">
      <a href="#" class="sidebar-toggle" @click.prevent="$emit('toggle-sidebar')">
        <i class="fa-solid fa-bars"></i>
      </a>
      <div class="navbar-right">
        <ul class="nav-menu">
          <li>
            <a href="#">
              <i class="fa-regular fa-envelope"></i>
              <span class="badge bg-gold-matte">0</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa-regular fa-bell"></i>
              <span class="badge bg-gold-matte">0</span>
            </a>
          </li>
          <!-- NÚT QUAY VỀ TRANG CHỦ -->
          <li class="user-menu">
            <a href="#" @click.prevent="goToHome" class="home-btn" title="Về trang chủ website">
              <i class="fa-solid fa-house"></i> Trang chủ
            </a>
          </li>
          <!-- NÚT ĐĂNG XUẤT -->
          <li class="user-menu">
            <a href="#" @click.prevent="handleLogout" class="logout-btn" title="Đăng xuất khỏi hệ thống">
              <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
            </a>
          </li>
        </ul>
      </div>
    </nav>
  </header>
</template>

<script setup>
defineEmits(['toggle-sidebar']);

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

// Hàm điều hướng về trang chủ
const goToHome = () => {
  window.location.href = '/';
};

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/dang-nhap';
};
</script>

<style scoped>
/* CSS cho Header */
.main-header { height: 60px; background: var(--bg-page); z-index: 5; border-bottom: 1px solid var(--border-light, #eaeaea); }
.navbar { display: flex; justify-content: space-between; align-items: center; height: 100%; padding-right: 20px; }
.sidebar-toggle { color: var(--text-main); padding: 0 20px; font-size: 20px; text-decoration: none; line-height: 60px; transition: 0.2s; }
.sidebar-toggle:hover { color: var(--gold-matte); }
.navbar-right .nav-menu { list-style: none; display: flex; margin: 0; padding: 0; align-items: center; gap: 5px; }
.nav-menu li a { color: var(--text-main); text-decoration: none; padding: 8px 12px; position: relative; display: flex; align-items: center; gap: 6px; font-weight: 500; font-size: 14.5px; border-radius: 6px; transition: 0.2s; }
.nav-menu li a:hover { color: var(--gold-matte); background-color: rgba(204, 161, 94, 0.1); }
.badge { position: absolute; top: -2px; right: 2px; padding: 3px 5px; border-radius: 10px; font-size: 10px; color: #fff; line-height: 1; min-width: 15px; text-align: center; font-weight: bold; }
.bg-gold-matte { background-color: var(--gold-matte) !important; color: #fff; }
</style>