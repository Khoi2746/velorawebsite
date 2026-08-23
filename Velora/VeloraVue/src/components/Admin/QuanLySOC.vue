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
            <p>Hệ thống giám sát an ninh mạng chủ động, phòng chống dò quét và kiểm duyệt nội dung thời gian thực.</p>
          </div>
          <div class="header-right">
            <div class="soc-live-badge">
              <span class="pulse-dot"></span> Cảnh Báo Động - Đang Giám Sát
            </div>
          </div>
        </header>

        <!-- Thẻ thống kê tổng quan -->
        <div class="soc-stats-grid">
          <div class="stat-card red-card">
            <h3>Sự Cố Mạng Chờ Xử Lý</h3>
            <div class="stat-number">{{ alerts.length }}</div>
          </div>
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
          <button class="soc-tab-btn" :class="{ active: currentTab === 'threats' }" @click="currentTab = 'threats'">
            <i class="fa-solid fa-radar"></i> Ra-da An Ninh (Threats)
          </button>
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
          
          <!-- TAB 1: RA-DA AN NINH -->
          <div v-if="currentTab === 'threats'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Thời Gian</th>
                  <th>Địa Chỉ IP</th>
                  <th>Loại Tấn Công</th>
                  <th>Mức Độ</th>
                  <th>Mô Tả Chi Tiết</th>
                  <th>Hành Động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="alert in alerts" :key="alert.maCanhBao">
                  <td>{{ formatDate(alert.ngayTao) }}</td>
                  <td><code>{{ alert.diaChiIP }}</code></td>
                  <td><span class="status-badge banned-status">{{ alert.loaiTanCong }}</span></td>
                  <td><span class="status-badge warn-status">{{ alert.mucDoNguyHiem }}</span></td>
                  <td>{{ alert.moTaChiTiet }}</td>
                  <td>
                    <button class="btn-action" style="background: #ffebee; color: #c62828;" @click="blockIp(alert.diaChiIP, alert.maCanhBao)">
                      <i class="fa-solid fa-ban"></i> Cô Lập IP
                    </button>
                  </td>
                </tr>
                <tr v-if="alerts.length === 0">
                  <td colspan="6" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                    Hệ thống an toàn. Không phát hiện cuộc tấn công nào.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- TAB 2: KIỂM SOÁT VI PHẠM (USER) -->
          <div v-if="currentTab === 'locked-users'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Mã User</th>
                  <th>Họ Tên</th>
                  <th>Email</th>
                  <th>Trạng Thái</th>
                  <th>Lý Do Vi Phạm</th>
                  <th>Hành Động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in lockedUsers" :key="u.maNguoiDung">
                  <td>#{{ u.maNguoiDung }}</td>
                  <td><strong>{{ u.hoTen }}</strong></td>
                  <td>{{ u.email }}</td>
                  <td><span class="status-badge banned-status">{{ u.trangThai }}</span></td>
                  <td>{{ u.lyDoViPham }}</td>
                  <td>
                    <button class="btn-action btn-unlock" @click="manageUser(u.maNguoiDung, 'unlock')">
                      <i class="fa-solid fa-user-check"></i> Mở Khóa
                    </button>
                  </td>
                </tr>
                <tr v-if="lockedUsers.length === 0">
                  <td colspan="6" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                    Không có tài khoản nào bị đình chỉ.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- TAB 3: BỘ LỌC NỘI DUNG (ĐÃ TÍCH HỢP ĐỒN LÊN ĐẦU, MÀU SẮC VÀ KHÓA USER) -->
          <div v-if="currentTab === 'comments'">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Thời Gian</th>
                  <th>Định Danh User</th>
                  <th>Nội Dung Chi Tiết</th>
                  <th>Trạng Thái Nội Dung</th>
                  <th>Hành Động Xử Lý (Khóa/Xóa)</th>
                </tr>
              </thead>
              <tbody>
                <!-- Dùng computed property sortedAndFilteredComments để đôn vi phạm lên trên -->
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
                    <!-- Nút khóa / mở khóa tài khoản ngay trực tiếp tại dòng bình luận -->
                    <button 
                      class="btn-action" 
                      :class="comment.userTrangThai === 'BI_KHOA' ? 'btn-unlock' : 'btn-lock'"
                      @click="toggleUserAccount(comment.maNguoiDung, comment.userTrangThai)"
                    >
                      <i :class="comment.userTrangThai === 'BI_KHOA' ? 'fa-solid fa-user-check' : 'fa-solid fa-user-lock'"></i>
                      {{ comment.userTrangThai === 'BI_KHOA' ? 'Mở Khóa User' : 'Khóa User' }}
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

          <!-- TAB 4: NHẬT KÝ TRUY CẬP -->
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
import { ref, computed, onMounted } from 'vue';
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';
import { showAlert } from '@/composables/useAlert';

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const isCollapsed = ref(false);
const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

const currentTab = ref('threats');

const alerts = ref([]);
const lockedUsers = ref([]);
const comments = ref([]);
const logs = ref([]);

// Tải toàn bộ dữ liệu SOC
const loadSocData = async () => {
  try {
    const [resAlerts, resUsers, resComments, resLogs] = await Promise.all([
      fetch(`${API_BASE}/api/soc/alerts`),
      fetch(`${API_BASE}/api/soc/locked-users`),
      fetch(`${API_BASE}/api/soc/comments`),
      fetch(`${API_BASE}/api/soc/logs`)
    ]);

    if (resAlerts.ok) alerts.value = await resAlerts.json();
    if (resUsers.ok) lockedUsers.value = await resUsers.json();
    if (resComments.ok) comments.value = await resComments.json();
    if (resLogs.ok) logs.value = await resLogs.json();
  } catch (err) {
    console.error('Lỗi kết nối dữ liệu SOC:', err);
  }
};

// 🔥 LOGIC CỰC KỲ QUAN TRỌNG: Đôn các bình luận vi phạm (isSpam = true) lên trên đầu bảng
const sortedAndFilteredComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    if (a.isSpam && !b.isSpam) return -1;
    if (!a.isSpam && b.isSpam) return 1;
    return new Date(b.ngayDanhGia) - new Date(a.ngayDanhGia);
  });
});

// Hành động cô lập IP từ bảng Threats
const blockIp = async (ip, maCanhBao) => {
  if (!confirm(`Xác nhận cô lập địa chỉ IP: ${ip}?`)) return;
  try {
    const res = await fetch(`${API_BASE}/api/soc/block-ip`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ip, maCanhBao })
    });
    if (res.ok) {
      showAlert('Đã cô lập IP thành công vào danh sách đen!', 'success');
      loadSocData();
    }
  } catch (err) {
    showAlert('Lỗi kết nối hệ thống!', 'error');
  }
};

// Hành động quản lý tài khoản user từ tab Kiểm soát vi phạm
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

// Hành động khóa / mở khóa user ngay tại bảng Bộ Lọc Nội Dung
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

// Xóa bình luận độc hại
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

/* Banner header SOC */
.soc-header-banner { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.soc-live-badge { background: #ffebee; color: #c62828; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.pulse-dot { width: 8px; height: 8px; background: #c62828; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.95); opacity: 0.8; } 50% { transform: scale(1.3); opacity: 1; } 100% { transform: scale(0.95); opacity: 0.8; } }

/* Thẻ Thống Kê */
.soc-stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 25px; }
.stat-card { padding: 20px; border-radius: 8px; color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.red-card { background: linear-gradient(135deg, #d32f2f, #b71c1c); }
.dark-card { background: linear-gradient(135deg, #37474f, #263238); }
.orange-card { background: linear-gradient(135deg, #ef6c00, #e65100); }
.stat-card h3 { font-size: 14px; font-weight: 500; margin-bottom: 8px; opacity: 0.9; }
.stat-number { font-size: 28px; font-weight: bold; }

/* Tabs điều hướng SOC */
.soc-tabs-bar { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #eaeaea; padding-bottom: 10px; }
.soc-tab-btn { background: #fff; border: 1px solid #ddd; padding: 10px 18px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; color: #555; transition: all 0.2s; }
.soc-tab-btn:hover { border-color: var(--gold-matte); color: var(--gold-matte); }
.soc-tab-btn.active { background: var(--wood-dark); color: #fff; border-color: var(--wood-dark); }

/* 🔥 HIỆU ỨNG MÀU SẮC ĐỎ NHẬT & XANH LÁ NHẬT CHO BỘ LỌC NỘI DUNG */
.row-violation {
  background-color: #fff5f5 !important; /* Đỏ nhạt cảnh báo */
}
.row-violation:hover {
  background-color: #ffe3e3 !important;
}

.row-safe {
  background-color: #f4fbf7 !important; /* Xanh lá nhạt an toàn */
}
.row-safe:hover {
  background-color: #e6f6ed !important;
}

/* Badge trạng thái nội dung */
.badge-danger { background-color: #ffebee; color: #c62828; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.badge-success { background-color: #e8f5e9; color: #2e7d32; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }

/* Nút khóa/mở khóa tài khoản */
.btn-lock { background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2; margin-right: 6px; }
.btn-lock:hover { background-color: #f44336; color: #fff; }

.btn-unlock { background-color: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; margin-right: 6px; }
.btn-unlock:hover { background-color: #4caf50; color: #fff; }
</style>