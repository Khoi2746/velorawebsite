package com.velora.website.Controller;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Service.BaoHanhService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
                        @RequestBody Map<String, Object> body) {

                String trangThai = (String) body.get("trangThai");
                LocalDateTime thoiGianHen = null;

                if (body.get("thoiGianHen") != null && !body.get("thoiGianHen").toString().isBlank()) {
                        thoiGianHen = LocalDateTime.parse(body.get("thoiGianHen").toString());
                }

                BaoHanh bh = baoHanhService.updateStatus(id, trangThai, thoiGianHen);

                return ResponseEntity.ok(
                                Map.of(
                                                "message", "Cập nhật trạng thái và gửi email thành công.",
                                                "data", bh));
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
        // ==========================
        // USER XÁC NHẬN LỊCH HẸN DO ADMIN ĐỀ XUẤT
        // ==========================
        @PutMapping("/{id}/confirm-schedule")
        public ResponseEntity<?> confirmSchedule(@PathVariable Integer id) {
            try {
                // Bạn có thể gọi service tương ứng để đổi trạng thái thành DA_TIEP_NHAN hoặc ĐÃ XÁC NHẬN
                BaoHanh bh = baoHanhService.updateStatus(id, "DA_TIEP_NHAN", null);
                return ResponseEntity.ok(Map.of(
                    "message", "Xác nhận lịch hẹn thành công!",
                    "data", bh
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }

        // ==========================
        // USER YÊU CẦU ĐỔI LỊCH HẸN KHÁC
        // ==========================
        @PutMapping("/{id}/reschedule-request")
        public ResponseEntity<?> rescheduleRequest(@PathVariable Integer id) {
            try {
                // Chuyển trạng thái sang chờ xếp lịch lại hoặc ghi nhận yêu cầu đổi lịch
                BaoHanh bh = baoHanhService.updateStatus(id, "CHO_XU_LY", null);
                return ResponseEntity.ok(Map.of(
                    "message", "Đã gửi yêu cầu đổi lịch đến trung tâm.",
                    "data", bh
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }
// ==========================
// USER XÁC NHẬN LỊCH HẸN DO ADMIN ĐỀ XUẤT
// ==========================
@PutMapping("/{id}/confirm-schedule")
public ResponseEntity<?> confirmSchedule1(@PathVariable Integer id) {
    try {
        // Truyền tham số theo đúng thứ tự mà hàm updateStatus bên Service yêu cầu 
        // (Ví dụ: truyền id, trạng thái mới và null hoặc giá trị thời gian tương ứng)
        BaoHanh bh = baoHanhService.updateStatus(id, "DA_TIEP_NHAN", null);
        
        return ResponseEntity.ok(Map.of("message", "Xác nhận lịch hẹn thành công!", "data", bh));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

// ==========================
// USER YÊU CẦU ĐỔI LỊCH HẸN KHÁC (Tự nhập giờ mong muốn)
// ==========================
@PutMapping("/{id}/reschedule-request")
public ResponseEntity<?> rescheduleRequest(@PathVariable Integer id, @RequestBody Map<String, String> body) {
    try {
        String thoiGianMoi = body.get("thoiGianMongMuon");
        BaoHanh bh = baoHanhService.requestReschedule(id, thoiGianMoi);
        return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu đổi lịch thành công đến hệ thống.", "data", bh));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

}
