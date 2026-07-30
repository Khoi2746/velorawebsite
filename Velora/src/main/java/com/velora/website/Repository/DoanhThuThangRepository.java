package com.velora.website.Repository;

import com.velora.website.Entity.DoanhThuThang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DoanhThuThangRepository extends JpaRepository<DoanhThuThang, Integer> {
    
    List<DoanhThuThang> findByNamOrderByThangAsc(int nam);

    // THÊM HÀM NÀY: Tìm chính xác record của tháng/năm hiện tại để cộng dồn tiền
    @Query("SELECT d FROM DoanhThuThang d WHERE d.thang = :thang AND d.nam = :nam")
    DoanhThuThang findByThangVaNamChinhXac(@Param("thang") int thang, @Param("nam") int nam);
}