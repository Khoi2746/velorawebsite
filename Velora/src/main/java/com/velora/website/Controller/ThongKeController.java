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
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true") // <-- THÊM DÒNG NÀY ĐỂ MỞ CỬA CORS
@RequiredArgsConstructor
public class ThongKeController {

    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;

    @GetMapping("/ngay")
    public ResponseEntity<List<DoanhThuNgay>> getDoanhThuNgay(
            @RequestParam int thang,
            @RequestParam int nam) {
        List<DoanhThuNgay> data = doanhThuNgayRepository.findByThangAndNam(thang, nam);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/thang")
    public ResponseEntity<List<DoanhThuThang>> getDoanhThuThang(@RequestParam int nam) {
        List<DoanhThuThang> data = doanhThuThangRepository.findByNamOrderByThangAsc(nam);
        return ResponseEntity.ok(data);
    }
}