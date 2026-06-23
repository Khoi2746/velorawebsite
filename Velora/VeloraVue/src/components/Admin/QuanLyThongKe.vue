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
                    <button class="logout" @click="handleLogout"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
                </div>
            </nav>

            <main class="content">
                <header class="header">
                    <div class="header-left">
                        <h1>Thống Kê <span class="gold">Doanh Thu</span></h1>
                        <p>Phân tích hiệu quả kinh doanh và theo dõi dòng tiền hệ thống.</p>
                    </div>
                    <div class="header-right">
                        <button class="btn-export" @click="exportReport">
                            <i class="fa-solid fa-file-pdf"></i> Xuất Báo Cáo PDF
                        </button>
                    </div>
                </header>

                <section class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-wallet"></i></div>
                        <div class="kpi-info">
                            <p>Tổng Doanh Thu (Tháng {{ currentDisplayMonth }})</p>
                            <h3>{{ formatPrice(totalMonthlyRevenue) }}</h3>
                        </div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-cart-shopping"></i></div>
                        <div class="kpi-info">
                            <p>Đơn Hàng Thành Công</p>
                            <h3>{{ totalOrders }} Đơn</h3>
                        </div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-icon"><i class="fa-solid fa-boxes-stacked"></i></div>
                        <div class="kpi-info">
                            <p>Sản Phẩm Đã Bán</p>
                            <h3>{{ totalProductsSold }} Chiếc</h3>
                        </div>
                    </div>
                </section>

                <section class="chart-section">
                    <div class="chart-header">
                        <h2><i class="fa-solid fa-calendar-day"></i> Lịch Sử Doanh Thu Theo Ngày</h2>
                        <div class="filter-box">
                            <label>Chọn tháng để xem ngày:</label>
                            <input type="month" v-model="selectedDailyMonth" @change="loadDailyChartData" class="filter-input" />
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
                            <input type="number" v-model="selectedYear" min="2020" max="2100" @change="loadMonthlyChartData" class="filter-input" style="width: 120px;" />
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

            <h1 class="print-title">
                BÁO CÁO THỐNG KÊ DOANH THU<br>
                <span class="print-subtitle">THÁNG {{ currentDisplayMonth }}</span>
            </h1>

            <div class="print-meta">
                <p><strong>Ngày lập báo cáo thống kê:</strong> {{ currentDateString }}</p>
                <p><strong>Người lập báo cáo thống kê:</strong> {{ userName }}</p>
            </div>

            <table class="print-table">
                <thead>
                    <tr>
                        <th style="width: 50%; text-align: left;">Sản phẩm</th>
                        <th style="width: 20%; text-align: center;">Số lượng</th>
                        <th style="width: 30%; text-align: right;">Doanh thu</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="sp in reportProductStats" :key="sp.id">
                        <td>{{ sp.tenSanPham }}</td>
                        <td style="text-align: center;">{{ sp.soLuong }}</td>
                        <td style="text-align: right;">{{ formatPrice(sp.doanhThu) }}</td>
                    </tr>
                </tbody>
            </table>

            <div class="print-footer-summary">
                <p class="total-text">Tổng doanh thu: {{ formatPrice(totalMonthlyRevenue) }}</p>
            </div>
            
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

// Phân quyền menu hiển thị
const isAdmin = ref(false);
const userName = ref('Admin');

const today = new Date();
const yyyy = today.getFullYear();
const mm = String(today.getMonth() + 1).padStart(2, '0');
const dd = String(today.getDate()).padStart(2, '0');
const currentDateString = `${dd}/${mm}/${yyyy}`;

const menuItems = [
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge', requiresAdmin: false },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open', requiresAdmin: false },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group', requiresAdmin: false },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users', requiresAdmin: true },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice', requiresAdmin: false },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked', requiresAdmin: false },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar', requiresAdmin: false },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem', requiresAdmin: false },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list', requiresAdmin: false },
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

// Gọi lệnh in tài liệu
const exportReport = () => {
    window.print();
};

// State lưu trữ dữ liệu tính toán tổng hợp
const totalMonthlyRevenue = ref(0);
const totalOrders = ref(0);
const totalProductsSold = ref(0);

const selectedDailyMonth = ref(`${yyyy}-${mm}`);
const selectedYear = ref(yyyy);
const currentDisplayMonth = computed(() => {
    const [year, month] = selectedDailyMonth.value.split('-');
    return `${month}/${year}`;
});

const dailyChartData = ref(null);
const monthlyChartData = ref(null);

// Mảng dữ liệu mẫu của bảng sản phẩm đổ ra form in (giả lập khớp tổng tiền)
const reportProductStats = ref([
    { id: 1, tenSanPham: 'Velora Noir Starlight (Automatic / Dây da)', soLuong: 12, doanhThu: 222000000 },
    { id: 2, tenSanPham: 'Rolex Daytona Cosmograph (Automatic / Thép 904L)', soLuong: 1, doanhThu: 450000000 },
    { id: 3, tenSanPham: 'Hublot Classic Fusion (Automatic / Cao su)', soLuong: 2, doanhThu: 360000000 },
    { id: 4, tenSanPham: 'Velora Da Galuchat (Automatic / Dây da cá đuối)', soLuong: 8, doanhThu: 120000000 }
]);

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: { position: 'top' },
        tooltip: {
            callbacks: {
                label: function(context) {
                    let label = context.dataset.label || '';
                    if (label) label += ': ';
                    if (context.parsed.y !== null) label += formatPrice(context.parsed.y);
                    return label;
                }
            }
        }
    },
    scales: {
        y: { beginAtZero: true, ticks: { callback: function(value) { return value / 1000000 + ' Tr'; } } }
    }
};

const loadDailyChartData = async () => {
    try {
        const [year, month] = selectedDailyMonth.value.split('-');
        const res = await fetch(`http://localhost:8080/api/thong-ke/ngay?thang=${month}&nam=${year}`);
        if (res.ok) {
            const dataDB = await res.json();
            const labels = dataDB.map(row => {
                const d = new Date(row.ngay);
                return `${d.getDate()}/${d.getMonth()+1}`;
            });
            const dataValues = dataDB.map(row => row.tongDoanhThu);

            dailyChartData.value = {
                labels: labels,
                datasets: [{ label: `Doanh thu Ngày trong tháng ${month} (VNĐ)`, backgroundColor: '#d1aa68', borderRadius: 4, data: dataValues }]
            };
        } else throw new Error();
    } catch (error) {
        const labelsMock = ['17/06', '18/06', '19/06', '20/06', '21/06', '22/06', '23/06'];
        const dataMock = [45000000, 180000000, 33500000, 85000000, 125000000, 0, 450000000];
        dailyChartData.value = { labels: labelsMock, datasets: [{ label: 'Doanh thu (VNĐ)', backgroundColor: '#d1aa68', borderRadius: 4, data: dataMock }] };
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
                totalRev += row.tongDoanhThu;
                totalOrd += row.soDonHangThanhCong;
                totalProd += row.soSanPhamBanRa;
            });

            totalMonthlyRevenue.value = totalRev;
            totalOrders.value = totalOrd;
            totalProductsSold.value = totalProd;

            monthlyChartData.value = {
                labels: labels,
                datasets: [{ label: `Biến động doanh thu năm ${selectedYear.value} (VNĐ)`, borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataValues }]
            };
        } else throw new Error();
    } catch (error) {
        const labelsMock = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7'];
        const dataMock = [1255000000, 850000000, 460000000, 470000000, 1150000000, 918500000, 0];
        monthlyChartData.value = { labels: labelsMock, datasets: [{ label: 'Biến động doanh thu (VNĐ)', borderColor: '#3e332e', backgroundColor: 'rgba(209, 170, 104, 0.2)', borderWidth: 2, pointBackgroundColor: '#d1aa68', fill: true, tension: 0.3, data: dataMock }] };
        totalMonthlyRevenue.value = 5103500000;
        totalOrders.value = 127;
        totalProductsSold.value = 145;
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
/* ================= CSS CHỈ DÀNH CHO MÀN HÌNH WEB ================= */
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }
.sidebar { width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; display: flex; flex-direction: column; flex-shrink: 0; }
.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { color: #ccc; text-decoration: none !important; display: flex; align-items: center; gap: 10px; transition: 0.3s; padding: 10px; border-radius: 6px; }
.menu a:hover, .menu a.active { color: #d1aa68; background-color: rgba(209, 170, 104, 0.1); }
.sidebar-bottom { margin-top: auto; border-top: 1px solid #5a4b44; padding-top: 20px; }
.exit, .logout { color: #aaa; background: none; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 10px; margin-bottom: 15px; text-decoration: none !important; }
.content { flex: 1; padding: 60px; min-width: 0; overflow-y: auto; }

.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 1px solid #e0dcd5; padding-bottom: 20px;}
.header h1 { color: #3e332e; font-size: 32px; margin-bottom: 5px; }
.gold { color: #d1aa68; }
.header p { color: #888; font-size: 14px; margin: 0;}

.btn-export { background-color: #d1aa68; color: #111; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: 0.3s; box-shadow: 0 4px 10px rgba(209, 170, 104, 0.3); }
.btn-export:hover { background-color: #b8955b; color: #fff; transform: translateY(-2px); }

.kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 40px; }
.kpi-card { background: #fff; border: 1px solid #e0dcd5; border-radius: 10px; padding: 20px; display: flex; align-items: center; gap: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.02); }
.kpi-icon { width: 60px; height: 60px; background: #fdfaf5; color: #d1aa68; border-radius: 12px; display: flex; justify-content: center; align-items: center; font-size: 24px; flex-shrink: 0; }
.kpi-info p { margin: 0 0 5px 0; color: #888; font-size: 14px; font-weight: bold; text-transform: uppercase;}
.kpi-info h3 { margin: 0; color: #3e332e; font-size: 24px; }

.chart-section { background: #fff; border: 1px solid #e0dcd5; border-radius: 10px; padding: 25px; box-shadow: 0 4px 10px rgba(0,0,0,0.02); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.chart-header h2 { margin: 0; font-size: 18px; color: #3e332e; display: flex; align-items: center; gap: 10px; }
.filter-box { display: flex; align-items: center; gap: 10px; }
.filter-box label { font-size: 14px; font-weight: bold; color: #555; }
.filter-input { border: 1px solid #d1aa68; padding: 6px 12px; border-radius: 6px; outline: none; color: #333; }
.chart-container { position: relative; height: 350px; width: 100%; }

/* MẶC ĐỊNH TRÊN MÀN HÌNH MÁY TÍNH -> GIẤU HOÀN TOÀN BẢN IN */
.print-only { display: none; }


/* ========================================================= */
/* CSS ĐẶC QUYỀN: CHỈ KÍCH HOẠT KHI IN (LƯU PDF HOẶC GIẤY)  */
/* ========================================================= */
@media print {
    /* 1. Đánh sập tàng hình toàn bộ giao diện web đồ họa màu mè */
    .no-print { display: none !important; }
    
    /* 2. Ép buộc lôi bản in văn bản ra hiển thị */
    .print-only { 
        display: block !important; 
        width: 100%; 
        background: #fff !important;
        color: #000 !important;
        font-family: 'Times New Roman', Times, serif !important;
    }

    @page { margin: 25mm 20mm; }

    .print-header-company {
        text-align: left;
        font-size: 13pt;
        line-height: 1.6;
        margin-bottom: 25px;
    }
    .print-header-company p { margin: 2px 0; }
    
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
    .print-subtitle { font-size: 16pt; font-weight: bold; }

    .print-meta {
        margin: 40px 0 20px 0;
        font-size: 13pt;
        line-height: 1.8;
    }
    .print-meta p { margin: 4px 0; }

    /* Định dạng bảng đen trắng chuẩn chỉ như ảnh đính kèm */
    .print-table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 15px;
        font-size: 13pt;
    }
    
    .print-table th, .print-table td {
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

    /* Thiết kế khu vực chữ ký 2 bên cuối trang */
    .print-signature-area {
        display: flex;
        justify-content: space-between;
        margin-top: 50px;
        text-align: center;
        font-size: 13pt;
        page-break-inside: avoid;
    }

    .signature-box { width: 45%; }
    .signature-box p { margin: 3px 0; }
    .signature-note { font-size: 11pt; }
    
    .signature-space {
        height: 90px; /* Tạo khoảng trống dài để ký tên */
    }
    
    .signature-name {
        font-weight: bold;
        text-decoration: underline;
    }
}
</style>