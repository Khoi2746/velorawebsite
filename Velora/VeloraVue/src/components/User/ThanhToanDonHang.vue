<template>
  <div class="checkout-page-wrapper">
    <Header />

    <main class="checkout-main-content" v-if="!loading && product">
      <div class="section-header">
        <h2>THỦ TỤC <span class="gold">THANH TOÁN</span></h2>
        <div class="header-divider"><span class="diamond"></span></div>
      </div>

      <div class="container checkout-container">
        <div class="checkout-grid">
          
          <div class="checkout-left-section">
            <div class="product-preview-card">
              <div class="image-box">
                <img 
                  :src="product.anhDaiDien && product.anhDaiDien.startsWith('http') ? product.anhDaiDien : '/img/' + (product.anhDaiDien || '')" 
                  :alt="product.tenSanPham" 
                  class="preview-img"
                />
              </div>
              <div class="product-summary">
                <h3 class="product-title">{{ product.tenSanPham }}</h3>
                <p class="product-qty">Số lượng đặt: <strong>{{ quantity }}</strong></p>
                <p class="product-total-price">Tổng thanh toán: <span class="gold">{{ formatPrice(product.giaBan * quantity) }}</span></p>
              </div>
            </div>
          </div>

          <div class="checkout-right-section">
            <div class="payment-methods-group">
              <label class="section-label">CHỌN PHƯƠNG THỨC THANH TOÁN</label>
              <div class="methods-row">
                <button 
                  type="button" 
                  class="btn-method" 
                  :class="{ active: hinhThucThanhToan === 'CHUYEN_KHOAN_QR' }"
                  @click="hinhThucThanhToan = 'CHUYEN_KHOAN_QR'"
                >
                  <i class="fa-solid fa-qrcode"></i> THANH TOÁN NGAY
                </button>
                <button 
                  type="button" 
                  class="btn-method" 
                  :class="{ active: hinhThucThanhToan === 'COD' }"
                  @click="hinhThucThanhToan = 'COD'"
                >
                  <i class="fa-solid fa-truck-ramped"></i> NHẬN HÀNG THANH TOÁN
                </button>
              </div>
            </div>

            <div class="shipping-form-group">
              <label class="section-label">THÔNG TIN GIAO NHẬN HÀNG</label>
              <form @submit.prevent="xacNhanDatHang" class="velora-form">
                
                <div class="form-field">
                  <label for="fullName">HỌ VÀ TÊN NGƯỜI NHẬN *</label>
                  <input type="text" id="fullName" v-model="formOrder.hoTen" required placeholder="Nhập đầy đủ họ và tên..." />
                </div>

                <div class="form-field">
                  <label for="phoneNumber">SỐ ĐIỆN THOẠI *</label>
                  <input type="tel" id="phoneNumber" v-model="formOrder.soDienThoai" required placeholder="Nhập số điện thoại di động..." />
                </div>

                <div class="form-field">
                  <label for="emailAddress">ĐỊA CHỈ GMAIL *</label>
                  <input type="email" id="emailAddress" v-model="formOrder.email" required placeholder="vi-du@gmail.com..." />
                </div>

                <div class="form-field">
                  <label>TỈNH / THÀNH PHỐ *</label>
                  <select v-model="selectedProvince" @change="onProvinceChange" required class="select-address">
                    <option value="">-- Chọn Tỉnh / Thành Phố --</option>
                    <option v-for="p in provinces" :key="p.code" :value="p">{{ p.name }}</option>
                  </select>
                </div>

                <div class="form-field">
                  <label>QUẬN / HUYỆN *</label>
                  <select v-model="selectedDistrict" @change="onDistrictChange" :disabled="!selectedProvince" required class="select-address">
                    <option value="">-- Chọn Quận / Huyện --</option>
                    <option v-for="d in districts" :key="d.code" :value="d">{{ d.name }}</option>
                  </select>
                </div>

                <div class="form-field">
                  <label>PHƯỜNG / XÃ *</label>
                  <select v-model="selectedWard" :disabled="!selectedDistrict" required class="select-address">
                    <option value="">-- Chọn Phường / Xã --</option>
                    <option v-for="w in wards" :key="w.code" :value="w">{{ w.name }}</option>
                  </select>
                </div>

                <div class="form-field">
                  <label for="specificAddress">ĐỊA CHỈ CỤ THỂ (SỐ NHÀ, TÊN ĐƯỜNG...) *</label>
                  <input type="text" id="specificAddress" v-model="specificAddress" required placeholder="Ví dụ: Số 12, Ngõ 45, Đường Lê Lợi..." />
                </div>

                <div class="form-field text-area-field">
                  <label for="orderNote">GHI CHÚ ĐƠN HÀNG (TÙY CHỌN)</label>
                  <textarea 
                    id="orderNote" 
                    v-model="orderNote" 
                    rows="5" 
                    placeholder="Nhập lời nhắn cho shipper, thời gian nhận hàng mong muốn..."
                  ></textarea>
                </div>

                <button type="submit" class="btn-submit-order">
                  XÁC NHẬN ĐẶT HÀNG ĐỘC QUYỀN
                </button>
              </form>
            </div>

          </div>
        </div>
      </div>
    </main>

    <main class="checkout-main-content loading-state" v-else-if="loading">
      <div class="loader"></div>
      <p>Đang lập cấu trúc đơn hàng bảo mật...</p>
    </main>

    <main class="checkout-main-content error-state" v-else>
      <div class="container text-center" style="padding: 60px 0; text-align: center;">
        <h3 style="color: #3e332e; margin-bottom: 15px;">Không tìm thấy thông tin sản phẩm</h3>
        <p style="color: #666; margin-bottom: 20px;">Vui lòng quay lại danh sách sản phẩm để chọn món đồ bạn yêu thích.</p>
        <button class="btn-submit-order" style="max-width: 250px; margin: 0 auto;" @click="router.push('/dong-ho-co-san')">QUAY LẠI CỬA HÀNG</button>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Header from '../../components/Header.vue';
import Footer from '../../components/Footer.vue';

const route = useRoute();
const router = useRouter();

const product = ref(null);
const quantity = ref(1);
const loading = ref(true);
const hinhThucThanhToan = ref('CHUYEN_KHOAN_QR'); 

const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const selectedProvince = ref('');
const selectedDistrict = ref('');
const selectedWard = ref('');
const specificAddress = ref('');
const orderNote = ref('');

const formOrder = ref({
  hoTen: '',
  soDienThoai: '',
  email: ''
});

const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const fetchProvinces = async () => {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/');
    if (res.ok) provinces.value = await res.json();
  } catch (err) {
    console.error('Lỗi tải danh sách Tỉnh/Thành:', err);
  }
};

const onProvinceChange = async () => {
  districts.value = []; wards.value = []; selectedDistrict.value = ''; selectedWard.value = '';
  if (!selectedProvince.value) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${selectedProvince.value.code}?depth=2`);
    if (res.ok) {
      const data = await res.json();
      districts.value = data.districts || [];
    }
  } catch (err) {
    console.error(err);
  }
};

const onDistrictChange = async () => {
  wards.value = []; selectedWard.value = '';
  if (!selectedDistrict.value) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${selectedDistrict.value.code}?depth=2`);
    if (res.ok) {
      const data = await res.json();
      wards.value = data.wards || [];
    }
  } catch (err) {
    console.error(err);
  }
};

const khoiTaoDonHang = async () => {
  loading.value = true;
  const productId = route.query.buyNowId;
  const qtyParam = route.query.qty;

  if (!productId) {
    alert('Không tìm thấy thông tin kiệt tác cần thanh toán!');
    router.push('/dong-ho-co-san');
    return;
  }

  quantity.value = qtyParam ? parseInt(qtyParam) : 1;

  try {
    const res = await fetch(`http://localhost:8080/api/san-pham/${productId}`);
    if (res.ok) {
      product.value = await res.json();
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          formOrder.value.hoTen = user.hoTen || '';
          formOrder.value.email = user.email || '';
          formOrder.value.soDienThoai = user.soDienThoai || '';
        } catch(e) { console.error(e); }
      }
    }
  } catch (error) {
    console.error("Lỗi lấy sản phẩm:", error);
  } finally {
    loading.value = false;
  }
};

const xacNhanDatHang = async () => {
  if (!selectedProvince.value || !selectedDistrict.value || !selectedWard.value) {
    alert('Vui lòng chọn đầy đủ thông tin địa chỉ!');
    return;
  }

  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : { maNguoiDung: 3 };

  const diaChiHoanChinh = `${specificAddress.value.trim()}, ${selectedWard.value.name}, ${selectedDistrict.value.name}, ${selectedProvince.value.name}`;
  const tongTienDonHang = product.value.giaBan * quantity.value;
  const maCodeNgauNhien = 'VELORA-' + Date.now();

  const payload = {
    maNguoiDung: user.maNguoiDung || 3,
    maDonHangCode: maCodeNgauNhien, 
    tenNguoiNhan: formOrder.value.hoTen,
    soDienThoaiGiaoHang: formOrder.value.soDienThoai,
    diaChiGiaoHang: diaChiHoanChinh,
    phuongThucThanhToan: hinhThucThanhToan.value,
    maSanPham: product.value.maSanPham,
    soLuong: quantity.value,
    tongTien: tongTienDonHang, 
    ghiChuDonHang: orderNote.value.trim() 
  };

  try {
    const res = await fetch('http://localhost:8080/api/don-hang/dat-ngay', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      if (hinhThucThanhToan.value === 'CHUYEN_KHOAN_QR') {
        router.push({
          path: '/scan-qr',
          query: {
            code: maCodeNgauNhien,
            amount: tongTienDonHang,
            buyer: formOrder.value.hoTen,
            phone: formOrder.value.soDienThoai
          }
        });
      } else {
        alert('Đặt hàng thành công! Hệ thống Velora đang xử lý đơn COD.');
        router.push('/don-hang'); 
      }
    } else {
      alert('Đặt hàng thất bại: ' + await res.text());
    }
  } catch (error) {
    alert('Không thể kết nối đến máy chủ.');
  }
};

onMounted(() => {
  khoiTaoDonHang();
  fetchProvinces(); 
});
</script>

<style scoped>
.checkout-page-wrapper { background: #f4f1ea; min-height: 100vh; font-family: sans-serif; }
.checkout-main-content { padding: 40px 0 80px 0; }
.section-header { text-align: center; margin-bottom: 40px; }
.section-header h2 { color: #3e332e; font-size: 28px; letter-spacing: 2px; }
.gold { color: #d1aa68; }
.header-divider { display: flex; justify-content: center; align-items: center; margin-top: 10px; }
.header-divider::before, .header-divider::after { content: ""; width: 50px; height: 1px; background: #d1aa68; }
.diamond { width: 6px; height: 6px; background: #d1aa68; transform: rotate(45deg); margin: 0 10px; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 15px; }
.checkout-grid { display: flex; gap: 40px; align-items: flex-start; }
.checkout-left-section { flex: 1; background: #fff; border: 1px solid #e0dcd5; padding: 30px; border-radius: 8px; }
.checkout-right-section { flex: 1; background: #fff; border: 1px solid #e0dcd5; padding: 30px; border-radius: 8px; }
.preview-img { width: 100%; height: auto; display: block; object-fit: cover; }
.product-summary { margin-top: 20px; border-top: 1px solid #f0efeb; padding-top: 15px; }
.product-title { font-size: 20px; color: #3e332e; margin-bottom: 10px; }
.product-qty { font-size: 14px; color: #666; }
.product-total-price { font-size: 18px; font-weight: bold; margin-top: 10px; }
.section-label { font-size: 13px; font-weight: bold; color: #3e332e; letter-spacing: 1px; display: block; margin-bottom: 15px; border-left: 3px solid #d1aa68; padding-left: 8px; }
.payment-methods-group { margin-bottom: 30px; }
.methods-row { display: flex; gap: 15px; }
.btn-method { flex: 1; padding: 15px; border: 1px solid #e0dcd5; background: #faf9f6; color: #3e332e; font-weight: bold; font-size: 13px; cursor: pointer; transition: 0.3s; border-radius: 4px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.btn-method:hover { border-color: #d1aa68; color: #d1aa68; }
.btn-method.active { background: #3e332e; color: #d1aa68; border-color: #3e332e; }
.velora-form { display: flex; flex-direction: column; gap: 15px; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 11px; font-weight: bold; color: #888; letter-spacing: 1px; }
.form-field input, .select-address { padding: 12px; border: 1px solid #e0dcd5; background-color: #faf9f6; border-radius: 4px; outline: none; transition: 0.3s; font-size: 14px; }
.form-field input:focus, .select-address:focus { border-color: #d1aa68; background-color: #fff; }
.select-address { cursor: pointer; color: #3e332e; }
.select-address:disabled { cursor: not-allowed; background-color: #e0dcd5; color: #888; }
.text-area-field textarea { width: 100%; padding: 12px; border: 1px solid #e0dcd5; background-color: #faf9f6; border-radius: 4px; outline: none; font-size: 14px; resize: vertical; font-family: inherit; transition: 0.3s; }
.text-area-field textarea:focus { border-color: #d1aa68; background-color: #fff; }
.btn-submit-order { width: 100%; padding: 16px; background-color: #3e332e; color: #fff; border: 1px solid #3e332e; font-weight: bold; letter-spacing: 2px; font-size: 14px; cursor: pointer; margin-top: 15px; transition: 0.3s; text-transform: uppercase; }
.btn-submit-order:hover { background-color: #d1aa68; border-color: #d1aa68; }
.loading-state { text-align: center; padding: 100px 0; color: #888; }
.loader { border: 4px solid #f3f3f3; border-top: 4px solid #d1aa68; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px auto; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>