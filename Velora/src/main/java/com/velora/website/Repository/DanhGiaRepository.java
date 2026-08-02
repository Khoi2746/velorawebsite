package com.velora.website.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.velora.website.Entity.DanhGia;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    // Tìm tất cả đánh giá thuộc về một mã sản phẩm cụ thể
    List<DanhGia> findBySanPham_MaSanPham(Integer maSanPham);
}