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

    // 1. GỬI MÃ OTP XÁC THỰC
    @PostMapping("/gui-otp")
    public ResponseEntity<?> guiOtpXacNhan(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ!");
        }

        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        otpStorage.put(email.trim().toLowerCase(), otp);

        String subject = "[VELORA BOUTIQUE] MÃ OTP XÁC NHẬN YÊU CẦU HOÀN TIỀN";
        String body = "Mã xác nhận OTP của quý khách là: " + otp 
                    + "\n\n⚠️ LƯU Ý QUY ĐỊNH HOÀN HÀNG VELORA:\n"
                    + "- Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc có lỗi trực tiếp từ Nhà Sản Xuất.\n"
                    + "- Nếu quý khách hoàn hàng quá 6 lần trong 1 năm, tài khoản sẽ tự động bị KHÓA và đưa vào Danh sách đen.";

        emailService.sendEmail(email.trim(), subject, body);
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

        // CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG TRONG DB
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

        String subject = "[VELORA BOUTIQUE] XÁC NHẬN ĐÃ TIẾP NHẬN YÊU CẦU HOÀN TIỀN #" + request.getMaDonHangCode();
        String body = "Kính chào " + request.getHoTen() + ",\n\n"
                + "Velora Boutique đã nhận được yêu cầu hoàn tiền cho đơn hàng #" + request.getMaDonHangCode() + ".\n"
                + "Trạng thái đơn hàng hiện tại: Yêu cầu hoàn tiền đã được gửi.\n"
                + "Bộ phận CSKH sẽ tiến hành đối soát và xử lý trong thời gian sớm nhất.\n\n"
                + "📌 QUY ĐỊNH HOÀN HÀNG VELORA:\n"
                + "1. Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc có lỗi từ Nhà Sản Xuất.\n"
                + "2. Nếu quý khách hoàn hàng quá 6 lần trong 1 năm, tài khoản sẽ tự động bị khóa và đưa vào Danh sách đen.\n\n"
                + "Trân trọng,\nĐội ngũ Velora.";

        emailService.sendEmail(emailClean, subject, body);

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

    // 5. ADMIN: XỬ LÝ DUYỆT HOẶC TỪ CHỐI HOÀN TIỀN (CẬP NHẬT TRỰC TIẾP CHUẨN XÁC)
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

            // Cập nhật bảng DonHang trong DB thành TU_CHOI_HOAN_TIEN
            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("TU_CHOI_HOAN_TIEN");
                    donHangRepository.save(dh);
                    System.out.println("✅ Đã đổi trạng thái DonHang thành TU_CHOI_HOAN_TIEN cho đơn #" + dh.getMaDonHangCode());
                    break;
                }
            }

            String subject = "[VELORA BOUTIQUE] THÔNG BÁO TỪ CHỐI HOÀN TIỀN #" + yc.getMaDonHangCode();
            String body = "Kính chào " + yc.getHoTen() + ",\n\n"
                    + "Yêu cầu hoàn tiền cho đơn hàng #" + yc.getMaDonHangCode() + " đã BỊ TỪ CHỐI.\n"
                    + "Lý do từ chối: " + req.getGhiChuNote().trim() + "\n\n"
                    + "📌 QUY ĐỊNH HOÀN HÀNG VELORA: Sản phẩm chỉ được hoàn trong tình trạng nguyên vẹn hoặc nếu có lỗi từ NSX.";
            emailService.sendEmail(yc.getEmail(), subject, body);

        } else if ("XAC_NHAN".equalsIgnoreCase(req.getHanhDong())) {
            yc.setTrangThai("DA_HOAN_TIEN");
            yc.setGhiChuAdmin(req.getGhiChuNote() != null ? req.getGhiChuNote().trim() : "");
            yeuCauHoanTienRepository.save(yc);

            // Cập nhật bảng DonHang trong DB thành DA_DUYET_HOAN_TIEN
            List<DonHang> allOrders = donHangRepository.findAll();
            for (DonHang dh : allOrders) {
                if (dh.getMaDonHangCode() != null && dh.getMaDonHangCode().contains(cleanCode)) {
                    dh.setTrangThaiDonHang("DA_DUYET_HOAN_TIEN");
                    donHangRepository.save(dh);
                    System.out.println("✅ Đã đổi trạng thái DonHang thành DA_DUYET_HOAN_TIEN cho đơn #" + dh.getMaDonHangCode());
                    break;
                }
            }

            String subject = "[VELORA BOUTIQUE] THÔNG BÁO HOÀN TIỀN THÀNH CÔNG #" + yc.getMaDonHangCode();
            String bodyApprove = "Kính chào " + yc.getHoTen() + ",\n\n"
                    + "Yêu cầu hoàn tiền đơn hàng #" + yc.getMaDonHangCode() + " đã ĐƯỢC PHÊ DUYỆT.\n"
                    + "Số tiền sẽ được chuyển về tài khoản: " + yc.getSoTaiKhoan() + " (" + yc.getTenNganHang() + ").\n\n"
                    + "Cảm ơn quý khách đã đồng hành cùng Velora Boutique.";
            emailService.sendEmail(yc.getEmail(), subject, bodyApprove);

            long soLanHoan = yeuCauHoanTienRepository.findByEmailIgnoreCase(yc.getEmail()).stream()
                    .filter(y -> "DA_HOAN_TIEN".equals(y.getTrangThai()))
                    .count();

            if (soLanHoan >= 6) {
                Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(yc.getEmail());
                if (userOpt.isPresent()) {
                    NguoiDung nd = userOpt.get();
                    nd.setTrangThai("BI_KHOA");
                    nguoiDungRepository.save(nd);

                    String subjectBlock = "[CẢNH BÁO BẢO MẬT] TÀI KHOẢN VELORA CỦA BẠN ĐÃ BỊ KHÓA";
                    String bodyBlock = "Kính chào " + yc.getHoTen() + ",\n\n"
                            + "Tài khoản của bạn đã vi phạm chính sách hoàn hàng quá 6 lần/năm.\n"
                            + "Hệ thống Velora đã tự động KHÓA TÀI KHOẢN và đưa địa chỉ Gmail này vào Danh sách đen (Blacklist).";
                    emailService.sendEmail(yc.getEmail(), subjectBlock, bodyBlock);
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