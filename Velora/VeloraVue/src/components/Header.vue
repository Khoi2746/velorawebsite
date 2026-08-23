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
                        <!-- 🔥 PREMIUM SEARCH TOGGLE UI -->
                        <div class="search-toggle-container">
                            <div class="premium-search" :class="{ open: isSearchOpen }">
                                <span class="premium-icon" @click="toggleSearch">
                                    <i class="fa-solid fa-magnifying-glass"></i>
                                </span>
                                
                                <input 
                                    ref="searchInputRef"
                                    type="text" 
                                    class="premium-input" 
                                    v-model="searchKeyword"
                                    @keyup.enter="handleSearch"
                                    placeholder="Tìm kiếm tuyệt tác..." 
                                />

                                <span class="close-btn" @click="closeSearch" v-if="isSearchOpen">
                                    <i class="fa-solid fa-xmark"></i>
                                </span>
                            </div>
                            
                            <div class="esc-hint" v-if="isSearchOpen">
                                <span class="esc-key">Esc</span> Đóng
                            </div>
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

        <!-- Menu Mobile Overlay -->
        <div class="menu-overlay" :class="{ 'active': isMenuOpen }">
            <button class="close-menu" @click="isMenuOpen = false">
                <i class="fa-solid fa-times"></i>
            </button>

            <nav class="overlay-nav">
                <router-link to="/" @click="isMenuOpen = false">TRANG CHỦ</router-link>
                <router-link to="/thuong-hieu" @click="isMenuOpen = false">THƯƠNG HIỆU</router-link>
                <router-link to="/dong-ho-co-san" @click="isMenuOpen = false">SẢN PHẨM</router-link>

                <router-link v-if="isAdmin || isStaff" to="/admin/dashboard" @click="isMenuOpen = false"
                    style="color: #d1aa68; font-weight: bold; border-top: 1px solid #333; padding-top: 20px; margin-top: 10px;">
                    {{ isStaff ? 'STAFF DASHBOARD' : 'ADMIN DASHBOARD' }}
                </router-link>
            </nav>
        </div>
    </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'

const router = useRouter()
const isLoggedIn = ref(false)
const userName = ref('')
const isAdmin = ref(false)
const isStaff = ref(false)

const isMenuOpen = ref(false)
const showDropdown = ref(false)
const userMenuRef = ref(null)

const isSearchOpen = ref(false)
const searchKeyword = ref('')
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
    isStaff.value = false
    cartCount.value = 0 
    alert('Đã đăng xuất!')
    window.location.href = '/'
}

const toggleDropdown = () => { showDropdown.value = !showDropdown.value }
const handleClickOutside = (event) => { if (userMenuRef.value && !userMenuRef.value.contains(event.target)) showDropdown.value = false }

// Logic điều khiển Search Toggle
const toggleSearch = () => {
    isSearchOpen.value = !isSearchOpen.value
    if (isSearchOpen.value) {
        nextTick(() => {
            searchInputRef.value?.focus()
        })
    } else {
        searchKeyword.value = ''
    }
}

const closeSearch = () => {
    isSearchOpen.value = false
    searchKeyword.value = ''
}

const handleSearch = () => {
    const keyword = searchKeyword.value.trim()
    if (keyword) {
        router.push({ path: '/dong-ho-co-san', query: { search: keyword } })
        closeSearch()
    }
}

const handleEsc = (e) => { 
    if (e.key === 'Escape') { 
        if (isMenuOpen.value) isMenuOpen.value = false; 
        if (isSearchOpen.value) closeSearch(); 
    } 
}

onMounted(() => {
    checkAuth()
    window.addEventListener('keydown', handleEsc)
    document.addEventListener('click', handleClickOutside)
    window.addEventListener('cart-updated', fetchCartCount) 
})

onUnmounted(() => {
    window.removeEventListener('keydown', handleEsc)
    document.removeEventListener('click', handleClickOutside)
    window.removeEventListener('cart-updated', fetchCartCount) 
})
</script>

<style scoped>
@import "./CSS/Header.css";

/* 🔥 1. Chốt cứng kích thước vùng chứa để giữ chỗ */
.search-toggle-container {
    position: relative;
    display: inline-block;
    width: 42px;
    height: 42px;
}

/* 🔥 2. Cho khung tìm kiếm LUÔN LUÔN là absolute và neo bên phải */
.premium-search {
    position: absolute;
    right: 0;
    top: 0;
    width: 42px;
    height: 42px;
    box-sizing: border-box;
    background: #ffffff;
    border: 2px solid #c5a880;
    border-radius: 42px; /* Bo tròn đều các góc */
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    /* Chuyển động mượt mà tập trung vào width */
    transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s ease;
    display: flex;
    align-items: center;
    z-index: 100;
}

/* 🔥 3. Khi mở, chỉ thay đổi width, nó sẽ tự trượt qua trái cực mượt */
.premium-search.open {
    width: 240px;
    padding: 0 8px; /* Thêm padding khi mở */
    box-shadow: 0 8px 25px rgba(197, 168, 128, 0.3);
}

.premium-icon {
    min-width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #c5a880;
    font-size: 15px;
    flex-shrink: 0;
    transition: color 0.2s;
}

.premium-icon:hover {
    color: #1a1a1a;
}

.premium-input {
    flex: 1;
    min-width: 0; 
    width: 100%;
    border: none;
    outline: none;
    font-size: 14px;
    font-family: 'Montserrat', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    letter-spacing: 0.5px;
    background: transparent;
    color: #1a1a1a;
    padding: 0 6px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.premium-input::placeholder {
    color: #888;
    font-family: 'Montserrat', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    font-style: normal;
    letter-spacing: 0.5px;
}

/* Chờ khung trượt ra một chút rồi mới hiện chữ */
.premium-search.open .premium-input {
    opacity: 1;
    transition-delay: 0.2s;
}

.premium-input::placeholder {
    color: #888;
    font-style: italic;
}

.close-btn {
    width: 26px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #777;
    font-size: 13px;
    border-radius: 50%;
    transition: all 0.2s;
    flex-shrink: 0;
}

.close-btn:hover {
    background: #f0f0f0;
    color: #1a1a1a;
}

.esc-hint {
    position: absolute;
    top: -32px;
    right: 0;
    background: #1a1a1a;
    color: #fff;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 10px;
    display: flex;
    align-items: center;
    gap: 5px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.15);
    z-index: 101;
    white-space: nowrap;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.esc-key {
    background: #333;
    border: 1px solid #555;
    padding: 1px 4px;
    border-radius: 3px;
    font-family: monospace;
    color: #c5a880;
    font-weight: bold;
}
</style>