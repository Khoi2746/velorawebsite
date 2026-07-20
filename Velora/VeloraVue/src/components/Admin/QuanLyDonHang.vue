<template>
  <div class="admin-wrapper">
    <nav class="sidebar">
      <h2 class="brand">VELORA ADMIN</h2>
      <ul class="menu">
        <li v-for="item in menuItems" :key="item.name">
          <router-link :to="item.link" active-class="active">
            <i :class="item.icon"></i> {{ item.name }}
          </router-link>
        </li>
      </ul>
      <div class="sidebar-bottom">
        <router-link to="/" class="exit"><i class="fa-solid fa-house"></i> Return</router-link>
        <button class="logout" @click="handleLogout"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
      </div>
    </nav>

    <main class="content">
      <header class="header">
        <div class="header-left">
          <h1>Quản Lý <span class="gold">Đơn Hàng</span></h1>
          <p>Theo dõi, luân chuyển trạng thái và xử lý các giao dịch giao hàng.</p>
        </div>
      </header>

      <!-- KHU VỰC THANH TÌM KIẾM & BỘ LỌC ĐA NĂNG -->
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
          <label for="filterDate" class="label-date"><i class="fa-solid fa-calendar-days"></i> Lọc ngày:</label>
          <input 
            type="date" 
            id="filterDate" 
            v-model="filterDate" 
            class="input-date"
          />
          <button v-if="filterDate" class="btn-clear-date" title="Xóa lọc ngày" @click="filterDate = ''">
            <i class="fa-solid fa-circle-xmark"></i>
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
              <th style="width: 160px;">Hành Động</th>
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
                      {{ 
                        method === 'CHUYEN_KHOAN_QR' ? 'Chuyển Khoản QR' : 
                        method === 'THE_TIN_DUNG' ? 'Thẻ Tín Dụng' : 
                        method === 'VNPAY' ? 'Cổng VNPAY' : method 
                      }}
                    </option>
                  </select>
                  <button class="btn-add-payment-method" title="Tạo thêm hình thức thanh toán mới" @click="openAddPaymentModal">+</button>
                </div>
                
                <div style="margin-top: 6px;">
                  <span class="payment-status" :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'paid' : 'unpaid'">
                    <i :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'fa-solid fa-check-circle' : 'fa-solid fa-clock'"></i>
                    {{ order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán' }}
                  </span>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(order.trangThaiDonHang)">
                  {{ getStatusText(order.trangThaiDonHang) }}
                </span>
              </td>
              <td>
                <select 
                  class="action-select" 
                  value=""
                  @change="handleActionSelect(order, $event)"
                >
                  <option value="" disabled selected>-- Chọn lệnh --</option>
                  <option value="VIEW">👁️ Xem chi tiết</option>
                  
                  <option 
                    v-if="order.trangThaiDonHang === 'CHO_XU_LY'" 
                    value="SHIPPING"
                  >
                    🚚 Giao hàng
                  </option>
                  
                  <option 
                    v-if="order.trangThaiDonHang === 'DANG_GIAO'" 
                    value="APPROVE"
                  >
                    ✅ Hoàn thành giao
                  </option>
                  
                  <option 
                    v-if="order.trangThaiDonHang === 'CHO_XU_LY' || order.trangThaiDonHang === 'DANG_GIAO'" 
                    value="CANCEL"
                    class="option-danger"
                  >
                    ❌ Hủy đơn hàng
                  </option>
                </select>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="7" class="empty-state">Không tìm thấy đơn hàng nào khớp với bộ lọc...</td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>

    <!-- MODAL XEM CHI TIẾT ĐƠN HÀNG -->
    <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <h2>Chi Tiết Đơn Hàng <span class="gold">#{{ selectedOrder?.maDonHangCode }}</span></h2>
          <button class="btn-close" @click="closeDetailModal"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <div class="order-info-grid">
            <div class="info-card">
              <h4><i class="fa-solid fa-location-dot"></i> Thông Tin Giao Hàng</h4>
              <p><strong>Người nhận:</strong> {{ selectedOrder?.tenNguoiNhan }}</p>
              <p><strong>Điện thoại:</strong> {{ selectedOrder?.soDienThoaiGiaoHang }}</p>
              <p><strong>Địa chỉ:</strong> {{ selectedOrder?.diaChiGiaoHang }}</p>
            </div>
            <div class="info-card">
              <h4><i class="fa-solid fa-credit-card"></i> Thông Tin Thanh Toán</h4>
              <p><strong>Hình thức:</strong> {{ selectedOrder?.phuongThucThanhToan }}</p>
              <p><strong>Tình trạng:</strong>
                <span :class="selectedOrder?.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'text-success' : 'text-danger'">
                  {{ selectedOrder?.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                </span>
              </p>
              <p><strong>Tổng cộng:</strong> <span class="price-large">{{ formatPrice(selectedOrder?.tongTien) }}</span>
              </p>
            </div>
          </div>
          <h4 class="table-title"><i class="fa-solid fa-box-open"></i> Danh Sách Sản Phẩm (Chi Tiết)</h4>
          <table class="detail-table">
            <thead>
              <tr>
                <th>Mã SP</th>
                <th>Đơn Giá Lúc Mua</th>
                <th style="text-align: center;">Số Lượng</th>
                <th style="text-align: right;">Thành Tiền</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in orderDetails" :key="item.maChiTietDonHang">
                <td><strong>SP #{{ item.maSanPham }}</strong></td>
                <td>{{ formatPrice(item.giaLucMua) }}</td>
                <td style="text-align: center;"><span class="qty-badge">{{ item.soLuong }}</span></td>
                <td style="text-align: right; font-weight: bold; color: #d1aa68;">{{ formatPrice(item.giaLucMua *
                  item.soLuong) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeDetailModal">Đóng</button>
        </div>
      </div>
    </div>

    <!-- MODAL POPUP THÊM HÌNH THỨC THANH TOÁN MỚI -->
    <div class="modal-overlay" v-if="showAddPaymentModal" @click.self="closeAddPaymentModal">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <h2>Thêm <span class="gold">Hình Thức Mới</span></h2>
          <button class="btn-close" @click="closeAddPaymentModal"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group-payment">
            <label for="newPaymentMethod" class="label-payment">Tên hình thức thanh toán:</label>
            <input 
              type="text" 
              id="newPaymentMethod" 
              v-model="newMethodName" 
              placeholder="Ví dụ: MoMo, Trả Góp 0%,..."
              class="input-payment"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-submit-payment" @click="addNewPaymentMethod">Xác Nhận Thêm</button>
          <button class="btn-cancel" @click="closeAddPaymentModal">Hủy</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const menuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group' },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked' },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar' },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem' },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list' },
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags' },
    { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check' }, 
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
    { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
];
const orders = ref([]);
const showDetailModal = ref(false);
const selectedOrder = ref(null);
const orderDetails = ref([]);

const searchQuery = ref('');
const filterDate = ref('');

const paymentMethods = ref(['COD', 'CHUYEN_KHOAN_QR', 'VNPAY', 'THE_TIN_DUNG']);
const showAddPaymentModal = ref(false);
const newMethodName = ref('');

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

const getStatusClass = (status) => {
  switch (status) {
    case 'CHO_XU_LY': return 'status-pending';
    case 'DANG_GIAO': return 'status-shipping';
    case 'DA_GIAO': return 'status-delivered';
    case 'DA_HUY': return 'status-canceled';
    default: return 'status-pending';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'CHO_XU_LY': return 'Chờ Xử Lý';
    case 'DANG_GIAO': return 'Đang Giao';
    case 'DA_GIAO': return 'Đã Giao';
    case 'DA_HUY': return 'Đã Hủy';
    default: return status || 'Chờ Xử Lý';
  }
};

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

const changePaymentMethod = async (id, hinhThucMoi) => {
  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${id}/cap-nhat-thanh-toan?phuongThucMoi=${hinhThucMoi}`, {
      method: 'PATCH'
    });

    if (res.ok) {
      alert("Cập nhật phương thức thanh toán của đơn hàng thành công!");
      loadOrders();
    } else {
      alert("Lỗi cập nhật backend: " + await res.text());
    }
  } catch (error) {
    alert("Không thể đồng bộ hình thức thanh toán mới lên máy chủ.");
  }
};

const handleActionSelect = (order, event) => {
  const selectedAction = event.target.value;
  if (!selectedAction) return;

  if (selectedAction === 'VIEW') {
    viewOrderDetails(order);
  } else if (selectedAction === 'SHIPPING') {
    changeOrderStatus(order.maDonHang, 'DANG_GIAO');
  } else if (selectedAction === 'APPROVE') {
    changeOrderStatus(order.maDonHang, 'DA_GIAO');
  } else if (selectedAction === 'CANCEL') {
    changeOrderStatus(order.maDonHang, 'DA_HUY');
  }

  event.target.value = "";
};

// ĐÃ SỬA: ÉP TRUYỀN THAM SỐ THANH TOÁN TRỰC TIẾP TỪ FRONTEND ĐỂ KHÔNG SỢ LỆCH LOGIC
const changeOrderStatus = async (id, statusMoi) => {
  let thongBao = "Bạn có chắc muốn thực hiện hành động này?";
  let url = `http://localhost:8080/api/don-hang/${id}/trang-thai?trangThaiMoi=${statusMoi}`;

  if (statusMoi === 'DANG_GIAO') {
    thongBao = "Xác nhận chuyển đơn hàng sang trạng thái [ĐANG GIAO]?";
  }
  if (statusMoi === 'DA_GIAO') {
    thongBao = "Xác nhận đã giao hàng thành công?\nHệ thống sẽ tự động chuyển trạng thái thanh toán thành [ĐÃ THANH TOÁN].";
    // Ép thêm param trực tiếp lên URL để Backend nhận lệnh xử lý luôn
    url += `&trangThaiThanhToanMoi=DA_THANH_TOAN`;
  }
  if (statusMoi === 'DA_HUY') {
    thongBao = "CẢNH BÁO: Bạn có chắc chắn muốn [HỦY] đơn hàng này?";
  }

  if (!confirm(thongBao)) return;

  try {
    const res = await fetch(url, {
      method: 'PATCH'
    });

    if (res.ok) {
      alert("Cập nhật trạng thái đơn đặt hàng thành công!");
      loadOrders(); 
    } else {
      alert("Lỗi: " + await res.text());
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
      orderDetails.value = [{ maChiTietDonHang: 1, maSanPham: 99, soLuong: 1, giaLucMua: order.tongTien }];
    }
  } catch (error) {
    orderDetails.value = [{ maChiTietDonHang: 1, maSanPham: 99, soLuong: 1, giaLucMua: order.tongTien }];
  }
};

const closeDetailModal = () => {
  showDetailModal.value = false;
  selectedOrder.value = null;
};

const openAddPaymentModal = () => {
  newMethodName.value = '';
  showAddPaymentModal.value = true;
};
const closeAddPaymentModal = () => {
  showAddPaymentModal.value = false;
};
const addNewPaymentMethod = () => {
  let valueTrimmed = newMethodName.value.trim().toUpperCase();
  valueTrimmed = valueTrimmed.replace(/À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, "A");
  valueTrimmed = valueTrimmed.replace(/È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, "E");
  valueTrimmed = valueTrimmed.replace(/Ì|Í|Ị|Ỉ|Ĩ/g, "I");
  valueTrimmed = valueTrimmed.replace(/Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, "O");
  valueTrimmed = valueTrimmed.replace(/Ù|Ú|Ụ|Ủ|U|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, "U");
  valueTrimmed = valueTrimmed.replace(/Ỳ|Ý|Y|Ỷ|Ỹ/g, "Y");
  valueTrimmed = valueTrimmed.replace(/Đ/g, "D");
  valueTrimmed = valueTrimmed.replace(/[^A-Z0-9\s]/g, '').replace(/\s+/g, '_');

  if (!valueTrimmed) {
    alert("Vui lòng điền tên hình thức thanh toán mới!");
    return;
  }
  if (paymentMethods.value.includes(valueTrimmed)) {
    alert("Hình thức thanh toán này đã tồn tại trong danh sách!");
    return;
  }
  paymentMethods.value.push(valueTrimmed);
  showAddPaymentModal.value = false;
  alert(`Đã thêm thành công [${valueTrimmed}] vào danh sách lựa chọn!`);
};

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/';
};

onMounted(() => { loadOrders(); });
</script>

<style scoped>
@import "../CSS/Admin/QuanLyDonHang.css";
</style>