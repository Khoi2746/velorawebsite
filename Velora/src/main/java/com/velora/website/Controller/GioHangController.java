package com.velora.website.Controller;

import com.velora.website.Entity.GioHang;
import com.velora.website.Repository.GioHangRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gio-hang")
public class GioHangController {

    private final GioHangRepository gioHangRepository;

    public GioHangController(GioHangRepository gioHangRepository) {
        this.gioHangRepository = gioHangRepository;
    }

    // API THÊM VÀO GIỎ HÀNG (Logic chính)
    @PostMapping("/them")
    public ResponseEntity<?> themVaoGioHang(@RequestBody GioHang request) {
        // Kiểm tra xem sản phẩm đã có trong giỏ của khách này chưa
        GioHang cartItem = gioHangRepository
                .findByMaNguoiDungAndMaSanPham(request.getMaNguoiDung(), request.getMaSanPham())
                .orElse(null);

        if (cartItem != null) {
            // Nếu có rồi -> Cộng dồn số lượng
            cartItem.setSoLuong(cartItem.getSoLuong() + request.getSoLuong());
            gioHangRepository.save(cartItem);
            return ResponseEntity.ok(cartItem);
        } else {
            // Nếu chưa có -> Tạo mới dòng trong DB
            GioHang newItem = new GioHang();
            newItem.setMaNguoiDung(request.getMaNguoiDung());
            newItem.setMaSanPham(request.getMaSanPham());
            newItem.setSoLuong(request.getSoLuong());
            gioHangRepository.save(newItem);
            return ResponseEntity.ok(newItem);
        }
    }

    // 2. API Lấy giỏ hàng của khách
    @GetMapping("/{maNguoiDung}")
    public ResponseEntity<?> layGioHang(@PathVariable Integer maNguoiDung) {
        return ResponseEntity.ok(gioHangRepository.layChiTietGioHang(maNguoiDung));
    }

    // 3. API Xóa 1 sản phẩm khỏi giỏ
    @DeleteMapping("/{maGioHang}")
    public ResponseEntity<?> xoaKhoiGio(@PathVariable Integer maGioHang) {
        gioHangRepository.deleteById(maGioHang);
        return ResponseEntity.ok().build();
    }

    // 4. API Tăng/Giảm số lượng
    @PatchMapping("/{maGioHang}/so-luong")
    public ResponseEntity<?> capNhatSoLuong(@PathVariable Integer maGioHang, @RequestParam Integer soLuong) {
        return gioHangRepository.findById(maGioHang).map(item -> {
            item.setSoLuong(soLuong);
            gioHangRepository.save(item);
            return ResponseEntity.ok(item);
        }).orElse(ResponseEntity.notFound().build());
    }
}