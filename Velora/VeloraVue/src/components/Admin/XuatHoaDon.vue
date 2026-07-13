<template>
  <div class="admin-wrapper">
    <nav class="sidebar no-print">
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

    <main class="content no-print">
      <header class="header">
        <div class="header-left">
          <h1>Xuất <span class="gold">Hóa Đơn</span></h1>
          <p>Chọn đơn hàng đã thanh toán để tiến hành in hóa đơn hoặc xuất file PDF.</p>
        </div>
      </header>

      <section class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>Mã Đơn Hàng</th>
              <th>Khách Hàng</th>
              <th>Tổng Tiền</th>
              <th>Thanh Toán</th>
              <th style="text-align: center;">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.maDonHang">
              <td class="order-code">{{ order.maDonHangCode }}</td>
              <td>
                <div class="customer-info">
                  <strong>{{ order.tenNguoiNhan }}</strong>
                  <span>{{ order.soDienThoaiGiaoHang }}</span>
                </div>
              </td>
              <td class="price">{{ formatPrice(order.tongTien) }}</td>
              <td>
                <span class="payment-badge" :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'paid' : 'unpaid'">
                  {{ order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán' }}
                </span>
              </td>
              <td style="text-align: center;">
                <button class="btn-print" @click="selectAndPrint(order)">
                  <i class="fa-solid fa-print"></i> In / Xuất PDF
                </button>
              </td>
            </tr>
            <tr v-if="orders.length === 0">
              <td colspan="5" class="empty-state">Đang tải dữ liệu đơn hàng...</td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>

    <div class="print-area" v-if="selectedOrder">
      <div class="invoice-box">
        <div class="invoice-header">
          <div class="company-logo">
            <h2>VELORA CLOCK</h2>
            <p>Tuyệt tác thời gian - Đẳng cấp thượng lưu</p>
          </div>
          <div class="invoice-title">
            <h1>HÓA ĐƠN BÁN HÀNG</h1>
            <h3 class="invoice-id">Mã số: {{ selectedOrder?.maDonHangCode }}</h3>
            <p>Ngày lập: {{ getTodayDate() }}</p>
          </div>
        </div>

        <div class="divider-gold"></div>

        <div class="invoice-details-grid">
          <div class="details-block">
            <h4>Thông Tin Khách Hàng</h4>
            <p><strong>Khách hàng:</strong> {{ selectedOrder?.tenNguoiNhan }}</p>
            <p><strong>Điện thoại:</strong> {{ selectedOrder?.soDienThoaiGiaoHang }}</p>
            <p><strong>Địa chỉ giao hàng:</strong> {{ selectedOrder?.diaChiGiaoHang }}</p>
          </div>
          <div class="details-block">
            <h4>Thông Tin Thanh Toán</h4>
            <p><strong>Phương thức:</strong> Chuyển khoản ngân hàng</p>
            <p><strong>Trạng thái đơn:</strong> {{ selectedOrder?.trangThaiDonHang }}</p>
            <p><strong>Tình trạng thanh toán:</strong>
              <span
                :style="{ color: selectedOrder?.trangThaiThanhToan === 'DA_THANH_TOAN' ? '#2e7d32' : '#c62828', fontWeight: 'bold' }">
                {{ selectedOrder?.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã thanh toán' : 'Chưa thanh toán' }}
              </span>
            </p>
          </div>
        </div>

        <table class="invoice-table">
          <thead>
            <tr>
              <th style="width: 5%; text-align: center;">STT</th>
              <th style="width: 50%;">Tên Sản Phẩm</th>
              <th style="width: 15%; text-align: center;">Số Lượng</th>
              <th style="width: 30%; text-align: right;">Thành Tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in mockItems" :key="index">
              <td style="text-align: center;">{{ index + 1 }}</td>
              <td><strong>{{ item.tenSanPham }}</strong></td>
              <td style="text-align: center;">{{ item.soLuong }}</td>
              <td style="text-align: right;">{{ formatPrice(item.giaLucMua) }}</td>
            </tr>
            <tr class="summary-row">
              <td colspan="2" class="no-border"></td>
              <td class="summary-label">Cộng tiền hàng:</td>
              <td class="summary-value">{{ formatPrice(selectedOrder?.tongTien) }}</td>
            </tr>
            <tr class="summary-row">
              <td colspan="2" class="no-border"></td>
              <td class="summary-label">Giảm giá / Voucher:</td>
              <td class="summary-value">0 ₫</td>
            </tr>
            <tr class="summary-row total-final">
              <td colspan="2" class="no-border"></td>
              <td class="summary-label">TỔNG THANH TOÁN:</td>
              <td class="summary-value gold-text">{{ formatPrice(selectedOrder?.tongTien) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="invoice-signatures">
          <div class="sig-block">
            <strong>Khách hàng</strong>
            <br><span>(Ký, ghi rõ họ tên)</span>
            <div class="sig-space"></div>
          </div>
          <div class="sig-block">
            <strong>Đại diện Velora Clock</strong>
            <br><span>(Ký, ghi rõ họ tên)</span>
            <div class="sig-space"></div>
          </div>
        </div>

        <div class="invoice-footer">
          <p>Cảm ơn Quý khách đã tin tưởng và lựa chọn sản phẩm của Velora Clock!</p>
          <p class="italic">Mọi thắc mắc về đơn hàng, vui lòng liên hệ Hotline: 1900 6868 hoặc truy cập veloraclock.vn
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';

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
const selectedOrder = ref(null);

const mockItems = ref([
  { tenSanPham: 'Đồng hồ cao cấp Velora Edition', soLuong: 1, giaLucMua: 0 }
]);

const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const getTodayDate = () => {
  const today = new Date();
  return today.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const loadOrders = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/don-hang');
    orders.value = res.data;
  } catch (error) {
    console.error('Lỗi lấy đơn hàng:', error);
  }
};

// Hàm gộp: Chọn đơn hàng -> Cập nhật DOM -> GỌi lệnh In
const selectAndPrint = async (order) => {
  // 1. Gán dữ liệu vào hóa đơn ẩn
  selectedOrder.value = order;
  mockItems.value[0].giaLucMua = order.tongTien;
  mockItems.value[0].tenSanPham = `Cỗ máy thời gian cao cấp (Mã đơn: ${order.maDonHangCode})`;

  // 2. Chờ Vue render cái div hóa đơn ẩn ra HTML xong xuôi
  await nextTick();

  // 3. Gọi hộp thoại máy in (Ctrl + P)
  window.print();
};

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/';
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
@import "../CSS/Admin/XuatHoaDon.css";
</style>