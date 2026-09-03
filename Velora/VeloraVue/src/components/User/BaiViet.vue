<template>
  <div class="marketing-page-wrapper">
    <Header />

    <main class="marketing-main-content">
      <div class="container">

        <!-- ========================================================================= -->
        <!-- GIAO DIỆN 1: DÀNH CHO ADMIN VÀ NHÂN VIÊN SALE (QUẢN LÝ) -->
        <!-- ========================================================================= -->
        <div v-if="isManager" class="manager-view">
          <header class="content-header">
            <div class="header-left">
              <h1>Quản Lý <span class="gold-text">Chiến Dịch</span></h1>
              <p class="sub-title">Không gian làm việc của Khối Kinh Doanh & Marketing.</p>
            </div>
            <button class="btn-add-new" @click="openAdminModal()">
              <i class="fa-solid fa-pen-nib"></i> Viết Bài Mới
            </button>
          </header>

          <div class="articles-grid">
            <!-- Loading Spinner -->
            <div v-if="loading" class="text-center" style="grid-column: 1 / -1; padding: 50px;">
              <i class="fa-solid fa-circle-notch fa-spin gold-text" style="font-size: 30px;"></i>
              <p style="margin-top: 10px;">Đang tải dữ liệu từ máy chủ...</p>
            </div>

            <div v-else class="article-card" v-for="article in articles" :key="article.maBaiViet">
              <div class="article-img">
                <img :src="article.anhBia" :alt="article.tieuDe" @error="(e) => e.target.src = '/img/VeloraIcon.png'" />
                <span class="status-badge" :class="article.trangThai === 'HIEN_THI' ? 'active' : 'hidden'">
                  {{ article.trangThai === 'HIEN_THI' ? 'Đang xuất bản' : 'Đã ẩn' }}
                </span>
              </div>

              <div class="article-body">
                <h3 class="article-title">{{ article.tieuDe }}</h3>
                <p class="article-excerpt">{{ truncateText(article.noiDung, 60) }}</p>

                <!-- Ẩn khung vé Coupon nếu bài viết không xài mã (Mã là NONE) -->
                <div class="coupon-ticket" v-if="article.maGiamGia && article.maGiamGia !== 'NONE'">
                  <div class="coupon-left">
                    <span class="discount-val">-{{ article.phanTramGiam }}%</span>
                    <span class="discount-label">GIẢM GIÁ</span>
                  </div>
                  <div class="coupon-right">
                    <div class="code-row">Mã: <strong>{{ article.maGiamGia }}</strong></div>
                    <div class="limit-row">
                      <i class="fa-solid fa-users"></i> Lượt dùng: {{ article.soLuotDaDung || 0 }}/{{
                      article.soLuotGioiHan }}
                    </div>
                  </div>
                </div>
              </div>

              <div class="article-actions">
                <button class="btn-edit" @click="openAdminModal(article)"><i class="fa-solid fa-pen-to-square"></i>
                  Sửa</button>
                <button class="btn-delete" @click="deleteArticle(article.maBaiViet)"><i class="fa-solid fa-trash"></i>
                  Xóa</button>
              </div>
            </div>
          </div>

          <!-- Popup Modal Thêm/Sửa (Chỉ Manager thấy) -->
          <div class="modal-overlay" v-if="showAdminModal" @click.self="closeAdminModal">
            <div class="modal-content">
              <div class="modal-header">
                <h2>{{ isEditing ? 'Chỉnh Sửa Chiến Dịch' : 'Tạo Chiến Dịch Mới' }}</h2>
                <button class="btn-close" @click="closeAdminModal"><i class="fa-solid fa-xmark"></i></button>
              </div>
              <div class="modal-body form-grid">

                <!-- CỘT 1: NỘI DUNG BÀI VIẾT -->
                <div class="form-column">
                  <h3 class="column-title"><i class="fa-solid fa-newspaper"></i> Nội Dung Bài Viết</h3>
                  <div class="form-group">
                    <label>Tiêu đề chiến dịch <span class="required">*</span></label>
                    <input type="text" v-model="formData.tieuDe" placeholder="VD: Khuyến mãi thu vàng..." required>
                  </div>

                  <!-- NÚT UPLOAD ẢNH TỪ MÁY TÍNH -->
                  <div class="form-group">
                    <label>Ảnh bìa chiến dịch <span class="required">*</span></label>
                    <div class="custom-file-upload">
                      <input type="file" id="file-upload" @change="handleFileUpload" accept="image/*"
                        class="hidden-input" />
                      <label for="file-upload" class="btn-upload">
                        <i class="fa-solid fa-cloud-arrow-up"></i> Chọn ảnh từ máy tính
                      </label>
                      <span class="file-name" v-if="selectedFileName" :title="selectedFileName">{{ selectedFileName
                        }}</span>
                    </div>
                    <!-- Khung Preview Ảnh ngay khi chọn -->
                    <div class="image-preview-box" v-if="imagePreview || formData.anhBia">
                      <img :src="imagePreview || formData.anhBia" alt="Preview ảnh bìa" />
                    </div>
                  </div>

                  <div class="form-group">
                    <label>Nội dung chi tiết <span class="required">*</span></label>
                    <textarea v-model="formData.noiDung" rows="6" placeholder="Nhập nội dung quảng bá sản phẩm..."
                      required></textarea>
                  </div>
                </div>

                <!-- CỘT 2: CHỌN MÃ ƯU ĐÃI TỪ HỆ THỐNG -->
                <div class="form-column coupon-setup">
                  <h3 class="column-title"><i class="fa-solid fa-ticket"></i> Mã Ưu Đãi Kèm Theo</h3>

                  <!-- SELECT CHỌN MÃ TỪ DATABASE -->
                  <div class="form-group">
                    <label>Chọn Mã Giảm Giá</label>
                    <select v-model="formData.maGiamGia" @change="handleCouponChange">
                      <option value="">-- Không áp dụng / Chọn mã --</option>
                      <option v-for="coupon in availableCoupons" :key="coupon.maCode || coupon.ma_code"
                        :value="coupon.maCode || coupon.ma_code">
                        {{ coupon.maCode || coupon.ma_code }} (Giảm {{ coupon.phanTramGiam || coupon.phan_tram_giam }}%)
                      </option>
                    </select>
                  </div>

                  <!-- KHUNG PREVIEW THÔNG TIN MÃ (KHÓA READ-ONLY) -->
                  <div v-if="formData.maGiamGia && formData.maGiamGia !== 'NONE'" class="coupon-preview-box">
                    <div class="form-row-2">
                      <div class="form-group">
                        <label>Mức giảm (%)</label>
                        <input type="number" v-model="formData.phanTramGiam" disabled class="disabled-input">
                      </div>
                      <div class="form-group">
                        <label>Giới hạn (lượt)</label>
                        <input type="number" v-model="formData.soLuotGioiHan" disabled class="disabled-input">
                      </div>
                    </div>
                    <div class="form-group">
                      <label>Hạn sử dụng</label>
                      <input type="datetime-local" v-model="formData.hanSuDung" disabled class="disabled-input">
                    </div>
                  </div>

                  <div class="form-group" style="margin-top: 25px;">
                    <label>Trạng thái hiển thị bài viết</label>
                    <select v-model="formData.trangThai">
                      <option value="HIEN_THI">Đang xuất bản</option>
                      <option value="AN">Lưu nháp / Đã ẩn</option>
                    </select>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button class="btn-cancel" @click="closeAdminModal">Hủy bỏ</button>
                <button class="btn-save" @click="saveArticle">
                  <i class="fa-solid fa-floppy-disk"></i> {{ isEditing ? 'Lưu Thay Đổi' : 'Đăng Bài Viết' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Popup Confirm Xóa (Chỉ Manager thấy) -->
          <div class="modal-overlay" v-if="showConfirmModal" @click.self="cancelDelete">
            <div class="modal-content confirm-modal">
              <div class="modal-body text-center" style="padding: 40px 30px;">
                <i class="fa-solid fa-circle-exclamation text-gold" style="font-size: 50px; margin-bottom: 20px;"></i>
                <h2 style="margin: 0 0 15px 0; color: #1a1a1a;">Xác nhận xóa?</h2>
                <p style="color: #666; font-size: 15px;">Bạn có chắc chắn muốn xóa chiến dịch này không? Thao tác này
                  không thể hoàn tác.</p>
                <div style="display: flex; justify-content: center; gap: 15px; margin-top: 30px;">
                  <button class="btn-cancel" @click="cancelDelete">Hủy bỏ</button>
                  <button class="btn-delete-confirm" @click="confirmDeleteAction">Xóa vĩnh viễn</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========================================================================= -->
        <!-- GIAO DIỆN 2: DÀNH CHO KHÁCH HÀNG (ĐỌC BÀI, NHẬN MÃ & BÌNH LUẬN) -->
        <!-- ========================================================================= -->
        <div v-else class="customer-view">

          <!-- Màn hình danh sách bài viết -->
          <div v-if="!viewingArticle">
            <div class="section-title-wrapper text-center" style="margin-bottom: 40px;">
              <span class="section-subtitle">Thông tin mới nhất</span>
              <h2 class="section-title">TIN TỨC & ƯU ĐÃI</h2>
              <div class="title-divider" style="margin: 15px auto;"><span class="diamond"></span></div>
            </div>

            <div class="articles-grid">
              <div v-if="loading" class="text-center" style="grid-column: 1 / -1; padding: 50px;">
                <i class="fa-solid fa-circle-notch fa-spin gold-text" style="font-size: 30px;"></i>
                <p style="margin-top: 10px;">Đang tải tin tức từ máy chủ...</p>
              </div>

              <div v-else class="article-card customer-card" v-for="article in articles" :key="article.maBaiViet">
                <div class="article-img">
                  <img :src="article.anhBia" :alt="article.tieuDe"
                    @error="(e) => e.target.src = '/img/VeloraIcon.png'" />
                </div>
                <div class="article-body">
                  <div class="article-meta-small">
                    <i class="fa-regular fa-clock"></i> {{ formatDate(article.ngayTao) }}
                  </div>
                  <h3 class="article-title">{{ article.tieuDe }}</h3>
                  <p class="article-excerpt">{{ truncateText(article.noiDung, 100) }}</p>
                  <button class="btn-read-more" @click="readArticle(article)">Xem Chi Tiết & Nhận Mã <i
                      class="fa-solid fa-arrow-right"></i></button>
                </div>
              </div>
            </div>

            <div v-if="!loading && articles.length === 0" class="text-center" style="padding: 50px; color: #888;">
              Chưa có bài viết hay chương trình khuyến mãi nào được xuất bản!
            </div>
          </div>

          <!-- Màn hình Chi tiết bài viết cho Khách -->
          <div v-else class="article-detail-view">
            <button class="btn-back" @click="viewingArticle = null">
              <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
            </button>

            <div class="hero-image">
              <img :src="viewingArticle.anhBia" :alt="viewingArticle.tieuDe"
                @error="(e) => e.target.src = '/img/VeloraIcon.png'" />
            </div>

            <div class="article-content-wrapper">
              <h1 class="article-detail-title">{{ viewingArticle.tieuDe }}</h1>
              <div class="article-meta">
                <span><i class="fa-regular fa-calendar"></i> Xuất bản: {{ formatDate(viewingArticle.ngayTao) }}</span>
                <span><i class="fa-solid fa-pen-nib"></i> Velora Editorial</span>
              </div>
              <div class="title-divider"><span class="diamond"></span></div>
              <p class="article-text">{{ viewingArticle.noiDung }}</p>

              <!-- Box Khuyến mãi (Ẩn nếu mã là NONE) -->
              <div class="customer-coupon-box" v-if="viewingArticle.maGiamGia && viewingArticle.maGiamGia !== 'NONE'">
                <h3><i class="fa-solid fa-gift text-gold"></i> ƯU ĐÃI ĐỘC QUYỀN TRONG BÀI VIẾT</h3>
                <p>Sử dụng mã dưới đây khi thanh toán để nhận đặc quyền từ Velora Clock.</p>
                <div class="coupon-display">
                  <div class="coupon-amount">-{{ viewingArticle.phanTramGiam }}%</div>
                  <div class="coupon-code">{{ viewingArticle.maGiamGia }}</div>
                  <button class="btn-copy" @click="copyCode(viewingArticle.maGiamGia)">
                    <i class="fa-regular fa-copy"></i> Sao chép
                  </button>
                </div>
                <p class="coupon-note">* Lưu ý: Áp dụng đến {{ formatDate(viewingArticle.hanSuDung) }}. Số lượng có hạn.
                </p>
              </div>

              <!-- ========================================================================= -->
              <!-- KHU VỰC BÌNH LUẬN (TÍCH HỢP COMPONENT SOC DÙNG CHUNG) -->
              <!-- ========================================================================= -->
              <DanhGiaSanPham v-if="viewingArticle && viewingArticle.maBaiViet" :maBaiViet="viewingArticle.maBaiViet"
                loaiDanhGia="BAI_VIET" />

            </div>
          </div>
        </div>

        <!-- ========================================================================= -->
        <!-- TOAST NOTIFICATION GÓC MÀN HÌNH -->
        <!-- ========================================================================= -->
        <div class="toast-container">
          <TransitionGroup name="toast">
            <div v-for="toast in toasts" :key="toast.id" class="toast-message" :class="toast.type">
              <i :class="toast.icon"></i>
              <div class="toast-content">
                <p class="toast-title">{{ toast.title }}</p>
                <p class="toast-desc">{{ toast.message }}</p>
              </div>
              <button class="toast-close" @click="removeToast(toast.id)"><i class="fa-solid fa-xmark"></i></button>
            </div>
          </TransitionGroup>
        </div>

      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Header from '../Header.vue';
import Footer from '../Footer.vue';

// 🔥 IMPORT COMPONENT ĐÁNH GIÁ (SỬ DỤNG CHUNG TỪ TRANG SẢN PHẨM)
import DanhGiaSanPham from './DanhGiaSanPham.vue'; // Điều chỉnh lại đường dẫn nếu file nằm ở thư mục khác

const host = window.location.hostname;
const API_URL = `http://${host}:8080/api/bai-viet`;

const isManager = ref(false);
const currentUserRole = ref('');
const currentUserId = ref(null);
const loading = ref(true);
const articles = ref([]);

// ==========================================
// HỆ THỐNG TOAST NOTIFICATION
// ==========================================
const toasts = ref([]);
let toastIdCounter = 0;

const showToast = (title, message, type = 'success') => {
  const id = toastIdCounter++;
  let icon = 'fa-solid fa-circle-check';
  if (type === 'error') icon = 'fa-solid fa-circle-exclamation';
  if (type === 'warning') icon = 'fa-solid fa-triangle-exclamation';

  toasts.value.push({ id, title, message, type, icon });
  setTimeout(() => removeToast(id), 3000);
};

const removeToast = (id) => { toasts.value = toasts.value.filter(t => t.id !== id); };

// ==========================================
// TẢI DỮ LIỆU BÀI VIẾT & MÃ GIẢM GIÁ
// ==========================================
const availableCoupons = ref([]);

const fetchArticles = async () => {
  loading.value = true;
  try {
    const endpoint = isManager.value ? API_URL : `${API_URL}/hien-thi`;
    const res = await fetch(endpoint);
    if (res.ok) {
      articles.value = await res.json();
    }
  } catch (error) {
    showToast('Lỗi tải dữ liệu', 'Không thể kết nối đến máy chủ bài viết', 'error');
  } finally {
    loading.value = false;
  }
};

const fetchCoupons = async () => {
  try {
    const res = await fetch(`http://${host}:8080/api/admin/ma-giam-gia?size=100`);
    if (res.ok) {
      const data = await res.json();
      availableCoupons.value = data.content || data;
    }
  } catch (error) {
    showToast('Lỗi', 'Không thể tải danh sách mã giảm giá', 'error');
  }
};

// ==========================================
// ON MOUNTED & KIỂM TRA PHÂN QUYỀN
// ==========================================
onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr && userStr !== 'undefined') {
    try {
      const user = JSON.parse(userStr);
      let role = '';
      if (user.vaiTros && Array.isArray(user.vaiTros) && user.vaiTros.length > 0) {
        role = user.vaiTros[0].tenVaiTro || '';
      } else {
        role = user.vaiTro || '';
      }

      currentUserRole.value = role.toUpperCase();
      currentUserId.value = user.maNguoiDung || user.id || 1;

      // Phân quyền màn hình
      if (currentUserRole.value === 'ROLE_ADMIN' || currentUserRole.value === 'ROLE_SALE') {
        isManager.value = true;
        fetchCoupons();
      } else {
        isManager.value = false;
      }
    } catch (e) {
      isManager.value = false;
    }
  } else {
    isManager.value = false;
  }

  fetchArticles();
});

// ==========================================
// LOGIC CỦA ADMIN/SALE (THÊM / SỬA / XÓA)
// ==========================================
const showAdminModal = ref(false);
const isEditing = ref(false);
const formData = ref({});
const selectedFile = ref(null);
const selectedFileName = ref('');
const imagePreview = ref('');

const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  selectedFile.value = file;
  selectedFileName.value = file.name;

  const reader = new FileReader();
  reader.onload = (e) => { imagePreview.value = e.target.result; };
  reader.readAsDataURL(file);
};

const toDatetimeLocal = (isoString) => {
  if (!isoString) return '';
  const date = new Date(isoString);
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const openAdminModal = (article = null) => {
  selectedFile.value = null;
  selectedFileName.value = '';
  imagePreview.value = '';

  if (article) {
    isEditing.value = true;
    formData.value = { ...article };

    if (formData.value.maGiamGia === 'NONE') {
      formData.value.maGiamGia = '';
    }

    if (formData.value.hanSuDung && !formData.value.hanSuDung.includes('2099-12-31')) {
      formData.value.hanSuDung = toDatetimeLocal(formData.value.hanSuDung);
    } else {
      formData.value.hanSuDung = '';
    }
  } else {
    isEditing.value = false;
    formData.value = {
      nguoiDang: currentUserId.value,
      tieuDe: '', noiDung: '', anhBia: '',
      maGiamGia: '', phanTramGiam: 0, soLuotGioiHan: 0,
      hanSuDung: '', trangThai: 'HIEN_THI'
    };
  }
  showAdminModal.value = true;
};
const closeAdminModal = () => showAdminModal.value = false;

const handleCouponChange = () => {
  const selected = availableCoupons.value.find(c => (c.maCode || c.ma_code) === formData.value.maGiamGia);
  if (selected) {
    formData.value.phanTramGiam = selected.phanTramGiam || selected.phan_tram_giam || 0;
    formData.value.soLuotGioiHan = selected.gioiHanSuDung || selected.gioi_han_su_dung || 100;

    const hsd = selected.ngayHetHan || selected.ngay_het_han;
    if (hsd) {
      formData.value.hanSuDung = toDatetimeLocal(hsd);
    } else {
      formData.value.hanSuDung = '';
    }
  } else {
    formData.value.phanTramGiam = 0;
    formData.value.soLuotGioiHan = 0;
    formData.value.hanSuDung = '';
  }
};

const saveArticle = async () => {
  try {
    const method = isEditing.value ? 'PUT' : 'POST';
    const url = isEditing.value ? `${API_URL}/${formData.value.maBaiViet}` : API_URL;

    const payload = { ...formData.value };

    if (selectedFile.value) {
      payload.anhBia = '/img/' + selectedFile.value.name;
    }

    if (payload.hanSuDung) {
      payload.hanSuDung = new Date(payload.hanSuDung).toISOString();
    } else {
      payload.hanSuDung = new Date('2099-12-31T23:59:59').toISOString();
    }

    if (!payload.maGiamGia || payload.maGiamGia.trim() === '') {
      payload.maGiamGia = 'NONE';
    }

    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      showToast('Thành công', isEditing.value ? 'Đã cập nhật chiến dịch!' : 'Đã xuất bản chiến dịch mới!', 'success');
      fetchArticles();
      closeAdminModal();
    } else {
      showToast('Thất bại', 'Có lỗi xảy ra khi lưu bài viết!', 'error');
    }
  } catch (error) {
    showToast('Lỗi kết nối', 'Không thể kết nối đến máy chủ!', 'error');
  }
};

const showConfirmModal = ref(false);
const articleToDelete = ref(null);

const deleteArticle = (id) => {
  articleToDelete.value = id;
  showConfirmModal.value = true;
};
const cancelDelete = () => {
  articleToDelete.value = null;
  showConfirmModal.value = false;
};

const confirmDeleteAction = async () => {
  if (!articleToDelete.value) return;

  try {
    const res = await fetch(`${API_URL}/${articleToDelete.value}`, { method: 'DELETE' });
    if (res.ok) {
      showToast('Đã xóa', 'Chiến dịch đã được xóa khỏi hệ thống!', 'success');
      fetchArticles();
    }
  } catch (error) {
    showToast('Lỗi', 'Có lỗi xảy ra trong quá trình xóa!', 'error');
  } finally {
    cancelDelete();
  }
};

// ==========================================
// LOGIC CỦA KHÁCH HÀNG (HIỂN THỊ CHI TIẾT BÀI VIẾT)
// ==========================================
const viewingArticle = ref(null);

const readArticle = (article) => {
  viewingArticle.value = article;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const copyCode = (code) => {
  navigator.clipboard.writeText(code);
  showToast('Đã lưu mã', `Mã ${code} đã được lưu vào bộ nhớ tạm!`, 'success');
};

const truncateText = (text, length) => {
  if (!text) return '';
  return text.length <= length ? text : text.substring(0, length) + '...';
}
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
};
</script>

<style scoped>
:root {
  --wood-dark: #362921;
  --gold-matte: #cca15e;
}

.gold-text {
  color: #cca15e;
}

.text-gold {
  color: #cca15e;
}

.text-gray {
  color: #ccc;
}

.text-center {
  text-align: center;
}

.marketing-page-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f8f6f0;
}

.marketing-main-content {
  flex: 1;
  padding: 50px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* CSS ADMIN VIEW */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
  border-bottom: 2px solid #eaeaea;
  padding-bottom: 20px;
}

.content-header h1 {
  font-size: 32px;
  color: #362921;
  margin: 0 0 8px 0;
  font-family: 'Playfair Display', serif;
}

.sub-title {
  color: #666;
  font-size: 15px;
  margin: 0;
}

.btn-add-new {
  background-color: #362921;
  color: #fff;
  border: none;
  padding: 12px 25px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: 0.3s;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn-add-new:hover {
  background-color: #cca15e;
  transform: translateY(-2px);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
}

.article-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  transition: transform 0.3s;
  border: 1px solid #eaeaea;
}

.article-card:hover {
  transform: translateY(-5px);
}

.article-img {
  width: 100%;
  height: 220px;
  position: relative;
}

.article-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
  color: #fff;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.status-badge.active {
  background-color: #2ecc71;
}

.status-badge.hidden {
  background-color: #e74c3c;
}

.article-body {
  padding: 25px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 20px;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.4;
  font-family: 'Playfair Display', serif;
  font-weight: bold;
}

.article-excerpt {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 25px;
  flex: 1;
}

.coupon-ticket {
  display: flex;
  background: #fffdf8;
  border: 1px dashed #cca15e;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
  position: relative;
}

.coupon-ticket::before,
.coupon-ticket::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  background: #fff;
  border: 1px dashed #cca15e;
  border-radius: 50%;
  top: 50%;
  transform: translateY(-50%);
}

.coupon-ticket::before {
  left: -9px;
  border-right-color: transparent;
  border-top-color: transparent;
  transform: translateY(-50%) rotate(45deg);
}

.coupon-ticket::after {
  right: -9px;
  border-left-color: transparent;
  border-bottom-color: transparent;
  transform: translateY(-50%) rotate(45deg);
}

.coupon-left {
  background: rgba(204, 161, 94, 0.1);
  padding: 15px 10px;
  text-align: center;
  border-right: 1px dashed #cca15e;
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 80px;
}

.discount-val {
  font-size: 20px;
  font-weight: 900;
  color: #cca15e;
}

.discount-label {
  font-size: 10px;
  color: #888;
  font-weight: bold;
  margin-top: 2px;
}

.coupon-right {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.code-row {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.code-row strong {
  color: #d35400;
  font-family: monospace;
  font-size: 16px;
  letter-spacing: 2px;
}

.limit-row {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.limit-row i {
  color: #cca15e;
  width: 16px;
}

.article-actions {
  display: flex;
  border-top: 1px solid #eaeaea;
  background: #faf9f6;
}

.article-actions button {
  flex: 1;
  padding: 15px 0;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.btn-edit {
  color: #2980b9;
  border-right: 1px solid #eaeaea !important;
}

.btn-edit:hover {
  background: #eaf2f8;
}

.btn-delete {
  color: #e74c3c;
}

.btn-delete:hover {
  background: #fdedec;
}

/* MODAL ADMIN & CSS FORM */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}

.modal-content {
  background: #fff;
  width: 900px;
  max-height: 90vh;
  overflow-y: auto;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideDown 0.3s ease-out;
}

.confirm-modal {
  width: 400px;
}

@keyframes slideDown {
  from {
    transform: translateY(-30px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 20px 30px;
  border-bottom: 1px solid #eaeaea;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fcfbf9;
  position: sticky;
  top: 0;
  z-index: 10;
}

.modal-header h2 {
  margin: 0;
  font-size: 22px;
  color: #362921;
  font-family: 'Playfair Display', serif;
}

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #888;
}

.btn-close:hover {
  color: #e74c3c;
}

.form-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 30px;
  padding: 30px;
}

.column-title {
  font-size: 16px;
  color: #cca15e;
  margin-top: 0;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.form-group {
  margin-bottom: 18px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #e74c3c;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: 0.3s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: #cca15e;
  outline: none;
  box-shadow: 0 0 0 3px rgba(204, 161, 94, 0.15);
}

.disabled-input {
  background-color: #f4f4f4 !important;
  color: #888 !important;
  cursor: not-allowed;
  border-color: #eaeaea !important;
}

.coupon-preview-box {
  background: #fff;
  padding: 15px;
  border: 1px dashed #ccc;
  border-radius: 8px;
}

.custom-file-upload {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 5px;
}

.hidden-input {
  display: none;
}

.btn-upload {
  background: #fff;
  border: 1px solid #cca15e;
  color: #cca15e;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  transition: 0.3s;
  display: inline-block;
}

.btn-upload:hover {
  background: #cca15e;
  color: #fff;
}

.file-name {
  font-size: 13px;
  color: #666;
  font-style: italic;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-preview-box {
  margin-top: 15px;
  width: 100%;
  max-height: 220px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px dashed #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
}

.image-preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coupon-setup {
  background: #faf9f6;
  padding: 25px;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.modal-footer {
  padding: 20px 30px;
  border-top: 1px solid #eaeaea;
  background: #faf9f6;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.btn-cancel {
  padding: 12px 25px;
  border: 1px solid #ccc;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
}

.btn-cancel:hover {
  background: #f0f0f0;
}

.btn-save {
  padding: 12px 30px;
  border: none;
  background: #362921;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: 0.3s;
}

.btn-save:hover {
  background: #cca15e;
  transform: translateY(-2px);
}

.btn-delete-confirm {
  padding: 12px 25px;
  border: none;
  background: #e74c3c;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(231, 76, 60, 0.2);
  transition: 0.3s;
}

.btn-delete-confirm:hover {
  background: #c0392b;
  transform: translateY(-2px);
}

/* CSS CUSTOMER VIEW */
.section-subtitle {
  display: block;
  font-size: 12px;
  letter-spacing: 3px;
  color: #c5a880;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.section-title {
  font-size: 32px;
  font-family: 'Playfair Display', serif;
  color: #1a1a1a;
  margin: 0;
}

.title-divider {
  width: 60px;
  height: 2px;
  background: #c5a880;
  position: relative;
}

.customer-card .article-img {
  height: 240px;
}

.article-meta-small {
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
  font-weight: 500;
}

.btn-read-more {
  background: transparent;
  border: none;
  color: #c5a880;
  font-weight: 600;
  font-size: 14px;
  text-align: left;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}

.btn-read-more:hover {
  color: #1a1a1a;
}

.btn-back {
  background: none;
  border: none;
  color: #666;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 20px;
  transition: 0.2s;
}

.btn-back:hover {
  color: #c5a880;
}

.hero-image {
  width: 100%;
  height: 450px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.hero-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-content-wrapper {
  max-width: 850px;
  margin: 0 auto;
  background: #fff;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.03);
}

.article-detail-title {
  font-size: 36px;
  font-family: 'Playfair Display', serif;
  color: #1a1a1a;
  margin: 0 0 15px 0;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #888;
  margin-bottom: 25px;
}

.article-text {
  font-size: 16px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 40px;
  white-space: pre-wrap;
}

.customer-coupon-box {
  background: #fffdf8;
  border: 2px dashed #c5a880;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  margin-bottom: 50px;
}

.customer-coupon-box h3 {
  margin: 0 0 10px 0;
  color: #1a1a1a;
  font-size: 18px;
}

.coupon-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin: 20px 0;
}

.coupon-amount {
  background: #1a1a1a;
  color: #c5a880;
  font-size: 24px;
  font-weight: bold;
  padding: 10px 20px;
  border-radius: 6px;
}

.coupon-code {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  color: #1a1a1a;
  border-bottom: 2px solid #1a1a1a;
  padding-bottom: 5px;
}

.btn-copy {
  background: transparent;
  border: 1px solid #1a1a1a;
  padding: 10px 15px;
  cursor: pointer;
  font-weight: 600;
  border-radius: 4px;
  transition: 0.2s;
}

.btn-copy:hover {
  background: #1a1a1a;
  color: #fff;
}

.coupon-note {
  font-size: 13px;
  color: #888;
  font-style: italic;
  margin: 0;
}

/* ========================================================================= */
/* CSS TOAST NOTIFICATION (HIỆU ỨNG TRƯỢT TỪ GÓC PHẢI MÀN HÌNH)               */
/* ========================================================================= */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.toast-message {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  background: #fff;
  padding: 18px 20px;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 320px;
  border-left: 5px solid;
  position: relative;
}

.toast-message i {
  font-size: 24px;
  margin-top: 2px;
}

.toast-message.success {
  border-left-color: #2ecc71;
}

.toast-message.success i {
  color: #2ecc71;
}

.toast-message.error {
  border-left-color: #e74c3c;
}

.toast-message.error i {
  color: #e74c3c;
}

.toast-message.warning {
  border-left-color: #f39c12;
}

.toast-message.warning i {
  color: #f39c12;
}

.toast-content {
  flex: 1;
}

.toast-title {
  margin: 0 0 5px 0;
  font-weight: bold;
  font-size: 15px;
  color: #333;
}

.toast-desc {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  position: absolute;
  top: 15px;
  right: 15px;
  cursor: pointer;
  color: #aaa;
  font-size: 16px;
}

.toast-close:hover {
  color: #333;
}

/* Hiệu ứng trượt (Transition) */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(50px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-30px) scale(0.9);
}

@media (max-width: 768px) {
  .coupon-display {
    flex-direction: column;
  }

  .article-content-wrapper {
    padding: 25px;
  }

  .toast-container {
    right: 50%;
    transform: translateX(50%);
    top: 10px;
  }
}
</style>