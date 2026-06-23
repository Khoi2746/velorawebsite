package com.velora.website.Repository;

import com.velora.website.Entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {
    boolean existsByMaCode(String maCode);
}