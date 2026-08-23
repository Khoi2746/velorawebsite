package com.velora.website.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.velora.website.Entity.NguoiDung;

import java.util.List;
import java.util.Optional;

// 🔥 SỬA TẠI ĐÂY: Chuyển hoàn toàn từ Long về Integer để đồng bộ với SQL Server và Controller
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    /**
     * Hàm tìm kiếm người dùng dựa trên Email.
     * Spring Data JPA sẽ tự động dịch tên hàm này thành câu lệnh SQL:
     * SELECT * FROM NguoiDung WHERE Email = ?
     * @param email Email do người dùng nhập vào từ form Vue.js
     * @return Optional<NguoiDung> (Giúp tránh lỗi NullPointerException nếu không tìm thấy)
     */
    Optional<NguoiDung> findByEmail(String email);

    /**
     * Hàm kiểm tra xem một Email đã tồn tại trong hệ thống hay chưa.
     * Cực kỳ cần thiết cho chức năng Đăng ký (Register) để chặn trùng tài khoản.
     * @param email Email cần kiểm tra
     * @return true nếu đã có người dùng, false nếu email chưa ai đăng ký
     */
    boolean existsByEmail(String email);

    // Sử dụng EntityGraph tự động JOIN FETCH chính xác thuộc tính vaiTros tránh lỗi Lazy Loading
    @Override
    @EntityGraph(attributePaths = {"vaiTros"})
    @NonNull
    List<NguoiDung> findAll();

    List<NguoiDung> findByTrangThaiOrThoiGianCamBinhLuanIsNotNull(String trangThai);

    Optional<NguoiDung> findByProviderAndProviderId(String provider, String providerId);
}