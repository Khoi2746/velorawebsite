package com.velora.website.Repository;

import com.velora.website.Entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
    
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    // Kế thừa JpaRepository tự động cung cấp toàn bộ hàm: findAll(), findById(), save(),...
}