package com.velora.website.Repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.velora.website.Entity.ThuongHieu;

import java.util.List;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    
    // Spring Data JPA tự động tạo câu query SQL tương đương: 
    // SELECT * FROM ThuongHieu WHERE TrangThai = 1
    List<ThuongHieu> findByTrangThaiTrue();
    
    // Tìm kiếm thương hiệu theo tên (có thể dùng cho thanh tìm kiếm sau này)
    ThuongHieu findByTenThuongHieu(String tenThuongHieu);
}