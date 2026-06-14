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
          <p>Theo dõi và xử lý các giao dịch mua đồng hồ từ khách hàng.</p>
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
                  <i :class="order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'fa-solid fa-check-circle' : 'fa-solid fa-clock'"></i>
                  {{ order.trangThaiThanhToan === 'DA_THANH_TOAN' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(order.trangThaiDonHang)">
                  {{ getStatusText(order.trangThaiDonHang) }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-action view" title="Xem chi tiết"><i class="fa-solid fa-eye"></i></button>
                <button class="btn-action approve" title="Duyệt đơn" v-if="order.trangThaiDonHang === 'CHO_XU_LY'">
                  <i class="fa-solid fa-check"></i>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const menuItems = [
  { name: 'Dashboard', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
  { name: 'Products', link: '/admin/products', icon: 'fa-solid fa-box-open' },
  { name: 'Users', link: '/admin/users', icon: 'fa-solid fa-users' },
  { name: 'Orders', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' }
];

const orders = ref([]);

// Format Tiền
const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Format Ngày tháng
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

// Logic màu sắc và text cho Trạng Thái Đơn Hàng
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

// Gọi API lấy dữ liệu đơn hàng
const loadOrders = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/don-hang');
    if (res.ok) {
      orders.value = await res.json();
    } else {
      console.warn("Chưa có API lấy danh sách đơn hàng.");
    }
  } catch (error) {
    console.error('Lỗi kết nối Backend:', error);
  }
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
/* ================= LAYOUT GỐC ================= */
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }

.sidebar { 
  width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; 
  display: flex; flex-direction: column; flex-shrink: 0; 
}
.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { 
  color: #ccc; text-decoration: none !important; display: flex; align-items: center; gap: 10px; transition: 0.3s; padding: 10px; border-radius: 6px;
}
.menu a:hover, .menu a.active { color: #d1aa68; background-color: rgba(209, 170, 104, 0.1); }
.sidebar-bottom { margin-top: auto; border-top: 1px solid #5a4b44; padding-top: 20px; }
.exit, .logout { 
  color: #aaa; background: none; border: none; cursor: pointer; font-size: 14px; 
  display: flex; align-items: center; gap: 10px; margin-bottom: 15px; text-decoration: none !important; 
}

.content { flex: 1; padding: 60px; min-width: 0; }
.gold { color: #d1aa68; }

/* ================= TRANG QUẢN LÝ ĐƠN HÀNG ================= */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header h1 { color: #3e332e; font-size: 32px; margin-bottom: 5px; }
.header p { color: #888; font-size: 14px; }

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

/* ================= BẢNG DỮ LIỆU ================= */
.table-container {
  background: #ffffff;
  border: 1px solid #e0dcd5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.02);
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

/* Cột thông tin đặc thù */
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
.customer-info strong { color: #111; }
.customer-info .phone { font-size: 12px; color: #888; }

.price {
  font-weight: bold;
  color: #d1aa68;
}

/* Trạng thái thanh toán */
.payment-status {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}
.payment-status.paid { color: #2e7d32; }
.payment-status.unpaid { color: #e65100; }

/* Badge Trạng thái Giao hàng */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  display: inline-block;
}
.status-pending { background-color: #fff8e1; color: #f57f17; } /* Vàng Cam */
.status-shipping { background-color: #e3f2fd; color: #1565c0; } /* Xanh dương */
.status-delivered { background-color: #e8f5e9; color: #2e7d32; } /* Xanh lá */
.status-canceled { background-color: #ffebee; color: #c62828; } /* Đỏ */

/* Các nút hành động */
.actions {
  display: flex;
  gap: 10px;
}

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

.btn-action.view { background-color: #f4f1ea; color: #3e332e; }
.btn-action.view:hover { background-color: #d1aa68; color: #fff; }

.btn-action.approve { background-color: #e8f5e9; color: #2e7d32; }
.btn-action.approve:hover { background-color: #2e7d32; color: #fff; }

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: #888;
}
</style>