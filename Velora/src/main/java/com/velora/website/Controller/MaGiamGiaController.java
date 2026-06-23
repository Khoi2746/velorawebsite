package com.velora.website.Controller;

import com.velora.website.Entity.MaGiamGia;
import com.velora.website.Repository.MaGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ma-giam-gia")

public class MaGiamGiaController {

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

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
}