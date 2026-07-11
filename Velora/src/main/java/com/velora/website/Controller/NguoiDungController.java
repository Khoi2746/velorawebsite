package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class NguoiDungController {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    public NguoiDungController(NguoiDungRepository nguoiDungRepository, EmailService emailService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.emailService = emailService;
    }

    // 1. Lấy danh sách
    @GetMapping("/thanh-vien")
    public ResponseEntity<List<NguoiDung>> layToanBoThanhVien() {
        return ResponseEntity.ok(nguoiDungRepository.findAll());
    }

    // 2. Thêm mới
    @PostMapping("/thanh-vien")
    public ResponseEntity<?> themMoiThanhVien(@RequestBody NguoiDung nguoiDung) {
        if (nguoiDungRepository.existsByEmail(nguoiDung.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        nguoiDung.setNgayTao(new Date());
        nguoiDung.setNgayCapNhat(new Date());
        nguoiDung.setTrangThai("HOAT_DONG");
        
        // 1. Lấy mật khẩu thực tế từ Frontend gửi lên
        String passThucTe = nguoiDung.getMatKhauMaHoa(); 
        
        // 2. Nếu Frontend trống (không nhập), mới gán mặc định là 123456
        if (passThucTe == null || passThucTe.trim().isEmpty()) {
            passThucTe = "123456";
        }
        
        // 3. Mã hóa mật khẩu và lưu lại
        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        
        try {
            // Gửi email báo luôn mật khẩu thực tế cho khách
            emailService.sendEmail(saved.getEmail(), "Chào mừng đến Velora Clock", 
                "Tài khoản đã tạo. Mật khẩu đăng nhập của bạn là: " + passThucTe);
        } catch (Exception e) { 
            System.err.println("Mail lỗi: " + e.getMessage()); 
        }
        
        return ResponseEntity.ok(saved);
    }
    // 3. Sửa thông tin
    @PutMapping("/thanh-vien/{id}")
    @Transactional
    public ResponseEntity<?> capNhatThanhVien(@PathVariable Integer id, @RequestBody NguoiDung form) {
        return nguoiDungRepository.findById(id).map(user -> {
            user.setHoTen(form.getHoTen());
            user.setSoDienThoai(form.getSoDienThoai());
            user.setDiaChi(form.getDiaChi());
            user.setTrangThai(form.getTrangThai());
            user.setNgayCapNhat(new Date());
            
            if (form.getVaiTros() != null) user.setVaiTros(form.getVaiTros());
            
            NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);
            
            try {
                emailService.sendEmail(updatedUser.getEmail(), "Thông báo cập nhật", "Thông tin của bạn đã được cập nhật.");
            } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }
            
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Xóa (ĐÃ FIX: CHẶN KHÔNG CHO XÓA ADMIN)
    @DeleteMapping("/thanh-vien/{id}")
    public ResponseEntity<?> xoaThanhVien(@PathVariable Integer id) {
        return nguoiDungRepository.findById(id).map(user -> {
            
            // KIỂM TRA: Nếu tài khoản có chứa quyền ROLE_ADMIN thì chặn lại ngay lập tức
            if (user.getVaiTros() != null && user.getVaiTros().stream()
                    .anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa tài khoản Quản trị viên!");
            }
            
            // Nếu không phải Admin -> Tiến hành xóa như bình thường
            nguoiDungRepository.delete(user);
            return ResponseEntity.ok("Xóa thành công!");
            
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Đổi trạng thái 
    @PatchMapping("/{id}/doi-trang-thai")
    @Transactional
    public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy!"));

        if (user.getVaiTros().stream().anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa Admin!");
        }

        String currentStatus = user.getTrangThai() != null ? user.getTrangThai().toUpperCase() : "";
        String trangThaiMoi = (currentStatus.contains("HOAT_DONG") || currentStatus.contains("HOẠT ĐỘNG")) ? "KHOA" : "HOAT_DONG";
        
        user.setTrangThai(trangThaiMoi);
        user.setNgayCapNhat(new Date());
        NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);

        try {
            String statusText = "KHOA".equals(trangThaiMoi) ? "BỊ KHÓA" : "MỞ KHÓA (HOẠT ĐỘNG)";
            emailService.sendEmail(updatedUser.getEmail(), "Thông báo trạng thái tài khoản Velora", 
                "Tài khoản của bạn trên Velora Clock hiện tại đã chuyển sang trạng thái: " + statusText);
        } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }

        return ResponseEntity.ok(updatedUser);
    }
    @PutMapping("/cap-nhat/{id}")
    public ResponseEntity<?> capNhatThongTin(@PathVariable Integer id, @RequestBody NguoiDung requestData) {
        Optional<NguoiDung> optNguoiDung = nguoiDungRepository.findById(id);
        
        if (optNguoiDung.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }

        NguoiDung nguoiDung = optNguoiDung.get();
        
        // Chỉ cập nhật các thông tin được phép (Không cho đổi Email hoặc Role ở đây)
        if (requestData.getHoTen() != null) {
            nguoiDung.setHoTen(requestData.getHoTen());
        }
        if (requestData.getSoDienThoai() != null) {
            nguoiDung.setSoDienThoai(requestData.getSoDienThoai());
        }
        if (requestData.getDiaChi() != null) {
            nguoiDung.setDiaChi(requestData.getDiaChi());
        }
        
        // Cập nhật ngày giờ (nếu có trường Ngày Cập Nhật)
        // nguoiDung.setNgayCapNhat(new java.util.Date()); 

        NguoiDung updatedUser = nguoiDungRepository.save(nguoiDung);
        
        return ResponseEntity.ok(updatedUser);
    }
}