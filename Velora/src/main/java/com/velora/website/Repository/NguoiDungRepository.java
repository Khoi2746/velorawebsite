package com.velora.website.Repository;

import com.velora.website.Entity.NguoiDung;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;


import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    /**
     * Tìm kiếm người dùng theo Email.
     * Thường dùng cho chức năng Đăng nhập và xác minh tài khoản.
     */
    Optional<NguoiDung> findByEmail(String email);

    /**
     * Kiểm tra xem Email đã tồn tại trong cơ sở dữ liệu hay chưa.
     * Sử dụng khi Đăng ký thành viên mới để tránh trùng lặp.
     */
    boolean existsByEmail(String email);

    /**
     * Lấy danh sách toàn bộ người dùng và Fetch sẵn danh sách Vai Trò (vaiTros),
     * giúp giải quyết dứt điểm lỗi LazyInitializationException khi Serialize JSON.
     */
    @Override
    @EntityGraph(attributePaths = {"vaiTros"})
    @NonNull
    List<NguoiDung> findAll();

    /**
     * Tìm kiếm người dùng bị khóa hoặc bị cấm bình luận.
     */
    List<NguoiDung> findByTrangThaiOrThoiGianCamBinhLuanIsNotNull(String trangThai);

    /**
     * Tìm kiếm tài khoản liên kết OAuth2 (Google, Facebook).
     */
    Optional<NguoiDung> findByProviderAndProviderId(String provider, String providerId);
}