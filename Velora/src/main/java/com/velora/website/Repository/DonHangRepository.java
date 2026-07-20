package com.velora.website.Repository;

import com.velora.website.Entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    Optional<DonHang> findByMaDonHangCode(String maDonHangCode);

    List<DonHang> findByMaNguoiDungOrderByMaDonHangDesc(Integer maNguoiDung);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, GiaLucMua) " +
                   "VALUES (:maDonHang, :maSanPham, :soLuong, :giaLucMua)", nativeQuery = true)
    void insertChiTietDonHang(
        @Param("maDonHang") Integer maDonHang, 
        @Param("maSanPham") Integer maSanPham, 
        @Param("soLuong") Integer soLuong, 
        @Param("giaLucMua") Double giaLucMua
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE SanPham SET SoLuongTonKho = SoLuongTonKho - ct.SoLuong " +
                   "FROM SanPham sp JOIN ChiTietDonHang ct ON sp.MaSanPham = ct.MaSanPham " +
                   "WHERE ct.MaDonHang = :maDonHang AND sp.SoLuongTonKho >= ct.SoLuong", nativeQuery = true)
    void truSoLuongTonKhoTheoMaDon(@Param("maDonHang") Integer maDonHang);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM GioHang WHERE MaNguoiDung = :maNguoiDung AND MaSanPham = :maSanPham", nativeQuery = true)
    void xoaSanPhamKhoiGioHang(@Param("maNguoiDung") Integer maNguoiDung, @Param("maSanPham") Integer maSanPham);
}