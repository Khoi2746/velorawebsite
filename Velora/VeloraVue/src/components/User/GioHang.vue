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
                  <img :src="item.anhDaiDien && item.anhDaiDien.startsWith('http') ? item.anhDaiDien : '/img/' + item.anhDaiDien" :alt="item.tenSanPham" class="item-img" />
                </router-link>
                <div class="item-details">
                  <router-link :to="`/san-pham/${item.maSanPham}`" class="item-name">
                    {{ item.tenSanPham }}
                  </router-link>
                  <p class="item-brand">{{ item.thuongHieu ? item.thuongHieu.tenThuongHieu : 'VELORA' }}</p>
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

              <!-- PHẦN NHẬP VOUCHER -->
              <div class="voucher-section">
                <div class="voucher-input-group">
                  <input type="text" v-model="voucherCode" placeholder="Nhập mã giảm giá..." @input="clearVoucherMessages" />
                  <button @click="applyVoucher" :disabled="isCheckingVoucher">
                    {{ isCheckingVoucher ? 'ĐANG XÉT...' : 'ÁP DỤNG' }}
                  </button>
                </div>
                <p v-if="voucherError" class="voucher-msg error"><i class="fas fa-exclamation-circle"></i> {{ voucherError }}</p>
                <p v-if="voucherSuccess" class="voucher-msg success"><i class="fas fa-check-circle"></i> {{ voucherSuccess }}</p>
              </div>

              <div class="summary-row discount-row" v-if="discountAmount > 0">
                <span>Giảm giá ({{ appliedVoucher.phanTramGiam }}%)</span>
                <span>- {{ formatPrice(discountAmount) }}</span>
              </div>
              
              <div class="summary-divider"></div>
              
              <div class="summary-row total-row">
                <span>TỔNG CỘNG</span>
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

    <!-- TOAST THÔNG BÁO TỒN KHO TRƯỢT TỪ DƯỚI LÊN (GIỮ NGUYÊN) -->
    <div class="stock-toast" :class="{ 'show': showToast }">
      <i class="fas fa-exclamation-triangle"></i> {{ toastMessage }}
    </div>

    <!-- 🔥 MODAL POPUP XÁC NHẬN VÀ CẢNH BÁO CHUẨN LUXURY (THAY THẾ WINDOW.ALERT) -->
    <div v-if="popup.show" class="custom-popup-overlay" @click.self="handleCancel">
      <div class="custom-popup-box">
        <div class="popup-icon">
          <i :class="popup.type === 'confirm' ? 'fa-solid fa-circle-question' : 'fa-solid fa-circle-exclamation'"></i>
        </div>
        <div class="popup-content">
          <h3>{{ popup.title }}</h3>
          <p>{{ popup.message }}</p>
        </div>
        <div class="popup-actions" v-if="popup.type === 'confirm'">
          <button type="button" class="btn-popup-cancel" @click="handleCancel">HỦY BỎ</button>
          <button type="button" class="btn-popup-confirm" @click="handleConfirm">XÁC NHẬN</button>
        </div>
        <div class="popup-actions" v-else>
          <button type="button" class="btn-popup-confirm" @click="handleConfirm">ĐỒNG Ý</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()
const cartItems = ref([])

// ================= BIẾN QUẢN LÝ VOUCHER =================
const voucherCode = ref('')
const appliedVoucher = ref(null)
const voucherError = ref('')
const voucherSuccess = ref('')
const isCheckingVoucher = ref(false)

const formatPrice = (value) => {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// ================= LOGIC POPUP CUSTOM THAY THẾ ALERT/CONFIRM =================
const popup = ref({
  show: false,
  type: 'alert', // 'alert' hoặc 'confirm'
  title: 'THÔNG BÁO',
  message: '',
  onConfirm: null,
  onCancel: null
})

// Mở popup dạng Cảnh báo (Chỉ có nút Đồng ý)
const openAlert = (message, onConfirmCallback = null) => {
  popup.value = { show: true, type: 'alert', title: 'THÔNG BÁO', message, onConfirm: onConfirmCallback, onCancel: null }
}

// Mở popup dạng Xác nhận (Có nút Hủy và Xác nhận)
const openConfirm = (message, onConfirmCallback) => {
  popup.value = { show: true, type: 'confirm', title: 'XÁC NHẬN', message, onConfirm: onConfirmCallback, onCancel: null }
}

const handleConfirm = () => {
  if (popup.value.onConfirm) popup.value.onConfirm()
  popup.value.show = false
}

const handleCancel = () => {
  if (popup.value.onCancel) popup.value.onCancel()
  popup.value.show = false
}

// ================= TÍNH TOÁN TIỀN BẠC =================
const subTotal = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.giaBan * item.soLuong), 0)
})

const discountAmount = computed(() => {
  if (!appliedVoucher.value) return 0
  return subTotal.value * (appliedVoucher.value.phanTramGiam / 100)
})

const finalTotal = computed(() => {
  return subTotal.value - discountAmount.value
})

const clearVoucherMessages = () => {
  voucherError.value = ''
  voucherSuccess.value = ''
}

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
      appliedVoucher.value = data 
      voucherSuccess.value = `Áp dụng mã thành công! Bạn được giảm ${data.phanTramGiam}%`
    } else {
      const errorMsg = await res.text()
      voucherError.value = errorMsg
      appliedVoucher.value = null 
    }
  } catch (error) {
    voucherError.value = 'Lỗi kết nối đến máy chủ!'
    appliedVoucher.value = null
  } finally {
    isCheckingVoucher.value = false
  }
}

// ================= LOGIC TOAST THÔNG BÁO TỒN KHO =================
const showToast = ref(false)
const toastMessage = ref('')
let toastTimer = null

const triggerToast = (msg) => {
  toastMessage.value = msg
  showToast.value = true
  if (toastTimer) clearTimeout(toastTimer)
  
  toastTimer = setTimeout(() => {
    showToast.value = false
  }, 1500)
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

// ================= CẬP NHẬT SỐ LƯỢNG (+ / -) CÓ CHECK TỒN KHO =================
const updateQuantity = async (index, newQuantity) => {
  const item = cartItems.value[index];
  const maxStock = item.soLuongTonKho !== undefined ? item.soLuongTonKho : 100; 
  
  if (newQuantity < 1) return;

  if (newQuantity > maxStock) {
    triggerToast(`Số lượng sản phẩm đã đạt giới hạn tối đa trong kho!`);
    return;
  }
  
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
const removeItem = (index) => {
  // Thay thế confirm mặc định bằng Popup Custom
  openConfirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?', async () => {
    const item = cartItems.value[index];
    try {
      const res = await fetch(`http://localhost:8080/api/gio-hang/${item.maGioHang}`, {
        method: 'DELETE'
      })
      if (res.ok) {
        cartItems.value.splice(index, 1)
        window.dispatchEvent(new Event('cart-updated'))
        if (cartItems.value.length === 0) {
          appliedVoucher.value = null
          voucherCode.value = ''
        }
      }
    } catch (error) {
      console.error('Lỗi xóa sản phẩm:', error)
    }
  });
}

// ================= THANH TOÁN =================
const proceedToCheckout = () => {
  const user = localStorage.getItem('user')
  
  if (!user) {
    // Thay thế alert đăng nhập bằng Popup, Đợi click ĐỒNG Ý thì chuyển trang
    openAlert('Vui lòng đăng nhập để tiến hành thanh toán!', () => {
      router.push('/dang-nhap')
    });
    return
  }

  if (cartItems.value.length === 0) {
    // Thay thế alert giỏ hàng trống bằng Popup
    openAlert('Giỏ hàng của bạn đang trống!')
    return
  }

  // Lưu voucher vào localStorage để trang checkout đọc được
  if (appliedVoucher.value) {
    localStorage.setItem('activeVoucher', JSON.stringify(appliedVoucher.value))
  } else {
    localStorage.removeItem('activeVoucher')
  }

  // Chuyển hướng sang route /checkout đúng đường dẫn kèm tham số from=cart
  router.push('/checkout?from=cart')
}

onMounted(() => {
  const user = localStorage.getItem('user')
  if (!user) {
    // Thay thế alert khi vào trang chưa đăng nhập bằng Popup, đợi Đồng ý thì redirect
    openAlert('Vui lòng đăng nhập để xem giỏ hàng!', () => {
      router.push('/dang-nhap')
    });
    return
  }
  loadCart()
})
</script>

<style scoped>
@import "../CSS/User/GioHang.css";

/* ==============================================================
   CSS CUSTOM POPUP VELORA LUXURY (THAY THẾ WINDOW.ALERT)
============================================================== */
.custom-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  backdrop-filter: blur(4px);
}

.custom-popup-box {
  background-color: #1a1918;
  border: 1px solid #d1aa68;
  border-radius: 8px;
  padding: 30px 25px;
  max-width: 440px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.85);
  animation: modalPopIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes modalPopIn {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

.popup-icon i {
  font-size: 44px;
  color: #d1aa68;
  margin-bottom: 12px;
}

.popup-content h3 {
  color: #d1aa68;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  margin: 0 0 12px 0;
  text-transform: uppercase;
}

.popup-content p {
  color: #ffffff;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 25px 0;
}

.popup-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-popup-cancel {
  background-color: #2e2b27;
  color: #cccccc;
  border: 1px solid #444444;
  padding: 10px 25px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-popup-cancel:hover {
  background-color: #444444;
  color: #ffffff;
}

.btn-popup-confirm {
  background-color: #d1aa68;
  color: #1a1918;
  border: none;
  padding: 10px 30px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(209, 170, 104, 0.3);
}

.btn-popup-confirm:hover {
  background-color: #e5be7a;
  color: #000000;
}
</style>