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

              <!-- NÚT BẤM CHUYỂN ẢNH TRÁI / PHẢI TRÊN ẢNH CHÍNH -->
              <button class="slider-arrow prev" @click="prevImage" v-if="allImages.length > 1">
                <i class="fas fa-chevron-left"></i>
              </button>

              <img
                :src="currentImage && currentImage.startsWith('http') ? currentImage : '/img/' + currentImage"
                :alt="product.tenSanPham" 
                class="main-image" 
              />

              <button class="slider-arrow next" @click="nextImage" v-if="allImages.length > 1">
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>

            <!-- DANH SÁCH THUMBNAIL (ẢNH NHỎ CÁC GÓC ĐỘ) -->
            <div class="thumbnail-list" v-if="allImages.length > 1">
              <div 
                v-for="(img, idx) in allImages" 
                :key="idx" 
                class="thumbnail-item"
                :class="{ 'active': currentImage === img }"
                @click="currentImage = img"
              >
                <img :src="img && img.startsWith('http') ? img : '/img/' + img" alt="Góc chụp chi tiết" />
              </div>
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
              Một tuyệt tác cơ học mang đậm dấu ấn nghệ thuật chế tác đồng hồ cao cấp. Sự kết hợp hoàn hảo giữa vật liệu thượng hạng và độ chính xác tuyệt đối.
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
                  v-if="product.trangThai === 'CON_HANG' || product.trangThai === 1">Còn hàng tại Boutique</span>
                <span class="spec-value status-out-stock" v-else>Liên hệ đặt trước</span>
              </li>
            </ul>

            <div class="action-buttons-group" style="display: flex; flex-direction: column; gap: 15px; margin-top: 25px;">
              <div class="primary-actions-row" style="display: flex; gap: 15px; width: 100%;">
                <div style="flex: 1;">
                  <!-- Component Thanh Toán -->
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
                  Bảo hành toàn cầu 5 năm chính hãng. Miễn phí bảo dưỡng lau dầu định kỳ trong 3 năm đầu tiên tại các trung tâm dịch vụ của Velora.
                </div>
              </div>
              <div class="accordion-item">
                <div class="accordion-header">
                  VẬN CHUYỂN & THANH TOÁN <span class="icon">+</span>
                </div>
                <div class="accordion-content">
                  Giao hàng bằng xe chuyên dụng hoặc chuyên viên mang đến tận nhà. Thanh toán linh hoạt, hỗ trợ bảo mật thông tin tuyệt đối.
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

// IMPORT HÀM DÙNG CHUNG BẬT POPUP
import { showAlert } from '@/composables/useAlert';

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(true)
const relatedProducts = ref([])
const carouselRef = ref(null)

const quantity = ref(1)

// ================= PHẦN QUẢN LÝ SLIDER NHIỀU ẢNH =================
const allImages = ref([])
const currentImage = ref('')

const nextImage = () => {
  if (allImages.value.length === 0) return
  const currentIndex = allImages.value.indexOf(currentImage.value)
  const nextIndex = (currentIndex + 1) % allImages.value.length
  currentImage.value = allImages.value[nextIndex]
}

const prevImage = () => {
  if (allImages.value.length === 0) return
  const currentIndex = allImages.value.indexOf(currentImage.value)
  const prevIndex = (currentIndex - 1 + allImages.value.length) % allImages.value.length
  currentImage.value = allImages.value[prevIndex]
}
// ===============================================================

const formatPrice = (value) => {
  if (!value) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const addToCart = async () => {
  const userStr = localStorage.getItem('user');
  
  if (!userStr) {
    showAlert('Vui lòng đăng nhập để thêm kiệt tác này vào giỏ hàng!', 'warning');
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
      showAlert(`Tuyệt vời! Đã thêm ${product.value.tenSanPham} vào giỏ hàng thành công!`, 'success');
    } else {
      showAlert('Có lỗi xảy ra khi thêm vào giỏ. Vui lòng thử lại!', 'error');
    }
  } catch (error) {
    console.error('Lỗi gọi API:', error);
    showAlert('Không thể kết nối đến máy chủ. Hãy chắc chắn Server Java đang chạy!', 'error');
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

      // GOM ẢNH ĐẠI DIỆN VÀ THƯ VIỆN ẢNH PHỤ VÀO MẢNG SLIDER
      allImages.value = []
      if (product.value.anhDaiDien) {
        allImages.value.push(product.value.anhDaiDien)
      }

      if (product.value.thuVienAnhs && Array.isArray(product.value.thuVienAnhs)) {
        product.value.thuVienAnhs.forEach(item => {
          if (item.duongDanAnh && !allImages.value.includes(item.duongDanAnh)) {
            allImages.value.push(item.duongDanAnh)
          }
        })
      }

      // Đặt ảnh hiển thị mặc định ban đầu là ảnh đầu tiên
      currentImage.value = allImages.value.length > 0 ? allImages.value[0] : ''
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
.main-image-wrapper {
  position: relative;
  width: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1 / 1;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

/* NÚT MŨI TÊN TRÁI PHẢI */
.slider-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(20, 20, 20, 0.6);
  color: #c5a880;
  border: 1px solid rgba(197, 168, 128, 0.4);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.slider-arrow:hover {
  background: #c5a880;
  color: #141414;
}

.slider-arrow.prev { left: 15px; }
.slider-arrow.next { right: 15px; }

/* DANH SÁCH THUMBNAIL PHÍA DƯỚI */
.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-top: 15px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.thumbnail-item {
  width: 75px;
  height: 75px;
  border-radius: 6px;
  border: 2px solid #eaeaea;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s ease;
  flex-shrink: 0;
  background: #fff;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-item:hover {
  opacity: 1;
  border-color: #c5a880;
}

.thumbnail-item.active {
  opacity: 1;
  border-color: #c5a880;
  box-shadow: 0 0 8px rgba(197, 168, 128, 0.4);
}
</style>