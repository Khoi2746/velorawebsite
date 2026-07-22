package com.velora.website.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Request.LoginRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final NguoiDungRepository nguoiDungRepository;
    
    // Tiêm (Inject) JavaMailSender vào để dùng
    private final JavaMailSender mailSender;

    /**
     * API ĐĂNG NHẬP HỆ THỐNG
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("--- ĐANG DEBUG LOGIN ---");
        
        // 1. Tìm người dùng trong Database
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(loginRequest.getEmail());

        if (!userOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "INVALID_CREDENTIALS");
            errorResponse.put("message", "Sai email hoặc mật khẩu!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        NguoiDung user = userOpt.get();

        // 2. KIỂM TRA KHÓA TÀI KHOẢN TRƯỚC
        String trangThai = user.getTrangThai();
        if ("KHOA".equalsIgnoreCase(trangThai) || "BI_KHOA".equalsIgnoreCase(trangThai)) {
            System.out.println("=> CHẶN LẬP TỨC: Tài khoản dính trạng thái khóa!");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "ACCOUNT_LOCKED");
            errorResponse.put("message", "Tài khoản của bạn đã bị khóa bởi Ban quản trị!");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // 3. So sánh mật khẩu Bcrypt
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
     * API ĐĂNG KÝ (Có tích hợp gửi Email tự động)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody NguoiDung nguoiDung) {
        // Kiểm tra trùng email
        if (nguoiDungRepository.findByEmail(nguoiDung.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email này đã tồn tại!");
        }

        // Băm mật khẩu trước khi lưu
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        nguoiDung.setMatKhauMaHoa(encoder.encode(nguoiDung.getMatKhauMaHoa()));
        nguoiDung.setTrangThai("HOAT_DONG");
        nguoiDung.setNgayTao(new java.util.Date());

        // 1. Lưu User vào Database
        nguoiDungRepository.save(nguoiDung);
        
        // 2. Gửi Email thông báo (Chạy ngầm hoặc chạy song song để không làm chậm API)
        // Dùng Thread để việc gửi email không block luồng phản hồi về Frontend
        new Thread(() -> {
            sendWelcomeEmail(nguoiDung.getEmail(), nguoiDung.getHoTen());
        }).start();
        
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    /**
     * API QUÊN MẬT KHẨU
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy email trong hệ thống!");
        }

        NguoiDung user = userOpt.get();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setMatKhauMaHoa(encoder.encode("123456")); 
        nguoiDungRepository.save(user);

        return ResponseEntity.ok("Mật khẩu đã được reset về 123456!");
    }

    /**
     * HÀM TIỆN ÍCH: Xử lý gửi email HTML
     */
    private void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Đặt true để cho phép gửi email dạng HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Chào mừng gia nhập Velora Clock - Vui lòng xác minh thông tin");
            
            // Template HTML chuẩn nhận diện thương hiệu
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                + "<h2 style='color: #d1aa68; text-align: center; letter-spacing: 2px; margin-bottom: 20px;'>VELORA CLOCK</h2>"
                + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Cảm ơn Quý khách đã tin tưởng và khởi tạo tài khoản tại hệ thống đồng hồ cao cấp Velora Clock.</p>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Để đảm bảo quyền lợi mua sắm và an toàn bảo mật, Quý khách vui lòng thực hiện bước sau:</p>"
                + "<div style='background: #f8f9fa; padding: 20px; border-left: 5px solid #d1aa68; margin: 25px 0; border-radius: 4px;'>"
                + "<b style='color: #111; font-size: 15px;'>1. Đăng nhập vào Website Velora.</b><br/>"
                + "<b style='color: #111; font-size: 15px;'>2. Truy cập phần <span style='color: #d1aa68;'>THÔNG TIN NGƯỜI DÙNG</span>.</b><br/>"
                + "<b style='color: #111; font-size: 15px;'>3. Cập nhật số điện thoại, địa chỉ giao hàng và bấm XÁC MINH.</b>"
                + "</div>"
                + "<p style='font-size: 15px; line-height: 1.6;'>Nếu cần bất kỳ sự hỗ trợ nào, xin vui lòng liên hệ bộ phận Chăm sóc khách hàng VVIP của chúng tôi.</p>"
                + "<br/>"
                + "<p style='font-size: 15px;'>Trân trọng,<br/><b style='color: #d1aa68;'>Ban Quản Trị Velora Clock</b></p>"
                + "</div>";

            helper.setText(htmlContent, true); // true = HTML
            
            System.out.println("Đang tiến hành gửi email đến: " + toEmail);
            mailSender.send(message);
            System.out.println("Gửi email thành công!");
            
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}