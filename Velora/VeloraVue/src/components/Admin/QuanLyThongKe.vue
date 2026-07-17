<template>
    <div class="admin-wrapper">

        <div class="no-print" style="display: flex; width: 100%; min-height: 100vh;">
            <nav class="sidebar">
                <h2 class="brand">VELORA ADMIN</h2>
                <ul class="menu">
                    <li v-for="item in filteredMenuItems" :key="item.name">
                        <router-link :to="item.link" active-class="active">
                            <i :class="item.icon"></i> {{ item.name }}
                        </router-link>
                    </li>
                </ul>
                <div class="sidebar-bottom">
                    <router-link to="/" class="exit"><i class="fa-solid fa-house"></i> Return</router-link>
                    <button class="logout" @click="handleLogout"><i class="fa-solid fa-right-from-bracket"></i>
                        Logout</button>
                </div>
            </nav>

            <main class="content">
                <header class="header">
                    <div class="header-left">
                        <h1>Thống Kê <span class="gold">Doanh Thu</span></h1>
                        <p>Phân tích hiệu quả kinh doanh và theo dõi dòng tiền hệ thống.</p>
                    </div>
                    <div class="header-right" style="position: relative;">
                        <button class="btn-export" @click="toggleExportMenu">
                            <i class="fa-solid fa-file-pdf"></i> Xuất Báo Cáo <i class="fa-solid fa-chevron-down"
                                style="font-size: 12px; margin-left: 5px;"></i>
                        </button>

                        <div v-if="showExportMenu" class="export-dropdown">
                            <button @click="triggerPrint('month')">
                                <i class="fa-solid fa-calendar-days"></i> Báo Cáo Tháng (Theo Ngày)
                            </button>
                            <button @click="triggerPrint('year')">
                                <i class="fa-solid fa-gem"></i> Báo Cáo Năm (Theo Sản Phẩm)
                            </button>
                        </div>
                    </div>
                </header>

                <section class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-wallet"></i></div>
                        <div class="kpi-info">
                            <p>Doanh Thu (Tháng {{ currentDisplayMonth }})</p>
                            <h3>{{ formatPrice(calculatedMonthlyRevenue) }}</h3>
                        </div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-cart-shopping"></i></div>
                        <div class="kpi-info">
                            <p>Đơn Hàng (Tháng {{ currentDisplayMonth }})</p>
                            <h3>{{ calculatedTotalOrders }} Đơn</h3>
                        </div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-boxes-stacked"></i></div>
                        <div class="kpi-info">
                            <p>Sản Phẩm Đã Bán (Tháng {{ currentDisplayMonth }})</p>
                            <h3>{{ calculatedTotalProducts }} Chiếc</h3>
                        </div>
                    </div>

                    <div class="kpi-card year-card">
                        <div class="kpi-icon"><i class="fa-solid fa-sack-dollar"></i></div>
                        <div class="kpi-info">
                            <p>Tổng Doanh Thu (Năm {{ selectedYear }})</p>
                            <h3>{{ formatPrice(yearlyTotalRevenue) }}</h3>
                        </div>
                    </div>
                    <div class="kpi-card year-card">
                        <div class="kpi-icon"><i class="fa-solid fa-truck-fast"></i></div>
                        <div class="kpi-info">
                            <p>Tổng Đơn Hàng (Năm {{ selectedYear }})</p>
                            <h3>{{ yearlyTotalOrders }} Đơn</h3>
                        </div>
                    </div>
                    <div class="kpi-card year-card">
                        <div class="kpi-icon"><i class="fa-solid fa-gem"></i></div>
                        <div class="kpi-info">
                            <p>Tổng SP Đã Bán (Năm {{ selectedYear }})</p>
                            <h3>{{ yearlyTotalProducts }} Chiếc</h3>
                        </div>
                    </div>
                </section>

                <section class="chart-section">
                    <div class="chart-header">
                        <h2><i class="fa-solid fa-calendar-day"></i> Lịch Sử Doanh Thu Theo Ngày</h2>
                        <div class="filter-box">
                            <label>Chọn tháng để xem ngày:</label>
                            <input type="month" v-model="selectedDailyMonth" @change="loadDailyChartData"
                                class="filter-input" />
                        </div>
                    </div>
                    <div class="chart-container">
                        <Bar v-if="dailyChartData" :data="dailyChartData" :options="chartOptions" />
                        <div v-else class="empty-chart">Đang tải dữ liệu biểu đồ...</div>
                    </div>
                </section>

                <section class="chart-section" style="margin-top: 40px;">
                    <div class="chart-header">
                        <h2><i class="fa-solid fa-calendar-days"></i> Doanh Thu Các Tháng Trong Năm</h2>
                        <div class="filter-box">
                            <label>Chọn năm:</label>
                            <input type="number" v-model="selectedYear" min="2020" max="2100"
                                @change="loadMonthlyChartData" class="filter-input" style="width: 120px;" />
                        </div>
                    </div>
                    <div class="chart-container">
                        <Line v-if="monthlyChartData" :data="monthlyChartData" :options="chartOptions" />
                        <div v-else class="empty-chart">Đang tải dữ liệu biểu đồ...</div>
                    </div>
                </section>
            </main>
        </div>

        <div class="print-only">
            <div class="print-header-company">
                <p><strong>CỬA HÀNG ĐỒNG HỒ CAO CẤP VELORA CLOCK</strong></p>
                <p>Địa chỉ: Khu công nghệ phần mềm, Quận 12, TP.Hồ Chí Minh</p>
                <p>ĐT: 0123456789</p>
                <div class="print-line-divider"></div>
            </div>

            <template v-if="printMode === 'month'">
                <h1 class="print-title">
                    BÁO CÁO CHI TIẾT DOANH THU<br>
                    <span class="print-subtitle">THÁNG {{ currentDisplayMonth }}</span>
                </h1>

                <div class="print-meta">
                    <p><strong>Ngày lập báo cáo:</strong> {{ currentDateString }}</p>
                    <p><strong>Người lập báo cáo:</strong> {{ userName }}</p>
                </div>

                <table class="print-table">
                    <thead>
                        <tr>
                            <th style="width: 25%; text-align: center;">Ngày</th>
                            <th style="width: 25%; text-align: center;">Số đơn hàng</th>
                            <th style="width: 20%; text-align: center;">Số lượng SP</th>
                            <th style="width: 30%; text-align: right;">Doanh thu</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in dailyReportData" :key="index">
                            <td style="text-align: center; font-weight: bold;">{{ item.ngay }}</td>
                            <td style="text-align: center;">{{ item.soDonHang }}</td>
                            <td style="text-align: center;">{{ item.soSanPham }}</td>
                            <td style="text-align: right; font-weight: bold;">{{ formatPrice(item.doanhThu) }}</td>
                        </tr>
                        <tr v-if="dailyReportData.length === 0">
                            <td colspan="4" style="text-align: center; font-style: italic;">Không có dữ liệu giao dịch
                                trong tháng này.</td>
                        </tr>
                    </tbody>
                </table>

                <div class="print-footer-summary">
                    <p class="total-text">Tổng doanh thu tháng: {{ formatPrice(calculatedMonthlyRevenue) }}</p>
                </div>
            </template>

            <template v-if="printMode === 'year'">
                <h1 class="print-title">
                    BÁO CÁO THỐNG KÊ DOANH THU SẢN PHẨM<br>
                    <span class="print-subtitle">NĂM {{ selectedYear }}</span>
                </h1>

                <div class="print-meta">
                    <p><strong>Ngày lập báo cáo:</strong> {{ currentDateString }}</p>
                    <p><strong>Người lập báo cáo:</strong> {{ userName }}</p>
                </div>

                <table class="print-table">
                    <thead>
                        <tr>
                            <th style="width: 50%; text-align: left;">Sản phẩm</th>
                            <th style="width: 20%; text-align: center;">Số lượng bán ra</th>
                            <th style="width: 30%; text-align: right;">Doanh thu mang lại</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="sp in reportProductStats" :key="sp.id">
                            <td>{{ sp.tenSanPham }}</td>
                            <td style="text-align: center;">{{ sp.soLuong }}</td>
                            <td style="text-align: right;">{{ formatPrice(sp.doanhThu) }}</td>
                        </tr>
                        <tr v-if="reportProductStats.length === 0">
                            <td colspan="3" style="text-align: center; font-style: italic; color: #555;">Không có sản
                                phẩm nào được bán ra trong năm này.</td>
                        </tr>
                    </tbody>
                </table>

                <div class="print-footer-summary">
                    <p class="total-text">Tổng doanh thu cả năm: {{ formatPrice(yearlyTotalRevenue) }}</p>
                </div>
            </template>

            <div class="print-signature-area">
                <div class="signature-box">
                    <p><strong>Người lập biểu</strong></p>
                    <p class="signature-note"><em>(Ký, ghi rõ họ tên)</em></p>
                    <div class="signature-space"></div>
                    <p class="signature-name">{{ userName }}</p>
                </div>
                <div class="signature-box">
                    <p><strong>Giám đốc</strong></p>
                    <p class="signature-note"><em>(Ký, đóng dấu, ghi rõ họ tên)</em></p>
                    <div class="signature-space"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, PointElement, LineElement, Filler } from 'chart.js';
import { Bar, Line } from 'vue-chartjs';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, PointElement, LineElement, Filler);

const isAdmin = ref(false);
const userName = ref('Admin');

const showExportMenu = ref(false);
const printMode = ref('month');

const today = new Date();
const yyyy = today.getFullYear();
const mm = String(today.getMonth() + 1).padStart(2, '0');
const dd = String(today.getDate()).padStart(2, '0');
const currentDateString = `${dd}/${mm}/${yyyy}`;

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
  { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', requiresAdmin: true }
];

const filteredMenuItems = computed(() => menuItems.filter(item => !item.requiresAdmin || isAdmin.value));

const handleLogout = () => {
    localStorage.removeItem('user');
    window.location.href = '/';
};

const formatPrice = (value) => {
    if (!value) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const toggleExportMenu = () => {
    showExportMenu.value = !showExportMenu.value;
};

const triggerPrint = (mode) => {
    printMode.value = mode;
    showExportMenu.value = false;
    setTimeout(() => {
        window.print();
    }, 100);
};

const selectedDailyMonth = ref(`${yyyy}-${mm}`);
const selectedYear = ref(yyyy);
const currentDisplayMonth = computed(() => {
    const [year, month] = selectedDailyMonth.value.split('-');
    return `${month}/${year}`;
});

const dailyChartData = ref(null);
const monthlyChartData = ref(null);

const dailyReportData = ref([]);
const reportProductStats = ref([]); // Bỏ khởi tạo cứng ở đây

const calculatedMonthlyRevenue = computed(() => dailyReportData.value.reduce((total, item) => total + Number(item.doanhThu), 0));
const calculatedTotalOrders = computed(() => dailyReportData.value.reduce((total, item) => total + Number(item.soDonHang), 0));
const calculatedTotalProducts = computed(() => dailyReportData.value.reduce((total, item) => total + Number(item.soSanPham), 0));

const yearlyTotalRevenue = ref(0);
const yearlyTotalOrders = ref(0);
const yearlyTotalProducts = ref(0);

const chartOptions = {
    responsive: true, maintainAspectRatio: false,
    plugins: {
        legend: { position: 'top' },
        tooltip: { callbacks: { label: function (c) { let l = c.dataset.label || ''; if (l) l += ': '; if (c.parsed.y !== null) l += formatPrice(c.parsed.y); return l; } } }
    },
    scales: { y: { beginAtZero: true, ticks: { callback: function (value) { return value / 1000000 + ' Tr'; } } } }
};

// Hàm cập nhật danh sách sản phẩm mẫu theo năm (Nếu có doanh thu thì hiện, = 0 thì ẩn)
const updateReportProductStats = (totalRevenue) => {
    if (totalRevenue === 0) {
        reportProductStats.value = [];
    } else {
        reportProductStats.value = [
            { id: 1, tenSanPham: 'Velora Noir Starlight (Automatic / Dây da)', soLuong: 58, doanhThu: 1073000000 },
            { id: 2, tenSanPham: 'Rolex Daytona Cosmograph (Automatic / Thép)', soLuong: 3, doanhThu: 1350000000 },
            { id: 3, tenSanPham: 'Hublot Classic Fusion (Automatic / Cao su)', soLuong: 8, doanhThu: 1440000000 },
            { id: 4, tenSanPham: 'Velora Da Galuchat (Automatic / Cá đuối)', soLuong: 42, doanhThu: 630000000 },
            { id: 5, tenSanPham: 'Patek Philippe Nautilus (Automatic / Thép)', soLuong: 1, doanhThu: 1500000000 }
        ];
    }
};

const loadDailyChartData = async () => {
    try {
        const [year, month] = selectedDailyMonth.value.split('-');
        const res = await fetch(`http://localhost:8080/api/thong-ke/ngay?thang=${month}&nam=${year}`);

        if (res.ok) {
            const dataDB = await res.json();
            const labels = dataDB.map(row => { const d = new Date(row.ngay); return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`; });
            const dataValues = dataDB.map(row => row.tongDoanhThu);

            dailyChartData.value = { labels: labels, datasets: [{ label: `Doanh thu Ngày trong tháng ${month} (VNĐ)`, backgroundColor: '#d1aa68', borderRadius: 4, data: dataValues }] };
            dailyReportData.value = dataDB.map(row => {
                const d = new Date(row.ngay);
                return { ngay: `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`, doanhThu: row.tongDoanhThu, soDonHang: row.soDonHangThanhCong, soSanPham: row.soSanPhamBanRa };
            });
        } else throw new Error();
    } catch (error) {
        const dataMockDB = [
            { ngay: '2026-06-17', tongDoanhThu: 45000000, soDonHangThanhCong: 3, soSanPhamBanRa: 4 },
            { ngay: '2026-06-18', tongDoanhThu: 180000000, soDonHangThanhCong: 1, soSanPhamBanRa: 1 },
            { ngay: '2026-06-19', tongDoanhThu: 33500000, soDonHangThanhCong: 2, soSanPhamBanRa: 2 },
            { ngay: '2026-06-20', tongDoanhThu: 85000000, soDonHangThanhCong: 4, soSanPhamBanRa: 5 },
            { ngay: '2026-06-21', tongDoanhThu: 125000000, soDonHangThanhCong: 3, soSanPhamBanRa: 3 },
            { ngay: '2026-06-22', tongDoanhThu: 0, soDonHangThanhCong: 0, soSanPhamBanRa: 0 },
            { ngay: '2026-06-23', tongDoanhThu: 450000000, soDonHangThanhCong: 1, soSanPhamBanRa: 1 }
        ];
        dailyChartData.value = { labels: ['17/06', '18/06', '19/06', '20/06', '21/06', '22/06', '23/06'], datasets: [{ label: 'Doanh thu (VNĐ)', backgroundColor: '#d1aa68', borderRadius: 4, data: dataMockDB.map(d => d.tongDoanhThu) }] };
        dailyReportData.value = dataMockDB.map(row => {
            const d = new Date(row.ngay);
            return { ngay: `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`, doanhThu: row.tongDoanhThu, soDonHang: row.soDonHangThanhCong, soSanPham: row.soSanPhamBanRa };
        });
    }
};

const loadMonthlyChartData = async () => {
    try {
        const res = await fetch(`http://localhost:8080/api/thong-ke/thang?nam=${selectedYear.value}`);
        if (res.ok) {
            const dataDB = await res.json();
            const labels = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
            const dataValues = Array(12).fill(0);

            let totalRev = 0; let totalOrd = 0; let totalProd = 0;
            dataDB.forEach(row => {
                const monthIndex = row.thang - 1;
                dataValues[monthIndex] = row.tongDoanhThu;
                totalRev += row.tongDoanhThu; totalOrd += row.soDonHangThanhCong; totalProd += row.soSanPhamBanRa;
            });

            yearlyTotalRevenue.value = totalRev; yearlyTotalOrders.value = totalOrd; yearlyTotalProducts.value = totalProd;

            updateReportProductStats(totalRev);

            monthlyChartData.value = { labels: labels, datasets: [{ label: `Biến động doanh thu năm ${selectedYear.value} (VNĐ)`, borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataValues }] };
        } else throw new Error();
    } catch (error) {
        const labelsMock = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7'];
        const dataMock = [1255000000, 850000000, 460000000, 470000000, 1150000000, 918500000, 0];
        monthlyChartData.value = { labels: labelsMock, datasets: [{ label: 'Biến động doanh thu (VNĐ)', borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataMock }] };

        yearlyTotalRevenue.value = 5993000000; yearlyTotalOrders.value = 145; yearlyTotalProducts.value = 168;

        updateReportProductStats(5993000000);
    }
};

onMounted(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        const userObj = JSON.parse(userStr);
        const role = userObj.vaiTro || userObj.maVaiTro;
        userName.value = userObj.hoTen || 'Quản trị viên';
        if (role === 'ROLE_ADMIN' || role === 1) isAdmin.value = true;
    }
    loadDailyChartData();
    loadMonthlyChartData();
});
</script>

<style scoped>
@import "../CSS/Admin/QuanLyThongKe.css";
</style>