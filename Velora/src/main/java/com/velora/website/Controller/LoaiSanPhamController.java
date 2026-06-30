package com.velora.website.Controller;

import com.velora.website.Entity.LoaiSanPham;
import com.velora.website.Repository.LoaiSanPhamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-san-pham")
public class LoaiSanPhamController {

    private final LoaiSanPhamRepository loaiSanPhamRepository;

    LoaiSanPhamController(LoaiSanPhamRepository loaiSanPhamRepository) {
        this.loaiSanPhamRepository = loaiSanPhamRepository;
    }

    // 1. Lấy danh sách loại sản phẩm (Vue: loadCategories)
    @GetMapping
    public ResponseEntity<List<LoaiSanPham>> getAllCategories() {
        return ResponseEntity.ok(loaiSanPhamRepository.findAll());
    }

    // 2. Thêm mới loại sản phẩm (Vue: saveCategory - POST)
    @PostMapping
    public ResponseEntity<LoaiSanPham> createCategory(@RequestBody LoaiSanPham loaiSanPham) {
        LoaiSanPham saved = loaiSanPhamRepository.save(loaiSanPham);
        return ResponseEntity.ok(saved);
    }

    // 3. Cập nhật loại sản phẩm (Vue: saveCategory - PUT)
    @PutMapping("/{id}")
    public ResponseEntity<LoaiSanPham> updateCategory(@PathVariable Long id, @RequestBody LoaiSanPham categoryDetails) {
        return loaiSanPhamRepository.findById(id).map(category -> {
            category.setTenLoai(categoryDetails.getTenLoai());
            category.setMoTa(categoryDetails.getMoTa());
            LoaiSanPham updated = loaiSanPhamRepository.save(category);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Xóa loại sản phẩm (Vue: deleteCategory - DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return loaiSanPhamRepository.findById(id).map(category -> {
            loaiSanPhamRepository.delete(category);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}