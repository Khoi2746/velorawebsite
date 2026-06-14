package com.velora.website.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.velora.website.Entity.ThuongHieu;
import com.velora.website.Repository.ThuongHieuRepository;

@RestController
@RequestMapping("/api/thuong-hieu")
@CrossOrigin("*") // Mở CORS để Vue tải được dữ liệu
public class ThuongHieuController {

    private ThuongHieuRepository thuongHieuRepository;

    @GetMapping
    public ResponseEntity<List<ThuongHieu>> getAllThuongHieu() {
        return ResponseEntity.ok(thuongHieuRepository.findAll());
    }
}