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
                        <ThemeToggle />

                        <!-- ĐÃ SỬA: Đổi sang sự kiện @click, thêm ref="userMenuRef" để bắt click ra ngoài -->
                        <div v-if="isLoggedIn" class="user-greeting" ref="userMenuRef" @click="toggleDropdown">
                            <span>Hi, {{ userName }}</span>
                            <!-- Icon xoay khi mở -->
                            <i class="fas fa-chevron-down" :class="{ 'open': showDropdown }"></i>

                            <!-- Menu xổ xuống -->
                            <transition name="fade-slide">
                                <div v-if="showDropdown" class="dropdown-menu" @click.stop>
                                    <router-link to="/thong-tin-ca-nhan" class="dropdown-item"
                                        @click="showDropdown = false">
                                        Thông tin cá nhân
                                    </router-link>
                                    <button @click="logout" class="dropdown-item btn-logout-menu">Đăng xuất</button>
                                </div>
                            </transition>
                        </div>
                        <div v-else class="auth-links">
                            <router-link to="/dang-ky" class="btn-register">Đăng ký</router-link>
                            <router-link to="/dang-nhap" class="btn-login">Đăng nhập</router-link>
                        </div>
                    </div>

                    <div class="top-right-bottom">
                        <!-- Nút kích hoạt mở Popup Tìm kiếm -->
                        <div class="search-trigger" @click="openSearch">
                            <i class="fas fa-search search-icon"></i>
                            <span>Tìm kiếm...</span>
                        </div>
                        <div class="action-icons">
                            <!-- Ô vuông Đơn Hàng -->
                            <router-link to="/don-hang" class="action-box">
                                <i class="fas fa-box"></i>
                                <span class="box-text">Đơn hàng</span>
                            </router-link>

                            <!-- Ô vuông Giỏ Hàng -->
                            <router-link to="/gio-hang" class="action-box cart-box">
                                <i class="fas fa-shopping-bag"></i>
                                <span class="box-text">Giỏ hàng</span>
                                <!-- Chấm vàng hiển thị số lượng (chỉ hiện khi có hàng) -->
                                <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
                            </router-link>
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

    <!-- Màn hình Popup Tìm kiếm phong cách Nike -->
    <transition name="slide-down">
        <div v-if="isSearchOpen" class="nike-search-overlay" @click.self="closeSearch">
            <!-- Panel trắng trượt xuống -->
            <div class="nike-search-panel">
                <div class="search-header-layout">
                    <!-- Trái: Logo -->
                    <div class="search-logo-area">
                        <img src="../img/VeloraIcon.png" alt="Logo" class="logo-img-dark" />
                    </div>

                    <!-- Giữa: Ô tìm kiếm & Từ khóa -->
                    <div class="search-center-area">
                        <div class="search-input-wrapper">
                            <i class="fas fa-search search-icon-inside"></i>
                            <input type="text" placeholder="Search" ref="searchInputRef" @keyup.enter="handleSearch" />
                        </div>

                        <div class="popular-searches">
                            <h4>Popular Search Terms</h4>
                            <div class="search-tags">
                                <span class="tag" @click="quickSearch('Classic Fusion')">classic fusion</span>
                                <span class="tag" @click="quickSearch('Rolex Daytona')">rolex daytona</span>
                                <span class="tag" @click="quickSearch('Moonphase')">moonphase</span>
                                <span class="tag" @click="quickSearch('Noir Starlight')">noir starlight</span>
                            </div>
                        </div>
                    </div>

                    <!-- Phải: Nút Cancel -->
                    <div class="search-cancel-area">
                        <button class="btn-cancel" @click="closeSearch">Cancel</button>
                    </div>
                </div>
            </div>
        </div>
    </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'

// ================= 1. KHAI BÁO BIẾN =================
const router = useRouter()
const isLoggedIn = ref(false)
const userName = ref('')
const isAdmin = ref(false)

const isMenuOpen = ref(false)
const showDropdown = ref(false)
const userMenuRef = ref(null)

const isSearchOpen = ref(false)
const searchInputRef = ref(null)

const cartCount = ref(0)

// ================= 2. LOGIC TÀI KHOẢN (AUTH) =================
const checkAuth = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            isLoggedIn.value = true;
            userName.value = user.hoTen;
            // Chốt chặn kiểm tra Admin
            isAdmin.value = (user.vaiTro && user.vaiTro.toUpperCase() === 'ROLE_ADMIN');
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
    window.location.href = '/'
}

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value
}

// Hàm tự động đóng menu user khi click ra ngoài
const handleClickOutside = (event) => {
    if (userMenuRef.value && !userMenuRef.value.contains(event.target)) {
        showDropdown.value = false
    }
}

// ================= 3. LOGIC MENU TRƯỢT & TÌM KIẾM =================
const toggleMenu = () => {
    isMenuOpen.value = !isMenuOpen.value
    document.body.style.overflow = isMenuOpen.value ? 'hidden' : 'auto'
}

const openSearch = () => {
    isSearchOpen.value = true
    document.body.style.overflow = 'hidden'
    nextTick(() => {
        if (searchInputRef.value) searchInputRef.value.focus()
    })
}

const closeSearch = () => {
    isSearchOpen.value = false
    document.body.style.overflow = 'auto'
}

const handleSearch = (e) => {
    const keyword = e.target.value
    console.log("Đang tìm kiếm:", keyword)
    // router.push({ path: '/tim-kiem', query: { q: keyword } })
    closeSearch()
}

const quickSearch = (keyword) => {
    if (searchInputRef.value) {
        searchInputRef.value.value = keyword
        searchInputRef.value.focus()
    }
}

// Xử lý bấm phím ESC để tắt các popup
const handleEsc = (e) => {
    if (e.key === 'Escape') {
        if (isMenuOpen.value) toggleMenu()
        if (isSearchOpen.value) closeSearch()
    }
}

// ================= 4. LOGIC ĐẾM GIỎ HÀNG =================
const updateCartCount = () => {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]')
    cartCount.value = cart.length
}

// ================= 5. VÒNG ĐỜI VUE (GỌI ĐÚNG 1 LẦN DƯỚI CÙNG) =================
onMounted(() => {
    checkAuth()
    updateCartCount()
    window.addEventListener('keydown', handleEsc)
    document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
    window.removeEventListener('keydown', handleEsc)
    document.removeEventListener('click', handleClickOutside)
})
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
    align-items: center;
    /* ĐÃ THÊM: Căn giữa theo chiều dọc cho nút đổi màu và thông tin user */
    gap: 20px;
    /* ĐÃ THÊM: Tạo khoảng cách giữa nút đổi màu và cục user */
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

/* USER / AUTH - GIAO DIỆN VUÔNG LUXURY */
.user-greeting {
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    position: relative;
    padding: 8px 15px;
    border: 1px solid #e0e0e0;
    /* Tạo khung vuông mỏng */
    text-transform: uppercase;
    letter-spacing: 1px;
    transition: all 0.3s ease;
}

.user-greeting:hover {
    border-color: #d1aa68;
    color: #d1aa68;
}

.user-greeting i {
    font-size: 10px;
    transition: transform 0.3s ease;
}

/* Xoay mũi tên 180 độ khi menu mở */
.user-greeting i.open {
    transform: rotate(180deg);
}

.dropdown-menu {
    position: absolute;
    top: calc(100% + 5px);
    /* Cách mép dưới một khoảng nhỏ */
    right: 0;
    width: 200px;
    background-color: #ffffff;
    border: 1px solid #eeeeee;
    border-radius: 0;
    /* XÓA BỎ BO GÓC - ÉP THÀNH KHỐI VUÔNG CHUẨN MEN */
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
    /* Đổ bóng nhẹ nhàng */
    z-index: 1000;
    display: flex;
    flex-direction: column;
    padding: 10px 0;
}

.dropdown-item {
    text-decoration: none;
    color: #111111;
    padding: 12px 20px;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 2px;
    text-transform: uppercase;
    transition: all 0.3s ease;
    background: none;
    border: none;
    border-left: 3px solid transparent;
    /* Chuẩn bị vạch kẻ bên trái */
    text-align: left;
    cursor: pointer;
}

/* Hiệu ứng rê chuột: Trượt chữ sang phải và hiện vạch vàng kim */
.dropdown-item:hover {
    background-color: #f9f9f9;
    color: #d1aa68;
    border-left: 3px solid #d1aa68;
    padding-left: 25px;
}

.btn-logout-menu {
    color: #ff5555;
}

.btn-logout-menu:hover {
    color: #ff5555;
    border-left-color: #ff5555;
}

/* Hiệu ứng chuyển động mượt mà khi menu xổ xuống */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-10px);
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

/* ================= Ô TÌM KIẾM (GIAO DIỆN VUÔNG LUXURY) ================= */
.search-trigger {
    background-color: transparent !important;
    border: 1px solid #444444 !important; /* Ép viền xám sáng lên */
    padding: 5px 20px !important;
    display: flex !important;
    align-items: center !important;
    gap: 12px !important; /* Tách xa cái kính lúp và chữ ra */
    cursor: pointer !important;
    transition: all 0.3s ease !important;
    /* Ép chiều cao và form vuông vức bằng với nút Đơn Hàng */
    border-radius: 0 !important; 
}

/* 1. Fix kính lúp bị đen thui */
.search-trigger .search-icon {
    color: #ffffff !important; 
    font-size: 14px !important;
    margin: 0 !important; /* Xóa margin lộn xộn cũ */
}

/* 2. Fix chữ bị to và chệch dòng */
.search-trigger span {
    color: #ffffff !important;
    font-size: 11px !important;
    font-weight: 600 !important;
    text-transform: uppercase !important;
    letter-spacing: 1.5px !important;
    white-space: nowrap !important; /* Bắt buộc nằm trên 1 dòng */
}

/* 3. Hiệu ứng Hover Vàng Kim sang chảnh */
.search-trigger:hover {
    border-color: #d1aa68 !important;
    background-color: rgba(209, 170, 104, 0.05) !important;
}

.search-trigger:hover .search-icon,
.search-trigger:hover span {
    color: #d1aa68 !important;
}
/* ================= ICON HÀNH ĐỘNG (ĐƠN HÀNG, GIỎ HÀNG) LUXURY ================= */
/* ================= Ô VUÔNG HÀNH ĐỘNG (ĐƠN HÀNG, GIỎ HÀNG) ================= */
.action-icons {
    display: flex;
    align-items: center;
    gap: 15px;
    /* Khoảng cách giữa 2 ô vuông */
}

.action-box {
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 5px 20px;
    /* Thu nhỏ padding ngang lại một chút cho vừa vặn */
    border: 1px solid #444444;
    color: #ffffff;
    text-decoration: none;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    transition: all 0.3s ease;

    /* BÍ KÍP Ở ĐÂY: Ép chữ không bao giờ bị rớt dòng */
    white-space: nowrap;
}

.action-box i {
    font-size: 14px;
}

/* Hiệu ứng khi rê chuột: Viền và chữ sáng lên màu Vàng Kim, lót nền mờ */
.action-box:hover {
    border-color: #d1aa68;
    color: #d1aa68;
    background-color: rgba(209, 170, 104, 0.05);
}

/* Chấm báo số lượng giỏ hàng (Được dời lên góc trên cùng bên phải ô vuông) */
.cart-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background-color: #d1aa68;
    color: #111111;
    font-size: 10px;
    font-weight: 700;
    min-width: 18px;
    height: 18px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 5px;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.3);
    border: 2px solid #24201D;
    /* Viền cắt ngang tiệp màu nền Header tối */
    transition: transform 0.3s ease;
}

.action-box:hover .cart-badge {
    transform: scale(1.1);
    /* Phóng to chấm vàng nhẹ khi hover */
}

/* Chấm báo số lượng giỏ hàng (Cart Badge) */

/* Khi hover, chấm vàng cũng sáng lên */
.icon-link:hover .cart-badge {
    background-color: #ffffff;
    transform: scale(1.1);
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
    transition: all 0.3s ease;
    display: inline-block;
    transform-origin: left center;
}

.overlay-nav a:hover {
    color: #d1aa68;
    transform: translateX(10px) scale(0.9);
}

/* ================= POPUP TÌM KIẾM PHONG CÁCH NIKE ================= */
/* Lớp phủ làm mờ nền phía sau */
.nike-search-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    z-index: 10000;
}

/* Panel màu trắng trượt từ trên xuống */
.nike-search-panel {
    background-color: #ffffff;
    width: 100%;
    padding: 30px 40px 50px 40px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.search-header-layout {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    max-width: 1400px;
    margin: 0 auto;
    gap: 40px;
}

/* Cột Trái: Logo */
.search-logo-area {
    flex: 1;
    padding-top: 5px;
}

.logo-img-dark {
    height: 35px;
    filter: brightness(0);
    /* Ép logo thành màu đen tuyền */
}

/* Cột Giữa: Thanh search & Tags */
.search-center-area {
    flex: 3;
    max-width: 700px;
}

.search-input-wrapper {
    position: relative;
    background-color: #f5f5f5;
    /* Xám nhạt */
    border-radius: 50px;
    /* Bo tròn mạnh như viên thuốc */
    display: flex;
    align-items: center;
    padding: 12px 25px;
    transition: background-color 0.3s ease;
}

.search-input-wrapper:hover {
    background-color: #eeeeee;
}

.search-icon-inside {
    font-size: 16px;
    color: #111111;
    margin-right: 15px;
}

.search-input-wrapper input {
    border: none;
    background: transparent;
    width: 100%;
    font-size: 16px;
    font-weight: 500;
    color: #111111;
    outline: none;
}

/* Phần từ khóa phổ biến */
.popular-searches {
    margin-top: 35px;
}

.popular-searches h4 {
    font-size: 13px;
    color: #777777;
    font-weight: 500;
    margin-bottom: 15px;
}

.search-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.tag {
    background-color: #f5f5f5;
    color: #111111;
    padding: 10px 22px;
    border-radius: 30px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.tag:hover {
    background-color: #e5e5e5;
}

/* Cột Phải: Nút Cancel */
.search-cancel-area {
    flex: 1;
    text-align: right;
    padding-top: 15px;
}

.btn-cancel {
    background: none;
    border: none;
    color: #111111;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
}

.btn-cancel:hover {
    color: #777777;
}

/* Hiệu ứng trượt mượt mà từ trên xuống */
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.slide-down-enter-active .nike-search-panel,
.slide-down-leave-active .nike-search-panel {
    transition: transform 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.slide-down-enter-from {
    opacity: 0;
}

.slide-down-enter-from .nike-search-panel {
    transform: translateY(-100%);
}

.slide-down-leave-to {
    opacity: 0;
}

.slide-down-leave-to .nike-search-panel {
    transform: translateY(-100%);
}
</style>