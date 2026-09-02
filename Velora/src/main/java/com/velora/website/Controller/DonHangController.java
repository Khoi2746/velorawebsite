package com.velora.website.Controller;

import com.velora.website.Entity.DonHang;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Repository.DoanhThuNgayRepository;
import com.velora.website.Repository.DoanhThuThangRepository;
import com.velora.website.Request.SepayResponse;
import com.velora.website.Request.SePayWebhookDto;
import com.velora.website.Service.EmailService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// =========================================================================
// [PHẦN 1: CẤU HÌNH CONTROLLER & DEPENDENCY INJECTION]
// =========================================================================
@RestController                                                         // Khai báo lớp Controller phục vụ RESTful Web Services
@RequestMapping("/api/don-hang")                                         // Tiền tố URL gốc cho toàn bộ các thao tác xử lý đơn hàng
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}) // Cấu hình CORS mở rộng đầy đủ các phương thức HTTP
@RequiredArgsConstructor                                                 // Tự động tạo Constructor tiêm các Repository & Service
public class DonHangController {

    // --- Các thành phần được tiêm vào Controller ---
    private final DonHangRepository donHangRepository;                   // Thao tác bảng DonHang trong CSDL
    private final EmailService emailService;                             // Dịch vụ gửi Email tự động
    private final SimpMessagingTemplate messagingTemplate;               // WebSocket bắn tín hiệu cập nhật thời gian thực (Realtime)
    
    private final DoanhThuNgayRepository doanhThuNgayRepository;         // Thao tác lưu doanh thu theo ngày
    private final DoanhThuThangRepository doanhThuThangRepository;       // Thao tác lưu doanh thu theo tháng

    // DÒNG MẶC ĐỊNH: Địa chỉ Email nhận thông báo của Quản trị viên (Đã cập nhật chính xác tài khoản mới)
    private static final String ADMIN_EMAIL = "veloraclock@gmail.com"; 
    // THAY THẾ: Đổi sang địa chỉ Email thực tế khác của cửa hàng:
    // private static final String ADMIN_EMAIL = "cskh.velorawatch@gmail.com";

    // =========================================================================
    // [PHẦN 2: CÁC API TRUY VẤN VÀ KIỂM TRA TRẠNG THÁI ĐƠN HÀNG]
    // =========================================================================

    /**
     * API 1: Lấy toàn bộ danh sách đơn hàng trong hệ thống (Dành cho trang Admin)
     * Endpoint: GET /api/don-hang
     */
    @GetMapping
    public ResponseEntity<List<DonHang>> getAllDonHang() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }

    /**
     * API 2: Lấy danh sách lịch sử đơn hàng của một người dùng cụ thể (Sắp xếp mới nhất lên đầu)
     * Endpoint: GET /api/don-hang/nguoi-dung/{maNguoiDung}
     */
    @GetMapping("/nguoi-dung/{maNguoiDung}")
    public ResponseEntity<List<DonHang>> getDonHangByNguoiDung(@PathVariable Integer maNguoiDung) {
        return ResponseEntity.ok(donHangRepository.findByMaNguoiDungOrderByMaDonHangDesc(maNguoiDung));
    }

    /**
     * API 3: Kiểm tra trạng thái thanh toán của đơn hàng theo mã code (Dùng cho trang quét mã QR)
     * Endpoint: GET /api/don-hang/check-status?code=VELORA12345
     */
    @GetMapping("/check-status")
    public ResponseEntity<?> checkDonHangPaidStatus(@RequestParam String code) {
        // Chuẩn hóa mã đơn: Viết hoa và loại bỏ ký tự đặc biệt
        String codeClean = code.toUpperCase().replaceAll("[^A-Z0-9]", "");

        List<DonHang> allOrders = donHangRepository.findAll();
        DonHang found = null;
        for (DonHang dh : allOrders) {
            if (dh.getMaDonHangCode() != null) {
                String dbCodeClean = dh.getMaDonHangCode().toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (dbCodeClean.equals(codeClean)) {
                    found = dh;
                    break;
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        if (found != null) {
            // Kiểm tra trạng thái đã hoàn tất thanh toán hay chưa
            boolean isPaid = "DA_THANH_TOAN".equalsIgnoreCase(found.getTrangThaiThanhToan()) 
                          || "Đã thanh toán".equalsIgnoreCase(found.getTrangThaiThanhToan());
            
            response.put("paid", isPaid);
            response.put("status", found.getTrangThaiThanhToan());
            return ResponseEntity.ok(response);
        }
        
        response.put("paid", false);
        response.put("message", "Không tìm thấy mã đơn hàng");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================================================
    // [PHẦN 3: XỬ LÝ WEBHOOK TỰ ĐỘNG TỪ CỔNG THANH TOÁN NGÂN HÀNG (SEPAY)]
    // =========================================================================

    /**
     * API 4: Nhận Webhook biến động số dư từ ngân hàng (SePay), tự động đối soát và khớp tiền
     * Endpoint: POST /api/don-hang/webhook-sepay
     */
    @PostMapping("/webhook-sepay")
    @Transactional                                                       // Đảm bảo cập nhật trạng thái đơn, kho và doanh thu đồng bộ
    public ResponseEntity<SepayResponse> receiveBankWebhook(@RequestBody SePayWebhookDto webhookData) {
        try {
            // Lấy số tiền thực tế nhận được từ giao dịch chuyển khoản
            Double moneyReceived = 0.0;
            if (webhookData.getTransferAmount() != null && webhookData.getTransferAmount() > 0) {
                moneyReceived = webhookData.getTransferAmount();
            } else if (webhookData.getAmountIn() != null && webhookData.getAmountIn() > 0) {
                moneyReceived = webhookData.getAmountIn();
            }

            if (moneyReceived > 0) {
                String noiDung = webhookData.getContent();
                if (noiDung != null) {
                    // Dùng biểu thức chính quy (Regex) quét tìm cú pháp mã đơn trong nội dung chuyển khoản
                    Pattern pattern = Pattern.compile("VELORA-?\\d+", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(noiDung);

                    String codeTimDuoc = null;
                    if (matcher.find()) {
                        codeTimDuoc = matcher.group().toUpperCase().replaceAll("[^A-Z0-9]", "");
                    }

                    if (codeTimDuoc != null) {
                        List<DonHang> allOrders = donHangRepository.findAll();
                        DonHang donHangKhop = null;

                        for (DonHang dh : allOrders) {
                            if (dh.getMaDonHangCode() != null) {
                                String dbCodeClean = dh.getMaDonHangCode().toUpperCase().replaceAll("[^A-Z0-9]", "");
                                if (dbCodeClean.equals(codeTimDuoc)) {
                                    donHangKhop = dh;
                                    break;
                                }
                            }
                        }

                        // Nếu tìm thấy đơn hàng trùng khớp nội dung chuyển tiền
                        if (donHangKhop != null) {
                            donHangKhop.setTrangThaiThanhToan("DA_THANH_TOAN"); // Đổi trạng thái tiền: ĐÃ THANH TOÁN
                            donHangKhop.setTrangThaiDonHang("CHO_XU_LY");       // Đổi trạng thái đơn: CHỜ XỬ LÝ
                            donHangRepository.save(donHangKhop);

                            // Cập nhật số liệu vào bảng báo cáo doanh thu
                            capNhatBangThongKe(donHangKhop);
                            // Bắn WebSocket thông báo Admin Dashboard cập nhật lại biểu đồ doanh thu
                            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");

                            // Trừ số lượng tồn kho của sản phẩm vừa bán
                            try {
                                donHangRepository.truSoLuongTonKhoTheoMaDon(donHangKhop.getMaDonHang());
                            } catch (Exception ex) {
                                System.out.println("⚠️ Lỗi trừ kho: " + ex.getMessage());
                            }

                            // [BỔ SUNG MỚI]: Bắn Email tự động báo Admin khi tiền SePay đã nổi vào tài khoản (Kèm ghi chú)
                            try {
                                String ghiChuSePay = (donHangKhop.getGhiChuDonHang() != null && !donHangKhop.getGhiChuDonHang().trim().isEmpty()) 
                                                     ? donHangKhop.getGhiChuDonHang().trim() : "Không có";

                                String subjectAdminSePay = "[SEPAY - ĐÃ NHẬN TIỀN] ĐƠN HÀNG #" + donHangKhop.getMaDonHangCode();
                                String contentAdminSePay = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> #" + donHangKhop.getMaDonHangCode() + "</p>"
                                    + "<p style='margin:0 0 6px 0;'><strong>• Số tiền nhận được:</strong> <span style='color: #27ae60; font-weight: bold;'>" + String.format("%,.0f", moneyReceived) + " VND</span></p>"
                                    + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + donHangKhop.getTenNguoiNhan() + " (" + donHangKhop.getEmail() + ")</p>"
                                    + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ nhận hàng:</strong> " + donHangKhop.getDiaChiGiaoHang() + "</p>"
                                    + "<p style='margin:0 0 6px 0; color: #cca15e;'><strong>• Ghi chú của khách:</strong> <em>" + ghiChuSePay + "</em></p>"
                                    + "<p style='margin:0;'><strong>• Trạng thái tiền:</strong> ĐÃ THANH TOÁN TỰ ĐỘNG QUA QR NGÂN HÀNG</p>";

                                String htmlAdminSePay = taoEmailHtmlVelora(
                                    "GIAO DỊCH CHUYỂN KHOẢN THÀNH CÔNG",
                                    "Thông báo Quản Trị Viên,",
                                    contentAdminSePay,
                                    "Đơn hàng đã được đối soát thành công. Vui lòng vào trang quản trị chuẩn bị kiệt tác để giao hàng."
                                );

                                emailService.sendEmail(ADMIN_EMAIL, subjectAdminSePay, htmlAdminSePay);
                            } catch (Exception exMail) {
                                System.err.println("Lỗi gửi mail thông báo SePay cho Admin: " + exMail.getMessage());
                            }
                        }
                    }
                }
            }
            return ResponseEntity.ok(new SepayResponse(true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SepayResponse(false));
        }
    }

    // =========================================================================
    // [PHẦN 4: CẬP NHẬT TRẠNG THÁI & HỦY ĐƠN HÀNG]
    // =========================================================================

    /**
     * API 5: Cập nhật trạng thái giao hàng, trạng thái thanh toán hoặc hủy đơn hàng
     * Endpoint: PATCH /api/don-hang/{id}/trang-thai
     */
    @PatchMapping("/{id}/trang-thai")
    @Transactional
    public ResponseEntity<?> capNhatTrangThaiHoacHuy(
            @PathVariable Integer id, 
            @RequestParam String trangThaiMoi,
            @RequestParam(required = false) String trangThaiThanhToanMoi,
            @RequestParam(required = false) String lyDo) {
        
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        String trangThaiCu = donHang.getTrangThaiDonHang();
        String thanhToanCu = donHang.getTrangThaiThanhToan();

        boolean wasThanhCongTruocDo = "DA_GIAO".equalsIgnoreCase(trangThaiCu) 
                                   || "DA_THANH_TOAN".equalsIgnoreCase(thanhToanCu);
        
        // Cập nhật trạng thái đơn hàng mới
        donHang.setTrangThaiDonHang(trangThaiMoi);

        // Lưu vết lý do nếu có
        if (lyDo != null && !lyDo.trim().isEmpty()) {
            donHang.setLyDoHuyDon(lyDo.trim());
        }

        // --- RÀNG BUỘC KHI HỦY ĐƠN ---
        // DÒNG MẶC ĐỊNH: Chỉ cho phép hủy khi ở trạng thái Chờ xử lý, Chuẩn bị hàng hoặc Đang chờ duyệt hủy
        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
            if (!"CHO_XU_LY".equalsIgnoreCase(trangThaiCu) 
                && !"CHUAN_BI_HANG".equalsIgnoreCase(trangThaiCu) 
                && !"YEU_CAU_HUY".equalsIgnoreCase(trangThaiCu)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chỉ được hủy đơn hàng ở trạng thái Chờ xử lý, Chuẩn bị hàng hoặc Đang chờ duyệt hủy!");
            }
        }
        // THAY THẾ: Cho phép hủy đơn ở bất kỳ trạng thái nào (Bỏ qua kiểm tra):
        // if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) { /* Cho phép hủy trực tiếp */ }

        // Cập nhật trạng thái thanh toán
        if (trangThaiThanhToanMoi != null && !trangThaiThanhToanMoi.trim().isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToanMoi);
        } else if ("DA_GIAO".equalsIgnoreCase(trangThaiMoi)) {
            donHang.setTrangThaiThanhToan("DA_THANH_TOAN");              // Nếu đã giao thành công thì tự động gán tiền Đã thanh toán
        }

        DonHang updatedDonHang = donHangRepository.save(donHang);

        boolean isGiaoThanhCong = "DA_GIAO".equalsIgnoreCase(trangThaiMoi) || "HOAN_TAT".equalsIgnoreCase(trangThaiMoi);
        boolean isThanhToanXong = "DA_THANH_TOAN".equalsIgnoreCase(updatedDonHang.getTrangThaiThanhToan());

        // Nếu đơn hàng chuyển sang giao thành công lần đầu thì cập nhật doanh thu
        if ((isGiaoThanhCong || isThanhToanXong) && !wasThanhCongTruocDo) {
            capNhatBangThongKe(updatedDonHang);
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");
        }

        // Gửi email tự động thông báo hủy đơn cho khách hàng và Admin
        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
            guiEmailThongBaoHuyDon(updatedDonHang, lyDo);
        }

        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }

    /**
     * API 5.1: Cập nhật trực tiếp phương thức thanh toán của đơn hàng (COD, CHUYEN_KHOAN_QR, VNPAY, ...)
     * Endpoint: PATCH /api/don-hang/{id}/phuong-thuc-thanh-toan?phuongThucMoi=...
     * Mục đích: Lưu vĩnh viễn lựa chọn thay đổi từ dropdown của Admin vào CSDL, không bị hoàn tác khi F5
     */
    @PatchMapping("/{id}/phuong-thuc-thanh-toan")
    @Transactional
    public ResponseEntity<?> capNhatPhuongThucThanhToan(
            @PathVariable Integer id,
            @RequestParam String phuongThucMoi) {
        
        // 1. Kiểm tra sự tồn tại của đơn hàng trong Database
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng cần cập nhật phương thức!");
        }

        DonHang donHang = optionalDonHang.get();

        // 2. Kiểm tra chuỗi phương thức thanh toán mới
        if (phuongThucMoi == null || phuongThucMoi.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: Phương thức thanh toán không được để trống!");
        }

        // 3. Ghi đè phương thức thanh toán mới và lưu xuống CSDL
        donHang.setPhuongThucThanhToan(phuongThucMoi.trim());
        donHangRepository.save(donHang);

        System.out.println("✅ [ADMIN] Đã cập nhật phương thức thanh toán đơn #" + donHang.getMaDonHangCode() + " thành: " + phuongThucMoi.trim());
        return ResponseEntity.ok("Cập nhật phương thức thanh toán thành công!");
    }

    /**
     * API 6: Đường dẫn rút gọn để hủy đơn nhanh
     * Endpoint: PATCH /api/don-hang/{id}/huy
     */
    @PatchMapping("/{id}/huy")
    @Transactional
    public ResponseEntity<?> huyDonHangNhanh(@PathVariable Integer id, @RequestParam(required = false) String lyDo) {
        return capNhatTrangThaiHoacHuy(id, "DA_HUY", null, lyDo);
    }

    // =========================================================================
    // [PHẦN 5: NGHIỆP VỤ ĐẶT HÀNG (MUA NGAY & ĐẶT TỪ GIỎ HÀNG)]
    // =========================================================================

    /**
     * API 7: Xử lý chức năng "Mua Ngay" 1 sản phẩm trực tiếp từ trang chi tiết
     * Endpoint: POST /api/don-hang/dat-ngay
     */
    @PostMapping("/dat-ngay")
    @Transactional
    public ResponseEntity<?> datHangNhanh(@RequestBody DatNgayRequest payload) {
        try {
            // Giá trị mặc định dự phòng nếu chưa đăng nhập hoặc ID rỗng
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0) payload.setMaNguoiDung(3); 
            if (payload.getMaSanPham() == null || payload.getMaSanPham() <= 0) payload.setMaSanPham(2); 

            DonHang donHang = new DonHang();
            donHang.setMaNguoiDung(payload.getMaNguoiDung());
            donHang.setMaDonHangCode(payload.getMaDonHangCode());
            donHang.setTongTien(BigDecimal.valueOf(payload.getTongTien()));
            donHang.setTenNguoiNhan(payload.getTenNguoiNhan());
            donHang.setSoDienThoaiGiaoHang(payload.getSoDienThoaiGiaoHang());
            donHang.setEmail(payload.getEmail()); 
            donHang.setDiaChiGiaoHang(payload.getDiaChiGiaoHang());
            donHang.setPhuongThucThanhToan(payload.getPhuongThucThanhToan());
            donHang.setTrangThaiDonHang("CHO_XU_LY");                    // Trạng thái đơn ban đầu: CHỜ XỬ LÝ
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");            // Trạng thái tiền ban đầu: CHƯA THANH TOÁN
            
            if (payload.getGhiChuDonHang() != null && !payload.getGhiChuDonHang().trim().isEmpty()) {
                donHang.setGhiChuDonHang(payload.getGhiChuDonHang().trim());
            }

            // Lưu đơn hàng vào CSDL
            DonHang donHangDaLuu = donHangRepository.save(donHang);
            double giaLucMua = payload.getTongTien() / payload.getSoLuong();

            // Thêm chi tiết sản phẩm vào bảng ChiTietDonHang
            donHangRepository.insertChiTietDonHang(
                donHangDaLuu.getMaDonHang(), 
                payload.getMaSanPham(), 
                payload.getSoLuong(), 
                giaLucMua
            );

            // Xóa sản phẩm khỏi giỏ hàng nếu đã mua ngay
            donHangRepository.xoaSanPhamKhoiGioHang(payload.getMaNguoiDung(), payload.getMaSanPham());
            
            // Gửi email xác nhận đặt hàng thành công (ĐÃ BỔ SUNG TRUYỀN GHI CHÚ ĐƠN HÀNG)
            guiEmailXacNhanDatHang(
                payload.getEmail(), 
                payload.getMaDonHangCode(), 
                payload.getTenNguoiNhan(), 
                payload.getTongTien(), 
                payload.getDiaChiGiaoHang(),
                payload.getGhiChuDonHang()
            );

            return ResponseEntity.ok("Đặt hàng kiệt tác thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API 8: Xử lý đặt toàn bộ sản phẩm có trong giỏ hàng
     * Endpoint: POST /api/don-hang/dat-gio-hang
     */
    @PostMapping("/dat-gio-hang")
    @Transactional
    public ResponseEntity<?> datHangTuGioHang(@RequestBody DatGioHangRequest payload) {
        try {
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0) payload.setMaNguoiDung(3);

            DonHang donHang = new DonHang();
            donHang.setMaNguoiDung(payload.getMaNguoiDung());
            donHang.setMaDonHangCode(payload.getMaDonHangCode());
            donHang.setTongTien(BigDecimal.valueOf(payload.getTongTien()));
            donHang.setTenNguoiNhan(payload.getTenNguoiNhan());
            donHang.setSoDienThoaiGiaoHang(payload.getSoDienThoaiGiaoHang());
            donHang.setEmail(payload.getEmail()); 
            donHang.setDiaChiGiaoHang(payload.getDiaChiGiaoHang());
            donHang.setPhuongThucThanhToan(payload.getPhuongThucThanhToan());
            donHang.setTrangThaiDonHang("CHO_XU_LY");
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");

            if (payload.getGhiChuDonHang() != null && !payload.getGhiChuDonHang().trim().isEmpty()) {
                donHang.setGhiChuDonHang(payload.getGhiChuDonHang().trim());
            }

            DonHang donHangDaLuu = donHangRepository.save(donHang);
            // Chuyển toàn bộ danh sách trong GioHang sang ChiTietDonHang
            donHangRepository.chuyenGioHangSangChiTietDonHang(donHangDaLuu.getMaDonHang(), payload.getMaNguoiDung());
            // Xóa sạch giỏ hàng của người dùng sau khi đã tạo đơn
            donHangRepository.xoaToanBoGioHangCuaUser(payload.getMaNguoiDung());

            // Gửi email xác nhận đặt hàng (ĐÃ BỔ SUNG TRUYỀN GHI CHÚ ĐƠN HÀNG)
            guiEmailXacNhanDatHang(
                payload.getEmail(), 
                payload.getMaDonHangCode(), 
                payload.getTenNguoiNhan(), 
                payload.getTongTien(), 
                payload.getDiaChiGiaoHang(),
                payload.getGhiChuDonHang()
            );

            return ResponseEntity.ok("Đặt hàng giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lý giỏ hàng: " + e.getMessage());
        }
    }

    // =========================================================================
    // [PHẦN 6: HÀM NỘI BỘ TỰ ĐỘNG TÍNH DOANH THU & GỬI EMAIL THÔNG BÁO]
    // =========================================================================

    /**
     * Tự động cộng dồn doanh thu ngày và doanh thu tháng khi có đơn hàng thành công
     */
    private void capNhatBangThongKe(DonHang donHang) {
        LocalDate ngayHienTai = LocalDate.now();
        int thang = ngayHienTai.getMonthValue();
        int nam = ngayHienTai.getYear();

        BigDecimal tienDonHang = donHang.getTongTien();
        
        int soLuongSp = 0;
        StringBuilder tenCacSanPham = new StringBuilder();
        
        // Tính tổng số lượng sản phẩm và danh sách tên sản phẩm trong đơn
        if (donHang.getChiTietDonHangs() != null) {
            for (com.velora.website.Entity.ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
                soLuongSp += ct.getSoLuong();
                if (ct.getSanPham() != null) {
                    if (tenCacSanPham.length() > 0) tenCacSanPham.append(", ");
                    tenCacSanPham.append(ct.getSanPham().getTenSanPham()).append(" (x").append(ct.getSoLuong()).append(")");
                }
            }
        }
        if (soLuongSp == 0) soLuongSp = 1;

        // 1. Cập nhật bảng DoanhThuNgay
        com.velora.website.Entity.DoanhThuNgay dtNgay = doanhThuNgayRepository.findByNgayChinhXac(ngayHienTai);
        if (dtNgay == null) {
            dtNgay = new com.velora.website.Entity.DoanhThuNgay();
            dtNgay.setNgay(ngayHienTai);
            dtNgay.setTongDoanhThu(BigDecimal.ZERO);
            dtNgay.setSoDonHangThanhCong(0);
            dtNgay.setSoSanPhamBanRa(0);
            dtNgay.setDanhSachSanPham("");
        }
        
        dtNgay.setTongDoanhThu(dtNgay.getTongDoanhThu().add(tienDonHang));
        dtNgay.setSoDonHangThanhCong(dtNgay.getSoDonHangThanhCong() + 1);
        dtNgay.setSoSanPhamBanRa(dtNgay.getSoSanPhamBanRa() + soLuongSp);
        
        String dsCu = dtNgay.getDanhSachSanPham() != null ? dtNgay.getDanhSachSanPham() : "";
        if (dsCu.isEmpty()) {
            dtNgay.setDanhSachSanPham(tenCacSanPham.toString());
        } else {
            dtNgay.setDanhSachSanPham(dsCu + ", " + tenCacSanPham.toString());
        }
        
        doanhThuNgayRepository.save(dtNgay);

        // 2. Cập nhật bảng DoanhThuThang
        com.velora.website.Entity.DoanhThuThang dtThang = doanhThuThangRepository.findByThangVaNamChinhXac(thang, nam);
        if (dtThang == null) {
            dtThang = new com.velora.website.Entity.DoanhThuThang();
            dtThang.setThang(thang);
            dtThang.setNam(nam);
            dtThang.setTongDoanhThu(BigDecimal.ZERO);
            dtThang.setSoDonHangThanhCong(0);
            dtNgay.setSoSanPhamBanRa(0);
        }
                
        dtThang.setTongDoanhThu(dtThang.getTongDoanhThu().add(tienDonHang));
        dtThang.setSoDonHangThanhCong(dtThang.getSoDonHangThanhCong() + 1);
        dtThang.setSoSanPhamBanRa(dtThang.getSoSanPhamBanRa() + soLuongSp);
        doanhThuThangRepository.save(dtThang);
    }

    /**
     * Dựng giao diện Email HTML phong cách Velora Luxury
     */
    private String taoEmailHtmlVelora(String tieuDeChinh, String loiChao, String noiDungChiTiet, String ghiChuFooter) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='UTF-8'></head>"
            + "<body style='margin: 0; padding: 35px 15px; background-color: #0d0805; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
            + "  <div style='max-width: 540px; margin: 0 auto; background-color: #1a0f0a; border: 1px solid #4a3423; border-top: 4px solid #cca15e; box-shadow: 0 15px 40px rgba(0,0,0,0.85);'>"
            + "    <div style='padding: 35px 25px 20px; text-align: center; border-bottom: 1px solid #2d1c12;'>"
            + "      <h1 style='margin: 0; color: #cca15e; font-size: 24px; letter-spacing: 4px; font-weight: 700; text-transform: uppercase;'>VELORA CLOCK</h1>"
            + "      <p style='margin: 6px 0 0; color: #b5a99a; font-size: 11px; letter-spacing: 2px; text-transform: uppercase;'>Thế giới kiệt tác thời gian</p>"
            + "    </div>"
            + "    <div style='padding: 30px 32px; color: #dedede;'>"
            + "      <h2 style='margin-top: 0; color: #cca15e; font-size: 17px; letter-spacing: 1.5px; text-transform: uppercase; border-bottom: 1px solid #2d1c12; padding-bottom: 12px; text-align: center;'>" + tieuDeChinh + "</h2>"
            + "      <p style='font-size: 14px; line-height: 1.6; color: #ffffff;'><strong>" + loiChao + "</strong></p>"
            + "      <div style='background-color: #24140d; border: 1px solid #3d2417; padding: 20px; margin: 20px 0; font-size: 14px; line-height: 1.8; color: #d6d0c7;'>"
            +          noiDungChiTiet
            + "      </div>"
            + "      <p style='font-size: 13px; line-height: 1.6; color: #a39686; margin-top: 20px;'>" + ghiChuFooter + "</p>"
            + "    </div>"
            + "    <div style='padding: 20px; text-align: center; background-color: #120905; border-top: 1px solid #24140c; font-size: 11px; color: #706354; letter-spacing: 0.5px;'>"
            + "      <p style='margin: 0;'>© 2026 VELORA BOUTIQUE. MỌI QUYỀN ĐƯỢC BẢO LƯU.</p>"
            + "      <p style='margin: 4px 0 0;'>Hotline hỗ trợ: 1900 xxxx | TP. Hồ Chí Minh</p>"
            + "    </div>"
            + "  </div>"
            + "</body></html>";
    }

    /**
     * Gửi email xác nhận sau khi khách hàng hoàn tất đặt đơn mới (ĐÃ BỔ SUNG GHI CHÚ ĐƠN HÀNG)
     */
    private void guiEmailXacNhanDatHang(String emailKhach, String maCode, String tenKhach, Double tongTien, String diaChi, String ghiChu) {
        try {
            // Chuẩn hóa chuỗi ghi chú: Nếu để trống thì hiển thị 'Không có'
            String ghiChuHienThi = (ghiChu != null && !ghiChu.trim().isEmpty()) ? ghiChu.trim() : "Không có";

            // 1. Gửi Email cho khách hàng
            String subject = "[VELORA CLOCK] XÁC NHẬN ĐƠN HÀNG #" + maCode;
            String contentKhach = "<p style='margin:0 0 10px 0;'>Cảm ơn quý khách đã tin tưởng và lựa chọn tuyệt tác thời gian tại Velora Clock.</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> <span style='color: #cca15e;'>#" + maCode + "</span></p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Tổng giá trị:</strong> <span style='color: #cca15e; font-weight: bold;'>" + String.format("%,.0f", tongTien) + " VND</span></p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ nhận hàng:</strong> " + diaChi + "</p>"
                + "<p style='margin:0;'><strong>• Ghi chú đơn hàng:</strong> <em>" + ghiChuHienThi + "</em></p>";
            
            String htmlKhach = taoEmailHtmlVelora(
                "XÁC NHẬN ĐƠN HÀNG THÀNH CÔNG", 
                "Kính chào quý khách " + (tenKhach != null ? tenKhach : "") + ",", 
                contentKhach, 
                "Hệ thống đang tiến hành đóng gói kiệt tác và sẽ sớm bàn giao cho đơn vị vận chuyển."
            );

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                emailService.sendEmail(emailKhach, subject, htmlKhach);
            }

            // 2. Gửi Email thông báo cho Quản trị viên
            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG MỚI CẦN XỬ LÝ #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> #" + maCode + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + " (" + emailKhach + ")</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Tổng tiền:</strong> " + String.format("%,.0f", tongTien) + " VND</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ:</strong> " + diaChi + "</p>"
                + "<p style='margin:0; color: #cca15e;'><strong>• Ghi chú của khách:</strong> " + ghiChuHienThi + "</p>";

            String htmlAdmin = taoEmailHtmlVelora(
                "CÓ ĐƠN HÀNG MỚI CẦN DUYỆT", 
                "Thông báo Quản Trị Viên,", 
                contentAdmin, 
                "Vui lòng truy cập trang Quản Trị Hệ Thống để kiểm tra kho và xác nhận đơn hàng."
            );

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email đặt hàng: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo hủy đơn kèm theo hướng dẫn hoàn tiền nếu đã thanh toán Online
     */
    private void guiEmailThongBaoHuyDon(DonHang donHang, String lyDo) {
        try {
            String emailKhach = donHang.getEmail();
            String maCode = donHang.getMaDonHangCode();
            String tenKhach = donHang.getTenNguoiNhan();
            String phuongThuc = donHang.getPhuongThucThanhToan();
            boolean isOnline = (phuongThuc != null && !phuongThuc.toUpperCase().contains("COD"));
            String ghiChuDon = (donHang.getGhiChuDonHang() != null && !donHang.getGhiChuDonHang().trim().isEmpty()) 
                               ? donHang.getGhiChuDonHang().trim() : "Không có";

            // 1. Gửi Email cho khách hàng
            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                String subjectKhach = "[VELORA CLOCK] THÔNG BÁO HỦY ĐƠN HÀNG #" + maCode;
                String contentKhach = "<p style='margin:0 0 10px 0;'>Yêu cầu hủy đơn hàng <strong>#" + maCode + "</strong> của quý khách đã được hệ thống ghi nhận thành công.</p>"
                    + "<p style='margin:0 0 10px 0; color: #e6a200;'><strong>• Lý do hủy:</strong> " + (lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Không có lý do cụ thể") + "</p>";
                
                if (isOnline) {
                    contentKhach += "<p style='margin:0; color: #2ecc71;'><strong>• Lưu ý hoàn tiền:</strong> Do đơn hàng thanh toán trực tuyến (" + phuongThuc + "), quý khách vui lòng vào mục <strong>'Đơn Hàng Của Bạn'</strong> trên website và nhấn <strong>'YÊU CẦU HOÀN TIỀN'</strong> để cung cấp số tài khoản nhận lại tiền.</p>";
                } else {
                    contentKhach += "<p style='margin:0;'>Đơn hàng COD đã được đóng thành công. Rất tiếc vì trải nghiệm chưa trọn vẹn lần này.</p>";
                }

                String htmlKhach = taoEmailHtmlVelora(
                    "THÔNG BÁO HỦY ĐƠN HÀNG", 
                    "Kính chào quý khách " + tenKhach + ",", 
                    contentKhach, 
                    "Hân hạnh được phục vụ quý khách trong các trải nghiệm mua sắm tiếp theo."
                );

                emailService.sendEmail(emailKhach, subjectKhach, htmlKhach);
            }

            // 2. Gửi Email cho Quản trị viên
            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG ĐÃ BỊ HỦY #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn:</strong> #" + maCode + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Thanh toán:</strong> " + phuongThuc + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Ghi chú ban đầu:</strong> " + ghiChuDon + "</p>"
                + "<p style='margin:0;'><strong>• Lý do hủy:</strong> " + (lyDo != null ? lyDo : "Không có") + "</p>";

            if (isOnline) {
                contentAdmin += "<p style='margin:10px 0 0 0; color: #e74c3c; font-weight: bold;'>⚠️ Đơn thanh toán Online: Kế toán sẵn sàng tiếp nhận hồ sơ hoàn tiền khi khách gửi thông tin.</p>";
            }

            String htmlAdmin = taoEmailHtmlVelora(
                "ĐƠN HÀNG ĐÃ BỊ HỦY", 
                "Thông báo Quản Trị Viên,", 
                contentAdmin, 
                "Dữ liệu trạng thái đơn hàng đã được cập nhật tự động trên hệ thống."
            );

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);

        } catch (Exception e) {
            System.err.println("Lỗi gửi email quy trình hủy đơn: " + e.getMessage());
        }
    }
}

// =========================================================================
// [PHẦN 7: CÁC DTO / REQUEST OBJECT NHẬN DỮ LIỆU TỪ CLIENT]
// =========================================================================

/**
 * Đối tượng nhận dữ liệu từ chức năng Mua Ngay
 */
@Data
class DatNgayRequest {
    private Integer maNguoiDung;         // ID người mua
    private String maDonHangCode;        // Mã code đơn hàng (VD: VELORA1001)
    private Double tongTien;             // Tổng tiền đơn hàng
    private String tenNguoiNhan;         // Họ tên người nhận
    private String soDienThoaiGiaoHang;  // SĐT giao hàng
    private String email;                // Email nhận thông báo
    private String diaChiGiaoHang;       // Địa chỉ nhận hàng
    private String phuongThucThanhToan;  // COD, VNPAY, CHUYEN_KHOAN_QR
    private Integer maSanPham;           // ID sản phẩm mua ngay
    private Integer soLuong;             // Số lượng đặt
    private String ghiChuDonHang;        // Ghi chú của khách
    private String maGiamGia;            // Mã khuyến mãi voucher (nếu có)
}

/**
 * Đối tượng nhận dữ liệu từ chức năng Đặt Giỏ Hàng
 */
@Data
class DatGioHangRequest {
    private Integer maNguoiDung;         // ID người mua
    private String maDonHangCode;        // Mã code đơn hàng
    private Double tongTien;             // Tổng tiền đơn hàng
    private String tenNguoiNhan;         // Họ tên người nhận
    private String soDienThoaiGiaoHang;  // SĐT giao hàng
    private String email;                // Email nhận thông báo
    private String diaChiGiaoHang;       // Địa chỉ nhận hàng
    private String phuongThucThanhToan;  // Phương thức thanh toán
    private String ghiChuDonHang;        // Ghi chú đơn hàng
    private String maGiamGia;            // Mã khuyến mãi voucher (nếu có)
}