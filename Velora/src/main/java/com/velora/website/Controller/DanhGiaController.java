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

    @PostMapping("/them")
    public ResponseEntity<?> addDanhGia(@RequestBody DanhGiaRequest payload, HttpServletRequest request) {
        try {
            NguoiDung user = nguoiDungRepository.findById(payload.getMaNguoiDung())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // 1. KIỂM TRA TÀI KHOẢN BỊ KHÓA VĨNH VIỄN (LẦN 3)
            if (user.getTrangThai() != null && "BI_KHOA".equals(user.getTrangThai().toUpperCase())) {
                return ResponseEntity.status(403).body(Map.of(
                    "code", "BANNED_3", 
                    "message", "Tài khoản của bạn đã bị khóa vĩnh viễn do vi phạm tiêu chuẩn cộng đồng nhiều lần!"
                ));
            }

            // 2. KIỂM TRA ĐANG TRONG THỜI GIAN CẤM TẠM THỜI (LẦN 2)
            if (user.getThoiGianCamBinhLuan() != null && user.getThoiGianCamBinhLuan().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body(Map.of(
                    "code", "TEMPORARILY_BANNED", 
                    "message", "Bạn đang trong thời gian bị cấm bình luận (3 phút) do vi phạm trước đó!"
                ));
            }

            String binhLuan = payload.getBinhLuan() != null ? payload.getBinhLuan() : "";
            String binhLuanLower = binhLuan.toLowerCase();

            // KIỂM TRA MÃ ĐỘC (XSS)
            boolean isSecurityThreat = binhLuanLower.contains("<script>") || 
                                       binhLuanLower.contains("javascript:") || 
                                       binhLuanLower.contains("alert(") || 
                                       binhLuanLower.contains("onerror=");
            
            if (isSecurityThreat) {
                String clientIp = request.getRemoteAddr();
                socService.createAndBroadcastAlert(
                    clientIp, "XSS_ATTACK", "NGHIEM_TRONG", 
                    "Phát hiện chèn mã độc: " + binhLuan
                );
                return ResponseEntity.status(403).body(Map.of("code", "MALICIOUS", "message", "Phát hiện nội dung nguy hiểm!"));
            }

            // 🔥 LUỒNG XỬ LÝ VI PHẠM 3 BƯỚC (BAD WORDS)
            List<String> badWords = Arrays.asList("dm", "vcl", "ngu", "chó", "lừa đảo");
            boolean containsBadWord = badWords.stream().anyMatch(binhLuanLower::contains);

            if (containsBadWord) {
                int viPham = (user.getSoLanViPham() == null) ? 0 : user.getSoLanViPham();
                viPham++;
                user.setSoLanViPham(viPham);

                if (viPham == 1) {
                    // LẦN 1: Chỉ cảnh báo vàng, không cấm thời gian
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of(
                        "code", "WARNING_1", 
                        "message", "Cảnh cáo lần 1: Ngôn từ không phù hợp. Nếu tái phạm sẽ bị cấm bình luận 3 phút!"
                    ));
                } 
                else if (viPham == 2) {
                    // LẦN 2: Cấm bình luận 3 phút
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusMinutes(3));
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of(
                        "code", "WARNING_2", 
                        "message", "Vi phạm lần 2: Tài khoản bị cấm bình luận trong 3 phút tới!"
                    ));
                } 
                else {
                    // LẦN 3: Khóa tài khoản vĩnh viễn (Chuyển trạng thái)
                    user.setTrangThai("BI_KHOA");
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusYears(100)); // Cấm luôn 100 năm cho chắc
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of(
                        "code", "BANNED_3", 
                        "message", "Vi phạm lần 3: Tài khoản của bạn đã bị KHÓA VĨNH VIỄN!"
                    ));
                }
            }

            // NẾU NỘI DUNG SẠCH SẼ -> LƯU VÀO DB
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