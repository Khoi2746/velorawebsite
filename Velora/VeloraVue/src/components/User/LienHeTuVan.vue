<template>
    <div class="profile-page">
        <Header />

        <main class="profile-content">
            <div class="container">
                <div class="title-wrapper">
                    <h1 class="page-title">ĐĂNG KÝ TƯ VẤN</h1>
                    <div class="title-divider">
                        <span class="diamond"></span>
                    </div>
                </div>

                <div class="profile-layout">
                    <!-- Cột Hướng dẫn bên trái -->
                    <div class="profile-sidebar">
                        <div class="user-avatar">
                            <i class="fas fa-calendar-alt"></i>
                            <h3>Đặt Lịch Hẹn</h3>
                            <p class="role-tag">Velora Services</p>
                        </div>
                        <div class="booking-notes">
                            <p><strong>Lưu ý:</strong></p>
                            <ul>
                                <li>Vui lòng đăng ký trước ít nhất 1 ngày.</li>
                                <li>Đội ngũ chuyên viên sẽ liên hệ xác nhận qua Số điện thoại / Email trong vòng 2 giờ làm việc.</li>
                            </ul>
                        </div>
                    </div>

                    <!-- Cột Form Đăng Ký bên phải -->
                    <div class="profile-form-section">
                        <h2 class="section-title">THÔNG TIN ĐẶT LỊCH</h2>
                        <p class="section-desc">Hãy để lại thông tin, Velora sẽ chuẩn bị không gian chu đáo nhất dành cho bạn.</p>

                        <form @submit.prevent="bookAppointment" class="velora-form">
                            <!-- Họ và tên -->
                            <div class="form-group">
                                <label>Họ và tên <span class="required">*</span></label>
                                <input type="text" v-model="appointment.tenKhachHang" placeholder="Nhập họ và tên của bạn..." required />
                            </div>

                            <div class="form-row">
                                <!-- Số điện thoại -->
                                <div class="form-group flex-1">
                                    <label>Số điện thoại <span class="required">*</span></label>
                                    <input type="tel" v-model="appointment.soDienThoai" placeholder="Nhập số điện thoại..." pattern="[0-9]{10,11}" title="Số điện thoại phải chứa 10-11 chữ số" required />
                                </div>

                                <!-- Email -->
                                <div class="form-group flex-1">
                                    <label>Email <span class="required">*</span></label>
                                    <input type="email" v-model="appointment.email" placeholder="Nhập địa chỉ email..." required />
                                </div>
                            </div>

                            <div class="form-row">
                                <!-- Ngày hẹn -->
                                <div class="form-group flex-1">
                                    <label>Ngày hẹn tư vấn <span class="required">*</span></label>
                                    <input type="date" v-model="appointment.ngayHen" :min="minDate" required />
                                </div>

                                <!-- Khung giờ -->
                                <div class="form-group flex-1">
                                    <label>Khung giờ <span class="required">*</span></label>
                                    <select v-model="appointment.thoiGian" required>
                                        <option value="" disabled selected>Chọn khung giờ...</option>
                                        <option value="09:00:00">09:00 - 10:00</option>
                                        <option value="10:30:00">10:30 - 11:30</option>
                                        <option value="14:00:00">14:00 - 15:00</option>
                                        <option value="15:30:00">15:30 - 16:30</option>
                                        <option value="17:00:00">17:00 - 18:00</option>
                                        <option value="19:30:00">19:30 - 20:30</option>
                                    </select>
                                </div>
                            </div>

                            <!-- Sản phẩm cần tư vấn -->
                            <div class="form-group">
                                <label>Sản phẩm bạn quan tâm</label>
                                <select v-model="appointment.idSanPham">
                                    <option :value="null">-- Chọn sản phẩm cụ thể (Tùy chọn) --</option>
                                    <option v-for="sp in danhSachSanPham" :key="sp.maSanPham" :value="sp.maSanPham">
                                        {{ sp.tenSanPham }}
                                    </option>
                                </select>
                            </div>

                            <!-- Ghi chú -->
                            <div class="form-group">
                                <label>Yêu cầu đặc biệt / Ghi chú</label>
                                <textarea v-model="appointment.ghiChu" rows="4" placeholder="Ví dụ: Cần tư vấn quà tặng, yêu cầu không gian riêng tư..."></textarea>
                            </div>

                            <!-- Khu vực nút bấm -->
                            <div class="form-actions">
                                <button type="submit" class="btn-primary" :disabled="isSubmitting">
                                    {{ isSubmitting ? 'ĐANG GỬI YÊU CẦU...' : 'ĐĂNG KÝ HẸN TƯ VẤN' }}
                                </button>
                            </div>

                            <!-- Khu vực hiển thị thông báo phản hồi -->
                            <p v-if="successMsg" class="msg success"><i class="fas fa-check-circle"></i> {{ successMsg }}</p>
                            <p v-if="errorMsg" class="msg error"><i class="fas fa-exclamation-circle"></i> {{ errorMsg }}</p>
                        </form>
                    </div>
                </div>
            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '../Header.vue' 
import Footer from '../Footer.vue'

const appointment = ref({
    tenKhachHang: '',
    soDienThoai: '',
    email: '',
    ngayHen: '',
    thoiGian: '',
    idSanPham: null,
    ghiChu: '',
    trangThai: 0 
})

const danhSachSanPham = ref([]) 
const isSubmitting = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const minDate = ref('')

onMounted(() => {
    const today = new Date()
    minDate.value = today.toISOString().split('T')[0]

    const userStr = localStorage.getItem('user')
    if (userStr) {
        const user = JSON.parse(userStr)
        appointment.value.tenKhachHang = user.hoTen || user.tenKhachHang || ''
        appointment.value.soDienThoai = user.soDienThoai || ''
        appointment.value.email = user.email || ''
    }
    fetchProducts()
})

const fetchProducts = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/san-pham') 
        if (res.ok) danhSachSanPham.value = await res.json()
    } catch (error) {
        console.error('Không thể tải danh sách sản phẩm:', error)
    }
}

const bookAppointment = async () => {
    isSubmitting.value = true
    successMsg.value = ''
    errorMsg.value = ''

    const payload = {
        tenKhachHang: appointment.value.tenKhachHang,
        soDienThoai: appointment.value.soDienThoai,
        email: appointment.value.email,
        ngayHen: appointment.value.ngayHen,
        thoiGian: appointment.value.thoiGian,
        ghiChu: appointment.value.ghiChu || '',
        trangThai: appointment.value.trangThai,
        idSanPham: appointment.value.idSanPham ? Number(appointment.value.idSanPham) : null
    }

    try {
        const res = await fetch('http://localhost:8080/api/lich-hen/dat-lich', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })

        if (res.ok) {
            const rawText = await res.text();
            
            try {
                const data = JSON.parse(rawText);
                const appointmentId = data.id || (data.data && data.data.id) || null;

                if (appointmentId !== null && appointmentId !== undefined) {
                    successMsg.value = `Đặt lịch thành công! Mã lịch hẹn của bạn là: #` + appointmentId;
                    resetForm();
                } else {
                    successMsg.value = `Đặt lịch thành công! Yêu cầu của bạn đang được hệ thống xử lý.`;
                    resetForm();
                }
            } catch (parseError) {
                successMsg.value = `Đặt lịch thành công!`;
                resetForm();
            }
        } else {
            const errText = await res.text()
            errorMsg.value = errText.includes("Lỗi") || errText.includes("không") ? errText : `Đăng ký thất bại. Lỗi máy chủ (${res.status}).`;
        }
    } catch (error) {
        errorMsg.value = 'Không thể kết nối tới server Spring Boot. Vui lòng thử lại!'
    } finally {
        isSubmitting.value = false
    }
}

const resetForm = () => {
    appointment.value.ngayHen = ''
    appointment.value.thoiGian = ''
    appointment.value.idSanPham = null
    appointment.value.ghiChu = ''
}
</script>

<style scoped>
.profile-page { width: 100%; min-height: 100vh; display: flex; flex-direction: column; background-color: #fdfdfd; }
.profile-content { flex: 1; padding: 60px 20px 100px; }
.container { max-width: 1200px; margin: 0 auto; }
.title-wrapper { text-align: center; margin-bottom: 60px; }
.page-title { color: #24201D; font-size: 26px; font-weight: 400; letter-spacing: 4px; margin-bottom: 15px; }
.title-divider { display: flex; align-items: center; justify-content: center; }
.title-divider::before, .title-divider::after { content: ''; display: block; width: 60px; height: 1px; background-color: #d1aa68; }
.diamond { width: 6px; height: 6px; background-color: #d1aa68; transform: rotate(45deg); margin: 0 15px; }
.profile-layout { display: flex; gap: 50px; align-items: flex-start; }
.profile-sidebar { flex: 1; background-color: #ffffff; border: 1px solid #eeeeee; padding: 40px 25px; }
.user-avatar { text-align: center; }
.user-avatar i { font-size: 50px; color: #d1aa68; margin-bottom: 15px; }
.user-avatar h3 { font-size: 18px; color: #24201D; margin-bottom: 5px; font-weight: 600; }
.role-tag { font-size: 11px; color: #888888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 25px; }
.booking-notes { font-size: 13px; color: #555555; line-height: 1.6; border-top: 1px dashed #e0e0e0; padding-top: 20px; }
.booking-notes ul { padding-left: 18px; margin-top: 8px; }
.booking-notes li { margin-bottom: 8px; color: #666; }
.profile-form-section { flex: 3; background-color: #ffffff; border: 1px solid #eeeeee; padding: 50px; }
.section-title { font-size: 18px; color: #24201D; margin-bottom: 10px; font-weight: 600; letter-spacing: 1px; }
.section-desc { font-size: 13px; color: #888888; margin-bottom: 35px; }
.velora-form .form-group { margin-bottom: 25px; }
.form-row { display: flex; gap: 20px; }
.flex-1 { flex: 1; }
.required { color: #ff4444; }
.velora-form label { display: block; font-size: 12px; color: #555555; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 10px; }
.velora-form input, .velora-form select, .velora-form textarea { width: 100%; border: 1px solid #e0e0e0; padding: 15px; font-size: 14px; color: #24201D; outline: none; transition: border-color 0.3s ease; background-color: #fff; border-radius: 0; font-family: inherit; }
.velora-form input:focus, .velora-form select:focus, .velora-form textarea:focus { border-color: #d1aa68; }
.form-actions { margin-top: 40px; }
.btn-primary { background-color: #24201D; color: #ffffff; border: none; padding: 15px 40px; font-size: 12px; font-weight: 600; letter-spacing: 2px; cursor: pointer; transition: all 0.3s ease; width: 100%; }
.btn-primary:hover { background-color: #d1aa68; }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }
.msg { margin-top: 20px; font-size: 13px; font-weight: 500; }
.msg.success { color: #2b7a0b; }
.msg.error { color: #ff4444; }

@media (max-width: 768px) {
    .profile-layout { flex-direction: column; }
    .form-row { flex-direction: column; gap: 0; }
    .profile-sidebar, .profile-form-section { width: 100%; }
}
</style>