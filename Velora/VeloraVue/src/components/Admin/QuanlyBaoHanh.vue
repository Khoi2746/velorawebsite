<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- 1. GỌI COMPONENT SIDEBAR MỚI -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. GỌI COMPONENT HEADER MỚI -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH (Giữ nguyên 100% logic của ku em) -->
      <main class="content">
        <header class="header">
          <div class="header-left">
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
              <tr v-for="item in paginatedRequests" :key="item.maBaoHanh">
                <td>#{{ item.maBaoHanh }}</td>
                <td>{{ item.maDonHangCode || 'Chưa có' }}</td>
                <td>{{ item.loaiSanPham || 'Chưa rõ' }}</td>
                <td>{{ item.moTaLoi || 'Không có mô tả' }}</td>
                <td>{{ formatDate(item.ngayGui) }}</td>
                <td>
                  <span
                    class="status-badge"
                    :class="{
                      pending: item.trangThai === 'CHO_XU_LY',
                      received: item.trangThai === 'DA_TIEP_NHAN',
                      processing: item.trangThai === 'DANG_XU_LY',
                      completed: item.trangThai === 'HOAN_TAT',
                      rejected: item.trangThai === 'TU_CHOI'
                    }"
                  >
                    {{ getStatusText(item.trangThai) }}
                  </span>
                </td>
                <td>
                  <!-- Ô chọn thời gian hẹn (chỉ hiện khi chọn Đã tiếp nhận) -->
                  <input 
                    v-if="item.trangThai === 'DA_TIEP_NHAN'" 
                    type="datetime-local" 
                    v-model="item.thoiGianHenInput" 
                    class="input-datetime" 
                    style="margin-bottom: 5px; display: block; font-size: 11px;"
                  />

                  <select v-model="item.trangThai" class="status-select">
                    <option value="CHO_XU_LY">Chờ xử lý</option>
                    <option value="DA_TIEP_NHAN">Đã tiếp nhận</option>
                    <option value="DANG_XU_LY">Đang xử lý</option>
                    <option value="HOAN_TAT">Hoàn tất</option>
                    <option value="TU_CHOI">Từ chối</option>
                  </select>

                  <button class="btn-confirm" @click="updateStatus(item)">
                    Cập nhật
                  </button>
                </td>
              </tr>
              <tr v-if="warrantyRequests.length === 0">
                <td colspan="7" class="empty-state">Không có yêu cầu bảo hành nào đang chờ xác nhận.</td>
              </tr>
            </tbody>
          </table>
          <div class="pagination-bar" v-if="totalPages > 1">
            <button class="btn-page" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">Trước</button>

            <template v-for="page in visiblePages" :key="page.value + page.type">
              <span v-if="page.type === 'ellipsis'" class="page-ellipsis">...</span>
              <button
                v-else
                class="btn-page-number"
                :class="{ active: currentPage === page.value }"
                @click="changePage(page.value)"
              >
                {{ page.value }}
              </button>
            </template>

            <button class="btn-page" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">Sau</button>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'

// IMPORT COMPONENT CON VÀO ĐÂY
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// ================= LOGIC ĐIỀU KHIỂN LAYOUT CHUNG =================
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// ================= LOGIC DỮ LIỆU CŨ CỦA KU EM (Giữ nguyên) =================
const API = "http://localhost:8080/api/bao-hanh"

const warrantyRequests = ref([])
const loading = ref(false)

const message = ref(null)
const currentPage = ref(1)
const itemsPerPage = ref(5)

const totalPages = computed(() => {
  return Math.max(Math.ceil(warrantyRequests.value.length / itemsPerPage.value), 1)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5

  if (totalPages.value <= maxVisible) {
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push({ type: 'page', value: i })
    }
    return pages
  }

  if (currentPage.value <= 3) {
    for (let i = 1; i <= 4; i++) {
      pages.push({ type: 'page', value: i })
    }
    pages.push({ type: 'ellipsis', value: '...' })
    pages.push({ type: 'page', value: totalPages.value })
    return pages
  }

  if (currentPage.value >= totalPages.value - 2) {
    pages.push({ type: 'page', value: 1 })
    pages.push({ type: 'ellipsis', value: '...' })
    for (let i = totalPages.value - 3; i <= totalPages.value; i++) {
      pages.push({ type: 'page', value: i })
    }
    return pages
  }

  pages.push({ type: 'page', value: 1 })
  pages.push({ type: 'ellipsis', value: '...' })
  pages.push({ type: 'page', value: currentPage.value - 1 })
  pages.push({ type: 'page', value: currentPage.value })
  pages.push({ type: 'page', value: currentPage.value + 1 })
  pages.push({ type: 'ellipsis', value: '...' })
  pages.push({ type: 'page', value: totalPages.value })
  return pages
})

const paginatedRequests = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return warrantyRequests.value.slice(start, end)
})

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

watch([warrantyRequests, totalPages], () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})

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
    if (!response.ok) throw new Error()
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
        const status = item.trangThai.trim();

        const response = await fetch(`${API}/${item.maBaoHanh}/status`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                trangThai: status,
                thoiGianHen: item.thoiGianHenInput || null // Gửi kèm thời gian hẹn nếu có
            })
        });

        if (!response.ok) throw new Error();

        message.value = {
            type: "success",
            text: "Cập nhật trạng thái và gửi email lịch hẹn thành công!"
        };

        await fetchWarrantyRequests();

    } catch (e) {
        console.log(e);
        message.value = {
            type: "error",
            text: "Không cập nhật được."
        };
    }
}

const getStatusText = (status) => {
  switch(status){
    case "CHO_XU_LY": return "Chờ xử lý"
    case "DA_TIEP_NHAN": return "Đã tiếp nhận"
    case "DANG_XU_LY": return "Đang xử lý"
    case "HOAN_TAT": return "Hoàn tất"
    case "TU_CHOI": return "Từ chối"
    default: return status
  }
}

// =============================
// Load
// =============================
onMounted(() => {
  fetchWarrantyRequests()
})
</script>

<!-- CSS DÙNG ĐỂ CHỨA BIẾN GLOBAL (Trị dứt điểm lỗi Sidebar trắng) -->
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
@import '../CSS/Admin/QuanLyBaoHanh.css';

/* ==============================================
   CSS LAYOUT CHUNG BỌC BÊN NGOÀI
   ============================================== */
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

/* Fix lại style cho thẻ header vì thẻ header cũ của em có thể đang dính CSS global */
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