package com.velora.website.Controller;

import com.velora.website.Entity.LichHen;
import com.velora.website.Entity.SanPham; // Bổ sung import Entity SanPham
import com.velora.website.Repository.LichHenRepository;
import com.velora.website.Repository.SanPhamRepository; // Bổ sung import Repository SanPham
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/lich-hen")
@CrossOrigin(originPatterns = "*")
public class LichHenController {

    private final LichHenRepository lichHenRepository;
    private final SanPhamRepository sanPhamRepository; // Khai báo thêm SanPhamRepository

    // Cập nhật constructor để inject SanPhamRepository
    public LichHenController(LichHenRepository lichHenRepository, SanPhamRepository sanPhamRepository) {
        this.lichHenRepository = lichHenRepository;
        this.sanPhamRepository = sanPhamRepository;
    }

    // API Đặt lịch hẹn - Trả về bản đồ dữ liệu chứa ID tường minh
    @PostMapping("/dat-lich")
    public ResponseEntity<?> createLichHen(@RequestBody Map<String, Object> payload) {
        try {
            LichHen lichHen = new LichHen();
            
            lichHen.setTenKhachHang((String) payload.get("tenKhachHang"));
            lichHen.setSoDienThoai((String) payload.get("soDienThoai"));
            lichHen.setEmail((String) payload.get("email"));
            lichHen.setThoiGian((String) payload.get("thoiGian"));
            lichHen.setGhiChu((String) payload.get("ghiChu"));
            lichHen.setTrangThai(0); 

            // ĐÃ SỬA: Tìm Sản phẩm từ DB và gán vào Lịch Hẹn
            if (payload.get("idSanPham") != null) {
                Integer sanPhamId = Integer.parseInt(payload.get("idSanPham").toString());
                SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
                lichHen.setSanPham(sanPham); // Truyền Object SanPham vào thay vì số Integer
            }

            if (payload.get("ngayHen") != null) {
                lichHen.setNgayHen(LocalDate.parse((String) payload.get("ngayHen")));
            }

            // Thực hiện lưu trữ dữ liệu
            LichHen ketQua = lichHenRepository.save(lichHen);
            
            // Đóng gói JSON phản hồi chứa thuộc tính id
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đặt lịch thành công");
            response.put("id", ketQua.getId()); 
            
            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi Server: " + e.getMessage());
        }
    }

    @GetMapping("/admin/danh-sach")
    public ResponseEntity<List<LichHen>> getAllLichHen() {
        return ResponseEntity.ok(lichHenRepository.findAll());
    }

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