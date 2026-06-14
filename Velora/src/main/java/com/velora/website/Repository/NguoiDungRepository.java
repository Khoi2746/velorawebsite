package com.velora.website.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.velora.website.Entity.NguoiDung;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    /**
     * Hàm tìm kiếm người dùng dựa trên Email.
     * Spring Data JPA sẽ tự động dịch tên hàm này thành câu lệnh SQL:
     * SELECT * FROM NguoiDung WHERE Email = ?
     * * @param email Email do người dùng nhập vào từ form Vue.js
     * @return Optional<NguoiDung> (Giúp tránh lỗi NullPointerException nếu không tìm thấy)
     */
    Optional<NguoiDung> findByEmail(String email);

    /**
     * Hàm kiểm tra xem một Email đã tồn tại trong hệ thống hay chưa.
     * Cực kỳ cần thiết cho chức năng Đăng ký (Register) để chặn trùng tài khoản.
     * * @param email Email cần kiểm tra
     * @return true nếu đã có người dùng, false nếu email chưa ai đăng ký
     */
    boolean existsByEmail(String email);
}