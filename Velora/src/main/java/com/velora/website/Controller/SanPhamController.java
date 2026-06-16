package com.velora.website.Controller;

import com.velora.website.Entity.DanhMuc;
import com.velora.website.Entity.SanPham;
import com.velora.website.Entity.ThuongHieu;
import com.velora.website.Repository.SanPhamRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin("*") // Mở cửa cho Vue ở cổng khác gọi vào
@RequiredArgsConstructor // GẮN LOMBOK VÀO ĐỂ TỰ ĐỘNG TIÊM DEPENDENCY
public class SanPhamController {
    
    // THÊM CHỮ final ĐỂ LOMBOK TỰ ĐỘNG INJECT
    private final SanPhamRepository sanPhamRepository;

    // Lấy tất cả sản phẩm
    @GetMapping
    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    // Lấy sản phẩm theo ID (dùng cho trang chi tiết sau này)
    @GetMapping("/{id}")
    public SanPham getById(@PathVariable Integer id) {
        return sanPhamRepository.findById(id).orElse(null);
    }

    // =========================================================================
    // GIỮ NGUYÊN VẸN LOGIC QUẢN LÝ KHO CŨ - KHÔNG THAY ĐỔI
    // =========================================================================
    @PutMapping("/{id}/cap-nhat-kho")
    public ResponseEntity<?> capNhatKhoMoiNhat(
            @PathVariable Integer id, 
            @RequestBody Map<String, Object> request) {
        
        try {
            // Lấy dữ liệu gửi lên và kiểm tra rỗng
            Object slObj = request.get("soLuongMoi");
            if (slObj == null || slObj.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Số lượng không được để trống!");
            }

            // Ép kiểu an toàn sang số nguyên
            Integer soLuongMoi = Integer.parseInt(slObj.toString());
            
            if (soLuongMoi < 0) {
                return ResponseEntity.badRequest().body("Số lượng không được nhỏ hơn 0!");
            }

            // Tìm sản phẩm trong DB
            SanPham sanPham = sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

            // Lưu đè số lượng mới
            sanPham.setSoLuongTonKho(soLuongMoi);
            
            // Tự động điều chỉnh trạng thái
            if (soLuongMoi > 0) {
                sanPham.setTrangThai("CON_HANG");
            } else {
                sanPham.setTrangThai("HET_HANG");
            }

            // Lưu vào CSDL
            sanPhamRepository.save(sanPham);
            
            return ResponseEntity.ok(sanPham);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Vui lòng nhập định dạng số hợp lệ!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }

    // =========================================================================
    // HÀM CRUD ĐÃ ĐƯỢC LOẠI BỎ TỒN KHO ĐỂ KHỚP VỚI GIAO DIỆN VUE
    // =========================================================================

    // 1. Thêm sản phẩm mới 
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody SanPham sanPham) {
        try {
            // Kiểm tra validate tên sản phẩm
            if (sanPham.getTenSanPham() == null || sanPham.getTenSanPham().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống!");
            }
            
            // TỰ ĐỘNG SINH SLUG
            if (sanPham.getDuongDanSlug() == null || sanPham.getDuongDanSlug().trim().isEmpty()) {
                String slug = sanPham.getTenSanPham().toLowerCase()
                        .replaceAll("[^a-z0-9\\s]", "") // Xóa ký tự đặc biệt
                        .replaceAll("\\s+", "-");        // Thay khoảng trắng bằng dấu -
                sanPham.setDuongDanSlug(slug);
            }

            // PHÒNG NGỪA CÁC TRƯỜNG TEXT KHÁC BỊ NULL
            if (sanPham.getLoaiMay() == null) sanPham.setLoaiMay("Automatic");
            if (sanPham.getChatLieuDay() == null) sanPham.setChatLieuDay("Thép không gỉ");
            if (sanPham.getChatLieuKinh() == null) sanPham.setChatLieuKinh("Sapphire");
            if (sanPham.getDuongKinhMat() == null) sanPham.setDuongKinhMat("40mm");
            if (sanPham.getDoChongNuoc() == null) sanPham.setDoChongNuoc("5 ATM");

            // ĐẢM BẢO TỒN KHO KHÔNG BỊ NULL KHI VUE KHÔNG TRUYỀN LÊN
            if (sanPham.getSoLuongTonKho() == null) {
                sanPham.setSoLuongTonKho(0); // Mặc định gán bằng 0
            }

            // XỬ LÝ KHÓA NGOẠI
            if (sanPham.getThuongHieu() == null) {
                ThuongHieu thMaoDanh = new ThuongHieu();
                thMaoDanh.setMaThuongHieu(1); 
                sanPham.setThuongHieu(thMaoDanh);
            }
            
            if (sanPham.getDanhMuc() == null) {
                DanhMuc dmMaoDanh = new DanhMuc();
                dmMaoDanh.setMaDanhMuc(1); 
                sanPham.setDanhMuc(dmMaoDanh);
            }

            // Lưu sản phẩm vào Database
            SanPham savedProduct = sanPhamRepository.save(sanPham);
            return ResponseEntity.ok(savedProduct);
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Lỗi Java tại Controller: " + e.getMessage());
        }
    }

    // 2. Chỉnh sửa toàn bộ thông tin sản phẩm (Đã loại bỏ trường Tồn kho để tránh bị ghi đè thành null)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody SanPham productDetails) {
        return sanPhamRepository.findById(id).map(sanPham -> {
            sanPham.setTenSanPham(productDetails.getTenSanPham());
            sanPham.setGiaBan(productDetails.getGiaBan());
            sanPham.setAnhDaiDien(productDetails.getAnhDaiDien());
            sanPham.setTrangThai(productDetails.getTrangThai());
            
            // Tự động cập nhật lại Slug theo tên mới nếu có sửa tên
            String slug = productDetails.getTenSanPham().toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
            sanPham.setDuongDanSlug(slug);
            
            // KHÔNG setSoLuongTonKho ở đây để bảo toàn số lượng cũ trong Database
            
            SanPham updatedProduct = sanPhamRepository.save(sanPham);
            return ResponseEntity.ok(updatedProduct);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 3. Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        return sanPhamRepository.findById(id).map(sanPham -> {
            sanPhamRepository.delete(sanPham);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}