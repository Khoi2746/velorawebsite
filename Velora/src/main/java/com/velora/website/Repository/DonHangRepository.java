package com.velora.website.Repository;

import com.velora.website.Entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    // Spring Data JPA sẽ tự lo hết các hàm lấy dữ liệu cơ bản
}