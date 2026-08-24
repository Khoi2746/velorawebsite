package com.velora.website.Controller;

import com.velora.website.Entity.LichHen;
import com.velora.website.Entity.SanPham;
import com.velora.website.Repository.LichHenRepository;
import com.velora.website.Repository.SanPhamRepository;
import com.velora.website.Service.EmailLichHen; 
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
    private final SanPhamRepository sanPhamRepository;
    private final EmailLichHen emailLichHen; 

    public LichHenController(LichHenRepository lichHenRepository, SanPhamRepository sanPhamRepository, EmailLichHen emailLichHen) {
        this.lichHenRepository = lichHenRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.emailLichHen = emailLichHen;
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

            // Tìm Sản phẩm từ DB và gán vào Lịch Hẹn
            if (payload.get("idSanPham") != null) {
                Integer sanPhamId = Integer.parseInt(payload.get("idSanPham").toString());
                SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElse(null);
                lichHen.setSanPham(sanPham); 
            }

            if (payload.get("ngayHen") != null) {
                lichHen.setNgayHen(LocalDate.parse((String) payload.get("ngayHen")));
            }

            // Thực hiện lưu trữ dữ liệu
            LichHen ketQua = lichHenRepository.save(lichHen);
            
            // Lấy tên sản phẩm (nếu khách hàng có chọn)
            String tenSanPham = (ketQua.getSanPham() != null) ? ketQua.getSanPham().getTenSanPham() : "Không chọn cụ thể";

            // Gọi hàm gửi Email cho Admin ngay sau khi lưu DB thành công
            emailLichHen.sendNewBookingToAdmin(
                "veloraclock@gmail.com", 
                ketQua.getTenKhachHang(),
                ketQua.getNgayHen(),
                ketQua.getThoiGian(),
                ketQua.getSoDienThoai(),
                tenSanPham
            );

            // Gọi hàm gửi Email xác nhận ĐĂNG KÝ THÀNH CÔNG cho Khách hàng (kèm PDF đính kèm)
            if (ketQua.getEmail() != null && !ketQua.getEmail().trim().isEmpty()) {
                emailLichHen.sendBookingConfirmationToCustomer(
                    ketQua.getEmail(),
                    ketQua.getTenKhachHang(),
                    ketQua.getNgayHen(),
                    ketQua.getThoiGian(),
                    ketQua.getSoDienThoai(),
                    tenSanPham,
                    ketQua.getId()
                );
            }

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

    // API Admin lấy tất cả danh sách lịch hẹn
    @GetMapping("/admin/danh-sach")
    public ResponseEntity<List<LichHen>> getAllLichHen() {
        return ResponseEntity.ok(lichHenRepository.findAll());
    }

    // API Admin đếm số lượng lịch hẹn mới (Chờ xác nhận - trangThai = 0)
    @GetMapping("/admin/dem-cho-xac-nhan")
    public ResponseEntity<?> countChoXacNhan() {
        long count = lichHenRepository.countByTrangThai(0);
        Map<String, Object> res = new HashMap<>();
        res.put("count", count);
        return ResponseEntity.ok(res);
    }

    // API Admin cập nhật trạng thái chung (Xác nhận/Hoàn thành)
    @PutMapping("/admin/cap-nhat-trang-thai/{id}")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestParam Integer trangThai) {
        LichHen lichHen = lichHenRepository.findById(id).orElse(null);
        if (lichHen != null) {
            lichHen.setTrangThai(trangThai);
            lichHenRepository.save(lichHen);

            // Gọi hàm gửi Email cho Khách hàng khi Admin cập nhật trạng thái
            emailLichHen.sendStatusToCustomer(
                lichHen.getEmail(),
                lichHen.getTrangThai(),
                lichHen.getNgayHen(),
                lichHen.getThoiGian(),
                lichHen.getTenKhachHang()
            );

            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn");
    }

    // API ADMIN HỦY LỊCH HẸN KÈM LÝ DO VÀ GỬI EMAIL CHO KHÁCH HÀNG
  @PutMapping("/admin/huy-lich-hen/{id}")
public ResponseEntity<?> adminHuyLichHen(
        @PathVariable Integer id, 
        @RequestBody Map<String, String> payload) {
    
    LichHen lichHen = lichHenRepository.findById(id).orElse(null);
    if (lichHen == null) {
        return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn");
    }

    String lyDoHuy = payload.get("lyDoHuy");
    if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
        return ResponseEntity.badRequest().body("Vui lòng nhập lý do hủy lịch!");
    }

    // 1. Cập nhật trạng thái = 3 (Đã hủy) & lưu lý do vào DB
    lichHen.setTrangThai(3);
    lichHen.setLyDoHuy(lyDoHuy);
    lichHenRepository.save(lichHen);

    // 2. Gửi email chứa lý do hủy mẫu giao diện Velora
    if (lichHen.getEmail() != null && !lichHen.getEmail().isEmpty()) {
        emailLichHen.sendCancelReasonToCustomer(
            lichHen.getEmail(),
            lichHen.getTenKhachHang(),
            lichHen.getNgayHen(),
            lichHen.getThoiGian(),
            lyDoHuy
        );
    }

    return ResponseEntity.ok("Đã hủy lịch hẹn và gửi email thông báo thành công!");
}
    // API Lấy danh sách lịch hẹn của 1 người dùng dựa vào Email
    @GetMapping("/nguoi-dung/email/{email}")
    public ResponseEntity<List<LichHen>> getLichHenByEmail(@PathVariable String email) {
        List<LichHen> list = lichHenRepository.findByEmail(email);
        return ResponseEntity.ok(list);
    }

    // API Hủy lịch hẹn từ phía khách hàng (chỉ hủy khi ở trạng thái Chờ xác nhận & trước ít nhất 2 ngày)
    @PutMapping("/huy/{id}")
    public ResponseEntity<?> huyLichHenCustomer(@PathVariable Integer id) {
        LichHen lichHen = lichHenRepository.findById(id).orElse(null);
        if (lichHen != null) {
            // 1. Kiểm tra trạng thái
            if (lichHen.getTrangThai() != 0) {
                return ResponseEntity.badRequest().body("Lịch hẹn chỉ có thể hủy khi ở trạng thái Chờ xác nhận!");
            }

            // 2. Ràng buộc: Kiểm tra hủy trước ít nhất 2 ngày
            if (lichHen.getNgayHen() != null) {
                LocalDate homNay = LocalDate.now();
                LocalDate ngayGioiHanHuy = lichHen.getNgayHen().minusDays(2);

                if (homNay.isAfter(ngayGioiHanHuy)) {
                    return ResponseEntity.badRequest().body("Quý khách chỉ có thể hủy lịch hẹn trước ngày hẹn ít nhất 2 ngày!");
                }
            }

            lichHen.setTrangThai(3); // 3: Đã hủy
            lichHenRepository.save(lichHen);
            return ResponseEntity.ok("Hủy lịch hẹn thành công!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn!");
    }
}