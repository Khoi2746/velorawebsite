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

            // CHẠY LOGIC XÓA SẢN PHẨM NẾU TỔNG DOANH THU = 0
            updateReportProductStats(totalRev);

            monthlyChartData.value = { labels: labels, datasets: [{ label: `Biến động doanh thu năm ${selectedYear.value} (VNĐ)`, borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataValues }] };
        } else throw new Error();
    } catch (error) {
        const labelsMock = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7'];
        const dataMock = [1255000000, 850000000, 460000000, 470000000, 1150000000, 918500000, 0];
        monthlyChartData.value = { labels: labelsMock, datasets: [{ label: 'Biến động doanh thu (VNĐ)', borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataMock }] };

        yearlyTotalRevenue.value = 5993000000; yearlyTotalOrders.value = 145; yearlyTotalProducts.value = 168;

        // CHẠY LOGIC MOCK NẾU CHƯA CÓ API
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
    overflow-y: auto;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    border-bottom: 1px solid #e0dcd5;
    padding-bottom: 20px;
}

.header h1 {
    color: #3e332e;
    font-size: 32px;
    margin-bottom: 5px;
}

.gold {
    color: #d1aa68;
}

.header p {
    color: #888;
    font-size: 14px;
    margin: 0;
}

.btn-export {
    background-color: #d1aa68;
    color: #111;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    font-weight: bold;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: 0.3s;
    box-shadow: 0 4px 10px rgba(209, 170, 104, 0.3);
}

.btn-export:hover {
    background-color: #b8955b;
    color: #fff;
    transform: translateY(-2px);
}

.export-dropdown {
    position: absolute;
    top: 120%;
    right: 0;
    background: #fff;
    border: 1px solid #e0dcd5;
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    min-width: 250px;
    z-index: 100;
    overflow: hidden;
    animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.export-dropdown button {
    background: none;
    border: none;
    padding: 15px 20px;
    text-align: left;
    font-size: 14px;
    color: #3e332e;
    cursor: pointer;
    transition: 0.2s;
    display: flex;
    align-items: center;
    gap: 10px;
}

.export-dropdown button:hover {
    background: #fdfaf5;
    color: #d1aa68;
}

.export-dropdown button:not(:last-child) {
    border-bottom: 1px solid #f0efeb;
}

.kpi-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 40px;
}

.kpi-card {
    background: #fff;
    border: 1px solid #e0dcd5;
    border-radius: 10px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 20px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.02);
}

.kpi-icon {
    width: 60px;
    height: 60px;
    background: #fdfaf5;
    color: #d1aa68;
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 24px;
    flex-shrink: 0;
}

.kpi-info p {
    margin: 0 0 5px 0;
    color: #888;
    font-size: 14px;
    font-weight: bold;
    text-transform: uppercase;
}

.kpi-info h3 {
    margin: 0;
    color: #3e332e;
    font-size: 24px;
}

.year-card .kpi-icon {
    background: #3e332e;
    color: #d1aa68;
}

.chart-section {
    background: #fff;
    border: 1px solid #e0dcd5;
    border-radius: 10px;
    padding: 25px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.02);
}

.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.chart-header h2 {
    margin: 0;
    font-size: 18px;
    color: #3e332e;
    display: flex;
    align-items: center;
    gap: 10px;
}

.filter-box {
    display: flex;
    align-items: center;
    gap: 10px;
}

.filter-box label {
    font-size: 14px;
    font-weight: bold;
    color: #555;
}

.filter-input {
    border: 1px solid #d1aa68;
    padding: 6px 12px;
    border-radius: 6px;
    outline: none;
    color: #333;
}

.chart-container {
    position: relative;
    height: 350px;
    width: 100%;
}

.print-only {
    display: none;
}

@media print {
    .no-print {
        display: none !important;
    }

    .print-only {
        display: block !important;
        width: 100%;
        background: #fff !important;
        color: #000 !important;
        font-family: 'Times New Roman', Times, serif !important;
    }

    @page {
        margin: 25mm 20mm;
    }

    .print-header-company {
        text-align: left;
        font-size: 13pt;
        line-height: 1.6;
        margin-bottom: 25px;
    }

    .print-header-company p {
        margin: 2px 0;
    }

    .print-line-divider {
        width: 35%;
        height: 1px;
        background-color: #000;
        margin-top: 8px;
    }

    .print-title {
        text-align: center;
        font-size: 20pt;
        font-weight: bold;
        margin: 35px 0 10px 0;
        line-height: 1.3;
    }

    .print-subtitle {
        font-size: 16pt;
        font-weight: bold;
    }

    .print-meta {
        margin: 40px 0 20px 0;
        font-size: 13pt;
        line-height: 1.8;
    }

    .print-meta p {
        margin: 4px 0;
    }

    .print-table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 15px;
        font-size: 13pt;
    }

    .print-table th,
    .print-table td {
        border: 1px solid #000;
        padding: 10px 12px;
    }

    .print-table th {
        background-color: #f2f2f2 !important;
        -webkit-print-color-adjust: exact;
        font-weight: bold;
    }

    .print-footer-summary {
        text-align: right;
        margin-top: 20px;
        padding-right: 10px;
    }

    .total-text {
        font-size: 14pt;
        font-weight: bold;
    }

    .print-signature-area {
        display: flex;
        justify-content: space-between;
        margin-top: 50px;
        text-align: center;
        font-size: 13pt;
        page-break-inside: avoid;
    }

    .signature-box {
        width: 45%;
    }

    .signature-box p {
        margin: 3px 0;
    }

    .signature-note {
        font-size: 11pt;
    }

    .signature-space {
        height: 90px;
    }

    .signature-name {
        font-weight: bold;
        text-decoration: underline;
    }
}
</style>