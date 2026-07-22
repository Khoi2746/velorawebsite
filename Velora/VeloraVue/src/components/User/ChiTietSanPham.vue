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

            <div class="action-buttons-group"
              style="display: flex; flex-direction: column; gap: 15px; margin-top: 25px;">
              <div class="primary-actions-row" style="display: flex; gap: 15px; width: 100%;">
                <div style="flex: 1;">
                  <ThanhToan :maSanPham="product.maSanPham" :soLuong="quantity" />
                </div>
                <div style="flex: 1;" v-if="product.giaBan && product.giaBan <= 400000000">
                  <button class="btn-primary" @click="addToCart" style="width: 100%; margin: 0; height: 100%;">
                    THÊM VÀO GIỎ HÀNG
                  </button>
                </div>
              </div>

              <div class="secondary-action-row" style="display: flex; gap: 15px; width: 100%;">
                <button @click="contactVVIP" class="btn-secondary"
                  style="flex: 1; margin: 0; display: inline-flex; justify-content: center; align-items: center; cursor: pointer;">
                  LIÊN HỆ TƯ VẤN VVIP
                </button>
              </div>
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

    <!-- HTML KHỐI POPUP THÔNG BÁO -->
    <div class="custom-popup-overlay" v-if="popup.show" @click="closePopup">
      <div class="custom-popup-box" :class="popup.type" @click.stop>
        <div class="popup-icon">
          <i v-if="popup.type === 'success'" class="fas fa-check-circle"></i>
          <i v-else-if="popup.type === 'warning'" class="fas fa-exclamation-triangle"></i>
          <i v-else class="fas fa-times-circle"></i>
        </div>
        <div class="popup-content">
          <h3>{{ popup.type === 'success' ? 'THÀNH CÔNG' : popup.type === 'warning' ? 'CHÚ Ý' : 'LỖI' }}</h3>
          <p>{{ popup.message }}</p>
        </div>
        <button class="popup-close-btn" @click="closePopup">ĐÓNG</button>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'
import Info from '../info.vue'
import ThanhToan from './ThanhToan.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(true)
const relatedProducts = ref([])
const carouselRef = ref(null)

const quantity = ref(1)

// -- STATE QUẢN LÝ POPUP THÔNG BÁO --
const popup = ref({
  show: false,
  message: '',
  type: 'success' // Các loại: 'success', 'error', 'warning'
})

let popupTimeout = null;

const showNotification = (message, type = 'success') => {
  popup.value = { show: true, message, type }
  
  // Xóa timeout cũ nếu có bấm liên tiếp
  if (popupTimeout) clearTimeout(popupTimeout)
  
  // Tự động đóng popup sau 3 giây (3000ms)
  popupTimeout = setTimeout(() => {
    closePopup()
  }, 3000)
}

const closePopup = () => {
  popup.value.show = false
  if (popupTimeout) clearTimeout(popupTimeout)
}
// ----------------------------------

const formatPrice = (value) => {
  if (!value) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const addToCart = async () => {
  const userStr = localStorage.getItem('user');
  
  // Kiểm tra đăng nhập
  if (!userStr) {
    showNotification('Vui lòng đăng nhập để thêm kiệt tác này vào giỏ hàng!', 'warning');
    // Chuyển hướng sau 1.5 giây để người dùng kịp đọc thông báo
    setTimeout(() => {
      router.push('/dang-nhap');
    }, 1500);
    return;
  }

  const user = JSON.parse(userStr);
  const payload = {
    maNguoiDung: user.maNguoiDung,
    maSanPham: product.value.maSanPham,
    soLuong: quantity.value
  };

  try {
    const response = await fetch('http://localhost:8080/api/gio-hang/them', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (response.ok) {
      window.dispatchEvent(new Event('cart-updated'));
      showNotification(`Tuyệt vời! Đã thêm ${product.value.tenSanPham} vào giỏ hàng thành công!`, 'success');
    } else {
      showNotification('Có lỗi xảy ra khi thêm vào giỏ. Vui lòng thử lại!', 'error');
    }
  } catch (error) {
    console.error('Lỗi gọi API:', error);
    showNotification('Không thể kết nối đến máy chủ. Hãy chắc chắn Server Java đang chạy!', 'error');
  }
}

const contactVVIP = () => {
  router.push({
    path: '/lien-he-tu-van', 
    query: {
      productId: product.value.maSanPham
    }
  })
}

const minDate = ref(new Date().toISOString().split('T')[0])

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
@import "../CSS/User/ChiTietSanPham.css";

/* --- CSS CHO KHỐI POPUP --- */
.custom-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7); /* Nền đen mờ */
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.custom-popup-box {
  background: #1e1e1e; /* Màu nền sang trọng của Velora */
  border: 1px solid #d1aa68; /* Viền vàng luxury */
  border-radius: 8px;
  padding: 30px 40px;
  width: 90%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  animation: scaleUp 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.popup-icon i {
  font-size: 55px;
}

/* Các màu trạng thái */
.custom-popup-box.success .popup-icon i { color: #2ecc71; }
.custom-popup-box.warning .popup-icon i { color: #f39c12; }
.custom-popup-box.error .popup-icon i { color: #e74c3c; }

.popup-content h3 {
  color: #d1aa68;
  margin: 0 0 10px 0;
  font-size: 20px;
  letter-spacing: 2px;
  font-weight: bold;
}

.popup-content p {
  color: #e0e0e0;
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
}

.popup-close-btn {
  margin-top: 15px;
  background: #d1aa68;
  color: #111;
  border: none;
  padding: 12px 30px;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 1px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.popup-close-btn:hover {
  background: #b8955b;
  transform: translateY(-2px);
}

/* Animation */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>