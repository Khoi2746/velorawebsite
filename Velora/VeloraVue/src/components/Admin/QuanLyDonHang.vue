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
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-gem fa-solid' },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list' },
    { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags' },
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
/* CSS DROPDOWN HÀNH ĐỘNG */
.action-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e0dcd5;
  background-color: #faf9f6;
  color: #3e332e;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.action-select:focus {
  border-color: #d1aa68;
  background-color: #fff;
  box-shadow: 0 0 5px rgba(209, 170, 104, 0.2);
}

.option-danger {
  color: #c62828;
  font-weight: bold;
}

/* CSS BỘ LỌC TÌM KIẾM */
.filter-wrapper {
  background: #ffffff;
  border: 1px solid #e0dcd5;
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.01);
}

.search-box {
  flex: 1;
  position: relative;
  max-width: 500px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #a09a95;
  font-size: 15px;
}

.input-search {
  width: 100%;
  padding: 10px 15px 10px 42px;
  border: 1px solid #e0dcd5;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  background-color: #faf9f6;
}

.input-search:focus {
  border-color: #d1aa68;
  background-color: #fff;
  box-shadow: 0 0 5px rgba(209, 170, 104, 0.2);
}

.date-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label-date {
  font-size: 14px;
  font-weight: bold;
  color: #3e332e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-date {
  padding: 9px 12px;
  border: 1px solid #e0dcd5;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background-color: #faf9f6;
  cursor: pointer;
  font-family: sans-serif;
}

.input-date:focus {
  border-color: #d1aa68;
  background-color: #fff;
}

.btn-clear-date {
  background: none;
  border: none;
  color: #c62828;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0;
  transition: color 0.2s;
}

.btn-clear-date:hover {
  color: #e53935;
}

/* CSS GIAO DIỆN CHÍNH */
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

.status-delivered {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-canceled {
  background-color: #ffebee;
  color: #c62828;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: #888;
}

/* CSS MODAL POPUP */
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

.modal-sm {
  width: 400px;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
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

.payment-edit-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.payment-select {
  padding: 6px 10px;
  border: 1px solid #e0dcd5;
  background-color: #fff;
  color: #3e332e;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  outline: none;
  max-width: 150px;
}

.payment-select:focus {
  border-color: #d1aa68;
}

.btn-add-payment-method {
  background-color: #3e332e;
  color: #d1aa68;
  border: 1px solid #d1aa68;
  width: 28px;
  height: 28px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-add-payment-method:hover {
  background-color: #d1aa68;
  color: #fff;
}

.form-group-payment {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.label-payment {
  font-size: 14px;
  font-weight: bold;
  color: #3e332e;
}

.input-payment {
  padding: 10px;
  border: 1px solid #e0dcd5;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.input-payment:focus {
  border-color: #d1aa68;
}

.btn-submit-payment {
  background-color: #d1aa68;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
}

.btn-submit-payment:hover {
  background-color: #b59253;
}
</style>