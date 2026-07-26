<template>
  <div class="admin-wrapper">
    <!-- THANH MENU SIDEBAR BÊN TRÁI -->
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

    <!-- NỘI DUNG CHÍNH QUẢN LÝ HOÀN TIỀN -->
    <main class="content">
      <header class="header-section">
        <div class="header-titles">
          <h1>QUẢN LÝ <span class="gold">HOÀN TIỀN & TRẢ HÀNG</span></h1>
          <p>Kiểm duyệt các yêu cầu bồi hoàn sản phẩm và quản lý danh sách đen khách hàng</p>
        </div>
      </header>

      <!-- THANH TAB ĐIỀU HƯỚNG TRẠNG THÁI -->
      <div class="refund-tabs-wrapper">
        <div class="refund-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.value"
            @click="currentTab = tab.value"
            :class="['tab-btn', { 'active': currentTab === tab.value }]"
          >
            {{ tab.label }}
            <span class="tab-count" v-if="tab.value !== 'THONG_KE_KH' && getCountByTab(tab.value) > 0">
              ({{ getCountByTab(tab.value) }})
            </span>
          </button>
        </div>
      </div>

      <!-- BẢNG DANH SÁCH HOÀN TIỀN (TAB 1, 2, 3) -->
      <div class="table-card" v-if="currentTab !== 'THONG_KE_KH'">
        <table class="data-table">
          <thead>
            <tr>
              <th>MÃ ĐƠN</th>
              <th>KHÁCH HÀNG</th>
              <th>NGÀY GỬI</th>
              <th>TỔNG TIỀN</th>
              <th>THANH TOÁN</th>
              <th>MINH CHỨNG</th>
              <th>TRẠNG THÁI</th>
              <th class="text-center">HÀNH ĐỘNG</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredRequests" :key="item.id">
              <td class="code-col">#{{ item.maDonHangCode }}</td>
              <td>
                <div class="user-info">
                  <strong>{{ item.hoTen }}</strong>
                  <span><i class="fas fa-phone"></i> {{ item.soDienThoai }}</span>
                  <span><i class="fas fa-envelope"></i> {{ item.email }}</span>
                </div>
              </td>
              <td class="date-col">{{ formatDate(item.ngayTao) }}</td>
              <td class="price-col">{{ formatPrice(item.tongTien) }}</td>
              <td><span class="badge-pay">{{ item.phuongThucThanhToan || 'CHUYEN_KHOAN' }}</span></td>
              <td>
                <button class="btn-view-proof" @click="viewImages(item)">
                  <i class="fas fa-images"></i> Xem {{ item.danhSachAnh?.length || 0 }} ảnh
                </button>
              </td>
              <td>
                <span :class="['badge-status', getStatusClass(item.trangThai)]">
                  {{ getStatusText(item.trangThai) }}
                </span>
              </td>
              <td class="text-center">
                <div class="action-buttons" v-if="item.trangThai === 'CHO_DUYET'">
                  <button class="btn-approve" @click="openActionModal(item, 'XAC_NHAN')">
                    <i class="fas fa-check"></i> Xác nhận
                  </button>
                  <button class="btn-reject" @click="openActionModal(item, 'KHONG_DUYET')">
                    <i class="fas fa-times"></i> Không duyệt
                  </button>
                </div>
                <div v-else class="note-view">
                  <span class="note-text" v-if="item.ghiChuAdmin"><i class="fas fa-comment-dots"></i> {{ item.ghiChuAdmin }}</span>
                  <span v-else class="no-note">--</span>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRequests.length === 0">
              <td colspan="8" class="empty-cell">Không có yêu cầu hoàn tiền nào ở mục này.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- TAB 4: THỐNG KÊ SỐ LẦN KHÁCH HÀNG HOÀN TIỀN -->
      <div class="table-card" v-else>
        <table class="data-table">
          <thead>
            <tr>
              <th>HỌ VÀ TÊN KHÁCH HÀNG</th>
              <th>GMAIL ĐẶT HÀNG</th>
              <th>SỐ ĐIỆN THOẠI</th>
              <th class="text-center">SỐ LẦN HOÀN (1 NĂM)</th>
              <th class="text-center">TRẠNG THÁI TÀI KHOẢN</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="kh in customerStats" :key="kh.email">
              <td><strong>{{ kh.hoTen }}</strong></td>
              <td>{{ kh.email }}</td>
              <td>{{ kh.soDienThoai }}</td>
              <td class="text-center"><span class="badge-count">{{ kh.soLanHoan }} Lần</span></td>
              <td class="text-center">
                <span v-if="kh.soLanHoan >= 6" class="badge-blacked">
                  <i class="fas fa-user-slash"></i> ĐÃ KHÓA & BLACKLIST (>6 LẦN)
                </span>
                <span v-else class="badge-safe"><i class="fas fa-shield-alt"></i> An Toàn</span>
              </td>
            </tr>
            <tr v-if="customerStats.length === 0">
              <td colspan="5" class="empty-cell">Chưa có dữ liệu thống kê số lần hoàn tiền của khách hàng.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <!-- MODAL DUYỆT / TỪ CHỐI BỒI HOÀN -->
    <div class="modal-overlay" v-if="showModal">
      <div class="modal-box">
        <h3 :class="currentAction === 'XAC_NHAN' ? 'title-green' : 'title-red'">
          <i :class="currentAction === 'XAC_NHAN' ? 'fas fa-check-circle' : 'fas fa-times-circle'"></i>
          {{ currentAction === 'XAC_NHAN' ? 'XÁC NHẬN HOÀN TIỀN' : 'TỪ CHỐI BỒI HOÀN' }}
        </h3>
        
        <p class="modal-sub">Mã Đơn: <strong>#{{ selectedItem?.maDonHangCode }}</strong> | Khách hàng: <strong>{{ selectedItem?.hoTen }}</strong></p>

        <div class="bank-details">
          <p><strong><i class="fas fa-university"></i> Ngân hàng nhận:</strong> {{ selectedItem?.tenNganHang }}</p>
          <p><strong><i class="fas fa-credit-card"></i> Số tài khoản:</strong> <span class="gold-text">{{ selectedItem?.soTaiKhoan }}</span> ({{ selectedItem?.tenChuTaiKhoan }})</p>
          <p><strong><i class="fas fa-info-circle"></i> Lý do hoàn trả:</strong> {{ selectedItem?.lyDo }}</p>
        </div>

        <div class="note-field">
          <label>Ghi chú của Quản trị viên <span v-if="currentAction === 'KHONG_DUYET'" class="red-star">* (Bắt buộc)</span>:</label>
          <textarea 
            v-model="adminNote" 
            rows="3" 
            placeholder="Nhập lý do hoặc phản hồi cho khách hàng..."
          ></textarea>
        </div>

        <div class="modal-actions">
          <button @click="showModal = false" class="btn-cancel">HỦY BỎ</button>
          <button 
            @click="submitAdminAction" 
            :class="currentAction === 'XAC_NHAN' ? 'btn-submit-approve' : 'btn-submit-reject'"
          >
            LƯU QUYẾT ĐỊNH
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL XEM ẢNH MINH CHỨNG -->
    <div class="modal-overlay" v-if="showImageModal" @click.self="showImageModal = false">
      <div class="modal-box img-modal">
        <h3><i class="fas fa-images"></i> MINH CHỨNG SẢN PHẨM HOÀN TRẢ (#{{ selectedImageItem?.maDonHangCode }})</h3>
        <div class="image-gallery">
          <img v-for="(img, idx) in selectedImageItem?.danhSachAnh" :key="idx" :src="img" alt="Minh chứng" class="gallery-img" />
        </div>
        <button @click="showImageModal = false" class="btn-close-modal">ĐÓNG</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const requests = ref([])
const customerStats = ref([])
const currentTab = ref('CHO_DUYET')

const showModal = ref(false)
const selectedItem = ref(null)
const currentAction = ref('XAC_NHAN')
const adminNote = ref('')

const showImageModal = ref(false)
const selectedImageItem = ref(null)

// 1. Phân quyền và Danh sách Menu Sidebar
const userRole = computed(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return ''
  try { return JSON.parse(userStr).vaiTro || '' } catch (e) { return '' }
})

const allMenuItems = [
  { name: 'Trang Quản Trị', link: '/admin/dashboard', icon: 'fa-solid fa-gauge', roles: ['ROLE_ADMIN'] },
  { name: 'Tư Vấn Khách Hàng', link: '/admin/tu-van-khach-hang', icon: 'fa-solid fa-comments', roles: ['ROLE_CHUYEN_VIEN_TU_VAN'] },
  { name: 'Quản Lý Sản Phẩm', link: '/admin/products', icon: 'fa-solid fa-box-open', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Loại Sản Phẩm', link: '/admin/categories', icon: 'fa-solid fa-layer-group', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Người Dùng', link: '/admin/users', icon: 'fa-solid fa-users', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Đơn Đặt', link: '/admin/orders', icon: 'fa-solid fa-file-invoice', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Hoàn Tiền', link: '/admin/quan-ly-hoan-tien', icon: 'fa-solid fa-rotate-left', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Kho', link: '/admin/inventory', icon: 'fa-solid fa-boxes-stacked', roles: ['ROLE_ADMIN'] },
  { name: 'Xuất Hóa Đơn', link: '/admin/invoices', icon: 'fa-solid fa-file-invoice-dollar', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Thương Hiệu', link: '/admin/manufacturers', icon: 'fa-solid fa-gem', roles: ['ROLE_ADMIN'] },
  { name: 'Phiếu Nhập Kho', link: '/admin/receipts', icon: 'fa-solid fa-clipboard-list', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Mã Giảm Giá', link: '/admin/ma-giam-gia', icon: 'fa-solid fa-tags', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Lịch Hẹn', link: '/admin/lich-hen', icon: 'fa-solid fa-calendar-check', roles: ['ROLE_ADMIN', 'ROLE_CHUYEN_VIEN_TU_VAN'] }, 
  { name: 'Thống Kê Doanh Thu', link: '/admin/statistics', icon: 'fa-solid fa-chart-pie', roles: ['ROLE_ADMIN'] },
  { name: 'Quản Lý Bảo Hành', link: '/admin/quan-ly-bao-hanh', icon: 'fa-solid fa-wrench', roles: ['ROLE_ADMIN'] }
]

const filteredMenuItems = computed(() => {
  if (!userRole.value) return []
  return allMenuItems.filter(item => item.roles.includes(userRole.value))
})

const handleLogout = () => {
  localStorage.removeItem('user')
  window.location.href = '/dang-nhap'
}

// 2. Logic Tabs & Filtering
const tabs = [
  { label: 'Sản phẩm chờ duyệt bồi hoàn', value: 'CHO_DUYET' },
  { label: 'Đơn đã hoàn', value: 'DA_HOAN_TIEN' },
  { label: 'Đơn không duyệt hoàn', value: 'TU_CHOI_HOAN' },
  { label: 'Danh sách số lần khách hàng đã hoàn', value: 'THONG_KE_KH' }
]

const filteredRequests = computed(() => {
  return requests.value.filter(item => item.trangThai === currentTab.value)
})

const getCountByTab = (tabValue) => {
  return requests.value.filter(item => item.trangThai === tabValue).length
}

const formatPrice = (val) => {
  if (!val) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

const getStatusText = (st) => {
  const map = {
    'CHO_DUYET': 'Chờ duyệt',
    'DA_HOAN_TIEN': 'Đã hoàn tiền',
    'TU_CHOI_HOAN': 'Không duyệt'
  }
  return map[st] || st
}

const getStatusClass = (st) => {
  const map = {
    'CHO_DUYET': 'st-pending',
    'DA_HOAN_TIEN': 'st-approved',
    'TU_CHOI_HOAN': 'st-rejected'
  }
  return map[st] || ''
}

const fetchRequests = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/hoan-tien/admin/danh-sach')
    if (res.ok) requests.value = await res.json()
  } catch (e) {
    console.error('Lỗi tải danh sách hoàn tiền:', e)
  }
}

const fetchCustomerStats = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/hoan-tien/admin/thong-ke-khach-hang')
    if (res.ok) customerStats.value = await res.json()
  } catch (e) {
    console.error('Lỗi tải thống kê khách hàng:', e)
  }
}

const viewImages = (item) => {
  selectedImageItem.value = item
  showImageModal.value = true
}

const openActionModal = (item, action) => {
  selectedItem.value = item
  currentAction.value = action
  adminNote.value = ''
  showModal.value = true
}

const submitAdminAction = async () => {
  if (currentAction.value === 'KHONG_DUYET' && !adminNote.value.trim()) {
    alert('BẮT BUỘC phải nhập ghi chú lý do khi KHÔNG DUYỆT hoàn tiền!')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/api/hoan-tien/admin/xu-ly', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        yeuCauId: selectedItem.value.id,
        hanhDong: currentAction.value,
        ghiChuNote: adminNote.value
      })
    })

    if (res.ok) {
      alert('Đã lưu quyết định bồi hoàn thành công!')
      showModal.value = false
      fetchRequests()
      fetchCustomerStats()
    } else {
      alert(await res.text())
    }
  } catch (e) {
    alert('Lỗi kết nối Backend!')
  }
}

onMounted(() => {
  fetchRequests()
  fetchCustomerStats()
})
</script>

<style scoped>
@import "../CSS/Admin/AdminDashboard.css";

.admin-wrapper { display: flex; min-height: 100vh; background: #faf8f5; font-family: sans-serif; }
.content { flex: 1; padding: 30px; }

.header-section { margin-bottom: 25px; }
.header-section h1 { font-size: 24px; color: #3e332e; letter-spacing: 1px; }
.gold { color: #d1aa68; }
.header-section p { font-size: 13px; color: #777; margin-top: 5px; }

.refund-tabs-wrapper { background: #fff; padding: 6px 15px; border-radius: 8px; border: 1px solid #e0dcd5; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.refund-tabs { display: flex; gap: 15px; overflow-x: auto; }
.tab-btn { background: none; border: none; padding: 14px 18px; font-size: 14px; font-weight: bold; color: #555; cursor: pointer; transition: 0.3s; position: relative; }
.tab-btn.active { color: #d1aa68; }
.tab-btn.active::after { content: ''; position: absolute; bottom: 0; left: 0; width: 100%; height: 3px; background: #d1aa68; }
.tab-count { background: #f3f4f6; padding: 2px 8px; border-radius: 10px; font-size: 12px; margin-left: 5px; color: #555; }

.table-card { background: #fff; border-radius: 8px; border: 1px solid #e0dcd5; padding: 20px; overflow-x: auto; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; }
.data-table th { background: #f5f2eb; padding: 12px; color: #3e332e; font-size: 12px; letter-spacing: 0.5px; border-bottom: 2px solid #e0dcd5; text-transform: uppercase; }
.data-table td { padding: 14px 12px; border-bottom: 1px solid #eee; vertical-align: middle; }

.code-col { font-weight: bold; color: #d1aa68; }
.price-col { font-weight: bold; color: #3e332e; font-size: 14px; }
.date-col { color: #666; font-size: 12px; }

.user-info strong { display: block; color: #3e332e; font-size: 14px; margin-bottom: 2px; }
.user-info span { display: block; font-size: 12px; color: #777; }

.badge-pay { background: #e0f2fe; color: #0369a1; padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
.badge-status { padding: 5px 10px; border-radius: 12px; font-size: 11px; font-weight: bold; display: inline-block; }
.st-pending { background: #fef3c7; color: #b45309; }
.st-approved { background: #dcfce7; color: #15803d; }
.st-rejected { background: #fee2e2; color: #b91c1c; }

.btn-view-proof { background: #f3f4f6; border: 1px solid #ddd; padding: 6px 12px; border-radius: 4px; font-size: 12px; cursor: pointer; color: #444; transition: 0.2s; }
.btn-view-proof:hover { background: #d1aa68; color: white; border-color: #d1aa68; }

.action-buttons { display: flex; gap: 8px; justify-content: center; }
.btn-approve { background: #16a34a; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold; transition: 0.2s; }
.btn-approve:hover { background: #15803d; }
.btn-reject { background: #dc2626; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold; transition: 0.2s; }
.btn-reject:hover { background: #b91c1c; }

.note-text { font-size: 12px; color: #666; font-style: italic; }
.no-note { color: #ccc; }

.badge-count { background: #f3f4f6; padding: 6px 12px; border-radius: 12px; font-weight: bold; color: #3e332e; }
.badge-blacked { background: #fee2e2; color: #b91c1c; padding: 6px 12px; border-radius: 12px; font-weight: bold; font-size: 11px; }
.badge-safe { color: #16a34a; font-weight: bold; font-size: 12px; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.65); display: flex; justify-content: center; align-items: center; z-index: 100; backdrop-filter: blur(2px); }
.modal-box { background: white; padding: 30px; border-radius: 8px; max-width: 520px; width: 100%; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.img-modal { max-width: 700px; }
.title-green { color: #16a34a; font-size: 18px; margin-bottom: 5px; }
.title-red { color: #dc2626; font-size: 18px; margin-bottom: 5px; }
.modal-sub { font-size: 13px; margin-bottom: 15px; color: #666; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.bank-details { background: #fdfbf7; padding: 15px; border: 1px solid #f0e6d2; border-radius: 6px; font-size: 13px; margin-bottom: 15px; line-height: 1.7; color: #444; }
.gold-text { color: #d1aa68; font-weight: bold; }
.note-field label { display: block; font-size: 12px; font-weight: bold; margin-bottom: 6px; color: #3e332e; }
.red-star { color: red; }
.note-field textarea { width: 100%; padding: 12px; border: 1px solid #ccc; border-radius: 4px; outline: none; font-size: 13px; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
.btn-cancel { background: #eee; border: none; padding: 10px 18px; border-radius: 4px; cursor: pointer; font-weight: bold; color: #555; }
.btn-submit-approve { background: #16a34a; color: white; border: none; padding: 10px 20px; font-weight: bold; border-radius: 4px; cursor: pointer; }
.btn-submit-reject { background: #dc2626; color: white; border: none; padding: 10px 20px; font-weight: bold; border-radius: 4px; cursor: pointer; }

.image-gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 20px 0; max-height: 400px; overflow-y: auto; }
.gallery-img { width: 100%; height: 180px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd; }
.btn-close-modal { width: 100%; padding: 12px; background: #3e332e; color: white; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; }
.empty-cell { text-align: center; color: #888; padding: 30px 0; }
</style>