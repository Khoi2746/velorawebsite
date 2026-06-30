package com.velora.website.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    /**
     * API ĐĂNG NHẬP HỆ THỐNG (ĐÃ CHUẨN HÓA LỖI JSON)
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

        // 2. KIỂM TRA KHÓA TÀI KHOẢN TRƯỚC (Bọc lỗi qua JSON Object tránh lỗi 403 trình duyệt)
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
     * API KIỂM TRA TRẠNG THÁI THỜI GIAN THỰC (Dùng cho Vue Router Guard)
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

        nguoiDungRepository.save(nguoiDung);
        
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy email trong hệ thống!");
        }

        // Lưu ý: Ở bản demo, mình reset về '123456', sau này em tích hợp gửi Email qua JavaMailSender nhé!
        NguoiDung user = userOpt.get();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setMatKhauMaHoa(encoder.encode("123456")); 
        nguoiDungRepository.save(user);

        return ResponseEntity.ok("Mật khẩu đã được reset về 123456!");
    }
}