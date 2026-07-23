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

        <!-- THANH TAB LỌC TRẠNG THÁI -->
        <div class="order-tabs-wrapper" v-if="orders.length > 0">
          <div class="order-tabs">
            <button 
              v-for="tab in tabs" 
              :key="tab.value"
              @click="currentTab = tab.value"
              :class="['tab-btn', { 'active': currentTab === tab.value }]"
            >
              {{ tab.label }}
              <span class="tab-count" v-if="getCountByTab(tab.value) > 0">
                ({{ getCountByTab(tab.value) }})
              </span>
            </button>
          </div>
        </div>

        <div class="order-layout" v-if="filteredOrders.length > 0">
          <div class="order-list-section">
            <h2 class="section-title">LỊCH SỬ GIAO DỊCH</h2>
            
            <div class="order-list">
              <div 
                v-for="order in filteredOrders" 
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
                <div class="detail-actions">
                  <button 
                    v-if="selectedOrder.trangThaiDonHang === 'CHO_XU_LY'" 
                    @click="cancelOrder(selectedOrder)" 
                    class="btn-cancel-order"
                  >
                    <i class="fas fa-ban"></i> HỦY ĐƠN
                  </button>
                  <button class="btn-print" @click="window.print()"><i class="fas fa-print"></i> IN BIÊN LAI</button>
                </div>
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

        <div v-else-if="orders.length > 0 && filteredOrders.length === 0" class="empty-orders">
          <i class="fas fa-box-open empty-icon"></i>
          <p>Không có đơn hàng nào ở trạng thái này.</p>
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

    <div class="stock-toast" :class="{ 'show': showToast }">
      <i class="fas fa-info-circle"></i> {{ toastMessage }}
    </div>

    <!-- CUSTOM LUXURY MODAL (HỖ TRỢ CẢ KHUNG NHẬP LÝ DO HỦY) -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
      <div class="w-full max-w-md p-8 bg-[#141414] border border-[#c5a880]/40 rounded-xl shadow-2xl text-center">
        <h3 class="text-xl font-semibold text-[#c5a880] mb-3 tracking-wider">{{ modalTitle }}</h3>
        <p class="text-gray-300 mb-6 leading-relaxed">{{ modalMessage }}</p>
        
        <!-- Khung nhập lý do hủy đơn -->
        <div v-if="modalType === 'cancel'" class="mb-6">
          <textarea 
            v-model="cancelReasonInput" 
            placeholder="Vui lòng nhập lý do hủy đơn hàng..." 
            class="w-full h-28 p-3 bg-[#1e1e1e] border border-gray-700 rounded-lg text-white text-sm focus:border-[#c5a880] focus:outline-none resize-none"
          ></textarea>
        </div>

        <div class="flex justify-center gap-4">
          <button 
            v-if="modalType === 'confirm' || modalType === 'cancel'"
            @click="handleModalClose" 
            class="px-5 py-2.5 rounded-lg border border-gray-600 text-gray-300 hover:bg-gray-800 transition font-medium"
          >
            Hủy
          </button>
          <button 
            @click="handleModalConfirm" 
            class="px-6 py-2.5 rounded-lg bg-[#c5a880] text-black font-semibold hover:bg-[#b0936d] transition shadow-lg"
          >
            {{ modalType === 'confirm' || modalType === 'cancel' ? 'Xác Nhận' : 'Đồng Ý' }}
          </button>
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
const orders = ref([])
const selectedOrder = ref(null)

// ================= BIẾN QUẢN LÝ CUSTOM MODAL =================
const showModal = ref(false)
const modalTitle = ref('THÔNG BÁO')
const modalMessage = ref('')
const modalType = ref('alert')
const cancelReasonInput = ref('')
let modalResolveFn = null

const triggerModal = (message, title = 'THÔNG BÁO', type = 'alert') => {
  modalMessage.value = message
  modalTitle.value = title
  modalType.value = type
  cancelReasonInput.value = ''
  showModal.value = true
  return new Promise((resolve) => {
    modalResolveFn = resolve
  })
}

const handleModalConfirm = () => {
  if (modalType.value === 'cancel') {
    if (!cancelReasonInput.value.trim()) {
      triggerToast('Vui lòng nhập lý do hủy đơn hàng!')
      return
    }
    showModal.value = false
    if (modalResolveFn) modalResolveFn(cancelReasonInput.value)
  } else {
    showModal.value = false
    if (modalResolveFn) modalResolveFn(true)
  }
}

const handleModalClose = () => {
  showModal.value = false
  if (modalResolveFn) modalResolveFn(false)
}

const showToast = ref(false)
const toastMessage = ref('')
let toastTimer = null

const triggerToast = (msg) => {
  toastMessage.value = msg
  showToast.value = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    showToast.value = false
  }, 2000)
}

const currentTab = ref('ALL')
const tabs = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Chờ xử lý', value: 'CHO_XU_LY' },
  { label: 'Chuẩn bị hàng', value: 'DANG_CHUAN_BI' },
  { label: 'Đang vận chuyển', value: 'DANG_GIAO' },
  { label: 'Đã giao', value: 'DA_GIAO' },
  { label: 'Đã hủy', value: 'DA_HUY' },
  { label: 'Hoàn tiền', value: 'HOAN_TIEN' }
]

const filteredOrders = computed(() => {
  if (currentTab.value === 'ALL') {
    return orders.value
  }
  return orders.value.filter(order => order.trangThaiDonHang === currentTab.value)
})

const getCountByTab = (tabValue) => {
  if (tabValue === 'ALL') return orders.value.length
  return orders.value.filter(order => order.trangThaiDonHang === tabValue).length
}

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const fetchUserOrders = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      await triggerModal('Vui lòng đăng nhập để xem lịch sử đơn hàng!', 'YÊU CẦU ĐĂNG NHẬP')
      router.push('/dang-nhap')
      return
    }
    
    const user = JSON.parse(userStr)
    const response = await fetch(`http://localhost:8080/api/don-hang/nguoi-dung/${user.maNguoiDung || user.id}`)
    
    if (response.ok) {
      const data = await response.json()
      
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
        items: (order.chiTietDonHangs || []).map(ct => ({
          ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Kiệt tác thời gian',
          soLuong: ct.soLuong,
          gia: ct.giaLucMua,
          anh: ct.sanPham ? ct.sanPham.anhDaiDien : ''
        }))
      }))

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

const cancelOrder = async (order) => {
  const reason = await triggerModal('Vui lòng cho biết lý do ku em muốn hủy đơn hàng này:', 'XÁC NHẬN HỦY ĐƠN', 'cancel')
  if (!reason) return // Bấm hủy hoặc tắt popup

  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${order.maDonHang}/huy?lyDo=${encodeURIComponent(reason)}`, {
      method: 'PATCH'
    })
    if (res.ok) {
      order.trangThaiDonHang = 'DA_HUY'
      triggerToast('Đã hủy đơn hàng thành công!')
    } else {
      const errText = await res.text()
      await triggerModal(errText || 'Không thể hủy đơn hàng này!', 'THÔNG BÁO')
    }
  } catch (error) {
    console.error('Lỗi kết nối khi hủy đơn:', error)
    await triggerModal('Lỗi kết nối đến máy chủ!', 'LỖI')
  }
}

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
    'DA_HUY': 'Đơn hàng đã hủy',
    'HOAN_TIEN': 'Đang hoàn tiền'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    'CHO_XU_LY': 'status-pending',
    'DANG_CHUAN_BI': 'status-prep',
    'DANG_GIAO': 'status-shipping',
    'DA_GIAO': 'status-delivered',
    'DA_HUY': 'status-cancelled',
    'HOAN_TIEN': 'status-refund'
  }
  return map[status] || ''
}

onMounted(() => {
  fetchUserOrders()
})
</script>

<style scoped>
@import "../CSS/User/DonHang.css";

.container {
  max-width: 1450px !important;
}

.order-tabs-wrapper {
  margin-bottom: 35px;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 4px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.order-tabs {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  white-space: nowrap;
}

.tab-btn {
  background: none;
  border: none;
  padding: 20px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #4b5563;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

.tab-btn:hover {
  color: #c5a880;
}

.tab-btn.active {
  color: #c5a880;
  font-weight: 700;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #c5a880;
  border-radius: 3px 3px 0 0;
}

.tab-count {
  font-size: 13px;
  background-color: #f3f4f6;
  padding: 3px 8px;
  border-radius: 12px;
  margin-left: 6px;
  color: #4b5563;
  font-weight: 600;
}

.tab-btn.active .tab-count {
  background-color: rgba(197, 168, 128, 0.15);
  color: #c5a880;
}

.order-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 35px;
  align-items: start;
}

.section-title {
  font-size: 14px;
  color: #374151;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 20px;
}

.order-card {
  padding: 24px;
  border-radius: 6px;
}

.order-code {
  font-size: 15px;
  font-weight: 700;
}

.order-date {
  font-size: 13px;
}

.order-total {
  font-size: 16px;
  font-weight: 700;
}

.order-status {
  font-size: 12px;
  padding: 6px 12px;
  font-weight: 700;
}

.detail-box {
  padding: 45px;
  border-radius: 8px;
}

.detail-header h2 {
  font-size: 21px;
}

.detail-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-print, .btn-cancel-order {
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 4px;
}

.btn-cancel-order {
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-cancel-order:hover {
  background-color: #f87171;
  color: #ffffff;
  border-color: #f87171;
}

.step-icon {
  width: 50px;
  height: 50px;
  font-size: 18px;
}

.step p {
  font-size: 13px;
  font-weight: 700;
}

.info-panel {
  padding: 30px;
}

.info-panel h3, .order-items h3 {
  font-size: 14px;
  font-weight: 700;
}

.info-panel p {
  font-size: 15px;
}

.p-name {
  font-size: 15px;
  font-weight: 600;
}

.p-qty {
  font-size: 13px;
}

.td-price {
  font-size: 16px;
  font-weight: 700;
}

.status-refund { 
  background-color: #f3e8ff; 
  color: #7c3aed; 
}
</style>