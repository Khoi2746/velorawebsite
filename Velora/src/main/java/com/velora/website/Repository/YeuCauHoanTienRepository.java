package com.velora.website.Repository; // Khai báo package chứa interface Repository

import com.velora.website.Entity.YeuCauHoanTien; // Import entity ánh xạ bảng YeuCauHoanTien
import org.springframework.data.jpa.repository.JpaRepository; // Import interface JpaRepository của Spring Data JPA

import java.util.List; // Import cấu trúc danh sách List

public interface YeuCauHoanTienRepository extends JpaRepository<YeuCauHoanTien, Integer> {
    // Công dụng: Kế thừa JpaRepository<Entity, Khóa_Chính> để có sẵn các hàm CRUD (save, findById, findAll, deleteById, count,...) mà không cần viết SQL.

    List<YeuCauHoanTien> findByEmailIgnoreCase(String email);
    // Công dụng: Tìm tất cả yêu cầu hoàn tiền theo email đặt hàng của khách.
    // Chữ 'IgnoreCase' giúp không phân biệt chữ hoa/thường (Ví dụ: Test@gmail.com khớp với test@gmail.com).

    // -------------------------------------------------------------------------
    // CÁC KỊCH BẢN THAY THẾ & MỞ RỘNG TRUY VẤN:
    // -------------------------------------------------------------------------

    // 1. Tìm theo Email và sắp xếp yêu cầu mới nhất lên đầu:
    // List<YeuCauHoanTien> findByEmailIgnoreCaseOrderByNgayTaoDesc(String email);

    // 2. Tìm yêu cầu hoàn tiền theo đúng 1 mã đơn hàng (MaDonHangCode):
    // java.util.Optional<YeuCauHoanTien> findByMaDonHangCode(String maDonHangCode);

    // 3. Kiểm tra xem 1 mã đơn hàng đã gửi yêu cầu hoàn tiền trước đó chưa (Trả về true/false):
    // boolean existsByMaDonHangCode(String maDonHangCode);

    // 4. Lọc danh sách theo Trạng thái duyệt ('CHO_DUYET', 'DA_HOAN_TIEN', 'TU_CHOI_HOAN'):
    // List<YeuCauHoanTien> findByTrangThai(String trangThai);

    // 5. Đếm số lần hoàn tiền thành công theo Email (Dùng để kiểm tra điều kiện Blacklist):
    // long countByEmailIgnoreCaseAndTrangThai(String email, String trangThai);
}