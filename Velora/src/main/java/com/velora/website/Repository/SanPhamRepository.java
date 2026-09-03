package com.velora.website.Repository;

import com.velora.website.Entity.SanPham; // đổi đúng package entity nếu khác
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query(value = """
        SELECT
            sp.MaSanPham,
            sp.TenSanPham,
            sp.GiaBan,
            sp.TrangThai,
            sp.AnhDaiDien,
            th.TenThuongHieu,
            sp.DuongDanSlug,
            sp.SoLuongTonKho
        FROM SanPham sp
        INNER JOIN ThuongHieu th ON th.MaThuongHieu = sp.MaThuongHieu
        WHERE ISNULL(sp.TrangThai, N'CON_HANG') <> N'NGUNG_BAN'
          AND ISNULL(th.TrangThai, 1) = 1
        ORDER BY th.TenThuongHieu, sp.TenSanPham
        """, nativeQuery = true)
    List<Object[]> layDuLieuChoChatbot();
}