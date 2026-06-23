package com.velora.website.Repository;

import com.velora.website.Entity.DoanhThuNgay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface DoanhThuNgayRepository extends JpaRepository<DoanhThuNgay, Integer> {
    // Tìm các ngày thuộc về một tháng & năm cụ thể, sắp xếp từ mùng 1 đến cuối tháng
    @Query("SELECT d FROM DoanhThuNgay d WHERE MONTH(d.ngay) = :thang AND YEAR(d.ngay) = :nam ORDER BY d.ngay ASC")
    List<DoanhThuNgay> findByThangAndNam(@Param("thang") int thang, @Param("nam") int nam);
}