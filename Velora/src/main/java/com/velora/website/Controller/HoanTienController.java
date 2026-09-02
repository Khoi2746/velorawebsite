package com.velora.website.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.website.Entity.DonHang;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.YeuCauHoanTien;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.YeuCauHoanTienRepository;
import com.velora.website.Service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// =========================================================================
// [PHẦN 1: KHAI BÁO CONTROLLER & DEPENDENCY INJECTION]
// =========================================================================
@RestController                                                          // Khai báo lớp này là REST API Controller trả về dữ liệu JSON
@RequestMapping("/api/hoan-tien")                                        // Tiền tố đường dẫn URL gốc cho toàn bộ API hoàn tiền
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true") // Cho phép Frontend (Vue.js/Vite) gọi API mà không bị chặn CORS
@RequiredArgsConstructor                                                 // Tự động tạo Constructor inject các Repository & Service qua Lombok
public class HoanTienController {

    // --- Các Service và Repository thao tác cơ sở dữ liệu ---
    private final EmailService emailService;                             // Service gửi email tự động (JavaMailSender)
    private final DonHangRepository donHangRepository;                   // Thao tác bảng DonHang trong CSDL
    private final NguoiDungRepository nguoiDungRepository;               // Thao tác bảng NguoiDung trong CSDL (Khóa tài khoản)
    private final YeuCauHoanTienRepository yeuCauHoanTienRepository;     // Thao tác bảng YeuCauHoanTien trong CSDL

    // --- Công cụ chuyển đổi JSON và bộ nhớ tạm lưu OTP ---
    private final ObjectMapper objectMapper = new ObjectMapper();         // Dùng để parse JSON danh sách ảnh sang String và ngược lại
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>(); // Bộ nhớ RAM an toàn đa luồng lưu tạm mã OTP theo Email

    // =========================================================================
    // [PHẦN 2: HÀM TIỆN ÍCH TẠO TEMPLATE EMAIL HTML CHUẨN VELORA]
    // =========================================================================
    /**
     * Hàm dựng giao diện Email HTML với tone màu nâu gỗ đen và vàng hoàng kim
     * @param tieuDeChinh Tiêu đề nổi bật trong khung thư
     * @param loiChao Lời chào gửi đến người nhận
     * @param noiDungChiTiet Nội dung thông báo chính
     * @param ghiChuFooter Ghi chú nhắc nhở ở cuối thư
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

    // =========================================================================
    // [PHẦN 3: API PHÍA KHÁCH HÀNG - GỬI MÃ OTP & XÁC THỰC YÊU CẦU]
    // =========================================================================

    /**
     * API 1: Tạo ngẫu nhiên mã OTP 6 số và gửi qua Gmail cho khách hàng
     * Endpoint: POST /api/hoan-tien/gui-otp?email=abc@gmail.com
     */
    @PostMapping("/gui-otp")
    public ResponseEntity<?> guiOtpXacNhan(@RequestParam String email) {
        // Kiểm tra tính hợp lệ của địa chỉ Email
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ!");
        }

        // --- DÒNG MẶC ĐỊNH: Tạo mã OTP 6 chữ số ngẫu nhiên từ 100000 đến 999999 ---
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        
        // THAY THẾ: Tạo mã OTP 4 chữ số ngắn gọn (từ 1000 đến 9999):
        // String otp = String.format("%04d", new Random().nextInt(9000) + 1000);
        
        // THAY THẾ: Mã OTP cố định để kiểm thử nhanh khi không có mạng (Ví dụ: 123456):
        // String otp = "123456";

        // Lưu mã OTP vào RAM theo địa chỉ Email đã viết thường
        otpStorage.put(email.trim().toLowerCase(), otp);

        // Dựng tiêu đề và nội dung HTML của Email thông báo mã OTP
        String subject = "[VELORA BOUTIQUE] MÃ OTP XÁC NHẬN YÊU CẦU HOÀN TIỀN";
        String content = "<p style='margin:0 0 10px 0;'>Mã xác thực OTP của quý khách là:</p>"
            + "<div style='text-align: center; margin: 15px 0;'><span style='font-size: 28px; font-weight: bold; letter-spacing: 8px; color: #cca15e; background: #120905; padding: 10px 24px; border: 1px dashed #cca15e;'>" + otp + "</span></div>"
            + "<p style='margin: 15px 0 5px 0; color: #e6a200; font-weight: bold;'>⚠️ QUY ĐỊNH HOÀN HÀNG VELORA:</p>"
            + "<p style='margin: 0; font-size: 12px; color: #bbb;'>- Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc lỗi do NSX.<br>- Hoàn hàng quá 6 lần/năm tài khoản sẽ bị khóa.</p>";

        String htmlBody = taoEmailHtmlVelora("XÁC THỰC MÃ OTP HOÀN TIỀN", "Kính chào quý khách,", content, "Vui lòng không cung cấp mã OTP này cho bất kỳ ai.");
        
        // Gửi email thực tế
        emailService.sendEmail(email.trim(), subject, htmlBody);
        return ResponseEntity.ok("Mã OTP xác thực đã được gửi về Gmail!");
    }

    /**
     * API 2: Xác nhận mã OTP, kiểm tra số lượng ảnh và lưu bản ghi YeuCauHoanTien
     * Endpoint: POST /api/hoan-tien/xac-nhan-yeu-cau
     */
    @PostMapping("/xac-nhan-yeu-cau")
    @Transactional                                                       // Đảm bảo cập nhật đơn hàng và thêm yêu cầu diễn ra đồng bộ
    public ResponseEntity<?> guiYeuCauHoanTien(@RequestBody YeuCauHoanTienRequest request) {
        // Kiểm tra Email rỗng
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email đặt hàng bị trống!");
        }

        String emailClean = request.getEmail().trim().toLowerCase();

        // Kiểm tra mã OTP người dùng nhập có khớp với OTP trong RAM không
        if (!otpStorage.containsKey(emailClean) || !otpStorage.get(emailClean).equals(request.getOtpCode())) {
            return ResponseEntity.badRequest().body("Mã OTP xác thực không chính xác hoặc đã hết hạn!");
        }

        // --- DÒNG MẶC ĐỊNH: Ràng buộc phải có từ 2 đến 6 ảnh minh chứng ---
        if (request.getDanhSachAnh() == null || request.getDanhSachAnh().size() < 2 || request.getDanhSachAnh().size() > 6) {
            return ResponseEntity.badRequest().body("Vui lòng tải từ 2 đến 6 hình ảnh minh chứng!");
        }
        // THAY THẾ: Chỉ cần tối thiểu 1 ảnh và tối đa 10 ảnh:
        // if (request.getDanhSachAnh() == null || request.getDanhSachAnh().size() < 1 || request.getDanhSachAnh().size() > 10) {
        //     return ResponseEntity.badRequest().body("Vui lòng tải từ 1 đến 10 hình ảnh minh chứng!");
        // }

        // Xóa OTP khỏi bộ nhớ sau khi đã sử dụng thành công (bảo mật 1 lần dùng)
        otpStorage.remove(emailClean);

        // Khởi tạo đối tượng Entity YeuCauHoanTien mới
        YeuCauHoanTien yc = new YeuCauHoanTien();
        yc.setMaDonHangCode(request.getMaDonHangCode());
        yc.setHoTen(request.getHoTen());
        yc.setSoDienThoai(request.getSoDienThoai());
        yc.setEmail(emailClean);
        yc.setDiaChi(request.getDiaChi());
        yc.setTenNganHang(request.getTenNganHang());
        yc.setSoTaiKhoan(request.getSoTaiKhoan());
        yc.setTenChuTaiKhoan(request.getTenChuTaiKhoan());
        yc.setLyDo(request.getLyDo());
        yc.setTrangThai("CHO_DUYET");                                    // Trạng thái ban đầu: CHỜ DUYỆT

        // Chuyển mảng danh sách ảnh Base64/URL thành chuỗi JSON String lưu vào CSDL
        try {
            if (request.getDanhSachAnh() != null && !request.getDanhSachAnh().isEmpty()) {
                yc.setDanhSachAnh(objectMapper.writeValueAsString(request.getDanhSachAnh()));
            } else {
                yc.setDanhSachAnh("[]");
            }
        } catch (Exception e) {
            yc.setDanhSachAnh("[]");
        }

        // Cập nhật trạng thái của Đơn Hàng trong bảng DonHang sang 'YEU_CAU_HOAN_TIEN'
        String cleanCode = request.getMaDonHangCode().replace("#", "").trim();
        List<DonHang> allOrders = donHangRepository.findAll();
        for (DonHang dh : allOrders) {
            if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                yc.setTongTien(dh.getTongTien());                        // Gán tổng tiền đơn hàng sang yêu cầu hoàn
                yc.setPhuongThucThanhToan(dh.getPhuongThucThanhToan());  // Gán phương thức thanh toán gốc
                dh.setTrangThaiDonHang("YEU_CAU_HOAN_TIEN");             // Cập nhật trạng thái đơn hàng
                donHangRepository.save(dh);                              // Lưu cập nhật đơn hàng
                break;
            }
        }

        // Lưu bản ghi yêu cầu hoàn tiền vào CSDL
        yeuCauHoanTienRepository.save(yc);

        // Gửi Email xác nhận tiếp nhận hồ sơ cho khách hàng
        String subject = "[VELORA BOUTIQUE] TIẾP NHẬN YÊU CẦU HOÀN TIỀN #" + request.getMaDonHangCode();
        String content = "<p style='margin:0 0 8px 0;'>Velora Boutique đã nhận được hồ sơ yêu cầu hoàn tiền cho đơn hàng <strong>#" + request.getMaDonHangCode() + "</strong>.</p>"
            + "<p style='margin:0 0 6px 0;'><strong>• Trạng thái:</strong> <span style='color: #e6a200;'>Đang chờ đối soát</span></p>"
            + "<p style='margin:0 0 6px 0;'><strong>• Ngân hàng nhận:</strong> " + request.getTenNganHang() + "</p>"
            + "<p style='margin:0;'><strong>• Số tài khoản:</strong> " + request.getSoTaiKhoan() + " (" + request.getTenChuTaiKhoan() + ")</p>";

        String htmlBody = taoEmailHtmlVelora("TIẾP NHẬN YÊU CẦU HOÀN TIỀN", "Kính chào quý khách " + request.getHoTen() + ",", content, "Bộ phận CSKH sẽ kiểm tra minh chứng và hoàn tất thủ tục.");
        emailService.sendEmail(emailClean, subject, htmlBody);

        return ResponseEntity.ok("Yêu cầu hoàn tiền đã gửi thành công!");
    }

    // =========================================================================
    // [PHẦN 4: API PHÍA QUẢN TRỊ VIÊN (ADMIN) - TRUY VẤN VÀ THỐNG KÊ]
    // =========================================================================

    /**
     * API 3: Lấy toàn bộ danh sách yêu cầu hoàn tiền (Đã parse mảng ảnh JSON)
     * Endpoint: GET /api/hoan-tien/admin/danh-sach
     */
    @GetMapping("/admin/danh-sach")
    public ResponseEntity<?> getDanhSachYeuCauAdmin() {
        List<YeuCauHoanTien> list = yeuCauHoanTienRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (YeuCauHoanTien yc : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", yc.getId());
            map.put("maDonHangCode", yc.getMaDonHangCode());
            map.put("hoTen", yc.getHoTen());
            map.put("soDienThoai", yc.getSoDienThoai());
            map.put("email", yc.getEmail());
            map.put("diaChi", yc.getDiaChi());
            map.put("tenNganHang", yc.getTenNganHang());
            map.put("soTaiKhoan", yc.getSoTaiKhoan());
            map.put("tenChuTaiKhoan", yc.getTenChuTaiKhoan());
            map.put("lyDo", yc.getLyDo());
            map.put("trangThai", yc.getTrangThai());
            map.put("ghiChuAdmin", yc.getGhiChuAdmin());
            map.put("tongTien", yc.getTongTien());
            map.put("phuongThucThanhToan", yc.getPhuongThucThanhToan());
            map.put("ngayTao", yc.getNgayTao());

            // Parse chuỗi String danhSachAnh thành List<String> trả về cho Vue.js render
            List<String> imgList = new ArrayList<>();
            String rawImg = yc.getDanhSachAnh();
            if (rawImg != null && !rawImg.trim().isEmpty()) {
                try {
                    if (rawImg.trim().startsWith("[")) {
                        imgList = objectMapper.readValue(rawImg, new TypeReference<List<String>>() {});
                    } else if (rawImg.contains("__VELORA_IMG__")) {
                        imgList = Arrays.asList(rawImg.split("__VELORA_IMG__"));
                    }
                } catch (Exception e) {
                    imgList = Collections.emptyList();
                }
            }
            map.put("danhSachAnh", imgList);
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * API 4: Thống kê số lần hoàn tiền của từng khách hàng và kiểm tra danh sách đen
     * Endpoint: GET /api/hoan-tien/admin/thong-ke-khach-hang
     */
    @GetMapping("/admin/thong-ke-khach-hang")
    public ResponseEntity<?> getThongKeSolanHoanKhachHang() {
        List<YeuCauHoanTien> list = yeuCauHoanTienRepository.findAll();
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (YeuCauHoanTien yc : list) {
            // Chỉ đếm những đơn đã được Admin phê duyệt 'DA_HOAN_TIEN'
            if ("DA_HOAN_TIEN".equals(yc.getTrangThai())) {
                String email = yc.getEmail();
                result.putIfAbsent(email, new HashMap<>());
                Map<String, Object> data = result.get(email);
                data.put("email", email);
                data.put("hoTen", yc.getHoTen());
                data.put("soDienThoai", yc.getSoDienThoai());

                int count = (int) data.getOrDefault("soLanHoan", 0);
                data.put("soLanHoan", count + 1);

                // --- DÒNG MẶC ĐỊNH: Đánh dấu Blacklist khi số lần hoàn >= 6 ---
                data.put("blackList", (count + 1) >= 6);
                
                // THAY THẾ: Đánh dấu Blacklist nghiêm ngặt hơn khi số lần hoàn >= 3:
                // data.put("blackList", (count + 1) >= 3);
            }
        }

        return ResponseEntity.ok(result.values());
    }

    // =========================================================================
    // [PHẦN 5: API QUẢN TRỊ VIÊN DUYỆT / TỪ CHỐI & TỰ ĐỘNG KHÓA TÀI KHOẢN]
    // =========================================================================

    /**
     * API 5: Xử lý phê duyệt hoặc từ chối bồi hoàn từ Admin Dashboard
     * Endpoint: POST /api/hoan-tien/admin/xu-ly
     */
    @PostMapping("/admin/xu-ly")
    @Transactional                                                       // Đảm bảo tính toàn vẹn CSDL khi cập nhật nhiều bảng
    public ResponseEntity<?> xuLyHoanTienAdmin(@RequestBody XuLyHoanTienAdminRequest req) {
        Integer idInt;
        try {
            idInt = Integer.parseInt(req.getYeuCauId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ID yêu cầu hoàn tiền không hợp lệ!");
        }

        // Kiểm tra yêu cầu hoàn tiền có tồn tại trong CSDL không
        Optional<YeuCauHoanTien> opt = yeuCauHoanTienRepository.findById(idInt);
        if (!opt.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy yêu cầu hoàn tiền!");
        }

        YeuCauHoanTien yc = opt.get();
        String cleanCode = yc.getMaDonHangCode().replace("#", "").trim();

        // ---------------------------------------------------------------------
        // TRƯỜNG HỢP A: ADMIN BẤM "KHÔNG DUYỆT" (TỪ CHỐI BỒI HOÀN)
        // ---------------------------------------------------------------------
        if ("KHONG_DUYET".equalsIgnoreCase(req.getHanhDong())) {
            // Bắt buộc Admin phải nhập lý do từ chối
            if (req.getGhiChuNote() == null || req.getGhiChuNote().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("BẮT BUỘC phải nhập lý do khi KHÔNG DUYỆT hoàn tiền!");
            }
            
            yc.setTrangThai("TU_CHOI_HOAN");                             // Cập nhật trạng thái yêu cầu
            yc.setGhiChuAdmin(req.getGhiChuNote().trim());               // Lưu lý do từ chối của Admin
            yeuCauHoanTienRepository.save(yc);

            // Cập nhật bảng DonHang sang 'TU_CHOI_HOAN_TIEN'
            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("TU_CHOI_HOAN_TIEN");
                    donHangRepository.save(dh);
                    break;
                }
            }

            // Gửi Email thông báo từ chối hoàn tiền kèm lý do chi tiết
            String subject = "[VELORA BOUTIQUE] THÔNG BÁO TỪ CHỐI HOÀN TIỀN #" + yc.getMaDonHangCode();
            String content = "<p style='margin:0 0 8px 0;'>Yêu cầu hoàn tiền cho đơn hàng <strong>#" + yc.getMaDonHangCode() + "</strong> đã <span style='color: #e74c3c; font-weight: bold;'>BỊ TỪ CHỐI</span>.</p>"
                + "<p style='margin:0; color: #ff7675;'><strong>• Lý do từ chối:</strong> " + req.getGhiChuNote().trim() + "</p>";

            String htmlBody = taoEmailHtmlVelora("TỪ CHỐI YÊU CẦU HOÀN TIỀN", "Kính chào quý khách " + yc.getHoTen() + ",", content, "Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc có lỗi từ NSX.");
            emailService.sendEmail(yc.getEmail(), subject, htmlBody);

        // ---------------------------------------------------------------------
        // TRƯỜNG HỢP B: ADMIN BẤM "XÁC NHẬN" (PHÊ DUYỆT HOÀN TIỀN)
        // ---------------------------------------------------------------------
        } else if ("XAC_NHAN".equalsIgnoreCase(req.getHanhDong())) {
            yc.setTrangThai("DA_HOAN_TIEN");                             // Cập nhật trạng thái yêu cầu sang ĐÃ HOÀN TIỀN
            yc.setGhiChuAdmin(req.getGhiChuNote() != null ? req.getGhiChuNote().trim() : "");
            yeuCauHoanTienRepository.save(yc);

            // Cập nhật bảng DonHang sang 'DA_DUYET_HOAN_TIEN'
            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("DA_DUYET_HOAN_TIEN");
                    donHangRepository.save(dh);
                    break;
                }
            }

            // Gửi Email thông báo hoàn tiền thành công
            String subject = "[VELORA BOUTIQUE] THÔNG BÁO HOÀN TIỀN THÀNH CÔNG #" + yc.getMaDonHangCode();
            String contentApprove = "<p style='margin:0 0 8px 0;'>Yêu cầu hoàn tiền cho đơn hàng <strong>#" + yc.getMaDonHangCode() + "</strong> đã <span style='color: #2ecc71; font-weight: bold;'>ĐƯỢC PHÊ DUYỆT</span>.</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Ngân hàng thụ hưởng:</strong> " + yc.getTenNganHang() + "</p>"
                + "<p style='margin:0;'><strong>• Số tài khoản nhận tiền:</strong> " + yc.getSoTaiKhoan() + "</p>";

            String htmlApprove = taoEmailHtmlVelora("PHÊ DUYỆT HOÀN TIỀN THÀNH CÔNG", "Kính chào quý khách " + yc.getHoTen() + ",", contentApprove, "Số tiền đã được chuyển lệnh thanh toán về tài khoản.");
            emailService.sendEmail(yc.getEmail(), subject, htmlApprove);

            // Đếm tổng số lần đã hoàn tiền thành công của Email này trong CSDL
            long soLanHoan = yeuCauHoanTienRepository.findByEmailIgnoreCase(yc.getEmail()).stream()
                    .filter(y -> "DA_HOAN_TIEN".equals(y.getTrangThai()))
                    .count();

            // --- DÒNG MẶC ĐỊNH: TỰ ĐỘNG KHÓA TÀI KHOẢN KHI ĐÃ HOÀN TỪ 6 LẦN TRỞ LÊN ---
            if (soLanHoan >= 6) {
            // THAY THẾ: Khóa tài khoản khi đã hoàn từ 3 lần trở lên:
            // if (soLanHoan >= 3) {
            
                Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(yc.getEmail());
                if (userOpt.isPresent()) {
                    NguoiDung nd = userOpt.get();
                    nd.setTrangThai("BI_KHOA");                          // Khóa tài khoản trong bảng NguoiDung
                    nguoiDungRepository.save(nd);

                    // Gửi Email cảnh báo khóa tài khoản do vi phạm chính sách hoàn hàng
                    String subjectBlock = "[CẢNH BÁO BẢO MẬT] TÀI KHOẢN VELORA ĐÃ BỊ KHÓA";
                    String contentBlock = "<p style='margin:0 0 8px 0; color: #e74c3c;'>Tài khoản đã hoàn trả vượt quá giới hạn <strong>6 lần / năm</strong>.</p>"
                        + "<p style='margin:0;'>Hệ thống đã tự động <span style='color: #e74c3c; font-weight: bold;'>KHÓA TÀI KHOẢN</span>.</p>";

                    String htmlBlock = taoEmailHtmlVelora("KHÓA TÀI KHOẢN VI PHẠM CHÍNH SÁCH", "Kính chào quý khách " + yc.getHoTen() + ",", contentBlock, "Mọi thắc mắc vui lòng liên hệ Hotline Velora.");
                    emailService.sendEmail(yc.getEmail(), subjectBlock, htmlBlock);
                }
            }
        }

        return ResponseEntity.ok("Cập nhật trạng thái hoàn tiền thành công!");
    }
}

// =========================================================================
// [PHẦN 6: CÁC DTO / REQUEST OBJECT NHẬN DỮ LIỆU TỪ CLIENT]
// =========================================================================

/**
 * Đối tượng nhận dữ liệu từ Form gửi yêu cầu hoàn tiền (Client Vue.js)
 */
@Data
class YeuCauHoanTienRequest {
    private String maDonHangCode;     // Mã đơn hàng (Ví dụ: DH1001)
    private String hoTen;             // Họ và tên người gửi
    private String soDienThoai;       // Số điện thoại liên hệ
    private String email;             // Địa chỉ email đặt hàng nhận OTP
    private String diaChi;            // Địa chỉ giao nhận
    private String tenNganHang;       // Tên ngân hàng nhận tiền hoàn
    private String soTaiKhoan;        // Số tài khoản ngân hàng
    private String tenChuTaiKhoan;    // Tên chủ tài khoản ngân hàng
    private String lyDo;              // Lý do yêu cầu hoàn tiền
    private List<String> danhSachAnh; // Danh sách 2 - 6 ảnh minh chứng dạng Base64/URL
    private String otpCode;           // Mã OTP 6 chữ số người dùng nhập
}

/**
 * Đối tượng nhận dữ liệu khi Admin bấm duyệt hoặc từ chối bồi hoàn
 */
@Data
class XuLyHoanTienAdminRequest {
    private String yeuCauId;          // ID bản ghi yêu cầu hoàn tiền
    private String hanhDong;          // Hành động: 'XAC_NHAN' hoặc 'KHONG_DUYET'
    private String ghiChuNote;        // Ghi chú/Lý do của Admin gửi cho khách
}