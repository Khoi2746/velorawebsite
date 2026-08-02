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

    // Khai báo biến final như bình thường
    private final DanhGiaRepository danhGiaRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SanPhamRepository sanPhamRepository;

    // 🔥 TUYỆT CHIÊU CUỐI: Viết Constructor bằng tay
    // Khỏi cần Lombok hay @Autowired, Spring tự động hiểu và tiêm data vào đây.
    public DanhGiaController(DanhGiaRepository danhGiaRepository, 
                             NguoiDungRepository nguoiDungRepository, 
                             SanPhamRepository sanPhamRepository) {
        this.danhGiaRepository = danhGiaRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.sanPhamRepository = sanPhamRepository;
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

    // 2. API Thêm mới đánh giá (Có tích hợp kiểm duyệt từ ngữ)
    @PostMapping("/them")
    public ResponseEntity<?> addDanhGia(@RequestBody DanhGiaRequest payload) {
        try {
            // Lúc này nguoiDungRepository chắc chắn 100% không thể null
            NguoiDung user = nguoiDungRepository.findById(payload.getMaNguoiDung())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            if (user.getTrangThai() != null && "0".equals(user.getTrangThai())) {
                return ResponseEntity.status(403).body(Map.of("message", "ACCOUNT_LOCKED"));
            }

            if (user.getThoiGianCamBinhLuan() != null && user.getThoiGianCamBinhLuan().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(403).body(Map.of("message", "BANNED_3_MINS"));
            }

            List<String> badWords = Arrays.asList("dm", "vcl", "ngu", "chó", "lừa đảo");
            String binhLuan = payload.getBinhLuan().toLowerCase();
            boolean containsBadWord = badWords.stream().anyMatch(binhLuan::contains);

            if (containsBadWord) {
                int viPham = (user.getSoLanViPham() == null) ? 0 : user.getSoLanViPham();
                viPham++;
                user.setSoLanViPham(viPham);

                if (viPham == 1) {
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusMinutes(3));
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("message", "BANNED_3_MINS"));
                } else {
                    user.setTrangThai("0"); 
                    nguoiDungRepository.save(user);
                    return ResponseEntity.status(403).body(Map.of("message", "ACCOUNT_LOCKED"));
                }
            }

            SanPham sanPham = sanPhamRepository.findById(payload.getMaSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kiệt tác đồng hồ này"));

            DanhGia newDanhGia = new DanhGia();
            newDanhGia.setSoSao(payload.getSoSaoDanhGia());
            newDanhGia.setBinhLuan(payload.getBinhLuan());
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