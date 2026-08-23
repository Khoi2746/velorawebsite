package com.velora.website.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.velora.website.Entity.DanhGia;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.SanPham;
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
    private final SocService socService; 

    public DanhGiaController(DanhGiaRepository danhGiaRepository, 
                             NguoiDungRepository nguoiDungRepository, 
                             SanPhamRepository sanPhamRepository,
                             SocService socService) {
        this.danhGiaRepository = danhGiaRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.socService = socService;
    }

    // 1. API Lấy danh sách đánh giá của 1 sản phẩm
    @GetMapping("/san-pham/{maSanPham}")
    public ResponseEntity<?> getDanhGiaBySanPham(@PathVariable Integer maSanPham) {
        List<DanhGia> danhGias = danhGiaRepository.findBySanPham_MaSanPham(maSanPham);

        List<Map<String, Object>> result = danhGias.stream().map(dg -> {
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

        return ResponseEntity.ok(result);
    }

    // 2. API Thêm mới đánh giá 
    @PostMapping("/them")
    public ResponseEntity<?> addDanhGia(@RequestBody DanhGiaRequest payload, HttpServletRequest request) {
        try {
            NguoiDung user = nguoiDungRepository.findById(payload.getMaNguoiDung())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // ➔ Kiểm tra nếu tài khoản đã bị khóa vĩnh viễn
            if (user.getTrangThai() != null && "0".equals(user.getTrangThai())) {
                return ResponseEntity.status(403).body(Map.of("message", "ACCOUNT_LOCKED"));
            }

            // ➔ TÁCH BIỆT: Kiểm tra nếu tài khoản đang trong thời gian cấm tạm thời (3 phút)
            if (user.getThoiGianCamBinhLuan() != null && user.getThoiGianCamBinhLuan().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body(Map.of("message", "TEMPORARILY_BANNED"));
            }

            String binhLuan = payload.getBinhLuan() != null ? payload.getBinhLuan() : "";
            String binhLuanLower = binhLuan.toLowerCase();

            // 🔥 PHẦN A: KIỂM TRA MÃ ĐỘC (XSS / Script Injection)
            boolean isSecurityThreat = binhLuanLower.contains("<script>") || 
                                       binhLuanLower.contains("javascript:") || 
                                       binhLuanLower.contains("alert(") || 
                                       binhLuanLower.contains("onerror=");
            
            if (isSecurityThreat) {
                String clientIp = request.getRemoteAddr();
                
                // Ngầm ghi log và bắn báo động đỏ về SOC ngay lập tức
                socService.createAndBroadcastAlert(
                    clientIp, 
                    "XSS_ATTACK", 
                    "NGHIEM_TRONG", 
                    "Phát hiện cố tình chèn mã độc qua form đánh giá sản phẩm: " + binhLuan
                );
                
                return ResponseEntity.status(403).body(Map.of("message", "MALICIOUS_CONTENT"));
            }

            // 🔥 PHẦN B: KIỂM TRA TỪ NGỮ THÔ TỤC / SPAM THÔNG THƯỜNG
            List<String> badWords = Arrays.asList("dm", "vcl", "ngu", "chó", "lừa đảo");
            boolean containsBadWord = badWords.stream().anyMatch(binhLuanLower::contains);

            if (containsBadWord) {
                int viPham = (user.getSoLanViPham() == null) ? 0 : user.getSoLanViPham();
                viPham++;
                user.setSoLanViPham(viPham);

                if (viPham == 1) {
                    // Đặt thời gian cấm 3 phút cho lần vi phạm đầu tiên
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusMinutes(3));
                    nguoiDungRepository.save(user);
                    
                    // ➔ Trả về mã cảnh cáo nhẹ nhàng (Warning) để nhắc nhở chỉnh sửa
                    return ResponseEntity.status(403).body(Map.of("message", "INAPPROPRIATE_LANGUAGE_WARNING"));
                } else {
                    user.setTrangThai("0"); // Khóa tài khoản từ vi phạm thứ 2
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("message", "ACCOUNT_LOCKED"));
                }
            }

            // ➔ NẾU NỘI DUNG SẠCH SẼ -> LƯU BÌNH LUẬN BÌNH THƯỜNG
            SanPham sanPham = sanPhamRepository.findById(payload.getMaSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kiệt tác đồng hồ này"));

            DanhGia newDanhGia = new DanhGia();
            newDanhGia.setSoSao(payload.getSoSaoDanhGia());
            newDanhGia.setBinhLuan(binhLuan);
            newDanhGia.setNgayDanhGia(LocalDateTime.now());
            newDanhGia.setSanPham(sanPham);
            newDanhGia.setNguoiDung(user);

            danhGiaRepository.save(newDanhGia);

            return ResponseEntity.ok(Map.of("message", "Đánh giá thành công!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }
}