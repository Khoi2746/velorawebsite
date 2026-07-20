<template>
  <div class="qr-page-wrapper">
    <!-- Header bọc v-if hoặc dùng nếu component tồn tại -->
    <Header v-if="Header" />

    <!-- MÀN HÌNH QUÉT MÃ QR -->
    <main class="qr-main-content" v-if="orderInfo && paymentStatus === 'SHOW_QR'">
      <div class="section-header">
        <h2>QUÉT MÃ <span class="gold">THANH TOÁN</span></h2>
        <div class="header-divider"><span class="diamond"></span></div>
        <p class="qr-notice">Hệ thống tự động duyệt tiền ngay khi chuyển khoản thành công.</p>
      </div>

      <div class="container qr-container">
        <div class="qr-grid">
          
          <div class="qr-left-box">
            <h3 class="box-title">Mã QR VietQR Tự Động</h3>
            <div class="qr-image-wrapper">
              <img :src="generateQRUrl" alt="VietQR Velora" class="main-qr-image" />
            </div>
            <p class="scan-instruction">💡 Mở App Ngân hàng quét mã. <strong>Giữ nguyên nội dung chuyển khoản!</strong></p>
          </div>

          <div class="qr-right-box">
            <h3 class="box-title">Thông tin đơn hàng</h3>
            <div class="receipt-card">
              <div class="receipt-row"><span>Mã đơn hàng:</span><strong>{{ orderInfo.code }}</strong></div>
              <div class="receipt-row"><span>Khách hàng:</span><strong>{{ orderInfo.buyer }}</strong></div>
              <div class="receipt-row"><span>Số điện thoại:</span><strong>{{ orderInfo.phone }}</strong></div>
              <div class="receipt-divider"></div>
              <div class="receipt-row total-row"><span>SỐ TIỀN THANH TOÁN:</span><span class="gold price-large">{{ formatPrice(orderInfo.amount) }}</span></div>
            </div>

            <div class="auto-status-box">
              <div class="garena-spinner-small"></div>
              <span>Đang chờ tín hiệu thanh toán từ ngân hàng...</span>
            </div>
          </div>

        </div>
      </div>
    </main>

    <!-- MÀN HÌNH CHỜ / LOAD KHI KHÔNG CÓ DỮ LIỆU -->
    <main class="qr-main-content loading-state" v-else-if="paymentStatus === 'LOADING'">
      <div class="loader"></div>
      <p>Đang chuẩn bị mã thanh toán...</p>
    </main>

    <!-- MÀN HÌNH CHUYỂN SANG THÀNH CÔNG -->
    <main class="qr-main-content" v-else-if="paymentStatus === 'SUCCESS'">
      <div class="garena-result-box">
        <div class="garena-success-icon">✓</div>
        <h2 class="garena-title-success">Thanh toán đã hoàn tất!</h2>
        <p class="garena-text-id">Mã đơn hàng: <span>{{ orderInfo?.code }}</span></p>
        <p class="garena-text-gray">SePay đã ghi nhận giao dịch thành công. Đơn hàng đang chuyển qua bộ phận đóng gói.</p>
        <button class="garena-btn btn-red" @click="goToOrders">Xem lịch sử đơn hàng</button>
      </div>
    </main>

    <Footer v-if="Footer" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// Kiểm tra lại đường dẫn Header/Footer chính xác theo cấu trúc dự án của bạn
import Header from '../../components/Header.vue';
import Footer from '../../components/Footer.vue';

const route = useRoute();
const router = useRouter();

const paymentStatus = ref('LOADING');
const orderInfo = ref(null);
let autoCheckInterval = null;

const generateQRUrl = computed(() => {
  if (!orderInfo.value) return '';
  return `https://img.vietqr.io/image/MB-1003172056-compact2.png?amount=${orderInfo.value.amount}&addInfo=${encodeURIComponent(orderInfo.value.code)}&accountName=NGUYEN%20LE%20QUOC%20THANG`;
});

const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const startAutoCheck = () => {
  autoCheckInterval = setInterval(async () => {
    if (!orderInfo.value) return;
    try {
      const response = await fetch(`http://localhost:8080/api/don-hang/check-status?code=${encodeURIComponent(orderInfo.value.code)}`);
      if (response.ok) {
        const data = await response.json();
        if (data.paid === true || data.status === 'DA_THANH_TOAN' || data.status === 'Đã thanh toán') {
          clearInterval(autoCheckInterval);
          paymentStatus.value = 'SUCCESS';
          window.dispatchEvent(new Event('cart-updated'));
        }
      }
    } catch (error) {
      console.error("Lỗi tra soát ngầm:", error);
    }
  }, 1200);
};

onUnmounted(() => {
  if (autoCheckInterval) clearInterval(autoCheckInterval);
});

onMounted(() => {
  if (route.query.code && route.query.amount) {
    orderInfo.value = {
      code: route.query.code,
      amount: parseInt(route.query.amount),
      buyer: route.query.buyer || '',
      phone: route.query.phone || ''
    };
    paymentStatus.value = 'SHOW_QR';
    startAutoCheck();
  } else {
    // Nếu không có thông tin đơn trên URL, chuyển về trang danh sách sản phẩm
    router.push('/dong-ho-co-san');
  }
});

const goToOrders = () => { router.push('/don-hang'); };
</script>

<style scoped>
.qr-page-wrapper { background: #f4f1ea; min-height: 100vh; font-family: sans-serif; }
.qr-main-content { padding: 40px 0 80px 0; }
.section-header { text-align: center; margin-bottom: 40px; }
.section-header h2 { color: #3e332e; font-size: 28px; letter-spacing: 2px; }
.gold { color: #d1aa68; }
.qr-notice { color: #2e7d32; font-weight: bold; font-size: 14px; margin-top: 10px; }
.header-divider { display: flex; justify-content: center; align-items: center; margin-top: 10px; }
.header-divider::before, .header-divider::after { content: ""; width: 50px; height: 1px; background: #d1aa68; }
.diamond { width: 6px; height: 6px; background: #d1aa68; transform: rotate(45deg); margin: 0 10px; }
.container { max-width: 1000px; margin: 0 auto; padding: 0 15px; }
.qr-grid { display: flex; gap: 40px; align-items: flex-start; }
.qr-left-box { flex: 1; background: #fff; border: 1px solid #e0dcd5; padding: 30px; border-radius: 8px; text-align: center; }
.qr-right-box { flex: 1; background: #fff; border: 1px solid #e0dcd5; padding: 30px; border-radius: 8px; }
.box-title { font-size: 16px; color: #3e332e; border-bottom: 1px solid #e0dcd5; padding-bottom: 12px; margin-top: 0; margin-bottom: 20px; text-align: left; }
.qr-image-wrapper { width: 280px; height: 280px; margin: 0 auto; border: 2px solid #3e332e; padding: 10px; border-radius: 8px; background: #fff; }
.main-qr-image { width: 100%; height: 100%; object-fit: contain; }
.scan-instruction { margin-top: 20px; font-size: 13px; color: #555; }
.receipt-card { background: #faf9f6; border: 1px solid #e0dcd5; padding: 20px; border-radius: 6px; }
.receipt-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; color: #555; }
.receipt-divider { height: 1px; background: #e0dcd5; margin: 15px 0; }
.total-row { align-items: center; font-weight: bold; color: #3e332e; }
.price-large { font-size: 22px; }
.auto-status-box { margin-top: 25px; padding: 15px; background: #e8f5e9; border: 1px solid #c8e6c9; border-radius: 6px; display: flex; align-items: center; gap: 12px; color: #2e7d32; font-size: 13px; font-weight: bold; }
.garena-spinner-small { width: 20px; height: 20px; border: 3px solid #a5d6a7; border-top: 3px solid #2e7d32; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.garena-result-box { background: #fff; max-width: 500px; margin: 40px auto; padding: 40px; border-radius: 8px; text-align: center; border: 1px solid #e0dcd5; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.garena-success-icon { color: #2e7d32; font-size: 65px; margin-bottom: 15px; font-weight: bold; }
.garena-title-success { font-size: 24px; color: #333; font-weight: bold; margin-bottom: 8px; }
.garena-text-id { font-size: 14px; color: #666; margin-bottom: 10px; }
.garena-text-id span { color: #d1aa68; font-weight: bold; }
.garena-text-gray { color: #777; font-size: 13px; margin-bottom: 25px; }
.garena-btn { width: 100%; padding: 14px; font-size: 14px; font-weight: bold; border: none; border-radius: 4px; cursor: pointer; }
.btn-red { background: #3e332e; color: white; text-transform: uppercase; letter-spacing: 1px; transition: 0.3s; }
.btn-red:hover { background: #d1aa68; }
.loading-state { text-align: center; color: #666; padding: 100px 0; }
.loader { width: 40px; height: 40px; border: 4px solid #ccc; border-top-color: #3e332e; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
</style>