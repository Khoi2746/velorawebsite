package com.velora.website.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.velora.website.Entity.ThuongHieu;
import com.velora.website.Repository.ThuongHieuRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {
    
    private final ThuongHieuRepository thuongHieuRepository;

    // 1. LẤY TOÀN BỘ DANH SÁCH THƯƠNG HIỆU
    @GetMapping
    public ResponseEntity<List<ThuongHieu>> getAllThuongHieu() {
        return ResponseEntity.ok(thuongHieuRepository.findAll());
    }

    // 2. THÊM MỚI THƯƠNG HIỆU
    @PostMapping
    public ResponseEntity<?> createThuongHieu(@RequestBody ThuongHieu thuongHieu) {
        try {
            ThuongHieu savedBrand = thuongHieuRepository.save(thuongHieu);
            return ResponseEntity.ok(savedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi dữ liệu đầu vào. Không thể thêm thương hiệu!");
        }
    }

    // 3. CẬP NHẬT THÔNG TIN / ĐỔI TRẠNG THÁI NHANH
    @PutMapping("/{id}")
    public ResponseEntity<?> updateThuongHieu(@PathVariable Integer id, @RequestBody ThuongHieu dataGuiLen) {
        Optional<ThuongHieu> optional = thuongHieuRepository.findById(id);
        
        if (optional.isPresent()) {
            ThuongHieu dbBrand = optional.get();
            
            // Đồng bộ dữ liệu từ form Vue 3 gửi lên CSDL
            dbBrand.setTenThuongHieu(dataGuiLen.getTenThuongHieu());
            dbBrand.setLogoThuongHieu(dataGuiLen.getLogoThuongHieu());
            dbBrand.setMoTaNgan(dataGuiLen.getMoTaNgan());
            dbBrand.setWebsiteThuongHieu(dataGuiLen.getWebsiteThuongHieu());
            dbBrand.setTrangThai(dataGuiLen.getTrangThai());
            
            return ResponseEntity.ok(thuongHieuRepository.save(dbBrand));
        }
        return ResponseEntity.notFound().build();
    }

    // 4. XÓA THƯƠNG HIỆU (Có bẫy lỗi ràng buộc khóa ngoại với bảng Sản Phẩm)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteThuongHieu(@PathVariable Integer id) {
        if (thuongHieuRepository.existsById(id)) {
            try {
                thuongHieuRepository.deleteById(id);
                return ResponseEntity.ok("Xóa đối tác thương hiệu thành công!");
            } catch (Exception e) {
                // Trả về thông báo lỗi nếu thương hiệu này đang có sản phẩm thuộc về nó
                return ResponseEntity.badRequest().body("Không thể xóa! Thương hiệu này đã có sản phẩm ràng buộc trong kho hàng.");
            }
        }
        return ResponseEntity.notFound().build();
    }
}