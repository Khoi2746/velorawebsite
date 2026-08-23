package com.velora.website.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.velora.website.Entity.CanhBaoAnNinh;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.NhatKyDangNhap;
import com.velora.website.Entity.VaiTro;
import com.velora.website.Repository.CanhBaoAnNinhRepository;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.NhatKyDangNhapRepository;
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
    private final NhatKyDangNhapRepository nhatKyDangNhapRepository;
    private final CanhBaoAnNinhRepository canhBaoAnNinhRepository;

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    /**
     * API LẤY THÔNG TIN USER HIỆN TẠI TỪ DATABASE (CHỐNG CACHE TRIỆT ĐỂ)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.set("Pragma", "no-cache");
        headers.set("Expires", "0");

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok().headers(headers).body(null);
        }

        Object principal = authentication.getPrincipal();
        Optional<NguoiDung> userOpt = Optional.empty();

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            
            String registrationId = "";
            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            }
            String provider = registrationId != null ? registrationId.toUpperCase() : "";

            if ("GOOGLE".equalsIgnoreCase(provider)) {
                // Google: Tìm theo Email trước, sau đó fallback tìm theo ProviderId (sub)
                String email = oauth2User.getAttribute("email");
                if (email != null && !email.isEmpty()) {
                    userOpt = nguoiDungRepository.findByEmail(email);
                }
                if (userOpt.isEmpty()) {
                    String sub = oauth2User.getAttribute("sub");
                    if (sub != null) {
                        userOpt = nguoiDungRepository.findByProviderAndProviderId("GOOGLE", sub);
                    }
                }
            } else if ("FACEBOOK".equalsIgnoreCase(provider)) {
                // 🔥 FACEBOOK: BẮT BUỘC TÌM BẰNG PROVIDER + PROVIDER ID (Tuyệt đối không dùng email)
                String fbId = oauth2User.getAttribute("id");
                if (fbId != null) {
                    userOpt = nguoiDungRepository.findByProviderAndProviderId("FACEBOOK", fbId);
                }
            }
        } else {
            // Đăng nhập thủ công bằng Email/Password truyền thống
            String email = authentication.getName();
            if (email != null && !email.isEmpty()) {
                userOpt = nguoiDungRepository.findByEmail(email);
            }
        }

        if (userOpt.isPresent()) {
            return ResponseEntity.ok().headers(headers).body(userOpt.get());
        }

        return ResponseEntity.ok().headers(headers).body(null);
    }

    /**
     * API ĐĂNG XUẤT (HỦY SESSION & XÓA BẢO MẬT PHÍA SERVER)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request, 
                                    jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Đăng xuất thành công!");
    }

    @GetMapping("/oauth2/prepare/{mode}")
    public void prepareOAuth2Mode(@PathVariable String mode, 
                                   @RequestParam String provider, 
                                   jakarta.servlet.http.HttpServletRequest request, 
                                   jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        jakarta.servlet.http.HttpSession session = request.getSession(true);
        session.setAttribute("oauth2_mode", mode);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    /**
     * API ĐĂNG NHẬP THỦ CÔNG (TÍCH HỢP TẠO SECURITY CONTEXT & CHẶN TÀI KHOẢN XÃ HỘI)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("--- ĐANG DEBUG LOGIN & AN NINH SOC ---");

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) userAgent = "Unknown Device";

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(loginRequest.getEmail());

        if (!userOpt.isPresent()) {
            saveLoginLog(loginRequest.getEmail(), clientIp, userAgent, "THAT_BAI_SAI_EMAIL");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "INVALID_CREDENTIALS");
            errorResponse.put("message", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        NguoiDung user = userOpt.get();

        // 🔥 NẾU TÀI KHOẢN NÀY ĐÃ ĐĂNG KÝ BẰNG MẠNG XÃ HỘI, CHẶN THỦ CÔNG VÀ KHÔNG TÍNH LẦN VI PHẠM
        if (user.getProvider() != null && !user.getProvider().trim().isEmpty()) {
            saveLoginLog(user.getEmail(), clientIp, userAgent, "THAT_BAI_TAI_KHOAN_SOCIAL");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "SOCIAL_ACCOUNT_EXISTS");
            errorResponse.put("message", "Tài khoản này đã được đăng ký trong một tài khoản khác! "+ ". Vui lòng sử dụng nút đăng nhập nền tảng " + " bên dưới!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        String trangThai = user.getTrangThai();
        if ("KHOA".equalsIgnoreCase(trangThai) || "BI_KHOA".equalsIgnoreCase(trangThai)) {
            saveLoginLog(user.getEmail(), clientIp, userAgent, "THAT_BAI_TAI_KHOAN_BI_KHOA");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", "ACCOUNT_LOCKED");
            errorResponse.put("message", "Tài khoản của bạn đã bị khóa do vi phạm bảo mật. Vui lòng dùng 'Quên mật khẩu' để khôi phục!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isMatch = encoder.matches(loginRequest.getPassword(), user.getMatKhauMaHoa());

        if (isMatch) {
            user.setSoLanViPham(0);
            nguoiDungRepository.save(user);

            saveLoginLog(user.getEmail(), clientIp, userAgent, "THANH_CONG");

            List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (user.getVaiTros() != null) {
                for (VaiTro role : user.getVaiTros()) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getTenVaiTro()));
                }
            }
            Authentication authentication = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            jakarta.servlet.http.HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());

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
            int currentViolations = (user.getSoLanViPham() != null) ? user.getSoLanViPham() + 1 : 1;
            user.setSoLanViPham(currentViolations);

            int remainingAttempts = 5 - currentViolations;
            String message;

            if (currentViolations >= 5) {
                user.setTrangThai("BI_KHOA");
                message = "Bạn đã nhập sai quá 5 lần. Tài khoản đã bị khóa an toàn! Vui lòng dùng 'Quên mật khẩu' để đặt lại.";

                CanhBaoAnNinh alert = new CanhBaoAnNinh();
                alert.setDiaChiIP(clientIp);
                alert.setThongTinThietBi(userAgent);
                alert.setLoaiTanCong("DO_MAT_KHAU_BRUTE_FORCE");
                alert.setMoTaChiTiet("Phát hiện Brute-force: Sai mật khẩu 5 lần liên tiếp đối với tài khoản " + user.getEmail());
                alert.setMucDoNguyHiem("NGHIEM_TRONG");
                alert.setDaXuLy(false);
                alert.setNgayTao(new java.util.Date());
                canhBaoAnNinhRepository.save(alert);
            } else {
                message = "Sai mật khẩu! Bạn còn " + remainingAttempts + " lần thử trước khi tài khoản bị khóa. Hãy dùng 'Quên mật khẩu' nếu cần.";
            }

            nguoiDungRepository.save(user);
            saveLoginLog(user.getEmail(), clientIp, userAgent, "THAT_BAI_SAI_MAT_KHAU");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", currentViolations >= 5 ? "ACCOUNT_LOCKED" : "INVALID_CREDENTIALS");
            errorResponse.put("message", message);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    private void saveLoginLog(String email, String ip, String device, String status) {
        try {
            NhatKyDangNhap log = new NhatKyDangNhap();
            log.setEmailDangNhap(email);
            log.setDiaChiIP(ip);
            log.setThongTinThietBi(device);
            log.setTrangThaiKetQua(status);
            log.setThoiGianDangNhap(new java.util.Date());
            nhatKyDangNhapRepository.save(log);
        } catch (Exception e) {
            System.err.println("Lỗi ghi log đăng nhập: " + e.getMessage());
        }
    }

    /**
     * API KIỂM TRA TRẠNG THÁI THỜI GIAN THỰC
     */
    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus(@RequestParam String email) {
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String trangThai = userOpt.get().getTrangThai();
            if (trangThai == null || trangThai.trim().isEmpty()) {
                trangThai = "HOAT_DONG";
            }
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

        VaiTro roleUser = vaiTroRepository.findByTenVaiTro("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi Hệ Thống: Không tìm thấy quyền ROLE_CUSTOMER trong Database!"));

        List<VaiTro> danhSachQuyen = new ArrayList<>();
        danhSachQuyen.add(roleUser);
        nguoiDung.setVaiTros(danhSachQuyen);

        nguoiDungRepository.save(nguoiDung);

        new Thread(() -> sendWelcomeEmail(nguoiDung.getEmail(), nguoiDung.getHoTen())).start();

        return ResponseEntity.ok("Đăng ký thành công!");
    }

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

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);

        new Thread(() -> sendOtpEmail(email, userOpt.get().getHoTen(), otp)).start();

        return ResponseEntity.ok("Mã xác nhận đã được gửi đến email của quý khách.");
    }

    @GetMapping("/get-ip")
    public ResponseEntity<Map<String, String>> getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        Map<String, String> response = new HashMap<>();
        response.put("ip", clientIp);
        return ResponseEntity.ok(response);
    }

    /**
     * API CẬP NHẬT THÔNG TIN BỔ SUNG CHO TÀI KHOẢN OAUTH2
     */
    @PutMapping("/cap-nhat-thong-tin")
    public ResponseEntity<?> capNhatThongTinOauth2(@RequestBody Map<String, String> request) {
        String originalEmail = request.get("originalEmail");
        String newEmail = request.get("email");
        String hoTen = request.get("hoTen");
        String soDienThoai = request.get("soDienThoai");
        String diaChi = request.get("diaChi");

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(originalEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy tài khoản!");
        }

        NguoiDung user = userOpt.get();

        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(originalEmail)) {
            if (nguoiDungRepository.findByEmail(newEmail).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email này đã được sử dụng bởi tài khoản khác!");
            }
            user.setEmail(newEmail);
        }

        user.setHoTen(hoTen);
        user.setSoDienThoai(soDienThoai);
        user.setDiaChi(diaChi);
        user.setTrangThai("HOAT_DONG");

        nguoiDungRepository.save(user);
        return ResponseEntity.ok(user);
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

        String storedOtp = otpStorage.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phiên làm việc không hợp lệ, vui lòng thử lại!");
        }

        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            NguoiDung user = userOpt.get();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            user.setMatKhauMaHoa(encoder.encode(newPassword));
            user.setTrangThai("HOAT_DONG");
            user.setSoLanViPham(0);
            nguoiDungRepository.save(user);

            otpStorage.remove(email);

            new Thread(() -> sendPasswordSuccessEmail(email, user.getHoTen())).start();

            return ResponseEntity.ok("Thay đổi mật khẩu thành công!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi hệ thống.");
    }

    private void sendOtpEmail(String toEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Mã xác nhận khôi phục mật khẩu");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<h2 style='color: #d1aa68;'>KHÔI PHỤC MẬT KHẨU</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                    + "<p style='font-size: 15px;'>Mã xác nhận (OTP) để thay đổi mật khẩu của quý khách là:</p>"
                    + "<div style='background-color: #170d08; padding: 15px; margin: 20px auto; border: 1px dashed #d1aa68; display: inline-block; font-size: 24px; font-weight: bold; color: #d1aa68; letter-spacing: 5px;'>"
                    + otp + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendPasswordSuccessEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Velora Clock - Thay đổi mật khẩu thành công");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<h2 style='color: #2ecc71;'>THÀNH CÔNG</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + fullName + "</b>,</p>"
                    + "<p style='font-size: 15px;'>Mật khẩu tài khoản Velora Clock của quý khách vừa được thay đổi thành công.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Chào mừng gia nhập Velora Clock");

            String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px;'>"
                    + "<p>Kính chào <b style='color: #d1aa68;'>" + fullName + "</b>,</p>"
                    + "<p>Cảm ơn Quý khách đã tin tưởng và khởi tạo tài khoản tại hệ thống đồng hồ cao cấp Velora Clock.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}