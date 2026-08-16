<template>
    <div class="profile-page">
        <Header />

        <main class="profile-main container">
            <div class="page-title-box">
                <h1>THÔNG TIN CÁ NHÂN</h1>
                <div class="title-divider"><span class="diamond"></span></div>
            </div>

            <div class="profile-layout">
                <!-- CỘT TRÁI: SIDEBAR MENU -->
                <aside class="profile-sidebar">
                    <div class="user-avatar-box">
                        <div class="avatar-circle">
                            <i class="fas fa-user"></i>
                        </div>
                        <h3>{{ userInfo.hoTen || 'Nguyễn Minh Admin' }}</h3>
                        <p class="role-text">{{ userInfo.vaiTro === 'ADMIN' ? 'QUẢN TRỊ VIÊN' : 'THÀNH VIÊN VVIP' }}</p>
                    </div>

                    <nav class="profile-menu">
                        <a href="#" :class="{ active: activeTab === 'profile' }" @click.prevent="activeTab = 'profile'">
                            Hồ sơ của tôi
                        </a>
                        <a href="#" :class="{ active: activeTab === 'history' }" @click.prevent="activeTab = 'history'">
                            Lịch sử đơn hàng
                        </a>
                        <a href="#" class="logout-link" @click.prevent="handleLogout">
                            Đăng xuất
                        </a>
                    </nav>
                </aside>

                <!-- CỘT PHẢI: NỘI DUNG TƯƠNG ỨNG TỪNG TAB -->
                <section class="profile-content">

                    <!-- TAB 1: HỒ SƠ CỦA TÔI -->
                    <div v-if="activeTab === 'profile'" class="tab-pane">
                        <div class="content-header">
                            <h2>HỒ SƠ CỦA TÔI</h2>
                            <p>Quản lý thông tin bảo mật để nhận các đặc quyền từ Velora.</p>
                        </div>

                        <form class="profile-form" @submit.prevent="saveProfile">
                            <div class="form-group">
                                <label>HỌ VÀ TÊN</label>
                                <input type="text" v-model="userInfo.hoTen" placeholder="Nhập họ và tên..." />
                            </div>

                            <div class="form-group">
                                <label>EMAIL (TÀI KHOẢN)</label>
                                <input type="email" v-model="userInfo.email" disabled class="disabled-input" />
                            </div>

                            <div class="form-group">
                                <label>SỐ ĐIỆN THOẠI</label>
                                <input type="tel" v-model="userInfo.sdt" placeholder="Nhập số điện thoại liên hệ..." />
                            </div>

                            <div class="form-group">
                                <label>ĐỊA CHỈ GIAO HÀNG MẶC ĐỊNH</label>
                                <textarea v-model="userInfo.diaChi" rows="3"
                                    placeholder="Nhập địa chỉ nhận hàng chi tiết..."></textarea>
                            </div>

                            <button type="submit" class="btn-save">LƯU THAY ĐỔI</button>
                        </form>
                    </div>

                    <!-- TAB 2: LỊCH SỬ ĐƠN HÀNG (Hiển thị vĩnh viễn các đơn đã mua) -->
                    <div v-if="activeTab === 'history'" class="tab-pane">
                        <div class="content-header">
                            <h2>LỊCH SỬ MUA HÀNG VVIP</h2>
                            <p>Danh sách các kiệt tác thời gian bạn đã sở hữu. Hỗ trợ yêu cầu bảo hành nhanh chóng.</p>
                        </div>

                        <div class="history-list">
                            <div v-if="historyOrders.length === 0" class="empty-msg">
                                <i class="fas fa-box-open"></i>
                                <p>Bạn chưa có lịch sử mua hàng nào hoàn tất.</p>
                            </div>

                            <div v-else v-for="order in historyOrders" :key="order.maDonHang" class="history-card">
                                <div class="hc-header">
                                    <span class="hc-code">#{{ order.maDonHangCode }}</span>
                                    <span class="hc-date">Hoàn tất ngày: {{ new
                                        Date(order.ngayTao).toLocaleDateString('vi-VN') }}</span>
                                </div>

                                <div class="hc-body">
                                    <div class="hc-price">Tổng thanh toán: <strong>{{ formatPrice(order.tongTien)
                                            }}</strong></div>
                                    <!-- Hiển thị sản phẩm tóm tắt -->
                                    <div class="hc-items">
                                        <span v-for="(item, idx) in order.chiTietDonHangs" :key="idx" class="item-tag">
                                            {{ item.sanPham?.tenSanPham || 'Đồng hồ Velora' }} (x{{ item.soLuong }})
                                        </span>
                                    </div>
                                </div>

                                <div class="hc-footer">
                                    <span class="hc-status"><i class="fas fa-check-circle"></i> Đã giao hàng thành
                                        công</span>
                                    <!-- 🔥 NÚT YÊU CẦU BẢO HÀNH CHUYỂN TRANG -->
                                    <button class="btn-warranty" @click="goToWarrantyPage(order)">
                                        <i class="fas fa-shield-alt"></i> YÊU CẦU BẢO HÀNH
                                    </button>
                                </div>
                            </div>
                        </div>

                    </div>
                </section>
            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()
const activeTab = ref('profile') // Mặc định mở tab hồ sơ
const userInfo = ref({ hoTen: '', email: '', sdt: '', diaChi: '', vaiTro: '' })
const historyOrders = ref([])

const getUserData = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
        const data = JSON.parse(userStr)
        userInfo.value = {
            id: data.maNguoiDung || data.id,
            hoTen: data.hoTen,
            email: data.email,
            sdt: data.soDienThoai || '',
            diaChi: data.diaChi || '',
            vaiTro: data.vaiTro || 'USER'
        }
        loadHistoryOrders(userInfo.value.id)
    } else {
        router.push('/dang-nhap')
    }
}

// 🔥 Tải toàn bộ đơn hàng, chỉ lọc lấy đơn ĐÃ GIAO (Không xóa sau 48h)
const loadHistoryOrders = async (userId) => {
    try {
        const res = await axios.get(`http://localhost:8080/api/don-hang/nguoi-dung/${userId}`)
        if (res.data) {
            historyOrders.value = res.data.filter(order => order.trangThaiDonHang === 'DA_GIAO' || order.trangThaiDonHang === 'HOAN_TAT')
        }
    } catch (err) {
        console.error("Lỗi tải lịch sử mua hàng:", err)
    }
}

const saveProfile = async () => {
    // Logic gọi API update thông tin cá nhân của e
    alert("Cập nhật thông tin thành công!")
}

const handleLogout = () => {
    localStorage.removeItem('user')
    router.push('/dang-nhap')
}

// 🔥 HÀM ĐẨY SANG TRANG BẢO HÀNH
const goToWarrantyPage = (order) => {
    localStorage.setItem('selectedWarrantyOrder', JSON.stringify(order))
    router.push('/bao-hanh')
}

const formatPrice = (val) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
    getUserData()
})
</script>

<style scoped>
.profile-page {
    background: #f8f6f0;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    font-family: 'Segoe UI', Tahoma, sans-serif;
}

.profile-main {
    flex: 1;
    padding: 40px 20px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
}

.page-title-box {
    text-align: center;
    margin-bottom: 40px;
}

.page-title-box h1 {
    font-size: 24px;
    color: #362921;
    letter-spacing: 2px;
    font-weight: 600;
    margin-bottom: 10px;
}

.title-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.title-divider::before,
.title-divider::after {
    content: '';
    width: 50px;
    height: 1px;
    background-color: #cca15e;
}

.diamond {
    width: 8px;
    height: 8px;
    background-color: #cca15e;
    transform: rotate(45deg);
}

.profile-layout {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 30px;
    align-items: start;
}

/* SIDEBAR */
.profile-sidebar {
    background: #fff;
    border: 1px solid #eaeaea;
    padding: 30px 0;
}

.user-avatar-box {
    text-align: center;
    padding: 0 20px 20px;
    border-bottom: 1px solid #eaeaea;
}

.avatar-circle {
    width: 70px;
    height: 70px;
    background: #cca15e;
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin: 0 auto 15px;
}

.user-avatar-box h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 5px;
}

.role-text {
    font-size: 11px;
    color: #888;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.profile-menu {
    display: flex;
    flex-direction: column;
    margin-top: 10px;
}

.profile-menu a {
    padding: 15px 30px;
    color: #555;
    text-decoration: none;
    font-size: 14px;
    transition: 0.2s;
    border-left: 3px solid transparent;
}

.profile-menu a:hover {
    color: #cca15e;
    background: #fdfbf7;
}

.profile-menu a.active {
    color: #cca15e;
    font-weight: 600;
    border-left-color: #cca15e;
    background: #fdfbf7;
}

.logout-link {
    color: #dc2626 !important;
}

/* CONTENT PANE */
.profile-content {
    background: #fff;
    padding: 40px;
    border: 1px solid #eaeaea;
    min-height: 500px;
}

.content-header {
    margin-bottom: 30px;
}

.content-header h2 {
    font-size: 18px;
    color: #362921;
    font-weight: 700;
    margin-bottom: 8px;
}

.content-header p {
    font-size: 13px;
    color: #777;
}

/* FORM CÁ NHÂN */
.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    font-size: 12px;
    font-weight: 600;
    color: #555;
    margin-bottom: 8px;
    letter-spacing: 0.5px;
}

.profile-form input,
.profile-form textarea {
    width: 100%;
    padding: 12px 15px;
    border: 1px solid #ddd;
    outline: none;
    font-family: inherit;
    font-size: 14px;
    color: #333;
    transition: 0.2s;
}

.profile-form input:focus,
.profile-form textarea:focus {
    border-color: #cca15e;
}

.disabled-input {
    background: #f5f5f5;
    color: #999 !important;
    cursor: not-allowed;
}

.btn-save {
    background: #222;
    color: #fff;
    border: none;
    padding: 12px 30px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    letter-spacing: 1px;
    transition: 0.2s;
}

.btn-save:hover {
    background: #cca15e;
}

/* HISTORY LIST */
.history-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.empty-msg {
    text-align: center;
    color: #888;
    padding: 40px;
}

.empty-msg i {
    font-size: 40px;
    color: #ddd;
    margin-bottom: 15px;
}

.history-card {
    border: 1px solid #eaeaea;
    padding: 20px;
    background: #fafafa;
}

.hc-header {
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid #eaeaea;
    padding-bottom: 12px;
    margin-bottom: 12px;
}

.hc-code {
    font-weight: bold;
    color: #362921;
}

.hc-date {
    font-size: 12px;
    color: #666;
}

.hc-price {
    font-size: 14px;
    margin-bottom: 8px;
}

.hc-price strong {
    color: #cca15e;
    font-size: 16px;
}

.hc-items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 15px;
}

.item-tag {
    background: #eee;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
    color: #555;
}

.hc-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.hc-status {
    font-size: 13px;
    color: #16a34a;
    font-weight: 600;
}

.btn-warranty {
    background: #fff;
    border: 1px solid #cca15e;
    color: #cca15e;
    padding: 8px 15px;
    font-size: 12px;
    font-weight: bold;
    cursor: pointer;
    transition: 0.2s;
}

.btn-warranty:hover {
    background: #cca15e;
    color: #fff;
}
</style>