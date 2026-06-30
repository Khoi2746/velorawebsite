package com.velora.website.Controller;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Service.EmailService; // BỔ SUNG: IMPORT THƯ VIỆN GỬI MAIL

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
    
    // TẠO CÔNG CỤ BĂM MẬT KHẨU
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // BỔ SUNG: GỌI SERVICE GỬI MAIL VÀO ĐÂY
    private final EmailService emailService;

    NguoiDungController(NguoiDungRepository nguoiDungRepository, EmailService emailService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.emailService = emailService;
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
        String passThucTe = "123456"; // Biến tạm để lưu pass thô gửi qua mail
        if (nguoiDung.getMatKhauMaHoa() == null || nguoiDung.getMatKhauMaHoa().trim().isEmpty()) {
            // Nếu bỏ trống -> Băm mật khẩu mặc định 123456
            nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        } else {
            // Nếu có nhập mật khẩu -> Băm chính mật khẩu đó
            passThucTe = nguoiDung.getMatKhauMaHoa();
            nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(passThucTe));
        }

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);

        // ==========================================
        // TÍNH NĂNG MỚI: GỬI MAIL KHI TẠO TÀI KHOẢN
        // ==========================================
        String tieuDe = "Chào mừng đến với Hệ thống Velora Clock!";
        String noiDung = "Xin chào " + saved.getHoTen() + ",\n\n"
                + "Tài khoản của bạn đã được quản trị viên khởi tạo thành công trên hệ thống.\n"
                + "- Email đăng nhập: " + saved.getEmail() + "\n"
                + "- Mật khẩu khởi tạo: " + passThucTe + "\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu sớm nhất để bảo mật tài khoản.\n"
                + "Trân trọng,\nĐội ngũ Velora.";
        emailService.sendEmail(saved.getEmail(), tieuDe, noiDung);

        return ResponseEntity.ok(saved);
    }

  // SỬA TẠI ĐÂY: Thêm "/thanh-vien" vào trước đường dẫn
@PatchMapping("/thanh-vien/{id}/doi-trang-thai")
@Transactional
public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
    NguoiDung user = nguoiDungRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

    if (user.getVaiTros().stream().anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa tài khoản Quản trị viên!");
    }

    String trangThaiMoi = "HOAT_DONG".equalsIgnoreCase(user.getTrangThai()) ? "KHOA" : "HOAT_DONG";
    user.setTrangThai(trangThaiMoi);
    user.setNgayCapNhat(new Date());

    NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);

    // Gửi mail chi tiết theo trạng thái
    try {
        boolean isKhoa = "KHOA".equals(trangThaiMoi);
        String tieuDe = isKhoa ? "CẢNH BÁO: Tài khoản của bạn đã bị KHÓA" : "Thông báo: Tài khoản đã được MỞ KHÓA";
        String noiDung = "Xin chào " + updatedUser.getHoTen() + ",\n\n"
                + (isKhoa 
                    ? "Tài khoản của bạn đã bị khóa bởi quản trị viên. Bạn sẽ không thể truy cập hệ thống lúc này." 
                    : "Tài khoản của bạn đã được quản trị viên mở khóa. Bạn có thể đăng nhập lại bình thường.")
                + "\n\nTrân trọng,\nĐội ngũ Velora.";
        emailService.sendEmail(updatedUser.getEmail(), tieuDe, noiDung);
    } catch (Exception e) {
        System.err.println("Lỗi gửi mail trạng thái: " + e.getMessage());
    }

    return ResponseEntity.ok(updatedUser);
}
    @DeleteMapping("/thanh-vien/{id}")
    public ResponseEntity<?> xoaVinhVienThanhVien(@PathVariable Integer id) {
        try {
            return nguoiDungRepository.findById(id).map(user -> {
                if (user.getVaiTros() != null) {
                    user.getVaiTros().clear();
                    nguoiDungRepository.save(user);
                }
                
                // Lấy email ra trước khi xóa khỏi DB để còn biết đường gửi
                String emailTo = user.getEmail();
                String hoTenTo = user.getHoTen();
                
                nguoiDungRepository.delete(user);

                // ==========================================
                // TÍNH NĂNG MỚI: GỬI MAIL KHI XÓA TÀI KHOẢN
                // ==========================================
                String tieuDe = "Thông báo XÓA tài khoản Velora Clock";
                String noiDung = "Xin chào " + hoTenTo + ",\n\n"
                        + "Chúng tôi rất tiếc phải thông báo rằng tài khoản của bạn đã bị xóa vĩnh viễn khỏi hệ thống Velora Clock bởi Quản trị viên.\n"
                        + "Mọi thắc mắc vui lòng phản hồi lại email này.\n"
                        + "Trân trọng,\nĐội ngũ Velora.";
                emailService.sendEmail(emailTo, tieuDe, noiDung);

                return ResponseEntity.ok("Xóa vĩnh viễn tài khoản thành công!");
            }).orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Lỗi: Không thể xóa do tài khoản vẫn đang dính líu dữ liệu khác.");
        }
    }

@PatchMapping("/{id}/doi-trang-thai")
    @Transactional
    public ResponseEntity<?> doiTrangThai(@PathVariable Integer id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (user.getVaiTros().stream().anyMatch(vt -> "ROLE_ADMIN".equalsIgnoreCase(vt.getTenVaiTro()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể khóa tài khoản Quản trị viên!");
        }

        String trangThaiMoi = "HOAT_DONG".equalsIgnoreCase(user.getTrangThai()) ? "KHOA" : "HOAT_DONG";
        user.setTrangThai(trangThaiMoi);
        user.setNgayCapNhat(new Date());

        NguoiDung updatedUser = nguoiDungRepository.saveAndFlush(user);

        // Gửi mail chi tiết theo trạng thái
        try {
            boolean isKhoa = "KHOA".equals(trangThaiMoi);
            String tieuDe = isKhoa ? "CẢNH BÁO: Tài khoản của bạn đã bị KHÓA" : "Thông báo: Tài khoản đã được MỞ KHÓA";
            String noiDung = "Xin chào " + updatedUser.getHoTen() + ",\n\n"
                    + (isKhoa 
                        ? "Tài khoản của bạn đã bị khóa bởi quản trị viên. Bạn sẽ không thể truy cập hệ thống lúc này." 
                        : "Tài khoản của bạn đã được quản trị viên mở khóa. Bạn có thể đăng nhập lại bình thường.")
                    + "\n\nTrân trọng,\nĐội ngũ Velora.";
            emailService.sendEmail(updatedUser.getEmail(), tieuDe, noiDung);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail trạng thái: " + e.getMessage());
        }

        return ResponseEntity.ok(updatedUser);
    }
}