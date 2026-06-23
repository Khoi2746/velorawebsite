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
          <button class="btn-add" @click="openAddModal">
            <i class="fa-solid fa-user-plus"></i> Thêm Tài Khoản
          </button>
        </div>
      </header>

      <section v-if="showForm" class="table-container" style="margin-bottom: 30px; padding: 25px;">
        <h3 class="gold" style="margin-bottom: 20px;">
          {{ isEditMode ? 'Chỉnh Sửa Thành Viên #' + userForm.maNguoiDung : 'Tạo Tài Khoản Mới' }}
        </h3>
        <form @submit.prevent="saveUser">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
            <div>
              <label style="display:block; margin-bottom:8px; color:#3e332e; font-weight:bold;">Họ và Tên</label>
              <input type="text" v-model="userForm.hoTen" required
                style="width:100%; padding:10px; border:1px solid #e0dcd5; border-radius:4px;">
            </div>
            <div>
              <label style="display:block; margin-bottom:8px; color:#3e332e; font-weight:bold;">Email Đăng Nhập</label>
              <input type="email" v-model="userForm.email" :disabled="isEditMode" required
                style="width:100%; padding:10px; border:1px solid #e0dcd5; border-radius:4px; background-color: #f5f5f5;">
            </div>
            <div>
              <label style="display:block; margin-bottom:8px; color:#3e332e; font-weight:bold;">Số Điện Thoại</label>
              <input type="text" v-model="userForm.soDienThoai"
                style="width:100%; padding:10px; border:1px solid #e0dcd5; border-radius:4px;">
            </div>
            <div>
              <label style="display:block; margin-bottom:8px; color:#3e332e; font-weight:bold;">Địa Chỉ</label>
              <input type="text" v-model="userForm.diaChi"
                style="width:100%; padding:10px; border:1px solid #e0dcd5; border-radius:4px;">
            </div>
            <div>
              <label style="display:block; margin-bottom:8px; color:#3e332e; font-weight:bold;">Phân Quyền</label>
              <select v-model="selectedRoleId" @change="updateRoleInForm($event)"
                style="width:100%; padding:10px; border:1px solid #e0dcd5; border-radius:4px; background:#fff;">
                <option value="3">ROLE_CUSTOMER (Khách hàng)</option>
                <option value="2">ROLE_STAFF (Nhân viên)</option>
                <option value="1">ROLE_ADMIN (Quản trị viên)</option>
              </select>
            </div>
          </div>
          <div style="display: flex; gap: 10px;">
            <button type="submit" class="btn-add">Lưu Thông Tin</button>
            <button type="button" @click="closeForm"
              style="background:#f4f1ea; border:1px solid #e0dcd5; padding:12px 24px; border-radius:6px; cursor:pointer; font-weight:bold;">Hủy</button>
          </div>
        </form>
      </section>

      <section class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Họ Tên</th>
              <th>Email</th>
              <th>Số Điện Thoại</th>
              <th>Vai Trò</th>
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
                <template v-if="user.vaiTros && user.vaiTros.length > 0">
                  <span v-for="vt in user.vaiTros" :key="vt.maVaiTro" class="status-badge"
                    :class="vt.tenVaiTro === 'ROLE_ADMIN' ? 'banned-status' : (vt.tenVaiTro === 'ROLE_STAFF' ? 'warn-status' : 'active-status')"
                    style="margin-right: 4px; display: inline-block;">
                    {{ vt.tenVaiTro }}
                  </span>
                </template>
                <span v-else class="text-muted" style="font-style: italic; font-size: 0.85rem;">
                  CHƯA GÁN
                </span>
              </td>
              <td>
                <span class="status-badge" :class="user.trangThai === 'HOAT_DONG' ? 'active-status' : 'banned-status'">
                  {{ user.trangThai === 'HOAT_DONG' ? 'Đang Hoạt Động' : 'Bị Khóa' }}
                </span>
              </td>

              <td class="actions">
                <button class="btn-action edit" @click="openEditModal(user)" title="Chỉnh sửa thông tin">
                  <i class="fa-solid fa-pen"></i>
                </button>
                <button class="btn-action delete" @click="deleteUser(user.maNguoiDung)" title="Xóa tài khoản">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </td>

            </tr>
            <tr v-if="users.length === 0">
              <td colspan="7" class="empty-state">Đang nạp dữ liệu từ hệ thống Velora...</td>
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
    { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge' },
    { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open' },
    { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group' },
    { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users' },
    { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice' },
    { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked' },
    { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar' },
    { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem' },
    { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list' },
    { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', requiresAdmin: true }
];


const users = ref([]);
const showForm = ref(false);
const isEditMode = ref(false);
const selectedRoleId = ref(3);

const userForm = ref({
  maNguoiDung: null,
  hoTen: '',
  email: '',
  soDienThoai: '',
  diaChi: '',
  trangThai: 'HOAT_DONG',
  vaiTros: []
});

const loadUsers = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/admin/thanh-vien', {
      method: 'GET',
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      }
    });
    if (res.ok) {
      users.value = await res.json();
    } else {
      console.error('Server trả về mã lỗi:', res.status);
    }
  } catch (error) {
    console.error('Lỗi kết nối API lấy danh sách:', error);
  }
};

const updateRoleInForm = (event) => {
  const value = parseInt(event.target.value);
  let name = 'ROLE_CUSTOMER';
  if (value === 1) name = 'ROLE_ADMIN';
  if (value === 2) name = 'ROLE_STAFF';

  userForm.value.vaiTros = [{ maVaiTro: value, tenVaiTro: name }];
};

const openAddModal = () => {
  isEditMode.value = false;
  selectedRoleId.value = 3;
  userForm.value = {
    maNguoiDung: null,
    hoTen: '',
    email: '',
    soDienThoai: '',
    diaChi: '',
    trangThai: 'HOAT_DONG',
    vaiTros: [{ maVaiTro: 3, tenVaiTro: 'ROLE_CUSTOMER' }]
  };
  showForm.value = true;
};

const openEditModal = (user) => {
  isEditMode.value = true;
  userForm.value = JSON.parse(JSON.stringify(user));

  if (user.vaiTros && user.vaiTros.length > 0) {
    selectedRoleId.value = user.vaiTros[0].maVaiTro;
  } else {
    selectedRoleId.value = 3;
    userForm.value.vaiTros = [{ maVaiTro: 3, tenVaiTro: 'ROLE_CUSTOMER' }];
  }
  showForm.value = true;
};

const closeForm = () => { showForm.value = false; };

const saveUser = async () => {
  const url = isEditMode.value
    ? `http://localhost:8080/api/admin/thanh-vien/${userForm.value.maNguoiDung}`
    : 'http://localhost:8080/api/admin/thanh-vien';

  const method = isEditMode.value ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(userForm.value)
    });

    if (res.ok) {
      alert(isEditMode.value ? 'Cập nhật thành công!' : 'Thêm tài khoản thành công!');
      showForm.value = false;
      await loadUsers();
    } else {
      const errText = await res.text();
      alert("Xảy ra lỗi từ hệ thống: " + errText);
    }
  } catch (error) {
    alert("Không thể kết nối đến máy chủ Backend.");
  }
};

// HÀM XÓA TÀI KHOẢN MỚI THÊM VÀO
const deleteUser = async (id) => {
  if (!confirm("Bạn có chắc chắn muốn XÓA tài khoản này khỏi hệ thống?")) return;
  try {
    const res = await fetch(`http://localhost:8080/api/admin/thanh-vien/${id}`, {
      method: 'DELETE',
      headers: { 'Accept': 'application/json' }
    });
    if (res.ok) {
      alert("Đã xóa tài khoản thành công!");
      await loadUsers();
    } else {
      console.error('Lỗi xóa, mã lỗi:', res.status);
      alert("Không thể xóa tài khoản này! (Có thể do tài khoản này đang dính líu tới Đơn Hàng hoặc Đánh Giá trong database).");
    }
  } catch (error) {
    console.error('Lỗi kết nối khi xóa:', error);
  }
};

const handleLogout = () => {
  localStorage.removeItem('user');
  window.location.href = '/';
};

onMounted(() => { loadUsers(); });
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

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
}

.active-status {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.banned-status {
  background-color: #ffebee;
  color: #c62828;
}

.warn-status {
  background-color: #fff3e0;
  color: #e65100;
}

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

/* CSS MỚI CHO NÚT XÓA */
.btn-action.delete {
  background-color: #ffebee;
  color: #c62828;
}

.btn-action.delete:hover {
  background-color: #c62828;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
  color: #888;
}
</style>