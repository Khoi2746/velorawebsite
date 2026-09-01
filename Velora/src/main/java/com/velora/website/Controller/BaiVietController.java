package com.velora.website.Controller;

import com.velora.website.Entity.BaiVietMarketing;
import com.velora.website.Repository.BaiVietMarketingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bai-viet")
@CrossOrigin(origins = "*")
public class BaiVietController {

    @Autowired
    private BaiVietMarketingRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Dùng để can thiệp thẳng vào bảng mã giảm giá

    // Lấy tất cả bài viết (Cho Admin/Sale)
    @GetMapping
    public List<BaiVietMarketing> getAll() {
        return repository.findAllByOrderByNgayTaoDesc();
    }

    // Lấy bài viết hiển thị (Cho Khách)
    @GetMapping("/hien-thi")
    public List<BaiVietMarketing> getVisible() {
        return repository.findByTrangThaiOrderByNgayTaoDesc("HIEN_THI");
    }

    // 🔥 KHI ĐĂNG BÀI: TỰ ĐỘNG TẠO MÃ GIẢM GIÁ
    @PostMapping
    public BaiVietMarketing create(@RequestBody BaiVietMarketing baiViet) {
        baiViet.setNgayTao(new Date());
        if (baiViet.getSoLuotDaDung() == null) {
            baiViet.setSoLuotDaDung(0);
        }
        BaiVietMarketing saved = repository.save(baiViet);

        try {
            String sql = "INSERT INTO ma_giam_gia (ma_code, phan_tram_giam, gioi_han_su_dung, so_luot_da_dung, ngay_het_han) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                baiViet.getMaGiamGia(), 
                baiViet.getPhanTramGiam(), 
                baiViet.getSoLuotGioiHan(), 
                0, 
                baiViet.getHanSuDung());
            System.out.println("✅ Đã tự động tạo mã giảm giá: " + baiViet.getMaGiamGia());
        } catch (Exception e) {
            System.err.println("❌ Lỗi đồng bộ mã giảm giá: " + e.getMessage());
        }

        return saved;
    }

    // 🔥 KHI SỬA BÀI: TỰ ĐỘNG CẬP NHẬT MÃ GIẢM GIÁ
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody BaiVietMarketing baiViet) {
        Optional<BaiVietMarketing> opt = repository.findById(id);
        if(opt.isPresent()) {
            BaiVietMarketing existing = opt.get();
            String oldCode = existing.getMaGiamGia(); // Nhớ tên mã cũ để lấy gốc update

            existing.setTieuDe(baiViet.getTieuDe());
            existing.setNoiDung(baiViet.getNoiDung());
            existing.setAnhBia(baiViet.getAnhBia());
            existing.setMaGiamGia(baiViet.getMaGiamGia());
            existing.setPhanTramGiam(baiViet.getPhanTramGiam());
            existing.setSoLuotGioiHan(baiViet.getSoLuotGioiHan());
            existing.setHanSuDung(baiViet.getHanSuDung());
            existing.setTrangThai(baiViet.getTrangThai());
            
            BaiVietMarketing updated = repository.save(existing);

            try {
                // Kiểm tra xem mã cũ có tồn tại bên bảng ma_giam_gia không
                String sqlCheck = "SELECT COUNT(*) FROM ma_giam_gia WHERE ma_code = ?";
                Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, oldCode);
                
                if (count != null && count > 0) {
                    // Cập nhật đè lên mã cũ
                    String sqlUpdate = "UPDATE ma_giam_gia SET ma_code = ?, phan_tram_giam = ?, gioi_han_su_dung = ?, ngay_het_han = ? WHERE ma_code = ?";
                    jdbcTemplate.update(sqlUpdate, 
                        baiViet.getMaGiamGia(), 
                        baiViet.getPhanTramGiam(), 
                        baiViet.getSoLuotGioiHan(), 
                        baiViet.getHanSuDung(),
                        oldCode);
                } else {
                    // Nếu chưa có thì Insert bù vào
                    String sqlInsert = "INSERT INTO ma_giam_gia (ma_code, phan_tram_giam, gioi_han_su_dung, so_luot_da_dung, ngay_het_han) VALUES (?, ?, ?, ?, ?)";
                    jdbcTemplate.update(sqlInsert, baiViet.getMaGiamGia(), baiViet.getPhanTramGiam(), baiViet.getSoLuotGioiHan(), 0, baiViet.getHanSuDung());
                }
                System.out.println("Đã tự động cập nhật mã giảm giá: " + baiViet.getMaGiamGia());
            } catch (Exception e) {
                System.err.println("Lỗi đồng bộ cập nhật mã: " + e.getMessage());
            }

            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // 🔥 KHI XÓA BÀI: TỰ ĐỘNG XÓA MÃ GIẢM GIÁ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Optional<BaiVietMarketing> opt = repository.findById(id);
        if(opt.isPresent()) {
            String maGiamGia = opt.get().getMaGiamGia();
            
            try {
                String sql = "DELETE FROM ma_giam_gia WHERE ma_code = ?";
                jdbcTemplate.update(sql, maGiamGia);
                System.out.println("✅ Đã tự động dọn rác mã giảm giá: " + maGiamGia);
            } catch (Exception e) {
                System.err.println("❌ Lỗi xóa mã giảm giá: " + e.getMessage());
            }

            repository.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }
}