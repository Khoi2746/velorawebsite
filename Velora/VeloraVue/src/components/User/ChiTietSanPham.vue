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
              {{ product.giaBan > 100000000 ? 'Liên hệ để biết thêm chi tiết' : formatPrice(product.giaBan) }}
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
              <button v-if="product.giaBan && product.giaBan <= 100000000" class="btn-primary" @click="addToCart">
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

// ================= HÀM LẤY KHÓA GIỎ HÀNG (MỚI) =================
const getCartKey = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            return user.maNguoiDung ? `cart_${user.maNguoiDung}` : 'cart_guest';
        } catch (e) {
            return 'cart_guest';
        }
    }
    return 'cart_guest';
}

// LOGIC THÊM VÀO GIỎ HÀNG
// Thay thế hoàn toàn hàm addToCart cũ bằng hàm này
const addToCart = async () => {
  // 1. Kiểm tra đăng nhập (Bắt buộc vì bảng GioHang cần MaNguoiDung để lưu DB)
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    alert('Vui lòng đăng nhập để thêm kiệt tác này vào giỏ hàng!');
    router.push('/dang-nhap');
    return;
  }
  
  const user = JSON.parse(userStr);

  // 2. Gom dữ liệu để gửi xuống Backend Java
  const payload = {
    maNguoiDung: user.maNguoiDung,
    maSanPham: product.value.maSanPham,
    soLuong: quantity.value // Lấy từ biến số lượng khách chọn trên giao diện
  };

  try {
    // 3. Gọi API thật đẩy dữ liệu vào SQL Server
    const response = await fetch('http://localhost:8080/api/gio-hang/them', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (response.ok) {
      // 4. Bắn sự kiện để Header nhảy số lượng chấm vàng ngay lập tức
      window.dispatchEvent(new Event('cart-updated'));
      
      // 5. Báo thành công bằng alert (Không dùng biến showAddedModal để tránh crash web)
      alert('Tuyệt vời! Đã thêm ' + product.value.tenSanPham + ' vào giỏ hàng thành công!');
    } else {
      // Bắt lỗi nếu Backend từ chối
      alert('Có lỗi xảy ra khi thêm vào giỏ. Vui lòng thử lại!');
    }
  } catch (error) {
    // In ra lỗi thật trong F12 để dễ soi nếu rớt mạng
    console.error('Lỗi gọi API:', error); 
    alert('Không thể kết nối đến máy chủ. Hãy chắc chắn Server Java đang chạy!');
  }
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
@import "../CSS/User/ChiTietSanPham.css";
</style>