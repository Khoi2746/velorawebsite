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
                    <!-- ĐÃ BỔ SUNG: Gắn sự kiện click gọi hàm goToBrand -->
                    <div class="brand-card" v-for="brand in brands" :key="brand.maThuongHieu" @click="goToBrand(brand.maThuongHieu)">
                        <img v-if="brand.logoThuongHieu"
                            :src="brand.logoThuongHieu.startsWith('http') ? brand.logoThuongHieu : '/img/' + brand.logoThuongHieu"
                            :alt="brand.tenThuongHieu" class="brand-logo" />
                        <span v-else class="brand-name-placeholder">{{ brand.tenThuongHieu }}</span>
                    </div>
                </div>

                <div v-else class="loading-state">
                    <p>Đang tải danh sách thương hiệu hoặc chưa có thương hiệu nào đang hoạt động...</p>
                </div>

            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router' // ĐÃ BỔ SUNG: Import vue-router
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import Info from '../info.vue'

// Khởi tạo router để chuyển trang
const router = useRouter()

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

// ĐÃ BỔ SUNG: Hàm chuyển hướng sang trang Đồng Hồ Có Sẵn kèm ID thương hiệu
const goToBrand = (idThuongHieu) => {
    router.push({ 
        path: '/dong-ho-co-san', 
        query: { brand: idThuongHieu } 
    })
}

onMounted(() => {
    fetchBrands()
})
</script>

<style scoped>
@import "../CSS/User/ThuongHieu.css";
</style>