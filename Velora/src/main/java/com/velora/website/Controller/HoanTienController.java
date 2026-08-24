package com.velora.website.Controller;

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

@RestController
@RequestMapping("/api/hoan-tien")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class HoanTienController {

    private final EmailService emailService;
    private final DonHangRepository donHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final YeuCauHoanTienRepository yeuCauHoanTienRepository;

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // ================= KHUNG TEMPLATE EMAIL VELORA LUXURY =================
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

    // 1. GỬI MÃ OTP XÁC THỰC
    @PostMapping("/gui-otp")
    public ResponseEntity<?> guiOtpXacNhan(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ!");
        }

        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        otpStorage.put(email.trim().toLowerCase(), otp);

        String subject = "[VELORA BOUTIQUE] MÃ OTP XÁC NHẬN YÊU CẦU HOÀN TIỀN";
        String content = "<p style='margin:0 0 10px 0;'>Mã xác thực OTP của quý khách là:</p>"
            + "<div style='text-align: center; margin: 15px 0;'><span style='font-size: 28px; font-weight: bold; letter-spacing: 8px; color: #cca15e; background: #120905; padding: 10px 24px; border: 1px dashed #cca15e;'>" + otp + "</span></div>"
            + "<p style='margin: 15px 0 5px 0; color: #e6a200; font-weight: bold;'>⚠️ QUY ĐỊNH HOÀN HÀNG VELORA:</p>"
            + "<p style='margin: 0; font-size: 12px; color: #bbb;'>- Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc lỗi do NSX.<br>- Hoàn hàng quá 6 lần/năm tài khoản sẽ bị khóa.</p>";

        String htmlBody = taoEmailHtmlVelora(
            "XÁC THỰC MÃ OTP HOÀN TIỀN", 
            "Kính chào quý khách,", 
            content, 
            "Vui lòng không cung cấp mã OTP này cho bất kỳ ai để đảm bảo an toàn bảo mật."
        );

        emailService.sendEmail(email.trim(), subject, htmlBody);
        return ResponseEntity.ok("Mã OTP xác thực đã được gửi về Gmail!");
    }

    // 2. KHÁCH HÀNG XÁC NHẬN GỬI YÊU CẦU HOÀN TIỀN
    @PostMapping("/xac-nhan-yeu-cau")
    @Transactional
    public ResponseEntity<?> guiYeuCauHoanTien(@RequestBody YeuCauHoanTienRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email đặt hàng bị trống!");
        }

        String emailClean = request.getEmail().trim().toLowerCase();

        if (!otpStorage.containsKey(emailClean) || !otpStorage.get(emailClean).equals(request.getOtpCode())) {
            return ResponseEntity.badRequest().body("Mã OTP xác thực không chính xác hoặc đã hết hạn!");
        }

        if (request.getDanhSachAnh() == null || request.getDanhSachAnh().size() < 2 || request.getDanhSachAnh().size() > 6) {
            return ResponseEntity.badRequest().body("Vui lòng tải từ 2 đến 6 hình ảnh minh chứng!");
        }

        otpStorage.remove(emailClean);

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
        yc.setTrangThai("CHO_DUYET");

        if (request.getDanhSachAnh() != null && !request.getDanhSachAnh().isEmpty()) {
            yc.setDanhSachAnh(String.join(";", request.getDanhSachAnh()));
        } else {
            yc.setDanhSachAnh("");
        }

        String cleanCode = request.getMaDonHangCode().replace("#", "").trim();
        List<DonHang> allOrders = donHangRepository.findAll();
        for (DonHang dh : allOrders) {
            if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                yc.setTongTien(dh.getTongTien());
                yc.setPhuongThucThanhToan(dh.getPhuongThucThanhToan());
                dh.setTrangThaiDonHang("YEU_CAU_HOAN_TIEN");
                donHangRepository.save(dh);
                break;
            }
        }

        yeuCauHoanTienRepository.save(yc);

        String subject = "[VELORA BOUTIQUE] TIẾP NHẬN YÊU CẦU HOÀN TIỀN #" + request.getMaDonHangCode();
        String content = "<p style='margin:0 0 8px 0;'>Velora Boutique đã nhận được hồ sơ yêu cầu hoàn tiền cho đơn hàng <strong>#" + request.getMaDonHangCode() + "</strong>.</p>"
            + "<p style='margin:0 0 6px 0;'><strong>• Trạng thái:</strong> <span style='color: #e6a200;'>Đang chờ đối soát</span></p>"
            + "<p style='margin:0 0 6px 0;'><strong>• Ngân hàng nhận:</strong> " + request.getTenNganHang() + "</p>"
            + "<p style='margin:0;'><strong>• Số tài khoản:</strong> " + request.getSoTaiKhoan() + " (" + request.getTenChuTaiKhoan() + ")</p>";

        String htmlBody = taoEmailHtmlVelora(
            "TIẾP NHẬN YÊU CẦU HOÀN TIỀN", 
            "Kính chào quý khách " + request.getHoTen() + ",", 
            content, 
            "Bộ phận CSKH sẽ kiểm tra minh chứng và hoàn tất thủ tục trong thời gian sớm nhất."
        );

        emailService.sendEmail(emailClean, subject, htmlBody);

        return ResponseEntity.ok("Yêu cầu hoàn tiền đã gửi thành công!");
    }

    // 3. ADMIN: LẤY DANH SÁCH YÊU CẦU HOÀN TIỀN
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

            if (yc.getDanhSachAnh() != null && !yc.getDanhSachAnh().trim().isEmpty()) {
                map.put("danhSachAnh", Arrays.asList(yc.getDanhSachAnh().split(";")));
            } else {
                map.put("danhSachAnh", Collections.emptyList());
            }

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    // 4. ADMIN: THỐNG KÊ SỐ LẦN HOÀN THEO KHÁCH HÀNG
    @GetMapping("/admin/thong-ke-khach-hang")
    public ResponseEntity<?> getThongKeSolanHoanKhachHang() {
        List<YeuCauHoanTien> list = yeuCauHoanTienRepository.findAll();
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (YeuCauHoanTien yc : list) {
            if ("DA_HOAN_TIEN".equals(yc.getTrangThai())) {
                String email = yc.getEmail();
                result.putIfAbsent(email, new HashMap<>());
                Map<String, Object> data = result.get(email);
                data.put("email", email);
                data.put("hoTen", yc.getHoTen());
                data.put("soDienThoai", yc.getSoDienThoai());

                int count = (int) data.getOrDefault("soLanHoan", 0);
                data.put("soLanHoan", count + 1);
                data.put("blackList", (count + 1) >= 6);
            }
        }

        return ResponseEntity.ok(result.values());
    }

    // 5. ADMIN: XỬ LÝ DUYỆT HOẶC TỪ CHỐI HOÀN TIỀN
    @PostMapping("/admin/xu-ly")
    @Transactional
    public ResponseEntity<?> xuLyHoanTienAdmin(@RequestBody XuLyHoanTienAdminRequest req) {
        Integer idInt;
        try {
            idInt = Integer.parseInt(req.getYeuCauId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ID yêu cầu hoàn tiền không hợp lệ!");
        }

        Optional<YeuCauHoanTien> opt = yeuCauHoanTienRepository.findById(idInt);
        if (!opt.isPresent()) {
            return ResponseEntity.badRequest().body("Không tìm thấy yêu cầu hoàn tiền!");
        }

        YeuCauHoanTien yc = opt.get();
        String cleanCode = yc.getMaDonHangCode().replace("#", "").trim();

        if ("KHONG_DUYET".equalsIgnoreCase(req.getHanhDong())) {
            if (req.getGhiChuNote() == null || req.getGhiChuNote().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("BẮT BUỘC phải nhập lý do khi KHÔNG DUYỆT hoàn tiền!");
            }
            yc.setTrangThai("TU_CHOI_HOAN");
            yc.setGhiChuAdmin(req.getGhiChuNote().trim());
            yeuCauHoanTienRepository.save(yc);

            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("TU_CHOI_HOAN_TIEN");
                    donHangRepository.save(dh);
                    break;
                }
            }

            String subject = "[VELORA BOUTIQUE] THÔNG BÁO TỪ CHỐI HOÀN TIỀN #" + yc.getMaDonHangCode();
            String content = "<p style='margin:0 0 8px 0;'>Yêu cầu hoàn tiền cho đơn hàng <strong>#" + yc.getMaDonHangCode() + "</strong> đã <span style='color: #e74c3c; font-weight: bold;'>BỊ TỪ CHỐI</span>.</p>"
                + "<p style='margin:0; color: #ff7675;'><strong>• Lý do từ chối:</strong> " + req.getGhiChuNote().trim() + "</p>";

            String htmlBody = taoEmailHtmlVelora(
                "TỪ CHỐI YÊU CẦU HOÀN TIỀN", 
                "Kính chào quý khách " + yc.getHoTen() + ",", 
                content, 
                "📌 QUY ĐỊNH HOÀN HÀNG: Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc có lỗi trực tiếp từ Nhà Sản Xuất."
            );

            emailService.sendEmail(yc.getEmail(), subject, htmlBody);

        } else if ("XAC_NHAN".equalsIgnoreCase(req.getHanhDong())) {
            yc.setTrangThai("DA_HOAN_TIEN");
            yc.setGhiChuAdmin(req.getGhiChuNote() != null ? req.getGhiChuNote().trim() : "");
            yeuCauHoanTienRepository.save(yc);

            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("DA_DUYET_HOAN_TIEN");
                    donHangRepository.save(dh);
                    break;
                }
            }

            String subject = "[VELORA BOUTIQUE] THÔNG BÁO HOÀN TIỀN THÀNH CÔNG #" + yc.getMaDonHangCode();
            String contentApprove = "<p style='margin:0 0 8px 0;'>Yêu cầu hoàn tiền cho đơn hàng <strong>#" + yc.getMaDonHangCode() + "</strong> đã <span style='color: #2ecc71; font-weight: bold;'>ĐƯỢC PHÊ DUYỆT</span>.</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Ngân hàng thụ hưởng:</strong> " + yc.getTenNganHang() + "</p>"
                + "<p style='margin:0;'><strong>• Số tài khoản nhận tiền:</strong> " + yc.getSoTaiKhoan() + "</p>";

            String htmlApprove = taoEmailHtmlVelora(
                "PHÊ DUYỆT HOÀN TIỀN THÀNH CÔNG", 
                "Kính chào quý khách " + yc.getHoTen() + ",", 
                contentApprove, 
                "Số tiền đã được chuyển lệnh thanh toán về tài khoản của bạn. Cảm ơn đã đồng hành cùng Velora Clock."
            );

            emailService.sendEmail(yc.getEmail(), subject, htmlApprove);

            long soLanHoan = yeuCauHoanTienRepository.findByEmailIgnoreCase(yc.getEmail()).stream()
                    .filter(y -> "DA_HOAN_TIEN".equals(y.getTrangThai()))
                    .count();

            if (soLanHoan >= 6) {
                Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(yc.getEmail());
                if (userOpt.isPresent()) {
                    NguoiDung nd = userOpt.get();
                    nd.setTrangThai("BI_KHOA");
                    nguoiDungRepository.save(nd);

                    String subjectBlock = "[CẢNH BÁO BẢO MẬT] TÀI KHOẢN VELORA ĐÃ BỊ KHÓA";
                    String contentBlock = "<p style='margin:0 0 8px 0; color: #e74c3c;'>Tài khoản của quý khách đã thực hiện hoàn trả vượt quá giới hạn <strong>6 lần / năm</strong>.</p>"
                        + "<p style='margin:0;'>Hệ thống đã tự động <span style='color: #e74c3c; font-weight: bold;'>KHÓA TÀI KHOẢN</span> và đưa địa chỉ Gmail này vào Danh sách đen (Blacklist).</p>";

                    String htmlBlock = taoEmailHtmlVelora(
                        "KHÓA TÀI KHOẢN VI PHẠM CHÍNH SÁCH", 
                        "Kính chào quý khách " + yc.getHoTen() + ",", 
                        contentBlock, 
                        "Mọi thắc mắc và khiếu nại vui lòng liên hệ Hotline ban quản trị Velora."
                    );

                    emailService.sendEmail(yc.getEmail(), subjectBlock, htmlBlock);
                }
            }
        }

        return ResponseEntity.ok("Cập nhật trạng thái hoàn tiền thành công!");
    }
}

@Data
class YeuCauHoanTienRequest {
    private String maDonHangCode;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String tenNganHang;
    private String soTaiKhoan;
    private String tenChuTaiKhoan;
    private String lyDo;
    private List<String> danhSachAnh;
    private String otpCode;
}

@Data
class XuLyHoanTienAdminRequest {
    private String yeuCauId;
    private String hanhDong;
    private String ghiChuNote;
}