<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- 1. SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH SOC -->
      <main class="content">
        <header class="header soc-header-banner">
          <div class="header-left">
            <h1>Velora Clock <span class="gold">Security Operations Center</span></h1>
            <p>Hệ thống giám sát an ninh, quản lý tài khoản và kiểm duyệt nội dung thời gian thực.</p>
          </div>
          <div class="header-right">
            <div class="soc-live-badge">
              <span class="pulse-dot"></span> Đang Giám Sát Chủ Động
            </div>
          </div>
        </header>

        <!-- Thẻ thống kê tổng quan (Đã chuyển thành 2 cột cho cân đối) -->
        <div class="soc-stats-grid">
          <div class="stat-card dark-card">
            <h3>Tài Khoản / IP Bị Cô Lập</h3>
            <div class="stat-number">{{ lockedUsers.length }}</div>
          </div>
          <div class="stat-card orange-card">
            <h3>Nội Dung Độc Hại (24h)</h3>
            <div class="stat-number">{{ comments.filter(c => c.isSpam).length }}</div>
          </div>
        </div>

        <!-- Thanh chuyển tab điều hướng SOC -->
        <div class="soc-tabs-bar">
          <button class="soc-tab-btn" :class="{ active: currentTab === 'locked-users' }" @click="currentTab = 'locked-users'">
            <i class="fa-solid fa-user-shield"></i> Kiểm Soát Vi Phạm
          </button>
          <button class="soc-tab-btn" :class="{ active: currentTab === 'comments' }" @click="currentTab = 'comments'">
            <i class="fa-solid fa-filter"></i> Bộ Lọc Nội Dung
          </button>
          <button class="soc-tab-btn" :class="{ active: currentTab === 'logs' }" @click="currentTab = 'logs'">
            <i class="fa-solid fa-clock-rotate-left"></i> Nhật Ký Truy Cập
          </button>
        </div>

        <!-- NỘI DUNG TỪNG TAB -->
        <section class="table-container">
          
          <!-- TAB 1: KIỂM SOÁT VI PHẠM (USER) -->
          <div v-if="currentTab === 'locked-users'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Mã User</th>
                  <th>Họ Tên</th>
                  <th>Email</th>
                  <th>Trạng Thái</th>
                  <th>Lý Do Vi Phạm</th>
                  <!-- 🔥 CỘT MỚI: THỜI GIAN ĐẾM NGƯỢC -->
                  <th style="text-align: center;">Thời Gian Phạt</th>
                  <th>Hành Động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in lockedUsers" :key="u.maNguoiDung">
                  <td>#{{ u.maNguoiDung }}</td>
                  <td><strong>{{ u.hoTen }}</strong></td>
                  <td>{{ u.email }}</td>
                  <td><span class="status-badge banned-status">{{ u.trangThai }}</span></td>
                  <td>{{ u.lyDoViPham || 'Vi phạm tiêu chuẩn cộng đồng' }}</td>
                  
                  <!-- 🔥 XỬ LÝ HIỂN THỊ THỜI GIAN -->
                  <td style="text-align: center;">
                    <span class="countdown-box" :class="getCountdownData(u).class">
                      <i :class="getCountdownData(u).icon"></i> 
                      {{ getCountdownData(u).text }}
                    </span>
                  </td>

                  <td>
                    <button class="btn-action btn-unlock" @click="manageUser(u.maNguoiDung, 'unlock')">
                      <i class="fa-solid fa-user-check"></i> Mở Khóa
                    </button>
                  </td>
                </tr>
                <tr v-if="lockedUsers.length === 0">
                  <td colspan="7" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                    Không có tài khoản nào bị đình chỉ.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- TAB 2: BỘ LỌC NỘI DUNG -->
          <div v-if="currentTab === 'comments'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Thời Gian</th>
                  <th>Định Danh User</th>
                  <th>Nội Dung Chi Tiết</th>
                  <th>Trạng Thái Nội Dung</th>
                  <th>Hành Động Xử Lý</th>
                </tr>
              </thead>
              <tbody>
                <!-- Đôn bình luận vi phạm lên đầu bảng -->
                <tr 
                  v-for="comment in sortedAndFilteredComments" 
                  :key="comment.maDanhGia"
                  :class="comment.isSpam ? 'row-violation' : 'row-safe'"
                >
                  <td>{{ formatDate(comment.ngayDanhGia) }}</td>
                  <td>
                    <strong>{{ comment.tenNguoiDung }}</strong>
                    <div style="font-size: 11px; color: #666;">ID: #{{ comment.maNguoiDung }}</div>
                  </td>
                  <td style="max-width: 320px; word-break: break-word;">
                    {{ comment.noiDung }}
                  </td>
                  <td>
                    <span class="status-badge" :class="comment.isSpam ? 'badge-danger' : 'badge-success'">
                      {{ comment.isSpam ? '⚠️ Vi phạm / Độc hại' : '✅ An toàn' }}
                    </span>
                  </td>
                  <td class="actions">
                    <!-- Nút khóa / mở khóa tài khoản -->
                    <button 
                      class="btn-action" 
                      :class="comment.userTrangThai === 'BI_KHOA' ? 'btn-unlock' : 'btn-lock'"
                      @click="toggleUserAccount(comment.maNguoiDung, comment.userTrangThai)"
                    >
                      <i :class="comment.userTrangThai === 'BI_KHOA' ? 'fa-solid fa-user-check' : 'fa-solid fa-user-lock'"></i>
                      {{ comment.userTrangThai === 'BI_KHOA' ? 'Mở Khóa' : 'Khóa User' }}
                    </button>

                    <!-- Nút xóa bình luận -->
                    <button class="btn-action delete" @click="deleteComment(comment.maDanhGia)" title="Xóa bình luận này">
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="comments.length === 0">
                  <td colspan="5" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                    Chưa có lịch sử bình luận nào được ghi nhận trên hệ thống.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- TAB 3: NHẬT KÝ TRUY CẬP -->
          <div v-if="currentTab === 'logs'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Tài Khoản</th>
                  <th>Địa Chỉ IP</th>
                  <th>Thời Gian Đăng Nhập</th>
                  <th>Trạng Thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(log, idx) in logs" :key="log.maNhatKy || idx">
                  <td>#{{ idx + 1 }}</td>
                  <td>{{ log.email || log.tenDangNhap || 'Khách vãng lai' }}</td>
                  <td><code>{{ log.diaChiIP }}</code></td>
                  <td>{{ formatDate(log.thoiGianDangNhap) }}</td>
                  <td>
                    <span class="status-badge" :class="log.thanhCong ? 'active-status' : 'banned-status'">
                      {{ log.thanhCong ? 'Thành công' : 'Thất bại' }}
                    </span>
                  </td>
                </tr>
                <tr v-if="logs.length === 0">
                  <td colspan="5" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                    Chưa có nhật ký truy cập nào.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';
import { showAlert } from '@/composables/useAlert';

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const isCollapsed = ref(false);
const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

// Đổi tab mặc định thành Kiểm Soát Vi Phạm
const currentTab = ref('locked-users');

const lockedUsers = ref([]);
const comments = ref([]);
const logs = ref([]);

// 🔥 BIẾN LƯU THỜI GIAN HIỆN TẠI (Được cập nhật mỗi giây bởi setInterval)
const currentTime = ref(Date.now());
let timerInterval = null;

// Tải dữ liệu SOC
const loadSocData = async () => {
  try {
    const [resUsers, resComments, resLogs] = await Promise.all([
      fetch(`${API_BASE}/api/soc/locked-users`),
      fetch(`${API_BASE}/api/soc/comments`),
      fetch(`${API_BASE}/api/soc/logs`)
    ]);

    if (resUsers.ok) lockedUsers.value = await resUsers.json();
    if (resComments.ok) comments.value = await resComments.json();
    if (resLogs.ok) logs.value = await resLogs.json();
  } catch (err) {
    console.error('Lỗi kết nối dữ liệu SOC:', err);
  }
};

// 🔥 HÀM TÍNH TOÁN ĐẾM NGƯỢC (Tự động chạy mỗi giây)
const getCountdownData = (user) => {
  // 1. Trạng thái BI_KHOA -> Cấm vĩnh viễn (Vi phạm lần 3)
  if (user.trangThai === 'BI_KHOA') {
    return { text: 'Khóa vĩnh viễn', class: 'ban-forever', icon: 'fa-solid fa-lock' };
  }

  // 2. Không có thời gian cấm -> Không bị phạt
  if (!user.thoiGianCamBinhLuan) {
    return { text: '--', class: 'ban-none', icon: 'fa-solid fa-minus' };
  }

  // 3. Xử lý an toàn kiểu dữ liệu LocalDateTime từ Spring Boot
  let targetTime;
  if (Array.isArray(user.thoiGianCamBinhLuan)) {
    // Nếu Spring Boot nén thành mảng: [2026, 9, 3, 15, 30, 0]
    const [y, m, d, h = 0, min = 0, s = 0] = user.thoiGianCamBinhLuan;
    // JS đếm tháng từ 0 (Tháng 1 = 0) nên phải dùng m - 1
    targetTime = new Date(y, m - 1, d, h, min, s).getTime();
  } else {
    // Nếu Spring Boot trả ra chuỗi ISO: "2026-09-03T15:30:00"
    targetTime = new Date(user.thoiGianCamBinhLuan).getTime();
  }

  // 4. Tính độ lệch thời gian
  const diff = targetTime - currentTime.value;

  // Nếu đếm ngược đã về <= 0
  if (diff <= 0) {
    return { text: 'Đã hết hạn', class: 'ban-expired', icon: 'fa-solid fa-unlock' };
  }

  // Nếu vẫn còn thời gian -> Convert ra Phút:Giây (MM:SS)
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const s = Math.floor((diff % (1000 * 60)) / 1000);

  const formatM = m < 10 ? '0' + m : m;
  const formatS = s < 10 ? '0' + s : s;

  return { 
    text: `${formatM}:${formatS}`, 
    class: 'ban-counting', 
    icon: 'fa-regular fa-clock' 
  };
};

const sortedAndFilteredComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    if (a.isSpam && !b.isSpam) return -1;
    if (!a.isSpam && b.isSpam) return 1;
    return new Date(b.ngayDanhGia) - new Date(a.ngayDanhGia);
  });
});

const manageUser = async (userId, action) => {
  try {
    const res = await fetch(`${API_BASE}/api/soc/users/${userId}/${action}`, { method: 'PUT' });
    if (res.ok) {
      showAlert('Thao tác thành công!', 'success');
      loadSocData();
    }
  } catch (err) {
    showAlert('Lỗi kết nối!', 'error');
  }
};

const toggleUserAccount = async (userId, currentStatus) => {
  const action = (currentStatus === 'BI_KHOA') ? 'unlock' : 'lock';
  const msg = action === 'lock' ? `Khóa khẩn cấp tài khoản #${userId}?` : `Mở khóa cho tài khoản #${userId}?`;
  if (!confirm(msg)) return;

  try {
    const res = await fetch(`${API_BASE}/api/soc/users/${userId}/${action}`, { method: 'PUT' });
    if (res.ok) {
      showAlert('Đã cập nhật trạng thái tài khoản!', 'success');
      loadSocData();
    }
  } catch (err) {
    showAlert('Lỗi kết nối!', 'error');
  }
};

const deleteComment = async (commentId) => {
  if (!confirm('Xóa vĩnh viễn bình luận này khỏi hệ thống?')) return;
  try {
    const res = await fetch(`${API_BASE}/api/soc/comments/${commentId}`, { method: 'DELETE' });
    if (res.ok) {
      showAlert('Đã xóa bình luận thành công!', 'success');
      loadSocData();
    }
  } catch (err) {
    showAlert('Lỗi kết nối!', 'error');
  }
};

const formatDate = (d) => {
  if (!d) return '';
  return new Date(d).toLocaleString('vi-VN');
};

onMounted(() => {
  loadSocData();
  // 🔥 BẬT BỘ ĐẾM GIÂY (Mỗi 1000ms sẽ kích hoạt render lại biến currentTime)
  timerInterval = setInterval(() => {
    currentTime.value = Date.now();
  }, 1000);
});

onUnmounted(() => {
  // Tắt bộ đếm khi thoát trang để tiết kiệm RAM
  if (timerInterval) clearInterval(timerInterval);
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
@import "../CSS/Admin/QuanLyNguoiDung.css";

.velora-admin-wrapper { 
  display: flex; height: 100vh; background-color: var(--bg-page); overflow: hidden; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
}
.content-wrapper { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; }
.content { flex: 1; padding: 30px; }

.soc-header-banner { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.soc-live-badge { background: #e8f5e9; color: #2e7d32; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.pulse-dot { width: 8px; height: 8px; background: #2e7d32; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.95); opacity: 0.8; } 50% { transform: scale(1.3); opacity: 1; } 100% { transform: scale(0.95); opacity: 0.8; } }

/* Chỉnh grid thành 2 cột đều nhau */
.soc-stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 25px; }
.stat-card { padding: 20px; border-radius: 8px; color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.dark-card { background: linear-gradient(135deg, #37474f, #263238); }
.orange-card { background: linear-gradient(135deg, #ef6c00, #e65100); }
.stat-card h3 { font-size: 14px; font-weight: 500; margin-bottom: 8px; opacity: 0.9; }
.stat-number { font-size: 28px; font-weight: bold; }

.soc-tabs-bar { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #eaeaea; padding-bottom: 10px; }
.soc-tab-btn { background: #fff; border: 1px solid #ddd; padding: 10px 18px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; color: #555; transition: all 0.2s; }
.soc-tab-btn:hover { border-color: var(--gold-matte); color: var(--gold-matte); }
.soc-tab-btn.active { background: var(--wood-dark); color: #fff; border-color: var(--wood-dark); }

/* 🔥 CSS RIÊNG CHO CỘT ĐẾM NGƯỢC THỜI GIAN */
.countdown-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace; /* Font kiểu đồng hồ điện tử */
  font-weight: bold;
  font-size: 14px;
  min-width: 110px;
}

.ban-counting { background-color: #fff3e0; color: #e65100; border: 1px solid #ffcc80; }
.ban-expired { background-color: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; }
.ban-forever { background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }
.ban-none { color: #aaa; }

.row-violation { background-color: #fff5f5 !important; }
.row-violation:hover { background-color: #ffe3e3 !important; }

.row-safe { background-color: #f4fbf7 !important; }
.row-safe:hover { background-color: #e6f6ed !important; }

.badge-danger { background-color: #ffebee; color: #c62828; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.badge-success { background-color: #e8f5e9; color: #2e7d32; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }

.btn-lock { background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2; margin-right: 6px; }
.btn-lock:hover { background-color: #f44336; color: #fff; }

.btn-unlock { background-color: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; margin-right: 6px; }
.btn-unlock:hover { background-color: #4caf50; color: #fff; }
</style>