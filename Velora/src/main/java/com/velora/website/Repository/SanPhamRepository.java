package com.velora.website.Repository;

import com.velora.website.Entity.SanPham;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    // Nếu sau này muốn lọc theo tên hay slug thì thêm hàm tại đây
    @Query("SELECT s.tenSanPham, s.giaBan, s.trangThai FROM SanPham s")
    List<Object[]> layDuLieuChoChatbot();
}