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
@import "../CSS/User/GioHang.css";
</style>