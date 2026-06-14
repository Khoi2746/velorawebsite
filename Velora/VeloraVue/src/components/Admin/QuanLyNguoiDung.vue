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
          <h1>Quản Lý <span class="gold">Người Dùng</span></h1>
          <p>Danh sách khách hàng và nhân viên trên hệ thống.</p>
        </div>
        <div class="header-right">
          <button class="btn-add"><i class="fa-solid fa-user-plus"></i> Thêm Tài Khoản</button>
        </div>
      </header>

      <section class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Họ Tên</th>
              <th>Email</th>
              <th>Số Điện Thoại</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.maNguoiDung">
              <td>#{{ user.maNguoiDung }}</td>
              <td class="user-name">
                <div class="user-info-flex">
                  <div class="avatar-placeholder"><i class="fa-solid fa-user"></i></div>
                  <span>{{ user.hoTen }}</span>
                </div>
              </td>
              <td class="email-text">{{ user.email }}</td>
              <td>{{ user.soDienThoai || 'Chưa cập nhật' }}</td>
              <td>
                <span class="status-badge" :class="user.trangThai === 'HOAT_DONG' ? 'active-status' : 'banned-status'">
                  {{ user.trangThai === 'HOAT_DONG' ? 'Đang Hoạt Động' : 'Bị Khóa' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-action edit" title="Chỉnh sửa thông tin"><i class="fa-solid fa-pen"></i></button>
                <button class="btn-action lock" title="Khóa tài khoản"><i class="fa-solid fa-lock"></i></button>
              </td>
            </tr>
            <tr v-if="users.length === 0">
              <td colspan="6" class="empty-state">Đang tải dữ liệu hoặc chưa có người dùng nào...</td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// Menu Sidebar
const menuItems = [
  { name: 'Dashboard', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
  { name: 'Products', link: '/admin/products', icon: 'fa-solid fa-box-open' },
  { name: 'Users', link: '/admin/users', icon: 'fa-solid fa-users' },
  { name: 'Orders', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' }
];

const users = ref([]);

// Gọi API lấy dữ liệu người dùng từ Spring Boot
// Giả định API của em là /api/nguoi-dung (sửa lại nếu em đặt tên khác trong Controller)
const loadUsers = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/nguoi-dung');
    if (res.ok) {
      users.value = await res.json();
    } else {
      console.warn("Chưa có API lấy danh sách người dùng hoặc sai đường dẫn.");
    }
  } catch (error) {
    console.error('Lỗi kết nối Backend:', error);
  }
};

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/';
};

// Chạy tự động khi trang vừa load
onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
/* ================= LAYOUT GỐC (ĐỒNG BỘ TỪ PAGE SẢN PHẨM) ================= */
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

/* ================= TRANG QUẢN LÝ ================= */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header h1 { color: #3e332e; font-size: 32px; margin-bottom: 5px; }
.header p { color: #888; font-size: 14px; }

.btn-add {
  background-color: #d1aa68;
  color: #111;
  border: none;
  padding: 12px 24px;
  font-weight: bold;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.btn-add:hover {
  background-color: #b8955b;
  transform: translateY(-2px);
}

/* ================= BẢNG DỮ LIỆU (TABLE) ================= */
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

/* Cột User Info (Avatar + Tên) */
.user-info-flex {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: bold;
  color: #3e332e;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  background-color: #f4f1ea;
  color: #d1aa68;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.email-text {
  color: #666;
}

/* Badge Trạng thái */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
}
.active-status { background-color: #e8f5e9; color: #2e7d32; }
.banned-status { background-color: #ffebee; color: #c62828; }

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

.btn-action.edit {
  background-color: #f4f1ea;
  color: #3e332e;
}
.btn-action.edit:hover {
  background-color: #d1aa68;
  color: #fff;
}

.btn-action.lock {
  background-color: #ffebee;
  color: #c62828;
}
.btn-action.lock:hover {
  background-color: #c62828;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: #888;
}
</style>