package com.velora.website.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

// Thêm các thư viện Mail của Spring
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.VaiTro;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.VaiTroRepository;
import com.velora.website.Request.LoginRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository; 
    private final JavaMailSender mailSender;

    // Biến lưu trữ OTP tạm thời (Key: Email, Value: Mã OTP)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    /**
     * API ĐĂNG NHẬP HỆ THỐNG
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("--- ĐANG DEBUG LOGIN ---");

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(loginRequest.getEmail());

        if (!userOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "INVALID_CREDENTIALS");
            errorResponse.put("message", "Sai email hoặc mật khẩu!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        NguoiDung user = userOpt.get();

        String trangThai = user.getTrangThai();
        if ("KHOA".equalsIgnoreCase(trangThai) || "BI_KHOA".equalsIgnoreCase(trangThai)) {
            System.out.println("=> CHẶN LẬP TỨC: Tài khoản dính trạng thái khóa!");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "ACCOUNT_LOCKED");
            errorResponse.put("message", "Tài khoản của bạn đã bị khóa bởi Ban quản trị!");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isMatch = encoder.matches(loginRequest.getPassword(), user.getMatKhauMaHoa());

        if (isMatch) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("maNguoiDung", user.getMaNguoiDung());
            responseData.put("hoTen", user.getHoTen());
            responseData.put("email", user.getEmail());

            String roleName = "ROLE_CUSTOMER";
            if (user.getVaiTros() != null && !user.getVaiTros().isEmpty()) {
                roleName = user.getVaiTros().get(0).getTenVaiTro();
            }
            responseData.put("vaiTro", roleName);

            return ResponseEntity.ok(responseData);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "INVALID_CREDENTIALS");
            errorResponse.put("message", "Sai email hoặc mật khẩu!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * API KIỂM TRA TRẠNG THÁI THỜI GIAN THỰC
     */
    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus(@RequestParam String email) {
        System.out.println("=== [GUARD] Đang check trạng thái tài khoản: " + email + " ===");

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String trangThai = userOpt.get().getTrangThai();

            if (trangThai == null || trangThai.trim().isEmpty()) {
                trangThai = "HOAT_DONG";
            }

            System.out.println("=> Trạng thái thực tế: [" + trangThai.toUpperCase() + "]");
            return ResponseEntity.ok(trangThai.toUpperCase());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
    }

    /**
     * API ĐĂNG KÝ
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody NguoiDung nguoiDung) {
        if (nguoiDungRepository.findByEmail(nguoiDung.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email này đã tồn tại!");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        nguoiDung.setMatKhauMaHoa(encoder.encode(nguoiDung.getMatKhauMaHoa()));
        nguoiDung.setTrangThai("HOAT_DONG");
        nguoiDung.setNgayTao(new java.util.Date());

        // GÁN QUYỀN MẶC ĐỊNH LÀ "ROLE_USER"
        VaiTro roleUser = vaiTroRepository.findByTenVaiTro("ROLE_CUSTOMER")
                .orElseThrow(
                        () -> new RuntimeException("Lỗi Hệ Thống: Không tìm thấy quyền ROLE_USER trong Database!"));

        List<VaiTro> danhSachQuyen = new ArrayList<>();
        danhSachQuyen.add(roleUser);
        nguoiDung.setVaiTros(danhSachQuyen);

        // Lưu User vào Database
        nguoiDungRepository.save(nguoiDung);

        // Gửi Email thông báo (Chạy ngầm)
        new Thread(() -> {
            sendWelcomeEmail(nguoiDung.getEmail(), nguoiDung.getHoTen());
        }).start();

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    /* =========================================================================
     * CÁC API QUÊN MẬT KHẨU (LUỒNG MỚI 3 BƯỚC)
     * ========================================================================= */

    /**
     * BƯỚC 1: GỬI MÃ OTP VỀ EMAIL
     */
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy email trong hệ thống!");
        }

        // Random mã OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp); // Lưu vào RAM

        // Gửi email chạy ngầm
        new Thread(() -> sendOtpEmail(email, userOpt.get().getHoTen(), otp)).start();

        return ResponseEntity.ok("Mã xác nhận đã được gửi đến email của quý khách.");
    }

    /**
     * BƯỚC 2: XÁC THỰC MÃ OTP
     */
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            return ResponseEntity.ok("Mã xác nhận hợp lệ!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã xác nhận không chính xác!");
    }

    /**
     * BƯỚC 3: ĐẶT LẠI MẬT KHẨU MỚI
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        // Check lại OTP lần cuối
        String storedOtp = otpStorage.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phiên làm việc không hợp lệ, vui lòng thử lại!");
        }

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            NguoiDung user = userOpt.get();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            user.setMatKhauMaHoa(encoder.encode(newPassword));
            nguoiDungRepository.save(user);

            otpStorage.remove(email);

            new Thread(() -> sendPasswordSuccessEmail(email, user.getHoTen())).start();

            return ResponseEntity.ok("Thay đổi mật khẩu thành công!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi hệ thống.");
    }


    /* =========================================================================
     * CÁC HÀM TIỆN ÍCH GỬI EMAIL
     * ========================================================================= */

    /**
     * Gửi email OTP
     */
    private void sendOtpEmail(String toEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Mã xác nhận khôi phục mật khẩu");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: #d1aa68;'>KHÔI PHỤC MẬT KHẨU</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                    + "<p style='font-size: 15px;'>Mã xác nhận (OTP) để thay đổi mật khẩu của quý khách là:</p>"
                    + "<div style='background-color: #170d08; padding: 15px; margin: 20px auto; border: 1px dashed #d1aa68; display: inline-block; font-size: 24px; font-weight: bold; color: #d1aa68; letter-spacing: 5px;'>"
                    + otp
                    + "</div>"
                    + "<p style='font-size: 14px; color: #aaa;'>Vui lòng không chia sẻ mã này cho bất kỳ ai. Mã có hiệu lực trong thời gian ngắn.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo đổi mật khẩu thành công
     */
    private void sendPasswordSuccessEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Thay đổi mật khẩu thành công");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: #2ecc71;'>THÀNH CÔNG</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                    + "<p style='font-size: 15px;'>Mật khẩu tài khoản Velora Clock của quý khách vừa được thay đổi thành công.</p>"
                    + "<p style='font-size: 15px;'>Nếu quý khách không thực hiện thao tác này, vui lòng liên hệ ngay với bộ phận CSKH của chúng tôi.</p>"
                    + "<br/><p style='color: #d1aa68;'>Trân trọng, Ban Quản Trị Velora Clock.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi email chào mừng khi đăng ký
     */
    private void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Chào mừng gia nhập Velora Clock - Vui lòng xác minh thông tin");

            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "  <img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 180px; height: auto;' />"
                    + "</div>"
                    + "<p style='font-size: 16px; color: #ffffff;'>Kính chào <b style='color: #d1aa68;'>" + fullName
                    + "</b>,</p>"
                    + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Cảm ơn Quý khách đã tin tưởng và khởi tạo tài khoản tại hệ thống đồng hồ cao cấp Velora Clock.</p>"
                    + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Để đảm bảo quyền lợi mua sắm và an toàn bảo mật, Quý khách vui lòng thực hiện bước sau:</p>"
                    + "<div style='background-color: #170d08; padding: 20px; border: 1px solid #d1aa68; border-left: 5px solid #d1aa68; margin: 25px 0; border-radius: 4px;'>"
                    + "<b style='color: #d1aa68; font-size: 15px;'>1. Đăng nhập vào Website Velora.</b><br/>"
                    + "<b style='color: #d1aa68; font-size: 15px;'>2. Truy cập phần <span style='color: #ffffff;'>THÔNG TIN NGƯỜI DÙNG</span>.</b><br/>"
                    + "<b style='color: #d1aa68; font-size: 15px;'>3. Cập nhật số điện thoại, địa chỉ giao hàng và bấm XÁC MINH.</b>"
                    + "</div>"
                    + "<p style='font-size: 15px; line-height: 1.6; color: #e0e0e0;'>Nếu cần bất kỳ sự hỗ trợ nào, xin vui lòng liên hệ bộ phận Chăm sóc khách hàng VVIP của chúng tôi.</p>"
                    + "<br/>"
                    + "<p style='font-size: 15px; color: #e0e0e0;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            System.out.println("Đang tiến hành gửi email đến: " + toEmail);
            mailSender.send(message);
            System.out.println("Gửi email thành công!");

        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}