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
        <div>
          <h1>Quản Lý <span class="gold">Bảo Hành</span></h1>
          <p>Danh sách yêu cầu bảo hành đang chờ xác nhận từ khách hàng.</p>
        </div>
      </header>

      <div v-if="message" :class="['notice', message.type]">
        {{ message.text }}
      </div>

      <section class="table-container">
        <table class="admin-table">
          <thead>
            <tr>
              <th>Mã yêu cầu</th>
              <th>Mã đơn hàng</th>
              <th>Loại sản phẩm</th>
              <th>Mô tả lỗi</th>
              <th>Ngày gửi</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in warrantyRequests" :key="item.maBaoHanh">
              <td>#{{ item.maBaoHanh }}</td>
              <td>{{ item.maDonHangCode || 'Chưa có' }}</td>
              <td>{{ item.loaiSanPham || 'Chưa rõ' }}</td>
              <td>{{ item.moTaLoi || 'Không có mô tả' }}</td>
              <td>{{ formatDate(item.ngayGui) }}</td>
              <td>

<span
class="status-badge"
:class="{

pending:item.trangThai==='CHO_XU_LY',

received:item.trangThai==='DA_TIEP_NHAN',

processing:item.trangThai==='DANG_XU_LY',

completed:item.trangThai==='HOAN_TAT',

rejected:item.trangThai==='TU_CHOI'

}">

{{ item.trangThai }}

</span>

</td>
              <td>
                <select
v-model="item.trangThai"
class="status-select"
:disabled="item.trangThai==='HOAN_TAT'">

<option value="CHO_XU_LY">Chờ xử lý</option>

<option value="DA_TIEP_NHAN">Đã tiếp nhận</option>

<option value="DANG_XU_LY">Đang xử lý</option>

<option value="HOAN_TAT">Hoàn tất</option>

<option value="TU_CHOI">Từ chối</option>

</select>
                <button
    class="btn-confirm"
    @click="updateStatus(item)"
    :disabled="item.trangThai==='HOAN_TAT' || item.trangThai==='TU_CHOI'">
    Cập nhật
</button>
                </td>
            </tr>
            <tr v-if="warrantyRequests.length === 0">
              <td colspan="7" class="empty-state">Không có yêu cầu bảo hành nào đang chờ xác nhận.</td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const API = "http://localhost:8080/api/bao-hanh"

const warrantyRequests = ref([])
const loading = ref(false)

const message = ref(null)

// =============================
// MENU
// =============================
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
  { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie' },
  { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench' }
]

// =============================
// Logout
// =============================
const handleLogout = () => {

  localStorage.removeItem("user")

  router.push("/dang-nhap")

}

// =============================
// Format ngày
// =============================
const formatDate = (date) => {

  if (!date) return "---"

  return new Date(date).toLocaleString("vi-VN")

}

// =============================
// Lấy danh sách
// =============================
const fetchWarrantyRequests = async () => {

  loading.value = true

  try {

    const response = await fetch(API)

    if (!response.ok)
      throw new Error()

    warrantyRequests.value = await response.json()

  } catch (e) {

    console.log(e)

    message.value = {
      type: "error",
      text: "Không tải được danh sách."
    }

  }

  loading.value = false

}

// =============================
// Cập nhật trạng thái
// =============================
const updateStatus = async (item) => {

  try {

    const response = await fetch(

      `${API}/${item.maBaoHanh}/status`,

      {
        method: "PUT",

        headers: {
          "Content-Type": "application/json"
        },

        body: JSON.stringify({

          trangThai: item.trangThai

        })

      }

    )

    if (!response.ok)
      throw new Error()

    message.value = {

      type: "success",

      text: "Cập nhật thành công."

    }

    await fetchWarrantyRequests()

  }

  catch (e) {

    console.log(e)

    message.value = {

      type: "error",

      text: "Không cập nhật được."

    }

  }

}

// =============================
// Load
// =============================
onMounted(() => {

  fetchWarrantyRequests()

})
</script>

<style scoped>
@import '../CSS/Admin/QuanLyBaoHanh.css';
</style>
