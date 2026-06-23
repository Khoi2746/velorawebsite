package com.velora.website.Repository;

import com.velora.website.Entity.DoanhThuThang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoanhThuThangRepository extends JpaRepository<DoanhThuThang, Integer> {
    // Spring Boot thông minh tự hiểu hàm này: Tìm theo Năm, sắp xếp Tháng tăng dần
    List<DoanhThuThang> findByNamOrderByThangAsc(Integer nam);
}