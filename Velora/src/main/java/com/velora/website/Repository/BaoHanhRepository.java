package com.velora.website.Repository;

import com.velora.website.Entity.BaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaoHanhRepository extends JpaRepository<BaoHanh, Integer> {

    List<BaoHanh> findByTrangThai(String trangThai);

    List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung);

    List<BaoHanh> findByMaNguoiDungOrderByNgayGuiDesc(Integer maNguoiDung);
}
