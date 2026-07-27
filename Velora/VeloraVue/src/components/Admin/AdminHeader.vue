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
          <li class="user-menu">
            <a href="#" @click.prevent="handleLogout" class="logout-btn">
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

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/dang-nhap';
};
</script>

<style scoped>
/* Chỉ giữ CSS của Header ở đây */
.main-header { height: 60px; background: var(--bg-page); z-index: 5; }
.navbar { display: flex; justify-content: space-between; align-items: center; height: 100%; padding-right: 20px; }
.sidebar-toggle { color: var(--text-main); padding: 0 20px; font-size: 20px; text-decoration: none; line-height: 60px; transition: 0.2s; }
.sidebar-toggle:hover { color: var(--gold-matte); }
.navbar-right .nav-menu { list-style: none; display: flex; margin: 0; padding: 0; align-items: center; }
.nav-menu li a { color: var(--text-main); text-decoration: none; padding: 0 15px; position: relative; display: block; font-weight: 500; font-size: 15px; }
.nav-menu li a:hover { color: var(--gold-matte); }
.badge { position: absolute; top: -5px; right: 5px; padding: 3px 5px; border-radius: 10px; font-size: 10px; color: #fff; line-height: 1; min-width: 15px; text-align: center; font-weight: bold; }
.bg-gold-matte { background-color: var(--gold-matte) !important; color: #fff; }
</style>