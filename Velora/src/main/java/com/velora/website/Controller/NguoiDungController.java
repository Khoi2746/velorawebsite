package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NguoiDungController {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JavaMailSender mailSender;

    public NguoiDungController(NguoiDungRepository nguoiDungRepository, 
                               PasswordEncoder passwordEncoder,
                               EmailService emailService, 
                               JavaMailSender mailSender) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.mailSender = mailSender;
    }

    // 1. Lấy danh sách toàn bộ thành viên
    @GetMapping("/thanh-vien")
    public ResponseEntity<List<NguoiDung>> layToanBoThanhVien() {
        List<NguoiDung> danhSach = nguoiDungRepository.findAll();
        danhSach.forEach(u -> u.setMatKhauMaHoa(null));
        return ResponseEntity.ok(danhSach);
    }

    // 2. Thêm mới thành viên
    @PostMapping(value = "/thanh-vien", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> themMoiThanhVien(@RequestBody NguoiDung nguoiDung) {
        if (nguoiDungRepository.existsByEmail(nguoiDung.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        nguoiDung.setNgayTao(new Date());
        nguoiDung.setNgayCapNhat(new Date());
        if (nguoiDung.getTrangThai() == null) {
            nguoiDung.setTrangThai("HOAT_DONG");
        }

        String passThucTe = nguoiDung.getMatKhauMaHoa();
        if (passThucTe == null || passThucTe.trim().isEmpty()) {
            passThucTe = "123456";
        }

        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);

        final String finalPass = passThucTe;
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(saved.getEmail(), "Chào mừng đến Velora Clock", 
                    "Tài khoản của bạn đã được khởi tạo thành công. Mật khẩu đăng nhập: " + finalPass);
            } catch (Exception e) {
                System.err.println("Mail lỗi khi tạo mới: " + e.getMessage());
            }
        });

        saved.setMatKhauMaHoa(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 3. Sửa thông tin thành viên (Phía Admin)
    @PutMapping(value = "/thanh-vien/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> capNhatThanhVien(@PathVariable Integer id, @RequestBody NguoiDung form) {
        Optional<NguoiDung> optUser = nguoiDungRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thành viên!");
        }

        NguoiDung user = optUser.get();
        user.setHoTen(form.getHoTen());
        user.setSoDienThoai(form.getSoDienThoai());
        user.setDiaChi(form.getDiaChi());
        user.setTrangThai(form.getTrangThai());
        user.setNgayCapNhat(new Date());

        if (form.getVaiTros() != null) {
            user.setVaiTros(form.getVaiTros());
        }

        NguoiDung updatedUser = nguoiDungRepository.save(user);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(updatedUser.getEmail(), "Thông báo cập nhật tài khoản", 
                    "Thông tin tài khoản của bạn đã được quản trị viên cập nhật thành công.");
            } catch (Exception e) {
                System.err.println("Mail lỗi khi cập nhật: " + e.getMessage());
            }
        });

        updatedUser.setMatKhauMaHoa(null);
        return ResponseEntity.ok(updatedUser);
    }

    // 4. Xóa thành viên (Chặn xóa Admin)
    @DeleteMapping("/thanh-vien/{id}")
    @Transactional
    public ResponseEntity<?> xoaThanhVien(@PathVariable Integer id) {
        Optional<NguoiDung> optUser = nguoiDungRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng!");
        }

        NguoiDung user = optUser.get();
        if (user.getVaiTros() != null && user.getVaiTros().stream()
                .anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa tài khoản Quản trị viên!");
        }

        nguoiDungRepository.delete(user);
        return ResponseEntity.ok("Xóa thành công!");
    }

    // 5. Đổi trạng thái (Khóa / Mở khóa)
    @PatchMapping("/thanh-vien/{id}/doi-trang-thai")
    @Transactional
    public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
        Optional<NguoiDung> optUser = nguoiDungRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng!");
        }

        NguoiDung user = optUser.get();
        if (user.getVaiTros() != null && user.getVaiTros().stream()
                .anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể thay đổi trạng thái của Admin!");
        }

        String currentStatus = user.getTrangThai() != null ? user.getTrangThai().toUpperCase() : "";
        String trangThaiMoi = (currentStatus.contains("HOAT_DONG") || currentStatus.contains("HOẠT ĐỘNG")) ? "KHOA" : "HOAT_DONG";

        user.setTrangThai(trangThaiMoi);
        user.setNgayCapNhat(new Date());
        NguoiDung updatedUser = nguoiDungRepository.save(user);

        CompletableFuture.runAsync(() -> {
            try {
                String statusText = "KHOA".equals(trangThaiMoi) ? "BỊ KHÓA" : "MỞ KHÓA (HOẠT ĐỘNG)";
                emailService.sendEmail(updatedUser.getEmail(), "Thông báo trạng thái tài khoản Velora", 
                    "Tài khoản của bạn trên Velora Clock hiện tại đã chuyển sang trạng thái: " + statusText);
            } catch (Exception e) {
                System.err.println("Mail lỗi khi đổi trạng thái: " + e.getMessage());
            }
        });

        updatedUser.setMatKhauMaHoa(null);
        return ResponseEntity.ok(updatedUser);
    }

   // 6. Cập nhật thông tin cá nhân (FormData + Upload File)
    @PutMapping(value = "/cap-nhat/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> capNhatThongTin(
            @PathVariable Integer id,
            @RequestParam(value = "hoTen", required = false) String hoTen,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            @RequestParam(value = "diaChi", required = false) String diaChi,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
            
        try {
            Optional<NguoiDung> optNguoiDung = nguoiDungRepository.findById(id);
            if (optNguoiDung.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
            }

            NguoiDung nguoiDung = optNguoiDung.get();
            boolean isLanDauXacMinh = (nguoiDung.getSoDienThoai() == null || nguoiDung.getSoDienThoai().trim().isEmpty());
            
            if (hoTen != null && !hoTen.trim().isEmpty()) {
                nguoiDung.setHoTen(hoTen);
            }
            nguoiDung.setSoDienThoai(soDienThoai);
            nguoiDung.setDiaChi(diaChi);
            
            // 1. Xử lý lưu File Avatar (nếu có)
            if (avatar != null && !avatar.isEmpty() && avatar.getOriginalFilename() != null) {
                try {
                    String uploadDir = "uploads/avatars/";
                    Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    String rawFilename = avatar.getOriginalFilename();
                    String cleanFilename = StringUtils.cleanPath(rawFilename);
                    String fileName = System.currentTimeMillis() + "_" + cleanFilename;
                    Path filePath = uploadPath.resolve(fileName);

                    try (InputStream inputStream = avatar.getInputStream()) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    nguoiDung.setAnhDaiDien("/uploads/avatars/" + fileName);
                } catch (Exception e) {
                    System.err.println("Lỗi lưu file avatar: " + e.getMessage());
                }
            }
            
            nguoiDung.setNgayCapNhat(new Date()); 
            NguoiDung updatedUser = nguoiDungRepository.save(nguoiDung);
            
            // Lấy thông tin Email và HoTen ra biến nguyên bản trước khi đẩy sang thread gửi mail
            final String targetEmail = updatedUser.getEmail();
            final String targetFullName = updatedUser.getHoTen();

            // 2. GỬI EMAIL KHÔNG LÀM CRASH REQUEST NẾU LỖI
            CompletableFuture.runAsync(() -> {
                try {
                    if (isLanDauXacMinh) {
                        sendVerificationSuccessEmail(targetEmail, targetFullName);
                    } else {
                        sendInfoChangedEmail(targetEmail, targetFullName);
                    }
                } catch (Throwable t) {
                    System.err.println("Gửi mail thất bại (nhưng dữ liệu đã lưu thành công): " + t.getMessage());
                }
            });
            
            // Tránh set null vào Entity đang Managed, tạo bản copy hoặc ẩn ở Jackson trong Entity
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi Backend: " + e.getMessage());
        }
    }
    /* =========================================================================================
     * HELPER METHODS - SEND HTML MAIL
     * ========================================================================================= */

    private void sendVerificationSuccessEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Xác minh tài khoản thành công");

            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "  <img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 180px; height: auto;' />"
                + "</div>"
                + "<p style='font-size: 16px; color: #ffffff;'>Kính chào <b style='color: #d1aa68;'>" + fullName + "</b>,</p>"
                + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Cảm ơn Quý khách đã hoàn tất việc cập nhật và xác minh thông tin cá nhân trên hệ thống Velora Clock.</p>"
                + "<div style='background-color: #170d08; padding: 20px; border: 1px solid #2ecc71; border-left: 5px solid #2ecc71; margin: 25px 0; border-radius: 4px;'>"
                + "<b style='color: #2ecc71; font-size: 16px;'>✓ TÀI KHOẢN ĐÃ ĐƯỢC XÁC MINH</b><br/><br/>"
                + "<span style='color: #e0e0e0;'>Giờ đây, Quý khách đã có thể trải nghiệm toàn bộ các dịch vụ và đặc quyền mua sắm cao cấp từ chúng tôi.</span>"
                + "</div>"
                + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Nếu cần hỗ trợ, xin vui lòng liên hệ bộ phận CSKH VVIP.</p>"
                + "<br/>"
                + "<p style='font-size: 15px; color: #e0e0e0;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
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

            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "  <img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 180px; height: auto;' />"
                + "</div>"
                + "<p style='font-size: 16px; color: #ffffff;'>Kính chào <b style='color: #d1aa68;'>" + fullName + "</b>,</p>"
                + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Hệ thống của chúng tôi vừa ghi nhận có sự <b style='color: #ffffff;'>thay đổi về thông tin (Số điện thoại hoặc Địa chỉ giao hàng)</b> trên hồ sơ tài khoản của Quý khách.</p>"
                + "<div style='background-color: #170d08; padding: 20px; border: 1px solid #f39c12; border-left: 5px solid #f39c12; margin: 25px 0; border-radius: 4px;'>"
                + "<b style='color: #f39c12; font-size: 15px;'>⚠️ CHÚ Ý BẢO MẬT:</b><br/><br/>"
                + "<span style='color: #e0e0e0;'>Nếu Quý khách là người thực hiện thay đổi này, xin vui lòng bỏ qua email này.</span><br/><br/>"
                + "<span style='color: #e0e0e0;'>Nếu Quý khách KHÔNG thực hiện thay đổi, vui lòng liên hệ ngay với chúng tôi để bảo vệ tài khoản!</span>"
                + "</div>"
                + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Cảm ơn Quý khách đã luôn đồng hành cùng Velora Clock.</p>"
                + "<br/>"
                + "<p style='font-size: 15px; color: #e0e0e0;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Đã gửi email [Thay Đổi Info] cho: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail thay đổi thông tin: " + e.getMessage());
        }
    }
}