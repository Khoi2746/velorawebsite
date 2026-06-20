package com.velora.website.Repository;

import com.velora.website.Entity.ChiTietPhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, Integer> {
    // Tìm toàn bộ danh sách sản phẩm thuộc về một phiếu nhập cụ thể
    List<ChiTietPhieuNhap> findByMaPhieuNhap(Integer maPhieuNhap);
}