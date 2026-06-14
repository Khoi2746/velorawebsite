<template>
  <div class="brand-contact-info">
    <div class="contact-grid">
      <div class="contact-item">
        <div class="contact-icon">
          <i class="fas fa-phone-alt"></i>
        </div>
        <div class="contact-text text-highlight">08228 77777</div>
      </div>

      <div class="contact-item">
        <div class="contact-icon">
          <i class="fas fa-map-marker-alt"></i>
        </div>
        <div class="contact-text">01 Nguyễn Gia Thiều, Quận Hoàn Kiếm Hà Nội, Việt Nam</div>
      </div>

      <div class="contact-item">
        <div class="contact-icon">
          <i class="fas fa-map-marker-alt"></i>
        </div>
        <div class="contact-text">81 Phan Kế Bính, phường Đa Kao, quận 1, Hồ Chi Minh</div>
      </div>

      <div class="contact-item">
        <div class="contact-icon">
          <i class="fas fa-envelope"></i>
        </div>
        <div class="contact-text">info@velora.com.vn</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Ku em bê nguyên phần CSS .brand-contact-info từ file ThuongHieu.vue sang đây */
.brand-contact-info {
  width: 100%;
  border-top: 1px solid #e8dec9;
  padding-top: 50px;
  margin-top: 40px;
  /* Thêm các dòng này để reset: */
  box-sizing: border-box; /* Đảm bảo padding không làm phình element */
  position: relative;
  z-index: 1;
}

/* Đảm bảo grid luôn cân bằng bất chấp cha nó là gì */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  margin: 0 auto; /* Tự căn giữa */
  max-width: 1200px; /* Giới hạn độ rộng tối đa */
}

.contact-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 0 10px;
  position: relative;
}

.contact-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -15px;
  top: 20%;
  height: 60%;
  width: 1px;
  background-color: #e0e0e0;
}

.contact-icon {
  font-size: 32px;
  color: #d1aa68;
  margin-bottom: 15px;
  height: 45px;
}

.contact-text {
  font-family: 'Arial', sans-serif;
  font-size: 14px;
  color: #333333;
  line-height: 1.6;
  font-weight: 500;
}

.text-highlight {
  font-size: 16px;
  font-weight: bold;
  color: #111111;
}

@media (max-width: 1200px) {
  .contact-grid { grid-template-columns: repeat(2, 1fr); gap: 40px; }
  .contact-item:not(:last-child)::after { display: none; }
}

@media (max-width: 600px) {
  .contact-grid { grid-template-columns: 1fr; }
}
</style>