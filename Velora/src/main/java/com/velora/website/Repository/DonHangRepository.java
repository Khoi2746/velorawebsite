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

// =========================================================================
// [PHẦN 1: KHAI BÁO INTERFACE REPOSITORY & KẾ THỪA JPA REPOSITORY]
// =========================================================================
/**
 * Interface quản lý các thao tác CRUD và truy vấn nâng cao cho bảng DonHang
 * Kế thừa JpaRepository<DonHang, Integer>: Hỗ trợ sẵn các hàm save(), findById(), findAll(), deleteById()
 */
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    // =====================================================================
    // [PHẦN 2: CÁC HÀM TRUY VẤN TỰ ĐỘNG TẠO SẴN CỦA SPRING DATA JPA]
    // =====================================================================

    /**
     * 1. Tìm đơn hàng theo mã định danh Code chính xác 100%
     * Ví dụ: 'VELORA1001'
     */
    Optional<DonHang> findByMaDonHangCode(String maDonHangCode);

    /**
     * 1b. Tìm đơn hàng theo mã Code linh hoạt (Chứa chuỗi con, bỏ qua dấu # hoặc tiền tố)
     * Ví dụ: Tìm '1001' sẽ khớp '#VELORA1001'
     */
    Optional<DonHang> findByMaDonHangCodeContaining(String maDonHangCode);

    /**
     * 2. Lấy toàn bộ danh sách đơn hàng của một người dùng (Sắp xếp đơn mới nhất lên đầu)
     */
    List<DonHang> findByMaNguoiDungOrderByMaDonHangDesc(Integer maNguoiDung);

    /**
     * 2b. Lấy danh sách đơn hàng của một người dùng có hỗ trợ phân trang (Pageable)
     * Dùng cho giao diện tải danh sách theo trang 1, 2, 3...
     */
    Page<DonHang> findByMaNguoiDungOrderByMaDonHangDesc(Integer maNguoiDung, Pageable pageable);

    /**
     * 3. Lọc danh sách đơn hàng theo trạng thái xử lý (CHO_XU_LY, DA_GIAO, DA_HUY...)
     */
    List<DonHang> findByTrangThaiDonHangOrderByMaDonHangDesc(String trangThaiDonHang);
    // THAY THẾ: Lọc đơn hàng theo cả trạng thái đơn VÀ trạng thái thanh toán:
    // List<DonHang> findByTrangThaiDonHangAndTrangThaiThanhToan(String trangThaiDonHang, String trangThaiThanhToan);

    // =====================================================================
    // [PHẦN 3: NATIVE QUERY - THAO TÁC TRỰC TIẾP BẢNG CHI TIẾT ĐƠN HÀNG]
    // =====================================================================

    /**
     * 4. Thêm 1 sản phẩm vào bảng ChiTietDonHang khi khách bấm "Mua Ngay"
     * @Modifying: Bắt buộc khai báo khi thực hiện câu lệnh INSERT, UPDATE, DELETE thuần
     * nativeQuery = true: Thực thi câu lệnh SQL trực tiếp xuống hệ quản trị CSDL
     */
    @Modifying
    @Query(value = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, GiaLucMua) " +
                   "VALUES (:maDonHang, :maSanPham, :soLuong, :giaLucMua)", nativeQuery = true)
    void insertChiTietDonHang(
        @Param("maDonHang") Integer maDonHang, 
        @Param("maSanPham") Integer maSanPham, 
        @Param("soLuong") Integer soLuong, 
        @Param("giaLucMua") double giaLucMua
    );

    /**
     * 5. Chuyển toàn bộ các sản phẩm từ GioHang sang ChiTietDonHang khi đặt hàng
     * Lấy trực tiếp giá bán hiện tại (GiaBan) từ bảng SanPham và gộp số lượng bằng hàm SUM()
     */
    @Modifying
    @Query(value = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, GiaLucMua) " +
                   "SELECT :maDonHang, gh.MaSanPham, SUM(gh.SoLuong), sp.GiaBan " +
                   "FROM GioHang gh " +
                   "JOIN SanPham sp ON gh.MaSanPham = sp.MaSanPham " +
                   "WHERE gh.MaNguoiDung = :maNguoiDung " +
                   "GROUP BY gh.MaSanPham, sp.GiaBan", nativeQuery = true)
    void chuyenGioHangSangChiTietDonHang(
        @Param("maDonHang") Integer maDonHang, 
        @Param("maNguoiDung") Integer maNguoiDung
    );

    // =====================================================================
    // [PHẦN 4: NATIVE QUERY - QUẢN LÝ DỌN DẸP GIỎ HÀNG]
    // =====================================================================

    /**
     * 6. Xóa sạch toàn bộ sản phẩm trong giỏ hàng của người dùng sau khi đã tạo đơn xong
     */
    @Modifying
    @Query(value = "DELETE FROM GioHang WHERE MaNguoiDung = :maNguoiDung", nativeQuery = true)
    void xoaToanBoGioHangCuaUser(@Param("maNguoiDung") Integer maNguoiDung);

    /**
     * 7. Xóa 1 sản phẩm chỉ định khỏi giỏ hàng sau khi người dùng mua riêng sản phẩm đó
     */
    @Modifying
    @Query(value = "DELETE FROM GioHang WHERE MaNguoiDung = :maNguoiDung AND MaSanPham = :maSanPham", nativeQuery = true)
    void xoaSanPhamKhoiGioHang(
        @Param("maNguoiDung") Integer maNguoiDung, 
        @Param("maSanPham") Integer maSanPham
    );

    // =====================================================================
    // [PHẦN 5: NATIVE QUERY - CẬP NHẬT TRỪ SỐ LƯỢNG TỒN KHO]
    // =====================================================================

    /**
     * 8. Trừ số lượng tồn kho của các sản phẩm có trong đơn hàng khi thanh toán thành công
     * Điều kiện ràng buộc an toàn: sp.SoLuongTonKho >= ct.SoLuong (Tránh tồn kho bị âm)
     */
    // DÒNG MẶC ĐỊNH: Cú pháp UPDATE JOIN chuẩn của SQL Server (T-SQL)
    @Modifying
    @Query(value = "UPDATE sp SET sp.SoLuongTonKho = sp.SoLuongTonKho - ct.SoLuong " +
                   "FROM SanPham sp JOIN ChiTietDonHang ct ON sp.MaSanPham = ct.MaSanPham " +
                   "WHERE ct.MaDonHang = :maDonHang AND sp.SoLuongTonKho >= ct.SoLuong", nativeQuery = true)
    void truSoLuongTonKhoTheoMaDon(@Param("maDonHang") Integer maDonHang);

    // THAY THẾ: Cú pháp UPDATE JOIN chuẩn khi sử dụng MySQL:
    // @Modifying
    // @Query(value = "UPDATE SanPham sp " +
    //                "JOIN ChiTietDonHang ct ON sp.MaSanPham = ct.MaSanPham " +
    //                "SET sp.SoLuongTonKho = sp.SoLuongTonKho - ct.SoLuong " +
    //                "WHERE ct.MaDonHang = :maDonHang AND sp.SoLuongTonKho >= ct.SoLuong", nativeQuery = true)
    // void truSoLuongTonKhoTheoMaDon(@Param("maDonHang") Integer maDonHang);

    // THAY THẾ: Cộng lại tồn kho khi đơn hàng bị hủy bỏ (Hoàn kho):
    // @Modifying
    // @Query(value = "UPDATE sp SET sp.SoLuongTonKho = sp.SoLuongTonKho + ct.SoLuong " +
    //                "FROM SanPham sp JOIN ChiTietDonHang ct ON sp.MaSanPham = ct.MaSanPham " +
    //                "WHERE ct.MaDonHang = :maDonHang", nativeQuery = true)
    // void hoanLaiSoLuongTonKhoTheoMaDon(@Param("maDonHang") Integer maDonHang);
}