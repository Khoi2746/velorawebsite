package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NguoiDungController {

    private final NguoiDungRepository nguoiDungRepository;

    NguoiDungController(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @GetMapping("/thanh-vien")
    public ResponseEntity<List<NguoiDung>> layToanBoThanhVien() {
        return ResponseEntity.ok(nguoiDungRepository.findAll());
    }

    @PostMapping("/thanh-vien")
    public ResponseEntity<?> themMoiThanhVien(@RequestBody NguoiDung nguoiDung) {
        if (nguoiDungRepository.existsByEmail(nguoiDung.getEmail())) {
            return ResponseEntity.badRequest().body("Email này đã tồn tại trong hệ thống!");
        }
        
        nguoiDung.setNgayTao(new Date());
        nguoiDung.setNgayCapNhat(new Date());
        if (nguoiDung.getTrangThai() == null) {
            nguoiDung.setTrangThai("HOAT_DONG");
        }
        
        if (nguoiDung.getMatKhauMaHoa() == null) {
            nguoiDung.setMatKhauMaHoa("$2a$10$sTfIDAN4qV1QZXN.hGhtueWm9zV94BwhoYt2jyaRTuooS8QP7cv9.");
        }

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/thanh-vien/{id}")
    public ResponseEntity<?> capNhatThanhVien(@PathVariable Integer id, @RequestBody NguoiDung form) {
        return nguoiDungRepository.findById(id).map(user -> {
            user.setHoTen(form.getHoTen());
            user.setSoDienThoai(form.getSoDienThoai());
            user.setDiaChi(form.getDiaChi());
            user.setNgayCapNhat(new Date());
            
            if (form.getVaiTros() != null) {
                user.setVaiTros(form.getVaiTros());
            }
            
            nguoiDungRepository.save(user);
            return ResponseEntity.ok("Cập nhật thông tin thành công!");
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/thanh-vien/{id}")
    public ResponseEntity<?> xoaVinhVienThanhVien(@PathVariable Integer id) {
        try {
            return nguoiDungRepository.findById(id).map(user -> {
                if (user.getVaiTros() != null) {
                    user.getVaiTros().clear();
                    nguoiDungRepository.save(user);
                }
                
                // 2. Thực hiện xóa vĩnh viễn
                nguoiDungRepository.delete(user);
                return ResponseEntity.ok("Xóa vĩnh viễn tài khoản thành công!");
            }).orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            e.printStackTrace(); // In ra log để xem lỗi (nếu có)
            return ResponseEntity.badRequest().body("Lỗi: Không thể xóa do tài khoản vẫn đang dính líu dữ liệu khác.");
        }
    }

    @PatchMapping("/thanh-vien/{id}/doi-trang-thai")
    public ResponseEntity<?> doiTrangThaiThanhVien(@PathVariable Integer id) {
        return nguoiDungRepository.findById(id).map(user -> {
            if ("HOAT_DONG".equals(user.getTrangThai())) {
                user.setTrangThai("BI_KHOA");
            } else {
                user.setTrangThai("HOAT_DONG");
            }
            user.setNgayCapNhat(new Date());
            nguoiDungRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}