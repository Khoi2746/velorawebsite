<template>
    <header class="velora-header">
        <div class="header-top">
            <div class="header-container">
                <div class="top-left">
                    <button class="btn-menu" @click="isMenuOpen = true">
                        <i class="fa-solid fa-bars"></i>
                        <span>Menu</span>
                    </button>
                </div>

                <div class="top-center">
                    <router-link to="/" class="logo-link">
                        <img src="../img/VeloraIcon.png" alt="Logo" class="logo-img" />
                    </router-link>
                </div>

                <div class="top-right">
                    <div class="top-right-top">
                        <div v-if="isLoggedIn" class="user-greeting" @mouseenter="showDropdown = true"
                            @mouseleave="showDropdown = false">
                            <span>Hi, {{ userName }}</span>
                            <i class="fas fa-chevron-down"></i>
                            <div v-if="showDropdown" class="dropdown-menu">
                                <router-link to="/thong-tin-ca-nhan" class="dropdown-item">Thông tin cá
                                    nhân</router-link>
                                <button @click="logout" class="dropdown-item btn-logout-menu">Đăng xuất</button>
                            </div>
                        </div>
                        <div v-else class="auth-links">
                            <router-link to="/dang-ky" class="btn-register">Đăng ký</router-link>
                            <router-link to="/dang-nhap" class="btn-login">Đăng nhập</router-link>
                        </div>
                    </div>

                    <div class="top-right-bottom">
                        <div class="search-box">
                            <i class="fas fa-search search-icon"></i>
                            <input type="text" placeholder="Search" />
                        </div>
                        <div class="action-icons">
                            <router-link to="/yeu-thich"><i class="far fa-heart"></i></router-link>
                            <router-link to="/don-hang"><i class="fas fa-box"></i></router-link>
                            <router-link to="/gio-hang"><i class="fas fa-shopping-bag"></i></router-link>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="header-bottom">
            <nav class="nav-menu">
                <router-link to="/" class="nav-item" active-class="active" exact>TRANG CHỦ</router-link>
                <router-link to="/thuong-hieu" class="nav-item" active-class="active">THƯƠNG HIỆU</router-link>
                <router-link to="/dong-ho-co-san" class="nav-item" active-class="active">ĐỒNG HỒ CÓ SẴN</router-link>
            </nav>
        </div>

        <div class="menu-overlay" :class="{ 'active': isMenuOpen }">
            <button class="close-menu" @click="isMenuOpen = false">
                <i class="fa-solid fa-times"></i>
            </button>

            <nav class="overlay-nav">
                <router-link to="/" @click="isMenuOpen = false">TRANG CHỦ</router-link>
                <router-link to="/thuong-hieu" @click="isMenuOpen = false">THƯƠNG HIỆU</router-link>
                <router-link to="/dong-ho-co-san" @click="isMenuOpen = false">SẢN PHẨM</router-link>

                <router-link v-if="isAdmin" to="/admin/dashboard" @click="isMenuOpen = false"
                    style="color: #d1aa68; font-weight: bold; border-top: 1px solid #333; padding-top: 20px; margin-top: 10px;">
                    ADMIN DASHBOARD
                </router-link>
            </nav>
        </div>
    </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(false)
const userName = ref('')
const isAdmin = ref(false)
const isMenuOpen = ref(false)
const showDropdown = ref(false)

const toggleMenu = () => {
    isMenuOpen.value = !isMenuOpen.value
    document.body.style.overflow = isMenuOpen.value ? 'hidden' : 'auto'
}

const handleEsc = (e) => {
    if (e.key === 'Escape' && isMenuOpen.value) {
        toggleMenu()
    }
}

const checkAuth = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            isLoggedIn.value = true;
            userName.value = user.hoTen;

            // CHỐT CHẶN CUỐI CÙNG LÀ Ở ĐÂY:
            // Phải gọi đúng chữ "vaiTro" và check đúng chữ "ROLE_ADMIN" từ Spring Boot gửi qua
            isAdmin.value = (user.vaiTro && user.vaiTro.toUpperCase() === 'ROLE_ADMIN');

            console.log("Check Admin:", isAdmin.value); // In ra để xem Vue đã nhận quyền chưa
        } catch (e) {
            console.error("Lỗi parse JSON:", e);
        }
    } else {
        isLoggedIn.value = false;
        isAdmin.value = false;
    }
}

const logout = () => {
    localStorage.removeItem('user')
    isLoggedIn.value = false
    isAdmin.value = false
    alert('Đã đăng xuất!')
    window.location.href = '/' // Ép load lại toàn bộ trang
}

onMounted(() => {
    checkAuth()
    window.addEventListener('keydown', handleEsc)
})

onUnmounted(() => window.removeEventListener('keydown', handleEsc))
</script>

<style scoped>
/* ================= TỔNG THỂ ================= */
.velora-header {
    width: 100%;
    font-family: 'Arial', sans-serif;
}

.header-container {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 80px;
}

/* ================= TẦNG 1 (NỀN TỐI) ================= */
.header-top {
    background-color: #24201D;
    color: #ffffff;
}

.top-left {
    flex: 1;
}

.btn-menu {
    background: transparent;
    border: none;
    color: #ffffff;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    transition: all 0.3s ease;
    font-size: 14px;
    letter-spacing: 1px;
    text-transform: uppercase;
}

.btn-menu:hover {
    color: #d1aa68;
}

.btn-menu i {
    font-size: 16px;
    transition: transform 0.3s ease;
}

.btn-menu:hover i {
    transform: rotate(90deg);
}

.btn-menu span {
    font-weight: 500;
}

.top-center {
    flex: 1;
    text-align: center;
    display: flex;
    justify-content: center;
    align-items: center;
}

.logo-link {
    text-decoration: none;
    display: inline-block;
}

.logo-img {
    height: 50px;
    max-height: 50px;
    width: auto;
    max-width: 280px;
    object-fit: contain;
    transition: transform 0.3s ease;
}

.logo-img:hover {
    transform: scale(1.05);
}

/* PHẢI */
.top-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-end;
    gap: 10px;
}

.top-right-top {
    display: flex;
    justify-content: flex-end;
}

.top-right-bottom {
    display: flex;
    align-items: center;
    gap: 20px;
}

/* USER / AUTH */
.user-greeting {
    font-size: 14px;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    position: relative;
    padding: 10px 0;
}

.user-greeting i {
    font-size: 10px;
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    width: 200px;
    background-color: #1a1714;
    border: 1px solid #332d27;
    border-radius: 8px;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
    z-index: 1000;
    display: flex;
    flex-direction: column;
    padding: 10px 0;
}

.dropdown-item {
    text-decoration: none;
    color: #cccccc;
    padding: 10px 20px;
    font-size: 13px;
    transition: all 0.2s;
    background: none;
    border: none;
    text-align: left;
    cursor: pointer;
}

.dropdown-item:hover {
    background-color: #24201D;
    color: #d1aa68;
}

.btn-logout-menu {
    color: #ff5555;
}

.auth-links {
    display: flex;
    align-items: center;
    gap: 12px;
    white-space: nowrap;
}

.btn-register,
.btn-login {
    font-size: 12px;
    font-weight: bold;
    text-transform: uppercase;
    text-decoration: none;
    padding: 7px 16px;
    border-radius: 6px;
    white-space: nowrap;
    transition: all 0.3s ease;
}

.btn-register {
    color: #d1aa68;
    border: 1px solid #d1aa68;
    background-color: transparent;
}

.btn-register:hover {
    background-color: rgba(209, 170, 104, 0.1);
}

.btn-login {
    color: #111111;
    background-color: #d1aa68;
    border: 1px solid #d1aa68;
}

.btn-login:hover {
    background-color: #b8955b;
    border-color: #b8955b;
}

/* SEARCH & ICONS */
.search-box {
    background-color: #ffffff;
    border-radius: 20px;
    padding: 5px 15px;
    display: flex;
    align-items: center;
    width: 180px;
}

.search-icon {
    color: #000000;
    font-size: 14px;
}

.search-box input {
    border: none;
    outline: none;
    background: transparent;
    padding-left: 8px;
    width: 100%;
    font-size: 13px;
    color: #333;
}

.action-icons {
    display: flex;
    gap: 15px;
}

.action-icons a {
    color: #ffffff;
    font-size: 20px;
    text-decoration: none;
    transition: color 0.3s;
}

.action-icons a:hover {
    color: #d1aa68;
}

/* ================= TẦNG 2 (NỀN TRẮNG) ================= */
.header-bottom {
    background-color: #ffffff;
    border-bottom: 1px solid #e0e0e0;
}

.nav-menu {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: center;
    gap: 50px;
    height: 50px;
}

.nav-item {
    text-decoration: none;
    color: #111111;
    font-size: 15px;
    font-weight: 500;
    line-height: 50px;
    text-transform: uppercase;
    border-bottom: 3px solid transparent;
    transition: all 0.3s ease;
}

.nav-item:hover,
.nav-item.active {
    color: #d1aa68;
    border-bottom: 3px solid #d1aa68;
}

/* ================= MENU OVERLAY (MENU TRƯỢT) ================= */
.menu-overlay {
    position: fixed;
    top: 0;
    left: -300px;
    width: 300px;
    height: 100%;
    background: #1a1714;
    transition: 0.5s cubic-bezier(0.77, 0, 0.175, 1);
    z-index: 9999;
    padding: 40px;
    box-shadow: 5px 0 15px rgba(0, 0, 0, 0.5);
}

.menu-overlay.active {
    left: 0;
}

.close-menu {
    background: none;
    border: none;
    color: #d1aa68;
    font-size: 24px;
    cursor: pointer;
    margin-bottom: 20px;
}

.overlay-nav {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.overlay-nav a {
    color: #fff;
    text-decoration: none !important;
    font-size: 18px;
    /* transition: all giúp mọi thay đổi đều mượt mà */
    transition: all 0.3s ease; 
    /* display: inline-block bắt buộc phải có để dùng được hiệu ứng transform */
    display: inline-block; 
    /* Giữ tâm thu nhỏ ở lề trái, không bị giật vào giữa */
    transform-origin: left center; 
}

.overlay-nav a:hover {
    color: #d1aa68;
    /* translateX(10px): Đẩy sang phải 10px */
    /* scale(0.9): Thu nhỏ chữ lại còn 90% so với ban đầu */
    transform: translateX(10px) scale(0.9); 
}
</style>