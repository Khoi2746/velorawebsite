package com.velora.website.Repository;

import com.velora.website.Entity.BaiVietMarketing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BaiVietMarketingRepository extends JpaRepository<BaiVietMarketing, Integer> {
    // Admin/Sale lấy toàn bộ bài viết mới nhất lên đầu
    List<BaiVietMarketing> findAllByOrderByNgayTaoDesc();
    
    // Khách hàng chỉ lấy những bài đang bật HIEN_THI
    List<BaiVietMarketing> findByTrangThaiOrderByNgayTaoDesc(String trangThai);
}