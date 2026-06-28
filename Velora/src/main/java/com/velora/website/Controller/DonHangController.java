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
@RequiredArgsConstructor // Tự động tạo Constructor giúp tránh gạch vàng cảnh báo hệ thống
public class DonHangController {

    private final DonHangRepository donHangRepository;

    /**
     * Lấy danh sách tất cả các đơn hàng
     */
    @GetMapping
    public ResponseEntity<List<DonHang>> getAllDonHang() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }

    /**
     * API CẬP NHẬT TRẠNG THÁI LINH HOẠT CHUYỂN BƯỚC HOẶC HỦY ĐƠN
     * LOGIC ĐỒNG BỘ: Khi cập nhật thành "Đã giao hàng" -> Tự động chuyển trạng thái thanh toán sang "Đã thanh toán"
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> capNhatTrangThai(@PathVariable Integer id, @RequestParam String trangThaiMoi) {
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        
        // 1. Cập nhật trạng thái mới nhận từ RequestParam vào trường chuẩn trangThaiDonHang
        donHang.setTrangThaiDonHang(trangThaiMoi);
        
        // 2. LOGIC ĐỒNG BỘ: Nếu trạng thái giao là "Đã giao hàng", ép trạng thái thanh toán thành "Đã thanh toán"
        if (trangThaiMoi != null && "Đã giao hàng".equalsIgnoreCase(trangThaiMoi.trim())) {
            donHang.setTrangThaiThanhToan("Đã thanh toán");
        }

        // 3. Lưu thực thể đơn hàng đã đồng bộ vào database
        donHangRepository.save(donHang);
        
        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng và đồng bộ thanh toán thành công!");
    }
}