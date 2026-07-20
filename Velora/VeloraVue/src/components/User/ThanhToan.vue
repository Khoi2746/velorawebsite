<template>
  <div class="thanh-toan-nhanh-container">
    <!-- Đã đồng bộ class name và cấu trúc giống nút gốc của bạn -->
    <button class="btn-primary" @click="xuLyThanhToanNhanh">
      THANH TOÁN NGAY
    </button>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  maSanPham: {
    type: Number,
    required: true
  },
  soLuong: {
    type: Number,
    default: 1
  }
});

const router = useRouter();

const xuLyThanhToanNhanh = () => {
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    alert('Vui lòng đăng nhập để thực hiện thủ tục mua kiệt tác nhanh!');
    router.push('/dang-nhap');
    return;
  }

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
.thanh-toan-nhanh-container {
  width: 100%;
  height: 100%;
}

/* Ép kiểu dáng, chiều cao và hiệu ứng hover khít hoàn toàn với nút THÊM VÀO GIỎ HÀNG của bạn */
.btn-primary {
  width: 100%;
  height: 100%;
  padding: 18px 24px;
  background-color: #000000; /* Màu nâu tối sang trọng giống nút giỏ hàng gốc */
  color: #ffffff;
  border: 1px solid #000000;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
}

/* Hiệu ứng đổi màu vàng kim hoàng gia khi hover giống hệt nút giỏ hàng của Velora */
.btn-primary:hover {
  background-color: #d1aa68;
  border-color: #d1aa68;
  color: #ffffff;
}
</style>