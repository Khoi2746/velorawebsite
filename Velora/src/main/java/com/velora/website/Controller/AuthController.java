package com.velora.website.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Request.LoginRequest;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("--- ĐANG DEBUG LOGIN ---");
        System.out.println("Email nhận được: " + loginRequest.getEmail());
        
        // 1. Tìm người dùng trong Database
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByEmail(loginRequest.getEmail());

        // Kiểm tra xem có tìm thấy user không
        if (!userOpt.isPresent()) {
            System.out.println("LỖI: Không tìm thấy email này trong DB!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
        }

        NguoiDung user = userOpt.get();
        System.out.println("Đã tìm thấy User: " + user.getHoTen());

        // 2. So sánh mật khẩu
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isMatch = encoder.matches(loginRequest.getPassword(), user.getMatKhauMaHoa());
        
        System.out.println("Kết quả so sánh (matches): " + isMatch);

        // 3. Xử lý khi đăng nhập thành công
        if (isMatch) {
            // Đóng gói dữ liệu an toàn để gửi về Vue.js (KHÔNG gửi mật khẩu mã hóa)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("maNguoiDung", user.getMaNguoiDung());
            responseData.put("hoTen", user.getHoTen());
            responseData.put("email", user.getEmail());
            
            // XỬ LÝ QUYỀN (RBAC):
            // Tạm thời gán quyền ROLE_ADMIN cho tài khoản admin@velora.com để thông mạch giao diện Vue.js.
            // Sau này khi cấu hình xong @ManyToMany, em sẽ query lấy List<VaiTro> từ DB đắp vào đây.
            if ("admin@velora.com".equals(user.getEmail())) {
                responseData.put("vaiTro", "ROLE_ADMIN");
            } else {
                responseData.put("vaiTro", "ROLE_CUSTOMER");
            }

            System.out.println("=> DỮ LIỆU ĐÓNG GÓI GỬI VỀ VUE.JS: " + responseData);
            return ResponseEntity.ok(responseData); 
            
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu!");
        }
    }
}