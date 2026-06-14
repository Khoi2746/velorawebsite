<template>
  <div class="home-wrapper">
    <Header />

    <main class="main-content">
      <section class="hero-section">
        <div class="hero-text">
          <h1>Khám Phá Kỷ Nguyên Thời Gian Mới</h1>
          <p>
            Trải nghiệm sự hoàn mỹ từ các kiệt tác Classic O1, Heritage Moonphase, Noir Starlight và Da Galuchat.
          </p>
          <button class="btn-explore">KHÁM PHÁ NGAY</button>
        </div>
      </section>
    </main>
  </div>
  <Footer />
</template>

<script setup>
// Import component Header từ thư mục components
// Nhớ đảm bảo đường dẫn '../components/Header.vue' là chuẩn với cấu trúc thư mục của em nha
import Header from '../Header.vue'
import Footer from '../Footer.vue'
</script>

<style scoped>
/* ================= TỔNG THỂ TRANG CHỦ ================= */
.home-wrapper {
  width: 100%;
  min-height: 100vh;
  background-color: #0d0d0d; /* Nền đen luxury tổng thể */
  color: #ffffff;
}

/* ================= BANNER CHÀO MỪNG ================= */
.hero-section {
  /* Cho banner chiếm hết chiều cao màn hình trừ đi 80px của thanh Header */
  height: calc(100vh - 80px); 
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  /* Hiệu ứng gradient nền tối sang trọng */
  background: radial-gradient(circle at center, #1a1714 0%, #0d0d0d 100%);
  padding: 0 20px;
}

.hero-text {
  max-width: 800px;
}

.hero-text h1 {
  font-family: 'Times New Roman', serif;
  font-size: 52px;
  color: #d1aa68; /* Màu vàng Gold thương hiệu */
  letter-spacing: 2px;
  margin-bottom: 20px;
}

.hero-text p {
  font-family: 'Arial', sans-serif;
  font-size: 16px;
  color: #cccccc;
  margin: 0 auto 40px auto;
  max-width: 600px;
  line-height: 1.6;
}

/* ================= NÚT KHÁM PHÁ ================= */
.btn-explore {
  padding: 15px 40px;
  background-color: #d1aa68;
  color: #111111;
  border: none;
  font-size: 14px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-explore:hover {
  background-color: #b8955b; /* Đổi màu vàng sậm hơn khi hover */
  transform: translateY(-3px); /* Hiệu ứng nổi bồng bềnh lên một xíu */
  box-shadow: 0 10px 20px rgba(209, 170, 104, 0.2);
}
</style>