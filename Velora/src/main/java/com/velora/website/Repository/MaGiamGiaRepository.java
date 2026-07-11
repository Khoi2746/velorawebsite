package com.velora.website.Repository;

import com.velora.website.Entity.MaGiamGia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {
    boolean existsByMaCode(String maCode);

    Optional<MaGiamGia> findByMaCode(String maCode);
}