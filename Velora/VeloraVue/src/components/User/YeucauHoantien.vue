<template>
  <div class="refund-page">
    <Header />

    <main class="refund-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">YÊU CẦU HOÀN TIỀN & TRẢ HÀNG</h1>
          <div class="title-divider"><span class="diamond"></span></div>
        </div>

        <div class="refund-card" v-if="orderData">
          <form @submit.prevent="xuLyGuiYeuCau" class="refund-form">
            
            <!-- 1. THÔNG TIN ĐỐI SOÁT ĐƠN HÀNG GỐC -->
            <h3 class="form-section-title">1. Thông Tin Đơn Hàng (Không được sửa)</h3>
            <div class="form-grid">
              <div class="form-group">
                <label>Mã Đơn Hàng</label>
                <input type="text" :value="'#' + orderData.maDonHangCode" disabled />
              </div>
              <div class="form-group">
                <label>Họ và Tên</label>
                <input type="text" v-model="orderData.tenNguoiNhan" disabled />
              </div>
              <div class="form-group">
                <label>Số Điện Thoại</label>
                <input type="text" v-model="orderData.soDienThoai" disabled />
              </div>
              <div class="form-group">
                <label>Địa Chỉ Gmail Đặt Hàng</label>
                <input type="email" v-model="orderData.email" disabled />
              </div>
            </div>

            <!-- 2. THÔNG TIN NGÂN HÀNG HOÀN TIỀN -->
            <h3 class="form-section-title">2. Tài Khoản Nhận Tiền Hoàn</h3>
            <div class="form-grid">
              <div class="form-group">
                <label>Chọn Ngân Hàng *</label>
                <select v-model="selectedBank" @change="traCuuTenChuTaiKhoan" required>
                  <option value="">-- Chọn ngân hàng --</option>
                  <option v-for="bank in bankList" :key="bank.code" :value="bank">
                    {{ bank.shortName }} - {{ bank.name }}
                  </option>
                </select>
              </div>
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
                  <span v-if="isLookingUp" class="lookup-status"><i class="fas fa-spinner fa-spin"></i> Đang tra cứu...</span>
                  <span v-else-if="lookupSuccess" class="lookup-status success"><i class="fas fa-check-circle"></i> Đã xác minh</span>
                </div>
              </div>
            </div>

            <!-- 3. LÝ DO & MINH CHỨNG -->
            <h3 class="form-section-title">3. Lý Do Trả Hàng & Minh Chứng (2 - 6 ảnh)</h3>
            <div class="form-group full-width">
              <label>Lý Do Cụ Thể *</label>
              <textarea v-model="refundReason" rows="4" placeholder="Nhập lý do quý khách muốn hoàn tiền trả hàng..." required></textarea>
            </div>

            <div class="form-group full-width">
              <label>Tải Ảnh Minh Chứng Sản Phẩm (Tối thiểu 2 ảnh, tối đa 6 ảnh) *</label>
              <input type="file" multiple accept="image/*" @change="handleFileUpload" class="file-input" />
              
              <div class="image-preview-grid" v-if="previewImages.length > 0">
                <div class="img-preview" v-for="(img, idx) in previewImages" :key="idx">
                  <img :src="img" alt="Minh chứng" />
                  <button type="button" class="btn-remove-img" @click="removeImage(idx)">✕</button>
                </div>
              </div>
            </div>

            <!-- KHUNG THÔNG BÁO QUY ĐỊNH BẮT BUỘC -->
            <div class="terms-warning-box">
              <p><i class="fas fa-exclamation-triangle"></i> <strong>LƯU Ý QUAN TRỌNG:</strong> Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc nếu có lỗi từ nhà sản xuất. Nếu quý khách gửi yêu cầu hoàn hàng quá <strong>6 lần trong 1 năm</strong>, tài khoản sẽ tự động bị khóa và liệt vào danh sách đen của Velora.</p>
            </div>

            <button type="submit" class="btn-submit-refund" :disabled="isSubmitting">
              {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN GỬI YÊU CẦU' }}
            </button>
          </form>
        </div>
      </div>
    </main>

    <!-- MODAL XÁC NHẬN CẢNH BÁO QUY ĐỊNH -->
    <div class="otp-modal-overlay" v-if="showTermsModal">
      <div class="otp-modal-box terms-modal">
        <h3 class="gold-title"><i class="fas fa-shield-alt"></i> QUY ĐỊNH HOÀN HÀNG</h3>
        <div class="terms-content">
          <p>📌 <strong>Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc nếu có lỗi trực tiếp từ Nhà Sản Xuất.</strong></p>
          <p>⚠️ <strong>Lưu ý:</strong> Nếu khách hàng thực hiện hoàn hàng <strong>quá 6 lần trong vòng 1 năm</strong>, hệ thống sẽ tự động <strong>khóa tài khoản</strong> và vĩnh viễn đưa địa chỉ Email vào <strong>Danh sách đen (Blacklist)</strong>.</p>
        </div>
        <div class="otp-actions">
          <button @click="showTermsModal = false" class="btn-cancel-otp">HỦY BỎ</button>
          <button @click="dongYDieuKhoanVaGuiOtp" class="btn-confirm-otp">TÔI ĐỒNG Ý & TIẾP TỤC</button>
        </div>
      </div>
    </div>

    <!-- POPUP OTP XÁC THỰC -->
    <div class="otp-modal-overlay" v-if="showOtpModal">
      <div class="otp-modal-box">
        <h3>XÁC THỰC MÃ OTP</h3>
        <p>Hệ thống đã gửi mã OTP 6 số đến Gmail đặt hàng: <strong>{{ orderData?.email }}</strong>. Vui lòng nhập để hoàn tất yêu cầu.</p>
        <input type="text" v-model="otpCode" placeholder="Nhập 6 chữ số..." maxlength="6" class="otp-input" />
        
        <div class="otp-actions">
          <button @click="showOtpModal = false" class="btn-cancel-otp">Hủy</button>
          <button @click="xacNhanOtpHoanTien" class="btn-confirm-otp">XÁC NHẬN</button>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

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

const handleFileUpload = (e) => {
  const files = Array.from(e.target.files)
  if (files.length + previewImages.value.length > 6) {
    alert('Quý khách chỉ được chọn tối đa 6 ảnh minh chứng!')
    return
  }

  files.forEach(file => {
    const reader = new FileReader()
    reader.onload = (event) => { previewImages.value.push(event.target.result) }
    reader.readAsDataURL(file)
  })
}

const removeImage = (idx) => { previewImages.value.splice(idx, 1) }

const xuLyGuiYeuCau = () => {
  if (previewImages.value.length < 2) {
    alert('Vui lòng tải lên ít nhất 2 ảnh minh chứng sản phẩm!')
    return
  }

  if (!orderData.value || !orderData.value.email) {
    alert('Không tìm thấy Gmail đặt hàng của đơn này!')
    return
  }

  // Mở Popup Cảnh báo Quy định cho Khách hàng xác nhận
  showTermsModal.value = true
}

const dongYDieuKhoanVaGuiOtp = async () => {
  showTermsModal.value = false
  isSubmitting.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/hoan-tien/gui-otp?email=${encodeURIComponent(orderData.value.email)}`, {
      method: 'POST'
    })

    if (res.ok) {
      showOtpModal.value = true
    } else {
      const errorMsg = await res.text()
      alert('Không thể gửi mã OTP: ' + errorMsg)
    }
  } catch (err) {
    alert('Lỗi kết nối đến máy chủ Backend!')
  } finally {
    isSubmitting.value = false
  }
}

const xacNhanOtpHoanTien = async () => {
  if (!otpCode.value || otpCode.value.length !== 6) {
    alert('Vui lòng nhập đúng 6 chữ số OTP!')
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
      alert('Yêu cầu hoàn tiền đã được gửi thành công!')
      showOtpModal.value = false
      router.push('/don-hang')
    } else {
      alert(await res.text())
    }
  } catch (err) {
    alert('Có lỗi xảy ra khi xác nhận OTP!')
  }
}

onMounted(() => {
  const savedOrder = localStorage.getItem('selectedRefundOrder')
  const userStr = localStorage.getItem('user')

  if (!savedOrder) {
    alert('Không tìm thấy thông tin đơn hàng hoàn tiền!')
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
.refund-page { background: #f4f1ea; min-height: 100vh; font-family: sans-serif; }
.refund-content { padding: 40px 0 80px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 15px; }
.title-wrapper { text-align: center; margin-bottom: 30px; }
.title-divider { display: flex; justify-content: center; align-items: center; margin-top: 8px; }
.title-divider::before, .title-divider::after { content: ""; width: 50px; height: 1px; background: #d1aa68; }
.diamond { width: 6px; height: 6px; background: #d1aa68; transform: rotate(45deg); margin: 0 10px; }
.refund-card { background: #fff; padding: 35px; border-radius: 8px; border: 1px solid #e0dcd5; }
.form-section-title { font-size: 15px; color: #d1aa68; margin-top: 20px; margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 8px; font-weight: bold; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.full-width { grid-column: span 2; }
.form-group label { display: block; font-size: 12px; font-weight: bold; margin-bottom: 5px; color: #555; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.form-group input:disabled { background: #f5f5f5; color: #777; }

.terms-warning-box { background: #fffbebfb; border: 1px solid #fde68a; padding: 15px; border-radius: 6px; color: #b45309; font-size: 13px; margin-top: 20px; line-height: 1.5; }

.account-name-wrapper { position: relative; }
.lookup-status { position: absolute; right: 12px; top: 12px; font-size: 12px; color: #888; font-weight: bold; }
.lookup-status.success { color: #2e7d32; }

.image-preview-grid { display: flex; gap: 10px; margin-top: 10px; flex-wrap: wrap; }
.img-preview { position: relative; width: 90px; height: 90px; border: 1px solid #ccc; border-radius: 4px; overflow: hidden; }
.img-preview img { width: 100%; height: 100%; object-fit: cover; }
.btn-remove-img { position: absolute; top: 2px; right: 2px; background: red; color: white; border: none; border-radius: 50%; width: 20px; height: 20px; cursor: pointer; font-size: 10px; }
.btn-submit-refund { width: 100%; padding: 16px; background: #3e332e; color: white; font-weight: bold; border: none; margin-top: 20px; cursor: pointer; border-radius: 4px; transition: 0.3s; }
.btn-submit-refund:hover { background: #d1aa68; }

.otp-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); display: flex; justify-content: center; align-items: center; z-index: 100; }
.otp-modal-box { background: white; padding: 30px; border-radius: 8px; max-width: 480px; text-align: center; }
.gold-title { color: #d1aa68; font-size: 18px; margin-bottom: 15px; }
.terms-content { text-align: left; background: #fdfbf7; padding: 15px; border: 1px solid #f0e6d2; border-radius: 6px; font-size: 13px; line-height: 1.6; color: #444; margin-bottom: 20px; }
.otp-input { text-align: center; font-size: 24px; letter-spacing: 5px; margin: 20px 0; width: 100%; padding: 10px; border: 2px solid #d1aa68; border-radius: 4px; }
.otp-actions { display: flex; gap: 10px; justify-content: center; }
.btn-confirm-otp { background: #d1aa68; color: white; border: none; padding: 10px 20px; font-weight: bold; border-radius: 4px; cursor: pointer; }
.btn-cancel-otp { background: #ccc; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
</style>