<template>
  <div class="order-page">
    <!-- =========================================================================
         [PHẦN 1: HEADER VÀ TIÊU ĐỀ TRANG]
    ========================================================================== -->
    <Header />

    <main class="order-content">
      <div class="container">
        
        <!-- 1.1 KHUNG TIÊU ĐỀ CHÍNH -->
        <div class="title-wrapper">
          <!-- DÒNG MẶC ĐỊNH: Tiêu đề in hoa chuẩn -->
          <h1 class="page-title">ĐƠN HÀNG CỦA BẠN</h1>
          <!-- THAY THẾ: Đổi màu tiêu đề sang VÀNG HOÀNG KIM:
          <h1 class="page-title" style="color: #c5a880;">ĐƠN HÀNG CỦA BẠN</h1> -->
          <!-- THAY THẾ: Đổi màu tiêu đề sang ĐỎ NỔI BẬT:
          <h1 class="page-title" style="color: #dc2626;">ĐƠN HÀNG CỦA BẠN</h1> -->

          <!-- Họa tiết trang trí đường kẻ & kim cương -->
          <div class="title-divider">
            <span class="diamond"></span>
          </div>
        </div>

        <!-- =========================================================================
             [PHẦN 2: THANH TAB LỌC TRẠNG THÁI ĐƠN HÀNG]
        ========================================================================== -->
        <div class="order-tabs-wrapper" v-if="orders.length > 0">
          <div class="order-tabs">
            <!-- Duyệt danh sách các tab trạng thái -->
            <button 
              v-for="tab in tabs" 
              :key="tab.value" 
              @click="currentTab = tab.value"
              :class="['tab-btn', { 'active': currentTab === tab.value }]"
            >
              {{ tab.label }}
              <!-- Số đếm số lượng đơn hàng theo từng tab -->
              <span class="tab-count" v-if="getCountByTab(tab.value) > 0">
                ({{ getCountByTab(tab.value) }})
              </span>
            </button>
          </div>
        </div>

        <!-- =========================================================================
             [PHẦN 3: BỐ CỤC 2 CỘT (DANH SÁCH & CHI TIẾT ĐƠN HÀNG)]
        ========================================================================== -->
        <div class="order-layout" v-if="filteredOrders.length > 0">
          
          <!-- -------------------------------------------------------------------
               3.1 CỘT BÊN TRÁI: DANH SÁCH ĐƠN HÀNG & PHÂN TRANG
          -------------------------------------------------------------------- -->
          <div class="order-list-section">
            <h2 class="section-title">LỊCH SỬ GIAO DỊCH</h2>

            <div class="order-list">
              <!-- Duyệt từng thẻ đơn hàng hiển thị theo trang -->
              <div 
                v-for="order in paginatedOrders" 
                :key="order.maDonHang" 
                class="order-card"
                :class="{ 'active': selectedOrder && selectedOrder.maDonHang === order.maDonHang }"
                @click="selectOrder(order)"
              >
                <!-- Mã đơn và ngày đặt -->
                <div class="order-card-header">
                  <span class="order-code">#{{ order.maDonHangCode }}</span>
                  <span class="order-date">{{ order.ngayTao }}</span>
                </div>

                <!-- Tổng tiền và badge trạng thái -->
                <div class="order-card-body">
                  <div class="order-card-footer" style="margin-top: 15px;">
                    <div class="order-total">{{ formatPrice(order.tongTien) }}</div>
                    <!-- THAY THẾ: Tổng tiền hiển thị màu đỏ:
                    <div class="order-total" style="color: #dc2626; font-weight: bold;">{{ formatPrice(order.tongTien) }}</div> -->

                    <div class="order-status" :class="getStatusClass(order.trangThaiDonHang)">
                      {{ getStatusText(order.trangThaiDonHang) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- THANH PHÂN TRANG (PAGINATION) -->
            <div class="pagination-wrapper" v-if="totalPages > 1">
              <!-- Nút trang trước -->
              <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
                <i class="fas fa-chevron-left"></i>
              </button>

              <!-- Các nút số trang -->
              <button 
                v-for="page in totalPages" 
                :key="page" 
                class="page-btn" 
                :class="{ active: currentPage === page }"
                @click="currentPage = page"
              >
                {{ page }}
              </button>

              <!-- Nút trang sau -->
              <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>

          <!-- -------------------------------------------------------------------
               3.2 CỘT BÊN PHẢI: KHUNG CHI TIẾT ĐƠN HÀNG ĐANG CHỌN
          -------------------------------------------------------------------- -->
          <div class="order-detail-section">
            <div v-if="selectedOrder" class="detail-box">

              <!-- HEADER KHUNG CHI TIẾT & CÁC NÚT THAO TÁC -->
              <div class="detail-header">
                <h2>CHI TIẾT ĐƠN HÀNG <span>#{{ selectedOrder.maDonHangCode }}</span></h2>
                <div class="detail-actions">

                  <!-- Badge thông báo khi đang chờ duyệt hủy -->
                  <div v-if="selectedOrder.trangThaiDonHang === 'YEU_CAU_HUY'" class="pending-cancel-badge">
                    <i class="fas fa-clock"></i> Đang chờ duyệt hủy
                  </div>

                  <!-- NÚT 1: HỦY ĐƠN (Chỉ hiện khi đơn ở trạng thái Chờ xử lý) -->
                  <button 
                    v-if="selectedOrder.trangThaiDonHang === 'CHO_XU_LY'" 
                    @click="cancelOrder(selectedOrder)"
                    class="btn-cancel-order"
                  >
                    <i class="fas fa-ban"></i> HỦY ĐƠN
                  </button>
                  <!-- THAY THẾ: Cho phép hủy cả khi đơn đang chuẩn bị hàng:
                  <button v-if="['CHO_XU_LY', 'CHUAN_BI_HANG'].includes(selectedOrder.trangThaiDonHang)" @click="cancelOrder(selectedOrder)" class="btn-cancel-order"><i class="fas fa-ban"></i> HỦY ĐƠN</button> -->

                  <!-- NÚT 2: HOÀN TIỀN (Hiện khi đã giao hoặc đơn thanh toán online bị hủy) -->
                  <button
                    v-if="selectedOrder.trangThaiDonHang === 'DA_GIAO' || (selectedOrder.trangThaiDonHang === 'DA_HUY' && isOnlinePayment(selectedOrder.phuongThucThanhToan))"
                    @click="goToRefundPage(selectedOrder)" 
                    class="btn-refund-order"
                  >
                    <i class="fas fa-undo-alt"></i> HOÀN TIỀN
                  </button>

                  <!-- NÚT 3: YÊU CẦU BẢO HÀNH (Hiện khi đơn đã giao thành công) -->
                  <button 
                    v-if="selectedOrder.trangThaiDonHang === 'DA_GIAO'" 
                    @click="goToWarrantyPage(selectedOrder)" 
                    class="btn-warranty-order"
                  >
                    <i class="fas fa-shield-alt"></i> YÊU CẦU BẢO HÀNH
                  </button>

                  <!-- NÚT MỚI THÊM: IN GIẤY BẢO HÀNH (Chỉ hiển thị khi đơn đã thanh toán: DA_THANH_TOAN) -->
                  <button 
                    v-if="isPaidOrder(selectedOrder)"
                    class="btn-print-warranty" 
                    @click="printWarrantyPaper(selectedOrder)"
                    title="In giấy bảo hành chính hãng Velora"
                  >
                    <i class="fas fa-certificate"></i> IN GIẤY BẢO HÀNH
                  </button>

                  <!-- NÚT 4: IN BIÊN LAI BÀN GIAO -->
                  <button class="btn-print" @click="printInvoice">
                    <i class="fas fa-print"></i> IN BIÊN LAI
                  </button>
                </div>
              </div>

              <!-- THANH TIẾN TRÌNH VẬN CHUYỂN (TRACKING TIMELINE 4 BƯỚC) -->
              <div class="tracking-timeline" v-if="!['DA_HUY', 'YEU_CAU_HUY'].includes(selectedOrder.trangThaiDonHang)">
                <!-- Bước 1: Chờ xử lý -->
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 1), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 1 }">
                  <div class="step-icon"><i class="fas fa-file-invoice-dollar"></i></div>
                  <p>Chờ xử lý</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 1 }"></div>
                
                <!-- Bước 2: Chuẩn bị -->
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 2), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 2 }">
                  <div class="step-icon"><i class="fas fa-box-open"></i></div>
                  <p>Chuẩn bị</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 2 }"></div>
                
                <!-- Bước 3: Đang giao -->
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 3), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 3 }">
                  <div class="step-icon"><i class="fas fa-truck-fast"></i></div>
                  <p>Đang giao</p>
                </div>
                <div class="step-line" :class="{ 'filled': getStepLevel(selectedOrder.trangThaiDonHang) > 3 }"></div>
                
                <!-- Bước 4: Đã giao -->
                <div class="step" :class="{ 'completed': isStepCompleted(selectedOrder.trangThaiDonHang, 4), 'active': getStepLevel(selectedOrder.trangThaiDonHang) === 4 }">
                  <div class="step-icon"><i class="fas fa-check"></i></div>
                  <p>Đã giao</p>
                </div>
              </div>

              <!-- KHUNG HIỂN THỊ LÝ DO HỦY ĐƠN (KHI ĐƠN BỊ HỦY / TỪ CHỐI) -->
              <div 
                v-if="['DA_HUY', 'YEU_CAU_HUY', 'TU_CHOI_HOAN_TIEN'].includes(selectedOrder.trangThaiDonHang)"
                class="cancel-info-box"
              >
                <h3 style="font-size: 14px; font-weight: bold; margin-bottom: 8px;">
                  <i class="fas fa-exclamation-triangle"></i> THÔNG TIN TRẠNG THÁI
                </h3>
                <p style="font-size: 14px; margin-bottom: 5px;">
                  <strong>Tình trạng:</strong> {{ getStatusText(selectedOrder.trangThaiDonHang) }}
                </p>
                <p v-if="selectedOrder.lyDoHuy" style="font-size: 14px; color: #991b1b;">
                  <strong>Lý do:</strong> {{ selectedOrder.lyDoHuy }}
                </p>
              </div>

              <!-- KHUNG THÔNG TIN NHẬN HÀNG VÀ BẢN ĐỒ GIAO HÀNG -->
              <div class="info-map-grid">
                <!-- Cột thông tin người nhận -->
                <div class="info-panel">
                  <h3>THÔNG TIN NHẬN HÀNG</h3>
                  <p><strong>Người nhận:</strong> {{ selectedOrder.tenNguoiNhan }}</p>
                  <p><strong>Số điện thoại:</strong> {{ selectedOrder.soDienThoai }}</p>
                  <p><strong>Địa chỉ:</strong> {{ selectedOrder.diaChi }}</p>
                  <p><strong>Thanh toán:</strong>
                    <span :class="isOnlinePayment(selectedOrder.phuongThucThanhToan) ? 'text-[#c5a880] font-bold' : ''">
                      {{ selectedOrder.phuongThucThanhToan }}
                    </span>
                  </p>
                  <p><strong>Trạng thái tiền:</strong>
                    <span :style="{ color: isPaidOrder(selectedOrder) ? '#27ae60' : '#d97706', fontWeight: 'bold' }">
                      {{ isPaidOrder(selectedOrder) ? 'ĐÃ THANH TOÁN (HỢP LỆ BẢO HÀNH)' : 'CHƯA THANH TOÁN' }}
                    </span>
                  </p>
                </div>

                <!-- Cột bản đồ mô phỏng -->
                <div class="map-panel">
                  <iframe
                    src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.325316278854!2d106.69499931474895!3d10.7863769923145!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f3458d926fb%3A0xc68297cbbf9f1a23!2sBitexco%20Financial%20Tower!5e0!3m2!1sen!2s!4v1623145214002!5m2!1sen!2s"
                    width="100%" height="100%" style="border:0;" allowfullscreen="" loading="lazy" class="luxury-map">
                  </iframe>
                  <!-- Chấm tròn nhấp nháy khi đang giao hàng -->
                  <div class="map-overlay" v-if="selectedOrder.trangThaiDonHang === 'DANG_GIAO'">
                    <div class="pulse-dot"></div> Đang di chuyển
                  </div>
                </div>
              </div>

              <!-- DANH SÁCH SẢN PHẨM TRONG ĐƠN HÀNG -->
              <div class="order-items">
                <h3>SẢN PHẨM TRONG ĐƠN</h3>
                <table class="item-table">
                  <tbody>
                    <tr v-for="(item, idx) in selectedOrder.items" :key="idx">
                      <!-- Ảnh sản phẩm -->
                      <td class="td-img">
                        <img :src="item.anh && item.anh.startsWith('http') ? item.anh : '../img/' + item.anh" alt="Kiệt tác">
                      </td>
                      <!-- Tên và số lượng -->
                      <td class="td-name">
                        <span class="p-name">{{ item.ten }}</span>
                        <span class="p-qty">x {{ item.soLuong }}</span>
                      </td>
                      <!-- Đơn giá -->
                      <td class="td-price">{{ formatPrice(item.gia) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

            </div>

            <!-- Khi chưa chọn đơn hàng nào -->
            <div v-else class="no-selection">
              <i class="fas fa-file-signature"></i>
              <p>Vui lòng chọn một giao dịch để xem chi tiết.</p>
            </div>
          </div>
        </div>

        <!-- Khi không tìm thấy đơn hàng trong tab đang chọn -->
        <div v-else-if="orders.length > 0 && filteredOrders.length === 0" class="empty-orders">
          <i class="fas fa-box-open empty-icon"></i>
          <p>Không có đơn hàng nào ở trạng thái này.</p>
        </div>

        <!-- Khi khách hàng chưa có bất kỳ đơn hàng nào trong hệ thống -->
        <div v-else class="empty-orders">
          <i class="fas fa-receipt empty-icon"></i>
          <p>Quý khách chưa có lịch sử giao dịch nào tại Velora Clock.</p>
          <router-link to="/dong-ho-co-san" class="btn-shopnow">
            MUA SẮM SIÊU PHẨM NGAY
          </router-link>
        </div>

      </div>
    </main>

    <Footer />

    <!-- =========================================================================
         [PHẦN 4: BIÊN BẢN BÀN GIAO CHỈ HIỂN THỊ KHI IN BIÊN LAI]
    ========================================================================== -->
    <div class="print-invoice-template" v-if="selectedOrder && printMode === 'invoice'">
      <div class="print-border-outer">
        <div class="print-border-inner">
          
          <!-- Header Logo & Tên Thương Hiệu -->
          <div class="print-header">
            <div class="print-logo">
              <img src="/img/VeloraIcon.png" alt="Velora Logo">
            </div>
            <div class="print-company-titles">
              <h2 class="company-name">VELORA CLOCK</h2>
              <p class="company-sub">HỆ THỐNG PHÂN PHỐI ĐỒNG HỒ CAO CẤP CHÍNH HÃNG</p>
            </div>
          </div>

          <!-- Thông số mã biên bản & ngày in -->
          <div class="print-meta-top">
            <div class="meta-left">
              <strong>Mã số:</strong> VEL-{{ selectedOrder.maDonHangCode }}/BBBG
            </div>
            <div class="meta-right">
              <strong>Ngày lập:</strong> {{ new Date().toLocaleDateString('vi-VN') }}<br>
              <strong>Trang:</strong> 1 / 1
            </div>
          </div>

          <!-- Tiêu đề văn bản -->
          <div class="print-title-box">
            <h2>BIÊN BẢN BÀN GIAO KIỆT TÁC</h2>
          </div>

          <!-- Phần I: Thông tin người nhận -->
          <div class="print-section">
            <h3>I. THÔNG TIN KHÁCH HÀNG:</h3>
            <table class="no-border-table">
              <tbody>
                <tr>
                  <td width="130"><strong>Khách hàng</strong></td>
                  <td>: {{ selectedOrder.tenNguoiNhan }}</td>
                  <td width="100"><strong>Điện thoại</strong></td>
                  <td>: {{ selectedOrder.soDienThoai }}</td>
                </tr>
                <tr>
                  <td><strong>Địa chỉ bàn giao</strong></td>
                  <td colspan="3">: {{ selectedOrder.diaChi }}</td>
                </tr>
                <tr>
                  <td><strong>Nội dung</strong></td>
                  <td colspan="3">: Bàn giao sản phẩm đồng hồ cao cấp chính hãng theo đúng quy chuẩn thương hiệu.</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Phần II: Chi tiết sản phẩm bàn giao -->
          <div class="print-section">
            <h3>II. CHI TIẾT SẢN PHẨM BÀN GIAO:</h3>
            <table class="bordered-table">
              <thead>
                <tr>
                  <th width="40">STT</th>
                  <th>Mã sản phẩm</th>
                  <th>Tên kiệt tác</th>
                  <th width="50">SL</th>
                  <th>Đơn giá (VNĐ)</th>
                  <th>Thành tiền (VNĐ)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in selectedOrder.items" :key="idx">
                  <td align="center">{{ idx + 1 }}</td>
                  <td align="center">VEL-{{ item.maSanPham }}</td>
                  <td>{{ item.ten }}</td>
                  <td align="center">{{ item.soLuong }}</td>
                  <td align="right">{{ formatPrice(item.gia).replace('₫', '') }}</td>
                  <td align="right">{{ formatPrice(item.gia * item.soLuong).replace('₫', '') }}</td>
                </tr>
                <tr>
                  <td colspan="5" align="right"><strong>TỔNG CỘNG:</strong></td>
                  <td align="right" class="total-price"><strong>{{ formatPrice(selectedOrder.tongTien).replace('₫', '') }}</strong></td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Phần III: Đánh giá tình trạng bàn giao -->
          <div class="print-section">
            <h3>III. XÁC NHẬN TÌNH TRẠNG:</h3>
            <table class="bordered-table">
              <thead>
                <tr>
                  <th>Hạng mục kiểm tra</th>
                  <th>Tình trạng đánh giá</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>1. Tình trạng ngoại quan (Vỏ, kính, dây đeo)</td>
                  <td align="center">Hoàn hảo / Không trầy xước</td>
                </tr>
                <tr>
                  <td>2. Phụ kiện đi kèm (Hộp gỗ, Thẻ bảo hành, Sách HDSD)</td>
                  <td align="center">Đầy đủ / Nguyên seal</td>
                </tr>
                <tr>
                  <td>3. Tình trạng hoạt động (Kim, lịch, bộ máy)</td>
                  <td align="center">Vận hành chính xác tuyệt đối</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Khu vực chữ ký 2 bên -->
          <div class="print-signatures">
            <div class="sig-box">
              <strong>ĐẠI DIỆN KHÁCH HÀNG</strong><br>
              <em class="sig-note">(Ký, ghi rõ họ tên)</em>
            </div>
            <div class="sig-box">
              <strong>ĐẠI DIỆN VELORA CLOCK</strong><br>
              <em class="sig-note">(Ký, đóng dấu, ghi rõ họ tên)</em>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- =========================================================================
         [PHẦN 4.1: GIẤY XÁC NHẬN BẢO HÀNH CHÍNH HÃNG KHI BẤM IN GIẤY BẢO HÀNH]
    ========================================================================== -->
    <div class="print-warranty-template" v-if="selectedOrder && printMode === 'warranty'">
      <div class="print-border-outer warranty-outer">
        <div class="print-border-inner warranty-inner">
          
          <!-- Header Logo & Tiêu đề bảo hành -->
          <div class="print-header">
            <div class="print-logo">
              <img src="/img/VeloraIcon.png" alt="Velora Logo">
            </div>
            <div class="print-company-titles">
              <h2 class="company-name">VELORA CLOCK BOUTIQUE</h2>
              <p class="company-sub">TRUNG TÂM BẢO HÀNH & KIỂM ĐỊNH THỜI GIAN ĐỘC QUYỀN</p>
            </div>
          </div>

          <div class="print-meta-top">
            <div class="meta-left">
              <strong>Số thẻ BH:</strong> WAR-{{ selectedOrder.maDonHangCode }}<br>
              <strong>Đơn hàng:</strong> #{{ selectedOrder.maDonHangCode }}
            </div>
            <div class="meta-right">
              <strong>Ngày kích hoạt:</strong> {{ selectedOrder.ngayTao }}<br>
              <strong>Thời hạn bảo hành:</strong> 02 Năm (24 Tháng Chính Hãng)
            </div>
          </div>

          <div class="print-title-box">
            <h2 style="color: #cca15e; letter-spacing: 2px;">GIẤY XÁC NHẬN BẢO HÀNH ĐỘC QUYỀN</h2>
            <p style="font-size: 12px; margin: 4px 0 0 0; font-style: italic; color: #555;">(Áp dụng toàn bộ hệ thống trung tâm bảo hành và tra cứu trực tuyến Velora)</p>
          </div>

          <!-- Thông tin chủ sở hữu -->
          <div class="print-section">
            <h3>I. THÔNG TIN CHỦ SỞ HỮU KIỆT TÁC:</h3>
            <table class="no-border-table">
              <tbody>
                <tr>
                  <td width="130"><strong>Họ và tên</strong></td>
                  <td>: {{ selectedOrder.tenNguoiNhan }}</td>
                  <td width="110"><strong>Số điện thoại</strong></td>
                  <td>: {{ selectedOrder.soDienThoai }}</td>
                </tr>
                <tr>
                  <td><strong>Địa chỉ liên hệ</strong></td>
                  <td>: {{ selectedOrder.diaChi }}</td>
                  <td><strong>Email đăng ký</strong></td>
                  <td>: {{ selectedOrder.email || 'Hệ thống bảo mật' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Danh sách đồng hồ được bảo hành -->
          <div class="print-section">
            <h3>II. DANH MỤC KIỆT TÁC BẢO HÀNH CHÍNH HÃNG:</h3>
            <table class="bordered-table">
              <thead>
                <tr>
                  <th width="40">STT</th>
                  <th>Mã số định danh</th>
                  <th>Tên cỗ máy / Phiên bản</th>
                  <th width="60">Số lượng</th>
                  <th>Thời hạn bảo hành</th>
                  <th>Trạng thái kích hoạt</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in selectedOrder.items" :key="idx">
                  <td align="center">{{ idx + 1 }}</td>
                  <td align="center"><strong>VEL-{{ item.maSanPham || 'EDITION' }}</strong></td>
                  <td>{{ item.ten }}</td>
                  <td align="center">{{ item.soLuong }}</td>
                  <td align="center">24 Tháng (Kể từ ngày mua)</td>
                  <td align="center" style="color: #27ae60; font-weight: bold;">ĐÃ XÁC THỰC</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Chính sách cam kết độc quyền Velora -->
          <div class="print-section">
            <h3>III. CHÍNH SÁCH BẢO HÀNH VELORA CARE+:</h3>
            <div style="font-size: 12px; line-height: 1.6; border: 1px dashed #cca15e; padding: 10px; background: #faf8f5;">
              <p style="margin: 0 0 4px 0;">• <strong>Phạm vi bảo hành:</strong> Miễn phí 100% chi phí sửa chữa, thay thế linh kiện chính hãng đối với các lỗi phát sinh do nhà sản xuất (bộ máy, IC, độ chịu nước theo tiêu chuẩn).</p>
              <p style="margin: 0 0 4px 0;">• <strong>Đặc quyền khách hàng:</strong> Thay pin miễn phí trọn đời cho dòng máy Quartz; kiểm tra độ chính xác và lau dầu định kỳ miễn phí trong thời gian bảo hành.</p>
              <p style="margin: 0;">• <strong>Tra cứu trực tuyến:</strong> Quý khách có thể truy cập mục <strong>"BẢO HÀNH"</strong> trên website <em>velora.com</em> và nhập mã đơn <strong>{{ selectedOrder.maDonHangCode }}</strong> để tra cứu hoặc đặt lịch hẹn trực tiếp với kỹ thuật viên.</p>
            </div>
          </div>

          <!-- Chữ ký & Con dấu -->
          <div class="print-signatures" style="margin-top: 20px;">
            <div class="sig-box">
              <strong>CHỦ SỞ HỮU</strong><br>
              <em class="sig-note">(Ký và ghi rõ họ tên)</em>
            </div>
            <div class="sig-box">
              <strong>GIÁM ĐỐC TRUNG TÂM BẢO HÀNH VELORA</strong><br>
              <em class="sig-note">(Ký, đóng dấu kiểm định chuẩn)</em>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- =========================================================================
         [PHẦN 5: POPUP MODAL HỦY ĐƠN & TOAST THÔNG BÁO]
    ========================================================================== -->
    <!-- Toast thông báo nhanh góc màn hình -->
    <div class="stock-toast" :class="{ 'show': showToast }">
      <i class="fas fa-info-circle"></i> {{ toastMessage }}
    </div>

    <!-- Modal xác nhận & nhập lý do hủy đơn -->
    <div class="velora-modal-overlay" v-if="showModal">
      <div class="velora-modal-box">
        <h3 class="velora-modal-title">{{ modalTitle }}</h3>
        <p class="velora-modal-msg">{{ modalMessage }}</p>

        <!-- Ô nhập lý do hủy đơn -->
        <div v-if="modalType === 'cancel'" style="margin-bottom: 20px;">
          <textarea 
            v-model="cancelReasonInput" 
            placeholder="Vui lòng nhập lý do hủy đơn hàng (Bắt buộc)..."
            class="velora-textarea"
          ></textarea>
        </div>

        <div class="velora-modal-actions">
          <button 
            v-if="modalType === 'confirm' || modalType === 'cancel'" 
            @click="handleModalClose"
            class="velora-btn-secondary"
          >
            Hủy
          </button>
          <button @click="handleModalConfirm" class="velora-btn-primary">
            {{ modalType === 'confirm' || modalType === 'cancel' ? 'Xác Nhận' : 'Đồng Ý' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Header from '../Header.vue'
import Footer from '../Footer.vue'

// Import thư viện WebSocket nhận tín hiệu Realtime
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// =========================================================================
// [LOGIC 1: KHỞI TẠO BIẾN DỮ LIỆU & CẤU HÌNH PHÂN TRANG]
// =========================================================================
const router = useRouter()
const orders = ref([])
const selectedOrder = ref(null)

const currentTab = ref('ALL')
const currentPage = ref(1)

// Chế độ in ấn: 'invoice' (in biên lai) hoặc 'warranty' (in giấy bảo hành)
const printMode = ref('invoice')

// DÒNG MẶC ĐỊNH: Số lượng đơn hàng hiển thị trên 1 trang (4 đơn)
const itemsPerPage = 4

// Danh sách các nút Tab lọc trạng thái
const tabs = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Chờ xử lý', value: 'CHO_XU_LY' },
  { label: 'Chuẩn bị hàng', value: 'CHUAN_BI_HANG' },
  { label: 'Đang vận chuyển', value: 'DANG_GIAO' },
  { label: 'Đã giao', value: 'DA_GIAO' },
  { label: 'Yêu cầu hủy', value: 'YEU_CAU_HUY' },
  { label: 'Đã hủy', value: 'DA_HUY' },
  { label: 'Hoàn tiền', value: 'HOAN_TIEN' }
]

// =========================================================================
// [LOGIC 2: COMPUTED FILTER & PHÂN TRANG]
// =========================================================================
// Lọc đơn hàng theo tab đang chọn
const filteredOrders = computed(() => {
  if (currentTab.value === 'ALL') return orders.value
  if (currentTab.value === 'HOAN_TIEN') {
    return orders.value.filter(o => ['YEU_CAU_HOAN_TIEN', 'TU_CHOI_HOAN_TIEN', 'DA_DUYET_HOAN_TIEN', 'HOAN_TIEN'].includes(o.trangThaiDonHang))
  }
  return orders.value.filter(order => order.trangThaiDonHang === currentTab.value)
})

// Tính tổng số trang
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / itemsPerPage);
})

// Cắt danh sách đơn hàng tương ứng với trang hiện tại
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredOrders.value.slice(start, start + itemsPerPage);
})

// Đếm số lượng đơn theo từng tab
const getCountByTab = (tabValue) => {
  if (tabValue === 'ALL') return orders.value.length
  if (tabValue === 'HOAN_TIEN') {
    return orders.value.filter(o => ['YEU_CAU_HOAN_TIEN', 'TU_CHOI_HOAN_TIEN', 'DA_DUYET_HOAN_TIEN', 'HOAN_TIEN'].includes(o.trangThaiDonHang)).length
  }
  return orders.value.filter(order => order.trangThaiDonHang === tabValue).length
}

// Khi chuyển tab: Reset về trang 1 và chọn đơn đầu tiên
watch(currentTab, () => {
  currentPage.value = 1;
  if (paginatedOrders.value.length > 0) {
    selectedOrder.value = paginatedOrders.value[0];
  } else {
    selectedOrder.value = null;
  }
})

// =========================================================================
// [LOGIC 3: QUẢN LÝ POPUP VÀ TOAST THÔNG BÁO]
// =========================================================================
const showModal = ref(false)
const modalTitle = ref('THÔNG BÁO')
const modalMessage = ref('')
const modalType = ref('alert')
const cancelReasonInput = ref('')
let modalResolveFn = null

const triggerModal = (message, title = 'THÔNG BÁO', type = 'alert') => {
  modalMessage.value = message
  modalTitle.value = title
  modalType.value = type
  cancelReasonInput.value = ''
  showModal.value = true
  return new Promise((resolve) => {
    modalResolveFn = resolve
  })
}

const handleModalConfirm = () => {
  if (modalType.value === 'cancel') {
    if (!cancelReasonInput.value.trim()) {
      triggerToast('Vui lòng nhập lý do hủy đơn hàng!')
      return
    }
    showModal.value = false
    if (modalResolveFn) modalResolveFn(cancelReasonInput.value)
  } else {
    showModal.value = false
    if (modalResolveFn) modalResolveFn(true)
  }
}

const handleModalClose = () => {
  showModal.value = false
  if (modalResolveFn) modalResolveFn(false)
}

const showToast = ref(false)
const toastMessage = ref('')
let toastTimer = null

const triggerToast = (msg) => {
  toastMessage.value = msg
  showToast.value = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { showToast.value = false }, 3500)
}

// Kiểm tra có phải đơn thanh toán trực tuyến hay không
const isOnlinePayment = (method) => {
  if (!method) return false;
  const m = method.toUpperCase();
  return m !== 'COD' && m !== 'THANH TOÁN COD';
}

// Kiểm tra đơn hàng ĐÃ THANH TOÁN (để mở khóa chức năng in giấy bảo hành)
const isPaidOrder = (order) => {
  if (!order) return false;
  const tt = order.trangThaiThanhToan;
  return tt === 'DA_THANH_TOAN' || tt === 'Đã thanh toán';
}

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// =========================================================================
// [LOGIC 4: GỌI API BACKEND & IN ẤN BẢO HÀNH]
// =========================================================================
const fetchUserOrders = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      await triggerModal('Vui lòng đăng nhập để xem lịch sử đơn hàng!', 'YÊU CẦU ĐĂNG NHẬP')
      router.push('/dang-nhap')
      return
    }
    
    const user = JSON.parse(userStr)
    const response = await fetch(`http://localhost:8080/api/don-hang/nguoi-dung/${user.maNguoiDung || user.id}`)
    
    if (response.ok) {
      const data = await response.json()
      
      orders.value = data.map(order => ({
        maDonHang: order.maDonHang,
        maDonHangCode: order.maDonHangCode,
        ngayTaoRaw: order.ngayTao, 
        ngayTao: new Date(order.ngayTao).toLocaleDateString('vi-VN'),
        tongTien: order.tongTien,
        tenNguoiNhan: order.tenNguoiNhan,
        soDienThoai: order.soDienThoaiGiaoHang,
        email: order.email || user.email || '', 
        diaChi: order.diaChiGiaoHang,
        trangThaiDonHang: order.trangThaiDonHang,
        trangThaiThanhToan: order.trangThaiThanhToan, // <--- BỔ SUNG TRƯỜNG THANH TOÁN
        phuongThucThanhToan: order.phuongThucThanhToan,
        lyDoHuy: order.lyDoHuy || order.ghiChu || '', 
        items: (order.chiTietDonHangs || []).map(ct => ({
          ten: ct.sanPham ? ct.sanPham.tenSanPham : 'Kiệt tác thời gian',
          maSanPham: ct.sanPham ? ct.sanPham.maSanPham : null,
          soLuong: ct.soLuong,
          gia: ct.giaLucMua,
          anh: ct.sanPham ? ct.sanPham.anhDaiDien : ''
        }))
      }))

      if (orders.value.length > 0) {
        if (selectedOrder.value) {
          const updatedSelected = orders.value.find(o => o.maDonHang === selectedOrder.value.maDonHang);
          selectedOrder.value = updatedSelected || paginatedOrders.value[0];
        } else {
          selectedOrder.value = paginatedOrders.value[0];
        }
      }
    }
  } catch (error) {
    console.error('Lỗi kết nối API đơn hàng:', error)
  }
}

// Chuyển hướng sang trang Bảo hành
const goToWarrantyPage = (order) => {
  localStorage.setItem('selectedWarrantyOrder', JSON.stringify(order))
  router.push('/bao-hanh')
}

// Kích hoạt in Giấy Xác Nhận Bảo Hành Chính Hãng
const printWarrantyPaper = async (order) => {
  if (!isPaidOrder(order)) {
    triggerToast('Chỉ những đơn hàng đã thanh toán mới được cấp giấy bảo hành chính hãng!')
    return
  }
  printMode.value = 'warranty'
  await nextTick()
  window.print()
}

// Kích hoạt in Biên lai bàn giao kiệt tác
const printInvoice = async () => {
  printMode.value = 'invoice'
  await nextTick()
  window.print()
}

// Kết nối WebSocket nhận thông báo cập nhật đơn Realtime
const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws-chat');
  const stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/orders', (message) => {
        if (message.body === 'RELOAD_ORDERS') {
          fetchUserOrders(); 
        }
      });
    }
  });
  stompClient.activate();
};

const selectOrder = (order) => {
  selectedOrder.value = order
}

// Gửi yêu cầu hủy đơn hàng
const cancelOrder = async (order) => {
  if (order.trangThaiDonHang !== 'CHO_XU_LY') {
    await triggerModal('Đơn hàng đã được duyệt, không thể hủy. Vui lòng liên hệ Hotline!', 'THÔNG BÁO')
    return
  }

  const reason = await triggerModal('Vui lòng cho biết lý do bạn muốn hủy đơn hàng này:', 'XÁC NHẬN YÊU CẦU HỦY', 'cancel')
  if (!reason) return

  try {
    const res = await fetch(`http://localhost:8080/api/don-hang/${order.maDonHang}/trang-thai?trangThaiMoi=YEU_CAU_HUY&lyDo=${encodeURIComponent(reason)}`, {
      method: 'PATCH'
    })

    if (res.ok) {
      order.trangThaiDonHang = 'YEU_CAU_HUY'
      triggerToast('Đã gửi yêu cầu hủy đơn thành công! Vui lòng chờ bộ phận CSKH xác nhận.')
      fetchUserOrders();
    } else {
      const errText = await res.text()
      await triggerModal(errText || 'Không thể gửi yêu cầu hủy lúc này!', 'THÔNG BÁO')
    }
  } catch (error) {
    await triggerModal('Lỗi kết nối đến máy chủ!', 'LỖI')
  }
}

// Chuyển hướng sang trang Hoàn tiền
const goToRefundPage = (order) => {
  localStorage.setItem('selectedRefundOrder', JSON.stringify(order))
  router.push('/yeu-cau-hoan-tien')
}

// Ánh xạ trạng thái sang cấp độ tiến trình
const getStepLevel = (status) => {
  switch (status) {
    case 'CHO_XU_LY': return 1;
    case 'CHUAN_BI_HANG': return 2;
    case 'DANG_GIAO': return 3;
    case 'DA_GIAO': return 4;
    default: return 1;
  }
}

const isStepCompleted = (status, stepNumber) => { return getStepLevel(status) > stepNumber; }

const getStatusText = (status) => {
  if (!status) return ''
  const cleanStatus = status.trim().toUpperCase()
  const map = {
    'CHO_XU_LY': 'Đang chờ xử lý',
    'YEU_CAU_HUY': 'Đang chờ duyệt hủy',
    'CHUAN_BI_HANG': 'Đang chuẩn bị hàng',
    'DANG_GIAO': 'Đang vận chuyển',
    'DA_GIAO': 'Đã giao thành công',
    'DA_HUY': 'Đơn hàng đã hủy',
    'YEU_CAU_HOAN_TIEN': 'Yêu cầu hoàn tiền đã gửi',
    'TU_CHOI_HOAN_TIEN': 'Từ chối hoàn tiền',
    'DA_DUYET_HOAN_TIEN': 'Đã duyệt hoàn tiền',
    'HOAN_TIEN': 'Đang xử lý hoàn tiền'
  }
  return map[cleanStatus] || status
}

const getStatusClass = (status) => {
  if (!status) return ''
  const cleanStatus = status.trim().toUpperCase()
  const map = {
    'CHO_XU_LY': 'status-pending',
    'YEU_CAU_HUY': 'status-cancel-request',
    'CHUAN_BI_HANG': 'status-prep',
    'DANG_GIAO': 'status-shipping',
    'DA_GIAO': 'status-delivered',
    'DA_HUY': 'status-cancelled',
    'YEU_CAU_HOAN_TIEN': 'status-refund-sent',
    'TU_CHOI_HOAN_TIEN': 'status-refund-rejected',
    'DA_DUYET_HOAN_TIEN': 'status-refund-approved',
    'HOAN_TIEN': 'status-refund-sent'
  }
  return map[cleanStatus] || ''
}

onMounted(() => {
  fetchUserOrders();
  connectWebSocket();
})
</script>

<style scoped>
@import "../CSS/User/DonHang.css";

.container {
  max-width: 1450px !important;
}

/* =========================================================================
   [CSS NHÓM 1: BỐ CỤC CHUNG & THANH TAB LỌC]
========================================================================= */
.order-tabs-wrapper {
  margin-bottom: 35px;
  background-color: #ffffff;
  border: 1px solid #f0f0f0;
  padding: 4px 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.order-tabs {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  white-space: nowrap;
}

.tab-btn {
  background: none;
  border: none;
  padding: 18px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

.tab-btn:hover { color: #c5a880; }
.tab-btn.active { 
  color: #1a1a1a;
  font-weight: 700; 
}
.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0;
  width: 100%; height: 3px;
  background-color: #c5a880;
  border-radius: 3px 3px 0 0;
}

.tab-count {
  font-size: 12px;
  background-color: #f5f5f5;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 6px;
  color: #666;
  font-weight: 600;
}
.tab-btn.active .tab-count {
  background-color: rgba(197, 168, 128, 0.1);
  color: #c5a880;
}

.order-layout {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 35px;
  align-items: start;
}

.section-title {
  font-size: 13px;
  color: #888;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 20px;
}

/* =========================================================================
   [CSS NHÓM 2: DANH SÁCH ĐƠN HÀNG CỘT TRÁI & PHÂN TRANG]
========================================================================= */
.order-card {
  padding: 20px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
  background: #ffffff;
  margin-bottom: 12px;
}

.order-card:hover {
  border-color: #e5e7eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
}

.order-card.active {
  border-color: #c5a880;
  box-shadow: 0 4px 15px rgba(197, 168, 128, 0.15);
  background: #fffcf8;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.order-code { font-size: 14px; font-weight: 700; color: #1a1a1a; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.order-date { font-size: 12px; color: #888; }
.order-total { font-size: 15px; font-weight: 700; color: #c5a880; }
.order-status { font-size: 11px; padding: 4px 8px; font-weight: 700; border-radius: 4px; text-transform: uppercase; }

/* PHÂN TRANG */
.pagination-wrapper { display: flex; justify-content: center; gap: 8px; margin-top: 25px; }
.page-btn {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #eaeaea; 
  background: white; 
  border-radius: 6px;
  cursor: pointer; 
  color: #555; 
  font-weight: 600; 
  font-size: 13px;
  transition: all 0.2s ease;
}
.page-btn:hover:not(:disabled) { border-color: #c5a880; color: #c5a880; }
.page-btn.active { 
  background: #c5a880;
  color: white; 
  border-color: #c5a880; 
}
.page-btn:disabled { background: #f9f9f9; color: #ccc; cursor: not-allowed; border-color: #f0f0f0; }

/* =========================================================================
   [CSS NHÓM 3: KHUNG CHI TIẾT ĐƠN HÀNG CỘT PHẢI]
========================================================================= */
.detail-box {
  padding: 35px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #eaeaea;
  box-shadow: 0 4px 25px rgba(0,0,0,0.03);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 35px;
  padding-bottom: 25px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-header h2 {
  font-size: 18px;
  color: #1a1a1a;
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.4;
}

.detail-header h2 span {
  display: block; 
  color: #888;
  font-size: 13px;
  margin-top: 6px;
  font-family: Consolas, monospace;
  font-weight: normal;
}

.detail-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-actions button {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  cursor: pointer;
}

/* Các kiểu nút */
.btn-print { background: #fff; border: 1px solid #e5e7eb; color: #374151; }
.btn-print:hover { background: #f9f9f9; border-color: #d1d5db; }
.btn-refund-order { background: #fffaf0; border: 1px solid #fde68a; color: #b45309; }
.btn-refund-order:hover { background: #fef3c7; }
.btn-warranty-order { background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; }
.btn-cancel-order { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; }

/* Nút in giấy bảo hành sang trọng */
.btn-print-warranty {
  background: #1a1614;
  color: #cca15e;
  border: 1px solid #cca15e;
  font-weight: 700 !important;
}
.btn-print-warranty:hover {
  background: #cca15e;
  color: #1a1614;
  box-shadow: 0 4px 12px rgba(204, 161, 94, 0.3);
}

/* =========================================================================
   [CSS NHÓM 4: THANH TIẾN TRÌNH TIMELINE VẬN CHUYỂN]
========================================================================= */
.tracking-timeline {
  display: flex;
  align-items: flex-start; 
  justify-content: space-between;
  margin: 10px 0 40px 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 90px;
  position: relative;
  z-index: 2;
}

.step-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #d1d5db;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.step-line {
  flex-grow: 1;
  height: 2px;
  background: #e5e7eb;
  margin-top: 21px; 
  z-index: 1;
  transition: background 0.3s;
}

.step.active .step-icon {
  border-color: #c5a880;
  color: #c5a880;
  box-shadow: 0 0 0 4px rgba(197, 168, 128, 0.1);
}

.step.completed .step-icon {
  background: #c5a880;
  border-color: #c5a880;
  color: #fff;
}

.step-line.filled { background: #c5a880; }

.step p {
  font-size: 11px;
  font-weight: 700;
  color: #888;
  text-align: center;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.step.active p, .step.completed p { color: #1a1a1a; }

/* =========================================================================
   [CSS NHÓM 5: THÔNG TIN NHẬN HÀNG VÀ BẢN ĐỒ]
========================================================================= */
.info-map-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  margin-bottom: 40px;
}

.info-panel {
  background: #fbfbfb;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 25px;
}

.info-panel h3, .order-items h3 {
  font-size: 12px;
  color: #888;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eaeaea;
}

.info-panel p {
  font-size: 13px;
  margin-bottom: 10px;
  color: #333;
  line-height: 1.6;
}

.info-panel p strong {
  display: inline-block;
  width: 120px;
  color: #666;
  font-weight: 600;
}

.map-panel {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  min-height: 200px;
  position: relative;
}

/* =========================================================================
   [CSS NHÓM 6: BẢNG SẢN PHẨM TRONG ĐƠN HÀNG]
========================================================================= */
.order-items {
  margin-top: 20px;
}

.item-table {
  width: 100%;
  border-collapse: collapse;
}

.item-table td {
  padding: 15px 0;
  border-bottom: 1px solid #f9f9f9;
}

.item-table tr:last-child td { border-bottom: none; }

.td-img img {
  width: 65px;
  height: 65px;
  object-fit: contain;
  background: #fdfdfd;
  border-radius: 8px;
  padding: 4px;
  border: 1px solid #f0f0f0;
}

.td-name { padding-left: 18px; }

.td-name .p-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
  display: block;
}

.td-name .p-qty { font-size: 13px; color: #888; font-weight: 500; }

.td-price {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  text-align: right;
}

/* =========================================================================
   [CSS NHÓM 7: BADGE TRẠNG THÁI & MODAL HỦY ĐƠN]
========================================================================= */
.status-refund-sent { background-color: #fef3c7 !important; color: #b45309 !important; }
.status-refund-rejected { background-color: #fee2e2 !important; color: #b91c1c !important; }
.status-refund-approved { background-color: #dcfce7 !important; color: #15803d !important; }
.cancel-info-box { background: #fff9e6; border: 1px solid #fde68a; padding: 15px 20px; border-radius: 8px; margin-bottom: 25px; color: #b45309; }
.pending-cancel-badge { background-color: #fffbeb; color: #b45309; padding: 8px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; border: 1px solid #fde68a; display: flex; align-items: center; gap: 8px; }
.no-selection { text-align: center; color: #888; padding: 50px 0; }
.no-selection i { font-size: 40px; margin-bottom: 15px; color: #ddd; }
.empty-orders { text-align: center; padding: 80px 0; background: #fff; border-radius: 12px; border: 1px solid #eaeaea; }
.empty-icon { font-size: 50px; color: #eaeaea; margin-bottom: 20px; }
.btn-shopnow { display: inline-block; margin-top: 20px; padding: 12px 25px; background: #1a1a1a; color: #c5a880; font-weight: bold; border-radius: 6px; text-decoration: none; font-size: 13px; letter-spacing: 1px; transition: 0.3s; }
.btn-shopnow:hover { background: #c5a880; color: #1a1a1a; }

.velora-modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background-color: rgba(0, 0, 0, 0.85); display: flex; align-items: center; justify-content: center; z-index: 9999; backdrop-filter: blur(4px); }
.velora-modal-box { width: 100%; max-width: 450px; background: #141414; border: 1px solid rgba(197, 168, 128, 0.4); border-radius: 12px; padding: 30px; text-align: center; box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5); }
.velora-modal-title { font-size: 18px; font-weight: 600; color: #c5a880; margin-bottom: 12px; letter-spacing: 1px; }
.velora-modal-msg { color: #d1d5db; font-size: 14px; margin-bottom: 20px; line-height: 1.5; }
.velora-textarea { width: 100%; height: 100px; background: #1e1e1e; border: 1px solid #374151; border-radius: 8px; padding: 12px; color: white; font-size: 14px; resize: none; font-family: inherit; }
.velora-textarea:focus { border-color: #c5a880; outline: none; }
.velora-modal-actions { display: flex; justify-content: center; gap: 15px; }
.velora-btn-secondary { padding: 10px 20px; border-radius: 8px; border: 1px solid #4b5563; background: transparent; color: #d1d5db; font-weight: 500; cursor: pointer; transition: 0.2s; }
.velora-btn-secondary:hover { background: #1f2937; }
.velora-btn-primary { padding: 10px 24px; border-radius: 8px; border: none; background: #c5a880; color: #141414; font-weight: 600; cursor: pointer; transition: 0.2s; }
.velora-btn-primary:hover { background: #b0936d; }

/* =========================================================================
   [CSS NHÓM 8: CẤU HÌNH IN BIÊN LAI & BẢO HÀNH (@media print)]
========================================================================= */
.print-invoice-template,
.print-warranty-template { display: none; }

@media print {
  @page { margin: 0 !important; size: A4 portrait; }

  iframe, [id*="chat"], [class*="chat"], [class*="widget"], div[style*="position: fixed"], div[style*="position:fixed"] {
    display: none !important;
  }

  .velora-header, .order-content, footer, .velora-modal-overlay, .stock-toast {
    display: none !important;
  }

  body, html, #app, .order-page {
    background: white !important;
    margin: 0 !important; padding: 0 !important;
    width: 100% !important; height: 100vh !important;
    overflow: hidden !important;
  }

  /* Kích hoạt template in tương ứng */
  .print-invoice-template,
  .print-warranty-template {
    display: block !important;
    width: 100%; height: 100vh;
    padding: 8mm 10mm !important;
    box-sizing: border-box;
    font-family: "Times New Roman", Times, serif; 
    color: #1a1a1a;
    position: relative; z-index: 1;
  }

  /* Watermark logo in chìm */
  .print-invoice-template::before,
  .print-warranty-template::before {
    content: ""; position: absolute;
    top: 35%; left: 20%; width: 60%; height: 40%;
    background-image: url('/img/VeloraIcon.png');
    background-repeat: no-repeat; background-position: center; background-size: contain;
    opacity: 0.05; z-index: -1;
    print-color-adjust: exact; -webkit-print-color-adjust: exact;
  }

  .print-border-outer { 
    border: 2px solid #c5a880; 
    padding: 3px; 
    height: 100%; 
    box-sizing: border-box;
  }
  
  .print-border-inner { 
    border: 1px solid #c5a880; 
    padding: 15px 20px; 
    height: 100%; 
    box-sizing: border-box;
    display: block;
  }

  .print-header {
    display: flex; align-items: center; justify-content: space-between;
    border-bottom: 2px solid #c5a880;
    padding-bottom: 8px; margin-bottom: 10px;
  }
  .print-logo { width: 110px; text-align: left; }
  .print-logo img { width: 80px; height: auto; }
  
  .print-company-titles { flex: 1; text-align: right; }
  .company-name {
    color: #c5a880 !important; 
    print-color-adjust: exact; -webkit-print-color-adjust: exact;
    font-size: 20px; font-weight: 700; margin: 0 0 3px 0;
    text-transform: uppercase; letter-spacing: 1px;
  }
  .company-sub { color: #1a1a1a !important; font-size: 11px; margin: 0; font-weight: bold; text-transform: uppercase; }

  .print-meta-top {
    display: flex; justify-content: space-between; align-items: flex-start;
    font-size: 13px; margin-bottom: 8px;
  }
  .meta-left { line-height: 1.4; }
  .meta-right { text-align: right; line-height: 1.4; }

  .print-title-box { text-align: center; margin-bottom: 15px; }
  .print-title-box h2 {
    font-size: 20px; font-weight: bold; color: #1a1a1a;
    margin: 0; text-transform: uppercase; letter-spacing: 1px;
  }

  .print-section { margin-bottom: 12px; }
  .print-section h3 { font-size: 14px; font-weight: bold; margin-bottom: 6px; color: #1a1a1a !important; }

  .no-border-table { width: 100%; font-size: 13px; line-height: 1.5; }
  .no-border-table td { padding: 2px 0; vertical-align: top; color: #1a1a1a; }

  .bordered-table { width: 100%; border-collapse: collapse; font-size: 13px; }
  .bordered-table th, .bordered-table td { border: 1px solid #1a1a1a; padding: 5px 7px; color: #1a1a1a; }
  .bordered-table th {
    background-color: #fbfbfb !important;
    print-color-adjust: exact; -webkit-print-color-adjust: exact;
    font-weight: bold; text-align: center; color: #c5a880 !important;
  }

  .total-price { color: #c5a880 !important; print-color-adjust: exact; -webkit-print-color-adjust: exact; font-size: 14px; }

  .print-signatures { 
    display: flex; 
    justify-content: space-around; 
    margin-top: 20px; 
    text-align: center; 
    font-size: 14px; 
  }
  .sig-box strong { color: #1a1a1a; display: block; margin-top: 3px; }
  .sig-note { font-size: 12px; color: #555; }
}
</style>