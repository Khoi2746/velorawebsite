package com.velora.website.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.velora.website.Entity.LichHen;
import java.util.List;

public interface LichHenRepository extends JpaRepository<LichHen, Integer> {
    // Bổ sung hàm tìm danh sách lịch hẹn theo Email của người dùng
    List<LichHen> findByEmail(String email);

    // Đếm số lượng lịch hẹn theo trạng thái
    long countByTrangThai(Integer trangThai);
}