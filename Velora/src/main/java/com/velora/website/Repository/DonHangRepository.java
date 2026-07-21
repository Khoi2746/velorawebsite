package com.velora.website.Repository;

import com.velora.website.Entity.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    // 1. Tìm đơn hàng theo mã code
    Optional<DonHang> findByMaDonHangCode(String maDonHangCode);

    // 2. Tìm danh sách đơn hàng theo người dùng
    List<DonHang> findByMaNguoiDungOrderByMaDonHangDesc(Integer maNguoiDung);

    // 2b. Tìm danh sách đơn hàng theo người dùng (có phân trang)
    Page<DonHang> findByMaNguoiDungOrderByMaDonHangDesc(Integer maNguoiDung, Pageable pageable);

    // 3. Tìm đơn hàng theo trạng thái đơn hàng
    List<DonHang> findByTrangThaiDonHangOrderByMaDonHangDesc(String trangThaiDonHang);

    // 4. Thêm chi tiết đơn hàng trực tiếp
    @Modifying
    @Query(value = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, GiaLucMua) " +
                   "VALUES (:maDonHang, :maSanPham, :soLuong, :giaLucMua)", nativeQuery = true)
    void insertChiTietDonHang(
        @Param("maDonHang") Integer maDonHang, 
        @Param("maSanPham") Integer maSanPham, 
        @Param("soLuong") Integer soLuong, 
        @Param("giaLucMua") double giaLucMua
    );

    // 5. ĐÃ FIX: Bỏ cột GiaKhuyenMai bị lỗi, lấy trực tiếp GiaBan từ bảng SanPham
    @Modifying
    @Query(value = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, GiaLucMua) " +
                   "SELECT :maDonHang, gh.MaSanPham, SUM(gh.SoLuong), sp.GiaBan " +
                   "FROM GioHang gh " +
                   "JOIN SanPham sp ON gh.MaSanPham = sp.MaSanPham " +
                   "WHERE gh.MaNguoiDung = :maNguoiDung " +
                   "GROUP BY gh.MaSanPham, sp.GiaBan", nativeQuery = true)
    void chuyenGioHangSangChiTietDonHang(@Param("maDonHang") Integer maDonHang, @Param("maNguoiDung") Integer maNguoiDung);

    // 6. Xóa toàn bộ giỏ hàng của người dùng
    @Modifying
    @Query(value = "DELETE FROM GioHang WHERE MaNguoiDung = :maNguoiDung", nativeQuery = true)
    void xoaToanBoGioHangCuaUser(@Param("maNguoiDung") Integer maNguoiDung);

    // 7. Xóa 1 sản phẩm cụ thể khỏi giỏ hàng
    @Modifying
    @Query(value = "DELETE FROM GioHang WHERE MaNguoiDung = :maNguoiDung AND MaSanPham = :maSanPham", nativeQuery = true)
    void xoaSanPhamKhoiGioHang(@Param("maNguoiDung") Integer maNguoiDung, @Param("maSanPham") Integer maSanPham);

    // 8. Trừ số lượng tồn kho theo đơn hàng
    @Modifying
    @Query(value = "UPDATE sp SET sp.SoLuongTonKho = sp.SoLuongTonKho - ct.SoLuong " +
                   "FROM SanPham sp JOIN ChiTietDonHang ct ON sp.MaSanPham = ct.MaSanPham " +
                   "WHERE ct.MaDonHang = :maDonHang AND sp.SoLuongTonKho >= ct.SoLuong", nativeQuery = true)
    void truSoLuongTonKhoTheoMaDon(@Param("maDonHang") Integer maDonHang);
}