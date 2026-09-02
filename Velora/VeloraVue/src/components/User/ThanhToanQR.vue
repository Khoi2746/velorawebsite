<template>
  <div class="qr-page-wrapper">
    <!-- =========================================================================
         [PHẦN 1: HEADER TRANG QUÉT MÃ QR]
    ========================================================================== -->
    <Header v-if="Header" />

    <!-- =========================================================================
         [PHẦN 2: MÀN HÌNH CHÍNH - QUÉT MÃ QR VÀ THÔNG TIN THANH TOÁN]
    ========================================================================== -->
    <main class="qr-main-content" v-if="orderInfo && paymentStatus === 'SHOW_QR'">
      
      <!-- 2.1 TIÊU ĐỀ TRANG -->
      <div class="section-header">
        <!-- DÒNG MẶC ĐỊNH: Chữ 'THANH TOÁN' màu vàng hoàng kim (class gold) -->
        <h2>QUÉT MÃ <span class="gold">THANH TOÁN</span></h2>
        <!-- THAY THẾ: Đổi màu chữ 'THANH TOÁN' sang ĐỎ:
        <h2>QUÉT MÃ <span style="color: #dc2626;">THANH TOÁN</span></h2> -->
        <!-- THAY THẾ: Đổi màu chữ 'THANH TOÁN' sang XANH LÁ:
        <h2>QUÉT MÃ <span style="color: #16a34a;">THANH TOÁN</span></h2> -->

        <!-- Họa tiết trang trí đường kẻ & viên kim cương -->
        <div class="header-divider"><span class="diamond"></span></div>
        
        <!-- Dòng chú thích tự động duyệt tiền -->
        <p class="qr-notice">Hệ thống tự động duyệt tiền ngay khi chuyển khoản thành công.</p>
      </div>

      <div class="container qr-container">
        <div class="qr-grid">
          
          <!-- -------------------------------------------------------------------
               2.2 KHUNG BÊN TRÁI: HIỂN THỊ ẢNH MÃ QR TỰ ĐỘNG TỪ VIETQR
          -------------------------------------------------------------------- -->
          <div class="qr-left-box">
            <h3 class="box-title">Mã QR VietQR Tự Động</h3>
            
            <!-- Khung bọc ảnh QR -->
            <div class="qr-image-wrapper">
              <img :src="generateQRUrl" alt="VietQR Velora" class="main-qr-image" />
            </div>
            
            <!-- Lời nhắc giữ nguyên nội dung chuyển khoản -->
            <p class="scan-instruction">💡 Mở App Ngân hàng quét mã. <strong>Giữ nguyên nội dung chuyển khoản!</strong></p>
          </div>

          <!-- -------------------------------------------------------------------
               2.3 KHUNG BÊN PHẢI: BIÊN LAI THÔNG TIN ĐƠN & TRẠNG THÁI CHỜ
          -------------------------------------------------------------------- -->
          <div class="qr-right-box">
            <h3 class="box-title">Thông tin đơn hàng</h3>
            
            <!-- Thẻ biên lai chi tiết -->
            <div class="receipt-card">
              <!-- Mã đơn -->
              <div class="receipt-row">
                <span>Mã đơn hàng:</span>
                <strong>{{ orderInfo.code }}</strong>
              </div>
              
              <!-- Tên khách hàng -->
              <div class="receipt-row">
                <span>Khách hàng:</span>
                <strong>{{ orderInfo.buyer }}</strong>
              </div>
              
              <!-- Số điện thoại -->
              <div class="receipt-row">
                <span>Số điện thoại:</span>
                <strong>{{ orderInfo.phone }}</strong>
              </div>
              
              <div class="receipt-divider"></div>
              
              <!-- Tổng tiền cần thanh toán -->
              <div class="receipt-row total-row">
                <span>SỐ TIỀN THANH TOÁN:</span>
                <!-- DÒNG MẶC ĐỊNH: Số tiền to rõ màu vàng kim -->
                <span class="gold price-large">{{ formatPrice(orderInfo.amount) }}</span>
                <!-- THAY THẾ: Số tiền màu đỏ nổi bật:
                <span class="price-large" style="color: #dc2626; font-weight: bold;">{{ formatPrice(orderInfo.amount) }}</span> -->
              </div>
            </div>

            <!-- Khung vòng xoay báo hiệu hệ thống đang lắng nghe webhook ngân hàng -->
            <div class="auto-status-box">
              <div class="garena-spinner-small"></div>
              <span>Đang chờ tín hiệu thanh toán từ ngân hàng...</span>
            </div>
          </div>

        </div>
      </div>
    </main>

    <!-- =========================================================================
         [PHẦN 3: MÀN HÌNH CHỜ / LOADING KHI ĐANG TẢI THÔNG TIN]
    ========================================================================== -->
    <main class="qr-main-content loading-state" v-else-if="paymentStatus === 'LOADING'">
      <div class="loader"></div>
      <p>Đang chuẩn bị mã thanh toán...</p>
    </main>

    <!-- =========================================================================
         [PHẦN 4: MÀN HÌNH THÔNG BÁO GIAO DỊCH THÀNH CÔNG]
    ========================================================================== -->
    <main class="qr-main-content" v-else-if="paymentStatus === 'SUCCESS'">
      <div class="garena-result-box">
        <!-- Biểu tượng dấu tích xanh thành công -->
        <div class="garena-success-icon">✓</div>
        
        <h2 class="garena-title-success">Thanh toán đã hoàn tất!</h2>
        
        <p class="garena-text-id">Mã đơn hàng: <span>{{ orderInfo?.code }}</span></p>
        
        <p class="garena-text-gray">SePay đã ghi nhận giao dịch thành công. Đơn hàng đang chuyển qua bộ phận đóng gói.</p>
        
        <!-- NÚT CHUYỂN VỀ TRANG LỊCH SỬ ĐƠN HÀNG -->
        <!-- DÒNG MẶC ĐỊNH: Nút màu nâu gỗ đậm -->
        <button class="garena-btn btn-red" @click="goToOrders">
          Xem lịch sử đơn hàng
        </button>
        <!-- THAY THẾ: Nút màu vàng hoàng kim:
        <button class="garena-btn btn-red" style="background-color: #d1aa68; color: #fff;" @click="goToOrders">Xem lịch sử đơn hàng</button> -->
        <!-- THAY THẾ: Nút màu xanh lá thành công:
        <button class="garena-btn btn-red" style="background-color: #16a34a; color: #fff;" @click="goToOrders">Xem lịch sử đơn hàng</button> -->
        <!-- THAY THẾ: Nút bo tròn hình viên thuốc:
        <button class="garena-btn btn-red" style="border-radius: 50px;" @click="goToOrders">Xem lịch sử đơn hàng</button> -->
      </div>
    </main>

    <Footer v-if="Footer" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// Component Header & Footer dùng chung của website
import Header from '../../components/Header.vue';
import Footer from '../../components/Footer.vue';

const route = useRoute();
const router = useRouter();

// =========================================================================
// [LOGIC 1: KHỞI TẠO STATE & COMPUTED TẠO LINK VIETQR]
// =========================================================================
// Trạng thái trang: 'LOADING' (Đang tải) | 'SHOW_QR' (Hiện mã QR) | 'SUCCESS' (Đã trả tiền xong)
const paymentStatus = ref('LOADING');

// Thông tin đơn hàng lấy từ URL Query Params
const orderInfo = ref(null);

// Biến giữ tiến trình tra soát ngầm theo chu kỳ
let autoCheckInterval = null;

// Tự động tạo link ảnh mã QR VietQR chuẩn mẫu compact2
const generateQRUrl = computed(() => {
  if (!orderInfo.value) return '';
  
  // DÒNG MẶC ĐỊNH: Cấu hình tài khoản ngân hàng MBBank
  return `https://img.vietqr.io/image/MB-1003172056-compact2.png?amount=${orderInfo.value.amount}&addInfo=${encodeURIComponent(orderInfo.value.code)}&accountName=NGUYEN%20LE%20QUOC%20THANG`;
  
  // THAY THẾ: Đổi sang ngân hàng khác (Ví dụ Vietcombank - STK 99998888):
  // return `https://img.vietqr.io/image/VCB-99998888-compact2.png?amount=${orderInfo.value.amount}&addInfo=${encodeURIComponent(orderInfo.value.code)}&accountName=NGUYEN%20LE%20QUOC%20THANG`;
});

// Format số tiền sang chuẩn tiền tệ VNĐ
const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// =========================================================================
// [LOGIC 2: HÀM QUÉT NGẦM TRẠNG THÁI THANH TOÁN (AUTO POLLING)]
// =========================================================================
const startAutoCheck = () => {
  // DÒNG MẶC ĐỊNH: Cứ mỗi 1.2 giây (1200ms) gọi API kiểm tra trạng thái 1 lần
  autoCheckInterval = setInterval(async () => {
    if (!orderInfo.value) return;
    try {
      // Gọi API Backend kiểm tra xem SePay đã bắn webhook khớp tiền cho mã đơn này chưa
      const response = await fetch(`http://localhost:8080/api/don-hang/check-status?code=${encodeURIComponent(orderInfo.value.code)}`);
      
      if (response.ok) {
        const data = await response.json();
        // Nếu đã thanh toán thành công
        if (data.paid === true || data.status === 'DA_THANH_TOAN' || data.status === 'Đã thanh toán') {
          clearInterval(autoCheckInterval);                   // Dừng quét ngầm
          paymentStatus.value = 'SUCCESS';                    // Đổi giao diện sang thông báo THÀNH CÔNG
          window.dispatchEvent(new Event('cart-updated'));     // Bắn sự kiện cập nhật lại icon giỏ hàng trên Header
        }
      }
    } catch (error) {
      console.error("Lỗi tra soát ngầm:", error);
    }
  }, 1200);

  // THAY THẾ: Tăng thời gian quét ngầm lên 2.5 giây (2500ms) để giảm tải cho server:
  // }, 2500);
};

// Hủy vòng lặp quét ngầm khi người dùng rời khỏi trang này
onUnmounted(() => {
  if (autoCheckInterval) clearInterval(autoCheckInterval);
});

// =========================================================================
// [LOGIC 3: KHỞI TẠO DỮ LIỆU KHI VÀO TRANG]
// =========================================================================
onMounted(() => {
  // Kiểm tra trên URL có truyền đủ mã đơn (code) và số tiền (amount) không
  if (route.query.code && route.query.amount) {
    orderInfo.value = {
      code: route.query.code,
      amount: parseInt(route.query.amount),
      buyer: route.query.buyer || '',
      phone: route.query.phone || ''
    };
    paymentStatus.value = 'SHOW_QR';                          // Chuyển sang màn hình hiện mã QR
    startAutoCheck();                                         // Bắt đầu tiến trình kiểm tra tự động
  } else {
    // Nếu không có thông tin đơn trên URL -> Chuyển về trang cửa hàng
    router.push('/dong-ho-co-san');
  }
});

// Chuyển hướng sang trang lịch sử đơn hàng
const goToOrders = () => { 
  router.push('/don-hang'); 
};
</script>

<style scoped>
/* =========================================================================
   [CSS NHÓM 1: BỐ CỤC KHUNG CHÍNH & TIÊU ĐỀ]
========================================================================= */
.qr-page-wrapper { 
  background: #f4f1ea;                          /* Màu nền trang: KEM NHẠT */
  min-height: 100vh; 
  font-family: sans-serif; 
}

.qr-main-content { 
  padding: 40px 0 80px 0;                       /* Khoảng cách đệm: trên 40px, dưới 80px */
}

.section-header { 
  text-align: center;                           /* Căn toàn bộ tiêu đề ra CHÍNH GIỮA */
  margin-bottom: 40px; 
}

.section-header h2 { 
  color: #3e332e;                               /* Màu chữ tiêu đề: NÂU GỖ ĐẬM */
  font-size: 28px; 
  letter-spacing: 2px; 
}

.gold { 
  color: #d1aa68;                               /* Màu vàng hoàng kim */
}

.qr-notice { 
  color: #2e7d32;                               /* Dòng thông báo màu XANH LÁ */
  font-weight: bold; 
  font-size: 14px; 
  margin-top: 10px; 
}

.header-divider { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  margin-top: 10px; 
}

.header-divider::before, 
.header-divider::after { 
  content: ""; 
  width: 50px; 
  height: 1px; 
  background: #d1aa68;                          /* Đường kẻ trang trí màu vàng */
}

.diamond { 
  width: 6px; 
  height: 6px; 
  background: #d1aa68; 
  transform: rotate(45deg);                     /* Viên kim cương xoay 45 độ */
  margin: 0 10px; 
}

.container { 
  max-width: 1000px; 
  margin: 0 auto; 
  padding: 0 15px; 
}

.qr-grid { 
  display: flex; 
  gap: 40px;                                    /* Khoảng cách giữa 2 cột: 40px */
  align-items: flex-start; 
}

/* =========================================================================
   [CSS NHÓM 2: KHUNG HIỂN THỊ MÃ QR BÊN TRÁI]
========================================================================= */
.qr-left-box { 
  flex: 1;                                      /* Chiếm 50% độ rộng */
  background: #fff;                             /* Nền TRẮNG */
  border: 1px solid #e0dcd5;                    /* Viền xám kem */
  padding: 30px; 
  border-radius: 8px;                           /* Bo góc 8px */
  text-align: center;                           /* Căn giữa ảnh mã QR */
}

.box-title { 
  font-size: 16px; 
  color: #3e332e;                               /* Tiêu đề khung: NÂU GỖ ĐẬM */
  border-bottom: 1px solid #e0dcd5; 
  padding-bottom: 12px; 
  margin-top: 0; 
  margin-bottom: 20px; 
  text-align: left; 
}

.qr-image-wrapper { 
  width: 280px;                                 /* Chiều rộng khung mã QR: 280px */
  height: 280px;                                /* Chiều cao khung mã QR: 280px */
  margin: 0 auto; 
  border: 2px solid #3e332e;                    /* Viền dày màu NÂU GỖ ĐẬM */
  padding: 10px; 
  border-radius: 8px; 
  background: #fff;                             /* Nền TRẮNG */
}

.main-qr-image { 
  width: 100%; 
  height: 100%; 
  object-fit: contain;                          /* Hiển thị trọn vẹn toàn bộ mã QR */
}

.scan-instruction { 
  margin-top: 20px; 
  font-size: 13px; 
  color: #555;                                  /* Màu chữ hướng dẫn: XÁM ĐẬM */
}

/* =========================================================================
   [CSS NHÓM 3: KHUNG THÔNG TIN ĐƠN HÀNG BÊN PHẢI]
========================================================================= */
.qr-right-box { 
  flex: 1;                                      /* Chiếm 50% độ rộng */
  background: #fff;                             /* Nền TRẮNG */
  border: 1px solid #e0dcd5; 
  padding: 30px; 
  border-radius: 8px; 
}

.receipt-card { 
  background: #faf9f6;                          /* Nền biên lai: KEM NHẠT */
  border: 1px solid #e0dcd5; 
  padding: 20px; 
  border-radius: 6px; 
}

.receipt-row { 
  display: flex; 
  justify-content: space-between;               /* Tách nhãn sang trái, giá trị sang phải */
  margin-bottom: 12px; 
  font-size: 14px; 
  color: #555; 
}

.receipt-divider { 
  height: 1px; 
  background: #e0dcd5;                          /* Đường kẻ ngăn cách */
  margin: 15px 0; 
}

.total-row { 
  align-items: center; 
  font-weight: bold; 
  color: #3e332e; 
}

.price-large { 
  font-size: 22px;                              /* Cỡ chữ tổng tiền 22px */
}

/* Khung trạng thái xoay tròn đang đợi tín hiệu ngân hàng */
.auto-status-box { 
  margin-top: 25px; 
  padding: 15px; 
  background: #e8f5e9;                          /* Nền trạng thái: XANH LÁ NHẠT */
  border: 1px solid #c8e6c9;                    /* Viền: XANH LÁ */
  border-radius: 6px; 
  display: flex; 
  align-items: center; 
  gap: 12px; 
  color: #2e7d32;                               /* Chữ: XANH LÁ ĐẬM */
  font-size: 13px; 
  font-weight: bold; 
}

.garena-spinner-small { 
  width: 20px; 
  height: 20px; 
  border: 3px solid #a5d6a7; 
  border-top: 3px solid #2e7d32;                /* Vòng xoay màu xanh lá */
  border-radius: 50%; 
  animation: spin 0.8s linear infinite; 
}

/* =========================================================================
   [CSS NHÓM 4: KHUNG THÔNG BÁO THANH TOÁN THÀNH CÔNG]
========================================================================= */
.garena-result-box { 
  background: #fff;                             /* Nền TRẮNG */
  max-width: 500px;                             /* Chiều rộng tối đa 500px */
  margin: 40px auto;                            /* Căn giữa trang */
  padding: 40px; 
  border-radius: 8px; 
  text-align: center;                           /* Căn toàn bộ nội dung ra CHÍNH GIỮA */
  border: 1px solid #e0dcd5; 
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);      /* Đổ bóng nhẹ */
}

.garena-success-icon { 
  color: #2e7d32;                               /* Dấu tích v: MÀU XANH LÁ */
  font-size: 65px;                              /* Kích cỡ 65px */
  margin-bottom: 15px; 
  font-weight: bold; 
}

.garena-title-success { 
  font-size: 24px; 
  color: #333; 
  font-weight: bold; 
  margin-bottom: 8px; 
}

.garena-text-id { 
  font-size: 14px; 
  color: #666; 
  margin-bottom: 10px; 
}

.garena-text-id span { 
  color: #d1aa68;                               /* Mã đơn hàng hiển thị màu VÀNG HOÀNG KIM */
  font-weight: bold; 
}

.garena-text-gray { 
  color: #777; 
  font-size: 13px; 
  margin-bottom: 25px; 
}

.garena-btn { 
  width: 100%; 
  padding: 14px; 
  font-size: 14px; 
  font-weight: bold; 
  border: none; 
  border-radius: 4px; 
  cursor: pointer; 
}

.btn-red { 
  background: #3e332e;                          /* Màu nền nút: NÂU GỖ ĐẬM */
  color: white;                                 /* Màu chữ: TRẮNG TINH */
  text-transform: uppercase; 
  letter-spacing: 1px; 
  transition: 0.3s; 
}

.btn-red:hover { 
  background: #d1aa68;                          /* Đổi sang MÀU VÀNG khi rê chuột vào */
}

/* =========================================================================
   [CSS NHÓM 5: TRẠNG THÁI LOADING]
========================================================================= */
.loading-state { text-align: center; color: #666; padding: 100px 0; }
.loader { 
  width: 40px; 
  height: 40px; 
  border: 4px solid #ccc; 
  border-top-color: #3e332e;                    /* Vòng xoay màu nâu gỗ */
  border-radius: 50%; 
  animation: spin 1s linear infinite; 
  margin: 0 auto 15px; 
}

@keyframes spin { 
  0% { transform: rotate(0deg); } 
  100% { transform: rotate(360deg); } 
}
</style>