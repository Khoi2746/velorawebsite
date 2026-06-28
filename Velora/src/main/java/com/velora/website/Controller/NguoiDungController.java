package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // IMPORT THƯ VIỆN NÀY
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NguoiDungController {

    private final NguoiDungRepository nguoiDungRepository;
    
    // TẠO CÔNG CỤ BĂM MẬT KHẨU
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        if (nguoiDung.getTrangThai() == null || nguoiDung.getTrangThai().isEmpty()) {
            nguoiDung.setTrangThai("HOAT_DONG");
        }
        
        // KIỂM TRA VÀ MÃ HÓA MẬT KHẨU TRƯỚC KHI LƯU
        if (nguoiDung.getMatKhauMaHoa() == null || nguoiDung.getMatKhauMaHoa().trim().isEmpty()) {
            // Nếu bỏ trống -> Băm mật khẩu mặc định 123456
            nguoiDung.setMatKhauMaHoa(passwordEncoder.encode("123456"));
        } else {
            // Nếu có nhập mật khẩu (VD: thanhkhoi123) -> Băm chính mật khẩu đó
            nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(nguoiDung.getMatKhauMaHoa()));
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
            
            // Xử lý đổi mật khẩu khi Edit
            if (form.getMatKhauMaHoa() != null && !form.getMatKhauMaHoa().trim().isEmpty()) {
                 user.setMatKhauMaHoa(passwordEncoder.encode(form.getMatKhauMaHoa()));
            }
            
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
                nguoiDungRepository.delete(user);
                return ResponseEntity.ok("Xóa vĩnh viễn tài khoản thành công!");
            }).orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            e.printStackTrace(); 
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