package com.velora.website.Repository;

import com.velora.website.Entity.DoanhThuNgay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DoanhThuNgayRepository extends JpaRepository<DoanhThuNgay, Integer> {
    
    @Query("SELECT d FROM DoanhThuNgay d WHERE MONTH(d.ngay) = :thang AND YEAR(d.ngay) = :nam ORDER BY d.ngay ASC")
    List<DoanhThuNgay> findByThangAndNam(@Param("thang") int thang, @Param("nam") int nam);

    // THÊM HÀM NÀY: Tìm chính xác record của ngày hôm nay để cộng dồn tiền
    @Query("SELECT d FROM DoanhThuNgay d WHERE d.ngay = :ngay")
    DoanhThuNgay findByNgayChinhXac(@Param("ngay") LocalDate ngay);
}