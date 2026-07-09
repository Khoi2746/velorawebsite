<template>
  <div class="product-detail-page">
    <Header />

    <main class="detail-content" v-if="!loading && product">
      <div class="section-header">
        <h2>CHI TIẾT SẢN PHẨM</h2>
        <div class="header-divider"><span class="diamond"></span></div>
      </div>
      <div class="container">
        <nav class="breadcrumb">
          <router-link to="/">TRANG CHỦ</router-link>
          <span class="separator">/</span>
          <router-link to="/dong-ho-co-san">SẢN PHẨM</router-link>

          <template v-if="route.query.fromId && route.query.fromName">
            <span class="separator">/</span>
            <router-link :to="`/san-pham/${route.query.fromId}`" class="breadcrumb-link">
              {{ route.query.fromName.toUpperCase() }}
            </router-link>
          </template>

          <template v-else-if="product.thuongHieu">
            <span class="separator">/</span>
            <span class="breadcrumb-link">{{ product.thuongHieu.tenThuongHieu.toUpperCase() }}</span>
          </template>

          <span class="separator">/</span>
          <span class="current">{{ product.tenSanPham }}</span>
        </nav>

        <div class="detail-grid">
          <div class="image-section">
            <div class="main-image-wrapper">
              <div class="tag-new" v-if="product.loaiSanPham">
                {{ product.loaiSanPham.tenLoai }}
              </div>
              <img
                :src="product.anhDaiDien && product.anhDaiDien.startsWith('http') ? product.anhDaiDien : '/img/' + product.anhDaiDien"
                :alt="product.tenSanPham" class="main-image" />
            </div>
          </div>

          <div class="info-section">
            <div class="brand-name" v-if="product.thuongHieu">{{ product.thuongHieu.tenThuongHieu }}</div>
            <h1 class="product-title">{{ product.tenSanPham }}</h1>

            <div class="product-price">
              {{ product.giaBan > 400000000 ? 'Giá chờ hàng' : formatPrice(product.giaBan) }}
            </div>

            <div class="title-divider"></div>

            <p class="short-description" v-if="product.moTaChiTiet">
              {{ product.moTaChiTiet }}
            </p>
            <p class="short-description" v-else>
              Một tuyệt tác cơ học mang đậm dấu ấn nghệ thuật chế tác đồng hồ cao cấp. Sự kết hợp hoàn hảo giữa vật liệu
              thượng
              hạng và độ chính xác tuyệt đối.
            </p>

            <ul class="specs-list">
              <li>
                <span class="spec-label">Mã sản phẩm:</span>
                <span class="spec-value">VEL-{{ product.maSanPham }}</span>
              </li>
              <li v-if="product.loaiSanPham">
                <span class="spec-label">Bộ sưu tập:</span>
                <span class="spec-value">{{ product.loaiSanPham.tenLoai }}</span>
              </li>
              <li>
                <span class="spec-label">Trạng thái:</span>
                <span class="spec-value status-in-stock"
                  v-if="product.trangThai === 'CON_HANG' || product.trangThai === 1">Còn
                  hàng tại Boutique</span>
                <span class="spec-value status-out-stock" v-else>Liên hệ đặt trước</span>
              </li>
            </ul>

            <div class="action-buttons">
              <button v-if="product.giaBan && product.giaBan <= 400000000" class="btn-primary" @click="addToCart">
                THÊM VÀO GIỎ HÀNG
              </button>

              <button class="btn-secondary" @click="contactVVIP">
                LIÊN HỆ TƯ VẤN VVIP
              </button>
            </div>

            <div class="accordion-group">
              <div class="accordion-item">
                <div class="accordion-header">
                  CHÍNH SÁCH BẢO HÀNH <span class="icon">+</span>
                </div>
                <div class="accordion-content">
                  Bảo hành toàn cầu 5 năm chính hãng. Miễn phí bảo dưỡng lau dầu định kỳ trong 3 năm đầu tiên tại các
                  trung tâm
                  dịch vụ của Velora.
                </div>
              </div>
              <div class="accordion-item">
                <div class="accordion-header">
                  VẬN CHUYỂN & THANH TOÁN <span class="icon">+</span>
                </div>
                <div class="accordion-content">
                  Giao hàng bằng xe chuyên dụng hoặc chuyên viên mang đến tận nhà. Thanh toán linh hoạt, hỗ trợ bảo mật
                  thông
                  tin tuyệt đối.
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="related-products" v-if="relatedProducts.length > 0">
          <div class="section-header">
            <h2>KHÁM PHÁ THÊM SẢN PHẨM</h2>
            <div class="header-divider"><span class="diamond"></span></div>
          </div>

          <div class="carousel-wrapper">
            <button class="carousel-btn prev-btn" @click="scrollCarousel(-1)">
              <i class="fas fa-chevron-left"></i>
            </button>

            <div class="carousel-track" ref="carouselRef">
              <div class="product-card" v-for="relProduct in relatedProducts" :key="relProduct.maSanPham">
                <div class="tag-new" v-if="relProduct.loaiSanPham">
                  {{ relProduct.loaiSanPham.tenLoai }}
                </div>
                <router-link
                  :to="{ path: `/san-pham/${relProduct.maSanPham}`, query: { fromId: product.maSanPham, fromName: product.tenSanPham } }"
                  class="product-image-wrapper">
                  <img
                    :src="relProduct.anhDaiDien && relProduct.anhDaiDien.startsWith('http') ? relProduct.anhDaiDien : '/img/' + relProduct.anhDaiDien"
                    :alt="relProduct.tenSanPham" class="product-image" />
                </router-link>

                <div class="product-info">
                  <router-link
                    :to="{ path: `/san-pham/${relProduct.maSanPham}`, query: { fromId: product.maSanPham, fromName: product.tenSanPham } }"
                    class="product-name-link">
                    <h3 class="product-name">{{ relProduct.tenSanPham }}</h3>
                  </router-link>
                  <div class="product-price">
                    {{ relProduct.giaBan > 400000000 ? 'Giá chờ hàng' : formatPrice(relProduct.giaBan) }}
                  </div>
                </div>

                <div class="product-action">
                  <router-link
                    :to="{ path: `/san-pham/${relProduct.maSanPham}`, query: { fromId: product.maSanPham, fromName: product.tenSanPham } }"
                    class="btn-contact">
                    XEM CHI TIẾT
                  </router-link>
                </div>
              </div>
            </div>

            <button class="carousel-btn next-btn" @click="scrollCarousel(1)">
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </main>

    <main class="detail-content loading-state" v-else-if="loading">
      <div class="loader"></div>
      <p>Đang tải thông tin tuyệt tác...</p>
    </main>

    <main class="detail-content error-state" v-else>
      <div class="container">
        <h2>KHÔNG TÌM THẤY SẢN PHẨM</h2>
        <p>Sản phẩm này có thể đã được gỡ hoặc không tồn tại.</p>
        <router-link to="/san-pham" class="btn-primary">QUAY LẠI CỬA HÀNG</router-link>
      </div>
    </main>

    <Info />
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import Info from '../info.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(true)
const relatedProducts = ref([])
const carouselRef = ref(null)

// Biến lưu số lượng
const quantity = ref(1)

const formatPrice = (value) => {
  if (!value) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// LOGIC THÊM VÀO GIỎ HÀNG
const addToCart = () => {
  let cart = JSON.parse(localStorage.getItem('cart') || '[]')

  const existingItemIndex = cart.findIndex(item => item.maSanPham === product.value.maSanPham)

  if (existingItemIndex > -1) {
    cart[existingItemIndex].soLuong += quantity.value
  } else {
    cart.push({
      maSanPham: product.value.maSanPham,
      tenSanPham: product.value.tenSanPham,
      giaBan: product.value.giaBan,
      anhDaiDien: product.value.anhDaiDien,
      thuongHieu: product.value.thuongHieu,
      soLuong: quantity.value
    })
  }

  localStorage.setItem('cart', JSON.stringify(cart))
  window.dispatchEvent(new Event('cart-updated')) // Cập nhật Header
  alert('Đã thêm ' + product.value.tenSanPham + ' vào giỏ hàng!')
}

// LOGIC LIÊN HỆ TƯ VẤN
const contactVVIP = () => {
  // Chuyển trang và đính kèm tên sản phẩm lên URL
  router.push({
    path: '/tu-van',
    query: {
      productName: product.value.tenSanPham,
      productId: product.value.maSanPham
    }
  })
}

// Hàm cuộn ngang carousel
const scrollCarousel = (direction) => {
  if (carouselRef.value) {
    carouselRef.value.scrollBy({ left: direction * 350, behavior: 'smooth' })
  }
}

const loadProductDetail = async () => {
  loading.value = true
  const productId = route.params.id

  try {
    const res = await fetch(`http://localhost:8080/api/san-pham/${productId}`)
    if (res.ok) {
      product.value = await res.json()
    }

    const resAll = await fetch(`http://localhost:8080/api/san-pham`)
    if (resAll.ok) {
      const allProducts = await resAll.json()
      relatedProducts.value = allProducts.filter(p => p.maSanPham !== parseInt(productId))
    }
  } catch (error) {
    console.error('Lỗi tải dữ liệu:', error)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, () => {
  loadProductDetail()
  window.scrollTo(0, 0)
})

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
/* =========== CSS CHI TIẾT SẢN PHẨM GỐC =========== */
.product-detail-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fdfdfd;
}

.detail-content {
  flex: 1;
  padding: 40px 20px 100px;
}

.container {
  max-width: 1300px;
  margin: 0 auto;
}

.breadcrumb {
  font-size: 11px;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #888888;
  margin-bottom: 50px;
  display: flex;
  align-items: center;
}

.breadcrumb a {
  color: #888888;
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb a:hover {
  color: #d1aa68;
}

.breadcrumb .separator {
  margin: 0 10px;
  color: #cccccc;
}

.breadcrumb .current {
  color: #24201D;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: flex-start;
}

.image-section {
  position: sticky;
  top: 100px;
}

.main-image-wrapper {
  background-color: #ffffff;
  border: 1px solid #f2e0c9;
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  min-height: 600px;
}

.tag-new {
  position: absolute;
  top: 20px;
  left: 20px;
  background-color: #24201D;
  color: #d1aa68;
  font-size: 10px;
  font-weight: 600;
  padding: 5px 15px;
  letter-spacing: 2px;
  text-transform: uppercase;
  border: 1px solid #d1aa68;
  z-index: 2;
}

.main-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.info-section {
  padding-top: 10px;
}

.brand-name {
  color: #d1aa68;
  font-size: 13px;
  letter-spacing: 3px;
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 15px;
}

.product-title {
  color: #24201D;
  font-size: 38px;
  font-family: 'Times New Roman', serif;
  font-weight: 400;
  letter-spacing: 2px;
  line-height: 1.3;
  margin-bottom: 25px;
}

.product-price {
  font-size: 24px;
  color: #24201D;
  font-weight: 500;
  letter-spacing: 1.5px;
  margin-bottom: 35px;
}

.title-divider {
  width: 100%;
  height: 1px;
  background-color: #eeeeee;
  margin-bottom: 35px;
}

.short-description {
  color: #555555;
  font-size: 15px;
  line-height: 1.8;
  margin-bottom: 40px;
}

.specs-list {
  list-style: none;
  padding: 0;
  margin: 0 0 45px 0;
}

.specs-list li {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}

.spec-label {
  color: #888888;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 12px;
}

.spec-value {
  color: #24201D;
  font-weight: 500;
}

.status-in-stock {
  color: #2b7a0b;
}

.status-out-stock {
  color: #d1aa68;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 60px;
}

.btn-primary,
.btn-secondary {
  width: 100%;
  padding: 18px 0;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 0;
  text-align: center;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background-color: #24201D;
  color: #ffffff;
  border: 1px solid #24201D;
}

.btn-primary:hover {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #24201D;
}

.btn-secondary {
  background-color: transparent;
  color: #24201D;
  border: 1px solid #cccccc;
}

.btn-secondary:hover {
  border-color: #24201D;
  background-color: #f9f9f9;
}

.accordion-group {
  border-top: 1px solid #eeeeee;
}

.accordion-item {
  border-bottom: 1px solid #eeeeee;
}

.accordion-header {
  padding: 20px 0;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #24201D;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.accordion-header .icon {
  font-size: 18px;
  font-weight: 300;
  color: #d1aa68;
}

.accordion-content {
  padding: 0 0 20px 0;
  color: #666666;
  font-size: 14px;
  line-height: 1.6;
}

/* =========== CSS PHẦN SẢN PHẨM KHÁC (CAROUSEL) =========== */
.related-products {
  margin-top: 100px;
  padding-top: 60px;
  border-top: 1px solid #eeeeee;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-header h2 {
  color: #24201D;
  font-size: 24px;
  font-family: 'Times New Roman', serif;
  font-weight: 400;
  letter-spacing: 4px;
  margin-bottom: 15px;
}

.header-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.header-divider::before,
.header-divider::after {
  content: '';
  display: block;
  width: 60px;
  height: 1px;
  background-color: #d1aa68;
}

.diamond {
  width: 6px;
  height: 6px;
  background-color: #d1aa68;
  transform: rotate(45deg);
}

.carousel-wrapper {
  position: relative;
  padding: 0 40px;
}

.carousel-track {
  display: flex;
  gap: 30px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding: 20px 10px;
}

/* Ẩn thanh cuộn ngang để nhìn cho sang */
.carousel-track::-webkit-scrollbar {
  display: none;
}

.carousel-track {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: #ffffff;
  border: 1px solid #eeeeee;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #24201D;
  font-size: 16px;
  transition: all 0.3s ease;
  z-index: 10;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.carousel-btn:hover {
  background-color: #d1aa68;
  color: #ffffff;
  border-color: #d1aa68;
}

.prev-btn {
  left: -10px;
}

.next-btn {
  right: -10px;
}

/* Product Card Style (Tái sử dụng từ trang Shop) */
.product-card {
  min-width: calc(33.333% - 20px);
  max-width: calc(33.333% - 20px);
  background-color: #ffffff;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 30px 20px;
  border: 1px solid #f2e0c9;
  border-radius: 4px;
  transition: all 0.4s ease;
  flex-shrink: 0;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(36, 32, 29, 0.06);
  border-color: #d1aa68;
}

.product-image-wrapper {
  width: 100%;
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
  overflow: hidden;
  display: block;
}

.product-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  transition: transform 0.6s ease;
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.product-info {
  margin-bottom: 20px;
  width: 100%;
}

.product-name-link {
  text-decoration: none;
  display: block;
}

.product-name {
  font-size: 14px;
  color: #24201D;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
  transition: color 0.3s ease;
}

.product-name-link:hover .product-name {
  color: #d1aa68;
}

.product-price {
  font-size: 14px;
  color: #d1aa68;
  font-weight: 600;
}

.product-action {
  width: 100%;
}

.btn-contact {
  background-color: #24201D;
  color: #ffffff;
  border: 1px solid #24201D;
  font-size: 11px;
  font-weight: 600;
  padding: 12px 0;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: block;
  width: 100%;
  text-align: center;
}

.btn-contact:hover {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #24201D;
}

/* Loading & Error States */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.loader {
  border: 2px solid #f3f3f3;
  border-top: 2px solid #d1aa68;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.error-state h2 {
  font-family: 'Times New Roman', serif;
  color: #24201D;
  letter-spacing: 2px;
  margin-bottom: 15px;
}

.error-state p {
  color: #888888;
  margin-bottom: 30px;
}

/* Responsive */
@media (max-width: 992px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .image-section {
    position: relative;
    top: 0;
  }

  .product-card {
    min-width: calc(50% - 15px);
    max-width: calc(50% - 15px);
  }
}

@media (max-width: 600px) {
  .product-card {
    min-width: 100%;
    max-width: 100%;
  }
}
</style>