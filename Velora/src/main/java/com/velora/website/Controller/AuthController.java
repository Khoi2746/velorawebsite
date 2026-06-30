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
     * API ĐĂNG NHẬP HỆ THỐNG
     */
   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    System.out.println("--- ĐANG DEBUG LOGIN ---");
    
    // 1. Tìm người dùng trong Database
    Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(loginRequest.getEmail());

    if (!userOpt.isPresent()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
    }

    NguoiDung user = userOpt.get();

    // 🔥 ĐƯA ĐOẠN NÀY LÊN TRÊN HẾT: Cứ bị khóa là chặn luôn, không cần check mật khẩu!
    String trangThai = user.getTrangThai();
    if ("KHOA".equalsIgnoreCase(trangThai) || "BI_KHOA".equalsIgnoreCase(trangThai)) {
        System.out.println("=> CHẶN LẬP TỨC: Tài khoản dính trạng thái khóa!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn đã bị khóa bởi Ban quản trị!");
    }

    // 2. Sau đó mới so sánh mật khẩu Bcrypt
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
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
            
            // Nếu cột trạng thái trong Database đang bị null hoặc rỗng, mặc định là HOAT_DONG
            if (trangThai == null || trangThai.trim().isEmpty()) {
                trangThai = "HOAT_DONG";
            }
            
            System.out.println("=> Trạng thái thực tế: [" + trangThai.toUpperCase() + "]");
            return ResponseEntity.ok(trangThai.toUpperCase());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
    }
}