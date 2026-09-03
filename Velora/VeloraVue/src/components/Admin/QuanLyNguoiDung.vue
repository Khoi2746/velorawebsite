<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- 1. GỌI COMPONENT SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. GỌI COMPONENT HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH -->
      <main class="content">
        <header class="header">
          <div class="header-left">
            <h1>Quản Lý <span class="gold">Người Dùng</span></h1>
            <p>Danh sách khách hàng và nhân viên trên hệ thống thương mại điện tử.</p>
          </div>
          <div class="header-right">
            <button class="btn-add" @click="openAddModal">
              <i class="fa-solid fa-user-plus"></i> Thêm Tài Khoản
            </button>
          </div>
        </header>

        <!-- Thanh Tìm Kiếm và Lọc Dữ Liệu -->
        <section class="filter-bar" style="display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
          <div class="search-box" style="flex: 2; min-width: 250px;">
            <input type="text" v-model="searchQuery" @input="currentPage = 1" 
              placeholder="Tìm theo tên, email hoặc số điện thoại..." 
              style="width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; outline: none;" />
          </div>
          <div class="filter-box" style="flex: 1; min-width: 180px;">
            <select v-model="filterRole" @change="currentPage = 1" style="width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; outline: none; background: #fff; cursor: pointer;">
              <option value="">-- Tất cả vai trò --</option>
              <option value="ROLE_ADMIN">ROLE_ADMIN (Quản trị)</option>
              <option value="ROLE_SALE">ROLE_SALE (Bán hàng)</option>
              <option value="ROLE_CUSTOMER">ROLE_CUSTOMER (Khách hàng)</option>
              <option value="ROLE_CHUYEN_VIEN_TU_VAN">ROLE_CHUYEN_VIEN_TU_VAN (Tư vấn)</option>
              <option value="ROLE_INVENTORY">ROLE_INVENTORY (Quản lý kho)</option>
            </select>
          </div>
          <div class="filter-box" style="flex: 1; min-width: 180px;">
            <select v-model="filterStatus" @change="currentPage = 1" style="width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; outline: none; background: #fff; cursor: pointer;">
              <option value="">-- Tất cả trạng thái --</option>
              <option value="HOAT_DONG">Đang Hoạt Động</option>
              <option value="KHOA">Bị Khóa</option>
            </select>
          </div>
        </section>

        <section class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Họ Tên</th>
                <th>Email</th>
                <th>Số Điện Thoại</th>
                <th>Vai Trò</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(user, index) in paginatedUsers" :key="user.maNguoiDung">
                <td>#{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
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
                      :class="getRoleBadgeClass(vt.tenVaiTro)"
                      style="margin-right: 4px; display: inline-block;">
                      {{ vt.tenVaiTro }}
                    </span>
                  </template>
                  <span v-else class="text-muted" style="font-style: italic; font-size: 0.85rem;">
                    CHƯA GÁN
                  </span>
                </td>
                <td>
                  <span class="status-badge"
                    :class="(user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'active-status' : 'banned-status'">
                    {{ (user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'Đang Hoạt Động' : 'Bị Khóa' }}
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
              <tr v-if="filteredUsers.length === 0">
                <td colspan="7" class="empty-state" style="text-align: center; padding: 30px; color: #888;">
                  Không tìm thấy người dùng nào phù hợp với bộ lọc.
                </td>
              </tr>
            </tbody>
          </table>

          <div class="pagination-wrapper" v-if="totalPages > 1">
            <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
              <i class="fa-solid fa-chevron-left"></i> Trước
            </button>

            <span class="page-info">Trang <b>{{ currentPage }}</b> / {{ totalPages }}</span>

            <button class="page-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
              Sau <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </section>
      </main>

      <!-- Modal Form Thêm/Sửa Tài Khoản -->
      <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
        <div class="modal-box">
          <div class="modal-header">
            <h3>{{ isEditMode ? 'Chỉnh Sửa Thành Viên #' + userForm.maNguoiDung : 'Tạo Tài Khoản Mới' }}</h3>
            <button class="close-btn" @click="closeForm">&times;</button>
          </div>
          <form @submit.prevent="saveUser">
            <div class="form-group">
              <label>Họ và Tên *</label>
              <input type="text" v-model="userForm.hoTen" :disabled="isEditMode" required
                :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}"
                placeholder="Ví dụ: Nguyễn Văn A" />
            </div>

            <div class="form-group">
              <label>Email Đăng Nhập *</label>
              <input type="email" v-model="userForm.email" :disabled="isEditMode" required
                :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}"
                placeholder="name@example.com" />
            </div>

            <div class="form-group" v-if="!isEditMode">
              <label>Mật Khẩu Khởi Tạo *</label>
              <input type="password" v-model="userForm.matKhau" required placeholder="Nhập mật khẩu tối thiểu 6 ký tự" />
            </div>

            <div class="form-group">
              <label>Số Điện Thoại</label>
              <input type="text" v-model="userForm.soDienThoai" :disabled="isEditMode"
                :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}"
                placeholder="Nhập số điện thoại" />
            </div>

            <div class="form-group">
              <label>Địa Chỉ</label>
              <input type="text" v-model="userForm.diaChi" :disabled="isEditMode"
                :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}"
                placeholder="Nhập địa chỉ cư trú" />
            </div>

            <div class="form-group">
              <label>Phân Quyền *</label>
              <select v-model="selectedRoleId" @change="updateRoleInForm($event)">
                <option value="3">ROLE_CUSTOMER (Khách hàng)</option>
                <option value="2">ROLE_SALE (Bán hàng)</option>
                <option value="4">ROLE_CHUYEN_VIEN_TU_VAN (Chuyên viên tư vấn)</option>
                <option value="5">ROLE_INVENTORY (Quản lý kho)</option>
                <option value="1">ROLE_ADMIN (Quản trị viên)</option>
              </select>
            </div>

            <div class="form-group" v-if="isEditMode">
              <label>Trạng Thái Tài Khoản</label>
              <select v-model="userForm.trangThai">
                <option value="HOAT_DONG">Đang Hoạt Động</option>
                <option value="KHOA">Bị Khóa</option>
              </select>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="closeForm">Hủy bỏ</button>
              <button type="submit" class="btn-submit">Lưu thông tin</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

// IMPORT COMPONENT CON
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

const host = window.location.hostname;
const API_BASE = `http://${host}:8080`;

const isCollapsed = ref(false);
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const users = ref([]);
const showForm = ref(false);
const isEditMode = ref(false);
const selectedRoleId = ref(3);

const searchQuery = ref('');
const filterRole = ref('');
const filterStatus = ref('');

const currentPage = ref(1);
const itemsPerPage = ref(5);

// Khớp chính xác ID và Tên Vai Trò từ SQL Database của bạn
const ROLE_MAP = {
  1: 'ROLE_ADMIN',
  2: 'ROLE_SALE',
  3: 'ROLE_CUSTOMER',
  4: 'ROLE_CHUYEN_VIEN_TU_VAN',
  5: 'ROLE_INVENTORY'
};

const getRoleBadgeClass = (roleName) => {
  switch (roleName) {
    case 'ROLE_ADMIN':
      return 'banned-status';
    case 'ROLE_SALE':
    case 'ROLE_CHUYEN_VIEN_TU_VAN':
    case 'ROLE_INVENTORY':
      return 'warn-status';
    default:
      return 'active-status';
  }
};

const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const query = searchQuery.value.toLowerCase().trim();
    const matchQuery = !query || 
      (user.hoTen && user.hoTen.toLowerCase().includes(query)) ||
      (user.email && user.email.toLowerCase().includes(query)) ||
      (user.soDienThoai && user.soDienThoai.toLowerCase().includes(query));

    const matchRole = !filterRole.value || 
      (user.vaiTros && user.vaiTros.some(vt => vt.tenVaiTro === filterRole.value));

    let standardizedStatus = 'KHOA';
    if (user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') {
      standardizedStatus = 'HOAT_DONG';
    }
    const matchStatus = !filterStatus.value || standardizedStatus === filterStatus.value;

    return matchQuery && matchRole && matchStatus;
  });
});

const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / itemsPerPage.value);
});

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredUsers.value.slice(start, end);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const userForm = ref({
  maNguoiDung: null,
  hoTen: '',
  email: '',
  matKhau: '', 
  soDienThoai: '',
  diaChi: '',
  trangThai: 'HOAT_DONG',
  vaiTros: []
});

const loadUsers = async () => {
  try {
    const res = await fetch(`${API_BASE}/api/admin/thanh-vien`, {
      method: 'GET',
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      }
    });
    if (res.ok) {
      users.value = await res.json();
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = 1;
      }
    } else {
      console.error('Server trả về mã lỗi:', res.status);
    }
  } catch (error) {
    console.error('Lỗi kết nối API lấy danh sách:', error);
  }
};

const openAddModal = () => {
  isEditMode.value = false;
  selectedRoleId.value = 3;
  userForm.value = {
    maNguoiDung: null,
    hoTen: '',
    email: '',
    matKhau: '', 
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
  userForm.value.matKhau = '';

  if (userForm.value.trangThai === 'ĐANG HOẠT ĐỘNG') {
    userForm.value.trangThai = 'HOAT_DONG';
  } else if (userForm.value.trangThai === 'BỊ KHÓA') {
    userForm.value.trangThai = 'KHOA';
  }

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
    ? `${API_BASE}/api/admin/thanh-vien/${userForm.value.maNguoiDung}`
    : `${API_BASE}/api/admin/thanh-vien`;

  const method = isEditMode.value ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      mode: 'cors', 
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(userForm.value)
    });

    if (res.ok) {
      const updatedUser = await res.json();
      alert(isEditMode.value ? 'Cập nhật thông tin thành công!' : 'Thêm tài khoản thành công!');
      
      if (isEditMode.value) {
        const index = users.value.findIndex(u => u.maNguoiDung === updatedUser.maNguoiDung);
        if (index !== -1) {
          users.value[index] = updatedUser; 
        }
      } else {
        users.value.push(updatedUser); 
      }

      showForm.value = false;
    } else {
      const errText = await res.text();
      alert("Lỗi hệ thống: " + errText);
    }
  } catch (error) {
    alert("Không thể kết nối đến máy chủ Backend.");
  }
};

const deleteUser = async (id) => {
  if (!confirm("Bạn có chắc chắn muốn XÓA tài khoản này khỏi hệ thống?")) return;
  try {
    const res = await fetch(`${API_BASE}/api/admin/thanh-vien/${id}`, {
      method: 'DELETE',
      headers: { 'Accept': 'application/json' }
    });
    
    if (res.ok) {
      alert("Đã xóa tài khoản thành công!");
      await loadUsers();
    } else {
      const errText = await res.text();
      alert(errText || "Không thể xóa tài khoản này!");
    }
  } catch (error) {
    console.error('Lỗi kết nối khi xóa:', error);
    alert("Lỗi kết nối hệ thống!");
  }
};

const updateRoleInForm = (event) => {
  const value = parseInt(event.target.value);
  const name = ROLE_MAP[value] || 'ROLE_CUSTOMER';
  
  userForm.value.vaiTros = [{ maVaiTro: value, tenVaiTro: name }];
  selectedRoleId.value = value;
};

onMounted(() => { loadUsers(); });
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
  display: flex; 
  height: 100vh; 
  background-color: var(--bg-page); 
  overflow: hidden; 
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
}

.content-wrapper { 
  flex-grow: 1; 
  display: flex; 
  flex-direction: column; 
  overflow-y: auto; 
}

.content { 
  flex: 1; 
  padding: 30px; 
}

.modal-overlay { 
  position: fixed; 
  inset: 0; 
  background: rgba(0,0,0,0.5); 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  z-index: 1000; 
  backdrop-filter: blur(2px); 
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.header-left h1 {
  font-size: 26px;
  font-weight: bold;
  color: var(--wood-dark);
  margin: 0 0 5px 0;
}

.header-left .gold {
  color: var(--gold-matte);
}

.header-left p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}
</style>
