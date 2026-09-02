<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- =========================================================================
         [PHẦN 1: KHUNG ĐIỀU HƯỚNG VÀ TIÊU ĐỀ TRANG]
    ========================================================================== -->
    <!-- Thanh menu bên trái -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- Thanh header trên cùng chứa thông tin admin -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <main class="content">
        <!-- TIÊU ĐỀ CHÍNH CỦA TRANG -->
        <header class="header">
          <div class="header-left">
            <!-- DÒNG MẶC ĐỊNH: Chữ 'Đơn Hàng' màu vàng hoàng kim (class gold) -->
            <h1>Quản Lý <span class="gold">Đơn Hàng</span></h1>
            <!-- THAY THẾ: Đổi chữ 'Đơn Hàng' thành MÀU ĐỎ:
            <h1>Quản Lý <span style="color: #dc3545;">Đơn Hàng</span></h1> -->
            <!-- THAY THẾ: Đổi chữ 'Đơn Hàng' thành MÀU XANH DƯƠNG:
            <h1>Quản Lý <span style="color: #0d6efd;">Đơn Hàng</span></h1> -->
            <!-- THAY THẾ: Viết hoa toàn bộ tiêu đề:
            <h1 style="text-transform: uppercase;">Quản Lý <span class="gold">Đơn Hàng</span></h1> -->

            <p>Theo dõi, luân chuyển trạng thái và xử lý các giao dịch giao hàng.</p>
          </div>
        </header>

        <!-- =========================================================================
             [PHẦN 2: BỘ LỌC TÌM KIẾM VÀ CHỌN NGÀY TẠO]
        ========================================================================== -->
        <section class="filter-wrapper">
          <!-- Ô nhập tìm kiếm từ khóa -->
          <div class="search-box">
            <i class="fa-solid fa-magnifying-glass search-icon"></i>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Tìm theo mã đơn, số điện thoại, tên khách hàng..." 
              class="input-search"
            />
            <!-- THAY THẾ: Thêm nút Xóa nhanh từ khóa vừa nhập trong ô tìm kiếm:
            <button v-if="searchQuery" @click="searchQuery = ''" style="border:none; background:none; cursor:pointer;"><i class="fa-solid fa-xmark"></i></button> -->
          </div>

          <!-- Ô chọn ngày tạo đơn hàng -->
          <div class="date-box">
            <label for="filterDate" class="label-date">Lọc ngày tạo:</label>
            <input 
              type="date" 
              id="filterDate" 
              v-model="filterDate" 
              class="input-date"
            />
            <!-- Nút bấm dấu X để xóa ngày đã chọn -->
            <button v-if="filterDate" class="btn-clear-date" title="Xóa lọc" @click="filterDate = ''">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </section>

        <!-- =========================================================================
             [PHẦN 3: BẢNG DỮ LIỆU ĐƠN HÀNG (TABLE DATA)]
        ========================================================================== -->
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
              <!-- Vòng lặp duyệt danh sách đơn hàng -->
              <tr v-for="order in filteredOrders" :key="order.maDonHang">
                
                <!-- Cột 1: Mã đơn hàng -->
                <td class="order-code">{{ order.maDonHangCode }}</td>
                <!-- THAY THẾ: Mã đơn hàng in đậm màu vàng kim:
                <td class="order-code" style="color: #cca15e; font-weight: bold;">{{ order.maDonHangCode }}</td> -->
                <!-- THAY THẾ: Mã đơn hàng in đậm màu đỏ:
                <td class="order-code" style="color: #dc3545; font-weight: bold;">{{ order.maDonHangCode }}</td> -->

                <!-- Cột 2: Tên & SĐT khách hàng -->
                <td class="customer-info">
                  <strong>{{ order.tenNguoiNhan }}</strong>
                  <span class="phone">{{ order.soDienThoaiGiaoHang }}</span>
                </td>
                
                <!-- Cột 3: Ngày đặt hàng -->
                <td>{{ formatDate(order.ngayTao) }}</td>
                
                <!-- Cột 4: Tổng tiền thanh toán -->
                <td class="price">{{ formatPrice(order.tongTien) }}</td>
                <!-- THAY THẾ: Tổng tiền hiển thị màu đỏ to rõ:
                <td class="price" style="color: #dc3545; font-weight: bold; font-size: 16px;">{{ formatPrice(order.tongTien) }}</td> -->
                <!-- THAY THẾ: Tổng tiền hiển thị màu xanh lá:
                <td class="price" style="color: #198754; font-weight: bold;">{{ formatPrice(order.tongTien) }}</td> -->

                <!-- Cột 5: Hình thức thanh toán & Nút dấu cộng (+) -->
                <td>
                  <div class="payment-edit-group">
                    <!-- Dropdown chọn phương thức: Bắt sự kiện thay đổi để gọi API lưu ngay vào Database -->
                    <select 
                      v-model="order.phuongThucThanhToan" 
                      @change="changePaymentMethod(order.maDonHang, order.phuongThucThanhToan)"
                      class="payment-select"
                    >
                      <option v-for="method in paymentMethods" :key="method" :value="method">
                        {{ getPaymentMethodLabel(method) }}
                      </option>
                    </select>

                    <!-- NÚT DẤU CỘNG (+) THÊM PHƯƠNG THỨC MỚI -->
                    <button class="btn-add-payment-method" title="Tạo thêm hình thức mới" @click="openAddPaymentModal">+</button>
                    <!-- THAY THẾ: Đổi dấu '+' thành nút chữ 'Thêm':
                    <button class="btn-add-payment-method" style="width: auto; padding: 0 8px;" @click="openAddPaymentModal">Thêm</button> -->
                  </div>

                  <!-- BADGE HIỂN THỊ TÌNH TRẠNG TIỀN (ĐÃ / CHƯA THANH TOÁN) -->
                  <div style="margin-top: 6px;">
                    <span class="payment-status" :class="isPaid(order.trangThaiThanhToan) ? 'paid' : 'unpaid'">
                      {{ isPaid(order.trangThaiThanhToan) ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                    </span>
                    <!-- THAY THẾ: Bấm trực tiếp vào badge để đảo trạng thái tiền mặt:
                    <span class="payment-status" style="cursor: pointer;" :class="isPaid(order.trangThaiThanhToan) ? 'paid' : 'unpaid'" @click="order.trangThaiThanhToan = isPaid(order.trangThaiThanhToan) ? 'CHUA_THANH_TOAN' : 'DA_THANH_TOAN'">
                      {{ isPaid(order.trangThaiThanhToan) ? 'Đã thanh toán' : 'Chưa thanh toán' }} (Click đổi)
                    </span> -->
                  </div>
                </td>

                <!-- Cột 6: Badge trạng thái giao hàng -->
                <td>
                  <span class="status-badge" :class="getStatusClass(order.trangThaiDonHang)">
                    {{ getStatusLabel(order.trangThaiDonHang) }}
                  </span>
                </td>

                <!-- Cột 7: Thao tác / Chuyển trạng thái / Xóa -->
                <td>
                  <!-- DÒNG MẶC ĐỊNH: Menu Dropdown chọn lệnh -->
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
                  <!-- THAY THẾ: Tách thành 2 nút Xem chi tiết và Hủy đơn riêng biệt:
                  <div style="display: flex; gap: 4px;">
                    <button class="btn-cancel" style="padding: 4px 8px; font-size: 12px;" @click="viewOrderDetails(order)">Xem</button>
                    <button class="btn-submit-cancel" style="padding: 4px 8px; font-size: 12px;" @click="orderToCancel = order; showCancelModal = true;">Hủy</button>
                  </div> -->
                  <!-- THAY THẾ: Thêm nút Xóa trực tiếp đơn hàng khỏi bảng danh sách:
                  <button style="background: #dc3545; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; font-size: 12px;" @click="deleteOrderDirectly(order.maDonHang)">Xóa</button> -->
                </td>
              </tr>

              <!-- Dòng thông báo khi không tìm thấy kết quả -->
              <tr v-if="filteredOrders.length === 0">
                <td colspan="7" class="empty-state">Không tìm thấy dữ liệu phù hợp.</td>
              </tr>
            </tbody>
          </table>
        </section>
      </main>

      <!-- =========================================================================
           [PHẦN 4: TOÀN BỘ CỬA SỔ POPUP & MODAL]
      ========================================================================== -->

      <!-- 4.1 CỬA SỔ XEM CHI TIẾT ĐƠN HÀNG VÀ SẢN PHẨM -->
      <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
        <div class="modal-box modal-lg">
          <div class="modal-header">
            <h2>Chi Tiết Đơn Hàng <span class="gold">#{{ selectedOrder?.maDonHangCode }}</span></h2>
            <button class="btn-close" @click="closeDetailModal">&#10005;</button>
          </div>
          <div class="modal-body">
            <!-- Hộp hiển thị lý do hủy (nếu đơn bị hủy) -->
            <div v-if="selectedOrder?.trangThaiDonHang === 'DA_HUY' || selectedOrder?.trangThaiDonHang === 'YEU_CAU_HUY'" class="cancel-reason-box" style="background: #fdf2f2; border: 1px solid #f8d7da; padding: 15px; border-radius: 6px; margin-bottom: 20px;">
              <h4 style="color: #dc3545; margin: 0 0 5px 0;"><i class="fa-solid fa-triangle-exclamation"></i> Thông Tin Hủy Đơn Từ Khách Hàng</h4>
              <p style="margin: 0; color: #721c24; font-size: 14px;">
                <strong>Lý do chi tiết:</strong> {{ selectedOrder?.lyDoHuyDon && selectedOrder.lyDoHuyDon.trim() !== '' ? selectedOrder.lyDoHuyDon : 'Khách hàng không cung cấp lý do cụ thể' }}
              </p>
            </div>

            <!-- Khung thông tin giao nhận và thanh toán -->
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

            <!-- Bảng danh sách các sản phẩm bên trong đơn -->
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

      <!-- 4.2 CỬA SỔ HỦY ĐƠN HÀNG VÀ NHẬP LÝ DO -->
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
      
      <!-- 4.3 CỬA SỔ THÊM PHƯƠNG THỨC THANH TOÁN MỚI -->
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

      <!-- 4.4 HỘP THOẠI XÁC NHẬN CHUYỂN TRẠNG THÁI (CUSTOM CONFIRM) -->
      <div class="modal-overlay" v-if="confirmDialog.show" @click.self="handleConfirmCancel">
        <div class="modal-box custom-dialog-box">
          <div class="dialog-icon-wrapper" :class="confirmDialog.type">
            <i class="fa-solid" :class="confirmDialog.type === 'danger' ? 'fa-triangle-exclamation' : 'fa-circle-question'"></i>
          </div>
          <h3 class="dialog-title">{{ confirmDialog.title }}</h3>
          <p class="dialog-message">{{ confirmDialog.message }}</p>
          <div class="dialog-actions">
            <button class="btn-dialog-cancel" @click="handleConfirmCancel">Hủy bỏ</button>
            <button 
              class="btn-dialog-confirm" 
              :class="confirmDialog.type === 'danger' ? 'btn-danger-confirm' : 'btn-gold-confirm'"
              @click="handleConfirmOk"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>

      <!-- 4.5 THÔNG BÁO TỰ TẮT Ở GÓC (TOAST NOTIFICATION) -->
      <div class="custom-alert-toast" :class="[alertToast.type, { 'show': alertToast.show }]">
        <i class="fa-solid" :class="alertToast.type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'"></i>
        <span>{{ alertToast.message }}</span>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// =========================================================================
// [LOGIC XỬ LÝ 1: CẤU HÌNH TRẠNG THÁI VÀ BẢN ĐỒ LỆNH]
// =========================================================================
const ORDER_STATUSES = {
  'CHO_XU_LY': { label: 'Chờ Xử Lý', cssClass: 'status-pending' },
  'CHUAN_BI_HANG': { label: 'Chuẩn Bị Hàng', cssClass: 'status-preparing' },
  'DANG_GIAO': { label: 'Đang Giao', cssClass: 'status-shipping' },
  'DA_GIAO': { label: 'Đã Giao', cssClass: 'status-delivered' },
  'DA_HUY': { label: 'Đã Hủy', cssClass: 'status-canceled' },
  'YEU_CAU_HUY': { label: 'Đang Chờ Duyệt Hủy', cssClass: 'status-pending' }
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
  ],
  'YEU_CAU_HUY': [
    { value: 'VIEW', label: 'Xem chi tiết', isDanger: false },
    { value: 'APPROVE_CANCEL', label: 'Đồng ý hủy đơn', isDanger: true },
    { value: 'REJECT_CANCEL', label: 'Từ chối hủy (Tiếp tục giao)', isDanger: false }
  ]
};

// =========================================================================
// [LOGIC XỬ LÝ 2: KHỞI TẠO BIẾN DỮ LIỆU & LƯU LOCALSTORAGE]
// =========================================================================
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

// Danh sách phương thức mặc định của hệ thống
const defaultPaymentMethods = ['COD', 'CHUYEN_KHOAN_QR', 'VNPAY', 'THE_TIN_DUNG'];

// Tải danh sách phương thức từ localStorage để không bao giờ bị mất khi F5
const savedPaymentMethods = localStorage.getItem('velora_payment_methods_list');
const paymentMethods = ref(savedPaymentMethods ? JSON.parse(savedPaymentMethods) : defaultPaymentMethods);

const showAddPaymentModal = ref(false);
const newPaymentMethodName = ref('');

const confirmDialog = ref({
  show: false,
  title: '',
  message: '',
  type: 'gold',
  resolve: null
});

const alertToast = ref({
  show: false,
  message: '',
  type: 'success'
});
let toastTimer = null;

// =========================================================================
// [LOGIC XỬ LÝ 3: THUẬT TOÁN TÍNH TOÁN & THÔNG BÁO]
// =========================================================================
const showToast = (message, type = 'success') => {
  alertToast.value = { show: true, message, type };
  if (toastTimer) clearTimeout(toastTimer);
  
  // DÒNG MẶC ĐỊNH: Tự tắt sau 3 giây (3000ms)
  toastTimer = setTimeout(() => { alertToast.value.show = false; }, 3000);
};

const openConfirm = (title, message, type = 'gold') => {
  confirmDialog.value = { show: true, title, message, type, resolve: null };
  return new Promise((resolve) => { confirmDialog.value.resolve = resolve; });
};

const handleConfirmOk = () => {
  confirmDialog.value.show = false;
  if (confirmDialog.value.resolve) confirmDialog.value.resolve(true);
};

const handleConfirmCancel = () => {
  confirmDialog.value.show = false;
  if (confirmDialog.value.resolve) confirmDialog.value.resolve(false);
};

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

const isPaid = (status) => status === 'DA_THANH_TOAN' || status === 'Đã thanh toán';
const getStatusLabel = (status) => ORDER_STATUSES[status]?.label || 'Không xác định';
const getStatusClass = (status) => ORDER_STATUSES[status]?.cssClass || 'status-pending';
const getAvailableActions = (status) => ORDER_ACTIONS_MAP[status] || [];

// =========================================================================
// [LOGIC XỬ LÝ 4: GỌI API BACKEND VÀ WEBSOCKET]
// =========================================================================
const loadOrders = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/don-hang');
    if (res.ok) orders.value = await res.json();
  } catch (error) {
    console.error('Lỗi kết nối Backend:', error);
  }
};

const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws'); 
  const stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/orders', (message) => {
        if (message.body === 'RELOAD_ORDERS') loadOrders();
      });
    }
  });
  stompClient.activate();
};

const handleActionSelect = async (order, event) => {
  const selectedAction = event.target.value;
  event.target.value = "";
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
    case 'APPROVE_CANCEL':
      if (await openConfirm("Đồng ý hủy đơn", `Xác nhận đồng ý hủy đơn hàng #${order.maDonHangCode}?`, 'danger')) {
        changeOrderStatus(order.maDonHang, 'DA_HUY');
      }
      break;
    case 'REJECT_CANCEL':
      if (await openConfirm("Từ chối hủy đơn", `Từ chối yêu cầu hủy và đưa đơn hàng #${order.maDonHangCode} về lại trạng thái Chờ xử lý?`, 'gold')) {
        changeOrderStatus(order.maDonHang, 'CHO_XU_LY');
      }
      break;
  }
};

const changeOrderStatus = async (id, statusMoi) => {
  let thongBao = `Xác nhận chuyển đơn hàng sang trạng thái: [${getStatusLabel(statusMoi)}]?`;
  let url = `http://localhost:8080/api/don-hang/${id}/trang-thai?trangThaiMoi=${statusMoi}`;

  if (statusMoi === 'DA_GIAO') {
    thongBao = "Xác nhận đã giao hàng thành công? Hệ thống sẽ tự động cập nhật trạng thái tiền thành [Đã thanh toán].";
    url += `&trangThaiThanhToanMoi=DA_THANH_TOAN`;
  }

  const confirmed = await openConfirm("Xác nhận cập nhật", thongBao, statusMoi === 'DA_HUY' ? 'danger' : 'gold');
  if (!confirmed) return;

  try {
    const res = await fetch(url, { method: 'PATCH' });
    if (res.ok) {
      showToast("Cập nhật trạng thái thành công!", "success");
      loadOrders(); 
    } else {
      showToast("Lỗi: " + await res.text(), "error");
    }
  } catch (error) {
    showToast("Không thể kết nối đến máy chủ Backend.", "error");
  }
};

const submitCancelOrder = async () => {
  if (!cancelReason.value.trim()) {
    showToast("Vui lòng nhập lý do hủy đơn để lưu vết hệ thống.", "error");
    return;
  }
  
  const url = `http://localhost:8080/api/don-hang/${orderToCancel.value.maDonHang}/trang-thai?trangThaiMoi=DA_HUY&lyDo=${encodeURIComponent(cancelReason.value.trim())}`;
  
  try {
    const res = await fetch(url, { method: 'PATCH' });
    if (res.ok) {
      closeCancelModal();
      showToast("Hủy đơn hàng thành công!", "success");
      loadOrders(); 
    } else {
      showToast("Lỗi khi hủy đơn: " + await res.text(), "error");
    }
  } catch (error) {
    showToast("Không thể kết nối đến máy chủ Backend.", "error");
  }
};

const deleteOrderDirectly = (orderId) => {
  orders.value = orders.value.filter(o => o.maDonHang !== orderId);
  showToast("Đã xóa đơn hàng thành công khỏi danh sách!", "success");
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

const openAddPaymentModal = () => {
  newPaymentMethodName.value = '';
  showAddPaymentModal.value = true;
};

const closeAddPaymentModal = () => {
  showAddPaymentModal.value = false;
  newPaymentMethodName.value = '';
};

// =========================================================================
// [LOGIC XỬ LÝ 5: THÊM & CẬP NHẬT PHƯƠNG THỨC THANH TOÁN (XỬ LÝ DỨT ĐIỂM F5)]
// =========================================================================

/**
 * 1. Xử lý thêm phương thức thanh toán mới: Lưu vào localStorage để F5 không bị mất
 */
const submitNewPaymentMethod = async () => {
  const method = newPaymentMethodName.value.trim();
  if (!method) {
    showToast("Vui lòng nhập tên phương thức thanh toán!", "error");
    return;
  }

  // Chuyển đổi tên nhập thành dạng mã chuỗi viết hoa không dấu gạch chân (VD: Momo -> MOMO)
  const codeMethod = method.toUpperCase().replace(/\s+/g, '_');

  if (paymentMethods.value.includes(codeMethod) || paymentMethods.value.includes(method)) {
    showToast("Phương thức thanh toán này đã tồn tại!", "error");
    return;
  }

  // Bổ sung vào mảng hiển thị hiện tại
  paymentMethods.value.push(codeMethod);
  
  // Lưu danh sách mới vào LocalStorage để không bao giờ bị mất sau khi reload trang
  localStorage.setItem('velora_payment_methods_list', JSON.stringify(paymentMethods.value));

  showToast(`Đã thêm thành công hình thức: ${method}`, "success");
  closeAddPaymentModal();
};

/**
 * 2. Xử lý đổi phương thức thanh toán: Gọi API lưu thẳng xuống Database
 */
const changePaymentMethod = async (orderId, newMethod) => {
  try {
    // Gọi API PATCH cập nhật phương thức thanh toán đã thêm ở DonHangController.java
    const url = `http://localhost:8080/api/don-hang/${orderId}/phuong-thuc-thanh-toan?phuongThucMoi=${encodeURIComponent(newMethod)}`;
    const res = await fetch(url, { method: 'PATCH' });

    if (res.ok) {
      showToast(`Đã lưu phương thức: ${getPaymentMethodLabel(newMethod)} vào hệ thống!`, "success");
    } else {
      const errText = await res.text();
      showToast(`Lỗi lưu CSDL: ${errText}`, "error");
      loadOrders(); // Khôi phục lại dữ liệu nếu lỗi
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật phương thức thanh toán:', error);
    showToast("Không thể kết nối đến Backend để lưu phương thức!", "error");
    loadOrders();
  }
};

onMounted(() => { 
  loadOrders(); 
  connectWebSocket();
});
</script>

<style>
/* =========================================================================
   [BẢNG MÃ MÀU TOÀN CỤC CỦA HỆ THỐNG VELORA]
========================================================================= */
:root {
  --wood-dark: #362921;     /* Màu nâu gỗ đậm (Dùng cho tiêu đề, bảng) */
  --wood-active: #47372c;   /* Màu nâu gỗ sẫm khi hover */
  --wood-medium: #544438;   /* Màu nâu gỗ vừa */
  --wood-light: #7a6352;    /* Màu nâu gỗ sáng */
  --gold-matte: #cca15e;    /* Màu vàng hoàng kim (Dùng cho nút thêm, viền active) */
  --bg-page: #f8f6f0;       /* Màu nền toàn bộ website (Trắng kem nhạt) */
  --border-light: #eaeaea;  /* Màu đường viền kẻ bảng */
  --text-main: #333333;     /* Màu chữ đen xám chính */
  --text-muted: #888888;    /* Màu chữ xám phụ */
}
</style>

<style scoped>
@import "../CSS/Admin/QuanLyDonHang.css";

/* =========================================================================
   [CSS KHUNG GIAO DIỆN CHÍNH & TIÊU ĐỀ]
========================================================================= */
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
  color: var(--wood-dark); /* Màu chữ tiêu đề: Nâu gỗ đậm */
  margin: 0 0 5px 0;
}

.header-left .gold {
  color: var(--gold-matte); /* Màu vàng hoàng kim cho chữ 'Đơn Hàng' */
}

.header-left p {
  font-size: 14px;
  color: var(--text-muted); /* Màu chữ xám phụ */
  margin: 0;
}

.status-preparing {
  background-color: #fff3cd; /* Màu nền vàng nhạt của badge Chuẩn Bị Hàng */
  color: #856404;            /* Màu chữ nâu vàng đậm của badge Chuẩn Bị Hàng */
  border: 1px solid #ffeeba; /* Đường viền vàng nhạt */
}

.option-danger {
  color: #dc3545; /* Màu chữ đỏ của các mục Hủy trong dropdown menu */
  font-weight: 500;
}

/* =========================================================================
   [CSS KHUNG MÀN HÌNH MỜ & HỘP THOẠI MODAL]
========================================================================= */

/* Lớp màn đen mờ phủ kín toàn màn hình */
.modal-overlay {
  position: fixed;                   /* Cố định vị trí trên màn hình kể cả khi cuộn chuột */
  top: 0;                            /* Dính sát mép trên cùng */
  left: 0;                           /* Dính sát mép bên trái */
  width: 100vw;                      /* Chiều rộng bằng 100% màn hình */
  height: 100vh;                     /* Chiều cao bằng 100% màn hình */
  background: rgba(0, 0, 0, 0.45);   /* Màu nền ĐEN trong suốt 45% để làm mờ nền phía sau */
  display: flex;                     /* Bật chế độ Flexbox */
  align-items: center;               /* Căn hộp modal vào CHÍNH GIỮA theo chiều dọc */
  justify-content: center;            /* Căn hộp modal vào CHÍNH GIỮA theo chiều ngang */
  z-index: 1000;                     /* Luôn nổi lên trên cùng */
  backdrop-filter: blur(2px);        /* Hiệu ứng làm mờ kính nhẹ 2px */
}

/* Hộp màu trắng chứa nội dung Modal */
.modal-box {
  background: #fff;                  /* Màu nền: MÀU TRẮNG TINH */
  border-radius: 6px;                /* Bo tròn 4 góc nhẹ 6px */
  width: 700px;                      /* Chiều rộng mặc định 700px */
  max-width: 90%;                    /* Không vượt quá 90% màn hình nhỏ */
  max-height: 90vh;                  /* Giới hạn chiều cao 90% màn hình */
  overflow-y: auto;                  /* Tự xuất hiện thanh cuộn dọc khi nội dung dài */
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); /* Đổ bóng xám nhẹ */
}

/* Thanh tiêu đề trên cùng của Modal */
.modal-header {
  padding: 16px 24px;                          /* Đệm: trên-dưới 16px, trái-phải 24px */
  border-bottom: 1px solid var(--border-light);/* Đường kẻ ngang ngăn cách màu xám nhạt */
  display: flex;                               /* Bật Flexbox */
  justify-content: space-between;              /* Đẩy Tiêu đề sang trái, nút đóng sang phải */
  align-items: center;                         /* Căn thẳng hàng theo chiều dọc */
  position: sticky;                            /* Giữ cố định thanh header khi cuộn chuột */
  top: 0;                                      /* Dính chặt trên đỉnh */
  background: #fff;                            /* Nền trắng che nội dung cuộn bên dưới */
  z-index: 10;                                 /* Nổi lên trên nội dung cuộn */
}

.modal-header h2 {
  font-size: 18px;                             /* Kích thước chữ tiêu đề modal */
  margin: 0;
  color: var(--wood-dark);                     /* Màu chữ: Nâu gỗ đậm */
}

/* Phần thân chứa nội dung Modal */
.modal-body {
  padding: 24px;                               /* Khoảng cách đệm xung quanh nội dung là 24px */
}

/* Thanh chân trang dưới cùng của Modal chứa các nút bấm */
.modal-footer {
  padding: 16px 24px;                          /* Đệm: trên-dưới 16px, trái-phải 24px */
  border-top: 1px solid var(--border-light);   /* Đường kẻ ngang ngăn cách phía trên màu xám nhạt */
  display: flex;                               /* Bật Flexbox */
  justify-content: flex-end;                   /* DÒNG MẶC ĐỊNH: Căn các nút sang góc BÊN PHẢI */
  gap: 12px;                                   /* Khoảng cách giữa các nút là 12px */
  position: sticky;                            /* Cố định thanh chân trang khi cuộn */
  bottom: 0;                                   /* Dính sát mép dưới */
  background: #fff;                            /* Nền TRẮNG che nội dung bên dưới */
}

.form-control:focus {
  outline: none;                               /* Tắt viền xanh mặc định của trình duyệt */
  border-color: var(--gold-matte);              /* Đổi màu viền thành VÀNG HOÀNG KIM khi bấm vào */
  box-shadow: 0 0 0 2px rgba(204, 161, 94, 0.2);/* Vầng sáng vàng nhẹ xung quanh */
}

/* =========================================================================
   [CSS TỪNG NÚT BẤM CỤ THỂ]
========================================================================= */

/* --- 1. NÚT DẤU X ĐÓNG MODAL (GÓC TRÊN BÊN PHẢI HỘP THOẠI) --- */
.btn-close {
  background: none;                  /* Không màu nền */
  border: none;                      /* Không viền */
  font-size: 18px;                   /* Cỡ chữ dấu X */
  cursor: pointer;                   /* Chuột hình bàn tay */
  color: #888;                       /* Màu mặc định: XÁM NHẸ */
  padding: 4px;
}
.btn-close:hover {
  color: #333;                       /* Đổi sang MÀU ĐEN khi rê chuột vào */
}

/* --- 2. NÚT 'HỦY BỎ' / 'ĐÓNG CỬA SỔ' (NẰM DƯỚI ĐÁY CÁC MODAL) --- */
.btn-cancel {
  background: #f4f4f4;               /* Màu nền: XÁM TRẮNG NHẠT */
  border: 1px solid #ddd;            /* Màu viền: XÁM BẠC NHẠT */
  padding: 8px 16px;                 /* Kích thước nút: Cao 8px, Rộng 16px */
  border-radius: 4px;                /* Bo góc nhẹ 4px */
  cursor: pointer;                   /* Chuột hình bàn tay */
  color: #555;                       /* Màu chữ: XÁM ĐẬM */
  font-weight: 500;                  /* Độ đậm vừa */
}
.btn-cancel:hover {
  background: #e9e9e9;               /* Nền chuyển sang XÁM TỐI HƠN khi rê chuột vào */
}

/* --- 3. NÚT 'XÁC NHẬN HỦY ĐƠN' (MÀU ĐỎ CẢNH BÁO TRONG MODAL HỦY) --- */
.btn-submit-cancel {
  background: #dc3545;               /* Màu nền: MÀU ĐỎ CẢNH BÁO */
  color: white;                      /* Màu chữ: MÀU TRẮNG TINH */
  border: none;                      /* Không viền */
  padding: 8px 16px;                 /* Kích thước: Cao 8px, Rộng 16px */
  border-radius: 4px;                /* Bo nhẹ góc 4px */
  cursor: pointer;                   /* Chuột hình bàn tay */
  font-weight: 500;
}
.btn-submit-cancel:hover {
  background: #c82333;               /* Nền chuyển sang MÀU ĐỎ ĐẬM khi rê chuột vào */
}

/* --- 4. NÚT 'XÁC NHẬN THÊM' (MÀU XANH LÁ TRONG MODAL THÊM PHƯƠNG THỨC) --- */
.btn-submit-success {
  background: #198754;               /* Màu nền: MÀU XANH LÁ CÂY */
  color: white;                      /* Màu chữ: MÀU TRẮNG TINH */
  border: none;                      /* Không viền */
  padding: 8px 16px;                 /* Kích thước: Cao 8px, Rộng 16px */
  border-radius: 4px;                /* Bo góc 4px */
  cursor: pointer;                   /* Chuột hình bàn tay */
  font-weight: 500;
  transition: 0.2s;
}
.btn-submit-success:hover {
  background: #157347;               /* Nền chuyển sang MÀU XANH LÁ ĐẬM khi rê chuột vào */
}

/* --- 5. NÚT DẤU CỘNG (+) THÊM PHƯƠNG THỨC THANH TOÁN (TRÊN BẢNG TABLE) --- */
.btn-add-payment-method {
  background: var(--gold-matte);     /* Màu nền: MÀU VÀNG HOÀNG KIM VELORA */
  color: white;                      /* Màu dấu cộng (+): MÀU TRẮNG */
  border: none;                      /* Không viền */
  width: 28px;                       /* Chiều rộng 28px */
  height: 28px;                      /* Chiều cao 28px (Hình vuông) */
  border-radius: 4px;                /* Bo góc nhẹ 4px */
  cursor: pointer;                   /* Chuột hình bàn tay */
  font-weight: bold;                 /* Dấu cộng in đậm */
}
.btn-add-payment-method:hover {
  background: #b88d4c;               /* Nền chuyển sang VÀNG ĐẬM khi rê chuột vào */
}

/* =========================================================================
   [CSS POPUP XÁC NHẬN CHUYỂN TRẠNG THÁI (CONFIRM DIALOG)]
========================================================================= */
.custom-dialog-box {
  width: 420px !important;           /* Chiều rộng: 420px */
  padding: 30px 25px;                /* Đệm: trên-dưới 30px, trái-phải 25px */
  text-align: center;                /* Căn toàn bộ chữ ra CHÍNH GIỮA */
  border-radius: 8px;                /* Bo góc hộp 8px */
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2); /* Đổ bóng đen nổi bật */
  animation: scaleIn 0.25s ease;     /* Hiệu ứng phóng to nhẹ trong 0.25 giây */
}

.dialog-icon-wrapper {
  width: 60px;                       /* Chiều rộng vòng tròn: 60px */
  height: 60px;                      /* Chiều cao vòng tròn: 60px */
  border-radius: 50%;                /* Bo tròn 100% thành hình tròn */
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;               /* Căn giữa và cách chữ bên dưới 16px */
  font-size: 26px;                   /* Cỡ biểu tượng icon */
}
.dialog-icon-wrapper.gold {
  background-color: #fcf6eb;         /* Màu nền: VÀNG KEM NHẠT */
  color: var(--gold-matte);          /* Màu biểu tượng: VÀNG HOÀNG KIM */
}
.dialog-icon-wrapper.danger {
  background-color: #fde8e8;         /* Màu nền: ĐỎ HỒNG NHẠT */
  color: #dc3545;                    /* Màu biểu tượng: ĐỎ TƯƠI */
}

.dialog-title {
  margin: 0 0 10px 0;
  font-size: 19px;
  color: var(--wood-dark);           /* Màu chữ tiêu đề: NÂU GỖ ĐẬM */
  font-weight: 700;
}

.dialog-message {
  margin: 0 0 24px 0;
  font-size: 14px;
  color: #666;                       /* Màu thông điệp: XÁM */
  line-height: 1.5;
}

.dialog-actions {
  display: flex;
  justify-content: center;           /* Căn 2 nút ra CHÍNH GIỮA */
  gap: 12px;
}

.btn-dialog-cancel {
  background: #f1f1f1;               /* Nền nút: XÁM NHẠT */
  border: 1px solid #ddd;            /* Viền: XÁM */
  color: #555;                       /* Chữ: XÁM ĐẬM */
  padding: 9px 20px;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-dialog-cancel:hover {
  background: #e4e4e4;
}

.btn-dialog-confirm {
  padding: 9px 22px;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  color: #fff;                       /* Chữ: MÀU TRẮNG TINH */
  transition: 0.2s;
}
.btn-gold-confirm {
  background-color: var(--gold-matte);/* Nền nút xác nhận: MÀU VÀNG */
}
.btn-gold-confirm:hover {
  background-color: #b88d4c;
}
.btn-danger-confirm {
  background-color: #dc3545;         /* Nền nút hủy đơn: MÀU ĐỎ */
}
.btn-danger-confirm:hover {
  background-color: #bd2130;
}

/* =========================================================================
   [CSS TOAST THÔNG BÁO TỰ BIẾN MẤT (VỊ TRÍ & MÀU SẮC TOAST)]
========================================================================= */
.custom-alert-toast {
  position: fixed;                   /* Cố định vị trí trên màn hình */
  bottom: 30px;                      /* Cách mép dưới màn hình 30px */
  right: 30px;                       /* Cách mép bên phải 30px (GÓC DƯỚI PHẢI) */
  padding: 14px 22px;                /* Khoảng cách đệm trong Toast */
  border-radius: 6px;                /* Bo góc 6px */
  background: #333;                  /* Màu nền mặc định: ĐEN XÁM */
  color: #fff;                       /* Màu chữ bên trong: MÀU TRẮNG */
  font-size: 14px;                   /* Cỡ chữ */
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;                         /* Cách icon và chữ 10px */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); /* Đổ bóng đen */
  z-index: 9999;                     /* Nổi lên trên cùng */
  transform: translateY(100px);      /* Vị trí ẩn: Thụt xuống dưới 100px */
  opacity: 0;                        /* Độ trong suốt bằng 0 */
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55); /* Hiệu ứng nảy */
}

.custom-alert-toast.show {
  transform: translateY(0);          /* Trượt lên vị trí hiển thị chuẩn */
  opacity: 1;                        /* Hiện rõ 100% */
}

.custom-alert-toast.success {
  background: #198754;               /* Nền thông báo thành công: MÀU XANH LÁ */
}

.custom-alert-toast.error {
  background: #dc3545;               /* Nền thông báo lỗi: MÀU ĐỎ */
}

@keyframes scaleIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>