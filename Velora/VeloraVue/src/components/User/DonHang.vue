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
            <button v-for="tab in tabs" :key="tab.value" @click="currentTab = tab.value"
              :class="['tab-btn', { 'active': currentTab === tab.value }]">
              {{ tab.label }}
              <span class="tab-count" v-if="getCountByTab(tab.value) > 0">
                ({{ getCountByTab(tab.value) }})
              </span>
            </button>
          </div>
        </div>

        <div class="order-layout" v-if="filteredOrders.length > 0">
          <!-- CỘT TRÁI: DANH SÁCH ĐƠN HÀNG CÓ PHÂN TRANG -->
          <div class="order-list-section">
            <h2 class="section-title">LỊCH SỬ GIAO DỊCH</h2>

            <div class="order-list">
              <!-- Render mảng paginatedOrders thay vì filteredOrders -->
              <div v-for="order in paginatedOrders" :key="order.maDonHang" class="order-card"
                :class="{ 'active': selectedOrder && selectedOrder.maDonHang === order.maDonHang }"
                @click="selectOrder(order)">
                <div class="order-card-header">
                  <span class="order-code">#{{ order.maDonHangCode }}</span>
                  <span class="order-date">{{ order.ngayTao }}</span>
                </div>

                <div class="order-card-body">
                  <!-- ĐÃ SỬA: Hiển thị toàn bộ sản phẩm trong thẻ đơn hàng -->
                  <div class="order-card-footer" style="margin-top: 15px;">
                    <div class="order-total">{{ formatPrice(order.tongTien) }}</div>
                    <div class="order-status" :class="getStatusClass(order.trangThaiDonHang)">
                      {{ getStatusText(order.trangThaiDonHang) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- COMPONENT PHÂN TRANG -->
            <div class="pagination-wrapper" v-if="totalPages > 1">
              <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
                <i class="fas fa-chevron-left"></i>
              </button>

              <button v-for="page in totalPages" :key="page" class="page-btn" :class="{ active: currentPage === page }"
                @click="currentPage = page">
                {{ page }}
              </button>

              <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>

          <!-- CỘT PHẢI: CHI TIẾT ĐƠN HÀNG -->
          <div class="order-detail-section">
            <div v-if="selectedOrder" class="detail-box">

              <div class="detail-header">
                <h2>CHI TIẾT ĐƠN HÀNG <span>#{{ selectedOrder.maDonHangCode }}</span></h2>
                <div class="detail-actions">

                  <div v-if="selectedOrder.trangThaiDonHang === 'YEU_CAU_HUY'" class="pending-cancel-badge">
                    <i class="fas fa-clock"></i> Đang chờ duyệt hủy
                  </div>

                  <button v-if="selectedOrder.trangThaiDonHang === 'CHO_XU_LY'" @click="cancelOrder(selectedOrder)"
                    class="btn-cancel-order">
                    <i class="fas fa-ban"></i> HỦY ĐƠN
                  </button>

                  <button
                    v-if="selectedOrder.trangThaiDonHang === 'DA_GIAO' || (selectedOrder.trangThaiDonHang === 'DA_HUY' && isOnlinePayment(selectedOrder.phuongThucThanhToan))"
                    @click="goToRefundPage(selectedOrder)" class="btn-refund-order">
                    <i class="fas fa-undo-alt"></i> HOÀN TIỀN
                  </button>

                  <button 
                    v-if="selectedOrder.trangThaiDonHang === 'DA_GIAO'" 
                    @click="goToWarrantyPage(selectedOrder)" 
                    class="btn-warranty-order">
                    <i class="fas fa-shield-alt"></i> YÊU CẦU BẢO HÀNH
                  </button>

                  <button class="btn-print" @click="printInvoice"><i class="fas fa-print"></i> IN BIÊN LAI</button>
                </div>
              </div>

              <div class="tracking-timeline" v-if="!['DA_HUY', 'YEU_CAU_HUY'].includes(selectedOrder.trangThaiDonHang)">
                <div class="step"
                  :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 1), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 1 }">
                  <div class="step-icon"><i class="fas fa-file-invoice-dollar"></i></div>
                  <p>Chờ xử lý</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 1 }"></div>
                <div class="step"
                  :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 2), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 2 }">
                  <div class="step-icon"><i class="fas fa-box-open"></i></div>
                  <p>Chuẩn bị</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 2 }"></div>
                <div class="step"
                  :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 3), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 3 }">
                  <div class="step-icon"><i class="fas fa-truck-fast"></i></div>
                  <p>Đang giao</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 3 }"></div>
                <div class="step"
                  :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 4), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 4 }">
                  <div class="step-icon"><i class="fas fa-check"></i></div>
                  <p>Đã giao</p>
                </div>
              </div>

              <div v-if="['DA_HUY', 'YEU_CAU_HUY', 'TU_CHOI_HOAN_TIEN'].includes(selectedOrder.trangThaiDonHang)"
                class="cancel-info-box">
                <h3 style="font-size: 14px; font-weight: bold; margin-bottom: 8px;"><i
                    class="fas fa-exclamation-triangle"></i> THÔNG TIN TRẠNG THÁI</h3>
                <p style="font-size: 14px; margin-bottom: 5px;"><strong>Tình trạng:</strong> {{
                  getStatusText(selectedOrder.trangThaiDonHang) }}</p>
                <p v-if="selectedOrder.lyDoHuy" style="font-size: 14px; color: #991b1b;">
                  <strong>Lý do:</strong> {{ selectedOrder.lyDoHuy }}
                </p>
              </div>

              <div class="info-map-grid">
                <div class="info-panel">
                  <h3>THÔNG TIN NHẬN HÀNG</h3>
                  <p><strong>Người nhận:</strong> {{ selectedOrder.tenNguoiNhan }}</p>
                  <p><strong>Số điện thoại:</strong> {{ selectedOrder.soDienThoai }}</p>
                  <p><strong>Địa chỉ:</strong> {{ selectedOrder.diaChi }}</p>
                  <p><strong>Thanh toán:</strong>
                    <span :class="isOnlinePayment(selectedOrder.phuongThucThanhToan) ? 'text-[#c5a880] font-bold' : ''">
                      {{ selectedOrder.phuongThucThanhToan }}
                    </span>
                  </p>
                </div>

                <div class="map-panel">
                  <iframe
                    src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.325316278854!2d106.69499931474895!3d10.7863769923145!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f3458d926fb%3A0xc68297cbbf9f1a23!2sBitexco%20Financial%20Tower!5e0!3m2!1sen!2s!4v1623145214002!5m2!1sen!2s"
                    width="100%" height="100%" style="border:0;" allowfullscreen="" loading="lazy" class="luxury-map">
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

    <!-- CUSTOM LUXURY MODAL -->
    <div class="velora-modal-overlay" v-if="showModal">
      <div class="velora-modal-box">
        <h3 class="velora-modal-title">{{ modalTitle }}</h3>
        <p class="velora-modal-msg">{{ modalMessage }}</p>

        <div v-if="modalType === 'cancel'" style="margin-bottom: 20px;">
          <textarea v-model="cancelReasonInput" placeholder="Vui lòng nhập lý do hủy đơn hàng (Bắt buộc)..."
            class="velora-textarea"></textarea>
        </div>

        <div class="velora-modal-actions">
          <button v-if="modalType === 'confirm' || modalType === 'cancel'" @click="handleModalClose"
            class="velora-btn-secondary">Hủy</button>
          <button @click="handleModalConfirm" class="velora-btn-primary">
            {{ modalType === 'confirm' || modalType === 'cancel' ? 'Xác Nhận' : 'Đồng Ý' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

// 🔥 IMPORT THƯ VIỆN WEBSOCKET
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const router = useRouter()
const orders = ref([])
const selectedOrder = ref(null)

const currentTab = ref('ALL')
const currentPage = ref(1)
const itemsPerPage = 4

const tabs = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Chờ xử lý', value: 'CHO_XU_LY' },
  { label: 'Chuẩn bị hàng', value: 'CHUAN_BI_HANG' },
  { label: 'Đang vận chuyển', value: 'DANG_GIAO' },
  { label: 'Đã giao', value: 'DA_GIAO' },
  { label: 'Yêu cầu hủy', value: 'YEU_CAU_HUY' },
  { label: 'Đã hủy', value: 'DA_HUY' },
  { label: 'Hoàn tiền', value: 'HOAN_TIEN' }
]

const filteredOrders = computed(() => {
  if (currentTab.value === 'ALL') return orders.value
  if (currentTab.value === 'HOAN_TIEN') {
    return orders.value.filter(o => ['YEU_CAU_HOAN_TIEN', 'TU_CHOI_HOAN_TIEN', 'DA_DUYET_HOAN_TIEN', 'HOAN_TIEN'].includes(o.trangThaiDonHang))
  }
  return orders.value.filter(order => order.trangThaiDonHang === currentTab.value)
})

const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / itemsPerPage);
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredOrders.value.slice(start, start + itemsPerPage);
})

const getCountByTab = (tabValue) => {
  if (tabValue === 'ALL') return orders.value.length
  if (tabValue === 'HOAN_TIEN') {
    return orders.value.filter(o => ['YEU_CAU_HOAN_TIEN', 'TU_CHOI_HOAN_TIEN', 'DA_DUYET_HOAN_TIEN', 'HOAN_TIEN'].includes(o.trangThaiDonHang)).length
  }
  return orders.value.filter(order => order.trangThaiDonHang === tabValue).length
}

watch(currentTab, () => {
  currentPage.value = 1;
  if (paginatedOrders.value.length > 0) {
    selectedOrder.value = paginatedOrders.value[0];
  } else {
    selectedOrder.value = null;
  }
})

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
  toastTimer = setTimeout(() => { showToast.value = false }, 3500)
}

const isOnlinePayment = (method) => {
  if (!method) return false;
  const m = method.toUpperCase();
  return m !== 'COD' && m !== 'THANH TOÁN COD';
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
      
      // ĐÃ XÓA CHECK 48 GIỜ - TẤT CẢ ĐƠN ĐỀU ĐƯỢC LOAD
      orders.value = data.map(order => ({
        maDonHang: order.maDonHang,
        maDonHangCode: order.maDonHangCode,
        ngayTaoRaw: order.ngayTao, 
        ngayTao: new Date(order.ngayTao).toLocaleDateString('vi-VN'),
        tongTien: order.tongTien,
        tenNguoiNhan: order.tenNguoiNhan,
        soDienThoai: order.soDienThoaiGiaoHang,
        email: order.email || user.email || '', 
        diaChi: order.diaChiGiaoHang,
        trangThaiDonHang: order.trangThaiDonHang,
        phuongThucThanhToan: order.phuongThucThanhToan,
        lyDoHuy: order.lyDoHuy || order.ghiChu || '', 
        items: (order.chiTietDonHangs || []).map(ct => ({
          ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Kiệt tác thời gian',
          maSanPham: ct.sanPham ? ct.sanPham.maSanPham : null,
          soLuong: ct.soLuong,
          gia: ct.giaLucMua,
          anh: ct.sanPham ? ct.sanPham.anhDaiDien : ''
        }))
      }))

      if (orders.value.length > 0) {
        if (selectedOrder.value) {
          const updatedSelected = orders.value.find(o => o.maDonHang === selectedOrder.value.maDonHang);
          selectedOrder.value = updatedSelected || paginatedOrders.value[0];
        } else {
          selectedOrder.value = paginatedOrders.value[0];
        }
      }
    }
  } catch (error) {
    console.error('Lỗi kết nối API đơn hàng:', error)
  }
}

// 🔥 HÀM ĐẨY THÔNG TIN SANG TRANG BẢO HÀNH
const goToWarrantyPage = (order) => {
  localStorage.setItem('selectedWarrantyOrder', JSON.stringify(order))
  router.push('/bao-hanh')
}

const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws-chat');
  const stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/orders', (message) => {
        if (message.body === 'RELOAD_ORDERS') {
          fetchUserOrders(); 
        }
      });
    }
  });
  stompClient.activate();
};

const selectOrder = (order) => {
  selectedOrder.value = order
}

const cancelOrder = async (order) => {
  if (order.trangThaiDonHang !== 'CHO_XU_LY') {
    await triggerModal('Đơn hàng đã được duyệt, không thể hủy. Vui lòng liên hệ Hotline!', 'THÔNG BÁO')
    return
  }

  const reason = await triggerModal('Vui lòng cho biết lý do bạn muốn hủy đơn hàng này:', 'XÁC NHẬN YÊU CẦU HỦY', 'cancel')
  if (!reason) return

  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${order.maDonHang}/trang-thai?trangThaiMoi=YEU_CAU_HUY&lyDo=${encodeURIComponent(reason)}`, {
      method: 'PATCH'
    })

    if (res.ok) {
      order.trangThaiDonHang = 'YEU_CAU_HUY'
      triggerToast('Đã gửi yêu cầu hủy đơn thành công! Vui lòng chờ bộ phận CSKH xác nhận.')
      fetchUserOrders();
    } else {
      const errText = await res.text()
      await triggerModal(errText || 'Không thể gửi yêu cầu hủy lúc này!', 'THÔNG BÁO')
    }
  } catch (error) {
    await triggerModal('Lỗi kết nối đến máy chủ!', 'LỖI')
  }
}

const goToRefundPage = (order) => {
  localStorage.setItem('selectedRefundOrder', JSON.stringify(order))
  router.push('/yeu-cau-hoan-tien')
}

const printInvoice = () => {
  window.print();
}

const getStepLevel = (status) => {
  switch (status) {
    case 'CHO_XU_LY': return 1;
    case 'CHUAN_BI_HANG': return 2;
    case 'DANG_GIAO': return 3;
    case 'DA_GIAO': return 4;
    default: return 1;
  }
}

const isStepCompleted = (status, stepNumber) => { return getStepLevel(status) > stepNumber; }

const getStatusText = (status) => {
  if (!status) return ''
  const cleanStatus = status.trim().toUpperCase()
  const map = {
    'CHO_XU_LY': 'Đang chờ xử lý',
    'YEU_CAU_HUY': 'Đang chờ duyệt hủy',
    'CHUAN_BI_HANG': 'Đang chuẩn bị hàng',
    'DANG_GIAO': 'Đang vận chuyển',
    'DA_GIAO': 'Đã giao thành công',
    'DA_HUY': 'Đơn hàng đã hủy',
    'YEU_CAU_HOAN_TIEN': 'Yêu cầu hoàn tiền đã gửi',
    'TU_CHOI_HOAN_TIEN': 'Từ chối hoàn tiền',
    'DA_DUYET_HOAN_TIEN': 'Đã duyệt hoàn tiền',
    'HOAN_TIEN': 'Đang xử lý hoàn tiền'
  }
  return map[cleanStatus] || status
}

const getStatusClass = (status) => {
  if (!status) return ''
  const cleanStatus = status.trim().toUpperCase()
  const map = {
    'CHO_XU_LY': 'status-pending',
    'YEU_CAU_HUY': 'status-cancel-request',
    'CHUAN_BI_HANG': 'status-prep',
    'DANG_GIAO': 'status-shipping',
    'DA_GIAO': 'status-delivered',
    'DA_HUY': 'status-cancelled',
    'YEU_CAU_HOAN_TIEN': 'status-refund-sent',
    'TU_CHOI_HOAN_TIEN': 'status-refund-rejected',
    'DA_DUYET_HOAN_TIEN': 'status-refund-approved',
    'HOAN_TIEN': 'status-refund-sent'
  }
  return map[cleanStatus] || ''
}

onMounted(() => {
  fetchUserOrders();
  connectWebSocket();
})
</script>

<style scoped>
@import "../CSS/User/DonHang.css";

.container {
  max-width: 1450px !important;
}

/* CSS PHÂN TRANG */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 25px;
  padding-bottom: 10px;
}

.page-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  color: #4b5563;
  font-weight: 600;
  transition: all 0.2s ease;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  border-color: #c5a880;
  color: #c5a880;
}

.page-btn.active {
  background: #c5a880;
  color: white;
  border-color: #c5a880;
}

.page-btn:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #f3f4f6;
}

/* BỔ SUNG: CSS CHO BẢNG SẢN PHẨM CHI TIẾT (Cột Phải) */
.order-items {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #e5e7eb;
}

.item-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.item-table td {
  padding: 15px 0;
  border-bottom: 1px solid #f3f4f6;
}

.item-table tr:last-child td {
  border-bottom: none;
}

.td-img img {
  width: 65px;
  height: 65px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.td-name {
  padding-left: 15px;
  vertical-align: middle;
}

.td-name .p-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  display: block;
  margin-bottom: 4px;
}

.td-name .p-qty {
  font-size: 13px;
  color: #6b7280;
}

.td-price {
  text-align: right;
  font-size: 15px;
  font-weight: 700;
  color: #c5a880;
  vertical-align: middle;
}

/* Các class UI khác */
.cancel-info-box {
  background: #fff9e6;
  border: 1px solid #fde68a;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  color: #b45309;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending-cancel-badge {
  background-color: #fffbeb;
  color: #b45309;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #fde68a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-cancel-request {
  background-color: #fef3c7 !important;
  color: #b45309 !important;
  border: 1px solid #fde68a;
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
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  background: #ffffff;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.order-card:hover {
  border-color: #e5e7eb;
  transform: translateY(-2px);
}

.order-card.active {
  border-color: #c5a880;
  box-shadow: 0 4px 12px rgba(197, 168, 128, 0.15);
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.order-code {
  font-size: 15px;
  font-weight: 700;
  color: #362921;
}

.order-date {
  font-size: 13px;
  color: #888;
}

.order-total {
  font-size: 16px;
  font-weight: 700;
  color: #c5a880;
}

.order-status {
  font-size: 11px;
  padding: 6px 10px;
  font-weight: 700;
  border-radius: 4px;
  text-transform: uppercase;
}

.detail-box {
  padding: 45px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.detail-header h2 {
  font-size: 21px;
  color: #362921;
}

.detail-header h2 span {
  color: #c5a880;
}

.detail-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-print,
.btn-cancel-order {
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 4px;
}

.btn-print {
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-print:hover {
  background-color: #e5e7eb;
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

.btn-refund-order {
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 4px;
  background-color: #fef3c7;
  border: 1px solid #fde68a;
  color: #b45309;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-refund-order:hover {
  background-color: #d97706;
  color: #ffffff;
  border-color: #d97706;
}

.btn-warranty-order { 
  padding: 10px 18px; 
  font-size: 13px; 
  font-weight: 700; 
  border-radius: 4px; 
  background-color: #f0fdf4; 
  border: 1px solid #bbf7d0; 
  color: #166534; 
  cursor: pointer; 
  transition: all 0.2s; 
  display: flex; 
  align-items: center; 
  gap: 6px; 
}

.btn-warranty-order:hover { 
  background-color: #22c55e; 
  color: #ffffff; 
  border-color: #22c55e; 
}

.status-refund-sent {
  background-color: #fef3c7 !important;
  color: #b45309 !important;
}

.status-refund-rejected {
  background-color: #fee2e2 !important;
  color: #b91c1c !important;
}

.status-refund-approved {
  background-color: #dcfce7 !important;
  color: #15803d !important;
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

.info-panel h3,
.order-items h3 {
  font-size: 14px;
  font-weight: 700;
}

.info-panel p {
  font-size: 15px;
}

.velora-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.velora-modal-box {
  width: 100%;
  max-width: 450px;
  background: #141414;
  border: 1px solid rgba(197, 168, 128, 0.4);
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
}

.velora-modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #c5a880;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.velora-modal-msg {
  color: #d1d5db;
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.5;
}

.velora-textarea {
  width: 100%;
  height: 100px;
  background: #1e1e1e;
  border: 1px solid #374151;
  border-radius: 8px;
  padding: 12px;
  color: white;
  font-size: 14px;
  resize: none;
  font-family: inherit;
}

.velora-textarea:focus {
  border-color: #c5a880;
  outline: none;
}

.velora-modal-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.velora-btn-secondary {
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  background: transparent;
  color: #d1d5db;
  font-weight: 500;
  cursor: pointer;
  transition: 0.2s;
}

.velora-btn-secondary:hover {
  background: #1f2937;
}

.velora-btn-primary {
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  background: #c5a880;
  color: #141414;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.velora-btn-primary:hover {
  background: #b0936d;
}
</style>