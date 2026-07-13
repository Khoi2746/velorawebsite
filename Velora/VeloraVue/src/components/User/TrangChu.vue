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
          <router-link to="/dong-ho-co-san" class="btn-explore">KHÁM PHÁ NGAY</router-link>
        </div>
      </section>

      <section class="values-section">
        <div class="container">
          <div class="values-grid">
            <div class="value-item">
              <span class="value-icon">✦</span>
              <h3>Tuyệt Tác Chính Hãng</h3>
              <p>Cam kết 100% sản phẩm chế tác cơ khí chính xác nguyên bản Thụy Sỹ.</p>
            </div>
            <div class="value-item">
              <span class="value-icon">🛡️</span>
              <h3>Bảo Hộ Độc Bản</h3>
              <p>Chế độ bảo hành toàn cầu lên đến 5 năm độc quyền tại hệ thống Velora.</p>
            </div>
            <div class="value-item">
              <span class="value-icon">✈️</span>
              <h3>Vận Chuyển Đế Vương</h3>
              <p>Giao hàng hỏa tốc miễn phí đính kèm bảo hiểm rủi ro tuyệt đối.</p>
            </div>
          </div>
        </div>
      </section>

      <section class="collections-section">
        <div class="container">
          <div class="section-title-wrapper">
            <h2 class="section-title">BỘ SƯU TẬP ĐẶC BIỆT</h2>
            <div class="title-divider"><span class="diamond"></span></div>
          </div>
          
          <div class="collections-grid">
            <div class="collection-card card-large">
              <div class="collection-overlay"></div>
              <div class="collection-content">
                <span>Dành Cho Quý Ông</span>
                <h3>MEN'S MECHANICAL</h3>
                <router-link to="/dong-ho-co-san" class="btn-link">Khám phá <i class="fas fa-arrow-right"></i></router-link>
              </div>
            </div>
            <div class="collection-card card-small">
              <div class="collection-overlay"></div>
              <div class="collection-content">
                <span>Tuyệt Tác Kim Cương</span>
                <h3>LADY EDITION</h3>
                <router-link to="/dong-ho-co-san" class="btn-link">Khám phá <i class="fas fa-arrow-right"></i></router-link>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="highlight-section">
        <div class="container">
          <div class="section-title-wrapper">
            <h2 class="section-title">KIỆT TÁC ĐÁNG CHÚ Ý</h2>
            <div class="title-divider"><span class="diamond"></span></div>
          </div>

          <div class="showcase-grid">
            <div class="showcase-card">
              <div class="card-tag">NEW</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Hublot Classic Fusion" @error="(e) => e.target.src='https://images.unsplash.com/photo-1547996160-81dfa63595aa?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <h4>Classic Fusion Titanium</h4>
                <p class="card-price">245.000.000 ₫</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>

            <div class="showcase-card">
              <div class="card-tag">PRESTIGE</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Patek Philippe Grand" @error="(e) => e.target.src='https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <h4>Grand Complications Gold</h4>
                <p class="card-price">Giá chờ hàng</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>

            <div class="showcase-card">
              <div class="card-tag">LIMITED</div>
              <div class="card-img-holder">
                <img src="/img/VeloraIcon.png" alt="Velora Noir" @error="(e) => e.target.src='https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?q=80&w=600'" />
              </div>
              <div class="card-meta">
                <h4>Velora Noir Starlight</h4>
                <p class="card-price">189.000.000 ₫</p>
                <router-link to="/dong-ho-co-san" class="btn-view-more">CHI TIẾT KIỆT TÁC</router-link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import Header from '../Header.vue'
import Footer from '../Footer.vue'
</script>

<style scoped>
@import "../CSS/User/TrangChu.css";
</style>