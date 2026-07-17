package com.velora.website.Controller; // Đảm bảo bạn đã có dòng package này ở dòng 1

// 1. Import Entity và Repository của project
import com.velora.website.Entity.LichHen;
import com.velora.website.Repository.LichHenRepository;

// 2. Import các thư viện của Spring Boot
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lich-hen")
@CrossOrigin(originPatterns = "*")
public class LichHenController {

    @Autowired
    private LichHenRepository lichHenRepository;

  
   // 1. API cho Khách hàng: Tạo lịch hẹn mới từ trang chi tiết sản phẩm
 @PostMapping("/dat-lich")
    public ResponseEntity<?> createLichHen(@RequestBody LichHen lichHen) {
        try {
            lichHen.setTrangThai(0); // Mặc định là Chờ xác nhận
            lichHenRepository.save(lichHen); // Lưu vào Database
            
            // SỬA DÒNG NÀY: Trả về một chuỗi JSON báo thành công thay vì object
            return ResponseEntity.ok().body("{\"message\": \"Đặt lịch thành công\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    // 2. API cho Admin: Lấy danh sách tất cả lịch hẹn
    @GetMapping("/admin/danh-sach")
    public ResponseEntity<List<LichHen>> getAllLichHen() {
        return ResponseEntity.ok(lichHenRepository.findAll());
    }

    // 3. API cho Admin: Cập nhật trạng thái lịch hẹn
    @PutMapping("/admin/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestParam Integer trangThai) {
        LichHen lichHen = lichHenRepository.findById(id).orElse(null);
        if (lichHen != null) {
            lichHen.setTrangThai(trangThai);
            lichHenRepository.save(lichHen);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn");
    }
}