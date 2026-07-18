package com.velora.website.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
// Thêm dòng import này để Java nhận diện được class LichHen
import com.velora.website.Entity.LichHen;

public interface LichHenRepository extends JpaRepository<LichHen, Integer> {
    // Có thể thêm các hàm tìm kiếm theo trạng thái, theo user (nếu cần)
}