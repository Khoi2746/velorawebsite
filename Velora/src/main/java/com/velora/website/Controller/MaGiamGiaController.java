package com.velora.website.Controller;

import com.velora.website.Entity.MaGiamGia;
import com.velora.website.Repository.MaGiamGiaRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/ma-giam-gia")
public class MaGiamGiaController {

    private final MaGiamGiaRepository maGiamGiaRepository;

    MaGiamGiaController(MaGiamGiaRepository maGiamGiaRepository) {
        this.maGiamGiaRepository = maGiamGiaRepository;
    }

    @GetMapping
    public ResponseEntity<?> layDanhSach(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(maGiamGiaRepository.findAll(PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<?> taoMa(@RequestBody MaGiamGia maGiamGia) {
        if (maGiamGiaRepository.existsByMaCode(maGiamGia.getMaCode())) {
            return ResponseEntity.badRequest().body("Mã giảm giá này đã tồn tại trên hệ thống!");
        }
        return ResponseEntity.ok(maGiamGiaRepository.save(maGiamGia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> capNhat(@PathVariable Long id, @RequestBody MaGiamGia dataCapNhat) {
        MaGiamGia tonTai = maGiamGiaRepository.findById(id).orElse(null);
        if (tonTai == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy mã giảm giá cần cập nhật!");
        }
        
        tonTai.setPhanTramGiam(dataCapNhat.getPhanTramGiam());
        tonTai.setGioiHanSuDung(dataCapNhat.getGioiHanSuDung());
        tonTai.setNgayHetHan(dataCapNhat.getNgayHetHan());
        
        return ResponseEntity.ok(maGiamGiaRepository.save(tonTai));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoa(@PathVariable Long id) {
        if (!maGiamGiaRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Mã giảm giá không tồn tại hoặc đã bị xóa trước đó!");
        }
        maGiamGiaRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa mã giảm giá thành công");
    }

    // ==============================================================
    // API MỚI DÀNH CHO KHÁCH HÀNG KIỂM TRA VOUCHER TRONG GIỎ HÀNG
    // ==============================================================
    @GetMapping("/kiem-tra")
    public ResponseEntity<?> kiemTraVoucher(@RequestParam String code) {
        // Tìm mã giảm giá trong DB (trim khoảng trắng và viết hoa để tránh lỗi gõ sai)
        Optional<MaGiamGia> optVoucher = maGiamGiaRepository.findByMaCode(code.trim().toUpperCase());

        if (optVoucher.isEmpty()) {
            return ResponseEntity.badRequest().body("Mã giảm giá không tồn tại!");
        }

        MaGiamGia voucher = optVoucher.get();

        // 1. Kiểm tra xem đã hết lượt dùng chưa
        if (voucher.getSoLuotDaDung() >= voucher.getGioiHanSuDung()) {
            return ResponseEntity.badRequest().body("Rất tiếc! Mã giảm giá này đã hết lượt sử dụng.");
        }

        // 2. Kiểm tra xem đã hết hạn chưa (Chỉ xét nếu Admin có cài đặt ngày hết hạn)
        if (voucher.getNgayHetHan() != null && voucher.getNgayHetHan().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Mã giảm giá này đã hết hạn!");
        }
        // Vượt qua hết các ải -> Trả thông tin về cho Frontend tính tiền
        return ResponseEntity.ok(voucher);
    }
}