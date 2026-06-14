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
.velora-footer {
    background-color: #0d0d0d;
    color: #cccccc;
    padding: 60px 20px 20px;
    border-top: 1px solid #332d27;
    overflow: hidden;
    /* Cực kỳ quan trọng: Ngăn thanh cuộn ngang khi các khối bay từ ngoài vào */
}

/* ================= ĐỊNH NGHĨA TRẠNG THÁI ẨN BAN ĐẦU (KHI CHƯA CUỘN TỚI) ================= */
.fade-left {
    opacity: 0;
    transform: translateX(-60px);
    transition: all 0.8s ease-out;
}

.fade-right {
    opacity: 0;
    transform: translateX(60px);
    transition: all 0.8s ease-out;
}

.fade-up {
    opacity: 0;
    transform: translateY(60px);
    transition: all 0.8s ease-out;
}

.fade-down {
    opacity: 0;
    transform: translateY(-60px);
    transition: all 0.8s ease-out;
}

.fade-zoom {
    opacity: 0;
    transform: scale(0.8);
    transition: all 0.8s ease-out;
}

/* ================= TRẠNG THÁI HIỂN THỊ (KHI CÓ CLASS IN-VIEW TỪ JAVASCRIPT) ================= */
/* Mọi phần tử sẽ trở về đúng vị trí (0) và hiện rõ (1) */
.in-view .fade-left,
.in-view .fade-right,
.in-view .fade-up,
.in-view .fade-down,
.in-view .fade-zoom {
    opacity: 1;
    transform: translate(0) scale(1);
}

/* ================= HIỆU ỨNG DELAY (BAY VÀO LẦN LƯỢT) ================= */
.in-view .fade-left {
    transition-delay: 0s;
}

/* Cột 1 bay vào ngay */
.in-view .fade-up {
    transition-delay: 0.2s;
}

/* Cột 2 đợi 0.2s */
.in-view .fade-down {
    transition-delay: 0.4s;
}

/* Cột 3 đợi 0.4s */
.in-view .fade-right {
    transition-delay: 0.6s;
}

/* Cột 4 đợi 0.6s */
.in-view .fade-zoom {
    transition-delay: 0.8s;
}

/* Dòng cuối cùng hiện ra cuối */


/* ================= STYLE CƠ BẢN (KHÔNG THAY ĐỔI) ================= */
.footer-container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-wrap: wrap;
    gap: 40px;
}

.footer-col {
    flex: 1;
    min-width: 250px;
}

.footer-logo {
    height: 50px;
    margin-bottom: 20px;
}

.brand-desc {
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 20px;
}

h4 {
    color: #d1aa68;
    font-size: 16px;
    margin-bottom: 20px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

ul {
    list-style: none;
    padding: 0;
}

ul li {
    margin-bottom: 12px;
}

ul li a {
    text-decoration: none;
    color: #cccccc;
    transition: 0.3s;
}

ul li a:hover {
    color: #d1aa68;
}

.social-icons a {
    color: #ffffff;
    font-size: 20px;
    margin-right: 15px;
    transition: 0.3s;
}

.social-icons a:hover {
    color: #d1aa68;
}

.footer-col p {
    font-size: 14px;
    margin-bottom: 10px;
}

.footer-bottom {
    text-align: center;
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #332d27;
    font-size: 12px;
    color: #666;
}
</style>