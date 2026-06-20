<template>
    <div class="brand-page">
        <Header />

        <main class="brand-content">
            <div class="container">
                <div class="title-wrapper">
                    <h1 class="page-title">THƯƠNG HIỆU</h1>
                    <div class="title-divider">
                        <span class="diamond"></span>
                    </div>
                </div>

                <div class="brand-grid" v-if="brands.length > 0">
                    <div class="brand-card" v-for="brand in brands" :key="brand.maThuongHieu">
                        <img v-if="brand.logoThuongHieu"
                            :src="brand.logoThuongHieu.startsWith('http') ? brand.logoThuongHieu : '/img/' + brand.logoThuongHieu"
                            :alt="brand.tenThuongHieu" class="brand-logo" />
                        <span v-else class="brand-name-placeholder">{{ brand.tenThuongHieu }}</span>
                    </div>
                </div>

                <div v-else class="loading-state">
                    <p>Đang tải danh sách thương hiệu hoặc chưa có thương hiệu nào đang hoạt động...</p>
                </div>

                <Info />

            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import Info from '../info.vue'

// Tự động nhúng Font Awesome nếu project của em chưa có để hiện icon điện thoại, map, email
onMounted(() => {
    if (!document.getElementById('font-awesome-cdn')) {
        const link = document.createElement('link')
        link.id = 'font-awesome-cdn'
        link.rel = 'stylesheet'
        link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'
        document.head.appendChild(link)
    }
})

const brands = ref([])

const fetchBrands = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/thuong-hieu')

        if (response.ok) {
            const data = await response.json()
            console.log("Dữ liệu từ DB trả về:", data)

            // BỘ LỌC THẦN THÁNH: Chỉ lấy những thương hiệu có trạng thái là 1 (hoặc true)
            brands.value = data.filter(brand => brand.trangThai === 1 || brand.trangThai === true)

        } else {
            console.error('Lỗi khi tải dữ liệu thương hiệu:', response.status)
        }
    } catch (error) {
        console.error('Không thể kết nối đến Backend. Spring Boot đã chạy chưa?', error)
    }
}

onMounted(() => {
    fetchBrands()
})
</script>

<style scoped>
/* ================= TỔNG THỂ TRANG ================= */
.brand-page {
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f8f8f8;
}

.brand-content {
    flex: 1;
    padding: 60px 20px 100px;
}

.container {
    max-width: 1400px;
    margin: 0 auto;
}

/* ================= TIÊU ĐỀ ================= */
.title-wrapper {
    text-align: center;
    margin-bottom: 60px;
}

.page-title {
    font-family: 'Arial', sans-serif;
    color: #555555;
    font-size: 24px;
    font-weight: normal;
    letter-spacing: 2px;
    margin-bottom: 15px;
}

.title-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
}

.title-divider::before,
.title-divider::after {
    content: '';
    display: block;
    width: 80px;
    height: 1px;
    background-color: #d1aa68;
}

.diamond {
    width: 8px;
    height: 8px;
    background-color: #d1aa68;
    transform: rotate(45deg);
}

/* ================= LƯỚI THƯƠNG HIỆU ================= */
.brand-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 15px;
    margin-bottom: 80px;
    /* Tạo khoảng cách an toàn với phần liên hệ */
}

.brand-card {
    background-color: #ffffff;
    aspect-ratio: 2.5 / 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
    cursor: pointer;
    transition: all 0.3s ease;
}

.brand-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.brand-logo {
    max-width: 80%;
    max-height: 80%;
    object-fit: contain;
    filter: grayscale(100%) opacity(0.6);
    transition: all 0.3s ease;
}

.brand-card:hover .brand-logo {
    filter: grayscale(0%) opacity(1);
}

.brand-name-placeholder {
    font-family: 'Times New Roman', serif;
    color: #888888;
    font-size: 15px;
    text-transform: uppercase;
    text-align: center;
    letter-spacing: 1px;
}

.loading-state {
    text-align: center;
    padding: 50px;
    color: #888;
}

/* ================= RESPONSIVE ================= */
@media (max-width: 1200px) {
    .brand-grid {
        grid-template-columns: repeat(4, 1fr);
    }

    .contact-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 40px;
    }

    .contact-item:not(:last-child)::after {
        display: none;
    }

    /* Ẩn vạch dọc khi vỡ dòng */
}

@media (max-width: 992px) {
    .brand-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 768px) {
    .brand-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 600px) {
    .contact-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 480px) {
    .brand-grid {
        grid-template-columns: 1fr;
    }
}
</style>