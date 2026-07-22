package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Service.EmailService;

// Import thêm thư viện để gửi Mail HTML
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class NguoiDungController {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;
    private final JavaMailSender mailSender; // Inject thêm JavaMailSender để gửi giao diện HTML

    public NguoiDungController(NguoiDungRepository nguoiDungRepository, EmailService emailService, JavaMailSender mailSender) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.emailService = emailService;
        this.mailSender = mailSender;
    }

    // 1. Lấy danh sách
    @GetMapping("/thanh-vien")
    public ResponseEntity<List<NguoiDung>> layToanBoThanhVien() {
        return ResponseEntity.ok(nguoiDungRepository.findAll());
    }

    // 2. Thêm mới
    @PostMapping("/thanh-vien")
    public ResponseEntity<?> themMoiThanhVien(@RequestBody NguoiDung nguoiDung) {
        if (nguoiDungRepository.existsByEmail(nguoiDung.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        nguoiDung.setNgayTao(new Date());
        nguoiDung.setNgayCapNhat(new Date());
        nguoiDung.setTrangThai("HOAT_DONG");
        
        // 1. Lấy mật khẩu thực tế từ Frontend gửi lên
        String passThucTe = nguoiDung.getMatKhauMaHoa(); 
        
        // 2. Nếu Frontend trống (không nhập), mới gán mặc định là 123456
        if (passThucTe == null || passThucTe.trim().isEmpty()) {
            passThucTe = "123456";
        }
        
        // 3. Mã hóa mật khẩu và lưu lại
        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        
        try {
            // Gửi email báo luôn mật khẩu thực tế cho khách
            emailService.sendEmail(saved.getEmail(), "Chào mừng đến Velora Clock", 
                "Tài khoản đã tạo. Mật khẩu đăng nhập của bạn là: " + passThucTe);
        } catch (Exception e) { 
            System.err.println("Mail lỗi: " + e.getMessage()); 
        }
        
        return ResponseEntity.ok(saved);
    }

    // 3. Sửa thông tin (Phía Admin)
    @PutMapping("/thanh-vien/{id}")
    @Transactional
    public ResponseEntity<?> capNhatThanhVien(@PathVariable Integer id, @RequestBody NguoiDung form) {
        return nguoiDungRepository.findById(id).map(user -> {
            user.setHoTen(form.getHoTen());
            user.setSoDienThoai(form.getSoDienThoai());
            user.setDiaChi(form.getDiaChi());
            user.setTrangThai(form.getTrangThai());
            user.setNgayCapNhat(new Date());
            
            if (form.getVaiTros() != null) user.setVaiTros(form.getVaiTros());
            
            NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);
            
            try {
                emailService.sendEmail(updatedUser.getEmail(), "Thông báo cập nhật", "Thông tin của bạn đã được quản trị viên cập nhật.");
            } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }
            
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Xóa (ĐÃ FIX: CHẶN KHÔNG CHO XÓA ADMIN)
    @DeleteMapping("/thanh-vien/{id}")
    public ResponseEntity<?> xoaThanhVien(@PathVariable Integer id) {
        return nguoiDungRepository.findById(id).map(user -> {
            
            // KIỂM TRA: Nếu tài khoản có chứa quyền ROLE_ADMIN thì chặn lại ngay lập tức
            if (user.getVaiTros() != null && user.getVaiTros().stream()
                    .anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa tài khoản Quản trị viên!");
            }
            
            // Nếu không phải Admin -> Tiến hành xóa như bình thường
            nguoiDungRepository.delete(user);
            return ResponseEntity.ok("Xóa thành công!");
            
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Đổi trạng thái 
    @PatchMapping("/{id}/doi-trang-thai")
    @Transactional
    public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy!"));

        if (user.getVaiTros().stream().anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa Admin!");
        }

        String currentStatus = user.getTrangThai() != null ? user.getTrangThai().toUpperCase() : "";
        String trangThaiMoi = (currentStatus.contains("HOAT_DONG") || currentStatus.contains("HOẠT ĐỘNG")) ? "KHOA" : "HOAT_DONG";
        
        user.setTrangThai(trangThaiMoi);
        user.setNgayCapNhat(new Date());
        NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);

        try {
            String statusText = "KHOA".equals(trangThaiMoi) ? "BỊ KHÓA" : "MỞ KHÓA (HOẠT ĐỘNG)";
            emailService.sendEmail(updatedUser.getEmail(), "Thông báo trạng thái tài khoản Velora", 
                "Tài khoản của bạn trên Velora Clock hiện tại đã chuyển sang trạng thái: " + statusText);
        } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }

        return ResponseEntity.ok(updatedUser);
    }

    // 6. Cập nhật thông tin (Người dùng tự cập nhật - Có gửi Email HTML)
    @PutMapping("/cap-nhat/{id}")
    public ResponseEntity<?> capNhatThongTin(@PathVariable Integer id, @RequestBody NguoiDung requestData) {
        Optional<NguoiDung> optNguoiDung = nguoiDungRepository.findById(id);
        
        if (optNguoiDung.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }

        NguoiDung nguoiDung = optNguoiDung.get();
        
        // KIỂM TRA LOGIC: Lần đầu xác minh hay đổi thông tin?
        // Nếu trường số điện thoại trong DB trước khi update là rỗng -> Đánh dấu là Xác minh lần đầu
        boolean isLanDauXacMinh = (nguoiDung.getSoDienThoai() == null || nguoiDung.getSoDienThoai().trim().isEmpty());
        
        // Chỉ cập nhật các thông tin được phép (Không cho đổi Email hoặc Role ở đây)
        if (requestData.getHoTen() != null) {
            nguoiDung.setHoTen(requestData.getHoTen());
        }
        if (requestData.getSoDienThoai() != null) {
            nguoiDung.setSoDienThoai(requestData.getSoDienThoai());
        }
        if (requestData.getDiaChi() != null) {
            nguoiDung.setDiaChi(requestData.getDiaChi());
        }
        
        nguoiDung.setNgayCapNhat(new java.util.Date()); 

        NguoiDung updatedUser = nguoiDungRepository.save(nguoiDung);
        
        // Chạy một Thread mới để bắn Email HTML, không làm chậm phản hồi API
        new Thread(() -> {
            if (isLanDauXacMinh) {
                sendVerificationSuccessEmail(updatedUser.getEmail(), updatedUser.getHoTen());
            } else {
                sendInfoChangedEmail(updatedUser.getEmail(), updatedUser.getHoTen());
            }
        }).start();
        
        return ResponseEntity.ok(updatedUser);
    }

    /* =========================================================================================
     * CÁC HÀM TIỆN ÍCH HỖ TRỢ GỬI MAIL HTML GIAO DIỆN LUXURY VELORA CLOCK
     * ========================================================================================= */

    private void sendVerificationSuccessEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Xác minh tài khoản thành công");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                + "<h2 style='color: #d1aa68; text-align: center; letter-spacing: 2px; margin-bottom: 20px;'>VELORA CLOCK</h2>"
                + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Cảm ơn Quý khách đã hoàn tất việc cập nhật và xác minh thông tin cá nhân trên hệ thống Velora Clock.</p>"
                + "<div style='background: #f8f9fa; padding: 20px; border-left: 5px solid #2ecc71; margin: 25px 0; border-radius: 4px;'>"
                + "<b style='color: #2ecc71; font-size: 16px;'>✓ TÀI KHOẢN ĐÃ ĐƯỢC XÁC MINH</b><br/><br/>"
                + "<span style='color: #555;'>Giờ đây, Quý khách đã có thể trải nghiệm toàn bộ các dịch vụ và đặc quyền mua sắm cao cấp từ chúng tôi.</span>"
                + "</div>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Nếu cần hỗ trợ, xin vui lòng liên hệ bộ phận CSKH VVIP.</p>"
                + "<br/>"
                + "<p style='font-size: 15px;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Đã gửi email [Xác Minh] cho: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail xác minh: " + e.getMessage());
        }
    }

    private void sendInfoChangedEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Cảnh báo thay đổi thông tin tài khoản");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                + "<h2 style='color: #d1aa68; text-align: center; letter-spacing: 2px; margin-bottom: 20px;'>VELORA CLOCK</h2>"
                + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Hệ thống của chúng tôi vừa ghi nhận có sự <b>thay đổi về thông tin (Số điện thoại hoặc Địa chỉ giao hàng)</b> trên hồ sơ tài khoản của Quý khách.</p>"
                + "<div style='background: #fff3cd; padding: 20px; border-left: 5px solid #f39c12; margin: 25px 0; border-radius: 4px;'>"
                + "<b style='color: #d35400; font-size: 15px;'>⚠️ CHÚ Ý BẢO MẬT:</b><br/><br/>"
                + "<span style='color: #555;'>Nếu Quý khách là người thực hiện thay đổi này, xin vui lòng bỏ qua email này.</span><br/><br/>"
                + "<span style='color: #555;'>Nếu Quý khách KHÔNG thực hiện thay đổi, vui lòng liên hệ ngay với chúng tôi để bảo vệ tài khoản!</span>"
                + "</div>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Cảm ơn Quý khách đã luôn đồng hành cùng Velora Clock.</p>"
                + "<br/>"
                + "<p style='font-size: 15px;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Đã gửi email [Thay Đổi Info] cho: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail thay đổi thông tin: " + e.getMessage());
        }
    }
}