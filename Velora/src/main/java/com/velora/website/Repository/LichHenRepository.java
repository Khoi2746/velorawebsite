package com.velora.website.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Thêm dòng import này để Java nhận diện được class LichHen
import com.velora.website.Entity.LichHen;

@Repository
public interface LichHenRepository extends JpaRepository<LichHen, Integer> {
    // Có thể thêm các hàm tìm kiếm theo trạng thái, theo user (nếu cần)
}