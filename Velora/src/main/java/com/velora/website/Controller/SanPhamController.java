package com.velora.website.Controller;

import com.velora.website.Entity.SanPham;
import com.velora.website.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin("*") // Mở cửa cho Vue ở cổng khác gọi vào
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Lấy tất cả sản phẩm
    @GetMapping
    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    // Lấy sản phẩm theo ID (dùng cho trang chi tiết sau này)
    @GetMapping("/{id}")
    public SanPham getById(@PathVariable Integer id) {
        return sanPhamRepository.findById(id).orElse(null);
    }
}