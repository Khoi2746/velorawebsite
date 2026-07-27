<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- KHỐI KHÔNG IN (GIAO DIỆN WEB QUẢN TRỊ) -->
    <div class="no-print layout-container">
      <!-- 1. GỌI COMPONENT SIDEBAR -->
      <AdminSidebar :isCollapsed="isCollapsed" />

      <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
        <!-- 2. GỌI COMPONENT HEADER -->
        <AdminHeader @toggle-sidebar="toggleSidebar" />

        <!-- 3. NỘI DUNG CHÍNH -->
        <main class="content">
          <header class="header">
            <div class="header-left">
              <h1>Xuất <span class="gold">Hóa Đơn</span></h1>
              <p>Chọn đơn hàng đã thanh toán để tiến hành in hóa đơn hoặc xuất file PDF.</p>
            </div>
          </header>

          <section class="table-container">
            <div class="table-responsive">
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
            </div>
          </section>
        </main>
      </div>
    </div>

    <!-- KHỐI CHỈ DÀNH CHO IN ẤN (INVOICE) -->
    <div class="print-area print-only" v-if="selectedOrder">
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

// IMPORT COMPONENT CON 
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

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

const selectAndPrint = async (order) => {
  selectedOrder.value = order;
  mockItems.value[0].giaLucMua = order.tongTien;
  mockItems.value[0].tenSanPham = `Cỗ máy thời gian cao cấp (Mã đơn: ${order.maDonHangCode})`;
  await nextTick();
  window.print();
};

onMounted(() => {
  loadOrders();
});
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

@media print {
  .no-print, .layout-container {
    display: none !important;
  }
  .print-area, .print-only {
    display: block !important;
  }
  .velora-admin-wrapper {
    background: white !important;
    height: auto !important;
    width: 100% !important;
    overflow: visible !important;
  }
}
</style>

<style scoped>
@import "../CSS/Admin/XuatHoaDon.css";

/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI (ĐÃ FIX TEO DÀN TRANG)
   ============================================== */
.velora-admin-wrapper {
  background-color: var(--bg-page);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.layout-container {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.content-wrapper {
  flex: 1;
  min-width: 0; /* THẦN CHÚ ÉP FLEXBOX KHÔNG BỊ BÓP MÉO */
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: var(--bg-page);
}

.content {
  flex: 1;
  padding: 30px;
  width: 100%;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  width: 100%;
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

/* FIX BẢNG DANH SÁCH ĐƠN HÀNG FULL CHIỀU NGANG */
.table-container {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid var(--border-light);
  margin-top: 20px;
  width: 100%;
  box-sizing: border-box;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px; /* Tránh bảng bị ép nhỏ quá */
}

.admin-table th {
  text-align: left;
  padding: 15px;
  background-color: #faf9f6;
  color: var(--wood-dark);
  font-weight: 600;
  border-bottom: 2px solid var(--border-light);
  text-transform: uppercase;
  font-size: 13px;
  white-space: nowrap;
}

.admin-table td {
  padding: 15px;
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
  color: var(--text-main);
}

.admin-table tbody tr:hover {
  background-color: #fcfcfc;
}

.empty-state {
  text-align: center !important;
  padding: 40px !important;
  color: var(--text-muted);
  font-style: italic;
}
</style>