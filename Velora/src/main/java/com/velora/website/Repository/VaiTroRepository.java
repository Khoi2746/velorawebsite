package com.velora.website.Repository;

import com.velora.website.Entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {
    // Thêm dòng này để tìm Role theo tên
    Optional<VaiTro> findByTenVaiTro(String tenVaiTro); 
}