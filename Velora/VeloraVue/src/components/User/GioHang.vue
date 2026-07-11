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
              <div class="col-total">TỔNG TIỀN</div>
            </div>

            <div class="cart-item" v-for="(item, index) in cartItems" :key="item.maGioHang || index">
              <div class="col-product item-info">
                <button class="btn-remove" @click="removeItem(index)">
                  <i class="fas fa-times"></i>
                </button>
                <router-link :to="`/san-pham/${item.maSanPham}`" class="item-img-link">
                  <img
                    :src="item.anhDaiDien && item.anhDaiDien.startsWith('http') ? item.anhDaiDien : '/img/' + item.anhDaiDien"
                    :alt="item.tenSanPham" class="item-img" />
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

          <!-- BẢNG TỔNG KẾT BÊN PHẢI -->
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

              <!-- PHẦN NHẬP VOUCHER MỚI -->
              <div class="voucher-section">
                <div class="voucher-input-group">
                  <input type="text" v-model="voucherCode" placeholder="Nhập mã giảm giá..."
                    @input="clearVoucherMessages" />
                  <button @click="applyVoucher" :disabled="isCheckingVoucher">
                    {{ isCheckingVoucher ? 'ĐANG XÉT...' : 'ÁP DỤNG' }}
                  </button>
                </div>
                <p v-if="voucherError" class="voucher-msg error"><i class="fas fa-exclamation-circle"></i> {{
                  voucherError }}</p>
                <p v-if="voucherSuccess" class="voucher-msg success"><i class="fas fa-check-circle"></i> {{
                  voucherSuccess }}</p>
              </div>

              <!-- HIỂN THỊ SỐ TIỀN ĐƯỢC GIẢM -->
              <div class="summary-row discount-row" v-if="discountAmount > 0">
                <span>Giảm giá ({{ appliedVoucher.phanTramGiam }}%)</span>
                <span>- {{ formatPrice(discountAmount) }}</span>
              </div>

              <div class="summary-divider"></div>

              <div class="summary-row total-row">
                <span>TỔNG CỘNG</span>
                <!-- Tính toán lại TỔNG CỘNG sau khi trừ Voucher -->
                <span class="total-price">{{ formatPrice(finalTotal) }}</span>
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

// Biến quản lý Voucher
const voucherCode = ref('')
const appliedVoucher = ref(null)
const voucherError = ref('')
const voucherSuccess = ref('')
const isCheckingVoucher = ref(false)

const formatPrice = (value) => {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// 1. Tính tổng tiền gốc
const subTotal = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.giaBan * item.soLuong), 0)
})

// 2. Tính số tiền được giảm
const discountAmount = computed(() => {
  if (!appliedVoucher.value) return 0
  return subTotal.value * (appliedVoucher.value.phanTramGiam / 100)
})

// 3. Tính tiền khách phải trả cuối cùng
const finalTotal = computed(() => {
  return subTotal.value - discountAmount.value
})

// Xóa thông báo khi người dùng gõ phím khác
const clearVoucherMessages = () => {
  voucherError.value = ''
  voucherSuccess.value = ''
}

// ================= GỌI API KIỂM TRA VOUCHER =================
const applyVoucher = async () => {
  if (!voucherCode.value.trim()) {
    voucherError.value = 'Vui lòng nhập mã giảm giá!'
    return
  }

  isCheckingVoucher.value = true
  clearVoucherMessages()

  try {
    const res = await fetch(`http://localhost:8080/api/admin/ma-giam-gia/kiem-tra?code=${voucherCode.value}`)

    if (res.ok) {
      const data = await res.json()
      appliedVoucher.value = data // Lưu lại thông tin voucher
      voucherSuccess.value = `Áp dụng mã thành công! Bạn được giảm ${data.phanTramGiam}%`
    } else {
      // Backend từ chối (hết hạn, hết lượt, sai mã)
      const errorMsg = await res.text()
      voucherError.value = errorMsg
      appliedVoucher.value = null // Xóa trạng thái giảm giá
    }
  } catch (error) {
    voucherError.value = 'Lỗi kết nối đến máy chủ!'
    appliedVoucher.value = null
  } finally {
    isCheckingVoucher.value = false
  }
}

// ================= TẢI GIỎ HÀNG TỪ DATABASE =================
const loadCart = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)

  try {
    const res = await fetch(`http://localhost:8080/api/gio-hang/${user.maNguoiDung}`)
    if (res.ok) {
      cartItems.value = await res.json()
      window.dispatchEvent(new Event('cart-updated'))
    }
  } catch (error) {
    console.error('Lỗi lấy giỏ hàng:', error)
  }
}

// ================= CẬP NHẬT SỐ LƯỢNG (+ / -) =================
const updateQuantity = async (index, newQuantity) => {
  if (newQuantity < 1 || newQuantity > 5) return;
  const item = cartItems.value[index];

  try {
    const res = await fetch(`http://localhost:8080/api/gio-hang/${item.maGioHang}/so-luong?soLuong=${newQuantity}`, {
      method: 'PATCH'
    })

    if (res.ok) {
      cartItems.value[index].soLuong = newQuantity
      window.dispatchEvent(new Event('cart-updated'))
    }
  } catch (error) {
    console.error('Lỗi cập nhật số lượng:', error)
  }
}

const increaseQty = (index) => {
  updateQuantity(index, cartItems.value[index].soLuong + 1);
}

const decreaseQty = (index) => {
  updateQuantity(index, cartItems.value[index].soLuong - 1);
}

// ================= XÓA KHỎI DB =================
const removeItem = async (index) => {
  if (!confirm('Bạn có chắc chắn muốn xóa siêu phẩm này khỏi giỏ hàng?')) return;
  const item = cartItems.value[index];

  try {
    const res = await fetch(`http://localhost:8080/api/gio-hang/${item.maGioHang}`, {
      method: 'DELETE'
    })

    if (res.ok) {
      cartItems.value.splice(index, 1)
      window.dispatchEvent(new Event('cart-updated'))
      // Nếu xóa hết giỏ hàng thì gỡ voucher luôn
      if (cartItems.value.length === 0) {
        appliedVoucher.value = null
        voucherCode.value = ''
      }
    }
  } catch (error) {
    console.error('Lỗi xóa sản phẩm:', error)
  }
}

// ================= THANH TOÁN =================
const proceedToCheckout = () => {
  const user = localStorage.getItem('user')
  if (!user) {
    alert('Vui lòng đăng nhập để tiến hành thanh toán!')
    router.push('/dang-nhap')
    return
  }

  // Mẹo: Ku em có thể lưu mã voucher vào localStorage để trang Thanh Toán biết mà gọi
  if (appliedVoucher.value) {
    localStorage.setItem('activeVoucher', JSON.stringify(appliedVoucher.value))
  } else {
    localStorage.removeItem('activeVoucher')
  }

  router.push('/thanh-toan')
}

onMounted(() => {
  const user = localStorage.getItem('user')
  if (!user) {
    alert('Vui lòng đăng nhập để xem giỏ hàng!')
    router.push('/dang-nhap')
    return
  }
  loadCart()
})
</script>

<style scoped>
/* =========== CSS GIỮ NGUYÊN CŨ CỦA EM (Chỉ thêm phần Voucher) =========== */
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

.cart-header {
  display: flex;
  padding-bottom: 15px;
  border-bottom: 1px solid #eeeeee;
  font-size: 11px;
  font-weight: 600;
  color: #888888;
  letter-spacing: 1.5px;
}

.col-product {
  flex: 3;
}

.col-price {
  flex: 1;
  text-align: center;
}

.col-qty {
  flex: 1;
  text-align: center;
}

.col-total {
  flex: 1;
  text-align: right;
}

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

.item-price,
.item-total {
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

/* CSS MỚI CHO VOUCHER */
.voucher-section {
  margin-top: 20px;
  margin-bottom: 20px;
}

.voucher-input-group {
  display: flex;
  border: 1px solid #e0e0e0;
  height: 40px;
  overflow: hidden;
}

.voucher-input-group input {
  flex: 1;
  border: none;
  padding: 0 15px;
  font-size: 13px;
  outline: none;
  text-transform: uppercase;
}

.voucher-input-group button {
  background-color: #f5f5f5;
  border: none;
  border-left: 1px solid #e0e0e0;
  padding: 0 15px;
  font-size: 11px;
  font-weight: 600;
  color: #24201D;
  cursor: pointer;
  transition: all 0.3s ease;
}

.voucher-input-group button:hover {
  background-color: #d1aa68;
  color: #fff;
}

.voucher-input-group button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.voucher-msg {
  font-size: 12px;
  margin-top: 8px;
  font-weight: 500;
}

.voucher-msg.error {
  color: #ff4444;
}

.voucher-msg.success {
  color: #2b7a0b;
}

.discount-row {
  color: #2b7a0b;
  font-weight: 500;
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
    display: none;
  }

  .cart-item {
    flex-wrap: wrap;
    position: relative;
    padding-top: 40px;
  }

  .col-product {
    flex: 100%;
    margin-bottom: 15px;
  }

  .col-price {
    flex: 50%;
    text-align: left;
  }

  .col-qty {
    flex: 50%;
    text-align: right;
  }

  .col-total {
    flex: 100%;
    text-align: right;
    margin-top: 15px;
    font-weight: bold;
  }

  .btn-remove {
    position: absolute;
    top: 10px;
    right: 0;
  }
}
</style>