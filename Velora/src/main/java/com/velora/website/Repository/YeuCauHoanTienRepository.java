package com.velora.website.Repository;

import com.velora.website.Entity.YeuCauHoanTien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YeuCauHoanTienRepository extends JpaRepository<YeuCauHoanTien, Integer> {
    List<YeuCauHoanTien> findByEmailIgnoreCase(String email);
}