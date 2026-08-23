package com.velora.website.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.velora.website.Entity.CanhBaoAnNinh;

import java.util.List;

public interface CanhBaoAnNinhRepository extends JpaRepository<CanhBaoAnNinh, Integer> {
    // Lấy 50 cảnh báo mới nhất
    List<CanhBaoAnNinh> findTop50ByOrderByNgayTaoDesc();
}