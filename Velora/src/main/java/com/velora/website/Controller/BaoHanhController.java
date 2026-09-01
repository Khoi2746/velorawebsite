package com.velora.website.Controller;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Repository.DonHangRepository;
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
    private final DonHangRepository donHangRepository;

    // Cập nhật constructor để inject cả BaoHanhService và DonHangRepository
    public BaoHanhController(BaoHanhService baoHanhService, DonHangRepository donHangRepository) {
        this.baoHanhService = baoHanhService;
        this.donHangRepository = donHangRepository;
    }

    // ==========================
    // Người dùng gửi yêu cầu bảo hành
    // ==========================
   @PostMapping("/send")
    public ResponseEntity<?> sendWarrantyRequest(
            @RequestBody Map<String, Object> payload) {
        try {
            Integer userId = Integer.parseInt(payload.get("maNguoiDung").toString());

            String maDonHangCode = payload.get("maDonHangCode") == null ? "" : payload.get("maDonHangCode").toString();
            String loaiSanPham = payload.get("loaiSanPham") == null ? "" : payload.get("loaiSanPham").toString();
            String moTaLoi = payload.get("moTaLoi") == null ? "" : payload.get("moTaLoi").toString();
            String hoTen = payload.get("hoTen") == null ? "" : payload.get("hoTen").toString();
            String sdt = payload.get("sdt") == null ? "" : payload.get("sdt").toString();
            String hinhThuc = payload.get("hinhThucGiaoNhan") == null ? "GUI_BUU_DIEN" : payload.get("hinhThucGiaoNhan").toString();

            if (maDonHangCode.isBlank() || loaiSanPham.isBlank() || moTaLoi.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Vui lòng nhập đầy đủ thông tin bảo hành."));
            }

            BaoHanh baoHanh = new BaoHanh();
            baoHanh.setMaNguoiDung(userId);
            baoHanh.setHoTen(hoTen);
            baoHanh.setSoDienThoai(sdt);
            baoHanh.setMaDonHangCode(maDonHangCode);
            baoHanh.setLoaiSanPham(loaiSanPham);
            baoHanh.setMoTaLoi(moTaLoi);
            baoHanh.setHinhThucGiaoNhan(hinhThuc);
            baoHanh.setTrangThai("CHO_XU_LY");

            BaoHanh saved = baoHanhService.saveRequest(baoHanh);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Gửi yêu cầu thành công.",
                            "data", saved));
        } catch (Exception e) {
            // In chi tiết lỗi ra console IntelliJ để bạn dễ theo dõi
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
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
    public List<BaoHanh> getHistory(@PathVariable Integer userId) {
        return baoHanhService.findByMaNguoiDung(userId);
    }

    @GetMapping("/my-requests")
    public List<BaoHanh> getMyWarrantyRequests(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("USER_ID");
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
    try {
        String trangThai = (String) body.get("trangThai");
        String thoiGianHen = null;
        if (body.get("thoiGianHen") != null && !body.get("thoiGianHen").toString().isBlank()) {
            thoiGianHen = body.get("thoiGianHen").toString();
        }

        BaoHanh bh = baoHanhService.updateStatus(id, trangThai, thoiGianHen);

        return ResponseEntity.ok(
                Map.of("message", "Cập nhật trạng thái và gửi email thành công.", "data", bh));
    } catch (IllegalStateException | IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Đã xảy ra lỗi hệ thống."));
    }
}

    // ==========================
    // USER HỦY YÊU CẦU
    // ==========================
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Integer id) {
        try {
            BaoHanh bh = baoHanhService.cancelRequest(id);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Đã hủy yêu cầu bảo hành.",
                            "data", bh));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================
    // USER XÁC NHẬN LỊCH HẸN DO ADMIN ĐỀ XUẤT
    // ==========================
@PutMapping("/{id}/confirm-schedule")
public ResponseEntity<?> confirmSchedule(@PathVariable Integer id) {
    try {
        BaoHanh bh = baoHanhService.confirmSchedule(id);
        return ResponseEntity.ok(Map.of("message", "Xác nhận lịch hẹn thành công!", "data", bh));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
    // ==========================
    // USER YÊU CẦU ĐỔI LỊCH HẸN KHÁC (Tự nhập giờ mong muốn)
    // ==========================
    @PutMapping("/{id}/reschedule-request")
    public ResponseEntity<?> rescheduleRequest(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String thoiGianMoi = (body != null) ? body.get("thoiGianMongMuon") : null;
            BaoHanh bh = baoHanhService.requestReschedule(id, thoiGianMoi);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Đã gửi yêu cầu đổi lịch thành công đến hệ thống.",
                            "data", bh));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

@GetMapping("/lookup")
    public ResponseEntity<?> lookupWarranty(
            @RequestParam("code") String code, 
            @RequestParam(value = "userId", required = false) Integer userId) {
        
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập mã đơn hàng cần tra cứu!"));
        }

        // Nếu Database của bạn LƯU CÓ DẤU # -> Giữ nguyên code.trim()
        // Nếu Database của bạn KHÔNG LƯU DẤU # -> Dùng .replace("#", "") như bên dưới
        String cleanCode = code.trim(); // Thay đổi ở đây tùy thuộc vào DB của bạn
        
        // 1. Tìm đơn hàng
        var optionalOrder = donHangRepository.findByMaDonHangCode(cleanCode);
        
        // (Phòng hờ nếu người dùng nhập có # mà DB không lưu, hoặc ngược lại)
        if (optionalOrder.isEmpty() && code.contains("#")) {
            optionalOrder = donHangRepository.findByMaDonHangCode(code.trim().replace("#", ""));
        } else if (optionalOrder.isEmpty()) {
            optionalOrder = donHangRepository.findByMaDonHangCode("#" + code.trim());
        }

        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Mã đơn hàng '" + code + "' không tồn tại trong hệ thống!"));
        }

        var order = optionalOrder.get();

        // 2. Kiểm tra quyền sở hữu
        if (userId != null && order.getMaNguoiDung() != null && !order.getMaNguoiDung().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền tra cứu mã đơn hàng của người khác!"));
        }

        // 3. Lấy tên sản phẩm và số tháng bảo hành linh hoạt từ Database
        String tenSanPham = "Sản phẩm Velora";
        int warrantyMonths = 12; // Giá trị mặc định phòng hờ
        
        if (order.getChiTietDonHangs() != null && !order.getChiTietDonHangs().isEmpty()) {
            var firstItem = order.getChiTietDonHangs().get(0);
            if (firstItem.getSanPham() != null) {
                var sanPham = firstItem.getSanPham();
                tenSanPham = sanPham.getTenSanPham();
                
                if (sanPham.getThoiGianBaoHanh() != null) {
                    warrantyMonths = sanPham.getThoiGianBaoHanh();
                }
            }
        }

        // 4. Tính toán thời gian hết hạn dựa trên ngày mua hàng (NgayTao)
        LocalDateTime ngayMua = order.getNgayTao() != null ? order.getNgayTao() : LocalDateTime.now();
        LocalDateTime ngayHetHan = ngayMua.plusMonths(warrantyMonths);
        
        boolean isValid = LocalDateTime.now().isBefore(ngayHetHan);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngayMuaStr = ngayMua.format(formatter);
        String ngayHetHanStr = ngayHetHan.format(formatter);

        String trangThaiBaoHanh = isValid 
            ? "Còn hạn bảo hành (Đến ngày " + ngayHetHanStr + ")" 
            : "Đã hết hạn bảo hành (Hết hạn từ ngày " + ngayHetHanStr + ")";

        // Trả về kết quả
        return ResponseEntity.ok(Map.of(
            "maDonHang", code,
            "tenSanPham", tenSanPham,
            "isValid", isValid,
            "trangThaiBaoHanh", trangThaiBaoHanh,
            "ngayMua", ngayMuaStr,
            "hanBaoHanh", ngayHetHanStr,
            "soThangBaoHanh", warrantyMonths
        ));
    }
   @GetMapping("/kiem-tra")
    public ResponseEntity<?> kiemTraDonHang(
            @RequestParam("code") String code, 
            @RequestParam(value = "userId", required = false) Integer userId) {
        
        // Loại bỏ dấu # nếu người dùng lỡ nhập vào để khớp chính xác với Database
        String cleanCode = code.trim().replace("#", "");

        var optionalOrder = donHangRepository.findByMaDonHangCode(cleanCode);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Mã đơn hàng không tồn tại trong hệ thống!"));
        }

        var order = optionalOrder.get();

        if (userId != null && order.getMaNguoiDung() != null && !order.getMaNguoiDung().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Đơn hàng này không thuộc quyền sở hữu của bạn!"));
        }

        List<Map<String, Object>> itemsList = new java.util.ArrayList<>();
        if (order.getChiTietDonHangs() != null && !order.getChiTietDonHangs().isEmpty()) {
            for (var ct : order.getChiTietDonHangs()) {
                String tenSp = ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "Sản phẩm Velora";
                String maSp = ct.getSanPham() != null ? String.valueOf(ct.getSanPham().getMaSanPham()) : "N/A";
                int soLuong = ct.getSoLuong() != null ? ct.getSoLuong() : 1;

                itemsList.add(Map.of(
                    "ten", tenSp,
                    "maSanPham", maSp,
                    "soLuong", soLuong
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
            "isValid", true,
            "message", "Mã đơn hàng hợp lệ.",
            "items", itemsList
        ));
    }
}