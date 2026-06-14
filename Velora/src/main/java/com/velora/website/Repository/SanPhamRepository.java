package com.velora.website.Repository;

import com.velora.website.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    // Nếu sau này muốn lọc theo tên hay slug thì thêm hàm tại đây
}