<template>
  <div class="refund-page">
    <!-- =========================================================================
         [PHẦN 1: HEADER & TIÊU ĐỀ TRANG]
    ========================================================================== -->
    <Header />

    <main class="refund-content">
      <div class="container">
        
        <!-- 1.1 TIÊU ĐỀ CHÍNH -->
        <div class="title-wrapper">
          <!-- DÒNG MẶC ĐỊNH: Tiêu đề in hoa chuẩn -->
          <h1 class="page-title">YÊU CẦU HOÀN TIỀN & TRẢ HÀNG</h1>
          <!-- THAY THẾ: Đổi tiêu đề sang MÀU VÀNG HOÀNG KIM:
          <h1 class="page-title" style="color: #d1aa68;">YÊU CẦU HOÀN TIỀN & TRẢ HÀNG</h1> -->
          <!-- THAY THẾ: Đổi tiêu đề sang MÀU ĐỎ CẢNH BÁO:
          <h1 class="page-title" style="color: #dc2626;">YÊU CẦU HOÀN TIỀN & TRẢ HÀNG</h1> -->

          <!-- Họa tiết đường kẻ và viên kim cương -->
          <div class="title-divider"><span class="diamond"></span></div>
        </div>

        <!-- =========================================================================
             [PHẦN 2: FORM NHẬP THÔNG TIN HOÀN TIỀN]
        ========================================================================== -->
        <div class="refund-card" v-if="orderData">
          <form @submit.prevent="xuLyGuiYeuCau" class="refund-form">
            
            <!-- --- MỤC 1: THÔNG TIN ĐƠN HÀNG CỐ ĐỊNH --- -->
            <h3 class="form-section-title">1. Thông Tin Đơn Hàng (Không được sửa)</h3>
            <div class="form-grid">
              
              <!-- Mã đơn hàng -->
              <div class="form-group">
                <label>Mã Đơn Hàng</label>
                <input type="text" :value="'#' + orderData.maDonHangCode" disabled />
                <!-- THAY THẾ: Mã đơn hàng in đậm màu vàng:
                <input type="text" :value="'#' + orderData.maDonHangCode" style="color: #d1aa68; font-weight: bold;" disabled /> -->
              </div>

              <!-- Họ và tên khách hàng -->
              <div class="form-group">
                <label>Họ và Tên</label>
                <input type="text" v-model="orderData.tenNguoiNhan" disabled />
              </div>

              <!-- Số điện thoại -->
              <div class="form-group">
                <label>Số Điện Thoại</label>
                <input type="text" v-model="orderData.soDienThoai" disabled />
              </div>

              <!-- Địa chỉ Gmail đặt hàng -->
              <div class="form-group">
                <label>Địa Chỉ Gmail Đặt Hàng</label>
                <input type="email" v-model="orderData.email" disabled />
              </div>
            </div>

            <!-- --- MỤC 2: THÔNG TIN TÀI KHOẢN NGÂN HÀNG NHẬN TIỀN --- -->
            <h3 class="form-section-title">2. Tài Khoản Nhận Tiền Hoàn</h3>
            <div class="form-grid">
              
              <!-- Chọn ngân hàng thụ hưởng -->
              <div class="form-group">
                <label>Chọn Ngân Hàng *</label>
                <select v-model="selectedBank" @change="traCuuTenChuTaiKhoan" required>
                  <option value="">-- Chọn ngân hàng --</option>
                  <option v-for="bank in bankList" :key="bank.code" :value="bank">
                    {{ bank.shortName }} - {{ bank.name }}
                  </option>
                </select>
              </div>

              <!-- Nhập số tài khoản -->
              <div class="form-group">
                <label>Số Tài Khoản *</label>
                <input 
                  type="text" 
                  v-model="accountNumber" 
                  @input="traCuuTenChuTaiKhoan" 
                  placeholder="Nhập số tài khoản..." 
                  required 
                />
              </div>

              <!-- Tên chủ tài khoản (Tự tra cứu tự động qua API VietQR) -->
              <div class="form-group full-width">
                <label>Tên Chủ Tài Khoản *</label>
                <div class="account-name-wrapper">
                  <input 
                    type="text" 
                    v-model="accountName" 
                    placeholder="Nhập tên chủ tài khoản (Viết hoa không dấu)..." 
                    :readonly="isLookingUp" 
                    required 
                  />
                  <!-- Trạng thái tra cứu tên -->
                  <span v-if="isLookingUp" class="lookup-status"><i class="fas fa-spinner fa-spin"></i> Đang tra cứu...</span>
                  <span v-else-if="lookupSuccess" class="lookup-status success"><i class="fas fa-check-circle"></i> Đã xác minh</span>
                </div>
              </div>
            </div>

            <!-- --- MỤC 3: LÝ DO TRẢ HÀNG & MINH CHỨNG ẢNH --- -->
            <h3 class="form-section-title">3. Lý Do Trả Hàng & Minh Chứng (2 - 6 ảnh)</h3>
            
            <!-- Ô nhập lý do chi tiết -->
            <div class="form-group full-width">
              <label>Lý Do Cụ Thể *</label>
              <textarea v-model="refundReason" rows="4" placeholder="Nhập lý do quý khách muốn hoàn tiền trả hàng..." required></textarea>
            </div>

            <!-- Ô tải lên ảnh minh chứng -->
            <div class="form-group full-width">
              <label>Tải Ảnh Minh Chứng Sản Phẩm (Tối thiểu 2 ảnh, tối đa 6 ảnh) *</label>
              <input type="file" multiple accept="image/*" @change="handleFileUpload" class="file-input" />
              
              <!-- Danh sách ảnh xem trước đã chọn -->
              <div class="image-preview-grid" v-if="previewImages.length > 0">
                <div class="img-preview" v-for="(img, idx) in previewImages" :key="idx">
                  <img :src="img" alt="Minh chứng" />
                  <!-- NÚT XÓA TỪNG ẢNH XEM TRƯỚC (DẤU X MÀU ĐỎ) -->
                  <button type="button" class="btn-remove-img" @click="removeImage(idx)">✕</button>
                </div>
              </div>
            </div>

            <!-- Khung cảnh báo quy định hoàn hàng (Vàng cam) -->
            <div class="terms-warning-box">
              <p><i class="fas fa-exclamation-triangle"></i> <strong>LƯU Ý QUAN TRỌNG:</strong> Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc nếu có lỗi từ nhà sản xuất. Nếu quý khách gửi yêu cầu hoàn hàng quá <strong>6 lần trong 1 năm</strong>, tài khoản sẽ tự động bị khóa và liệt vào danh sách đen của Velora.</p>
            </div>

            <!-- NÚT GỬI FORM YÊU CẦU HOÀN TIỀN -->
            <!-- DÒNG MẶC ĐỊNH (Nâu gỗ đậm): -->
            <button type="submit" class="btn-submit-refund" :disabled="isSubmitting">
              {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN GỬI YÊU CẦU' }}
            </button>
            <!-- THAY THẾ: Nút gửi yêu cầu đổi sang MÀU VÀNG HOÀNG KIM:
            <button type="submit" class="btn-submit-refund" style="background: #d1aa68; color: white;" :disabled="isSubmitting">{{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN GỬI YÊU CẦU' }}</button> -->
            <!-- THAY THẾ: Nút gửi yêu cầu bo tròn hình viên thuốc:
            <button type="submit" class="btn-submit-refund" style="border-radius: 50px;" :disabled="isSubmitting">{{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN GỬI YÊU CẦU' }}</button> -->

          </form>
        </div>
      </div>
    </main>

    <!-- =========================================================================
         [PHẦN 3: CÁC CỬA SỔ POPUP & MODAL]
    ========================================================================== -->

    <!-- 3.1 MODAL XÁC NHẬN CẢNH BÁO QUY ĐỊNH & BLACKLIST -->
    <div class="otp-modal-overlay" v-if="showTermsModal">
      <div class="otp-modal-box terms-modal">
        <h3 class="gold-title"><i class="fas fa-shield-alt"></i> QUY ĐỊNH HOÀN HÀNG</h3>
        <div class="terms-content">
          <p>📌 <strong>Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc nếu có lỗi trực tiếp từ Nhà Sản Xuất.</strong></p>
          <p>⚠️ <strong>Lưu ý:</strong> Nếu khách hàng thực hiện hoàn hàng <strong>quá 6 lần trong vòng 1 năm</strong>, hệ thống sẽ tự động <strong>khóa tài khoản</strong> và vĩnh viễn đưa địa chỉ Email vào <strong>Danh sách đen (Blacklist)</strong>.</p>
        </div>
        <div class="otp-actions">
          <!-- Nút Hủy bỏ đóng modal quy định -->
          <button @click="showTermsModal = false" class="btn-cancel-otp">HỦY BỎ</button>
          <!-- Nút Đồng ý và tiếp tục chuyển sang gửi OTP -->
          <button @click="dongYDieuKhoanVaGuiOtp" class="btn-confirm-otp">TÔI ĐỒNG Ý & TIẾP TỤC</button>
        </div>
      </div>
    </div>

    <!-- 3.2 MODAL NHẬP MÃ XÁC THỰC OTP QUA EMAIL -->
    <div class="otp-modal-overlay" v-if="showOtpModal">
      <div class="otp-modal-box">
        <h3>XÁC THỰC MÃ OTP</h3>
        <p>Hệ thống đã gửi mã OTP 6 số đến Gmail đặt hàng: <strong>{{ orderData?.email }}</strong>. Vui lòng nhập để hoàn tất yêu cầu.</p>
        
        <!-- Ô nhập 6 số OTP -->
        <input type="text" v-model="otpCode" placeholder="Nhập 6 chữ số..." maxlength="6" class="otp-input" />
        
        <div class="otp-actions">
          <!-- Nút Hủy bỏ -->
          <button @click="showOtpModal = false" class="btn-cancel-otp">HỦY BỎ</button>
          <!-- Nút Xác nhận gửi hoàn tất -->
          <button @click="xacNhanOtpHoanTien" class="btn-confirm-otp">XÁC NHẬN</button>
        </div>
      </div>
    </div>

    <!-- 3.3 THÔNG BÁO TỰ TẮT Ở GÓC (CUSTOM ALERT TOAST) -->
    <div class="custom-alert-toast" :class="[alertToast.type, { 'show': alertToast.show }]">
      <i class="fa-solid" :class="alertToast.type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'"></i>
      <span>{{ alertToast.message }}</span>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

// =========================================================================
// [LOGIC XỬ LÝ 1: KHỞI TẠO BIẾN DỮ LIỆU]
// =========================================================================
const router = useRouter()
const orderData = ref(null)

const bankList = ref([])
const selectedBank = ref('')
const accountNumber = ref('')
const accountName = ref('')
const refundReason = ref('')

const isLookingUp = ref(false)
const lookupSuccess = ref(false)

const previewImages = ref([])
const isSubmitting = ref(false)

const showTermsModal = ref(false)
const showOtpModal = ref(false)
const otpCode = ref('')

const alertToast = ref({
  show: false,
  message: '',
  type: 'success'
})
let toastTimer = null

// =========================================================================
// [LOGIC XỬ LÝ 2: THÔNG BÁO TOAST & API TRA CỨU NGÂN HÀNG]
// =========================================================================
const showToast = (message, type = 'success') => {
  alertToast.value = { show: true, message, type }
  if (toastTimer) clearTimeout(toastTimer)
  
  // DÒNG MẶC ĐỊNH: Thông báo tự tắt sau 3 giây (3000ms)
  toastTimer = setTimeout(() => { alertToast.value.show = false }, 3000)
  // THAY THẾ: Hiện thông báo lâu hơn trong 5 giây:
  // toastTimer = setTimeout(() => { alertToast.value.show = false }, 5000)
  // THAY THẾ: Tắt thông báo nhanh trong 1 giây:
  // toastTimer = setTimeout(() => { alertToast.value.show = false }, 1000)
}

// Gọi API VietQR lấy danh sách tất cả các ngân hàng Việt Nam
const fetchBanks = async () => {
  try {
    const res = await fetch('https://api.vietqr.io/v2/banks')
    if (res.ok) {
      const data = await res.json()
      bankList.value = data.data || []
    }
  } catch (err) {
    console.error('Lỗi tải danh sách ngân hàng:', err)
  }
}

// Tự động tra cứu tên chủ tài khoản sau 600ms khi người dùng nhập số tài khoản
let lookupTimer = null
const traCuuTenChuTaiKhoan = () => {
  lookupSuccess.value = false
  if (!selectedBank.value || !accountNumber.value || accountNumber.value.trim().length < 6) return

  if (lookupTimer) clearTimeout(lookupTimer)

  lookupTimer = setTimeout(async () => {
    isLookingUp.value = true
    try {
      const res = await fetch('https://api.vietqr.io/v2/lookup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ bin: selectedBank.value.bin, accountNumber: accountNumber.value.trim() })
      })

      if (res.ok) {
        const data = await res.json()
        if (data.code === '00' && data.data && data.data.accountName) {
          accountName.value = data.data.accountName
          lookupSuccess.value = true
        }
      }
    } catch (err) {
      console.warn('Không thể tra cứu tên tự động.')
    } finally {
      isLookingUp.value = false
    }
  }, 600)
}

// =========================================================================
// [LOGIC XỬ LÝ 3: QUẢN LÝ TẢI ẢNH VÀ GỬI DỮ LIỆU]
// =========================================================================
const handleFileUpload = (e) => {
  const files = Array.from(e.target.files)
  if (files.length + previewImages.value.length > 6) {
    showToast('Quý khách chỉ được chọn tối đa 6 ảnh minh chứng!', 'error')
    return
  }

  files.forEach(file => {
    const reader = new FileReader()
    reader.onload = (event) => { previewImages.value.push(event.target.result) }
    reader.readAsDataURL(file)
  })
}

const removeImage = (idx) => { previewImages.value.splice(idx, 1) }

// Bước 1: Kiểm tra điều kiện và mở popup quy định
const xuLyGuiYeuCau = () => {
  if (previewImages.value.length < 2) {
    showToast('Vui lòng tải lên ít nhất 2 ảnh minh chứng sản phẩm!', 'error')
    return
  }

  if (!orderData.value || !orderData.value.email) {
    showToast('Không tìm thấy Gmail đặt hàng của đơn này!', 'error')
    return
  }

  showTermsModal.value = true
}

// Bước 2: Đồng ý quy định và gửi mã OTP về Gmail
const dongYDieuKhoanVaGuiOtp = async () => {
  showTermsModal.value = false
  isSubmitting.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/hoan-tien/gui-otp?email=${encodeURIComponent(orderData.value.email)}`, {
      method: 'POST'
    })

    if (res.ok) {
      showOtpModal.value = true
      showToast('Đã gửi mã OTP đến Gmail của bạn!', 'success')
    } else {
      const errorMsg = await res.text()
      showToast('Không thể gửi mã OTP: ' + errorMsg, 'error')
    }
  } catch (err) {
    showToast('Lỗi kết nối đến máy chủ Backend!', 'error')
  } finally {
    isSubmitting.value = false
  }
}

// Bước 3: Xác thực OTP và lưu yêu cầu hoàn tiền vào cơ sở dữ liệu
const xacNhanOtpHoanTien = async () => {
  if (!otpCode.value || otpCode.value.length !== 6) {
    showToast('Vui lòng nhập đúng 6 chữ số OTP!', 'error')
    return
  }

  const payload = {
    maDonHangCode: orderData.value.maDonHangCode,
    hoTen: orderData.value.tenNguoiNhan,
    soDienThoai: orderData.value.soDienThoai,
    email: orderData.value.email,
    diaChi: orderData.value.diaChi,
    tenNganHang: selectedBank.value.shortName || selectedBank.value,
    soTaiKhoan: accountNumber.value,
    tenChuTaiKhoan: accountName.value.toUpperCase(),
    lyDo: refundReason.value,
    danhSachAnh: previewImages.value,
    otpCode: otpCode.value
  }

  try {
    const res = await fetch('http://localhost:8080/api/hoan-tien/xac-nhan-yeu-cau', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (res.ok) {
      showOtpModal.value = false
      showToast('Yêu cầu hoàn tiền đã được gửi thành công!', 'success')
      setTimeout(() => {
        router.push('/don-hang')
      }, 1500)
    } else {
      showToast(await res.text(), 'error')
    }
  } catch (err) {
    showToast('Có lỗi xảy ra khi xác nhận OTP!', 'error')
  }
}

// Khởi chạy khi load trang: Lấy dữ liệu đơn hàng đã chọn từ localStorage
onMounted(() => {
  const savedOrder = localStorage.getItem('selectedRefundOrder')
  const userStr = localStorage.getItem('user')

  if (!savedOrder) {
    showToast('Không tìm thấy thông tin đơn hàng hoàn tiền!', 'error')
    router.push('/don-hang')
    return
  }

  const parsedOrder = JSON.parse(savedOrder)
  const loggedUser = userStr ? JSON.parse(userStr) : null

  if (!parsedOrder.email && loggedUser) {
    parsedOrder.email = loggedUser.email || ''
  }

  orderData.value = parsedOrder
  fetchBanks()
})
</script>

<style scoped>
/* =========================================================================
   [CSS NHÓM 1: BỐ CỤC KHUNG CHÍNH CỦA TRANG HOÀN TIỀN]
========================================================================= */
.refund-page { 
  background: #f4f1ea;                         /* Màu nền toàn trang: KEM NHẠT */
  min-height: 100vh;                            /* Chiều cao tối thiểu chiếm trọn màn hình */
  font-family: sans-serif; 
}

.refund-content { 
  padding: 40px 0 80px 0;                       /* Khoảng cách đệm: trên 40px, dưới 80px */
}

.container { 
  max-width: 900px;                             /* Chiều rộng tối đa khung giữa 900px */
  margin: 0 auto;                               /* Căn giữa màn hình */
  padding: 0 15px;                              /* Đệm trái phải 15px */
}

.title-wrapper { 
  text-align: center;                           /* Căn toàn bộ tiêu đề ra CHÍNH GIỮA */
  margin-bottom: 30px; 
}

.title-divider { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  margin-top: 8px; 
}

.title-divider::before, 
.title-divider::after { 
  content: ""; 
  width: 50px;                                  /* Chiều dài đường kẻ ngang: 50px */
  height: 1px;                                  /* Độ dày đường kẻ ngang: 1px */
  background: #d1aa68;                          /* Màu đường kẻ: VÀNG HOÀNG KIM */
}

.diamond { 
  width: 6px; 
  height: 6px; 
  background: #d1aa68;                          /* Màu viên kim cương giữa đường kẻ: VÀNG */
  transform: rotate(45deg);                     /* Xoay 45 độ tạo hình thoi */
  margin: 0 10px; 
}

/* =========================================================================
   [CSS NHÓM 2: KHUNG FORM VÀ CÁC Ô NHẬP LIỆU (INPUT)]
========================================================================= */
.refund-card { 
  background: #fff;                             /* Màu nền khung form: TRẮNG TINH */
  padding: 35px;                                /* Đệm bên trong khung 35px */
  border-radius: 8px;                           /* Bo góc khung 8px */
  border: 1px solid #e0dcd5;                    /* Viền xám kem */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);   /* Đổ bóng nhẹ */
}

.form-section-title { 
  font-size: 15px; 
  color: #d1aa68;                               /* Màu tiêu đề từng mục (1, 2, 3): VÀNG HOÀNG KIM */
  margin-top: 20px; 
  margin-bottom: 15px; 
  border-bottom: 1px solid #eee;                /* Đường gạch dưới tiêu đề mục */
  padding-bottom: 8px; 
  font-weight: bold; 
}

.form-grid { 
  display: grid; 
  grid-template-columns: 1fr 1fr;               /* Chia thành 2 cột đều nhau */
  gap: 15px;                                    /* Khoảng cách giữa các ô nhập: 15px */
}

.full-width { 
  grid-column: span 2;                          /* Kéo dài chiếm trọn cả 2 cột */
}

.form-group label { 
  display: block; 
  font-size: 12px; 
  font-weight: bold; 
  margin-bottom: 5px; 
  color: #555;                                  /* Màu chữ nhãn: XÁM ĐẬM */
}

.form-group input, 
.form-group select, 
.form-group textarea { 
  width: 100%; 
  padding: 12px; 
  border: 1px solid #ddd;                       /* Viền ô nhập: XÁM NHẸ */
  border-radius: 4px;                           /* Bo góc ô nhập: 4px */
  font-size: 14px; 
  outline: none;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #d1aa68;                        /* Viền chuyển sang VÀNG khi người dùng bấm vào */
}

/* Ô nhập bị vô hiệu hóa (Mục thông tin cố định) */
.form-group input:disabled { 
  background: #f5f5f5;                          /* Màu nền ô khóa: XÁM NHẸ */
  color: #777;                                  /* Màu chữ ô khóa: XÁM */
  cursor: not-allowed;                          /* Chuột hình vòng tròn gạch chéo */
}

/* Khung cảnh báo quy định hoàn hàng */
.terms-warning-box { 
  background: #fffbebfb;                        /* Màu nền cảnh báo: VÀNG KEM NHẸ */
  border: 1px solid #fde68a;                    /* Viền cảnh báo: VÀNG CAM */
  padding: 15px; 
  border-radius: 6px; 
  color: #b45309;                               /* Màu chữ cảnh báo: NÂU CAM */
  font-size: 13px; 
  margin-top: 20px; 
  line-height: 1.5; 
}

/* =========================================================================
   [CSS NHÓM 3: KHUNG TRA CỨU TÊN VÀ XEM TRƯỚC ẢNH]
========================================================================= */
.account-name-wrapper { position: relative; }

/* Dòng trạng thái đang tra cứu tên chủ tài khoản */
.lookup-status { 
  position: absolute; 
  right: 12px; 
  top: 12px; 
  font-size: 12px; 
  color: #888;                                  /* Màu chữ đang tra cứu: XÁM */
  font-weight: bold; 
}
.lookup-status.success { 
  color: #2e7d32;                               /* Màu chữ tra cứu thành công: XANH LÁ */
}

.image-preview-grid { 
  display: flex; 
  gap: 10px; 
  margin-top: 10px; 
  flex-wrap: wrap; 
}

.img-preview { 
  position: relative; 
  width: 90px;                                  /* Chiều rộng khung ảnh nhỏ: 90px */
  height: 90px;                                 /* Chiều cao khung ảnh nhỏ: 90px */
  border: 1px solid #ccc; 
  border-radius: 4px; 
  overflow: hidden; 
}
.img-preview img { 
  width: 100%; 
  height: 100%; 
  object-fit: cover; 
}

/* Nút dấu X màu đỏ xóa từng ảnh xem trước */
.btn-remove-img { 
  position: absolute; 
  top: 2px; 
  right: 2px; 
  background: red;                              /* Màu nền nút xóa ảnh: ĐỎ TƯƠI */
  color: white;                                 /* Dấu X: TRẮNG */
  border: none; 
  border-radius: 50%;                           /* Nút hình tròn hoàn toàn */
  width: 20px; 
  height: 20px; 
  cursor: pointer; 
  font-size: 10px; 
}

/* =========================================================================
   [CSS NHÓM 4: NÚT GỬI YÊU CẦU CHÍNH TRÊN FORM]
========================================================================= */
.btn-submit-refund { 
  width: 100%;                                  /* Nút bấm kéo dài 100% chiều rộng form */
  padding: 16px; 
  background: #3e332e;                          /* Màu nền nút gửi: NÂU GỖ ĐẬM */
  color: white;                                 /* Màu chữ nút gửi: TRẮNG TINH */
  font-weight: bold; 
  border: none; 
  margin-top: 20px; 
  cursor: pointer; 
  border-radius: 4px; 
  transition: 0.3s; 
}
.btn-submit-refund:hover { 
  background: #d1aa68;                          /* Đổi sang MÀU VÀNG HOÀNG KIM khi rê chuột vào */
}
.btn-submit-refund:disabled {
  background: #888;                             /* Màu xám khi đang bận gửi dữ liệu */
  cursor: not-allowed;
}

/* =========================================================================
   [CSS NHÓM 5: KHUNG MODAL QUY ĐỊNH & MODAL OTP]
========================================================================= */
.otp-modal-overlay { 
  position: fixed;                              /* Cố định toàn màn hình */
  inset: 0;                                     /* top, right, bottom, left = 0 */
  background: rgba(0,0,0,0.7);                  /* Màu nền ĐEN mờ 70% */
  display: flex; 
  justify-content: center;                      /* Căn giữa theo chiều ngang */
  align-items: center;                          /* Căn giữa theo chiều dọc */
  z-index: 100; 
}

.otp-modal-box { 
  background: white;                            /* Màu nền hộp popup: TRẮNG TINH */
  padding: 30px; 
  border-radius: 8px;                           /* Bo góc hộp 8px */
  max-width: 480px;                             /* Chiều rộng tối đa 480px */
  text-align: center;                           /* Căn chữ ra CHÍNH GIỮA */
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2); 
}

.gold-title { 
  color: #d1aa68;                               /* Tiêu đề modal: VÀNG HOÀNG KIM */
  font-size: 18px; 
  margin-bottom: 15px; 
}

.terms-content { 
  text-align: left; 
  background: #fdfbf7;                          /* Nền khung điều khoản: KEM NHẠT */
  padding: 15px; 
  border: 1px solid #f0e6d2;                    /* Viền khung điều khoản: VÀNG KEM */
  border-radius: 6px; 
  font-size: 13px; 
  line-height: 1.6; 
  color: #444; 
  margin-bottom: 20px; 
}

/* Ô nhập mã OTP 6 số to rõ */
.otp-input { 
  text-align: center;                           /* Căn số OTP ra CHÍNH GIỮA */
  font-size: 24px;                              /* Cỡ chữ 24px */
  letter-spacing: 5px;                          /* Khoảng cách giữa các chữ số là 5px */
  margin: 20px 0; 
  width: 100%; 
  padding: 10px; 
  border: 2px solid #d1aa68;                    /* Viền ô OTP: VÀNG HOÀNG KIM */
  border-radius: 4px; 
}

.otp-actions { 
  display: flex; 
  gap: 10px; 
  justify-content: center;                      /* Căn 2 nút ra CHÍNH GIỮA */
}

/* Nút Xác nhận OTP / Đồng ý điều khoản */
.btn-confirm-otp { 
  background: #d1aa68;                          /* Màu nền nút Xác nhận: VÀNG HOÀNG KIM */
  color: white;                                 /* Màu chữ: TRẮNG */
  border: none; 
  padding: 10px 20px; 
  font-weight: bold; 
  border-radius: 4px; 
  cursor: pointer; 
  
  /* THAY THẾ: Nút xác nhận OTP chuyển sang MÀU XANH LÁ:
  background: #16a34a; */
  /* THAY THẾ: Nút xác nhận OTP bo tròn hoàn toàn:
  border-radius: 50px; */
}
.btn-confirm-otp:hover {
  background: #b88d4c;
}

/* Nút Hủy bỏ trong popup */
.btn-cancel-otp { 
  background: #ccc;                             /* Màu nền nút Hủy: XÁM */
  color: #333;                                  /* Màu chữ: ĐEN XÁM */
  border: none; 
  padding: 10px 20px; 
  border-radius: 4px; 
  cursor: pointer; 
  
  /* THAY THẾ: Nút Hủy bo tròn hoàn toàn:
  border-radius: 50px; */
}
.btn-cancel-otp:hover {
  background: #bbb;
}

/* =========================================================================
   [CSS NHÓM 6: TOAST THÔNG BÁO TỰ TẮT (VỊ TRÍ & MÀU SẮC TOAST)]
========================================================================= */
.custom-alert-toast {
  position: fixed;                              /* Cố định vị trí trên màn hình */
  
  /* [VỊ TRÍ HIỂN THỊ CỦA THÔNG BÁO TOAST] */
  bottom: 30px;                                 /* DÒNG MẶC ĐỊNH: Cách mép dưới 30px */
  right: 30px;                                  /* DÒNG MẶC ĐỊNH: Cách mép phải 30px (GÓC DƯỚI PHẢI) */

  /* THAY THẾ: Chuyển Toast sang GÓC DƯỚI BÊN TRÁI:
  bottom: 30px; left: 30px; */
  
  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN PHẢI:
  top: 30px; right: 30px; */
  
  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN TRÁI:
  top: 30px; left: 30px; */
  
  /* THAY THẾ: Căn Toast ra CHÍNH GIỮA MÀN HÌNH PHÍA TRÊN:
  top: 30px; left: 50%; transform: translateX(-50%); */

  padding: 14px 22px; 
  border-radius: 6px; 
  background: #333;                             /* Nền mặc định: ĐEN XÁM */
  color: #fff;                                  /* Chữ bên trong: TRẮNG */
  font-size: 14px; 
  font-weight: 500; 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); 
  z-index: 9999; 
  transform: translateY(100px);                 /* Vị trí ẩn: Thụt xuống 100px */
  opacity: 0;                                   /* Độ trong suốt bằng 0 */
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55); 
}

/* Khi kích hoạt hiển thị thông báo */
.custom-alert-toast.show {
  transform: translateY(0);                     /* Trượt lên vị trí chuẩn */
  opacity: 1;                                   /* Hiện rõ 100% */
}

/* Khi thông báo là THÀNH CÔNG */
.custom-alert-toast.success { 
  background: #198754;                          /* Nền thông báo thành công: MÀU XANH LÁ */
}

/* Khi thông báo là THẤT BẠI / LỖI */
.custom-alert-toast.error { 
  background: #dc3545;                          /* Nền thông báo lỗi: MÀU ĐỎ */
}
</style>