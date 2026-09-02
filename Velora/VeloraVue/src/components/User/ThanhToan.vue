<template>
  <div class="thanh-toan-nhanh-container">
    <!-- =========================================================================
         [PHẦN 1: NÚT BẤM THANH TOÁN NGAY (BUY NOW BUTTON)]
    ========================================================================== -->
    <!-- DÒNG MẶC ĐỊNH: Nút thanh toán chữ in hoa chuẩn -->
    <button class="btn-primary" @click="xuLyThanhToanNhanh">
      THANH TOÁN NGAY
    </button>
    <!-- THAY THẾ: Thêm icon sấm sét / thẻ ngân hàng vào trước chữ:
    <button class="btn-primary" @click="xuLyThanhToanNhanh"><i class="fas fa-bolt"></i> MUA NGAY</button> -->
    <!-- THAY THẾ: Đổi chữ thành 'ĐẶT HÀNG NHANH':
    <button class="btn-primary" @click="xuLyThanhToanNhanh">ĐẶT HÀNG NHANH</button> -->
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
// Import hàm popup thông báo tùy biến từ composables
import { showAlert } from '@/composables/useAlert';

// =========================================================================
// [PHẦN 2: ĐỊNH NGHĨA PROPS NHẬN TỪ COMPONENT CHA]
// =========================================================================
// Nhận mã sản phẩm và số lượng mua ngay truyền từ trang chi tiết sản phẩm
const props = defineProps({
  maSanPham: {
    type: Number,
    required: true // Bắt buộc phải có mã sản phẩm
  },
  soLuong: {
    type: Number,
    default: 1    // Số lượng mặc định là 1 nếu không truyền
  }
});

const router = useRouter();

// =========================================================================
// [PHẦN 3: LOGIC KIỂM TRA ĐĂNG NHẬP & CHUYỂN TRANG THANH TOÁN]
// =========================================================================
const xuLyThanhToanNhanh = () => {
  // Kiểm tra thông tin người dùng lưu trong localStorage
  const userStr = localStorage.getItem('user');
  
  if (!userStr) {
    // Nếu chưa đăng nhập: Hiển thị popup cảnh báo
    showAlert('Vui lòng đăng nhập để thực hiện thủ tục mua kiệt tác nhanh!', 'warning');
    
    // --- DÒNG MẶC ĐỊNH: Tạm dừng 1.5 giây (1500ms) để đọc thông báo rồi mới chuyển trang đăng nhập ---
    setTimeout(() => {
      router.push('/dang-nhap');
    }, 1500);
    // THAY THẾ: Chuyển trang đăng nhập ngay lập tức (không chờ):
    // router.push('/dang-nhap');
    
    return;
  }

  // Nếu đã đăng nhập: Chuyển thẳng sang trang /checkout kèm query tham số buyNowId và qty
  router.push({
    path: '/checkout',
    query: { 
      buyNowId: props.maSanPham, 
      qty: props.soLuong 
    }
  });
};
</script>

<style scoped>
/* =========================================================================
   [CSS PHẦN 1: KHUNG BAO NGOÀI NÚT BẤM (CONTAINER)]
========================================================================= */
.thanh-toan-nhanh-container {
  /* DÒNG MẶC ĐỊNH: Nút nằm vừa khít trong khung cha */
  width: 100%;
  height: 100%;

  /* THAY THẾ: Chuyển nút sang CỐ ĐỊNH Ở GÓC DƯỚI BÊN TRÁI MÀN HÌNH:
  position: fixed; bottom: 30px; left: 30px; width: auto; height: auto; z-index: 999; */
  
  /* THAY THẾ: Chuyển nút sang CỐ ĐỊNH Ở GÓC DƯỚI BÊN PHẢI MÀN HÌNH:
  position: fixed; bottom: 30px; right: 30px; width: auto; height: auto; z-index: 999; */
}

/* =========================================================================
   [CSS PHẦN 2: KIỂU DÁNG NÚT THANH TOÁN (BUTTON STYLES)]
========================================================================= */
.btn-primary {
  width: 100%;                                  /* Chiều rộng chiếm trọn khung */
  height: 100%;                                 /* Chiều cao chiếm trọn khung */
  padding: 18px 24px;                           /* Khoảng cách đệm: trên-dưới 18px, trái-phải 24px */
  
  /* DÒNG MẶC ĐỊNH: Màu nền ĐEN SANG TRỌNG */
  background-color: #000000;
  color: #ffffff;                               /* Màu chữ: TRẮNG TINH */
  border: 1px solid #000000;                    /* Viền ĐEN */
  
  /* THAY THẾ: Đổi nút sang MÀU VÀNG HOÀNG KIM (Nổi bật):
  background-color: #d1aa68; border-color: #d1aa68; color: #ffffff; */

  /* THAY THẾ: Đổi nút sang MÀU ĐỎ CẢNH BÁO / SALE:
  background-color: #dc2626; border-color: #dc2626; color: #ffffff; */

  /* THAY THẾ: Đổi nút sang MÀU XANH LÁ CÂY (Thanh toán an toàn):
  background-color: #16a34a; border-color: #16a34a; color: #ffffff; */

  /* THAY THẾ: Nút nền trắng viền vàng chữ vàng:
  background-color: #ffffff; border: 2px solid #d1aa68; color: #d1aa68; */

  font-size: 13px;                              /* Cỡ chữ 13px */
  font-weight: 700;                             /* Chữ in đậm */
  letter-spacing: 2px;                          /* Khoảng cách giữa các chữ cái là 2px */
  cursor: pointer;                              /* Chuột biến thành hình bàn tay */
  transition: all 0.3s ease;                    /* Hiệu ứng chuyển động mượt mà trong 0.3s */
  text-transform: uppercase;                    /* Tự động viết hoa toàn bộ chữ */

  /* THAY THẾ: Bo tròn nút thành HÌNH VIÊN THUỐC:
  border-radius: 50px; */

  /* THAY THẾ: Bo góc nhẹ hiện đại:
  border-radius: 8px; */
}

/* =========================================================================
   [CSS PHẦN 3: HIỆU ỨNG KHI RÊ CHUỘT VÀO NÚT (HOVER EFFECT)]
========================================================================= */
/* DÒNG MẶC ĐỊNH: Khi rê chuột vào chuyển sang MÀU VÀNG HOÀNG KIM */
.btn-primary:hover {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #ffffff;
}

/* THAY THẾ: Khi rê chuột vào chuyển sang MÀU ĐEN:
.btn-primary:hover { background-color: #1a1a1a; border-color: #1a1a1a; color: #d1aa68; } */

/* THAY THẾ: Khi rê chuột vào nút phóng to nhẹ:
.btn-primary:hover { transform: scale(1.03); box-shadow: 0 8px 20px rgba(209, 170, 104, 0.4); } */
</style>