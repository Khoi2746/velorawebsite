<template>
  <div class="checkout-page-wrapper">
    <!-- =========================================================================
         [PHẦN 1: HEADER & TIÊU ĐỀ TRANG THANH TOÁN]
    ========================================================================== -->
    <!-- Gọi component Header dùng chung (chứa logo Velora, menu điều hướng và icon giỏ hàng) -->
    <Header />

    <!-- 
      Khung thẻ <main> chứa toàn bộ nội dung xử lý đơn hàng.
      Điều kiện v-if="!loading && (product || cartList.length > 0)":
      1. '!loading': Khi trạng thái đang tải đã kết thúc (dữ liệu từ server đã sẵn sàng).
      2. 'product': Có dữ liệu của 1 sản phẩm khi khách bấm "Mua Ngay".
      3. 'cartList.length > 0': Có ít nhất 1 sản phẩm trong mảng giỏ hàng khi khách bấm "Thanh Toán Giỏ Hàng".
    -->
    <main class="checkout-main-content" v-if="!loading && (product || cartList.length > 0)">
      
      <!-- Khung tiêu đề chính của trang thanh toán -->
      <div class="section-header">
        <!-- DÒNG MẶC ĐỊNH: Chữ 'THANH TOÁN' mang màu vàng hoàng kim (class gold) -->
        <h2>THỦ TỤC <span class="gold">THANH TOÁN</span></h2>
        <!-- THAY THẾ: Đổi chữ 'THANH TOÁN' sang MÀU ĐỎ NỔI BẬT:
        <h2>THỦ TỤC <span style="color: #dc2626;">THANH TOÁN</span></h2> -->
        <!-- THAY THẾ: Đổi chữ 'THANH TOÁN' sang MÀU XANH LÁ:
        <h2>THỦ TỤC <span style="color: #16a34a;">THANH TOÁN</span></h2> -->

        <!-- Họa tiết trang trí: 2 đường kẻ kim loại và viên kim cương xoay 45 độ ở giữa -->
        <div class="header-divider"><span class="diamond"></span></div>
      </div>

      <div class="container checkout-container">
        <!-- Khung bao lưới 2 cột: Cột trái (Chi tiết đơn & Voucher) / Cột phải (Hình thức & Địa chỉ giao hàng) -->
        <div class="checkout-grid">
          
          <!-- =====================================================================
               [PHẦN 2: CỘT TRÁI - DANH SÁCH SẢN PHẨM, MÃ GIẢM GIÁ & TÍNH TIỀN]
          ====================================================================== -->
          <div class="checkout-left-section">
            
            <!-- -------------------------------------------------------------------
                 TRƯỜNG HỢP 1: THANH TOÁN TỪ GIỎ HÀNG (CÓ NHIỀU SẢN PHẨM)
                 Điều kiện v-if="isFromCart": Kích hoạt khi URL có tham số '?from=cart'
            -------------------------------------------------------------------- -->
            <div v-if="isFromCart" class="cart-summary-list">
              <!-- Tiêu đề danh mục kèm tổng số lượng mặt hàng trong giỏ -->
              <h3 class="section-title-sub">SẢN PHẨM TRONG ĐƠN HÀNG ({{ cartList.length }})</h3>
              
              <!-- 
                Vòng lặp v-for hiển thị từng sản phẩm trong giỏ hàng:
                - 'item in cartList': Duyệt qua từng object sản phẩm.
                - ':key="item.maGioHang"': Định danh duy nhất cho từng phần tử giúp Vue tối ưu DOM.
              -->
              <div class="cart-checkout-item" v-for="item in cartList" :key="item.maGioHang">
                <!-- 
                  Ảnh đại diện nhỏ của sản phẩm:
                  - 'item.anhDaiDien.startsWith("http")': Kiểm tra nếu là link ảnh Cloud/Online thì hiển thị trực tiếp.
                  - Nếu là ảnh nội bộ thì tự động nối thêm đường dẫn '/img/'.
                -->
                <img 
                  :src="item.anhDaiDien && item.anhDaiDien.startsWith('http') ? item.anhDaiDien : '/img/' + (item.anhDaiDien || '')" 
                  :alt="item.tenSanPham" 
                  class="cart-item-thumb"
                />
                
                <!-- Thông tin mô tả ngắn: Tên sản phẩm, số lượng và tổng giá từng món -->
                <div class="cart-item-info">
                  <h4 class="cart-item-name">{{ item.tenSanPham }}</h4>
                  <p class="cart-item-meta">Số lượng: <strong>x{{ item.soLuong }}</strong></p>
                  <!-- Thành tiền của dòng = Giá bán nhân với Số lượng đặt -->
                  <p class="cart-item-price">{{ formatPrice(item.giaBan * item.soLuong) }}</p>
                </div>
              </div>
            </div>

            <!-- -------------------------------------------------------------------
                 TRƯỜNG HỢP 2: THANH TOÁN MUA NGAY 1 SẢN PHẨM TRỰC TIẾP
                 Điều kiện v-else-if="product": Kích hoạt khi khách bấm nút Mua Ngay từ trang chi tiết
            -------------------------------------------------------------------- -->
            <div v-else-if="product" class="product-preview-card">
              <!-- Khung hiển thị ảnh sản phẩm kích thước lớn -->
              <div class="image-box">
                <img 
                  :src="product.anhDaiDien && product.anhDaiDien.startsWith('http') ? product.anhDaiDien : '/img/' + (product.anhDaiDien || '')" 
                  :alt="product.tenSanPham" 
                  class="preview-img"
                />
              </div>

              <!-- Bảng tóm tắt thông tin sản phẩm mua ngay -->
              <div class="product-summary">
                <h3 class="product-title">{{ product.tenSanPham }}</h3>
                <p class="product-qty">Số lượng đặt: <strong>{{ quantity }}</strong></p>
                <p class="product-total-price">
                  <!-- Hiển thị giá tạm tính bằng Đơn giá nhân số lượng -->
                  Đơn giá tạm tính: <span class="gold">{{ formatPrice(product.giaBan * quantity) }}</span>
                </p>
              </div>
            </div>

            <!-- -------------------------------------------------------------------
                 [KHỐI MÃ GIẢM GIÁ (VOUCHER) ĐỘC QUYỀN]
                 Liên kết với API kiểm tra: GET /api/admin/ma-giam-gia/kiem-tra?code=...
            -------------------------------------------------------------------- -->
            <div class="voucher-box-container">
              <label class="voucher-label">
                <i class="fa-solid fa-ticket gold"></i> MÃ GIẢM GIÁ ĐỘC QUYỀN
              </label>

              <!-- 
                TRẠNG THÁI A: Chưa áp dụng mã (appliedVoucher == null)
                Hiển thị ô nhập chuỗi mã code và nút ÁP DỤNG
              -->
              <div class="voucher-input-group" v-if="!appliedVoucher">
                <!-- 
                  Ô nhập mã voucher:
                  - 'v-model="voucherCodeInput"': Ràng buộc biến 2 chiều với dữ liệu người dùng gõ.
                  - '@keyup.enter="kiemTraVaApDungVoucher"': Bấm phím Enter trên bàn phím sẽ kích hoạt kiểm tra ngay.
                -->
                <input 
                  type="text" 
                  v-model="voucherCodeInput" 
                  placeholder="Nhập mã ưu đãi (VD: SALE10)..." 
                  @keyup.enter="kiemTraVaApDungVoucher" 
                  class="input-voucher"
                />
                <!-- 
                  Nút bấm kiểm tra mã:
                  - ':disabled="isCheckingVoucher"': Khóa nút khi đang gửi request lên server để tránh gửi nhiều lần.
                  - '@click="kiemTraVaApDungVoucher"': Gọi hàm gửi mã về backend.
                -->
                <button 
                  type="button" 
                  class="btn-apply-voucher" 
                  :disabled="isCheckingVoucher" 
                  @click="kiemTraVaApDungVoucher"
                >
                  <i v-if="isCheckingVoucher" class="fas fa-spinner fa-spin"></i>
                  {{ isCheckingVoucher ? 'ĐANG KIỂM TRA...' : 'ÁP DỤNG' }}
                </button>
              </div>

              <!-- 
                TRẠNG THÁI B: Đã áp dụng mã thành công (appliedVoucher != null)
                Hiển thị Badge màu xanh lá báo thành công kèm nút dấu X để hủy mã
              -->
              <div class="applied-voucher-badge" v-else>
                <div class="applied-info">
                  <i class="fa-solid fa-circle-check text-success"></i>
                  <span>Đã áp dụng mã <strong>{{ appliedVoucher.maCode }}</strong>: Giảm {{ appliedVoucher.phanTramGiam }}%</span>
                </div>
                <!-- Nút dấu X để hủy bỏ voucher đang dùng và trả về giá trị ban đầu -->
                <button type="button" class="btn-remove-voucher" title="Hủy áp dụng mã này" @click="huyApDungVoucher">
                  <i class="fa-solid fa-xmark"></i>
                </button>
              </div>
            </div>

            <!-- -------------------------------------------------------------------
                 HỘP TÍNH TOÁN TỔNG TIỀN VÀ KHẤU TRỪ VOUCHER
            -------------------------------------------------------------------- -->
            <div class="total-calculation-box">
              <!-- Dòng 1: Tạm tính tổng giá trị hàng ban đầu (chưa giảm) -->
              <div class="calc-row">
                <span>Tạm tính giá trị hàng:</span>
                <span>{{ formatPrice(subTotalAmount) }}</span>
              </div>

              <!-- Dòng 2: Số tiền được giảm (Chỉ hiển thị khi có mã giảm giá được áp dụng thành công) -->
              <div class="calc-row discount-text" v-if="appliedVoucher">
                <span>Voucher giảm giá ({{ appliedVoucher.phanTramGiam }}%):</span>
                <span>- {{ formatPrice(discountAmount) }}</span>
              </div>

              <!-- Dòng 3: Tổng tiền thanh toán cuối cùng sau khi đã trừ chiết khấu -->
              <div class="calc-row total-final-row">
                <span>Tổng thanh toán:</span>
                <!-- DÒNG MẶC ĐỊNH: Màu vàng hoàng kim -->
                <span class="gold">{{ formatPrice(finalTotalAmount) }}</span>
                <!-- THAY THẾ: Hiển thị tổng tiền màu đỏ to rõ:
                <span style="color: #dc2626; font-weight: bold;">{{ formatPrice(finalTotalAmount) }}</span> -->
              </div>
            </div>

          </div>

          <!-- =====================================================================
               [PHẦN 3: CỘT PHẢI - CHỌN PHƯƠNG THỨC THANH TOÁN & FORM GIAO HÀNG]
          ====================================================================== -->
          <div class="checkout-right-section">
            
            <!-- --- 3.1 KHUNG CHỌN PHƯƠNG THỨC THANH TOÁN --- -->
            <div class="payment-methods-group">
              <label class="section-label">CHỌN PHƯƠNG THỨC THANH TOÁN</label>
              <div class="methods-row">
                
                <!-- NÚT 1: Chuyển khoản quét mã QR SePay tự động -->
                <!-- Class ':class="{ active: hinhThucThanhToan === "CHUYEN_KHOAN_QR" }"' bật nền nâu viền vàng khi được chọn -->
                <button 
                  type="button" 
                  class="btn-method" 
                  :class="{ active: hinhThucThanhToan === 'CHUYEN_KHOAN_QR' }"
                  @click="hinhThucThanhToan = 'CHUYEN_KHOAN_QR'"
                >
                  <i class="fa-solid fa-qrcode"></i> THANH TOÁN NGAY
                </button>

                <!-- NÚT 2: Trả tiền mặt khi nhận hàng (COD) -->
                <!-- Class ':class="{ active: hinhThucThanhToan === "COD" }"' bật nền nâu viền vàng khi được chọn -->
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

            <!-- --- 3.2 FORM NHẬP THÔNG TIN VẬN CHUYỂN & GIAO HÀNG --- -->
            <div class="shipping-form-group">
              <label class="section-label">THÔNG TIN GIAO NHẬN HÀNG</label>
              
              <!-- Form gửi dữ liệu với sự kiện @submit.prevent (chặn reload trang và gọi hàm xacNhanDatHang) -->
              <form @submit.prevent="xacNhanDatHang" class="velora-form">
                
                <!-- Ô 1: Họ tên người nhận hàng (Bắt buộc) -->
                <div class="form-field">
                  <label for="fullName">HỌ VÀ TÊN NGƯỜI NHẬN *</label>
                  <input type="text" id="fullName" v-model="formOrder.hoTen" required placeholder="Nhập đầy đủ họ và tên..." />
                </div>

                <!-- Ô 2: Số điện thoại nhận hàng (Bắt buộc) -->
                <div class="form-field">
                  <label for="phoneNumber">SỐ ĐIỆN THOẠI *</label>
                  <input type="tel" id="phoneNumber" v-model="formOrder.soDienThoai" required placeholder="Nhập số điện thoại di động..." />
                </div>

                <!-- Ô 3: Địa chỉ Gmail nhận hóa đơn và thông báo đơn (Bắt buộc) -->
                <div class="form-field">
                  <label for="emailAddress">ĐỊA CHỈ GMAIL *</label>
                  <input type="email" id="emailAddress" v-model="formOrder.email" required placeholder="vi-du@gmail.com..." />
                </div>

                <!-- Ô 4: Dropdown chọn Tỉnh / Thành Phố (Bắt buộc) -->
                <!-- Sự kiện @change="onProvinceChange": Tự động kích hoạt tải danh sách quận/huyện tương ứng -->
                <div class="form-field">
                  <label>TỈNH / THÀNH PHỐ *</label>
                  <select v-model="selectedProvince" @change="onProvinceChange" required class="select-address">
                    <option value="">-- Chọn Tỉnh / Thành Phố --</option>
                    <option v-for="p in provinces" :key="p.code" :value="p">{{ p.name }}</option>
                  </select>
                </div>

                <!-- Ô 5: Dropdown chọn Quận / Huyện (Bắt buộc) -->
                <!-- Thuộc tính ':disabled="!selectedProvince"': Khóa chọn nếu chưa chọn Tỉnh phía trên -->
                <div class="form-field">
                  <label>QUẬN / HUYỆN *</label>
                  <select v-model="selectedDistrict" @change="onDistrictChange" :disabled="!selectedProvince" required class="select-address">
                    <option value="">-- Chọn Quận / Huyện --</option>
                    <option v-for="d in districts" :key="d.code" :value="d">{{ d.name }}</option>
                  </select>
                </div>

                <!-- Ô 6: Dropdown chọn Phường / Xã (Bắt buộc) -->
                <!-- Thuộc tính ':disabled="!selectedDistrict"': Khóa chọn nếu chưa chọn Quận phía trên -->
                <div class="form-field">
                  <label>PHƯỜNG / XÃ *</label>
                  <select v-model="selectedWard" :disabled="!selectedDistrict" required class="select-address">
                    <option value="">-- Chọn Phường / Xã --</option>
                    <option v-for="w in wards" :key="w.code" :value="w">{{ w.name }}</option>
                  </select>
                </div>

                <!-- Ô 7: Số nhà, tên ngõ, tên đường cụ thể (Bắt buộc) -->
                <div class="form-field">
                  <label for="specificAddress">ĐỊA CHỈ CỤ THỂ (SỐ NHÀ, TÊN ĐƯỜNG...) *</label>
                  <input type="text" id="specificAddress" v-model="specificAddress" required placeholder="Ví dụ: Số 12, Ngõ 45, Đường Lê Lợi..." />
                </div>

                <!-- Ô 8: Lời nhắn ghi chú cho shipper (Không bắt buộc) -->
                <div class="form-field text-area-field">
                  <label for="orderNote">GHI CHÚ ĐƠN HÀNG (TÙY CHỌN)</label>
                  <textarea 
                    id="orderNote" 
                    v-model="orderNote" 
                    rows="4" 
                    placeholder="Nhập lời nhắn cho shipper, thời gian nhận hàng mong muốn..."
                  ></textarea>
                </div>

                <!-- 
                  NÚT BẤM XÁC NHẬN ĐẶT HÀNG:
                  - ':disabled="isSubmitting"': Vô hiệu hóa nút khi đang gửi dữ liệu lên server tránh spam.
                  - 'v-if="isSubmitting"': Hiện icon xoay tròn biểu thị hệ thống đang xử lý.
                -->
                <!-- DÒNG MẶC ĐỊNH: Màu nâu gỗ đậm, chữ in hoa -->
                <button type="submit" class="btn-submit-order" :disabled="isSubmitting">
                  <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
                  {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN ĐẶT HÀNG ĐỘC QUYỀN' }}
                </button>
                <!-- THAY THẾ: Nút màu vàng hoàng kim:
                <button type="submit" class="btn-submit-order" style="background-color: #d1aa68; border-color: #d1aa68; color: #fff;" :disabled="isSubmitting"><i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i> {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN ĐẶT HÀNG ĐỘC QUYỀN' }}</button> -->
                <!-- THAY THẾ: Nút màu đỏ nổi bật:
                <button type="submit" class="btn-submit-order" style="background-color: #dc2626; border-color: #dc2626; color: #fff;" :disabled="isSubmitting"><i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i> {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN ĐẶT HÀNG ĐỘC QUYỀN' }}</button> -->
                <!-- THAY THẾ: Nút bo tròn hình viên thuốc:
                <button type="submit" class="btn-submit-order" style="border-radius: 50px;" :disabled="isSubmitting"><i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i> {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN ĐẶT HÀNG ĐỘC QUYỀN' }}</button> -->

              </form>
            </div>

          </div>
        </div>
      </div>
    </main>

    <!-- =========================================================================
         [PHẦN 4: TRẠNG THÁI LOADING VÀ BÁO LỖI KHI KHÔNG CÓ ĐƠN]
    ========================================================================== -->
    <!-- Màn hình chờ: Hiện vòng tròn xoay màu vàng khi đang tải dữ liệu ban đầu -->
    <main class="checkout-main-content loading-state" v-else-if="loading">
      <div class="loader"></div>
      <p>Đang thiết lập cấu trúc đơn hàng bảo mật...</p>
    </main>

    <!-- Màn hình rỗng: Hiện khi giỏ hàng trống hoặc link sản phẩm không hợp lệ -->
    <main class="checkout-main-content error-state" v-else>
      <div class="container text-center" style="padding: 60px 0; text-align: center;">
        <h3 style="color: #3e332e; margin-bottom: 15px;">Không tìm thấy thông tin đơn hàng</h3>
        <p style="color: #666; margin-bottom: 20px;">Vui lòng kiểm tra lại giỏ hàng hoặc danh mục kiệt tác thời gian.</p>
        <!-- Nút bấm quay lại cửa hàng -->
        <button class="btn-submit-order" style="max-width: 250px; margin: 0 auto;" @click="router.push('/dong-ho-co-san')">QUAY LẠI CỬA HÀNG</button>
      </div>
    </main>

    <!-- =========================================================================
         [PHẦN 5: CUSTOM POPUP VELORA LUXURY (THAY THẾ WINDOW.ALERT MẶC ĐỊNH)]
    ========================================================================== -->
    <!-- Popup tùy biến phong cách hoàng gia nền đen viền vàng hoàng kim -->
    <div class="custom-popup-overlay" v-if="popup.show" @click="closePopup">
      <!-- '@click.stop': Ngăn chặn nổi bọt sự kiện để khi click vào trong hộp không bị đóng popup -->
      <div class="custom-popup-box" :class="popup.type" @click.stop>
        <!-- Icon trạng thái tương ứng -->
        <div class="popup-icon">
          <i v-if="popup.type === 'success'" class="fas fa-check-circle"></i>
          <i v-else-if="popup.type === 'warning'" class="fas fa-exclamation-triangle"></i>
          <i v-else class="fas fa-times-circle"></i>
        </div>
        <!-- Tiêu đề và nội dung thông điệp -->
        <div class="popup-content">
          <h3>{{ popup.type === 'success' ? 'THÀNH CÔNG' : (popup.type === 'warning' ? 'CHÚ Ý' : 'LỖI') }}</h3>
          <p>{{ popup.message }}</p>
        </div>
        <!-- Nút bấm xác nhận đóng popup -->
        <button class="popup-close-btn" @click="closePopup">XÁC NHẬN</button>
      </div>
    </div>

    <!-- =========================================================================
         [PHẦN 6: CUSTOM TOAST THÔNG BÁO NHANH GÓC MÀN HÌNH]
    ========================================================================== -->
    <!-- Toast nhỏ trượt lên ở góc màn hình tự động biến mất sau 3 giây -->
    <div class="custom-alert-toast" :class="[toast.type, { 'show': toast.show }]">
      <i class="fa-solid" :class="toast.type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'"></i>
      <span>{{ toast.message }}</span>
    </div>

    <!-- Gọi component Footer dùng chung dưới chân trang -->
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Header from '../../components/Header.vue';
import Footer from '../../components/Footer.vue';

// Khởi tạo các hook định tuyến của Vue Router
const route = useRoute();   // Dùng để lấy các tham số Query trên URL (?from=cart hoặc ?buyNowId=...)
const router = useRouter(); // Dùng để điều hướng chuyển trang (router.push)

// =========================================================================
// [LOGIC 1: KHỞI TẠO CÁC BIẾN DỮ LIỆU ĐƠN HÀNG VÀ VOUCHER]
// =========================================================================
const product = ref(null);         // Chứa thông tin chi tiết 1 sản phẩm khi mua ngay
const cartList = ref([]);          // Mảng chứa danh sách các sản phẩm lấy từ CSDL giỏ hàng
const isFromCart = ref(false);      // Cờ logic: true = Đặt từ giỏ hàng, false = Mua ngay trực tiếp
const quantity = ref(1);           // Số lượng đặt mua (mặc định = 1)
const loading = ref(true);          // Biến quản lý trạng thái chờ tải dữ liệu (true = đang tải, false = tải xong)
const isSubmitting = ref(false);    // Cờ khóa nút bấm khi đang thực thi API đặt hàng tránh spam nút

// DÒNG MẶC ĐỊNH: Hình thức thanh toán khởi tạo là Chuyển khoản QR ngân hàng (SePay)
const hinhThucThanhToan = ref('CHUYEN_KHOAN_QR'); 
// THAY THẾ: Mặc định chọn nhận hàng trả tiền mặt (COD):
// const hinhThucThanhToan = ref('COD');

// --- Biến quản lý mã giảm giá ---
const voucherCodeInput = ref('');    // Chuỗi mã người dùng gõ vào ô input (VD: SALE10)
const appliedVoucher = ref(null);     // Đối tượng MaGiamGia hợp lệ nhận từ API backend
const isCheckingVoucher = ref(false); // Cờ hiển thị vòng xoay loading khi đang kiểm tra mã

// Mảng chứa danh sách dữ liệu hành chính Việt Nam từ Open API
const provinces = ref([]);         // Mảng danh sách 63 Tỉnh/Thành phố
const districts = ref([]);         // Mảng danh sách Quận/Huyện của Tỉnh đã chọn
const wards = ref([]);             // Mảng danh sách Phường/Xã của Quận đã chọn

// Các biến lưu giá trị được chọn trên Form giao hàng
const selectedProvince = ref('');
const selectedDistrict = ref('');
const selectedWard = ref('');
const specificAddress = ref('');
const orderNote = ref('');

// Đối tượng lưu thông tin người nhận hàng
const formOrder = ref({
  hoTen: '',
  soDienThoai: '',
  email: ''
});

// Hàm tiện ích format số nguyên sang định dạng tiền tệ VNĐ (Ví dụ: 1500000 -> 1.500.000 ₫)
const formatPrice = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// =========================================================================
// [LOGIC 2: ĐIỀU KHIỂN CUSTOM POPUP VÀ TOAST THÔNG BÁO]
// =========================================================================
// Quản lý biến Popup trung tâm
const popup = ref({
  show: false,
  message: '',
  type: 'success', // 'success' (thành công) | 'warning' (chú ý) | 'error' (lỗi)
  redirectUrl: null // Đường dẫn cần chuyển đến sau khi người dùng bấm Xác Nhận
});

// Hàm kích hoạt mở popup
const showNotification = (message, type = 'success', redirectUrl = null) => {
  popup.value = { show: true, message, type, redirectUrl };
};

// Hàm đóng popup và thực hiện chuyển trang (nếu có cấu hình redirectUrl)
const closePopup = () => {
  const nextRoute = popup.value.redirectUrl;
  popup.value.show = false;
  if (nextRoute) {
    router.push(nextRoute);
  }
};

// Quản lý biến Toast thông báo nhanh góc dưới
const toast = ref({
  show: false,
  message: '',
  type: 'success'
});
let toastTimer = null;

// Hàm bật Toast và tự động hẹn giờ tắt sau 3 giây (3000ms)
const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type };
  if (toastTimer) clearTimeout(toastTimer); // Xóa bộ đếm cũ nếu có thông báo mới đè lên
  
  // DÒNG MẶC ĐỊNH: Thông báo tự tắt sau 3 giây (3000ms)
  toastTimer = setTimeout(() => {
    toast.value.show = false;
  }, 3000);
  // THAY THẾ: Thông báo hiển thị lâu hơn 5 giây:
  // toastTimer = setTimeout(() => { toast.value.show = false; }, 5000);
  // THAY THẾ: Thông báo tắt nhanh trong 1 giây:
  // toastTimer = setTimeout(() => { toast.value.show = false; }, 1000);
};

// =========================================================================
// [LOGIC 3: TÍNH TOÁN TỔNG TIỀN VÀ KHẤU TRỪ VOUCHER GIẢM GIÁ]
// =========================================================================
// Computed: Tính tổng tạm tính của toàn bộ sản phẩm trước khi giảm giá
const subTotalAmount = computed(() => {
  if (isFromCart.value) {
    // Nếu từ giỏ: Dùng hàm reduce cộng dồn (Đơn giá * Số lượng) của từng món
    return cartList.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
  }
  // Nếu mua ngay: Lấy đơn giá sản phẩm nhân số lượng mua
  return product.value ? (product.value.giaBan * quantity.value) : 0;
});

// Computed: Tính số tiền thực tế được trừ theo % của mã giảm giá
const discountAmount = computed(() => {
  if (!appliedVoucher.value || !appliedVoucher.value.phanTramGiam) return 0;
  // Số tiền giảm = Tổng tạm tính * (Phần trăm giảm / 100)
  return subTotalAmount.value * (appliedVoucher.value.phanTramGiam / 100);
});

// Computed: Tính số tiền thực tế khách phải trả = Tạm tính trừ đi tiền giảm
const finalTotalAmount = computed(() => {
  const total = subTotalAmount.value - discountAmount.value;
  return total > 0 ? total : 0; // Đảm bảo số tiền không bị âm
});

// =========================================================================
// [LOGIC 4: KIỂM TRA & ÁP DỤNG MÃ GIẢM GIÁ (GỌI API BACKEND)]
// =========================================================================
/**
 * Gọi API kiểm tra mã voucher:
 * Endpoint: GET http://localhost:8080/api/admin/ma-giam-gia/kiem-tra?code=...
 */
const kiemTraVaApDungVoucher = async () => {
  const code = voucherCodeInput.value.trim().toUpperCase(); // Tự động viết hoa mã code
  if (!code) {
    showToast('Vui lòng nhập mã giảm giá!', 'warning');
    return;
  }

  isCheckingVoucher.value = true; // Bật cờ xoay icon loading trên nút
  try {
    const res = await fetch(`http://localhost:8080/api/admin/ma-giam-gia/kiem-tra?code=${encodeURIComponent(code)}`);
    
    if (res.ok) {
      // Nhận về đối tượng MaGiamGia hợp lệ từ Spring Boot
      const voucherData = await res.json();
      appliedVoucher.value = voucherData;
      
      // Lưu vào LocalStorage để khi khách F5 lại trang không bị mất voucher
      localStorage.setItem('activeVoucher', JSON.stringify(voucherData));
      
      showToast(`Áp dụng mã [${voucherData.maCode}] thành công! Bạn được giảm ${voucherData.phanTramGiam}%.`, 'success');
      voucherCodeInput.value = ''; // Xóa trắng ô nhập sau khi áp dụng thành công
    } else {
      // Backend trả về mã lỗi 400 kèm nguyên nhân (Hết hạn, hết lượt dùng...)
      const errorMsg = await res.text();
      showToast(errorMsg || 'Mã giảm giá không hợp lệ hoặc đã hết hạn!', 'error');
    }
  } catch (error) {
    console.error('Lỗi kiểm tra voucher:', error);
    showToast('Không thể kết nối đến máy chủ để xác minh mã giảm giá!', 'error');
  } finally {
    isCheckingVoucher.value = false; // Tắt icon loading
  }
};

/**
 * Gỡ bỏ mã giảm giá đang áp dụng và đưa tiền về mức ban đầu
 */
const huyApDungVoucher = () => {
  appliedVoucher.value = null;
  localStorage.removeItem('activeVoucher'); // Xóa khỏi bộ nhớ máy
  showToast('Đã hủy áp dụng mã giảm giá.', 'warning');
};

// =========================================================================
// [LOGIC 5: GỌI API ĐỊA GIỚI HÀNH CHÍNH (TỈNH - QUẬN - PHƯỜNG)]
// =========================================================================
// Gọi API lấy danh sách toàn bộ Tỉnh/Thành phố Việt Nam
const fetchProvinces = async () => {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/');
    if (res.ok) provinces.value = await res.json();
  } catch (err) {
    console.error('Lỗi tải danh sách Tỉnh/Thành:', err);
  }
};

// Khi chọn Tỉnh: Reset Quận/Phường và tải danh sách Quận trực thuộc
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

// Khi chọn Quận: Reset Phường và tải danh sách Phường/Xã trực thuộc
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

// =========================================================================
// [LOGIC 6: KHỞI TẠO DỮ LIỆU BAN ĐẦU KHI VÀO TRANG]
// =========================================================================
const khoiTaoDonHang = async () => {
  loading.value = true;
  
  // Tự động khôi phục mã giảm giá nếu phiên trước đó đã áp dụng
  const voucherStr = localStorage.getItem('activeVoucher');
  if (voucherStr) {
    try { appliedVoucher.value = JSON.parse(voucherStr); } catch (e) {}
  }

  // Tự động điền thông tin tài khoản nếu đã đăng nhập vào hệ thống
  const userStr = localStorage.getItem('user');
  let user = null;
  if (userStr) {
    try {
      user = JSON.parse(userStr);
      formOrder.value.hoTen = user.hoTen || user.tenNguoiDung || '';
      formOrder.value.email = user.email || '';
      formOrder.value.soDienThoai = user.soDienThoai || '';
    } catch(e) { console.error(e); }
  }

  // Phân loại luồng dựa theo URL Query:
  // Luồng 1: Thanh toán từ giỏ hàng (?from=cart)
  if (route.query.from === 'cart') {
    isFromCart.value = true;
    const userId = user ? user.maNguoiDung : 3;
    try {
      const res = await fetch(`http://localhost:8080/api/gio-hang/${userId}`);
      if (res.ok) {
        cartList.value = await res.json();
      }
    } catch (error) {
      console.error("Lỗi lấy danh sách giỏ hàng:", error);
    } finally {
      loading.value = false;
    }
  } else {
    // Luồng 2: Mua ngay 1 sản phẩm (?buyNowId=...&qty=...)
    isFromCart.value = false;
    const productId = route.query.buyNowId;
    const qtyParam = route.query.qty;

    if (!productId) {
      showToast('Không tìm thấy thông tin sản phẩm thanh toán!', 'warning');
      router.push('/dong-ho-co-san');
      return;
    }

    quantity.value = qtyParam ? parseInt(qtyParam) : 1;

    try {
      const res = await fetch(`http://localhost:8080/api/san-pham/${productId}`);
      if (res.ok) {
        product.value = await res.json();
      }
    } catch (error) {
      console.error("Lỗi lấy thông tin sản phẩm:", error);
    } finally {
      loading.value = false;
    }
  }
};

// =========================================================================
// [LOGIC 7: GỌI API BACKEND XÁC NHẬN ĐẶT HÀNG]
// =========================================================================
const xacNhanDatHang = async () => {
  // Kiểm tra tính hợp lệ của địa chỉ
  if (!selectedProvince.value || !selectedDistrict.value || !selectedWard.value) {
    showToast('Vui lòng chọn đầy đủ thông tin Tỉnh / Quận / Phường!', 'warning');
    return;
  }

  if (!specificAddress.value.trim()) {
    showToast('Vui lòng nhập số nhà, tên đường cụ thể!', 'warning');
    return;
  }

  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : { maNguoiDung: 3 };

  // Nối chuỗi địa chỉ đầy đủ
  const diaChiHoanChinh = `${specificAddress.value.trim()}, ${selectedWard.value.name}, ${selectedDistrict.value.name}, ${selectedProvince.value.name}`;
  
  // DÒNG MẶC ĐỊNH: Mã đơn hàng ngẫu nhiên có tiền tố VELORA-
  const maCodeNgauNhien = 'VELORA-' + Date.now();
  // THAY THẾ: Mã đơn hàng có tiền tố DH-:
  // const maCodeNgauNhien = 'DH-' + Date.now();

  let payload = {};
  let endpoint = '';

  // Đóng gói dữ liệu JSON gửi lên Spring Boot (Kèm mã giảm giá nếu có áp dụng)
  if (isFromCart.value) {
    endpoint = 'http://localhost:8080/api/don-hang/dat-gio-hang';
    payload = {
      maNguoiDung: user.maNguoiDung || 3,
      maDonHangCode: maCodeNgauNhien,
      tenNguoiNhan: formOrder.value.hoTen,
      soDienThoaiGiaoHang: formOrder.value.soDienThoai,
      email: formOrder.value.email.trim(),
      diaChiGiaoHang: diaChiHoanChinh,
      phuongThucThanhToan: hinhThucThanhToan.value,
      tongTien: finalTotalAmount.value,
      ghiChuDonHang: orderNote.value.trim(),
      maGiamGia: appliedVoucher.value ? appliedVoucher.value.maCode : null
    };
  } else {
    endpoint = 'http://localhost:8080/api/don-hang/dat-ngay';
    payload = {
      maNguoiDung: user.maNguoiDung || 3,
      maDonHangCode: maCodeNgauNhien, 
      tenNguoiNhan: formOrder.value.hoTen,
      soDienThoaiGiaoHang: formOrder.value.soDienThoai,
      email: formOrder.value.email.trim(),
      diaChiGiaoHang: diaChiHoanChinh,
      phuongThucThanhToan: hinhThucThanhToan.value,
      maSanPham: product.value.maSanPham,
      soLuong: quantity.value,
      tongTien: finalTotalAmount.value, 
      ghiChuDonHang: orderNote.value.trim(),
      maGiamGia: appliedVoucher.value ? appliedVoucher.value.maCode : null
    };
  }

  isSubmitting.value = true; // Khóa nút bấm chống spam

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      // Đặt đơn thành công -> Xóa voucher khỏi bộ nhớ tạm
      localStorage.removeItem('activeVoucher');

      if (hinhThucThanhToan.value === 'CHUYEN_KHOAN_QR') {
        // Nếu chọn Chuyển khoản QR: Điều hướng sang trang quét mã SePay
        router.push({
          path: '/scan-qr',
          query: {
            code: maCodeNgauNhien,
            amount: finalTotalAmount.value,
            buyer: formOrder.value.hoTen,
            phone: formOrder.value.soDienThoai
          }
        });
      } else {
        // Nếu chọn COD: Bật popup thông báo thành công và chuyển về trang lịch sử đơn hàng
        showNotification(
          'Đặt hàng thành công! Hệ thống Velora đang xử lý đơn hàng COD của bạn.',
          'success',
          '/don-hang'
        );
      }
    } else {
      const errTxt = await res.text();
      showNotification('Đặt hàng thất bại: ' + errTxt, 'error');
    }
  } catch (error) {
    showNotification('Không thể kết nối đến máy chủ Backend. Vui lòng thử lại sau!', 'error');
  } finally {
    isSubmitting.value = false; // Mở lại nút bấm
  }
};

// Khi Component được tải xong: Gọi hàm lấy đơn hàng và danh sách tỉnh thành
onMounted(() => {
  khoiTaoDonHang();
  fetchProvinces(); 
});
</script>

<style scoped>
/* =========================================================================
   [CSS NHÓM 1: BỐ CỤC KHUNG CHÍNH CỦA TRANG THANH TOÁN]
========================================================================= */
.checkout-page-wrapper { 
  background: #f4f1ea;                          /* Màu nền toàn trang: KEM NHẠT */
  min-height: 100vh;                            /* Chiều cao tối thiểu 100% màn hình */
  font-family: sans-serif; 
}

.checkout-main-content { 
  padding: 40px 0 80px 0;                       /* Khoảng cách đệm: trên 40px, dưới 80px */
}

.section-header { 
  text-align: center;                           /* Căn tiêu đề ra CHÍNH GIỮA */
  margin-bottom: 40px; 
}

.section-header h2 { 
  color: #3e332e;                               /* Màu chữ tiêu đề: NÂU GỖ ĐẬM */
  font-size: 28px; 
  letter-spacing: 2px;                          /* Khoảng cách chữ: 2px */
}

.gold { 
  color: #d1aa68;                               /* Màu vàng hoàng kim thương hiệu Velora */
}

.header-divider { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  margin-top: 10px; 
}

.header-divider::before, 
.header-divider::after { 
  content: ""; 
  width: 50px;                                  /* Chiều dài đường kẻ ngang: 50px */
  height: 1px;                                  /* Độ dày đường kẻ 1px */
  background: #d1aa68;                          /* Màu đường kẻ: VÀNG */
}

.diamond { 
  width: 6px; 
  height: 6px; 
  background: #d1aa68; 
  transform: rotate(45deg);                     /* Viên kim cương xoay 45 độ */
  margin: 0 10px; 
}

.container { 
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 0 15px; 
}

.checkout-grid { 
  display: flex; 
  gap: 40px;                                    /* Khoảng cách giữa 2 cột là 40px */
  align-items: flex-start; 
}

/* =========================================================================
   [CSS NHÓM 2: 2 KHUNG CỘT TRÁI (SẢN PHẨM) & CỘT PHẢI (FORM GIAO HÀNG)]
========================================================================= */
.checkout-left-section { 
  flex: 1; 
  background: #fff;                             /* Nền khung TRẮNG TINH */
  border: 1px solid #e0dcd5;                    /* Viền xám kem */
  padding: 30px; 
  border-radius: 8px;                           /* Bo góc 8px */
}

.checkout-right-section { 
  flex: 1; 
  background: #fff; 
  border: 1px solid #e0dcd5; 
  padding: 30px; 
  border-radius: 8px; 
}

.section-title-sub { 
  font-size: 14px; 
  font-weight: bold; 
  color: #3e332e; 
  margin-bottom: 20px; 
  padding-bottom: 10px; 
  border-bottom: 1px solid #e0dcd5; 
  letter-spacing: 1px; 
}

.cart-checkout-item { 
  display: flex; 
  gap: 15px; 
  margin-bottom: 15px; 
  padding-bottom: 15px; 
  border-bottom: 1px dashed #eee;               /* Đường đứt đoạn ngăn cách */
  align-items: center; 
}

.cart-item-thumb { 
  width: 65px; 
  height: 65px; 
  object-fit: cover; 
  border-radius: 4px; 
  border: 1px solid #eee; 
}

.cart-item-info { flex: 1; }
.cart-item-name { font-size: 14px; color: #3e332e; margin-bottom: 4px; }
.cart-item-meta { font-size: 12px; color: #777; margin-bottom: 2px; }
.cart-item-price { font-size: 14px; font-weight: bold; color: #d1aa68; }

/* --- CSS KHỐI NHẬP MÃ GIẢM GIÁ (VOUCHER) --- */
.voucher-box-container {
  margin-top: 25px;
  padding: 18px;
  background: #faf8f5;                          /* Nền kem nhạt */
  border: 1px dashed #d1aa68;                   /* Viền vàng đứt nét sang trọng */
  border-radius: 6px;
}

.voucher-label {
  display: block;
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 1px;
  color: #3e332e;
  margin-bottom: 10px;
}

.voucher-input-group {
  display: flex;
  gap: 10px;
}

.input-voucher {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e0dcd5;
  border-radius: 4px;
  outline: none;
  font-size: 13px;
  text-transform: uppercase;                    /* Luôn viết hoa mã code */
  font-weight: 600;
  letter-spacing: 1px;
  transition: 0.3s;
}

.input-voucher:focus {
  border-color: #d1aa68;
  background: #fff;
}

.btn-apply-voucher {
  padding: 10px 20px;
  background: #3e332e;                          /* Nền nâu gỗ đậm */
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
  white-space: nowrap;
}

.btn-apply-voucher:hover:not(:disabled) {
  background: #d1aa68;                          /* Đổi sang VÀNG khi hover */
}

.btn-apply-voucher:disabled {
  background: #999;
  cursor: not-allowed;
}

.applied-voucher-badge {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #e6f9ec;                          /* Nền xanh lá nhạt báo hợp lệ */
  border: 1px solid #a3e9b8;
  padding: 10px 14px;
  border-radius: 4px;
}

.applied-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #198754;
}

.btn-remove-voucher {
  background: none;
  border: none;
  color: #dc3545;                               /* Nút dấu X màu đỏ */
  font-size: 15px;
  cursor: pointer;
  padding: 4px;
  transition: 0.2s;
}

.btn-remove-voucher:hover {
  transform: scale(1.2);
}

/* Hộp hiển thị tính tiền */
.total-calculation-box { 
  background: #faf9f6; 
  padding: 20px; 
  border-radius: 6px; 
  margin-top: 20px; 
  border: 1px solid #f0efeb; 
}

.calc-row { 
  display: flex; 
  justify-content: space-between; 
  font-size: 14px; 
  color: #555; 
  margin-bottom: 10px; 
}

.discount-text { 
  color: #198754;                               /* Màu chữ tiền giảm voucher: XANH LÁ */
  font-weight: bold; 
}

.total-final-row { 
  border-top: 1px solid #e0dcd5; 
  padding-top: 12px; 
  margin-top: 10px; 
  font-size: 18px; 
  font-weight: bold; 
  color: #3e332e; 
}

/* Ảnh và thông tin khi Mua Ngay 1 sản phẩm */
.preview-img { width: 100%; height: auto; display: block; object-fit: cover; }
.product-summary { margin-top: 20px; border-top: 1px solid #f0efeb; padding-top: 15px; }
.product-title { font-size: 20px; color: #3e332e; margin-bottom: 10px; }
.product-qty { font-size: 14px; color: #666; }
.product-total-price { font-size: 18px; font-weight: bold; margin-top: 10px; }

/* =========================================================================
   [CSS NHÓM 3: NÚT CHỌN PHƯƠNG THỨC THANH TOÁN]
========================================================================= */
.section-label { 
  font-size: 13px; 
  font-weight: bold; 
  color: #3e332e; 
  letter-spacing: 1px; 
  display: block; 
  margin-bottom: 15px; 
  border-left: 3px solid #d1aa68;               /* Vạch kẻ vàng trang trí bên trái */
  padding-left: 8px; 
}

.payment-methods-group { margin-bottom: 30px; }
.methods-row { display: flex; gap: 15px; }

.btn-method { 
  flex: 1; 
  padding: 15px; 
  border: 1px solid #e0dcd5; 
  background: #faf9f6; 
  color: #3e332e; 
  font-weight: bold; 
  font-size: 13px; 
  cursor: pointer; 
  transition: 0.3s; 
  border-radius: 4px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 8px; 
}

.btn-method:hover { 
  border-color: #d1aa68; 
  color: #d1aa68; 
}

.btn-method.active { 
  background: #3e332e;                          /* Nền NÂU GỖ ĐẬM khi chọn */
  color: #d1aa68;                               /* Chữ VÀNG HOÀNG KIM */
  border-color: #3e332e; 
}

/* =========================================================================
   [CSS NHÓM 4: CÁC Ô FORM NHẬP THÔNG TIN & NÚT ĐẶT HÀNG]
========================================================================= */
.velora-form { display: flex; flex-direction: column; gap: 15px; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 11px; font-weight: bold; color: #888; letter-spacing: 1px; }

.form-field input, 
.select-address { 
  padding: 12px; 
  border: 1px solid #e0dcd5; 
  background-color: #faf9f6; 
  border-radius: 4px; 
  outline: none; 
  transition: 0.3s; 
  font-size: 14px; 
}

.form-field input:focus, 
.select-address:focus { 
  border-color: #d1aa68; 
  background-color: #fff; 
}

.select-address { cursor: pointer; color: #3e332e; }
.select-address:disabled { 
  cursor: not-allowed; 
  background-color: #e0dcd5; 
  color: #888; 
}

.text-area-field textarea { 
  width: 100%; 
  padding: 12px; 
  border: 1px solid #e0dcd5; 
  background-color: #faf9f6; 
  border-radius: 4px; 
  outline: none; 
  font-size: 14px; 
  resize: vertical; 
  font-family: inherit; 
  transition: 0.3s; 
}

.text-area-field textarea:focus { 
  border-color: #d1aa68; 
  background-color: #fff; 
}

.btn-submit-order { 
  width: 100%; 
  padding: 16px; 
  background-color: #3e332e; 
  color: #fff; 
  border: 1px solid #3e332e; 
  font-weight: bold; 
  letter-spacing: 2px; 
  font-size: 14px; 
  cursor: pointer; 
  margin-top: 15px; 
  transition: 0.3s; 
  text-transform: uppercase; 
}

.btn-submit-order:hover { 
  background-color: #d1aa68; 
  border-color: #d1aa68; 
}

.btn-submit-order:disabled {
  background-color: #888; 
  border-color: #888; 
  cursor: not-allowed; 
}

/* =========================================================================
   [CSS NHÓM 5: CUSTOM POPUP VELORA LUXURY]
========================================================================= */
.custom-popup-overlay {
  position: fixed; 
  top: 0; 
  left: 0; 
  width: 100vw; 
  height: 100vh; 
  background: rgba(0, 0, 0, 0.7); 
  backdrop-filter: blur(5px); 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  z-index: 99999; 
  animation: fadeIn 0.3s ease; 
}

.custom-popup-box {
  background: #1e1e1e;                          /* Màu nền hộp popup: ĐEN LUXURY */
  border: 1px solid #d1aa68;                    /* Viền hộp: VÀNG HOÀNG KIM */
  border-radius: 0 !important; 
  padding: 30px 40px; 
  width: 90%; 
  max-width: 420px; 
  text-align: center; 
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6); 
  animation: scaleUp 0.3s ease; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  gap: 15px; 
}

.popup-icon i { font-size: 55px; }
.custom-popup-box.success .popup-icon i { color: #2ecc71; }
.custom-popup-box.warning .popup-icon i { color: #f39c12; }
.custom-popup-box.error .popup-icon i { color: #e74c3c; }

.popup-content h3 {
  color: #d1aa68; 
  margin: 0 0 10px 0; 
  font-size: 20px; 
  letter-spacing: 2px; 
  font-weight: bold; 
}

.popup-content p {
  color: #e0e0e0; 
  margin: 0; 
  font-size: 15px; 
  line-height: 1.6; 
}

.popup-close-btn {
  margin-top: 15px; 
  background: #d1aa68; 
  color: #111; 
  border: none; 
  padding: 12px 30px; 
  font-size: 14px; 
  font-weight: bold; 
  letter-spacing: 1px; 
  border-radius: 0 !important; 
  cursor: pointer; 
  transition: all 0.3s ease; 
  width: 100%; 
}

.popup-close-btn:hover {
  background: #b8955b; 
  transform: translateY(-2px); 
}

/* =========================================================================
   [CSS NHÓM 6: CUSTOM TOAST THÔNG BÁO NHANH GÓC MÀN HÌNH]
========================================================================= */
.custom-alert-toast {
  position: fixed; 
  
  /* [VỊ TRÍ HIỂN THỊ CỦA THÔNG BÁO TOAST] */
  bottom: 30px;                                 /* DÒNG MẶC ĐỊNH: Cách mép dưới 30px */
  right: 30px;                                  /* DÒNG MẶC ĐỊNH: Cách mép phải 30px (GÓC DƯỚI BÊN PHẢI) */

  /* THAY THẾ: Chuyển Toast sang GÓC DƯỚI BÊN TRÁI:
  bottom: 30px; left: 30px; right: auto; */

  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN PHẢI:
  top: 30px; right: 30px; bottom: auto; */

  /* THAY THẾ: Chuyển Toast lên GÓC TRÊN BÊN TRÁI:
  top: 30px; left: 30px; bottom: auto; right: auto; */

  /* THAY THẾ: Căn Toast ra CHÍNH GIỮA MÀN HÌNH PHÍA TRÊN:
  top: 30px; left: 50%; transform: translateX(-50%); bottom: auto; right: auto; */

  padding: 14px 22px; 
  border-radius: 6px; 
  background: #333; 
  color: #fff; 
  font-size: 14px; 
  font-weight: 500; 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); 
  z-index: 99999; 
  transform: translateY(100px); 
  opacity: 0; 
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55); 
}

.custom-alert-toast.show {
  transform: translateY(0); 
  opacity: 1; 
}

.custom-alert-toast.success { background: #198754; }
.custom-alert-toast.warning { background: #f39c12; }
.custom-alert-toast.error { background: #dc3545; }

/* =========================================================================
   [CSS NHÓM 7: HIỆU ỨNG LOADING VÀ RESPONSIVE DI ĐỘNG]
========================================================================= */
.loading-state { text-align: center; padding: 100px 0; color: #888; }

.loader { 
  border: 4px solid #f3f3f3; 
  border-top: 4px solid #d1aa68; 
  border-radius: 50%; 
  width: 40px; 
  height: 40px; 
  animation: spin 1s linear infinite; 
  margin: 0 auto 20px auto; 
}

@keyframes spin { 
  0% { transform: rotate(0deg); } 
  100% { transform: rotate(360deg); } 
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@media (max-width: 850px) {
  .checkout-grid {
    flex-direction: column; 
  }
}
</style>