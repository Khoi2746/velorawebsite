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

                        <div v-if="isLoggedIn" class="user-greeting" ref="userMenuRef" @click="toggleDropdown">
                            <span>Hi, {{ userName }}</span>
                            <i class="fas fa-chevron-down" :class="{ 'open': showDropdown }"></i>

                            <transition name="fade-slide">
                                <div v-if="showDropdown" class="dropdown-menu" @click.stop>
                                    <router-link to="/thong-tin-ca-nhan" class="dropdown-item" @click="showDropdown = false">
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
                        <div class="search-trigger" @click="openSearch">
                            <i class="fas fa-search search-icon"></i>
                            <span>Tìm kiếm...</span>
                        </div>
                        
                        <div class="action-icons">
                            <router-link to="/don-hang" class="action-box">
                                <i class="fas fa-box"></i>
                                <span class="box-text">Đơn hàng</span>
                            </router-link>

                            <router-link to="/gio-hang" class="action-box cart-box">
                                <i class="fas fa-shopping-bag"></i>
                                <span class="box-text">Giỏ hàng</span>
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
                <router-link to="/bao-hanh" class="nav-item" active-class="active">BẢO HÀNH</router-link>
                <router-link to="/lien-he-tu-van" class="nav-item" active-class="active">LIÊN HỆ</router-link>
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

                <!-- Đã cập nhật: hiển thị Staff hoặc Admin tùy role -->
                <router-link v-if="isAdmin || isStaff" to="/admin/dashboard" @click="isMenuOpen = false"
                    style="color: #d1aa68; font-weight: bold; border-top: 1px solid #333; padding-top: 20px; margin-top: 10px;">
                    {{ isStaff ? 'STAFF DASHBOARD' : 'ADMIN DASHBOARD' }}
                </router-link>
            </nav>
        </div>
    </header>

    <transition name="fade-luxury">
        <!-- Phần này giữ nguyên -->
        <div v-if="isSearchOpen" class="nike-search-overlay" @click.self="closeSearch">
            <div class="nike-search-panel">
                <div class="search-header-layout">
                    <div class="search-logo-area">
                        <img src="../img/VeloraIcon.png" alt="Logo" class="logo-img-dark" />
                    </div>
                    <div class="search-center-area">
                        <div class="search-input-wrapper">
                            <i class="fas fa-search search-icon-inside"></i>
                            <input type="text" placeholder="Search" ref="searchInputRef" @keyup.enter="handleSearch" />
                        </div>
                        <div class="popular-searches">
                            <h4>Đề Xuất Sản Phẩm</h4>
                            <div class="search-tags">
                                <span class="tag" @click="quickSearch('Classic Fusion')">classic fusion</span>
                                <span class="tag" @click="quickSearch('Rolex Daytona')">rolex daytona</span>
                                <span class="tag" @click="quickSearch('Moonphase')">moonphase</span>
                                <span class="tag" @click="quickSearch('Noir Starlight')">noir starlight</span>
                            </div>
                        </div>
                    </div>
                    <div class="search-cancel-area">
                        <button class="btn-cancel" @click="closeSearch">Cancel</button>
                    </div>
                </div>
            </div>
        </div>
    </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'

const router = useRouter()
const isLoggedIn = ref(false)
const userName = ref('')
const isAdmin = ref(false)
const isStaff = ref(false) // Thêm biến isStaff

const isMenuOpen = ref(false)
const showDropdown = ref(false)
const userMenuRef = ref(null)

const isSearchOpen = ref(false)
const searchInputRef = ref(null)

const cartCount = ref(0)

const fetchCartCount = async () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        cartCount.value = 0;
        return;
    }
    try {
        const user = JSON.parse(userStr);
        const res = await fetch(`http://localhost:8080/api/gio-hang/${user.maNguoiDung}`);
        if (res.ok) {
            const cartItems = await res.json();
            cartCount.value = cartItems.length;
        }
    } catch (error) {
        console.error('Lỗi tải số đếm giỏ hàng:', error);
    }
}

const checkAuth = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            isLoggedIn.value = true;
            userName.value = user.hoTen;
            
            // Cập nhật logic check Role
            const role = user.vaiTro ? user.vaiTro.toUpperCase() : '';
            isAdmin.value = (role === 'ROLE_ADMIN');
            isStaff.value = (role === 'ROLE_CHUYEN_VIEN_TU_VAN');
            
            fetchCartCount(); 
        } catch (e) {
            console.error("Lỗi parse JSON:", e);
        }
    } else {
        isLoggedIn.value = false;
        isAdmin.value = false;
        isStaff.value = false;
        cartCount.value = 0; 
    }
}

const logout = () => {
    localStorage.removeItem('user')
    isLoggedIn.value = false
    isAdmin.value = false
    isStaff.value = false // Reset isStaff
    cartCount.value = 0 
    alert('Đã đăng xuất!')
    window.location.href = '/'
}

// ... các hàm còn lại (toggleDropdown, handleClickOutside, toggleMenu, ...) giữ nguyên không thay đổi
const toggleDropdown = () => { showDropdown.value = !showDropdown.value }
const handleClickOutside = (event) => { if (userMenuRef.value && !userMenuRef.value.contains(event.target)) showDropdown.value = false }
const toggleMenu = () => { isMenuOpen.value = !isMenuOpen.value; document.body.style.overflow = isMenuOpen.value ? 'hidden' : 'auto' }
const openSearch = () => { isSearchOpen.value = true; document.body.style.overflow = 'hidden'; nextTick(() => { if (searchInputRef.value) searchInputRef.value.focus() }) }
const closeSearch = () => { isSearchOpen.value = false; document.body.style.overflow = 'auto' }
const handleSearch = (e) => { const keyword = e.target.value.trim(); if (keyword) { router.push({ path: '/dong-ho-co-san', query: { search: keyword } }); e.target.value = '' }; closeSearch() }
const quickSearch = (keyword) => { router.push({ path: '/dong-ho-co-san', query: { search: keyword } }); closeSearch() }
const handleEsc = (e) => { if (e.key === 'Escape') { if (isMenuOpen.value) toggleMenu(); if (isSearchOpen.value) closeSearch() } }

onMounted(() => {
    checkAuth()
    window.addEventListener('keydown', handleEsc)
    document.addEventListener('click', handleClickOutside)
    window.addEventListener('cart-updated', fetchCartCount) 
})

onUnmounted(() => {
    window.removeEventListener('keydown', handleEsc)
    window.removeEventListener('click', handleClickOutside)
    window.removeEventListener('cart-updated', fetchCartCount) 
})
</script>

<style scoped>
@import "./CSS/Header.css";
</style>