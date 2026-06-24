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
                <button class="btn-invoice-action" @click="selectOrder(order)">
                  <i class="fa-solid fa-file-invoice"></i> Xem & Xuất Bill
                </button>
              </td>
            </tr>
            <tr v-if="orders.length === 0">
              <td colspan="5" class="empty-state">Đang tải dữ liệu đơn hàng...</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="preview-section" v-if="selectedOrder">
        <div class="preview-header">
          <h3><i class="fa-solid fa-eye"></i> Bản Xem Trước Hóa Đơn</h3>
          <button class="btn-print" @click="triggerPrint">
            <i class="fa-solid fa-print"></i> In / Xuất PDF Ngay
          </button>
        </div>
        <p class="preview-hint">Mẹo: Cuộn xuống dưới để xem toàn bộ biểu mẫu trước khi bấm nút xuất file.</p>
      </section>
    </main>

    <div class="print-area" v-if="selectedOrder">
      <div class="invoice-box">
        <div class="invoice-header">
          <div class="company-logo">
            <h2>VELORA CLOCK</h2>
            <p>Nghệ Thuật Khắc Họa Thời Gian Cao Cấp</p>
          </div>
          <div class="invoice-title">
            <h1>HÓA ĐƠN BÁN HÀNG</h1>
            <p class="invoice-id">Số: <strong>{{ selectedOrder.maDonHangCode }}</strong></p>
            <p>Ngày xuất: {{ getTodayDate() }}</p>
          </div>
        </div>

        <div class="divider-gold"></div>

        <div class="invoice-details-grid">
          <div class="details-block">
            <h4>ĐƠN VỊ BÁN HÀNG</h4>
            <p><strong>Công ty TNHH Velora Clock Việt Nam</strong></p>
            <p>Địa chỉ: 81 Phan Kế Bính, phường Đa Kao, quận 1, Hồ Chi Minh</p>
            <p>Hotline: 1900 VELORA</p>
            <p>Email: contact@velora.com</p>
          </div>
          <div class="details-block">
            <h4>KHÁCH HÀNG NHẬN HÀNG</h4>
            <p><strong>Tên người nhận:</strong> {{ selectedOrder.tenNguoiNhan }}</p>
            <p><strong>Số điện thoại:</strong> {{ selectedOrder.soDienThoaiGiaoHang }}</p>
            <p><strong>Địa chỉ giao:</strong> {{ selectedOrder.diaChiGiaoHang }}</p>
            <p><strong>Hình thức:</strong> {{ selectedOrder.phuongThucThanhToan }}</p>
          </div>
        </div>

        <table class="invoice-table">
          <thead>
            <tr>
              <th style="width: 50px;">STT</th>
              <th>Tên Sản Phẩm / Cỗ Máy</th>
              <th style="text-align: center; width: 100px;">Số Lượng</th>
              <th style="text-align: right; width: 180px;">Đơn Giá</th>
              <th style="text-align: right; width: 200px;">Thành Tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in selectedOrder.items || mockItems" :key="index">
              <td style="text-align: center;">{{ index + 1 }}</td>
              <td><strong>{{ item.tenSanPham }}</strong></td>
              <td style="text-align: center;">{{ item.soLuong }}</td>
              <td style="text-align: right;">{{ formatPrice(item.giaLucMua) }}</td>
              <td style="text-align: right; font-weight: bold;">{{ formatPrice(item.soLuong * item.giaLucMua) }}</td>
            </tr>
            
            <tr class="summary-row">
              <td colspan="3" class="no-border"></td>
              <td class="summary-label">Tổng tiền hàng:</td>
              <td class="summary-value">{{ formatPrice(selectedOrder.tongTien) }}</td>
            </tr>
            <tr class="summary-row">
              <td colspan="3" class="no-border"></td>
              <td class="summary-label">Phí vận chuyển (Bảo hiểm):</td>
              <td class="summary-value">Miễn phí</td>
            </tr>
            <tr class="summary-row total-final">
              <td colspan="3" class="no-border"></td>
              <td class="summary-label">TỔNG CỘNG PHẢI TT:</td>
              <td class="summary-value gold-text">{{ formatPrice(selectedOrder.tongTien) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="invoice-signatures">
          <div class="sig-block">
            <p><strong>Khách hàng người mua</strong></p>
            <span>(Ký và ghi rõ họ tên)</span>
          </div>
          <div class="sig-block text-center">
            <p><strong>Người lập hóa đơn</strong></p>
            <span>(Ký, đóng dấu và ghi rõ họ tên)</span>
            <div class="sig-space"></div>
            <p style="text-transform: uppercase; font-weight: bold;">Velora Clock Admin</p>
          </div>
        </div>

        <div class="invoice-footer">
          <p>Cảm ơn quý khách đã tin tưởng và lựa chọn cỗ máy thời gian tại Velora Clock!</p>
          <p class="italic">* Hóa đơn này có giá trị pháp lý làm căn cứ bảo hành chính hãng độc quyền.</p>
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
];

const orders = ref([]);
const selectedOrder = ref(null);

// Dữ liệu sản phẩm dự phòng nếu API đơn hàng chưa kịp lặp mảng chi tiết sản phẩm
const mockItems = ref([
  { tenSanPham: 'Đồng hồ cao cấp Velora Edition (Mặc định đơn)', soLuong: 1, giaLucMua: 0 }
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
    const res = await fetch('http://localhost:8080/api/don-hang');
    if (res.ok) {
      orders.value = await res.json();
      // Mặc định chọn đơn đầu tiên nếu có dữ liệu để hiển thị preview luôn
      if (orders.value.length > 0) {
        selectOrder(orders.value[0]);
      }
    }
  } catch (error) {
    console.error('Lỗi lấy đơn hàng:', error);
  }
};

const selectOrder = (order) => {
  selectedOrder.value = order;
  // Cập nhật giá sản phẩm mẫu khớp với tổng tiền đơn hàng để hóa đơn nhìn logic
  mockItems.value[0].giaLucMua = order.tongTien;
  mockItems.value[0].tenSanPham = `Cỗ máy thời gian cao cấp (Mã đơn: ${order.maDonHangCode})`;
};

const triggerPrint = () => {
  window.print(); // Kích hoạt lệnh in hệ thống -> Xuất PDF cực chuẩn
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
/* ================= CSS GIAO DIỆN ADMIN CHUẨN LUXURY ================= */
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }
.sidebar { width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; display: flex; flex-direction: column; flex-shrink: 0; }
.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { color: #ccc; text-decoration: none !important; display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: 6px; transition: 0.3s; }
.menu a:hover, .menu a.active { color: #d1aa68; background-color: rgba(209, 170, 104, 0.1); }
.sidebar-bottom { margin-top: auto; border-top: 1px solid #5a4b44; padding-top: 20px; }
.exit, .logout { color: #aaa; background: none; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 10px; margin-bottom: 15px; text-decoration: none !important; }

.content { flex: 1; padding: 40px 60px; min-width: 0; }
.gold { color: #d1aa68; }
.header { margin-bottom: 30px; }
.header h1 { color: #3e332e; font-size: 32px; margin: 0 0 5px 0; }
.header p { color: #888; font-size: 14px; margin: 0; }

.table-container { background: #ffffff; border: 1px solid #e0dcd5; border-radius: 8px; overflow: hidden; margin-bottom: 40px; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.02); }
.admin-table { width: 100%; border-collapse: collapse; text-align: left; }
.admin-table th { background-color: #fcfbf9; color: #3e332e; padding: 15px 20px; font-size: 13px; text-transform: uppercase; border-bottom: 2px solid #e0dcd5; letter-spacing: 1px; }
.admin-table td { padding: 14px 20px; border-bottom: 1px solid #f0efeb; vertical-align: middle; color: #555; font-size: 14px; }
.admin-table tbody tr:hover { background-color: #fdfaf5; }

.order-code { font-weight: bold; color: #3e332e; }
.customer-info { display: flex; flex-direction: column; gap: 2px; }
.customer-info strong { color: #111; }
.customer-info span { font-size: 12px; color: #888; }
.price { font-weight: bold; color: #d1aa68; }

.payment-badge { padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: bold; }
.paid { background-color: #e8f5e9; color: #2e7d32; }
.unpaid { background-color: #fff3e0; color: #e65100; }

.btn-invoice-action { background-color: #3e332e; color: #d1aa68; border: 1px solid #d1aa68; padding: 8px 14px; font-weight: bold; border-radius: 4px; cursor: pointer; transition: 0.2s; }
.btn-invoice-action:hover { background-color: #d1aa68; color: #fff; }

/* Khu vực Preview trên màn hình web */
.preview-section { background: #fff; border: 1px dashed #d1aa68; padding: 20px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.preview-header h3 { margin: 0; color: #3e332e; }
.preview-hint { font-size: 12px; color: #999; margin: 5px 0 0 0; }
.btn-print { background-color: #7c9363; color: #fff; border: none; padding: 12px 24px; font-weight: bold; border-radius: 4px; cursor: pointer; font-size: 15px; transition: 0.2s; }
.btn-print:hover { background-color: #667c50; }

.print-area { background: #fff; border: 1px solid #e0dcd5; padding: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); max-width: 850px; margin: 0 auto 50px auto; }

/* ================= CSS THIẾT KẾ PHÔI HÓA ĐƠN ĐỒ HIỆU ================= */
.invoice-box { font-family: 'Arial', sans-serif; color: #222; line-height: 1.6; }
.invoice-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.company-logo h2 { color: #3e332e; letter-spacing: 2px; margin: 0; font-size: 24px; }
.company-logo p { margin: 5px 0 0 0; font-size: 12px; color: #888; text-transform: uppercase; }
.invoice-title { text-align: right; }
.invoice-title h1 { font-size: 26px; color: #3e332e; margin: 0 0 5px 0; font-weight: bold; letter-spacing: 1px; }
.invoice-id { font-size: 16px; margin: 0; color: #555; }
.invoice-title p { margin: 3px 0; font-size: 13px; color: #666; }

.divider-gold { height: 2px; background-color: #d1aa68; margin-bottom: 25px; }

.invoice-details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; margin-bottom: 35px; }
.details-block h4 { font-size: 12px; color: #888; text-transform: uppercase; letter-spacing: 1px; margin: 0 0 10px 0; border-bottom: 1px solid #eee; padding-bottom: 5px; }
.details-block p { margin: 5px 0; font-size: 14px; color: #333; }

.invoice-table { width: 100%; border-collapse: collapse; margin-bottom: 40px; }
.invoice-table th { background: #f9f8f6; border: 1px solid #ddd; padding: 12px; font-size: 13px; text-transform: uppercase; color: #444; }
.invoice-table td { border: 1px solid #ddd; padding: 12px; font-size: 14px; }

.summary-row td { padding: 8px 12px; border: 1px solid #ddd; }
.summary-row .no-border { border: none !important; background: none; }
.summary-label { font-weight: bold; text-align: right; background: #fafafa; }
.summary-value { text-align: right; font-weight: 500; }
.total-final { font-size: 16px; font-weight: bold; }
.total-final .summary-label { background: #3e332e !important; color: #fff; }
.total-final .summary-value { background: #fdfaf5; border: 2px solid #d1aa68; }
.gold-text { color: #b8955b; font-size: 18px; }

.invoice-signatures { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; margin: 50px 0; }
.sig-block { text-align: center; font-size: 14px; }
.sig-block span { font-size: 12px; color: #777; font-style: italic; }
.sig-space { height: 80px; }

.invoice-footer { text-align: center; border-top: 1px dashed #ddd; padding-top: 25px; margin-top: 40px; }
.invoice-footer p { margin: 5px 0; font-size: 13px; color: #555; }
.invoice-footer .italic { font-style: italic; color: #999; font-size: 12px; }

/* ================= BỘ CSS QUYẾT ĐỊNH ĐỂ XUẤT RA FILE PDF CHUẨN ================= */
@media print {
  /* 1. Ẩn sạch sành sanh các thành phần thừa trên giao diện quản lý */
  .no-print, .sidebar, .content, header, button, .preview-section {
    display: none !important;
  }
  
  /* 2. Cấu hình lại layout bọc ngoài để phôi hóa đơn bung ra tràn trang giấy A4 */
  .admin-wrapper {
    background: #fff !important;
    display: block !important;
  }
  
  .print-area {
    border: none !important;
    box-shadow: none !important;
    padding: 0 !important;
    margin: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    display: block !important;
  }
  
  /* Đảm bảo trình duyệt in luôn cả màu nền xám nhạt của Header Table */
  .invoice-table th {
    background-color: #f9f8f6 !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .total-final .summary-label {
    background-color: #3e332e !important;
    color: #fff !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>