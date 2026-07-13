<template>
  <div class="order-page">
    <Header />

    <main class="order-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">ĐƠN HÀNG CỦA BẠN</h1>
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
@import "../CSS/User/DonHang.css";
</style>