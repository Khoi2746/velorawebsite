<template>
  <div class="home-wrapper">
    <Header />

    <main class="main-content">
      <!-- 1. HERO SECTION (Banner chính tràn viền có Slider) -->
      <section class="hero-section">
        <!-- Vòng lặp các ảnh Background -->
        <div 
          v-for="(img, index) in heroImages" 
          :key="index"
          class="hero-slide"
          :class="{ active: index === currentSlide }"
          :style="{ backgroundImage: `url(${img})` }"
        ></div>

        <div class="hero-overlay"></div>
        <div class="container hero-container">
          <div class="hero-text">
            <span class="hero-subtitle">BỘ SƯU TẬP 2026</span>
            <h1>Tuyệt Tác Thời Gian <br /> <span class="gold-text">Độc Bản</span></h1>
            <p>
              Khám phá kỷ nguyên cơ học mới cùng Velora. Nơi kỹ thuật chế tác thủ công Thụy Sĩ hòa quyện cùng nghệ thuật
              thiết kế đương đại qua các dòng Classic O1, Heritage Moonphase và Noir Starlight.
            </p>
            <div class="hero-actions">
              <router-link to="/dong-ho-co-san" class="btn-primary btn-hero">KHÁM PHÁ NGAY</router-link>
              <router-link to="/thuong-hieu" class="btn-outline btn-hero">CÂU CHUYỆN THƯƠNG HIỆU</router-link>
            </div>
            
            <!-- Chấm tròn/thanh ngang chuyển slide -->
            <div class="slider-dots">
              <span 
                v-for="(img, index) in heroImages" 
                :key="'dot-'+index"
                class="dot"
                :class="{ active: index === currentSlide }"
                @click="goToSlide(index)"
              ></span>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. VALUES SECTION (Giá trị cốt lõi) -->
      <section class="values-section">
        <div class="container">
          <div class="values-grid">
            <div class="value-item">
              <span class="value-icon"><i class="fa-solid fa-award"></i></span>
              <h3>Bảo Hành 5 Năm</h3>
              <p>Chế độ bảo hành toàn cầu, miễn phí lau dầu và bảo dưỡng định kỳ.</p>
            </div>
            <div class="value-item">
              <span class="value-icon"><i class="fa-regular fa-gem"></i></span>
              <h3>Tuyệt Tác Chính Hãng</h3>
              <p>Cam kết 100% sản phẩm nguyên bản, đầy đủ hộp sổ thẻ (Fullbox).</p>
            </div>
            <div class="value-item">
              <span class="value-icon"><i class="fa-solid fa-truck-fast"></i></span>
              <h3>Vận Chuyển Hỏa Tốc</h3>
              <p>Giao hàng chuyên dụng miễn phí đính kèm bảo hiểm rủi ro tuyệt đối.</p>
            </div>
            <div class="value-item">
              <span class="value-icon"><i class="fa-solid fa-screwdriver-wrench"></i></span>
              <h3>Chuyên Gia Chăm Sóc</h3>
              <p>Đội ngũ kỹ thuật viên được đào tạo trực tiếp từ các hãng đồng hồ danh tiếng.</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. BỘ SƯU TẬP ĐẶC BIỆT -->
      <section class="collections-section">
        <div class="container">
          <div class="section-title-wrapper">
            <span class="section-subtitle">Dành cho quý ngài & quý cô</span>
            <h2 class="section-title">BỘ SƯU TẬP NỔI BẬT</h2>
            <div class="title-divider"><span class="diamond"></span></div>
          </div>

          <div class="collections-grid">
            <div class="collection-card card-large">
              <img src="https://images.unsplash.com/photo-1612817288484-6f916006741a?q=80&w=1000" alt="Mechanical Watch"
                class="collection-bg" />
              <div class="collection-overlay"></div>
              <div class="collection-content">
                <span>Nghệ Thuật Cơ Khí (Haute Horlogerie)</span>
                <h3>CỖ MÁY THỜI GIAN PHỨC TẠP</h3>
                <router-link to="/dong-ho-co-san" class="btn-link">Khám phá Grand Complications <i
                    class="fas fa-arrow-right"></i></router-link>
              </div>
            </div>
            <div class="collection-card card-small">
              <img src="https://images.unsplash.com/photo-1501163268664-3fdf329d019f?q=80&w=1000" alt="Diamond Watch"
                class="collection-bg" />
              <div class="collection-overlay"></div>
              <div class="collection-content">
                <span>Sự Kiêu Sa Rạng Rỡ</span>
                <h3>KIỆT TÁC ĐÍNH ĐÁ</h3>
                <router-link to="/dong-ho-co-san" class="btn-link">Khám phá Lady Edition <i
                    class="fas fa-arrow-right"></i></router-link>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. KIỆT TÁC ĐÁNG CHÚ Ý -->
      <section class="highlight-section">
        <div class="container">
          <div class="section-title-wrapper">
            <span class="section-subtitle">Tuyển chọn từ Velora</span>
            <h2 class="section-title">KIỆT TÁC MỚI NHẤT</h2>
            <div class="title-divider"><span class="diamond"></span></div>
          </div>

          <div class="showcase-grid">
            <div class="showcase-card">
              <div class="card-tag">NEW</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Hublot Classic Fusion"
                  @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1547996160-81dfa63595aa?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <p class="brand-text">HUBLOT</p>
                <h4>Classic Fusion Titanium</h4>
                <p class="card-price">245.000.000 ₫</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>

            <div class="showcase-card">
              <div class="card-tag gold-tag">LIMITED</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Patek Philippe Grand"
                  @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <p class="brand-text">PATEK PHILIPPE</p>
                <h4>Grand Complications Gold</h4>
                <p class="card-price">Giá chờ hàng</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>

            <div class="showcase-card">
              <div class="card-tag">EXCLUSIVE</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Velora Noir"
                  @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <p class="brand-text">VELORA</p>
                <h4>Velora Noir Starlight</h4>
                <p class="card-price">18.500.000 ₫</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>
          </div>

          <div class="text-center" style="margin-top: 40px;">
            <router-link to="/dong-ho-co-san" class="btn-outline-dark">XEM TOÀN BỘ SẢN PHẨM</router-link>
          </div>
        </div>
      </section>

      <!-- 5. CÂU CHUYỆN THƯƠNG HIỆU -->
      <section class="brand-story-section">
        <div class="container">
          <div class="story-grid">
            <div class="story-image">
              <!-- 🔥 ĐÃ THAY ẢNH CƠ KHÍ NÉT CĂNG VÀ CHUẨN XÁC, BỎ CÁI LOGO FALLBACK GÂY LỖI DỊ DẠNG -->
              <img 
                src="https://images.unsplash.com/photo-1548690312-e3b507d8c110?auto=format&fit=crop&w=1000&q=80" 
                alt="Nghệ nhân đồng hồ chế tác" 
              />
            </div>
            <div class="story-text">
              <h2 class="section-title" style="text-align: left;">DI SẢN <br /><span class="gold-text">VƯỢT THỜI GIAN</span></h2>
              <div class="title-divider" style="margin: 20px 0;"><span class="diamond"></span></div>
              <p>Tại Velora Boutique, chúng tôi không chỉ bán những chiếc đồng hồ. Chúng tôi trao gửi những di sản cơ
                học, những tuyệt tác nghệ thuật được chế tác thủ công bởi các nghệ nhân hàng đầu thế giới.</p>
              <p>Mỗi tiếng tíc tắc đều mang trong mình một câu chuyện về sự kiên nhẫn, đam mê và khao khát vươn tới sự
                hoàn mỹ tuyệt đối.</p>
              <!-- 🔥 ĐÃ THAY CLASS SANG btn-link-dark ĐỂ CHỮ MÀU ĐEN HIỆN RÕ TRÊN NỀN TRẮNG -->
              <router-link to="/thuong-hieu" class="btn-link-dark" style="margin-top: 15px; display: inline-block;">
                Khám phá câu chuyện của chúng tôi <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </section>

    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

// 🔥 LOGIC HIỆU ỨNG SLIDER CHO HERO BANNER
const heroImages = [
  'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?q=80&w=2000', // Movement cơ học
  'https://images.unsplash.com/photo-1547996160-81dfa63595aa?q=80&w=2000', // Đồng hồ trên tay
  'https://images.unsplash.com/photo-1509048191080-d2984bad6ae5?q=80&w=2000', // Cổ điển
  'https://images.unsplash.com/photo-1622434641406-a158123450f9?q=80&w=2000'  // Luxury Lifestyle
]

const currentSlide = ref(0)
let slideInterval = null

// Tự động chuyển ảnh mỗi 5 giây
const startSlider = () => {
  slideInterval = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % heroImages.length
  }, 5000)
}

// Cho phép người dùng tự bấm để chuyển cảnh
const goToSlide = (index) => {
  currentSlide.value = index
  clearInterval(slideInterval)
  startSlider() // Reset lại bộ đếm sau khi bấm
}

onMounted(() => {
  startSlider()
})

onUnmounted(() => {
  if (slideInterval) clearInterval(slideInterval)
})
</script>

<style scoped>
@import "../CSS/User/TrangChu.css";

/* ====================================================
   🔥 CSS LUXURY BANNER & KEN BURNS EFFECT
==================================================== */
:root {
  --gold: #c5a880;
  --dark: #1a1a1a;
}

.gold-text {
  color: #c5a880;
}

.text-center {
  text-align: center;
}

/* --- HERO SECTION --- */
.hero-section {
  position: relative;
  min-height: 85vh;
  display: flex;
  align-items: center;
  justify-content: center; /* Căn giữa container theo chiều ngang */
  overflow: hidden; 
  background-color: #000;
}

/* Các Slide Ảnh tĩnh chìm ở dưới */
.hero-slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1.5s ease-in-out, transform 7s linear;
  transform: scale(1);
  z-index: 0;
}

.hero-slide.active {
  opacity: 1;
  transform: scale(1.08); 
  z-index: 1;
}

/* Lớp phủ Đen Mờ */
.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(26, 26, 26, 0.9) 0%, rgba(26, 26, 26, 0.4) 50%, rgba(26, 26, 26, 0.7) 100%);
  z-index: 2; 
}

/* Content Nằm trên cùng */
.hero-container {
  position: relative;
  z-index: 3;
  width: 100%;
  display: flex;
  justify-content: center; /* Đảm bảo content nằm chính giữa trang */
}

/* 🔥 Tinh chỉnh căn giữa toàn bộ Text và Nút */
.hero-text {
  max-width: 800px; 
  color: #fff;
  text-align: center; 
  display: flex;
  flex-direction: column;
  align-items: center; 
}

.hero-subtitle {
  display: block;
  font-size: 14px;
  letter-spacing: 4px;
  color: #c5a880;
  margin-bottom: 15px;
}

.hero-text h1 {
  font-size: 52px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 25px;
  text-transform: uppercase;
  font-family: 'Playfair Display', serif;
}

.hero-text p {
  font-size: 16px;
  line-height: 1.8;
  color: #e0e0e0;
  margin-bottom: 40px;
  max-width: 700px; 
}

.hero-actions {
  display: flex;
  justify-content: center; 
  gap: 20px;
  margin-bottom: 40px;
}

/* 🔥 Căn giữa các thanh trượt */
.slider-dots {
  display: flex;
  justify-content: center; 
  gap: 12px;
}

.dot {
  width: 40px;
  height: 3px;
  background-color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  background-color: #c5a880;
  width: 60px;
}

.btn-hero {
  padding: 15px 35px;
  font-size: 13px;
  letter-spacing: 2px;
  font-weight: 600;
  text-transform: uppercase;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #c5a880;
  color: #1a1a1a;
  border: 1px solid #c5a880;
}

.btn-primary:hover {
  background-color: #b09570;
}

.btn-outline {
  background-color: transparent;
  color: #fff;
  border: 1px solid #c5a880;
}

.btn-outline:hover {
  background-color: #c5a880;
  color: #1a1a1a;
}

/* 🔥 CLASS NÚT MỚI: Dành cho nút nền trắng ở dưới cùng */
.btn-outline-dark {
  display: inline-block;
  padding: 15px 35px;
  font-size: 13px;
  letter-spacing: 2px;
  font-weight: 600;
  text-transform: uppercase;
  text-decoration: none;
  background-color: transparent;
  color: #1a1a1a; 
  border: 1px solid #c5a880; 
  transition: all 0.3s ease;
}

.btn-outline-dark:hover {
  background-color: #c5a880;
  color: #ffffff; 
}

/* --- SECTION TIÊU ĐỀ --- */
.section-subtitle {
  display: block;
  font-size: 12px;
  letter-spacing: 3px;
  color: #c5a880;
  text-align: center;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.section-title {
  font-size: 32px;
  font-family: 'Playfair Display', serif;
  color: #1a1a1a;
}

/* --- VALUES SECTION --- */
.values-section {
  background-color: #fcfbf9;
  padding: 80px 0;
  border-bottom: 1px solid #eaeaea;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
}

.value-item {
  text-align: center;
  padding: 20px;
}

.value-icon {
  font-size: 36px;
  color: #c5a880;
  margin-bottom: 20px;
  display: block;
}

.value-item h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #1a1a1a;
}

.value-item p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* --- COLLECTIONS SECTION --- */
.collections-section {
  padding: 90px 0;
}

.collections-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
  height: 500px;
}

.collection-card {
  position: relative;
  overflow: hidden;
  border-radius: 4px;
}

.collection-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s ease;
}

.collection-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.2));
}

.collection-content {
  position: absolute;
  bottom: 40px;
  left: 40px;
  color: #fff;
  z-index: 2;
}

.collection-content span {
  font-size: 12px;
  letter-spacing: 2px;
  color: #c5a880;
  text-transform: uppercase;
}

.collection-content h3 {
  font-size: 32px;
  font-family: 'Playfair Display', serif;
  margin: 10px 0 20px 0;
}

.collection-card:hover .collection-bg {
  transform: scale(1.08);
}

.btn-link {
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid #c5a880;
  padding-bottom: 5px;
  transition: color 0.3s;
}

.btn-link:hover {
  color: #c5a880;
}

/* 🔥 LINK MÀU ĐEN MỚI CHO NỀN TRẮNG */
.btn-link-dark {
  color: #1a1a1a;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid #c5a880;
  padding-bottom: 5px;
  transition: color 0.3s;
}

.btn-link-dark:hover {
  color: #c5a880;
}

/* --- SHOWCASE SECTION --- */
.highlight-section {
  padding: 90px 0;
  background-color: #faf9f6;
}

.showcase-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.showcase-card {
  background: #fff;
  border: 1px solid #eaeaea;
  transition: transform 0.3s, box-shadow 0.3s;
  position: relative;
}

.showcase-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.05);
}

.card-img-holder {
  padding: 40px;
  text-align: center;
  background: #fff;
}

.card-img-holder img {
  max-width: 100%;
  height: 250px;
  object-fit: contain;
}

.card-meta {
  padding: 25px;
  text-align: center;
  border-top: 1px solid #eaeaea;
}

.brand-text {
  font-size: 11px;
  letter-spacing: 2px;
  color: #c5a880;
  margin-bottom: 8px;
}

.card-meta h4 {
  font-size: 18px;
  color: #1a1a1a;
  margin-bottom: 15px;
}

.card-price {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 25px;
}

.btn-view-more {
  display: block;
  width: 100%;
  padding: 12px;
  background-color: #1a1a1a; 
  border: 1px solid #1a1a1a;
  color: #ffffff !important; 
  text-decoration: none;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: 0.3s;
}

.btn-view-more:hover {
  background-color: #c5a880; 
  border-color: #c5a880;
  color: #ffffff !important;
}

.card-tag {
  position: absolute;
  top: 20px;
  left: 20px;
  background: #1a1a1a;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 5px 10px;
  letter-spacing: 1px;
}

.gold-tag {
  background: #c5a880;
  color: #1a1a1a;
}

/* --- BRAND STORY --- */
.brand-story-section {
  padding: 100px 0;
}

.story-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.story-image img {
  width: 100%;
  height: 500px;
  object-fit: cover;
  border-radius: 4px;
}

.story-text p {
  font-size: 15px;
  color: #555;
  line-height: 1.8;
  margin-bottom: 20px;
}

/* Responsive */
@media (max-width: 992px) {
  .values-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .collections-grid,
  .story-grid {
    grid-template-columns: 1fr;
    height: auto;
  }

  .collection-card {
    height: 400px;
  }

  .showcase-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-text h1 {
    font-size: 36px;
  }

  .showcase-grid {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>