package com.velora.website.Repository;

import com.velora.website.Entity.BaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaoHanhRepository
        extends JpaRepository<BaoHanh, Integer> {

    List<BaoHanh> findByTrangThai(String trangThai);

    List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung);

}