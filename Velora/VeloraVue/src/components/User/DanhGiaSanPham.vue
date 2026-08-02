<template>
  <div class="reviews-section">
    <div class="section-header">
      <h2>ĐÁNH GIÁ KIỆT TÁC</h2>
      <div class="header-divider"><span class="diamond">◆</span></div>
    </div>

    <!-- KHU VỰC VIẾT ĐÁNH GIÁ -->
    <div class="review-form-container">
      <h3 class="form-title">Chia sẻ cảm nhận của bạn</h3>
      
      <div class="star-rating-input">
        <span class="label">Chất lượng tuyệt tác:</span>
        <div class="stars">
          <i 
            v-for="star in 5" 
            :key="star"
            class="fa-star"
            :class="star <= newReview.soSao ? 'fas active' : 'far'"
            @click="newReview.soSao = star"
          ></i>
        </div>
      </div>

      <textarea 
        v-model="newReview.binhLuan" 
        class="review-textarea" 
        placeholder="Tuyệt tác này mang lại cảm giác thế nào khi trên tay? Trải nghiệm bộ máy và độ hoàn thiện ra sao?..."
        rows="4"
      ></textarea>

      <button class="btn-submit-review" @click="submitReview" :disabled="isSubmitting">
        {{ isSubmitting ? 'ĐANG GỬI...' : 'GỬI ĐÁNH GIÁ' }}
      </button>
    </div>

    <!-- DANH SÁCH BÌNH LUẬN -->
    <div class="reviews-list">
      <div v-if="loading" class="loader">Đang tải đánh giá...</div>
      <div v-else-if="reviews.length === 0" class="no-reviews">
        Chưa có đánh giá nào. Hãy là người đầu tiên sở hữu và đánh giá kiệt tác này!
      </div>
      
      <template v-else>
        <!-- Hiển thị danh sách đánh giá theo trang -->
        <div class="review-item" v-for="review in paginatedReviews" :key="review.maDanhGia">
          <div class="review-header">
            <div class="reviewer-info">
              <div class="avatar"><i class="fas fa-user"></i></div>
              <span class="reviewer-name">{{ review.nguoiDung?.hoTen || 'Khách hàng VVIP' }}</span>
            </div>
            <div class="review-stars">
              <i v-for="s in 5" :key="s" class="fa-star" :class="s <= review.soSao ? 'fas active' : 'far'"></i>
            </div>
          </div>
          <div class="review-date">{{ formatDate(review.ngayDanhGia) }}</div>
          <p class="review-content">{{ review.binhLuan }}</p>
        </div>

        <!-- THANH PHÂN TRANG -->
        <div class="pagination-container" v-if="totalPages > 1">
          <button 
            class="page-btn" 
            :disabled="currentPage === 1" 
            @click="currentPage--"
          >
            <i class="fas fa-chevron-left"></i> Trước
          </button>
          
          <div class="page-numbers">
            <button 
              v-for="page in totalPages" 
              :key="page" 
              class="page-number-btn"
              :class="{ active: page === currentPage }"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
          </div>

          <button 
            class="page-btn" 
            :disabled="currentPage === totalPages" 
            @click="currentPage++"
          >
            Sau <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { showAlert } from '@/composables/useAlert';

const props = defineProps({
  maSanPham: {
    type: Number,
    required: true
  }
});

const reviews = ref([]);
const loading = ref(false);
const isSubmitting = ref(false);

// Cấu hình phân trang
const currentPage = ref(1);
const pageSize = ref(3); // Số lượng đánh giá hiển thị trên mỗi trang

const newReview = ref({
  soSao: 5,
  binhLuan: ''
});

// Tính tổng số trang
const totalPages = computed(() => {
  return Math.ceil(reviews.value.length / pageSize.value) || 1;
});

// Cắt danh sách đánh giá theo trang hiện tại
const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return reviews.value.slice(start, end);
});

// Lấy danh sách đánh giá
const fetchReviews = async () => {
  loading.value = true;
  try {
    const res = await fetch(`http://localhost:8080/api/danh-gia/san-pham/${props.maSanPham}`);
    if (res.ok) {
      const data = await res.json();
      reviews.value = data;
      currentPage.value = 1; // Reset về trang 1 khi load lại
    }
  } catch (err) {
    console.error('Lỗi tải đánh giá:', err);
  } finally {
    loading.value = false;
  }
};

// Gửi đánh giá mới
const submitReview = async () => {
  if (!newReview.value.binhLuan.trim()) {
    showAlert('Vui lòng nhập nội dung đánh giá!', 'warning');
    return;
  }

  const userStr = localStorage.getItem('user');
  if (!userStr) {
    showAlert('Vui lòng đăng nhập để đánh giá!', 'warning');
    return;
  }

  const user = JSON.parse(userStr);
  
  const payload = {
    maSanPham: props.maSanPham,
    maNguoiDung: user.id || user.maNguoiDung, 
    soSaoDanhGia: newReview.value.soSao,
    binhLuan: newReview.value.binhLuan
  };

  isSubmitting.value = true;
  try {
    const res = await fetch('http://localhost:8080/api/danh-gia/them', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json().catch(() => null);

    if (res.ok) {
      showAlert('Cảm ơn bạn đã chia sẻ cảm nhận!', 'success');
      newReview.value.binhLuan = '';
      newReview.value.soSao = 5; 
      fetchReviews(); 
    } else {
      if (res.status === 403 && data?.message === 'BANNED_3_MINS') {
        showAlert('Bình luận vi phạm tiêu chuẩn! Bạn bị cấm bình luận trong 3 phút.', 'error');
      } else if (res.status === 403 && data?.message === 'ACCOUNT_LOCKED') {
        showAlert('Tài khoản của bạn đã bị khóa do vi phạm nhiều lần. Vui lòng liên hệ Admin.', 'error');
      } else {
        showAlert(data?.message || 'Gửi đánh giá thất bại. Vui lòng thử lại.', 'error');
      }
    }
  } catch (err) {
    console.error('Lỗi gửi đánh giá:', err);
    showAlert('Không thể kết nối đến máy chủ!', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};

onMounted(() => {
  fetchReviews();
});
</script>

<style scoped>
.reviews-section {
  margin-top: 50px;
  padding: 30px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  font-family: 'Playfair Display', serif, sans-serif;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.header-divider {
  color: #c5a880;
  font-size: 12px;
}

.review-form-container {
  background: #fbfbfb;
  margin-bottom: 40px;
  padding: 25px;
  border: 1px solid #eaeaea;
  border-radius: 10px;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.star-rating-input {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.star-rating-input .label {
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

.stars .fa-star {
  font-size: 20px;
  color: #d1d5db;
  cursor: pointer;
  margin-right: 6px;
  transition: color 0.2s;
}

.stars .fa-star.active {
  color: #c5a880;
}

/* Đổi font chữ textarea và placeholder sang Times New Roman */
.review-textarea {
  width: 100%;
  padding: 14px;
  font-size: 15px;
  font-family: 'Times New Roman', Times, serif;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  resize: vertical;
  outline: none;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.review-textarea::placeholder {
  font-family: 'Times New Roman', Times, serif;
  color: #888;
  font-style: italic;
}

.review-textarea:focus {
  border-color: #c5a880;
  box-shadow: 0 0 0 3px rgba(197, 168, 128, 0.15);
}

.btn-submit-review {
  background: #1a1a1a;
  color: #c5a880;
  border: 1px solid #c5a880;
  padding: 12px 28px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1.5px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.btn-submit-review:hover {
  background: #c5a880;
  color: #1a1a1a;
}

.btn-submit-review:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.loader, .no-reviews {
  text-align: center;
  color: #777;
  font-style: italic;
  padding: 20px 0;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-info .avatar {
  width: 36px;
  height: 36px;
  background: #f3f0ea;
  color: #c5a880;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
}

.reviewer-name {
  font-weight: 600;
  font-size: 15px;
  color: #222;
}

.review-stars .fa-star {
  font-size: 13px;
  color: #d1d5db;
  margin-left: 2px;
}

.review-stars .fa-star.active {
  color: #c5a880;
}

.review-date {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  margin-left: 48px;
}

.review-content {
  margin-top: 8px;
  margin-left: 48px;
  font-size: 14px;
  color: #444;
  line-height: 1.6;
}

/* Style cho thanh phân trang */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 30px;
}

.page-btn {
  background: #fff;
  border: 1px solid #d1d5db;
  color: #333;
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #c5a880;
  border-color: #c5a880;
  color: #fff;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

.page-number-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #d1d5db;
  color: #333;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-number-btn:hover {
  border-color: #c5a880;
  color: #c5a880;
}

.page-number-btn.active {
  background: #c5a880;
  border-color: #c5a880;
  color: #fff;
  font-weight: 600;
}
</style>