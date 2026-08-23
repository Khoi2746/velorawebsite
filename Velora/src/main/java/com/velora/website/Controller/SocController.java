package com.velora.website.Controller;

import com.velora.website.Entity.CanhBaoAnNinh;
import com.velora.website.Entity.NhatKyDangNhap;
import com.velora.website.Repository.CanhBaoAnNinhRepository;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.NhatKyDangNhapRepository;
import com.velora.website.Service.SocService; // 🔥 1. IMPORT SOC SERVICE
import com.velora.website.dto.SocCommentDTO;
import com.velora.website.dto.SocUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soc")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class SocController {

    private final CanhBaoAnNinhRepository canhBaoRepo;
    private final NhatKyDangNhapRepository nhatKyRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final SocService socService; // 🔥 2. KHAI BÁO BIẾN SOC SERVICE Ở ĐÂY

    // 1. API lấy danh sách cảnh báo tấn công
    @GetMapping("/alerts")
    public ResponseEntity<List<CanhBaoAnNinh>> getAlerts() {
        return ResponseEntity.ok(socService.getRecentAlerts());
    }

    // 2. API lấy danh sách tài khoản bị khóa hoặc cấm bình luận
    @GetMapping("/locked-users")
    public ResponseEntity<List<SocUserDTO>> getLockedUsers() {
        return ResponseEntity.ok(socService.getLockedUsers());
    }

    // 3. API lấy lịch sử nhật ký truy cập
    @GetMapping("/logs")
    public ResponseEntity<List<NhatKyDangNhap>> getLogs() {
        return ResponseEntity.ok(socService.getRecentLogs());
    }

    // 🔥 4. SỬA CHỖ NÀY: Gọi hàm getMonitoredComments từ SocService thay vì List.of()
    @GetMapping("/comments")
    public ResponseEntity<List<SocCommentDTO>> getComments() {
        return ResponseEntity.ok(socService.getMonitoredComments());
    }

    // 🔥 5. BỔ SUNG API XÓA BÌNH LUẬN TỪ MÀN HÌNH SOC
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        socService.deleteComment(id);
        return ResponseEntity.ok("Đã xóa bình luận thành công!");
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

    // 6. API thực thi khóa / mở khóa tài khoản từ màn hình SOC
    @PutMapping("/users/{id}/{action}")
    public ResponseEntity<?> manageUser(@PathVariable Integer id, @PathVariable String action) {
        socService.updateUserStatus(id, action.toUpperCase());
        return ResponseEntity.ok("Thành công!");
    }
}