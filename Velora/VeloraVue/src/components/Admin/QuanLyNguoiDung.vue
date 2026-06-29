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
                <!-- SỬA ĐỔI: Nhận diện linh hoạt cả chữ HOA tiếng Anh lẫn tiếng Việt từ DB -->
                <span class="status-badge" :class="(user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'active-status' : 'banned-status'">
                  {{ (user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'Đang Hoạt Động' : 'Bị Khóa' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-action edit" @click="openEditModal(user)" title="Chỉnh sửa thông tin">
                  <i class="fa-solid fa-pen"></i>
                </button>

                <!-- SỬA ĐỔI: Nút chuyển trạng thái thông minh tự động đổi màu sắc, icon dựa trên state thực tế -->
                <button 
                  class="btn-action" 
                  :style="{
                    backgroundColor: (user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? '#ffebee' : '#e8f5e9',
                    color: (user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? '#c62828' : '#2e7d32'
                  }"
                  @click="toggleUserStatus(user)" 
                  :title="(user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'Khóa tài khoản' : 'Mở khóa tài khoản'"
                >
                  <i :class="(user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG') ? 'fa-solid fa-user-lock' : 'fa-solid fa-user-check'"></i>
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

        <!-- Thanh phân trang (Pagination UI) -->
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

    <!-- Modal Popup Thêm / Sửa thành viên -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? 'Chỉnh Sửa Thành Viên #' + userForm.maNguoiDung : 'Tạo Tài Khoản Mới' }}</h3>
          <button class="close-btn" @click="closeForm">&times;</button>
        </div>
        <form @submit.prevent="saveUser">
          <div class="form-group">
            <label>Họ và Tên *</label>
            <input 
              type="text" 
              v-model="userForm.hoTen" 
              :disabled="isEditMode" 
              required 
              :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}" 
              placeholder="Ví dụ: Nguyễn Văn A" 
            />
          </div>

          <div class="form-group">
            <label>Email Đăng Nhập *</label>
            <input 
              type="email" 
              v-model="userForm.email" 
              :disabled="isEditMode" 
              required
              :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}" 
              placeholder="name@example.com" 
            />
          </div>

          <div class="form-group" v-if="!isEditMode">
            <label>Mật Khẩu Khởi Tạo *</label>
            <input 
              type="password" 
              v-model="userForm.matKhauMaHoa" 
              required 
              placeholder="Nhập mật khẩu tối thiểu 6 ký tự" 
            />
          </div>

          <div class="form-group">
            <label>Số Điện Thoại</label>
            <input 
              type="text" 
              v-model="userForm.soDienThoai" 
              :disabled="isEditMode"
              :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}" 
              placeholder="Nhập số điện thoại" 
            />
          </div>

          <div class="form-group">
            <label>Địa Chỉ</label>
            <input 
              type="text" 
              v-model="userForm.diaChi" 
              :disabled="isEditMode"
              :style="isEditMode ? { backgroundColor: '#f5f5f5', color: '#888', cursor: 'not-allowed' } : {}" 
              placeholder="Nhập địa chỉ cư trú" 
            />
          </div>

          <div class="form-group">
            <label>Phân Quyền *</label>
            <select v-model="selectedRoleId" @change="updateRoleInForm($event)">
              <option value="3">ROLE_CUSTOMER (Khách hàng)</option>
              <option value="2">ROLE_STAFF (Nhân viên)</option>
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
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

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
];

const users = ref([]);
const showForm = ref(false);
const isEditMode = ref(false);
const selectedRoleId = ref(3);

const currentPage = ref(1);
const itemsPerPage = ref(5); 

const totalPages = computed(() => {
  return Math.ceil(users.value.length / itemsPerPage.value);
});

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return users.value.slice(start, end);
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
  matKhauMaHoa: '',
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
      if (currentPage.value > totalPages.value) {
        currentPage.value = 1;
      }
    } else {
      console.error('Server trả về mã lỗi:', res.status);
    }
  } catch (error) {
    console.error('Lỗi kết nối API lấy danh sách:', error);
  }
};

// SỬA ĐỔI HOÀN CHỈNH: Hàm khóa/mở khóa nhanh bằng phương thức PUT chuẩn hóa cấu trúc Object lồng nhau
const toggleUserStatus = async (user) => {
  // 1. Chặn nhanh nếu đối tượng có quyền ROLE_ADMIN
  if (user.vaiTros && user.vaiTros.some(vt => vt.tenVaiTro === 'ROLE_ADMIN')) {
    alert("Không thể khóa tài khoản có quyền Quản trị viên (ROLE_ADMIN)!");
    return;
  }

  // 2. Xác định câu thông báo dựa trên trạng thái hiện tại
  const isActive = user.trangThai === 'HOAT_DONG' || user.trangThai === 'ĐANG HOẠT ĐỘNG';
  const actionText = isActive ? 'KHÓA' : 'MỞ KHÓA';

  if (!confirm(`Bạn có chắc chắn muốn ${actionText} tài khoản của ${user.hoTen}?`)) return;
  
  try {
    // 3. ĐÃ SỬA URL: Bỏ chữ "/thanh-vien", gọi trực tiếp tới endpoint PATCH chính xác của Backend
    const res = await fetch(`http://localhost:8080/api/admin/${user.maNguoiDung}/doi-trang-thai`, {
      method: 'PATCH',
      headers: { 'Accept': 'application/json' }
    });
    
    if (res.ok) {
      alert(`${actionText} tài khoản thành công!`);
      
      // 4. Đảo ngược trạng thái ở local để Vue render lại giao diện lập tức không cần F5
      user.trangThai = isActive ? 'KHOA' : 'HOAT_DONG'; 
      
      // 5. Tải lại toàn bộ danh sách để đồng bộ với DB
      await loadUsers(); 
    } else {
      const errorMessage = await res.text();
      alert("Thất bại: " + errorMessage);
    }
  } catch (error) {
    console.error('Lỗi kết nối PATCH:', error);
    alert("Không thể kết nối đến máy chủ.");
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
    matKhauMaHoa: '',
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
  userForm.value.matKhauMaHoa = ''; 

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
      alert("Không thể xóa tài khoản này!");
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
/* Toàn bộ đoạn mã CSS giữ nguyên như thiết kế tinh tế của bạn */
.admin-wrapper { display: flex; min-height: 100vh; background: #f4f1ea; font-family: sans-serif; }
.sidebar { width: 260px; background: #3e332e; color: #fff; padding: 40px 20px; display: flex; flex-direction: column; flex-shrink: 0; }
.brand { font-size: 18px; color: #d1aa68; margin-bottom: 50px; text-align: center; letter-spacing: 2px; }
.menu li { margin-bottom: 20px; list-style: none; }
.menu a { color: #ccc; text-decoration: none !important; display: flex; align-items: center; gap: 10px; transition: 0.3s; padding: 10px; border-radius: 6px; }
.menu a:hover, .menu a.active { color: #d1aa68; background-color: rgba(209, 170, 104, 0.1); }
.sidebar-bottom { margin-top: auto; border-top: 1px solid #5a4b44; padding-top: 20px; }
.exit, .logout { color: #aaa; background: none; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 10px; margin-bottom: 15px; text-decoration: none !important; }
.content { flex: 1; padding: 60px; min-width: 0; }
.gold { color: #d1aa68; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
.header h1 { color: #3e332e; font-size: 32px; margin-bottom: 5px; }
.header p { color: #888; font-size: 14px; }
.btn-add { background-color: #d1aa68; color: #111; border: none; padding: 12px 24px; font-weight: bold; border-radius: 6px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 8px; }
.btn-add:hover { background-color: #b8955b; transform: translateY(-2px); }
.table-container { background: #ffffff; border: 1px solid #e0dcd5; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.02); }
.admin-table { width: 100%; border-collapse: collapse; text-align: left; }
.admin-table th { background-color: #fcfbf9; color: #3e332e; padding: 18px 20px; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 2px solid #e0dcd5; }
.admin-table td { padding: 15px 20px; border-bottom: 1px solid #f0efeb; vertical-align: middle; color: #555; font-size: 14px; }
.admin-table tbody tr:hover { background-color: #fdfaf5; }
.user-info-flex { display: flex; align-items: center; gap: 12px; font-weight: bold; color: #3e332e; }
.avatar-placeholder { width: 36px; height: 36px; background-color: #f4f1ea; color: #d1aa68; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.email-text { color: #666; }
.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 11px; font-weight: bold; text-transform: uppercase; }
.active-status { background-color: #e8f5e9; color: #2e7d32; }
.banned-status { background-color: #ffebee; color: #c62828; }
.warn-status { background-color: #fff3e0; color: #e65100; }
.actions { display: flex; gap: 10px; }
.btn-action { width: 32px; height: 32px; border: none; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.btn-action.edit { background-color: #f4f1ea; color: #3e332e; }
.btn-action.edit:hover { background-color: #d1aa68; color: #fff; }
.btn-action.delete { background-color: #ffebee; color: #c62828; }
.btn-action.delete:hover { background-color: #c62828; color: #fff; }
.empty-state { text-align: center; padding: 40px !important; color: #888; }
.pagination-wrapper { display: flex; justify-content: flex-end; align-items: center; gap: 15px; padding: 15px 20px; background: #fcfbf9; border-top: 1px solid #e0dcd5; }
.page-btn { background: #ffffff; border: 1px solid #ccbfb5; color: #3e332e; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: bold; display: flex; align-items: center; gap: 6px; transition: all 0.2s; }
.page-btn:hover:not(:disabled) { border-color: #d1aa68; color: #d1aa68; background: #fdfaf5; }
.page-btn:disabled { background: #f5f5f5; color: #bbb; border-color: #e0dcd5; cursor: not-allowed; }
.page-info { font-size: 13px; color: #555; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal-box { background: #fff; padding: 30px; border-radius: 8px; width: 500px; max-width: 90%; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15); max-height: 90vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e0dcd5; padding-bottom: 15px; margin-bottom: 20px; }
.modal-header h3 { color: #3e332e; margin: 0; font-size: 20px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #888; }
.form-group { margin-bottom: 15px; text-align: left; }
label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: bold; color: #555; }
input, select { width: 100%; padding: 10px; border: 1px solid #ccbfb5; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
input:focus, select:focus { border-color: #d1aa68; outline: none; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; }
.btn-cancel { background: #f4f1ea; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; color: #555; }
.btn-submit { background: #3e332e; color: #fff; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
.btn-submit:hover { background: #52433d; }
</style>