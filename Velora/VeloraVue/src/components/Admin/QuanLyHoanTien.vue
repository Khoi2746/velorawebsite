<template>
  <div class="velora-admin-wrapper admin-wrapper">
    <!-- =========================================================================
         [PHẦN 1: KHUNG ĐIỀU HƯỚNG VÀ TIÊU ĐỀ TRANG]
    ========================================================================== -->
    <!-- 1. GỌI COMPONENT SIDEBAR -->
    <AdminSidebar :isCollapsed="isCollapsed" />

    <div class="content-wrapper" :class="{ 'content-expanded': isCollapsed }">
      <!-- 2. GỌI COMPONENT HEADER -->
      <AdminHeader @toggle-sidebar="toggleSidebar" />

      <!-- 3. NỘI DUNG CHÍNH -->
      <main class="content">
        <!-- 3.1 TIÊU ĐỀ TRANG -->
        <header class="header-section">
          <div class="header-titles">
            <!-- DÒNG MẶC ĐỊNH: Chữ 'HOÀN TIỀN & TRẢ HÀNG' màu vàng hoàng kim (class gold) -->
            <h1>QUẢN LÝ <span class="gold">HOÀN TIỀN & TRẢ HÀNG</span></h1>
            <!-- THAY THẾ: Đổi chữ 'HOÀN TIỀN & TRẢ HÀNG' sang MÀU ĐỎ:
            <h1>QUẢN LÝ <span style="color: #dc2626;">HOÀN TIỀN & TRẢ HÀNG</span></h1> -->
            <!-- THAY THẾ: Đổi chữ 'HOÀN TIỀN & TRẢ HÀNG' sang MÀU XANH DƯƠNG:
            <h1>QUẢN LÝ <span style="color: #0d6efd;">HOÀN TIỀN & TRẢ HÀNG</span></h1> -->

            <p>Kiểm duyệt các yêu cầu bồi hoàn sản phẩm và quản lý danh sách đen khách hàng</p>
          </div>
        </header>

        <!-- =========================================================================
             [PHẦN 2: THANH TAB ĐIỀU HƯỚNG TRẠNG THÁI]
        ========================================================================== -->
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

        <!-- =========================================================================
             [PHẦN 3: BẢNG DANH SÁCH YÊU CẦU HOÀN TIỀN (TAB 1, 2, 3)]
        ========================================================================== -->
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
                
                <!-- Cột 1: Mã đơn hàng -->
                <td class="code-col">#{{ item.maDonHangCode }}</td>
                <!-- THAY THẾ: Mã đơn hàng hiển thị màu đỏ in đậm:
                <td class="code-col" style="color: #dc2626; font-weight: bold;">#{{ item.maDonHangCode }}</td> -->

                <!-- Cột 2: Thông tin khách hàng -->
                <td>
                  <div class="user-info">
                    <strong>{{ item.hoTen }}</strong>
                    <span><i class="fas fa-phone"></i> {{ item.soDienThoai }}</span>
                    <span><i class="fas fa-envelope"></i> {{ item.email }}</span>
                  </div>
                </td>
                
                <!-- Cột 3: Ngày gửi yêu cầu -->
                <td class="date-col">{{ formatDate(item.ngayTao) }}</td>
                
                <!-- Cột 4: Tổng tiền hoàn trả -->
                <td class="price-col">{{ formatPrice(item.tongTien) }}</td>
                <!-- THAY THẾ: Tổng tiền hiển thị màu đỏ to rõ:
                <td class="price-col" style="color: #dc2626; font-size: 16px; font-weight: bold;">{{ formatPrice(item.tongTien) }}</td> -->

                <!-- Cột 5: Phương thức thanh toán gốc -->
                <td>
                  <span class="badge-pay">{{ item.phuongThucThanhToan || 'CHUYEN_KHOAN' }}</span>
                </td>
                
                <!-- Cột 6: Nút xem ảnh minh chứng sản phẩm lỗi -->
                <td>
                  <button class="btn-view-proof" @click="viewImages(item)">
                    <i class="fas fa-images"></i> Xem {{ item.danhSachAnh?.length || 0 }} ảnh
                  </button>
                  <!-- THAY THẾ: Đổi nút Xem ảnh thành hình tròn chỉ chứa icon:
                  <button class="btn-view-proof" style="border-radius: 50%; width: 32px; height: 32px; padding: 0;" title="Xem ảnh" @click="viewImages(item)"><i class="fas fa-images"></i></button> -->
                </td>

                <!-- Cột 7: Badge trạng thái yêu cầu -->
                <td>
                  <span :class="['badge-status', getStatusClass(item.trangThai)]">
                    {{ getStatusText(item.trangThai) }}
                  </span>
                </td>

                <!-- Cột 8: Các nút hành động duyệt / từ chối / xem ghi chú -->
                <td class="text-center">
                  <div class="action-buttons" v-if="item.trangThai === 'CHO_DUYET'">
                    <!-- NÚT XÁC NHẬN DUYỆT (MÀU XANH LÁ) -->
                    <button class="btn-approve" @click="openActionModal(item, 'XAC_NHAN')">
                      <i class="fas fa-check"></i> Xác nhận
                    </button>
                    <!-- THAY THẾ: Nút Xác nhận bo tròn hình viên thuốc:
                    <button class="btn-approve" style="border-radius: 50px;" @click="openActionModal(item, 'XAC_NHAN')"><i class="fas fa-check"></i> Xác nhận</button> -->

                    <!-- NÚT TỪ CHỐI DUYỆT (MÀU ĐỎ) -->
                    <button class="btn-reject" @click="openActionModal(item, 'KHONG_DUYET')">
                      <i class="fas fa-times"></i> Không duyệt
                    </button>
                    <!-- THAY THẾ: Nút Không duyệt bo tròn hình viên thuốc:
                    <button class="btn-reject" style="border-radius: 50px;" @click="openActionModal(item, 'KHONG_DUYET')"><i class="fas fa-times"></i> Không duyệt</button> -->
                  </div>
                  
                  <div v-else class="note-view">
                    <span class="note-text" v-if="item.ghiChuAdmin"><i class="fas fa-comment-dots"></i> {{ item.ghiChuAdmin }}</span>
                    <span v-else class="no-note">--</span>
                  </div>
                </td>
              </tr>

              <!-- Dòng thông báo khi không có dữ liệu -->
              <tr v-if="filteredRequests.length === 0">
                <td colspan="8" class="empty-cell">Không có yêu cầu hoàn tiền nào ở mục này.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- =========================================================================
             [PHẦN 4: BẢNG THỐNG KÊ KHÁCH HÀNG & DANH SÁCH ĐEN (TAB 4)]
        ========================================================================== -->
        <div class="table-card" v-else>
          <table class="data-table">
            <thead>
              <tr>
                <th>HỌ VÀ TÊN KHÁCH HÀNG</th>
                <th>GMAIL ĐẶT HÀNG</th>
                <th>SỐ ĐIỆN THOẠI</th>
                <th class="text-center">SỐ LẦN HOÀN </th>
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
                  <!-- DÒNG MẶC ĐỊNH: Điều kiện khóa tài khoản khi số lần hoàn >= 6 -->
                  <span v-if="kh.soLanHoan >= 6" class="badge-blacked">
                    <i class="fas fa-user-slash"></i> ĐÃ KHÓA & BLACKLIST (>6 LẦN)
                  </span>
                  <!-- THAY THẾ: Khóa tài khoản nghiêm ngặt hơn khi số lần hoàn >= 3:
                  <span v-if="kh.soLanHoan >= 3" class="badge-blacked"><i class="fas fa-user-slash"></i> ĐÃ KHÓA & BLACKLIST (>3 LẦN)</span> -->
                  
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

      <!-- =========================================================================
           [PHẦN 5: CÁC MODAL DIALOG & TOAST POPUP]
      ========================================================================== -->

      <!-- 5.1 MODAL DUYỆT / TỪ CHỐI BỒI HOÀN KÈM THÔNG TIN NGÂN HÀNG -->
      <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
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
            <!-- Nút Hủy bỏ đóng modal -->
            <button @click="showModal = false" class="btn-cancel">HỦY BỎ</button>
            <!-- Nút Lưu quyết định duyệt/từ chối -->
            <button 
              @click="submitAdminAction" 
              :class="currentAction === 'XAC_NHAN' ? 'btn-submit-approve' : 'btn-submit-reject'"
            >
              LƯU QUYẾT ĐỊNH
            </button>
          </div>
        </div>
      </div>

      <!-- 5.2 MODAL XEM DANH SÁCH ẢNH MINH CHỨNG -->
      <div class="modal-overlay" v-if="showImageModal" @click.self="showImageModal = false">
        <div class="modal-box img-modal">
          <h3><i class="fas fa-images"></i> MINH CHỨNG SẢN PHẨM HOÀN TRẢ (#{{ selectedImageItem?.maDonHangCode }})</h3>
          <div class="image-gallery">
            <img v-for="(img, idx) in selectedImageItem?.danhSachAnh" :key="idx" :src="img" alt="Minh chứng" class="gallery-img" />
          </div>
          <!-- Nút đóng modal ảnh -->
          <button @click="showImageModal = false" class="btn-close-modal">ĐÓNG</button>
        </div>
      </div>

      <!-- 5.3 POPUP XÁC NHẬN HÀNH ĐỘNG (CUSTOM CONFIRM DIALOG) -->
      <div class="modal-overlay" v-if="confirmDialog.show" @click.self="handleConfirmCancel">
        <div class="modal-box custom-dialog-box">
          <div class="dialog-icon-wrapper" :class="confirmDialog.type">
            <i class="fa-solid" :class="confirmDialog.type === 'danger' ? 'fa-triangle-exclamation' : 'fa-circle-question'"></i>
          </div>
          <h3 class="dialog-title">{{ confirmDialog.title }}</h3>
          <p class="dialog-message">{{ confirmDialog.message }}</p>
          <div class="dialog-actions">
            <button class="btn-dialog-cancel" @click="handleConfirmCancel">Hủy bỏ</button>
            <button 
              class="btn-dialog-confirm" 
              :class="confirmDialog.type === 'danger' ? 'btn-danger-confirm' : 'btn-gold-confirm'"
              @click="handleConfirmOk"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>

      <!-- 5.4 THÔNG BÁO TỰ TẮT Ở GÓC (CUSTOM ALERT TOAST) -->
      <div class="custom-alert-toast" :class="[alertToast.type, { 'show': alertToast.show }]">
        <i class="fa-solid" :class="alertToast.type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'"></i>
        <span>{{ alertToast.message }}</span>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue';
import AdminHeader from './AdminHeader.vue';

// =========================================================================
// [LOGIC 1: ĐIỀU KHIỂN GIAO DIỆN & THANH MENU]
// =========================================================================
const isCollapsed = ref(false);
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// =========================================================================
// [LOGIC 2: QUẢN LÝ DỮ LIỆU HOÀN TIỀN & THỐNG KÊ]
// =========================================================================
const requests = ref([])
const customerStats = ref([])
const currentTab = ref('CHO_DUYET')

const showModal = ref(false)
const selectedItem = ref(null)
const currentAction = ref('XAC_NHAN')
const adminNote = ref('')

const showImageModal = ref(false)
const selectedImageItem = ref(null)

// Quản lý Dialog Confirm xác nhận hành động
const confirmDialog = ref({
  show: false,
  title: '',
  message: '',
  type: 'gold', // 'gold' hoặc 'danger'
  resolve: null
})

// Quản lý Toast thông báo nhanh ở góc
const alertToast = ref({
  show: false,
  message: '',
  type: 'success' // 'success' hoặc 'error'
})
let toastTimer = null

// =========================================================================
// [LOGIC 3: HÀM HIỂN THỊ THÔNG BÁO VÀ TIỆN ÍCH]
// =========================================================================
const showToast = (message, type = 'success') => {
  alertToast.value = { show: true, message, type }
  if (toastTimer) clearTimeout(toastTimer)
  
  // DÒNG MẶC ĐỊNH: Tự đóng sau 3 giây (3000ms)
  toastTimer = setTimeout(() => { alertToast.value.show = false }, 3000)
  // THAY THẾ: Hiện thông báo lâu hơn trong 5 giây:
  // toastTimer = setTimeout(() => { alertToast.value.show = false }, 5000)
  // THAY THẾ: Tắt thông báo nhanh trong 1 giây:
  // toastTimer = setTimeout(() => { alertToast.value.show = false }, 1000)
}

const openConfirm = (title, message, type = 'gold') => {
  confirmDialog.value = {
    show: true,
    title,
    message,
    type,
    resolve: null
  }
  return new Promise((resolve) => {
    confirmDialog.value.resolve = resolve
  })
}

const handleConfirmOk = () => {
  confirmDialog.value.show = false
  if (confirmDialog.value.resolve) confirmDialog.value.resolve(true)
}

const handleConfirmCancel = () => {
  confirmDialog.value.show = false
  if (confirmDialog.value.resolve) confirmDialog.value.resolve(false)
}

// Cấu hình danh sách các Tab
const tabs = [
  { label: 'Sản phẩm chờ duyệt bồi hoàn', value: 'CHO_DUYET' },
  { label: 'Đơn đã hoàn', value: 'DA_HOAN_TIEN' },
  { label: 'Đơn không duyệt hoàn', value: 'TU_CHOI_HOAN' },
  { label: 'Danh sách số lần khách hàng đã hoàn', value: 'THONG_KE_KH' }
]

// Lọc dữ liệu theo tab đang chọn
const filteredRequests = computed(() => {
  return requests.value.filter(item => item.trangThai === currentTab.value)
})

// Đếm số lượng yêu cầu theo từng tab
const getCountByTab = (tabValue) => {
  return requests.value.filter(item => item.trangThai === tabValue).length
}

// Format tiền tệ VNĐ
const formatPrice = (val) => {
  if (!val) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

// Format ngày tháng
const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

// Chuyển mã trạng thái sang tiếng Việt
const getStatusText = (st) => {
  const map = {
    'CHO_DUYET': 'Chờ duyệt',
    'DA_HOAN_TIEN': 'Đã hoàn tiền',
    'TU_CHOI_HOAN': 'Không duyệt'
  }
  return map[st] || st
}

// Ánh xạ class màu CSS theo trạng thái
const getStatusClass = (st) => {
  const map = {
    'CHO_DUYET': 'st-pending',
    'DA_HOAN_TIEN': 'st-approved',
    'TU_CHOI_HOAN': 'st-rejected'
  }
  return map[st] || ''
}

// =========================================================================
// [LOGIC 4: GỌI API BACKEND SPRING BOOT]
// =========================================================================
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

// Gửi quyết định phê duyệt / từ chối về backend
const submitAdminAction = async () => {
  if (currentAction.value === 'KHONG_DUYET' && !adminNote.value.trim()) {
    showToast('BẮT BUỘC phải nhập ghi chú lý do khi KHÔNG DUYỆT hoàn tiền!', 'error')
    return
  }

  const actionName = currentAction.value === 'XAC_NHAN' ? 'xác nhận hoàn tiền' : 'từ chối bồi hoàn'
  const isDanger = currentAction.value === 'KHONG_DUYET'
  
  const confirmed = await openConfirm(
    'Xác nhận quyết định',
    `Bạn có chắc chắn muốn ${actionName} cho đơn hàng #${selectedItem.value?.maDonHangCode}?`,
    isDanger ? 'danger' : 'gold'
  )
  if (!confirmed) return

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
      showToast('Đã lưu quyết định bồi hoàn thành công!', 'success')
      showModal.value = false
      fetchRequests()
      fetchCustomerStats()
    } else {
      const errText = await res.text()
      showToast(errText || 'Không thể xử lý yêu cầu!', 'error')
    }
  } catch (e) {
    showToast('Lỗi kết nối đến máy chủ Backend!', 'error')
  }
}

onMounted(() => {
  fetchRequests()
  fetchCustomerStats()
})
</script>

<style>
/* =========================================================================
   [BẢNG MÃ MÀU TOÀN CỤC CỦA HỆ THỐNG VELORA]
========================================================================= */
:root {
  --wood-dark: #362921;     /* Màu nâu gỗ đậm (Dùng cho tiêu đề chính) */
  --wood-active: #47372c;   /* Màu nâu gỗ sẫm khi hover */
  --wood-medium: #544438;   /* Màu nâu gỗ vừa */
  --wood-light: #7a6352;    /* Màu nâu gỗ sáng */
  --gold-matte: #cca15e;    /* Màu vàng hoàng kim Velora */
  --bg-page: #f8f6f0;       /* Màu nền toàn bộ website (Trắng kem nhạt) */
  --border-light: #eaeaea;  /* Màu đường viền phân cách */
  --text-main: #333333;     /* Màu chữ đen xám chính */
  --text-muted: #888888;    /* Màu chữ xám mô tả phụ */
}
</style>

<style scoped>
@import "../CSS/Admin/AdminDashboard.css";

/* =========================================================================
   [CSS NHÓM 1: BỐ CỤC KHUNG CHÍNH & TIÊU ĐỀ]
========================================================================= */
.velora-admin-wrapper { 
  display: flex;                                /* Sắp xếp sidebar và nội dung cạnh nhau */
  height: 100vh;                                /* Chiều cao chiếm trọn 100% màn hình */
  background-color: var(--bg-page);             /* Màu nền trang: Trắng kem nhạt */
  overflow: hidden;                             /* Chặn thanh cuộn ngoài cùng */
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
}

.content-wrapper { 
  flex-grow: 1;                                 /* Tự co giãn chiếm toàn bộ phần diện tích còn lại */
  display: flex; 
  flex-direction: column; 
  overflow-y: auto;                             /* Xuất hiện thanh cuộn dọc khi nội dung dài */
}

.content { 
  flex: 1; 
  padding: 30px;                                /* Đệm xung quanh nội dung chính 30px */
}

.header-section { margin-bottom: 25px; }
.header-section h1 { 
  font-size: 24px;                              /* Cỡ chữ tiêu đề chính */
  color: var(--wood-dark);                      /* Màu chữ: Nâu gỗ đậm */
  letter-spacing: 1px;                          /* Khoảng cách giữa các chữ cái 1px */
  font-weight: bold;
}
.gold { color: var(--gold-matte); }             /* Màu vàng hoàng kim của tiêu đề */
.header-section p { 
  font-size: 13px; 
  color: var(--text-muted);                     /* Màu chữ xám mô tả nhỏ */
  margin-top: 5px; 
}

/* =========================================================================
   [CSS NHÓM 2: THANH TAB ĐIỀU HƯỚNG TRẠNG THÁI]
========================================================================= */
.refund-tabs-wrapper { 
  background: #fff;                             /* Nền thanh tab: MÀU TRẮNG TINH */
  padding: 6px 15px; 
  border-radius: 8px;                           /* Bo góc thanh tab 8px */
  border: 1px solid #e0dcd5;                    /* Viền xám kem */
  margin-bottom: 20px; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.02); 
}
.refund-tabs { 
  display: flex; 
  gap: 15px;                                    /* Khoảng cách giữa các nút tab 15px */
  overflow-x: auto;                             /* Cho phép cuộn ngang trên màn hình nhỏ */
}
.tab-btn { 
  background: none;                             /* Nút tab không có nền mặc định */
  border: none;                                 /* Nút tab không có viền */
  padding: 14px 18px; 
  font-size: 14px; 
  font-weight: bold; 
  color: #555;                                  /* Màu chữ tab khi chưa chọn: XÁM */
  cursor: pointer;                              /* Chuột hình bàn tay */
  transition: 0.3s; 
  position: relative; 
}
.tab-btn.active { 
  color: #d1aa68;                               /* Màu chữ tab khi ĐANG CHỌN: VÀNG HOÀNG KIM */
}
.tab-btn.active::after { 
  content: ''; 
  position: absolute; 
  bottom: 0; 
  left: 0; 
  width: 100%; 
  height: 3px;                                  /* Chiều cao thanh gạch chân màu vàng: 3px */
  background: #d1aa68;                          /* Màu thanh gạch chân: VÀNG HOÀNG KIM */
}
.tab-count { 
  background: #f3f4f6;                          /* Nền số đếm đơn: XÁM NHẸ */
  padding: 2px 8px; 
  border-radius: 10px;                          /* Bo tròn hình viên thuốc */
  font-size: 12px; 
  margin-left: 5px; 
  color: #555;                                  /* Màu số đếm: XÁM ĐẬM */
}

/* =========================================================================
   [CSS NHÓM 3: BẢNG DỮ LIỆU DATA TABLE]
========================================================================= */
.table-card { 
  background: #fff;                             /* Màu nền khung bảng: MÀU TRẮNG TINH */
  border-radius: 8px;                           /* Bo góc khung bảng 8px */
  border: 1px solid #e0dcd5;                    /* Viền khung bảng màu xám kem */
  padding: 20px; 
  overflow-x: auto; 
  box-shadow: 0 4px 12px rgba(0,0,0,0.03); 
}
.data-table { 
  width: 100%; 
  border-collapse: collapse; 
  text-align: left; 
  font-size: 13px; 
}
.data-table th { 
  background: #f5f2eb;                          /* Màu nền tiêu đề cột: KEM NHẠT */
  padding: 12px; 
  color: #3e332e;                               /* Màu chữ tiêu đề cột: NÂU ĐẬM */
  font-size: 12px; 
  letter-spacing: 0.5px; 
  border-bottom: 2px solid #e0dcd5;             /* Viền dưới tiêu đề cột */
  text-transform: uppercase; 
}
.data-table td { 
  padding: 14px 12px; 
  border-bottom: 1px solid #eee;                /* Viền ngăn cách giữa các dòng */
  vertical-align: middle; 
}

.code-col { font-weight: bold; color: #d1aa68; }/* Màu mã đơn hàng: VÀNG HOÀNG KIM */
.price-col { font-weight: bold; color: #3e332e; font-size: 14px; } /* Màu tiền: NÂU ĐẬM */
.date-col { color: #666; font-size: 12px; }

.user-info strong { display: block; color: #3e332e; font-size: 14px; margin-bottom: 2px; }
.user-info span { display: block; font-size: 12px; color: #777; }

/* =========================================================================
   [CSS NHÓM 4: CÁC BADGE NHÃN HIỂN THỊ TRẠNG THÁI]
========================================================================= */
.badge-pay { 
  background: #e0f2fe;                          /* Màu nền badge thanh toán: XANH DƯƠNG NHẠT */
  color: #0369a1;                               /* Màu chữ badge thanh toán: XANH DƯƠNG ĐẬM */
  padding: 4px 8px; 
  border-radius: 4px; 
  font-size: 11px; 
  font-weight: bold; 
}
.badge-status { 
  padding: 5px 10px; 
  border-radius: 12px; 
  font-size: 11px; 
  font-weight: bold; 
  display: inline-block; 
}
.st-pending { 
  background: #fef3c7;                          /* Màu nền badge Chờ duyệt: VÀNG NHẠT */
  color: #b45309;                               /* Màu chữ badge Chờ duyệt: CAM VÀNG */
}
.st-approved { 
  background: #dcfce7;                          /* Màu nền badge Đã hoàn tiền: XANH LÁ NHẠT */
  color: #15803d;                               /* Màu chữ badge Đã hoàn tiền: XANH LÁ ĐẬM */
}
.st-rejected { 
  background: #fee2e2;                          /* Màu nền badge Không duyệt: ĐỎ NHẠT */
  color: #b91c1c;                               /* Màu chữ badge Không duyệt: ĐỎ ĐẬM */
}

.badge-count { 
  background: #f3f4f6;                          /* Nền badge đếm số lần hoàn: XÁM NHẸ */
  padding: 6px 12px; 
  border-radius: 12px; 
  font-weight: bold; 
  color: #3e332e; 
}
.badge-blacked { 
  background: #fee2e2;                          /* Nền badge bị khóa: ĐỎ CẢNH BÁO */
  color: #b91c1c;                               /* Chữ badge bị khóa: ĐỎ TƯƠI */
  padding: 6px 12px; 
  border-radius: 12px; 
  font-weight: bold; 
  font-size: 11px; 
}
.badge-safe { 
  color: #16a34a;                               /* Màu chữ tài khoản an toàn: XANH LÁ */
  font-weight: bold; 
  font-size: 12px; 
}

/* =========================================================================
   [CSS NHÓM 5: TẬP HỢP TẤT CẢ CÁC NÚT BẤM (BUTTONS)]
========================================================================= */

/* 5.1 NÚT XEM ẢNH MINH CHỨNG (TRÊN DÒNG BẢNG) */
.btn-view-proof { 
  background: #f3f4f6;                          /* Màu nền nút xem ảnh: XÁM NHẠT */
  border: 1px solid #ddd;                       /* Viền nút xem ảnh: XÁM */
  padding: 6px 12px; 
  border-radius: 4px; 
  font-size: 12px; 
  cursor: pointer; 
  color: #444;                                  /* Màu chữ nút xem ảnh: XÁM ĐEN */
  transition: 0.2s; 
}
.btn-view-proof:hover { 
  background: #d1aa68;                          /* Đổi sang MÀU VÀNG khi rê chuột vào */
  color: white;                                 /* Đổi chữ sang MÀU TRẮNG khi rê chuột vào */
  border-color: #d1aa68; 
}

/* 5.2 NÚT XÁC NHẬN DUYỆT HOÀN TIỀN (TRÊN DÒNG BẢNG) */
.action-buttons { display: flex; gap: 8px; justify-content: center; }
.btn-approve { 
  background: #16a34a;                          /* Màu nền nút Xác nhận: XANH LÁ CÂY */
  color: white;                                 /* Màu chữ nút Xác nhận: TRẮNG TINH */
  border: none; 
  padding: 8px 12px; 
  border-radius: 4px; 
  cursor: pointer; 
  font-size: 12px; 
  font-weight: bold; 
  transition: 0.2s; 
  
  /* THAY THẾ: Nút Xác nhận chuyển sang MÀU XANH DƯƠNG:
  background: #0d6efd; */
}
.btn-approve:hover { 
  background: #15803d;                          /* Nền chuyển sang XANH LÁ ĐẬM khi rê chuột vào */
}

/* 5.3 NÚT KHÔNG DUYỆT TỪ CHỐI (TRÊN DÒNG BẢNG) */
.btn-reject { 
  background: #dc2626;                          /* Màu nền nút Không duyệt: MÀU ĐỎ CẢNH BÁO */
  color: white;                                 /* Màu chữ nút Không duyệt: TRẮNG TINH */
  border: none; 
  padding: 8px 12px; 
  border-radius: 4px; 
  cursor: pointer; 
  font-size: 12px; 
  font-weight: bold; 
  transition: 0.2s; 
  
  /* THAY THẾ: Nút Không duyệt chuyển sang MÀU CAM:
  background: #ea580c; */
}
.btn-reject:hover { 
  background: #b91c1c;                          /* Nền chuyển sang ĐỎ ĐẬM khi rê chuột vào */
}

.note-text { font-size: 12px; color: #666; font-style: italic; }
.no-note { color: #ccc; }

/* =========================================================================
   [CSS NHÓM 6: KHUNG MÀN HÌNH MỜ & HỘP THOẠI MODAL CHI TIẾT]
========================================================================= */

/* Lớp màn đen mờ phủ kín toàn màn hình */
.modal-overlay { 
  position: fixed;                              /* Cố định toàn màn hình */
  inset: 0;                                     /* Phủ kín 4 góc: top, right, bottom, left = 0 */
  background: rgba(0,0,0,0.65);                  /* Màu nền ĐEN trong suốt 65% */
  display: flex; 
  justify-content: center;                      /* Căn giữa theo chiều ngang */
  align-items: center;                          /* Căn giữa theo chiều dọc */
  z-index: 1000;                                /* Nổi lên trên cùng */
  backdrop-filter: blur(2px);                   /* Làm mờ nền phía sau 2px */
}

/* Hộp trắng chứa nội dung modal */
.modal-box { 
  background: white;                            /* Màu nền hộp Modal: MÀU TRẮNG TINH */
  padding: 30px; 
  border-radius: 8px;                           /* Bo góc hộp 8px */
  max-width: 520px;                             /* Chiều rộng tối đa 520px */
  width: 100%; 
  box-shadow: 0 10px 25px rgba(0,0,0,0.2); 
  
  /* THAY THẾ: Bo tròn góc Modal nhiều hơn:
  border-radius: 16px; */
}
.img-modal { max-width: 700px; }                /* Hộp modal xem ảnh rộng 700px */
.title-green { color: #16a34a; font-size: 18px; margin-bottom: 5px; } /* Tiêu đề khi xác nhận: MÀU XANH */
.title-red { color: #dc2626; font-size: 18px; margin-bottom: 5px; }   /* Tiêu đề khi từ chối: MÀU ĐỎ */
.modal-sub { font-size: 13px; margin-bottom: 15px; color: #666; border-bottom: 1px solid #eee; padding-bottom: 10px; }

.bank-details { 
  background: #fdfbf7;                          /* Nền khung thông tin ngân hàng: KEM NHẠT */
  padding: 15px; 
  border: 1px solid #f0e6d2;                    /* Viền khung ngân hàng: VÀNG KEM */
  border-radius: 6px; 
  font-size: 13px; 
  margin-bottom: 15px; 
  line-height: 1.7; 
  color: #444; 
}
.gold-text { color: #d1aa68; font-weight: bold; }/* Chữ số tài khoản: VÀNG HOÀNG KIM */

.note-field label { display: block; font-size: 12px; font-weight: bold; margin-bottom: 6px; color: #3e332e; }
.red-star { color: red; }
.note-field textarea { 
  width: 100%; 
  padding: 12px; 
  border: 1px solid #ccc; 
  border-radius: 4px; 
  outline: none; 
  font-size: 13px; 
  font-family: inherit; 
}

/* Vị trí các nút bấm dưới đáy modal */
.modal-actions { 
  display: flex; 
  gap: 10px; 
  justify-content: flex-end;                    /* DÒNG MẶC ĐỊNH: Căn nút sang góc PHẢI */
  /* THAY THẾ: Căn các nút ra CHÍNH GIỮA:
  justify-content: center; */
  /* THAY THẾ: Căn các nút sang góc TRÁI:
  justify-content: flex-start; */
  margin-top: 20px; 
}

/* 6.1 NÚT HỦY BỎ TRONG MODAL */
.btn-cancel { 
  background: #eee;                             /* Nền nút Hủy bỏ: XÁM NHẸ */
  border: none; 
  padding: 10px 18px; 
  border-radius: 4px; 
  cursor: pointer; 
  font-weight: bold; 
  color: #555;                                  /* Chữ nút Hủy bỏ: XÁM ĐẬM */
  
  /* THAY THẾ: Nút Hủy bo tròn hoàn toàn:
  border-radius: 50px; */
}

/* 6.2 NÚT LƯU QUYẾT ĐỊNH (KHI DUYỆT THÀNH CÔNG) */
.btn-submit-approve { 
  background: #16a34a;                          /* Nền nút Lưu duyệt: MÀU XANH LÁ */
  color: white;                                 /* Chữ nút: MÀU TRẮNG */
  border: none; 
  padding: 10px 20px; 
  font-weight: bold; 
  border-radius: 4px; 
  cursor: pointer; 
  
  /* THAY THẾ: Nút Lưu duyệt bo tròn hoàn toàn:
  border-radius: 50px; */
}

/* 6.3 NÚT LƯU QUYẾT ĐỊNH (KHI TỪ CHỐI) */
.btn-submit-reject { 
  background: #dc2626;                          /* Nền nút Lưu từ chối: MÀU ĐỎ */
  color: white;                                 /* Chữ nút: MÀU TRẮNG */
  border: none; 
  padding: 10px 20px; 
  font-weight: bold; 
  border-radius: 4px; 
  cursor: pointer; 
  
  /* THAY THẾ: Nút Lưu từ chối bo tròn hoàn toàn:
  border-radius: 50px; */
}

.image-gallery { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr);        /* Chia làm 3 cột ảnh đều nhau */
  gap: 12px; 
  margin: 20px 0; 
  max-height: 400px; 
  overflow-y: auto; 
}
.gallery-img { width: 100%; height: 180px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd; }

/* 6.4 NÚT ĐÓNG MODAL ẢNH */
.btn-close-modal { 
  width: 100%; 
  padding: 12px; 
  background: #3e332e;                          /* Nền nút đóng modal ảnh: NÂU GỖ ĐẬM */
  color: white;                                 /* Chữ nút: MÀU TRẮNG */
  border: none; 
  border-radius: 4px; 
  font-weight: bold; 
  cursor: pointer; 
}
.empty-cell { text-align: center; color: #888; padding: 30px 0; }

/* =========================================================================
   [CSS NHÓM 7: POPUP XÁC NHẬN HÀNH ĐỘNG (CUSTOM CONFIRM DIALOG)]
========================================================================= */
.custom-dialog-box {
  width: 420px !important;                      /* Chiều rộng hộp xác nhận: 420px */
  padding: 30px 25px; 
  text-align: center;                           /* Căn toàn bộ chữ ra CHÍNH GIỮA */
  border-radius: 8px; 
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2); 
  animation: scaleIn 0.25s ease;                /* Hiệu ứng phóng to nhẹ trong 0.25 giây */
}

/* Vòng tròn bọc icon ở giữa */
.dialog-icon-wrapper {
  width: 60px; 
  height: 60px; 
  border-radius: 50%;                           /* Bo tròn 100% */
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin: 0 auto 16px; 
  font-size: 26px; 
}
.dialog-icon-wrapper.gold {
  background-color: #fcf6eb;                    /* Nền icon: VÀNG KEM NHẠT */
  color: var(--gold-matte);                     /* Màu icon: VÀNG HOÀNG KIM */
}
.dialog-icon-wrapper.danger {
  background-color: #fde8e8;                    /* Nền icon: ĐỎ HỒNG NHẠT */
  color: #dc3545;                               /* Màu icon: ĐỎ TƯƠI */
}

.dialog-title {
  margin: 0 0 10px 0; 
  font-size: 19px; 
  color: var(--wood-dark);                      /* Màu chữ tiêu đề: NÂU GỖ ĐẬM */
  font-weight: 700; 
}

.dialog-message {
  margin: 0 0 24px 0; 
  font-size: 14px; 
  color: #666; 
  line-height: 1.5; 
}

.dialog-actions {
  display: flex; 
  justify-content: center;                      /* Căn 2 nút ra CHÍNH GIỮA */
  gap: 12px; 
}

.btn-dialog-cancel {
  background: #f1f1f1;                          /* Nền nút: XÁM NHẠT */
  border: 1px solid #ddd;                       /* Viền: XÁM */
  color: #555; 
  padding: 9px 20px; 
  border-radius: 4px; 
  font-weight: 600; 
  cursor: pointer; 
  transition: 0.2s; 
}
.btn-dialog-cancel:hover {
  background: #e4e4e4; 
}

.btn-dialog-confirm {
  padding: 9px 22px; 
  border-radius: 4px; 
  font-weight: 600; 
  cursor: pointer; 
  border: none; 
  color: #fff;                                  /* Chữ nút: MÀU TRẮNG */
  transition: 0.2s; 
}
.btn-gold-confirm {
  background-color: var(--gold-matte);          /* Nền nút xác nhận duyệt: MÀU VÀNG */
}
.btn-gold-confirm:hover {
  background-color: #b88d4c; 
}
.btn-danger-confirm {
  background-color: #dc3545;                    /* Nền nút xác nhận từ chối: MÀU ĐỎ */
}
.btn-danger-confirm:hover {
  background-color: #bd2130; 
}

/* =========================================================================
   [CSS NHÓM 8: TOAST THÔNG BÁO TỰ TẮT (VỊ TRÍ & MÀU SẮC TOAST)]
========================================================================= */
.custom-alert-toast {
  position: fixed;                              /* Cố định vị trí trên màn hình */
  
  /* [VỊ TRÍ HIỂN THỊ CỦA THÔNG BÁO TOAST] */
  bottom: 30px;                                 /* DÒNG MẶC ĐỊNH: Cách mép dưới 30px */
  right: 30px;                                  /* DÒNG MẶC ĐỊNH: Cách mép phải 30px (GÓC DƯỚI PHẢI) */

  /* THAY THẾ: Chuyển Toast sang GÓC DƯỚI BÊN TRÁI:
  bottom: 30px; left: 30px; */
  
  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN PHẢI:
  top: 30px; right: 30px; */
  
  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN TRÁI:
  top: 30px; left: 30px; */
  
  /* THAY THẾ: Căn Toast ra CHÍNH GIỮA MÀN HÌNH PHÍA TRÊN:
  top: 30px; left: 50%; transform: translateX(-50%); */

  padding: 14px 22px; 
  border-radius: 6px; 
  background: #333;                             /* Nền mặc định: ĐEN XÁM */
  color: #fff;                                  /* Chữ bên trong: MÀU TRẮNG */
  font-size: 14px; 
  font-weight: 500; 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); 
  z-index: 9999; 
  transform: translateY(100px);                 /* Vị trí ẩn: Thụt xuống 100px */
  opacity: 0;                                   /* Độ trong suốt bằng 0 */
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55); 
}

/* Khi kích hoạt hiển thị thông báo */
.custom-alert-toast.show {
  transform: translateY(0);                     /* Trượt lên vị trí chuẩn */
  opacity: 1;                                   /* Hiện rõ 100% */
}

/* Khi thông báo là THÀNH CÔNG */
.custom-alert-toast.success {
  background: #198754;                          /* Nền thông báo thành công: MÀU XANH LÁ */
}

/* Khi thông báo là THẤT BẠI / LỖI */
.custom-alert-toast.error {
  background: #dc3545;                          /* Nền thông báo lỗi: MÀU ĐỎ */
}

/* Hiệu ứng phóng to nhẹ của Modal */
@keyframes scaleIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>