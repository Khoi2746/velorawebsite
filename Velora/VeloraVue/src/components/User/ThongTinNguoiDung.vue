<template>
  <div class="profile-page">
    <Header />

    <main class="profile-content">
      <div class="container">
        <div class="title-wrapper">
          <h1 class="page-title">THÔNG TIN CÁ NHÂN</h1>
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <div class="profile-layout">
          <!-- Cột Menu bên trái -->
          <div class="profile-sidebar">
            <div class="user-avatar">
              <i class="fas fa-user-circle"></i>
              <h3>{{ userInfo.hoTen || 'Thành Viên' }}</h3>
              <p class="role-tag">{{ isAdmin ? 'Quản Trị Viên' : 'Khách Hàng VIP' }}</p>
            </div>
            <ul class="sidebar-menu">
              <li>
                <a href="#" :class="{ active: activeTab === 'profile' }" @click.prevent="activeTab = 'profile'">
                  Hồ sơ của tôi
                </a>
              </li>
              <li>
                <router-link to="/don-hang">Lịch sử đơn hàng</router-link>
              </li>
              <!-- NÚT LỊCH SỬ LỊCH HẸN -->
              <li>
                <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="switchTab('appointments')">
                  Lịch sử lịch hẹn
                </a>
              </li>
              <li>
                <a href="#" @click.prevent="logout" class="text-danger">Đăng xuất</a>
              </li>
            </ul>
          </div>

          <!-- Cột bên phải -->
          <div class="profile-form-section">
            
            <!-- TAB 1: FORM HỒ SƠ CỦA TÔI -->
            <div v-if="activeTab === 'profile'">
              <h2 class="section-title">HỒ SƠ CỦA TÔI</h2>
              <p class="section-desc">Quản lý thông tin bảo mật để nhận các đặc quyền từ Velora.</p>

              <form @submit.prevent="updateProfile" class="velora-form">
                <div class="form-group">
                  <label>Họ và tên</label>
                  <input type="text" v-model="userInfo.hoTen" placeholder="Nhập họ và tên..." required />
                </div>

                <div class="form-group">
                  <label>Email (Tài khoản)</label>
                  <input type="email" v-model="userInfo.email" class="readonly-input" readonly title="Không thể thay đổi email" />
                </div>

                <div class="form-group">
                  <label>Số điện thoại</label>
                  <input type="tel" v-model="userInfo.soDienThoai" placeholder="Nhập số điện thoại liên hệ..." />
                </div>

                <div class="form-group">
                  <label>Địa chỉ giao hàng mặc định</label>
                  <textarea v-model="userInfo.diaChi" rows="3" placeholder="Nhập địa chỉ nhận hàng chi tiết..."></textarea>
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn-primary" :disabled="isUpdating">
                    {{ isUpdating ? 'ĐANG CẬP NHẬT...' : 'LƯU THAY ĐỔI' }}
                  </button>
                </div>

                <p v-if="successMsg" class="msg success"><i class="fas fa-check-circle"></i> {{ successMsg }}</p>
                <p v-if="errorMsg" class="msg error"><i class="fas fa-exclamation-circle"></i> {{ errorMsg }}</p>
              </form>
            </div>

            <!-- TAB 2: QUẢN LÝ LỊCH SỬ LỊCH HẸN -->
            <div v-if="activeTab === 'appointments'">
              <h2 class="section-title">LỊCH SỬ LỊCH HẸN</h2>
              <p class="section-desc">Theo dõi trạng thái các lịch hẹn trải nghiệm và dịch vụ tại Velora.</p>

              <!-- Bộ lọc trạng thái -->
              <div class="appointment-filters">
                <button 
                  v-for="status in ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED']" 
                  :key="status"
                  :class="['filter-btn', selectedStatus === status ? 'active' : '']"
                  @click="selectedStatus = status"
                >
                  {{ getStatusText(status) }}
                </button>
              </div>

              <!-- Trạng thái Loading -->
              <div v-if="isLoadingAppointments" class="text-center py-4">
                <i class="fas fa-spinner fa-spin fa-2x"></i>
                <p>Đang tải danh sách lịch hẹn...</p>
              </div>

              <!-- Danh sách lịch hẹn -->
              <div v-else-if="filteredAppointments.length > 0" class="appointment-list">
                <div v-for="item in filteredAppointments" :key="item.id || item.maLichHen" class="appointment-card">
                  <div class="card-header">
                    <span class="appointment-id">Mã lịch hẹn: #{{ item.id || item.maLichHen }}</span>
                    <span :class="['status-badge', getBadgeClass(item.status || item.trangThai)]">
                      {{ getStatusText(item.status || item.trangThai) }}
                    </span>
                  </div>

                  <div class="card-body">
                    <div class="info-row">
                      <i class="far fa-clock"></i>
                      <span><strong>Thời gian:</strong> {{ formatDateTime(item.appointmentTime || item.thoiGianHen) }}</span>
                    </div>
                    <div class="info-row">
                      <i class="fas fa-map-marker-alt"></i>
                      <span><strong>Chi nhánh:</strong> {{ item.location || item.diaDiem || 'Showroom Velora' }}</span>
                    </div>
                    <div class="info-row">
                      <i class="fas fa-concierge-bell"></i>
                      <span><strong>Dịch vụ:</strong> {{ item.serviceName || item.tenDichVu || 'Tư vấn & Thử đồng hồ' }}</span>
                    </div>
                    <div v-if="item.note || item.ghiChu" class="info-row">
                      <i class="far fa-comment-alt"></i>
                      <span><strong>Ghi chú:</strong> {{ item.note || item.ghiChu }}</span>
                    </div>
                  </div>

                  <div v-if="(item.status || item.trangThai) === 'PENDING'" class="card-footer">
                    <button class="btn-cancel" @click="cancelAppointment(item.id || item.maLichHen)">
                      Hủy lịch hẹn
                    </button>
                  </div>
                </div>
              </div>

              <!-- Trạng thái chưa có lịch hẹn -->
              <div v-else class="empty-state">
                <i class="far fa-calendar-times"></i>
                <p>Chưa có lịch hẹn nào được ghi nhận.</p>
              </div>
            </div>

          </div>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Header from '../Header.vue';
import Footer from '../Footer.vue';

const router = useRouter();

// Tab hiện tại ('profile' hoặc 'appointments')
const activeTab = ref('profile');

// Thông tin người dùng
const userInfo = ref({
  maNguoiDung: '',
  hoTen: '',
  email: '',
  soDienThoai: '',
  diaChi: ''
});

const isAdmin = ref(false);
const isUpdating = ref(false);
const successMsg = ref('');
const errorMsg = ref('');

// Quản lý Lịch hẹn
const appointments = ref([]);
const selectedStatus = ref('ALL');
const isLoadingAppointments = ref(false);

// Khởi tạo thông tin người dùng khi Mounted
onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    alert('Vui lòng đăng nhập để xem thông tin!');
    router.push('/dang-nhap');
    return;
  }

  const user = JSON.parse(userStr);
  userInfo.value.maNguoiDung = user.maNguoiDung;
  userInfo.value.hoTen = user.hoTen;
  userInfo.value.email = user.email;
  userInfo.value.soDienThoai = user.soDienThoai || '';
  userInfo.value.diaChi = user.diaChi || '';

  isAdmin.value = (user.vaiTro && user.vaiTro.toUpperCase() === 'ROLE_ADMIN');
});

// Chuyển tab
const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === 'appointments') {
    fetchAppointments();
  }
};

// Hàm hỗ trợ map số trangThai sang Enum Tiếng Anh cho badge CSS
const parseTrangThai = (trangThaiNum) => {
  switch (trangThaiNum) {
    case 0: return 'PENDING';     // Chờ xác nhận
    case 1: return 'CONFIRMED';   // Đã xác nhận
    case 2: return 'COMPLETED';   // Hoàn thành
    case 3: return 'CANCELLED';   // Đã hủy
    default: return 'PENDING';
  }
};

// Gọi API lấy lịch sử lịch hẹn dựa trên EMAIL của user
const fetchAppointments = async () => {
  isLoadingAppointments.value = true;

  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;
  const userEmail = userInfo.value.email || (user ? user.email : null);

  if (!userEmail) {
    console.error('Không tìm thấy Email người dùng!');
    isLoadingAppointments.value = false;
    return;
  }

  const apiUrl = `http://localhost:8080/api/lich-hen/nguoi-dung/email/${userEmail}`;

  try {
    const res = await fetch(apiUrl);
    if (res.ok) {
      const data = await res.json();
      
      appointments.value = data.map(item => ({
        id: item.id,
        appointmentTime: `${item.ngayHen || ''} ${item.thoiGian || ''}`.trim(),
        location: 'Showroom Velora',
        serviceName: item.sanPham ? item.sanPham.tenSanPham : 'Tư vấn & Thử đồng hồ',
        note: item.ghiChu,
        status: parseTrangThai(item.trangThai)
      }));
    } else {
      console.error(`Lỗi API (${res.status}):`, await res.text());
    }
  } catch (error) {
    console.error('Lỗi kết nối Backend:', error);
  } finally {
    isLoadingAppointments.value = false;
  }
};

// Hàm hủy lịch hẹn từ phía khách hàng
const cancelAppointment = async (id) => {
  if (confirm('Bạn có chắc chắn muốn hủy lịch hẹn này không?')) {
    try {
      const res = await fetch(`http://localhost:8080/api/lich-hen/huy/${id}`, {
        method: 'PUT'
      });
      if (res.ok) {
        alert('Đã hủy lịch hẹn thành công!');
        await fetchAppointments();
      } else {
        alert('Không thể hủy lịch hẹn!');
      }
    } catch (error) {
      console.error('Lỗi khi hủy lịch hẹn:', error);
    }
  }
};

// Lọc lịch hẹn theo trạng thái chọn
const filteredAppointments = computed(() => {
  if (selectedStatus.value === 'ALL') return appointments.value;
  return appointments.value.filter(item => (item.status || item.trangThai) === selectedStatus.value);
});

// Định dạng văn bản hiển thị cho trạng thái
const getStatusText = (status) => {
  const map = {
    ALL: 'Tất cả',
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy'
  };
  return map[status] || status;
};

// Gán class màu cho Badge
const getBadgeClass = (status) => {
  const map = {
    PENDING: 'badge-pending',
    CONFIRMED: 'badge-confirmed',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled'
  };
  return map[status] || 'badge-default';
};

// Format ngày giờ hiển thị
const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;
  return date.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const clearMessages = () => {
  successMsg.value = '';
  errorMsg.value = '';
};

// Cập nhật thông tin Hồ sơ cá nhân
const updateProfile = async () => {
  isUpdating.value = true;
  clearMessages();

  try {
    const res = await fetch(`http://localhost:8080/api/admin/cap-nhat/${userInfo.value.maNguoiDung}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        hoTen: userInfo.value.hoTen,
        soDienThoai: userInfo.value.soDienThoai,
        diaChi: userInfo.value.diaChi
      })
    });

    if (res.ok) {
      const updatedUser = await res.json();
      let currentUser = JSON.parse(localStorage.getItem('user'));
      currentUser.hoTen = updatedUser.hoTen;
      currentUser.soDienThoai = updatedUser.soDienThoai;
      currentUser.diaChi = updatedUser.diaChi;

      localStorage.setItem('user', JSON.stringify(currentUser));
      successMsg.value = 'Cập nhật thông tin thành công!';
      window.dispatchEvent(new Event('user-updated'));
    } else {
      const errorText = await res.text();
      console.error("Java báo lỗi:", errorText);
      errorMsg.value = `Lỗi Backend (Mã ${res.status})`;
    }
  } catch (error) {
    console.error('Lỗi kết nối:', error);
    errorMsg.value = 'Không thể kết nối đến máy chủ.';
  } finally {
    isUpdating.value = false;
  }
};

const logout = () => {
  localStorage.removeItem('user');
  alert('Đã đăng xuất!');
  window.location.href = '/';
};
</script>

<style scoped>
@import "../CSS/User/ThongTinNguoiDung.css";
</style>