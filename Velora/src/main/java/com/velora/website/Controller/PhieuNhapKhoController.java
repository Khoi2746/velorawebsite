package com.velora.website.Controller;

import com.velora.website.Entity.ChiTietPhieuNhap;
import com.velora.website.Entity.PhieuNhapKho;
import com.velora.website.Entity.SanPham;
import com.velora.website.Repository.ChiTietPhieuNhapRepository;
import com.velora.website.Repository.PhieuNhapKhoRepository;
import com.velora.website.Repository.SanPhamRepository;
import com.velora.website.Request.PhieuNhapRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/phieu-nhap")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PhieuNhapKhoController {

    private final PhieuNhapKhoRepository phieuNhapKhoRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
    private final SanPhamRepository sanPhamRepository;

    // 1. LẤY TOÀN BỘ DANH SÁCH PHIẾU NHẬP
    @GetMapping
    public ResponseEntity<List<PhieuNhapKho>> getAllPhieuNhap() {
        return ResponseEntity.ok(phieuNhapKhoRepository.findAll());
    }

    // 2. LẤY CHI TIẾT SẢN PHẨM CỦA MỘT PHIẾU NHẬP
    @GetMapping("/{id}/chi-tiet")
    public ResponseEntity<List<ChiTietPhieuNhap>> getChiTiet(@PathVariable Integer id) {
        return ResponseEntity.ok(chiTietPhieuNhapRepository.findByMaPhieuNhap(id));
    }

    // 3. STAFF TẠO PHIẾU NHẬP MỚI (MẶC ĐỊNH CHỜ DUYỆT)
    @PostMapping
    @Transactional
    public ResponseEntity<?> createPhieuNhap(@RequestBody PhieuNhapRequest request) {
        try {
            // Tạo thông tin phiếu nhập tổng quan
            PhieuNhapKho phieu = new PhieuNhapKho();
            // Sinh mã code ngẫu nhiên dạng: PNK-XXXXX
            String randomCode = "PNK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            phieu.setMaPhieuNhapCode(randomCode);
            phieu.setMaNguoiYeuCau(request.getMaNguoiYeuCau());
            phieu.setGhiChu(request.getGhiChu());
            phieu.setTrangThai("CHO_DUYET");
            
            // BỔ SUNG DÒNG NÀY ĐỂ ẤN ĐỊNH NGÀY TẠO NGAY TRÊN JAVA
            phieu.setNgayYeuCau(LocalDateTime.now());
            
            PhieuNhapKho savedPhieu = phieuNhapKhoRepository.save(phieu);

            // Lưu danh sách chi tiết các mặt hàng cần nhập
            for (PhieuNhapRequest.ChiTietDto dto : request.getChiTietList()) {
                ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
                chiTiet.setMaPhieuNhap(savedPhieu.getMaPhieuNhap());
                chiTiet.setMaSanPham(dto.getMaSanPham());
                chiTiet.setSoLuongNhap(dto.getSoLuongNhap());
                chiTiet.setGiaNhap(dto.getGiaNhap());
                chiTietPhieuNhapRepository.save(chiTiet);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Đã gửi yêu cầu lập phiếu nhập kho!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // 4. ADMIN DUYỆT HOẶC TỪ CHỐI PHIẾU NHẬP (CẬP NHẬT KHO THỰC TẾ)
    @PatchMapping("/{id}/trang-thai")
    @Transactional
    public ResponseEntity<?> capNhatTrangThai(@PathVariable Integer id, @RequestParam String trangThai) {
        PhieuNhapKho phieu = phieuNhapKhoRepository.findById(id).orElse(null);
        if (phieu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy phiếu nhập kho này!");
        }

        if (!phieu.getTrangThai().equals("CHO_DUYET")) {
            return ResponseEntity.badRequest().body("Phiếu này đã được xử lý từ trước!");
        }

        // Cập nhật trạng thái phê duyệt và mốc thời gian
        phieu.setTrangThai(trangThai);
        phieu.setNgayDuyet(LocalDateTime.now());
        phieu.setMaNguoiDuyet(1); // Mặc định ID Admin phê duyệt là 1
        phieuNhapKhoRepository.save(phieu);

        // NẾU ADMIN DUYỆT -> TỰ ĐỘNG CHẠY VÒNG LẶP CỘNG DỒN VÀO BẢNG SẢN PHẨM
        if (trangThai.equals("DA_DUYET")) {
            List<ChiTietPhieuNhap> chiTietList = chiTietPhieuNhapRepository.findByMaPhieuNhap(id);
            for (ChiTietPhieuNhap chiTiet : chiTietList) {
                SanPham sanPham = sanPhamRepository.findById(chiTiet.getMaSanPham()).orElse(null);
                if (sanPham != null) {
                    // Cộng tồn kho cũ với số lượng nhập thêm
                    int tonKhoMoi = (sanPham.getSoLuongTonKho() != null ? sanPham.getSoLuongTonKho() : 0) + chiTiet.getSoLuongNhap();
                    sanPham.setSoLuongTonKho(tonKhoMoi);
                    sanPhamRepository.save(sanPham);
                }
            }
        }

        return ResponseEntity.ok("Xử lý phiếu nhập kho thành công!");
    }
}