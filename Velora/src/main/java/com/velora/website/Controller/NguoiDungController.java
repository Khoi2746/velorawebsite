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
        
        String passThucTe = "123456";
        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        
        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        
        try {
            emailService.sendEmail(saved.getEmail(), "Chào mừng đến Velora Clock", 
                "Tài khoản đã tạo. MK: " + passThucTe);
        } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }
        
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

    // 4. Xóa
    @DeleteMapping("/thanh-vien/{id}")
    public ResponseEntity<?> xoaThanhVien(@PathVariable Integer id) {
        return nguoiDungRepository.findById(id).map(user -> {
            nguoiDungRepository.delete(user);
            return ResponseEntity.ok("Xóa thành công!");
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Đổi trạng thái (KHÓA/MỞ)
    @PatchMapping("/thanh-vien/{id}/doi-trang-thai")
    @Transactional
    public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy!"));

        if (user.getVaiTros().stream().anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa Admin!");
        }

        String trangThaiMoi = "HOAT_DONG".equalsIgnoreCase(user.getTrangThai()) ? "KHOA" : "HOAT_DONG";
        user.setTrangThai(trangThaiMoi);
        user.setNgayCapNhat(new Date());
        NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);

        try {
            emailService.sendEmail(updatedUser.getEmail(), "Thông báo trạng thái", "Tài khoản đã: " + trangThaiMoi);
        } catch (Exception e) { System.err.println("Mail lỗi: " + e.getMessage()); }

        return ResponseEntity.ok(updatedUser);
    }
}