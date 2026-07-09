<template>
  <div class="cart-page">
    <Header />

    <main class="cart-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">GIỎ HÀNG CỦA BẠN</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <div v-if="cartItems.length > 0" class="cart-layout">
          <div class="cart-items-section">
            <div class="cart-header">
              <div class="col-product">SẢN PHẨM</div>
              <div class="col-price">GIÁ</div>
              <div class="col-qty">SỐ LƯỢNG</div>
              <div class="col-total">TỔNG CON</div>
            </div>

            <div class="cart-item" v-for="(item, index) in cartItems" :key="index">
              <div class="col-product item-info">
                <button class="btn-remove" @click="removeItem(index)">
                  <i class="fas fa-times"></i>
                </button>
                <router-link :to="`/san-pham/${item.maSanPham}`" class="item-img-link">
                  <img :src="item.anhDaiDien && item.anhDaiDien.startsWith('http') ? item.anhDaiDien : '/img/' + item.anhDaiDien" :alt="item.tenSanPham" class="item-img" />
                </router-link>
                <div class="item-details">
                  <router-link :to="`/san-pham/${item.maSanPham}`" class="item-name">
                    {{ item.tenSanPham }}
                  </router-link>
                  <p class="item-brand">{{ item.thuongHieu ? item.thuongHieu.tenThuongHieu : 'Velora' }}</p>
                </div>
              </div>

              <div class="col-price item-price">{{ formatPrice(item.giaBan) }}</div>
              
              <div class="col-qty item-qty">
                <div class="qty-control">
                  <button @click="decreaseQty(index)">-</button>
                  <input type="text" readonly :value="item.soLuong" />
                  <button @click="increaseQty(index)">+</button>
                </div>
              </div>

              <div class="col-total item-total">{{ formatPrice(item.giaBan * item.soLuong) }}</div>
            </div>
          </div>

          <div class="cart-summary-section">
            <div class="summary-box">
              <h2 class="summary-title">TỔNG ĐƠN HÀNG</h2>
              
              <div class="summary-row">
                <span>Tạm tính</span>
                <span>{{ formatPrice(subTotal) }}</span>
              </div>
              <div class="summary-row">
                <span>Giao hàng</span>
                <span>Miễn phí (VIP)</span>
              </div>
              
              <div class="summary-divider"></div>
              
              <div class="summary-row total-row">
                <span>TỔNG CỘNG</span>
                <span class="total-price">{{ formatPrice(subTotal) }}</span>
              </div>

              <button class="btn-checkout" @click="proceedToCheckout">
                TIẾN HÀNH THANH TOÁN
              </button>
              
              <router-link to="/dong-ho-co-san" class="continue-shopping">
                <i class="fas fa-arrow-left"></i> Tiếp tục mua sắm
              </router-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-cart">
          <i class="fas fa-shopping-bag empty-icon"></i>
          <p>Giỏ hàng của quý khách đang trống.</p>
          <router-link to="/dong-ho-co-san" class="btn-continue">
            KHÁM PHÁ BỘ SƯU TẬP
          </router-link>
        </div>

      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()
const cartItems = ref([])

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

// Load giỏ hàng từ localStorage
const loadCart = () => {
  const key = getCartKey() // Fix: Lấy đúng key
  const storedCart = localStorage.getItem(key)
  if (storedCart) {
    cartItems.value = JSON.parse(storedCart)
  } else {
    cartItems.value = [] // Fix: Đảm bảo giỏ hàng trống nếu chuyển tài khoản
  }
}

// Lưu lại giỏ hàng và kích hoạt event để Header update cái chấm vàng
const saveCart = () => {
  const key = getCartKey() // Fix: Lưu đúng key
  localStorage.setItem(key, JSON.stringify(cartItems.value))
  // Trigger event để các component khác (như Header) biết giỏ hàng thay đổi
  window.dispatchEvent(new Event('cart-updated')) 
}

const formatPrice = (value) => {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// Tính tổng tiền
const subTotal = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.giaBan * item.soLuong), 0)
})

const increaseQty = (index) => {
  if (cartItems.value[index].soLuong < 5) { 
    cartItems.value[index].soLuong++
    saveCart()
  }
}

const decreaseQty = (index) => {
  if (cartItems.value[index].soLuong > 1) {
    cartItems.value[index].soLuong--
    saveCart()
  }
}

const removeItem = (index) => {
  if(confirm('Bạn có chắc chắn muốn xóa siêu phẩm này khỏi giỏ hàng?')) {
    cartItems.value.splice(index, 1)
    saveCart()
  }
}

const proceedToCheckout = () => {
  const user = localStorage.getItem('user')
  if (!user) {
    alert('Vui lòng đăng nhập để tiến hành thanh toán cho kiệt tác này!')
    router.push('/dang-nhap')
    return
  }
  // Chuyển sang trang Thanh Toán (Tạo sau)
  router.push('/thanh-toan')
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
}

.cart-content {
  flex: 1;
  padding: 60px 20px 100px;
}

.container {
  max-width: 1300px;
  margin: 0 auto;
}

.title-wrapper {
  text-align: center;
  margin-bottom: 50px;
}

.page-title {
  color: #111111;
  font-size: 26px;
  font-weight: 400;
  letter-spacing: 4px;
  margin-bottom: 15px;
}

.title-divider {
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-divider::before,
.title-divider::after {
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
  margin: 0 15px;
}

/* ================= LAYOUT 2 CỘT ================= */
.cart-layout {
  display: flex;
  gap: 50px;
  align-items: flex-start;
}

.cart-items-section {
  flex: 2;
}

.cart-summary-section {
  flex: 1;
  position: sticky;
  top: 100px;
}

/* ================= DANH SÁCH SẢN PHẨM ================= */
.cart-header {
  display: flex;
  padding-bottom: 15px;
  border-bottom: 1px solid #eeeeee;
  font-size: 11px;
  font-weight: 600;
  color: #888888;
  letter-spacing: 1.5px;
}

.col-product { flex: 3; }
.col-price { flex: 1; text-align: center; }
.col-qty { flex: 1; text-align: center; }
.col-total { flex: 1; text-align: right; }

.cart-item {
  display: flex;
  align-items: center;
  padding: 30px 0;
  border-bottom: 1px solid #f5f5f5;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-remove {
  background: none;
  border: none;
  color: #cccccc;
  font-size: 16px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.btn-remove:hover {
  color: #ff4444;
}

.item-img {
  width: 90px;
  height: 90px;
  object-fit: contain;
  background-color: #fcfcfc;
  border: 1px solid #f0f0f0;
  padding: 10px;
}

.item-name {
  font-size: 14px;
  color: #111111;
  text-decoration: none;
  font-weight: 500;
  display: block;
  margin-bottom: 5px;
  transition: color 0.3s ease;
}

.item-name:hover {
  color: #d1aa68;
}

.item-brand {
  font-size: 11px;
  color: #888888;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.item-price, .item-total {
  font-size: 14px;
  color: #111111;
  font-weight: 500;
}

.item-total {
  color: #d1aa68;
}

.qty-control {
  display: inline-flex;
  border: 1px solid #e0e0e0;
  height: 36px;
}

.qty-control button {
  background: none;
  border: none;
  width: 30px;
  cursor: pointer;
  font-size: 14px;
  color: #555555;
  transition: all 0.2s;
}

.qty-control button:hover {
  background-color: #f5f5f5;
  color: #d1aa68;
}

.qty-control input {
  width: 40px;
  border: none;
  border-left: 1px solid #e0e0e0;
  border-right: 1px solid #e0e0e0;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #111111;
  outline: none;
}

/* ================= TỔNG KẾT ĐƠN HÀNG ================= */
.summary-box {
  background-color: #fcfcfc;
  border: 1px solid #eeeeee;
  padding: 35px 30px;
}

.summary-title {
  font-size: 14px;
  font-weight: 600;
  color: #111111;
  letter-spacing: 2px;
  margin-bottom: 25px;
  border-bottom: 1px solid #eeeeee;
  padding-bottom: 15px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #555555;
  margin-bottom: 15px;
}

.summary-divider {
  height: 1px;
  background-color: #eeeeee;
  margin: 20px 0;
}

.total-row {
  font-size: 15px;
  color: #111111;
  font-weight: 600;
  margin-bottom: 30px;
}

.total-price {
  color: #d1aa68;
  font-size: 18px;
}

.btn-checkout {
  width: 100%;
  background-color: #24201D;
  color: #ffffff;
  border: none;
  height: 48px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.btn-checkout:hover {
  background-color: #d1aa68;
  box-shadow: 0 5px 15px rgba(209, 170, 104, 0.3);
}

.continue-shopping {
  display: block;
  text-align: center;
  color: #888888;
  font-size: 12px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.continue-shopping:hover {
  color: #d1aa68;
}

/* ================= GIỎ HÀNG TRỐNG ================= */
.empty-cart {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 60px;
  color: #eeeeee;
  margin-bottom: 20px;
}

.empty-cart p {
  font-size: 16px;
  color: #555555;
  margin-bottom: 30px;
}

.btn-continue {
  display: inline-block;
  background-color: transparent;
  color: #24201D;
  border: 1px solid #24201D;
  padding: 12px 30px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-continue:hover {
  background-color: #24201D;
  color: #ffffff;
}

/* ================= RESPONSIVE ================= */
@media (max-width: 992px) {
  .cart-layout {
    flex-direction: column;
  }
  .cart-summary-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .cart-header {
    display: none; /* Ẩn header trên mobile */
  }
  .cart-item {
    flex-wrap: wrap;
    position: relative;
    padding-top: 40px;
  }
  .col-product { flex: 100%; margin-bottom: 15px; }
  .col-price { flex: 50%; text-align: left; }
  .col-qty { flex: 50%; text-align: right; }
  .col-total { flex: 100%; text-align: right; margin-top: 15px; font-weight: bold; }
  .btn-remove { position: absolute; top: 10px; right: 0; }
}
</style>