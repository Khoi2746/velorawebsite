package com.velora.website.Controller;

import com.velora.website.Entity.DonHang;
import com.velora.website.Repository.DonHangRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/don-hang")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DonHangController {

    private final DonHangRepository donHangRepository;

    @GetMapping
    public ResponseEntity<List<DonHang>> getAllDonHang() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }

    // API CẬP NHẬT TRẠNG THÁI LINH HOẠT CHUYỂN BƯỚC HOẶC HỦY ĐƠN
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> capNhatTrangThai(@PathVariable Integer id, @RequestParam String trangThaiMoi) {
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        
        // Cập nhật trạng thái mới nhận từ RequestParam công lên
        donHang.setTrangThaiDonHang(trangThaiMoi);
        donHangRepository.save(donHang);
        
        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }
}