<template>
  <div class="order-page">
    <Header />

    <main class="order-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">ĐƠN HÀNG CỦA TÔI</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <div class="order-layout" v-if="orders.length > 0">
          <div class="order-list-section">
            <h2 class="section-title">LỊCH SỬ GIAO DỊCH</h2>
            
            <div class="order-list">
              <div 
                v-for="order in orders" 
                :key="order.maDonHang" 
                class="order-card"
                :class="{ 'active': selectedOrder && selectedOrder.maDonHang === order.maDonHang }"
                @click="selectOrder(order)"
              >
                <div class="order-card-header">
                  <span class="order-code">#{{ order.maDonHangCode }}</span>
                  <span class="order-date">{{ order.ngayTao }}</span>
                </div>
                <div class="order-card-body">
                  <div class="order-total">{{ formatPrice(order.tongTien) }}</div>
                  <div class="order-status" :class="getStatusClass(order.trangThaiDonHang)">
                    {{ getStatusText(order.trangThaiDonHang) }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="order-detail-section">
            <div v-if="selectedOrder" class="detail-box">
              
              <div class="detail-header">
                <h2>CHI TIẾT ĐƠN HÀNG <span>#{{ selectedOrder.maDonHangCode }}</span></h2>
                <button class="btn-print" @click="window.print()"><i class="fas fa-print"></i> IN BIÊN LAI</button>
              </div>

              <div class="tracking-timeline">
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 1), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 1 }">
                  <div class="step-icon"><i class="fas fa-file-invoice-dollar"></i></div>
                  <p>Chờ xử lý</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 1 }"></div>
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 2), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 2 }">
                  <div class="step-icon"><i class="fas fa-box-open"></i></div>
                  <p>Chuẩn bị</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 2 }"></div>
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 3), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 3 }">
                  <div class="step-icon"><i class="fas fa-truck-fast"></i></div>
                  <p>Đang giao</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 3 }"></div>
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 4), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 4 }">
                  <div class="step-icon"><i class="fas fa-check"></i></div>
                  <p>Đã giao</p>
                </div>
              </div>

              <div class="info-map-grid">
                <div class="info-panel">
                  <h3>THÔNG TIN NHẬN HÀNG</h3>
                  <p><strong>Người nhận:</strong> {{ selectedOrder.tenNguoiNhan }}</p>
                  <p><strong>Số điện thoại:</strong> {{ selectedOrder.soDienThoai }}</p>
                  <p><strong>Địa chỉ:</strong> {{ selectedOrder.diaChi }}</p>
                  <p><strong>Thanh toán:</strong> {{ selectedOrder.phuongThucThanhToan }}</p>
                </div>
                
                <div class="map-panel">
                  <iframe 
                    src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.325316278854!2d106.69499931474895!3d10.7863769923145!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f3458d926fb%3A0xc68297cbbf9f1a23!2sBitexco%20Financial%20Tower!5e0!3m2!1sen!2s!4v1623145214002!5m2!1sen!2s" 
                    width="100%" 
                    height="100%" 
                    style="border:0;" 
                    allowfullscreen="" 
                    loading="lazy"
                    class="luxury-map">
                  </iframe>
                  <div class="map-overlay" v-if="selectedOrder.trangThaiDonHang === 'DANG_GIAO'">
                    <div class="pulse-dot"></div> Đang di chuyển
                  </div>
                </div>
              </div>

              <div class="order-items">
                <h3>SẢN PHẨM TRONG ĐƠN</h3>
                <table class="item-table">
                  <tbody>
                    <tr v-for="(item, idx) in selectedOrder.items" :key="idx">
                      <td class="td-img">
                        <img :src="item.anh && item.anh.startsWith('http') ? item.anh : '/img/' + item.anh" alt="">
                      </td>
                      <td class="td-name">
                        <span class="p-name">{{ item.ten }}</span>
                        <span class="p-qty">x {{ item.soLuong }}</span>
                      </td>
                      <td class="td-price">{{ formatPrice(item.gia) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

            </div>
            <div v-else class="no-selection">
              <i class="fas fa-file-signature"></i>
              <p>Vui lòng chọn một giao dịch để xem chi tiết.</p>
            </div>
          </div>
        </div>

        <div v-else class="empty-orders">
          <i class="fas fa-receipt empty-icon"></i>
          <p>Quý khách chưa có lịch sử giao dịch nào tại Velora Clock.</p>
          <router-link to="/dong-ho-co-san" class="btn-shopnow">
            MUA SẮM SIÊU PHẨM NGAY
          </router-link>
        </div>

      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const router = useRouter()
const orders = ref([])
const selectedOrder = ref(null)

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// ================= FETCH DATA TỪ BACKEND SPRING BOOT =================
const fetchUserOrders = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      alert('Vui lòng đăng nhập để xem lịch sử đơn hàng!')
      router.push('/dang-nhap')
      return
    }
    
    const user = JSON.parse(userStr)
    // Gọi API lấy đơn hàng theo mã người dùng đăng nhập
    const response = await fetch(`http://localhost:8080/api/don-hang/nguoi-dung/${user.maNguoiDung || user.id}`)
    
    if (response.ok) {
      const data = await response.json()
      
      // MAPPING LAYER: Đồng bộ cấu trúc Object từ JPA Entities Backend trả về sang Frontend
      orders.value = data.map(order => ({
        maDonHang: order.maDonHang,
        maDonHangCode: order.maDonHangCode,
        ngayTao: new Date(order.ngayTao).toLocaleDateString('vi-VN'),
        tongTien: order.tongTien,
        tenNguoiNhan: order.tenNguoiNhan,
        soDienThoai: order.soDienThoaiGiaoHang,
        diaChi: order.diaChiGiaoHang,
        trangThaiDonHang: order.trangThaiDonHang,
        phuongThucThanhToan: order.phuongThucThanhToan,
        // Duyệt mảng ChiTietDonHang lồng bên trong thực thể Đơn Hàng số đông
        items: (order.chiTietDonHangs || []).map(ct => ({
          ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Kiệt tác thời gian',
          soLuong: ct.soLuong,
          gia: ct.giaLucMua,
          anh: ct.sanPham ? ct.sanPham.anhDaiDien : ''
        }))
      }))

      // Mặc định chọn ngay đơn hàng mới nhất ở đầu danh sách để hiển thị bên phải
      if (orders.value.length > 0) {
        selectedOrder.value = orders.value[0]
      }
    } else {
      console.error('Không thể lấy danh sách đơn hàng thực tế:', response.status)
    }
  } catch (error) {
    console.error('Lỗi kết nối API đơn hàng đến Spring Boot:', error)
  }
}

const selectOrder = (order) => {
  selectedOrder.value = order
}

// ================= LOGIC TRACKING TIMELINE =================
const getStepLevel = (status) => {
  switch(status) {
    case 'CHO_XU_LY': return 1;
    case 'DANG_CHUAN_BI': return 2;
    case 'DANG_GIAO': return 3;
    case 'DA_GIAO': return 4;
    default: return 1;
  }
}

const isStepCompleted = (status, stepNumber) => {
  return getStepLevel(status) > stepNumber;
}

const getStatusText = (status) => {
  const map = {
    'CHO_XU_LY': 'Đang chờ xử lý',
    'DANG_CHUAN_BI': 'Đang chuẩn bị hàng',
    'DANG_GIAO': 'Đang vận chuyển',
    'DA_GIAO': 'Đã giao thành công',
    'DA_HUY': 'Đơn hàng đã hủy'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    'CHO_XU_LY': 'status-pending',
    'DANG_CHUAN_BI': 'status-prep',
    'DANG_GIAO': 'status-shipping',
    'DA_GIAO': 'status-delivered',
    'DA_HUY': 'status-cancelled'
  }
  return map[status] || ''
}

onMounted(() => {
  fetchUserOrders()
})
</script>

<style scoped>
.order-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8;
}

.order-content {
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
.order-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 30px;
  align-items: start;
}

/* ================= CỘT TRÁI ================= */
.section-title {
  font-size: 13px;
  color: #555555;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  background-color: #ffffff;
  border: 1px solid #eeeeee;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-card:hover {
  border-color: #cccccc;
}

.order-card.active {
  border-color: #d1aa68;
  box-shadow: 0 5px 15px rgba(209, 170, 104, 0.1);
  background-color: rgba(209, 170, 104, 0.02);
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.order-code {
  font-size: 13px;
  font-weight: 600;
  color: #111111;
}

.order-date {
  font-size: 11px;
  color: #888888;
}

.order-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-total {
  font-size: 14px;
  color: #d1aa68;
  font-weight: bold;
}

.order-status {
  font-size: 10px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 2px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.status-pending { background-color: #f5f5f5; color: #555555; }
.status-prep { background-color: #fff9e6; color: #e6a200; }
.status-shipping { background-color: #e6f7ff; color: #0077ff; }
.status-delivered { background-color: #eafcf1; color: #00b34a; }
.status-cancelled { background-color: #fde2e2; color: #f56c6c; }

/* ================= CỘT PHẢI ================= */
.detail-box {
  background-color: #ffffff;
  border: 1px solid #eeeeee;
  padding: 40px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eeeeee;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.detail-header h2 {
  font-size: 18px;
  font-weight: 400;
  letter-spacing: 1px;
  color: #111111;
}

.detail-header h2 span {
  font-weight: 600;
  color: #d1aa68;
}

.btn-print {
  background: none;
  border: 1px solid #eeeeee;
  padding: 8px 15px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-print:hover {
  background-color: #f5f5f5;
  border-color: #cccccc;
}

/* Timeline */
.tracking-timeline {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
  position: relative;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  z-index: 2;
  width: 80px;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #ffffff;
  border: 2px solid #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #cccccc;
  transition: all 0.3s ease;
}

.step p {
  font-size: 11px;
  color: #888888;
  font-weight: 600;
  text-transform: uppercase;
  text-align: center;
}

.step.completed .step-icon {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #ffffff;
}

.step.active .step-icon {
  border-color: #d1aa68;
  color: #d1aa68;
  box-shadow: 0 0 0 4px rgba(209, 170, 104, 0.1);
}

.step.completed p, .step.active p { color: #111111; }

.step-line {
  flex: 1;
  height: 2px;
  background-color: #eeeeee;
  margin-top: 20px;
  z-index: 1;
  transition: background-color 0.3s ease;
}

.step-line.filled { background-color: #d1aa68; }

/* Grid Info Map */
.info-map-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 40px;
}

.info-panel {
  background-color: #fcfcfc;
  padding: 25px;
  border: 1px solid #eeeeee;
}

.info-panel h3 {
  font-size: 12px;
  color: #555555;
  letter-spacing: 2px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eeeeee;
  padding-bottom: 10px;
}

.info-panel p {
  font-size: 13px;
  color: #333333;
  margin-bottom: 10px;
  line-height: 1.6;
}

.map-panel {
  height: 100%;
  min-height: 200px;
  background-color: #e0e0e0;
  position: relative;
  overflow: hidden;
  border: 1px solid #eeeeee;
}

.luxury-map {
  filter: grayscale(100%) contrast(1.2) opacity(0.8);
}

.map-overlay {
  position: absolute;
  top: 15px;
  left: 15px;
  background-color: #24201D;
  color: #ffffff;
  padding: 6px 12px;
  font-size: 10px;
  font-weight: bold;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-transform: uppercase;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background-color: #d1aa68;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(209, 170, 104, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(209, 170, 104, 0); }
  100% { box-shadow: 0 0 0 0 rgba(209, 170, 104, 0); }
}

/* Bảng sản phẩm */
.order-items h3 {
  font-size: 12px;
  color: #555555;
  letter-spacing: 2px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eeeeee;
  padding-bottom: 10px;
}

.item-table { width: 100%; border-collapse: collapse; }
.item-table td { padding: 15px 0; border-bottom: 1px solid #f5f5f5; vertical-align: middle; }

.td-img img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  background-color: #fcfcfc;
  border: 1px solid #eeeeee;
}
.td-name { padding-left: 20px; }
.p-name { display: block; font-size: 13px; font-weight: 500; color: #111111; margin-bottom: 5px; }
.p-qty { font-size: 11px; color: #888888; }
.td-price { text-align: right; font-size: 14px; font-weight: 600; color: #d1aa68; }

.no-selection {
  background-color: #ffffff;
  border: 1px solid #eeeeee;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #cccccc;
}
.no-selection i { font-size: 50px; margin-bottom: 20px; }
.no-selection p { font-size: 14px; color: #888888; letter-spacing: 1px; }

/* Trạng thái trống */
.empty-orders {
  text-align: center;
  padding: 100px 0;
}
.empty-icon { font-size: 60px; color: #dddddd; margin-bottom: 25px; }
.empty-orders p { font-size: 15px; color: #666666; margin-bottom: 30px; letter-spacing: 0.5px; }
.btn-shopnow {
  display: inline-block;
  background-color: #24201D;
  color: #ffffff;
  border: 1px solid #24201D;
  padding: 14px 35px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  text-decoration: none;
  transition: all 0.3s ease;
}
.btn-shopnow:hover { background-color: #d1aa68; border-color: #d1aa68; color: #24201D; }

@media (max-width: 1024px) { .order-layout { grid-template-columns: 1fr; } }
@media (max-width: 768px) {
  .info-map-grid { grid-template-columns: 1fr; }
  .tracking-timeline { flex-wrap: wrap; gap: 20px; }
  .step-line { display: none; }
}
</style>