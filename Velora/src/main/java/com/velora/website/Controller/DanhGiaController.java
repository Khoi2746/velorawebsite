package com.velora.website.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.velora.website.Entity.BaiVietMarketing;
import com.velora.website.Entity.DanhGia;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.SanPham;
import com.velora.website.Repository.BaiVietMarketingRepository;
import com.velora.website.Repository.DanhGiaRepository;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.SanPhamRepository;
import com.velora.website.Request.DanhGiaRequest;
import com.velora.website.Service.SocService; 
import jakarta.servlet.http.HttpServletRequest; 

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/danh-gia")
@CrossOrigin(origins = "*") 
public class DanhGiaController {

    private final DanhGiaRepository danhGiaRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SanPhamRepository sanPhamRepository;
    private final BaiVietMarketingRepository baiVietRepository; // Đã thêm repo bài viết
    private final SocService socService; 

    public DanhGiaController(DanhGiaRepository danhGiaRepository, 
                             NguoiDungRepository nguoiDungRepository, 
                             SanPhamRepository sanPhamRepository,
                             BaiVietMarketingRepository baiVietRepository,
                             SocService socService) {
        this.danhGiaRepository = danhGiaRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.baiVietRepository = baiVietRepository;
        this.socService = socService;
    }

    // ==============================================================
    // 1. LẤY BÌNH LUẬN CỦA SẢN PHẨM
    // ==============================================================
    @GetMapping("/san-pham/{maSanPham}")
    public ResponseEntity<?> getDanhGiaBySanPham(@PathVariable Integer maSanPham) {
        List<DanhGia> danhGias = danhGiaRepository.findBySanPham_MaSanPham(maSanPham);
        return ResponseEntity.ok(formatDanhGiaResponse(danhGias));
    }

    // ==============================================================
    // 2. LẤY BÌNH LUẬN CỦA BÀI VIẾT MARKETING
    // ==============================================================
    @GetMapping("/bai-viet/{maBaiViet}")
    public ResponseEntity<?> getDanhGiaByBaiViet(@PathVariable Integer maBaiViet) {
        List<DanhGia> danhGias = danhGiaRepository.findByBaiViet_MaBaiViet(maBaiViet);
        return ResponseEntity.ok(formatDanhGiaResponse(danhGias));
    }

    private List<Map<String, Object>> formatDanhGiaResponse(List<DanhGia> danhGias) {
        return danhGias.stream().map(dg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maDanhGia", dg.getMaDanhGia());
            map.put("soSao", dg.getSoSao());
            map.put("binhLuan", dg.getBinhLuan());
            map.put("ngayDanhGia", dg.getNgayDanhGia());

            if (dg.getNguoiDung() != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("hoTen", dg.getNguoiDung().getHoTen());
                map.put("nguoiDung", userMap);
            }
            return map;
        }).collect(Collectors.toList());
    }

    // ==============================================================
    // 3. THÊM BÌNH LUẬN (CHỐNG LỖI NULL ID VÀ TÍCH HỢP SOC)
    // ==============================================================
    @PostMapping("/them")
    public ResponseEntity<?> addDanhGia(@RequestBody DanhGiaRequest payload, HttpServletRequest request) {
        try {
            if (payload.getMaNguoiDung() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin người dùng!"));
            }

            NguoiDung user = nguoiDungRepository.findById(payload.getMaNguoiDung())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // 1. KIỂM TRA TÀI KHOẢN BỊ KHÓA
            if (user.getTrangThai() != null && "BI_KHOA".equals(user.getTrangThai().toUpperCase())) {
                return ResponseEntity.status(403).body(Map.of("code", "BANNED_3", "message", "Tài khoản của bạn đã bị khóa vĩnh viễn!"));
            }

            // 2. KIỂM TRA ĐANG TRONG THỜI GIAN CẤM
            if (user.getThoiGianCamBinhLuan() != null && user.getThoiGianCamBinhLuan().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body(Map.of("code", "TEMPORARILY_BANNED", "message", "Bạn đang trong thời gian bị cấm bình luận (3 phút)!"));
            }

            String binhLuan = payload.getBinhLuan() != null ? payload.getBinhLuan() : "";
            String binhLuanLower = binhLuan.toLowerCase();

            // 3. KIỂM TRA MÃ ĐỘC (XSS)
            boolean isSecurityThreat = binhLuanLower.contains("<script>") || binhLuanLower.contains("javascript:") || binhLuanLower.contains("alert(") || binhLuanLower.contains("onerror=");
            if (isSecurityThreat) {
                socService.createAndBroadcastAlert(request.getRemoteAddr(), "XSS_ATTACK", "NGHIEM_TRONG", "Phát hiện chèn mã độc: " + binhLuan);
                return ResponseEntity.status(403).body(Map.of("code", "MALICIOUS", "message", "Hệ thống an ninh đã chặn nội dung nguy hiểm!"));
            }

            // 4. LUỒNG XỬ LÝ VI PHẠM TỪ NGỮ ĐỘC HẠI
            List<String> badWords = Arrays.asList("dm", "vcl", "ngu", "chó", "lừa đảo");
            boolean containsBadWord = badWords.stream().anyMatch(binhLuanLower::contains);

            if (containsBadWord) {
                int viPham = (user.getSoLanViPham() == null) ? 0 : user.getSoLanViPham();
                viPham++;
                user.setSoLanViPham(viPham);

                if (viPham == 1) {
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("code", "WARNING_1", "message", "Cảnh cáo lần 1: Ngôn từ không phù hợp!"));
                } 
                else if (viPham == 2) {
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusMinutes(3));
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("code", "WARNING_2", "message", "Vi phạm lần 2: Cấm bình luận trong 3 phút!"));
                } 
                else {
                    user.setTrangThai("BI_KHOA");
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusYears(100)); 
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("code", "BANNED_3", "message", "Vi phạm lần 3: Tài khoản đã bị KHÓA VĨNH VIỄN!"));
                }
            }

            // 5. PHÂN LUỒNG LƯU DỮ LIỆU ĐỂ CHỐNG LỖI "THE GIVEN ID MUST NOT BE NULL"
            DanhGia newDanhGia = new DanhGia();
            newDanhGia.setSoSao(payload.getSoSaoDanhGia());
            newDanhGia.setBinhLuan(binhLuan);
            newDanhGia.setNgayDanhGia(LocalDateTime.now());
            newDanhGia.setNguoiDung(user);

            // Kiểm tra xem Frontend gửi lên loại đánh giá là Sản phẩm hay Bài viết
            if ("BAI_VIET".equalsIgnoreCase(payload.getLoaiDanhGia())) {
                if (payload.getMaBaiViet() == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: Không tìm thấy ID Bài Viết!"));
                }
                BaiVietMarketing baiViet = baiVietRepository.findById(payload.getMaBaiViet())
                        .orElseThrow(() -> new RuntimeException("Bài viết này không tồn tại!"));
                newDanhGia.setBaiViet(baiViet);
            } 
            else {
                if (payload.getMaSanPham() == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: Không tìm thấy ID Sản Phẩm!"));
                }
                SanPham sanPham = sanPhamRepository.findById(payload.getMaSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm này không tồn tại!"));
                newDanhGia.setSanPham(sanPham);
            }

            danhGiaRepository.save(newDanhGia);
            return ResponseEntity.ok(Map.of("message", "Gửi đánh giá thành công!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }
}