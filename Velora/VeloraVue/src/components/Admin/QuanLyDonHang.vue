<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- COMPONENT SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- COMPONENT HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- NỘI DUNG CHÍNH -->
      <main class="content">
        <header class="header">
          <div class="header-left">
            <h1>Quản Lý <span class="gold">Đơn Hàng</span></h1>
            <p>Theo dõi, luân chuyển trạng thái và xử lý các giao dịch giao hàng.</p>
          </div>
        </header>

        <section class="filter-wrapper">
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Tìm theo mã đơn, số điện thoại, tên khách hàng..." 
              class="input-search"
            />
          </div>
          <div class="date-box">
            <label for="filterDate" class="label-date">Lọc ngày tạo:</label>
            <input 
              type="date" 
              id="filterDate" 
              v-model="filterDate" 
              class="input-date"
            />
            <button v-if="filterDate" class="btn-clear-date" title="Xóa lọc" @click="filterDate = ''">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </section>

        <section class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Mã Đơn</th>
                <th>Khách Hàng</th>
                <th>Ngày Đặt</th>
                <th>Tổng Tiền</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái</th>
                <th style="width: 170px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredOrders" :key="order.maDonHang">
                <td class="order-code">{{ order.maDonHangCode }}</td>
                <td class="customer-info">
                  <strong>{{ order.tenNguoiNhan }}</strong>
                  <span class="phone">{{ order.soDienThoaiGiaoHang }}</span>
                </td>
                <td>{{ formatDate(order.ngayTao) }}</td>
                <td class="price">{{ formatPrice(order.tongTien) }}</td>
                <td>
                  <div class="payment-edit-group">
                    <select 
                      v-model="order.phuongThucThanhToan" 
                      @change="changePaymentMethod(order.maDonHang, order.phuongThucThanhToan)"
                      class="payment-select"
                    >
                      <option v-for="method in paymentMethods" :key="method" :value="method">
                        {{ getPaymentMethodLabel(method) }}
                      </option>
                    </select>
                    <!-- Nút cộng thêm phương thức thanh toán -->
                    <button class="btn-add-payment-method" title="Tạo thêm hình thức mới" @click="openAddPaymentModal">+</button>
                  </div>
                  <div style="margin-top: 6px;">
                    <span class="payment-status" :class="isPaid(order.trangThaiThanhToan) ? 'paid' : 'unpaid'">
                      {{ isPaid(order.trangThaiThanhToan) ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                    </span>
                  </div>
                </td>
                <td>
                  <!-- GỌI ĐỘNG LABEL VÀ CLASS TỪ OBJECT CẤU HÌNH -->
                  <span class="status-badge" :class="getStatusClass(order.trangThaiDonHang)">
                    {{ getStatusLabel(order.trangThaiDonHang) }}
                  </span>
                </td>
                <td>
                  <!-- DANH SÁCH LỆNH ĐƯỢC RENDER ĐỘNG DỰA THEO TRẠNG THÁI -->
                  <select 
                    class="action-select" 
                    @change="handleActionSelect(order, $event)"
                  >
                    <option value="" disabled selected>-- Chọn lệnh --</option>
                    <option 
                      v-for="action in getAvailableActions(order.trangThaiDonHang)" 
                      :key="action.value" 
                      :value="action.value"
                      :class="{ 'option-danger': action.isDanger }"
                    >
                      {{ action.label }}
                    </option>
                  </select>
                </td>
              </tr>
              <tr v-if="filteredOrders.length === 0">
                <td colspan="7" class="empty-state">Không tìm thấy dữ liệu phù hợp.</td>
              </tr>
            </tbody>
          </table>
        </section>
      </main>

      <!-- MODAL XEM CHI TIẾT (ĐÃ BỔ SUNG KHUNG HIỂN THỊ LÝ DO HỦY) -->
      <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
        <div class="modal-box modal-lg">
          <div class="modal-header">
            <h2>Chi Tiết Đơn Hàng <span class="gold">#{{ selectedOrder?.maDonHangCode }}</span></h2>
            <button class="btn-close" @click="closeDetailModal">&#10005;</button>
          </div>
          <div class="modal-body">
            
            <!-- HIỂN THỊ LÝ DO HỦY ĐƠN NẾU ĐƠN ĐÃ BỊ HỦY -->
            <div v-if="selectedOrder?.trangThaiDonHang === 'DA_HUY'" class="cancel-reason-box" style="background: #fdf2f2; border: 1px solid #f8d7da; padding: 15px; border-radius: 6px; margin-bottom: 20px;">
              <h4 style="color: #dc3545; margin: 0 0 5px 0;"><i class="fa-solid fa-triangle-exclamation"></i> Thông Tin Hủy Đơn</h4>
              <p style="margin: 0; color: #721c24; font-size: 14px;">
                <strong>Lý do hủy từ khách hàng:</strong> {{ selectedOrder?.lyDoHuyDon || 'Không có lý do cụ thể' }}
              </p>
            </div>

            <div class="order-info-grid">
              <div class="info-card">
                <h4>Thông Tin Giao Hàng</h4>
                <p><strong>Người nhận:</strong> {{ selectedOrder?.tenNguoiNhan }}</p>
                <p><strong>Điện thoại:</strong> {{ selectedOrder?.soDienThoaiGiaoHang }}</p>
                <p><strong>Địa chỉ:</strong> {{ selectedOrder?.diaChiGiaoHang }}</p>
              </div>
              <div class="info-card">
                <h4>Thông Tin Thanh Toán</h4>
                <p><strong>Hình thức:</strong> {{ getPaymentMethodLabel(selectedOrder?.phuongThucThanhToan) }}</p>
                <p><strong>Tình trạng:</strong>
                  <span :class="isPaid(selectedOrder?.trangThaiThanhToan) ? 'text-success' : 'text-danger'">
                    {{ isPaid(selectedOrder?.trangThaiThanhToan) ? 'Đã hoàn tất' : 'Chưa hoàn tất' }}
                  </span>
                </p>
                <p><strong>Tổng cộng:</strong> <span class="price-large">{{ formatPrice(selectedOrder?.tongTien) }}</span></p>
              </div>
            </div>
            <h4 class="table-title">Danh Sách Sản Phẩm</h4>
            <table class="detail-table">
              <thead>
                <tr>
                  <th>Mã SP</th>
                  <th>Đơn Giá</th>
                  <th style="text-align: center;">Số Lượng</th>
                  <th style="text-align: right;">Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in orderDetails" :key="item.maChiTietDonHang">
                  <td><strong>SP #{{ item.maSanPham }}</strong></td>
                  <td>{{ formatPrice(item.giaLucMua) }}</td>
                  <td style="text-align: center;"><span class="qty-badge">{{ item.soLuong }}</span></td>
                  <td style="text-align: right; font-weight: bold; color: #d1aa68;">{{ formatPrice(item.giaLucMua * item.soLuong) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeDetailModal">Đóng cửa sổ</button>
          </div>
        </div>
      </div>

      <!-- MODAL XÁC NHẬN HỦY ĐƠN (DÀNH CHO ADMIN NẾU CẦN) -->
      <div class="modal-overlay" v-if="showCancelModal" @click.self="closeCancelModal">
        <div class="modal-box" style="width: 500px;">
          <div class="modal-header">
            <h2>Hủy Đơn Hàng <span style="color: #dc3545;">#{{ orderToCancel?.maDonHangCode }}</span></h2>
            <button class="btn-close" @click="closeCancelModal">&#10005;</button>
          </div>
          <div class="modal-body">
            <p style="margin-bottom: 12px; font-weight: 500;">Lý do hủy đơn (Bắt buộc):</p>
            <textarea 
              v-model="cancelReason" 
              rows="4" 
              placeholder="Nhập lý do để lưu vết hệ thống và thông báo qua email cho khách hàng..." 
              class="form-control"
              style="width: 100%; padding: 12px; border-radius: 4px; border: 1px solid #ccc; font-family: inherit; resize: none;"
            ></textarea>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeCancelModal">Quay lại</button>
            <button class="btn-submit-cancel" @click="submitCancelOrder">Xác nhận hủy đơn</button>
          </div>
        </div>
      </div>
      
      <!-- MODAL THÊM PHƯƠNG THỨC THANH TOÁN MỚI -->
      <div class="modal-overlay" v-if="showAddPaymentModal" @click.self="closeAddPaymentModal">
        <div class="modal-box" style="width: 400px;">
          <div class="modal-header">
            <h2>Thêm Phương Thức Mới</h2>
            <button class="btn-close" @click="closeAddPaymentModal">&#10005;</button>
          </div>
          <div class="modal-body">
            <p style="margin-bottom: 12px; font-weight: 500;">Tên phương thức thanh toán:</p>
            <input 
              type="text" 
              v-model="newPaymentMethodName" 
              placeholder="VD: Ví Momo, ZaloPay, Trả góp..." 
              class="form-control"
              style="width: 100%; padding: 10px 12px; border-radius: 4px; border: 1px solid #ccc; font-family: inherit;"
              @keyup.enter="submitNewPaymentMethod"
            />
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeAddPaymentModal">Hủy bỏ</button>
            <button class="btn-submit-success" @click="submitNewPaymentMethod">Xác nhận thêm</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// CẤU HÌNH TRUNG TÂM TRẠNG THÁI
const ORDER_STATUSES = {
  'CHO_XU_LY': { label: 'Chờ Xử Lý', cssClass: 'status-pending' },
  'CHUAN_BI_HANG': { label: 'Chuẩn Bị Hàng', cssClass: 'status-preparing' },
  'DANG_GIAO': { label: 'Đang Giao', cssClass: 'status-shipping' },
  'DA_GIAO': { label: 'Đã Giao', cssClass: 'status-delivered' },
  'DA_HUY': { label: 'Đã Hủy', cssClass: 'status-canceled' }
};

const ORDER_ACTIONS_MAP = {
  'CHO_XU_LY': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false },
    { value: 'CONFIRM', label: 'Xác nhận (Chuyển chuẩn bị hàng)', isDanger: false },
    { value: 'CANCEL', label: 'Hủy đơn hàng', isDanger: true }
  ],
  'CHUAN_BI_HANG': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false },
    { value: 'SHIPPING', label: 'Tiến hành giao hàng', isDanger: false },
    { value: 'CANCEL', label: 'Hủy đơn hàng', isDanger: true }
  ],
  'DANG_GIAO': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false },
    { value: 'APPROVE', label: 'Hoàn tất đơn (Đã giao)', isDanger: false },
    { value: 'CANCEL', label: 'Hủy (Giao thất bại)', isDanger: true }
  ],
  'DA_GIAO': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false }
  ],
  'DA_HUY': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false }
  ]
};

const isCollapsed = ref(false);
const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

const orders = ref([]);
const showDetailModal = ref(false);
const selectedOrder = ref(null);
const orderDetails = ref([]);

const showCancelModal = ref(false);
const orderToCancel = ref(null);
const cancelReason = ref('');

const searchQuery = ref('');
const filterDate = ref('');
const paymentMethods = ref(['COD', 'CHUYEN_KHOAN_QR', 'VNPAY', 'THE_TIN_DUNG']);

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const query = searchQuery.value.trim().toLowerCase();
    const matchesQuery = !query || 
      (order.maDonHangCode && order.maDonHangCode.toLowerCase().includes(query)) ||
      (order.tenNguoiNhan && order.tenNguoiNhan.toLowerCase().includes(query)) ||
      (order.soDienThoaiGiaoHang && order.soDienThoaiGiaoHang.includes(query));

    let matchesDate = true;
    if (filterDate.value && order.ngayTao) {
      const orderDate = new Date(order.ngayTao).toISOString().split('T')[0];
      matchesDate = (orderDate === filterDate.value);
    }
    return matchesQuery && matchesDate;
  });
});

const showAddPaymentModal = ref(false);
const newPaymentMethodName = ref('');

const openAddPaymentModal = () => {
  newPaymentMethodName.value = '';
  showAddPaymentModal.value = true;
};

const closeAddPaymentModal = () => {
  showAddPaymentModal.value = false;
  newPaymentMethodName.value = '';
};

const submitNewPaymentMethod = async () => {
  const method = newPaymentMethodName.value.trim();
  if (!method) {
    alert("Vui lòng nhập tên phương thức thanh toán!");
    return;
  }
  if (paymentMethods.value.includes(method)) {
    alert("Phương thức thanh toán này đã tồn tại!");
    return;
  }
  paymentMethods.value.push(method);
  alert(`Đã thêm thành công: ${method}`);
  closeAddPaymentModal();
};

const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getPaymentMethodLabel = (method) => {
  const map = {
    'CHUYEN_KHOAN_QR': 'Chuyển khoản QR',
    'THE_TIN_DUNG': 'Thẻ Tín Dụng',
    'VNPAY': 'Cổng VNPAY',
    'COD': 'Thanh toán COD'
  };
  return map[method] || method;
};

const isPaid = (status) => {
  return status === 'DA_THANH_TOAN' || status === 'Đã thanh toán';
};

const getStatusLabel = (status) => ORDER_STATUSES[status]?.label || 'Không xác định';
const getStatusClass = (status) => ORDER_STATUSES[status]?.cssClass || 'status-pending';
const getAvailableActions = (status) => ORDER_ACTIONS_MAP[status] || [];

const loadOrders = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/don-hang');
    if (res.ok) {
      orders.value = await res.json();
    }
  } catch (error) {
    console.error('Lỗi kết nối Backend:', error);
  }
};

const handleActionSelect = (order, event) => {
  const selectedAction = event.target.value;
  if (!selectedAction) return;

  switch (selectedAction) {
    case 'VIEW':
      viewOrderDetails(order);
      break;
    case 'CONFIRM':
      changeOrderStatus(order.maDonHang, 'CHUAN_BI_HANG');
      break;
    case 'SHIPPING':
      changeOrderStatus(order.maDonHang, 'DANG_GIAO');
      break;
    case 'APPROVE':
      changeOrderStatus(order.maDonHang, 'DA_GIAO');
      break;
    case 'CANCEL':
      orderToCancel.value = order;
      cancelReason.value = '';
      showCancelModal.value = true;
      break;
  }
  event.target.value = "";
};

const changeOrderStatus = async (id, statusMoi) => {
  let thongBao = `Xác nhận chuyển đơn hàng sang trạng thái: [${getStatusLabel(statusMoi)}]?`;
  let url = `http://localhost:8080/api/don-hang/${id}/trang-thai?trangThaiMoi=${statusMoi}`;

  if (statusMoi === 'DA_GIAO') {
    thongBao = "Xác nhận đã giao hàng thành công?\nHệ thống sẽ tự động cập nhật trạng thái tiền thành [Đã thanh toán].";
    url += `&trangThaiThanhToanMoi=DA_THANH_TOAN`;
  }

  if (!confirm(thongBao)) return;

  try {
    const res = await fetch(url, { method: 'PATCH' });
    if (res.ok) {
      loadOrders(); 
    } else {
      alert("Lỗi từ hệ thống: " + await res.text());
    }
  } catch (error) {
    alert("Không thể kết nối đến máy chủ Backend.");
  }
};

const submitCancelOrder = async () => {
  if (!cancelReason.value.trim()) {
    alert("Vui lòng nhập lý do hủy đơn để lưu vết hệ thống.");
    return;
  }
  
  const url = `http://localhost:8080/api/don-hang/${orderToCancel.value.maDonHang}/trang-thai?trangThaiMoi=DA_HUY&lyDo=${encodeURIComponent(cancelReason.value.trim())}`;
  
  try {
    const res = await fetch(url, { method: 'PATCH' });
    if (res.ok) {
      closeCancelModal();
      loadOrders(); 
    } else {
      alert("Lỗi khi hủy đơn: " + await res.text());
    }
  } catch (error) {
    alert("Không thể kết nối đến máy chủ Backend.");
  }
};

const viewOrderDetails = async (order) => {
  selectedOrder.value = order;
  showDetailModal.value = true;
  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${order.maDonHang}/chi-tiet`);
    if (res.ok) {
      orderDetails.value = await res.json();
    } else {
      orderDetails.value = [{ maChiTietDonHang: 1, maSanPham: 2, soLuong: 1, giaLucMua: order.tongTien }];
    }
  } catch (error) {
    orderDetails.value = [{ maChiTietDonHang: 1, maSanPham: 2, soLuong: 1, giaLucMua: order.tongTien }];
  }
};

const closeDetailModal = () => {
  showDetailModal.value = false;
  selectedOrder.value = null;
};

const closeCancelModal = () => {
  showCancelModal.value = false;
  orderToCancel.value = null;
  cancelReason.value = '';
};

const changePaymentMethod = async (orderId, newMethod) => {
  try {
    console.log(`Đã đổi phương thức thanh toán đơn ${orderId} thành ${newMethod}`);
  } catch (error) {
    console.error('Lỗi', error);
  }
};

onMounted(() => { loadOrders(); });
</script>

<style>
:root {
  --wood-dark: #362921;
  --wood-active: #47372c;
  --wood-medium: #544438;
  --wood-light: #7a6352;
  --gold-matte: #cca15e;
  --bg-page: #f8f6f0;
  --border-light: #eaeaea;
  --text-main: #333333;
  --text-muted: #888888;
}
</style>

<style scoped>
@import "../CSS/Admin/QuanLyDonHang.css";

.velora-admin-wrapper {
  display: flex;
  height: 100vh;
  background-color: var(--bg-page);
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.content-wrapper {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.header-left h1 {
  font-size: 26px;
  font-weight: bold;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
}
.header-left .gold {
  color: var(--gold-matte);
}
.header-left p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.option-danger {
  color: #dc3545;
  font-weight: 500;
}

.status-preparing {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeeba;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-box {
  background: #fff;
  border-radius: 6px;
  width: 700px;
  max-width: 90%;
  max-height: 90vh; 
  overflow-y: auto; 
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 10;
}

.modal-header h2 {
  font-size: 18px;
  margin: 0;
  color: var(--wood-dark);
}

.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #888;
  padding: 4px;
}
.btn-close:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  position: sticky;
  bottom: 0;
  background: #fff;
}

.btn-cancel {
  background: #f4f4f4;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  color: #555;
  font-weight: 500;
}
.btn-cancel:hover {
  background: #e9e9e9;
}

.btn-submit-cancel {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}
.btn-submit-cancel:hover {
  background: #c82333;
}

.btn-submit-success {
  background: #198754;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: 0.2s;
}
.btn-submit-success:hover {
  background: #157347;
}

.form-control:focus {
  outline: none;
  border-color: var(--gold-matte);
  box-shadow: 0 0 0 2px rgba(204, 161, 94, 0.2);
}
</style>