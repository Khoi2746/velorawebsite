<template>
  <div class="bao-hanh-page">
    <Header />

    <main class="bao-hanh-main">
      <section class="hero-banner">
        <div>
          <h1>BẢO HÀNH</h1>
          <p>
            Velora cam kết mang đến dịch vụ bảo hành chuyên nghiệp, nhanh chóng và minh bạch cho từng sản phẩm.
            Quý khách chỉ cần điền thông tin đơn giản, đội ngũ hỗ trợ của chúng tôi sẽ xác nhận tiến độ xử lý trong thời
            gian sớm nhất.
          </p>
        </div>

        <div class="hero-badge">
          <div class="badge-icon">🛡️</div>
          <strong>CHÍNH SÁCH 5 NĂM</strong>
          <p>Hỗ trợ kỹ thuật, kiểm tra sản phẩm và thay thế linh kiện chính hãng.</p>
        </div>
      </section>

      <section class="section-card">
        <h2 class="section-title">Thông tin dịch vụ</h2>
        <p class="section-subtitle">Hệ thống bảo hành Velora dành cho đồng hồ cơ, đồng hồ điện tử và sản phẩm cao cấp.
        </p>

        <div class="info-grid">
          <div class="info-item">
            <h3>1. Kiểm tra tình trạng</h3>
            <p>Nhân viên chuyên môn sẽ xác định nguyên nhân hư hỏng và tình trạng bảo hành của sản phẩm.</p>
          </div>
          <div class="info-item">
            <h3>2. Hướng dẫn xử lý</h3>
            <p>Khách hàng được tư vấn rõ ràng về phương án sửa chữa, thay mới hoặc bảo dưỡng kỹ thuật.</p>
          </div>
          <div class="info-item">
            <h3>3. Bảo hành tối ưu</h3>
            <p>Quy trình rõ ràng, thời gian phản hồi nhanh và linh kiện chính hãng.</p>
          </div>
        </div>
      </section>

      <section class="section-card">
        <h2 class="section-title">Quy trình bảo hành</h2>
        <div class="step-grid">
          <div class="step-item">
            <div class="step-number">1</div>
            <h3>Gửi yêu cầu</h3>
            <p>Khách hàng điền thông tin mã đơn hàng, mô tả lỗi và hình ảnh sản phẩm.</p>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <h3>Nhận phản hồi</h3>
            <p>Velora xác nhận, báo về thời gian tiếp nhận và yêu cầu bổ sung nếu cần.</p>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <h3>Kiểm tra kỹ thuật</h3>
            <p>Đội ngũ kỹ thuật tiến hành kiểm tra, đánh giá tình trạng sản phẩm.</p>
          </div>
          <div class="step-item">
            <div class="step-number">4</div>
            <h3>Hoàn tất</h3>
            <p>Sản phẩm được bảo hành, sửa chữa hoặc gửi trả kèm báo cáo rõ ràng.</p>
          </div>
        </div>
      </section>

      <section class="section-card">
        <h2 class="section-title">Chính sách bảo hành</h2>
        <div class="policy-grid">
          <div class="policy-item">
            <h3>Phạm vi áp dụng</h3>
            <p>Áp dụng cho các sản phẩm từ hệ thống Velora với tem bảo hành và hóa đơn mua hàng hợp lệ.</p>
          </div>
          <div class="policy-item">
            <h3>Không thuộc diện bảo hành</h3>
            <p>Hỏng hóc do va đập, rơi rớt, nước, thay đổi cơ cấu, hoặc không bảo quản đúng cách.</p>
          </div>
        </div>
      </section>

      <section class="section-card">
        <h2 class="section-title">Yêu cầu hỗ trợ bảo hành</h2>
        <div class="contact-grid">
          <div class="contact-card">
            <strong>Velora Care Center</strong>
            <p>Hotline: 1800 1234</p>
            <p>Email: support@velora.vn</p>
            <p>Địa chỉ: 123 Lê Lợi, Quận 1, TP. Hồ Chí Minh</p>
          </div>

          <div class="contact-card">
            <form class="bao-hanh-form" @submit.prevent="submitForm">
              <div class="form-row">
                <input v-model="form.hoTen" type="text" placeholder="Họ và tên" required />
                <input v-model="form.sdt" type="tel" placeholder="Số điện thoại" required />
              </div>

              <div class="form-row">
                <input v-model="form.maDonHang" type="text" placeholder="Mã đơn hàng" required />
                <select v-model="form.loaiSanPham" required>
                  <option value="">Chọn loại sản phẩm</option>
                  <option>Đồng hồ nam</option>
                  <option>Đồng hồ nữ</option>
                  <option>Đồng hồ cao cấp</option>
                </select>
              </div>

              <textarea v-model="form.moTa" rows="5" placeholder="Mô tả lỗi sản phẩm hoặc nguyên nhân cần bảo hành"
                required></textarea>

              <button class="btn-submit" type="submit">Gửi yêu cầu bảo hành</button>

              <div v-if="message" :class="['alert', message.type]">
                {{ message.text }}
              </div>
            </form>

            <div v-if="requestList.length > 0" class="request-history">
              <h3>Yêu cầu đã gửi</h3>

              <div v-for="item in requestList" :key="item.maBaoHanh" class="request-history-item">
                <div class="request-history-top">
                  <strong>{{ item.hoTen }}</strong>

                  <span :class="item.trangThai">
  {{ getStatusText(item.trangThai) }}
</span>
                </div>

                <p>
                  Mã đơn:
                  {{ item.maDonHangCode }}
                  •
                  {{ item.loaiSanPham }}
                </p>

                <p>{{ item.moTaLoi }}</p>

                <small>
                  {{ new Date(item.ngayGui).toLocaleString('vi-VN') }}
                </small>

              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

const API = "http://localhost:8080/api/bao-hanh"

const form = reactive({
  hoTen: '',
  sdt: '',
  maDonHang: '',
  loaiSanPham: '',
  moTa: ''
})

const message = ref(null)
const requestList = ref([])

let timer = null

// ===========================
// Lấy lịch sử bảo hành
// ===========================
const fetchWarrantyRequests = async () => {

  try {

    const user = JSON.parse(

localStorage.getItem("user")

)

const res = await axios.get(

`http://localhost:8080/api/bao-hanh/my-history/${user.maNguoiDung}`

)

requestList.value = res.data

  } catch (err) {

    console.error(err)

  }

}

// ===========================
// Gửi yêu cầu
// ===========================
const submitForm = async () => {

  try {

const res = await axios.post(
    "http://localhost:8080/api/bao-hanh/send",
    {
        maNguoiDung: user.maNguoiDung,
        hoTen: form.hoTen,
        sdt: form.sdt,
        maDonHangCode: form.maDonHang,
        loaiSanPham: form.loaiSanPham,
        moTaLoi: form.moTa
    }
)

    message.value = {
      type: "success",
      text: res.data.message
    }

    form.hoTen = ""
    form.sdt = ""
    form.maDonHang = ""
    form.loaiSanPham = ""
    form.moTa = ""

    await fetchWarrantyRequests()

  } catch (err) {

    console.log(err)

    message.value = {
      type: "error",
      text:
        err.response?.data?.message ||
        "Không thể gửi yêu cầu."
    }

  }

}

// ===========================
// Hiển thị trạng thái
// ===========================
const getStatusText = (status) => {

  switch (status) {

    case "CHO_XU_LY":
      return "🟡 Chờ xử lý"

    case "DA_TIEP_NHAN":
      return "🔵 Đã tiếp nhận"

    case "DANG_XU_LY":
      return "🟠 Đang xử lý"

    case "HOAN_TAT":
      return "🟢 Hoàn tất"

    case "TU_CHOI":
      return "🔴 Từ chối"

    default:
      return status

  }

}

// ===========================
// Load khi mở trang
// ===========================
onMounted(() => {

  fetchWarrantyRequests()

  // Tự động cập nhật mỗi 5 giây
  timer = setInterval(() => {

    fetchWarrantyRequests()

  }, 5000)

})
const user = JSON.parse(localStorage.getItem("user"))

// ===========================
// Hủy timer
// ===========================
onUnmounted(() => {

  clearInterval(timer)

})
</script>

<style scoped>
@import '../CSS/User/BaoHanh.css';
</style>
