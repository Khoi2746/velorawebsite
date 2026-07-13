<template>
    <footer class="velora-footer" ref="footerRef">
        <div class="footer-container">

            <div class="footer-col fade-left">
                <img src="../img/VeloraIcon.png" alt="Velora Logo" class="footer-logo" />
                <p class="brand-desc">
                    Velora Clock mang đến sự kết hợp hoàn hảo giữa kỹ thuật cơ khí chính xác
                    và nghệ thuật thiết kế đương đại. Định nghĩa lại khái niệm thời gian của bạn.
                </p>
                <div class="social-icons">
                    <a href="#"><i class="fab fa-facebook"></i></a>
                    <a href="#"><i class="fab fa-instagram"></i></a>
                    <a href="#"><i class="fab fa-youtube"></i></a>
                </div>
            </div>

            <div class="footer-col fade-up">
                <h4>SẢN PHẨM</h4>
                <ul>
                    <li><router-link to="/dong-ho-co-san">Đồng hồ có sẵn</router-link></li>
                    <li><router-link to="/thuong-hieu">Thương hiệu</router-link></li>
                    <li><router-link to="/thu-mua-nhap-doi">Thu mua & Nhập đổi</router-link></li>
                </ul>
            </div>

            <div class="footer-col fade-down">
                <h4>HỖ TRỢ</h4>
                <ul>
                    <li><router-link to="/tuyen-dung">Tuyển dụng</router-link></li>
                    <li><a href="#">Chính sách bảo hành</a></li>
                    <li><a href="#">Hướng dẫn mua hàng</a></li>
                </ul>
            </div>

            <div class="footer-col fade-right">
                <h4>LIÊN HỆ</h4>
                <p><i class="fas fa-map-marker-alt"></i> District 1, Ho Chi Minh City</p>
                <p><i class="fas fa-phone"></i> 0901.234.567</p>
                <p><i class="fas fa-envelope"></i> ìno@velora.com.vn</p>
            </div>
        </div>

        <div class="footer-bottom fade-zoom">
            <p>&copy; 2026 Velora Clock. Bản quyền thuộc về Velora.co</p>
        </div>
    </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const footerRef = ref(null)

onMounted(() => {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Cuộn xuống thấy footer: Thêm class 'in-view' để kích hoạt mọi animation
                entry.target.classList.add('in-view')
            } else {
                // Cuộn lên khuất footer: Gỡ class 'in-view' để reset, lần sau cuộn xuống sẽ bay lại
                entry.target.classList.remove('in-view')
            }
        })
    }, { threshold: 0.15 }) // Footer hiện ra 15% là bắt đầu hiệu ứng

    if (footerRef.value) {
        observer.observe(footerRef.value)
    }
})
</script>

<style scoped>
@import "./CSS/Footer.css";
</style>