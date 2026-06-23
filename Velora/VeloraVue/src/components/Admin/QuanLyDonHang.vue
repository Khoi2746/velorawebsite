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
        <div class="header-right">
          <button class="btn-export"><i class="fa-solid fa-file-export"></i> Xuất Báo Cáo</button>
        </div>
      </header>

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
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.maDonHang">
              <td class="order-code">{{ order.maDonHangCode }}</td>
              <td class="customer-info">
                <strong>{{ order.tenNguoiNhan }}</strong>
                <span class="phone">{{ order.soDienThoaiGiaoHang }}</span>
              </td>
              <td>{{ formatDate(order.ngayTao) }}</td>
              <td class="price">{{ formatPrice(order.tongTien) }}</td>
              <td>
                <span class="payment-status" :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'paid' : 'unpaid'">
                  <i
                    :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'fa-solid fa-check-circle' : 'fa-solid fa-clock'"></i>
                  {{ order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(order.trangThaiDonHang)">
                  {{ getStatusText(order.trangThaiDonHang) }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-action view" title="Xem chi tiết" @click="viewOrderDetails(order)">
                  <i class="fa-solid fa-eye"></i>
                </button>

                <button class="btn-action shipping" title="Xác nhận giao hàng (Chuyển thành Đang Giao)"
                  v-if="order.trangThaiDonHang === 'CHO_XU_LY'"
                  @click="changeOrderStatus(order.maDonHang, 'DANG_GIAO')">
                  <i class="fa-solid fa-truck"></i>
                </button>

                <button class="btn-action approve" title="Xác nhận khách đã nhận (Chuyển thành Đã Giao)"
                  v-if="order.trangThaiDonHang === 'DANG_GIAO'" @click="changeOrderStatus(order.maDonHang, 'DA_GIAO')">
                  <i class="fa-solid fa-check"></i>
                </button>

                <button class="btn-action cancel" title="Hủy đơn hàng này"
                  v-if="order.trangThaiDonHang === 'CHO_XU_LY' || order.trangThaiDonHang === 'DANG_GIAO'"
                  @click="changeOrderStatus(order.maDonHang, 'DA_HUY')">
                  <i class="fa-solid fa-xmark"></i>
                </button>
              </td>
            </tr>
            <tr v-if="orders.length === 0">
              <td colspan="7" class="empty-state">Đang tải dữ liệu hoặc chưa có đơn hàng nào...</td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

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
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', requiresAdmin: true }
];
const orders = ref([]);
const showDetailModal = ref(false);
const selectedOrder = ref(null);
const orderDetails = ref([]);

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

// HÀM CHUYỂN ĐỔI TRẠNG THÁI TỔNG HỢP CỰC KỲ THÔNG MINH
const changeOrderStatus = async (id, statusMoi) => {
  let thongBao = "Bạn có chắc muốn thực hiện hành động này?";
  if (statusMoi === 'DANG_GIAO') thongBao = "Xác nhận chuyển đơn hàng sang trạng thái [ĐANG GIAO]?";
  if (statusMoi === 'DA_GIAO') thongBao = "Xác nhận shipper đã giao đơn hàng này [THÀNH CÔNG]?";
  if (statusMoi === 'DA_HUY') thongBao = "CẢNH BÁO: Bạn có chắc chắn muốn [HỦY] đơn hàng này?";

  if (!confirm(thongBao)) return;

  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${id}/trang-thai?trangThaiMoi=${statusMoi}`, {
      method: 'PATCH'
    });

    if (res.ok) {
      alert("Cập nhật trạng thái đơn đặt hàng thành công!");
      loadOrders(); // Tải lại bảng để cập nhật màu và nút bấm realtime
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

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/';
};

onMounted(() => { loadOrders(); });
</script>

<style scoped>
/* ================= LAYOUT VÀ BẢNG GỐC CỦA EM ================= */
.admin-wrapper {
  display: flex;
  min-height: 100vh;
  background: #f4f1ea;
  font-family: sans-serif;
}

.sidebar {
  width: 260px;
  background: #3e332e;
  color: #fff;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.brand {
  font-size: 18px;
  color: #d1aa68;
  margin-bottom: 50px;
  text-align: center;
  letter-spacing: 2px;
}

.menu li {
  margin-bottom: 20px;
  list-style: none;
}

.menu a {
  color: #ccc;
  text-decoration: none !important;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: 0.3s;
  padding: 10px;
  border-radius: 6px;
}

.menu a:hover,
.menu a.active {
  color: #d1aa68;
  background-color: rgba(209, 170, 104, 0.1);
}

.sidebar-bottom {
  margin-top: auto;
  border-top: 1px solid #5a4b44;
  padding-top: 20px;
}

.exit,
.logout {
  color: #aaa;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  text-decoration: none !important;
}

.content {
  flex: 1;
  padding: 60px;
  min-width: 0;
}

.gold {
  color: #d1aa68;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header h1 {
  color: #3e332e;
  font-size: 32px;
  margin-bottom: 5px;
}

.header p {
  color: #888;
  font-size: 14px;
}

.btn-export {
  background-color: #fff;
  color: #3e332e;
  border: 1px solid #d1aa68;
  padding: 12px 24px;
  font-weight: bold;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-export:hover {
  background-color: #d1aa68;
  color: #fff;
}

.table-container {
  background: #ffffff;
  border: 1px solid #e0dcd5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.02);
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.admin-table th {
  background-color: #fcfbf9;
  color: #3e332e;
  padding: 18px 20px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 2px solid #e0dcd5;
}

.admin-table td {
  padding: 15px 20px;
  border-bottom: 1px solid #f0efeb;
  vertical-align: middle;
  color: #555;
  font-size: 14px;
}

.admin-table tbody tr:hover {
  background-color: #fdfaf5;
}

.order-code {
  font-weight: bold;
  color: #3e332e;
  letter-spacing: 0.5px;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-info strong {
  color: #111;
}

.customer-info .phone {
  font-size: 12px;
  color: #888;
}

.price {
  font-weight: bold;
  color: #d1aa68;
}

.payment-status {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}

.payment-status.paid {
  color: #2e7d32;
}

.payment-status.unpaid {
  color: #e65100;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  display: inline-block;
}

.status-pending {
  background-color: #fff8e1;
  color: #f57f17;
}

.status-shipping {
  background-color: #e3f2fd;
  color: #1565c0;
}

/* Màu xanh dương xịn cho đang giao */
.status-delivered {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-canceled {
  background-color: #ffebee;
  color: #c62828;
}

.actions {
  display: flex;
  gap: 8px;
}

/* TINH CHỈNH CSS CHO CÁC NÚT HÀNH ĐỘNG MỚI */
.btn-action {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-action.view {
  background-color: #f4f1ea;
  color: #3e332e;
}

.btn-action.view:hover {
  background-color: #d1aa68;
  color: #fff;
}

.btn-action.shipping {
  background-color: #e3f2fd;
  color: #1565c0;
}

.btn-action.shipping:hover {
  background-color: #1565c0;
  color: #fff;
}

.btn-action.approve {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.btn-action.approve:hover {
  background-color: #2e7d32;
  color: #fff;
}

.btn-action.cancel {
  background-color: #ffebee;
  color: #c62828;
}

.btn-action.cancel:hover {
  background-color: #c62828;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: #888;
}

/* ================= MODAL POPUP CHI TIẾT ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}

.modal-box {
  background: #fff;
  border-radius: 10px;
  width: 500px;
  max-width: 95%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out forwards;
}

.modal-lg {
  width: 750px;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  padding: 20px 25px;
  border-bottom: 1px solid #f0efeb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #faf9f6;
  border-radius: 10px 10px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #3e332e;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #888;
  cursor: pointer;
  transition: 0.2s;
}

.btn-close:hover {
  color: #d1aa68;
}

.modal-body {
  padding: 25px;
  overflow-y: auto;
}

.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 25px;
}

.info-card {
  background: #fdfaf5;
  border: 1px solid #e0dcd5;
  padding: 15px;
  border-radius: 8px;
}

.info-card h4 {
  margin: 0 0 10px 0;
  color: #3e332e;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e0dcd5;
  padding-bottom: 8px;
}

.info-card p {
  margin: 6px 0;
  font-size: 14px;
  color: #555;
}

.info-card p strong {
  color: #333;
  display: inline-block;
  width: 90px;
}

.text-success {
  color: #2e7d32;
  font-weight: bold;
}

.text-danger {
  color: #c62828;
  font-weight: bold;
}

.price-large {
  font-size: 18px;
  font-weight: bold;
  color: #d1aa68;
}

.table-title {
  margin: 0 0 15px 0;
  color: #3e332e;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.detail-table th {
  background: #f4f1ea;
  color: #3e332e;
  padding: 12px;
  font-size: 13px;
  text-transform: uppercase;
  border-bottom: 2px solid #e0dcd5;
  text-align: left;
}

.detail-table td {
  padding: 12px;
  border-bottom: 1px solid #f0efeb;
  font-size: 14px;
  color: #444;
}

.qty-badge {
  background: #3e332e;
  color: #fff;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.modal-footer {
  padding: 15px 25px;
  border-top: 1px solid #f0efeb;
  background: #faf9f6;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-radius: 0 0 10px 10px;
}

.btn-cancel {
  background: #e0dcd5;
  color: #333;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
}

.btn-cancel:hover {
  background: #ccbfb5;
}
</style>