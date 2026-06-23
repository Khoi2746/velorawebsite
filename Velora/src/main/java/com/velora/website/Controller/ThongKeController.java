package com.velora.website.Controller;

import com.velora.website.Entity.DoanhThuNgay;
import com.velora.website.Entity.DoanhThuThang;
import com.velora.website.Repository.DoanhThuNgayRepository;
import com.velora.website.Repository.DoanhThuThangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ThongKeController {

    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;

    // 1. API Lấy thống kê từng ngày trong tháng
    // URL Demo: http://localhost:8080/api/thong-ke/ngay?thang=6&nam=2026
    @GetMapping("/ngay")
    public ResponseEntity<List<DoanhThuNgay>> getDoanhThuNgay(
            @RequestParam int thang,
            @RequestParam int nam) {
        List<DoanhThuNgay> data = doanhThuNgayRepository.findByThangAndNam(thang, nam);
        return ResponseEntity.ok(data);
    }

    // 2. API Lấy thống kê 12 tháng trong 1 năm
    // URL Demo: http://localhost:8080/api/thong-ke/thang?nam=2026
    @GetMapping("/thang")
    public ResponseEntity<List<DoanhThuThang>> getDoanhThuThang(@RequestParam int nam) {
        List<DoanhThuThang> data = doanhThuThangRepository.findByNamOrderByThangAsc(nam);
        return ResponseEntity.ok(data);
    }
}