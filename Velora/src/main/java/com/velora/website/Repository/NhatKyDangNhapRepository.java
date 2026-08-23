package com.velora.website.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.velora.website.Entity.NhatKyDangNhap;

import java.util.List;

public interface NhatKyDangNhapRepository extends JpaRepository<NhatKyDangNhap, Integer> {
    List<NhatKyDangNhap> findTop100ByOrderByThoiGianDangNhapDesc();
}