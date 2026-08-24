<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- 1. GỌI COMPONENT SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. GỌI COMPONENT HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH CỦA TRANG DASHBOARD -->
      <main class="content">
        <header class="content-header">
          <h1>Tổng quan <span class="gold-text">Velora Clock</span></h1>
          <p class="sub-title">Báo cáo hoạt động kinh doanh và chỉ số hệ thống theo thời gian thực từ cơ sở dữ liệu.</p>
        </header>

        <!-- HÀNG 1: CÁC THẺ THỐNG KÊ -->
        <section class="metrics-grid">
          <div class="metric-card">
            <div class="metric-icon" style="background-color: #fcf4e6; color: var(--gold-matte);">
              <i class="fa-solid fa-wallet"></i>
            </div>
            <div class="metric-info">
              <p class="metric-title">Doanh Thu (Tháng {{ currentMonth }}/{{ currentYear }})</p>
              <h3 class="metric-value">{{ formatPrice(animatedStats.tongDoanhThu) }}</h3>
              <span class="metric-trend positive"><i class="fa-solid fa-chart-line"></i> Đồng bộ từ DB Thống kê</span>
            </div>
          </div>

          <div class="metric-card">
            <div class="metric-icon" style="background-color: #e6f7ff; color: #1890ff;">
              <i class="fa-solid fa-file-invoice"></i>
            </div>
            <div class="metric-info">
              <p class="metric-title">Đơn Hàng (Tháng {{ currentMonth }}/{{ currentYear }})</p>
              <h3 class="metric-value">{{ Math.round(animatedStats.tongDonHang) }} Đơn</h3>
              <span class="metric-trend positive"><i class="fa-solid fa-arrow-trend-up"></i> Khớp 100% dữ liệu</span>
            </div>
          </div>

          <div class="metric-card">
            <div class="metric-icon" style="background-color: #f6ffed; color: #52c41a;">
              <i class="fa-solid fa-users"></i>
            </div>
            <div class="metric-info">
              <p class="metric-title">Tổng Khách Hàng</p>
              <h3 class="metric-value">{{ Math.round(animatedStats.tongKhachHang) }} Tài khoản</h3>
              <span class="metric-trend neutral"><i class="fa-solid fa-shield-halved"></i> Hoạt động ổn định</span>
            </div>
          </div>

          <!-- 🔥 Ô SỐ 4 ĐÃ ĐƯỢC ĐỔI THÀNH TỔNG SẢN PHẨM -->
          <div class="metric-card">
            <div class="metric-icon" style="background-color: #f3e8ff; color: #9333ea;">
              <i class="fa-solid fa-box-open"></i>
            </div>
            <div class="metric-info">
              <p class="metric-title">Tổng Sản Phẩm</p>
              <h3 class="metric-value">{{ Math.round(animatedStats.tongSanPham) }} Mẫu</h3>
              <span class="metric-trend positive"><i class="fa-solid fa-boxes-stacked"></i> Đang bán trên hệ thống</span>
            </div>
          </div>
        </section>

        <!-- HÀNG 2: BIỂU ĐỒ DOANH THU TUẦN -->
        <section class="box mt-4" style="margin-bottom: 24px;">
          <div class="box-header">
            <h3><i class="fa-solid fa-chart-bar" style="color: var(--gold-matte); margin-right: 8px;"></i> Biểu Đồ Doanh
              Thu 7 Ngày Gần Nhất</h3>
          </div>
          <div class="chart-container" style="height: 300px; position: relative;">
            <Bar v-if="chartData" :data="chartData" :options="chartOptions" />
            <div v-else class="empty-chart" style="text-align: center; padding: 50px; color: #888;">Đang tải dữ liệu
              biểu đồ từ máy chủ...</div>
          </div>
        </section>

        <!-- HÀNG 3: DANH SÁCH HOẠT ĐỘNG & ĐƠN HÀNG -->
        <section class="dashboard-bottom-grid">
          <!-- Bảng Đơn Hàng Gần Đây -->
          <div class="box recent-orders-box">
            <div class="box-header">
              <h3>Đơn Hàng Mới Nhất</h3>
              <router-link to="/admin/orders" class="view-all">Xem tất cả</router-link>
            </div>
            <div class="table-responsive">
              <table class="dashboard-table">
                <thead>
                  <tr>
                    <th>Mã Đơn</th>
                    <th>Khách Hàng</th>
                    <th>Tổng Tiền</th>
                    <th>Trạng Thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in recentOrders" :key="order.maDonHang">
                    <td><strong>{{ order.maDonHangCode }}</strong></td>
                    <td>{{ order.tenNguoiNhan }}</td>
                    <td class="gold-text fw-bold">{{ formatPrice(order.tongTien) }}</td>
                    <td>
                      <span :class="['badge-status', getStatusClass(order.trangThaiDonHang)]">
                        {{ getStatusText(order.trangThaiDonHang) }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="recentOrders.length === 0">
                    <td colspan="4" style="text-align: center; padding: 20px; color: #888;">Chưa có đơn hàng nào trong
                      hệ thống.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Danh sách thao tác nhanh (Quick Links) -->
          <div class="box quick-actions-box">
            <div class="box-header">
              <h3>Truy Cập Nhanh</h3>
            </div>
            <div class="quick-links">
              <router-link v-for="card in filteredCards" :key="card.title" :to="card.link" class="quick-link-item">
                <i :class="card.icon"></i>
                <span>{{ card.title }}</span>
              </router-link>
            </div>
          </div>
        </section>

      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const isCollapsed = ref(false);
const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

const today = new Date();
const currentMonth = ref(String(today.getMonth() + 1).padStart(2, '0'));
const currentYear = ref(today.getFullYear());

// 🔥 ĐỔI BIẾN sanPhamSapHet THÀNH tongSanPham
const stats = ref({
  tongDoanhThu: 0,
  tongDonHang: 0,
  tongKhachHang: 0,
  tongSanPham: 0 
});

const animatedStats = ref({
  tongDoanhThu: 0,
  tongDonHang: 0,
  tongKhachHang: 0,
  tongSanPham: 0 
});

const recentOrders = ref([]);
const chartData = ref(null);

const animateValue = (key, endValue) => {
  const startValue = 0;
  const duration = 1500;
  let startTime = null;

  const step = (currentTime) => {
    if (!startTime) startTime = currentTime;
    const progress = Math.min((currentTime - startTime) / duration, 1);
    const easeProgress = 1 - Math.pow(1 - progress, 4); 
    
    animatedStats.value[key] = startValue + (endValue - startValue) * easeProgress;

    if (progress < 1) {
      requestAnimationFrame(step);
    } else {
      animatedStats.value[key] = endValue;
    }
  };
  requestAnimationFrame(step);
};

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: function (context) {
          return ' Doanh thu: ' + formatPrice(context.parsed.y);
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function (value) {
          return value / 1000000 + ' Tr ₫';
        }
      }
    }
  }
};

const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const userRole = computed(() => {
  try {
    const userStr = localStorage.getItem('user');
    if (userStr && userStr !== 'undefined') {
      const user = JSON.parse(userStr);
      return user?.vaiTro || '';
    }
  } catch (e) {
    console.error(e);
  }
  return '';
});

const allCards = [
  { title: 'Sản phẩm', icon: 'fa-solid fa-box-open', link: '/admin/products' },
  { title: 'Lịch hẹn', icon: 'fa-solid fa-calendar-check', link: '/admin/lich-hen' },
  { title: 'Người dùng', icon: 'fa-solid fa-users', link: '/admin/users', requiresAdmin: true },
  { title: 'Nhập kho', icon: 'fa-solid fa-clipboard-list', link: '/admin/receipts' },
  { title: 'Hoàn tiền', icon: 'fa-solid fa-rotate-left', link: '/admin/quan-ly-hoan-tien' },
  { title: 'Mã giảm giá', icon: 'fa-solid fa-tags', link: '/admin/ma-giam-gia' }
];

const filteredCards = computed(() => {
  if (!userRole.value) return allCards; 
  if (userRole.value === 'ROLE_ADMIN' || userRole.value === 1) return allCards;
  return allCards.filter(card => !card.requiresAdmin);
});

const getStatusText = (status) => {
  const map = {
    'CHO_XU_LY': 'Chờ xử lý',
    'DANG_GIAO': 'Đang giao',
    'DA_GIAO': 'Đã giao',
    'DA_HUY': 'Đã hủy'
  };
  return map[status] || status;
};

const getStatusClass = (status) => {
  const map = {
    'CHO_XU_LY': 'status-pending',
    'DANG_GIAO': 'status-processing',
    'DA_GIAO': 'status-success',
    'DA_HUY': 'status-danger'
  };
  return map[status] || 'status-pending';
};

const fetchDashboardData = async () => {
  try {
    const axiosConfig = { withCredentials: true };
    const timestamp = new Date().getTime();

    // 1. ĐỒNG BỘ DOANH THU & ĐƠN HÀNG TỪ BẢNG THỐNG KÊ
    const resChart = await axios.get(`${API_BASE}/api/thong-ke/ngay?thang=${currentMonth.value}&nam=${currentYear.value}&t=${timestamp}`, axiosConfig);
    
    if (resChart.data && resChart.data.length > 0) {
      const dataDB = resChart.data;
      
      stats.value.tongDoanhThu = dataDB.reduce((sum, row) => sum + (Number(row.tongDoanhThu) || 0), 0);
      stats.value.tongDonHang = dataDB.reduce((sum, row) => sum + (Number(row.soDonHangThanhCong) || 0), 0);

      const labels = dataDB.map(row => `${String(new Date(row.ngay).getDate()).padStart(2, '0')}/${String(new Date(row.ngay).getMonth() + 1).padStart(2, '0')}`);
      const values = dataDB.map(row => row.tongDoanhThu);

      chartData.value = {
        labels: labels,
        datasets: [{
          label: 'Doanh thu (VNĐ)',
          backgroundColor: '#d1aa68',
          borderRadius: 6,
          data: values
        }]
      };
    } else {
        stats.value.tongDoanhThu = 0;
        stats.value.tongDonHang = 0;
    }

    // 2. LẤY DANH SÁCH ĐƠN MỚI NHẤT 
    const resOrders = await axios.get(`${API_BASE}/api/don-hang?t=${timestamp}`, axiosConfig);
    if (resOrders.data) {
      const sortedOrders = resOrders.data.sort((a, b) => b.maDonHang - a.maDonHang);
      recentOrders.value = sortedOrders.slice(0, 5);
    }

    // 3. ĐẾM KHÁCH HÀNG 
    const resUsers = await axios.get(`${API_BASE}/api/admin/thanh-vien?t=${timestamp}`, axiosConfig);
    if (resUsers.data) {
      const users = resUsers.data;
      stats.value.tongKhachHang = users.filter(u => {
        if (u.vaiTros && u.vaiTros.length > 0) {
            return u.vaiTros.some(role => role.tenVaiTro === 'ROLE_CUSTOMER');
        }
        return false;
      }).length;
    }

    // 4. ĐẾM TỔNG SẢN PHẨM HIỆN CÓ TRONG KHO (Thay vì lọc < 5 thì lấy hết độ dài mảng)
    const resProducts = await axios.get(`${API_BASE}/api/san-pham?t=${timestamp}`, axiosConfig);
    if (resProducts.data) {
      stats.value.tongSanPham = resProducts.data.length;
    }

    // KÍCH HOẠT HIỆU ỨNG CHẠY SỐ
    animateValue('tongDoanhThu', stats.value.tongDoanhThu);
    animateValue('tongDonHang', stats.value.tongDonHang);
    animateValue('tongKhachHang', stats.value.tongKhachHang);
    animateValue('tongSanPham', stats.value.tongSanPham); // Đã đổi biến

  } catch (error) {
    console.error('Lỗi tải dữ liệu Dashboard:', error);
  }
};

onMounted(() => {
  fetchDashboardData();
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
</style>

<style scoped>
.velora-admin-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: var(--bg-page);
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}
.content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: var(--bg-page);
}
.content {
  flex: 1;
  padding: 30px;
  color: var(--text-main);
  width: 100%;
  box-sizing: border-box;
}
.content-header {
  margin-bottom: 25px;
  width: 100%;
}
.content-header h1 {
  font-size: 26px;
  font-weight: bold;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
}
.content-header .sub-title {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}
.gold-text {
  color: var(--gold-matte);
}
.fw-bold {
  font-weight: bold;
}
.mt-4 {
  margin-top: 24px;
}
.box {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}
.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.box-header h3 {
  font-size: 18px;
  color: var(--wood-dark);
  font-weight: 600;
  margin: 0;
}
.view-all {
  color: var(--gold-matte);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}
.view-all:hover {
  text-decoration: underline;
}
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  width: 100%;
}
.metric-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}
.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}
.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.metric-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.metric-title {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 5px 0;
  font-weight: 500;
  white-space: nowrap;
}
.metric-value {
  font-size: 22px;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
  font-weight: 700;
}
.metric-trend {
  font-size: 12px;
  font-weight: 500;
}
.metric-trend.positive { color: #52c41a; }
.metric-trend.negative { color: #f5222d; }
.metric-trend.neutral { color: #8c8c8c; }

.dashboard-bottom-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  width: 100%;
}
@media (max-width: 1024px) {
  .dashboard-bottom-grid { grid-template-columns: 1fr; }
}
.table-responsive { width: 100%; overflow-x: auto; }
.dashboard-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 450px;
}
.dashboard-table th {
  text-align: left;
  padding: 12px;
  color: #6b7280;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 2px solid var(--border-light);
  white-space: nowrap;
}
.dashboard-table td {
  padding: 14px 12px;
  font-size: 14px;
  color: var(--text-main);
  border-bottom: 1px solid var(--border-light);
}
.dashboard-table tbody tr:hover { background-color: #faf9f6; }
.badge-status {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.status-pending { background-color: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.status-processing { background-color: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status-success { background-color: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-danger { background-color: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

.quick-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  width: 100%;
}
.quick-link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-main);
  font-weight: 500;
  transition: all 0.2s ease;
  background-color: #faf9f6;
  text-align: center;
}
.quick-link-item i {
  font-size: 24px;
  color: var(--gold-matte);
  margin-bottom: 10px;
}
.quick-link-item:hover {
  background-color: var(--wood-dark);
  color: #fff;
  border-color: var(--wood-dark);
}
</style>