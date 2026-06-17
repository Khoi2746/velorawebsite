package com.velora.website.Controller;

import com.velora.website.Entity.DonHang;
import com.velora.website.Repository.DonHangRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/don-hang")
@CrossOrigin("*") // Cho phép Vue.js gọi sang
@RequiredArgsConstructor // Chuẩn code xịn, không dùng @Autowired
public class DonHangController {

    private final DonHangRepository donHangRepository;

    // Lấy danh sách toàn bộ đơn hàng
    @GetMapping
    public ResponseEntity<List<DonHang>> getAllDonHang() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }
}