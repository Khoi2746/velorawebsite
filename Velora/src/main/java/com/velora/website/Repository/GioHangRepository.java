package com.velora.website.Repository;

import com.velora.website.Entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.Map;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    // Tìm xem user này đã có sản phẩm này trong giỏ chưa
    Optional<GioHang> findByMaNguoiDungAndMaSanPham(Integer maNguoiDung, Integer maSanPham);
    
    // Lấy toàn bộ giỏ hàng của 1 user
    List<GioHang> findByMaNguoiDung(Integer maNguoiDung);

    @Query("SELECT new map(g.maGioHang as maGioHang, g.soLuong as soLuong, s.maSanPham as maSanPham, s.tenSanPham as tenSanPham, s.giaBan as giaBan, s.anhDaiDien as anhDaiDien) " +
           "FROM GioHang g, SanPham s " +
           "WHERE g.maSanPham = s.maSanPham AND g.maNguoiDung = :maNguoiDung")
    List<Map<String, Object>> layChiTietGioHang(@Param("maNguoiDung") Integer maNguoiDung);
    
}