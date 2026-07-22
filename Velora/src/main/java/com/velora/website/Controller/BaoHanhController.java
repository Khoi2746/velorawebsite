package com.velora.website.Controller;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Service.BaoHanhService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bao-hanh")
@CrossOrigin(originPatterns = "*")
public class BaoHanhController {

        private final BaoHanhService baoHanhService;

        BaoHanhController(BaoHanhService baoHanhService) {
                this.baoHanhService = baoHanhService;
        }

        // ==========================
        // Người dùng gửi yêu cầu bảo hành
        // ==========================
        @PostMapping("/send")
        public ResponseEntity<?> sendWarrantyRequest(
                        @RequestBody Map<String, Object> payload) {

                try {

                        // Khi làm Login thì thay bằng session
                        Integer userId =

                                        Integer.parseInt(

                                                        payload.get("maNguoiDung").toString()

                                        );

                        String maDonHangCode = payload.get("maDonHangCode") == null
                                        ? ""
                                        : payload.get("maDonHangCode").toString();

                        String loaiSanPham = payload.get("loaiSanPham") == null
                                        ? ""
                                        : payload.get("loaiSanPham").toString();

                        String moTaLoi = payload.get("moTaLoi") == null
                                        ? ""
                                        : payload.get("moTaLoi").toString();

                        String hoTen = payload.get("hoTen") == null
                                        ? ""
                                        : payload.get("hoTen").toString();

                        String sdt = payload.get("sdt") == null
                                        ? ""
                                        : payload.get("sdt").toString();

                        if (maDonHangCode.isBlank()
                                        || loaiSanPham.isBlank()
                                        || moTaLoi.isBlank()) {

                                return ResponseEntity.badRequest().body(
                                                Map.of(
                                                                "message",
                                                                "Vui lòng nhập đầy đủ thông tin."));
                        }

                        BaoHanh baoHanh = new BaoHanh();

                        baoHanh.setMaNguoiDung(userId);
                        baoHanh.setHoTen(hoTen);
                        baoHanh.setSoDienThoai(sdt);
                        baoHanh.setMaDonHangCode(maDonHangCode);
                        baoHanh.setLoaiSanPham(loaiSanPham);
                        baoHanh.setMoTaLoi(moTaLoi);

                        // mặc định
                        baoHanh.setTrangThai("CHO_XU_LY");

                        BaoHanh saved = baoHanhService.saveRequest(baoHanh);

                        return ResponseEntity.status(HttpStatus.CREATED).body(
                                        Map.of(
                                                        "message",
                                                        "Gửi yêu cầu thành công.",
                                                        "data",
                                                        saved));

                } catch (Exception e) {

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        e.getMessage()));
                }
        }

        // ==========================
        // ADMIN xem toàn bộ yêu cầu
        // ==========================
        @GetMapping
        public List<BaoHanh> getAllRequests() {

                return baoHanhService.getAllRequests();

        }

        // ==========================
        // ADMIN xem yêu cầu chờ xử lý
        // ==========================
        @GetMapping("/pending")
        public List<BaoHanh> getPendingRequests() {

                return baoHanhService.getPendingRequests();

        }

        // ==========================
        // Người dùng xem lịch sử bảo hành
        // ==========================
        @GetMapping("/my-history/{userId}")
        public List<BaoHanh> getHistory(

                        @PathVariable Integer userId) {

                return baoHanhService.findByMaNguoiDung(userId);

        }

        public List<BaoHanh> getMyWarrantyRequests(
                        HttpSession session) {

                Integer userId = (Integer) session.getAttribute("USER_ID");

                // Test nếu chưa login
                if (userId == null) {

                        userId = 1;

                }

                return baoHanhService.findByMaNguoiDung(userId);

        }

        // ==========================
        // ADMIN cập nhật trạng thái
        // ==========================
        @PutMapping("/{id}/status")
        public ResponseEntity<?> updateStatus(
                        @PathVariable Integer id,
                        @RequestBody Map<String, String> body) {

                BaoHanh bh = baoHanhService.updateStatus(
                                id,
                                body.get("trangThai"));

                return ResponseEntity.ok(
                                Map.of(
                                                "message",
                                                "Cập nhật trạng thái thành công.",
                                                "data",
                                                bh));
        }

        // ==========================
        // USER HỦY YÊU CẦU
        // ==========================
        @PutMapping("/{id}/cancel")
        public ResponseEntity<?> cancelRequest(
                        @PathVariable Integer id) {

                try {

                        BaoHanh bh = baoHanhService.cancelRequest(id);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Đã hủy yêu cầu bảo hành.",
                                                        "data",
                                                        bh));

                } catch (Exception e) {

                        return ResponseEntity.badRequest()
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        e.getMessage()));

                }

        }
}