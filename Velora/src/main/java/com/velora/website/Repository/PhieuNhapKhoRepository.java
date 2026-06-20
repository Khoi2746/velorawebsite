package com.velora.website.Repository;

import com.velora.website.Entity.PhieuNhapKho;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhieuNhapKhoRepository extends JpaRepository<PhieuNhapKho, Integer> {
    // JpaRepository tự lo các hàm cơ bản findAll, findById, save
}