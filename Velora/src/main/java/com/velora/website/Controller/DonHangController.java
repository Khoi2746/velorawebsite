package com.velora.website.Controller;

import com.velora.website.Entity.DonHang;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Request.SepayResponse;
import com.velora.website.Request.SePayWebhookDto;
import com.velora.website.Service.EmailService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/don-hang")
// ĐÃ FIX: Dùng originPatterns = "*" thay cho origins = "*" để không bị đụng allowCredentials
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true") 
@RequiredArgsConstructor 
public class DonHangController {

    private final DonHangRepository donHangRepository;
    private final EmailService emailService; // 🔥 BỔ SUNG Inject EmailService

    private static final String ADMIN_EMAIL = "admin@velora.com"; 

    @GetMapping
    public ResponseEntity<List<DonHang>> getAllDonHang() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }

    @GetMapping("/nguoi-dung/{maNguoiDung}")
    public ResponseEntity<List<DonHang>> getDonHangByNguoiDung(@PathVariable Integer maNguoiDung) {
        return ResponseEntity.ok(donHangRepository.findByMaNguoiDungOrderByMaDonHangDesc(maNguoiDung));
    }

    @GetMapping("/check-status")
    public ResponseEntity<?> checkDonHangPaidStatus(@RequestParam String code) {
        String codeClean = code.toUpperCase().replaceAll("[^A-Z0-9]", "");

        List<DonHang> allOrders = donHangRepository.findAll();
        DonHang found = null;
        for (DonHang dh : allOrders) {
            if (dh.getMaDonHangCode() != null) {
                String dbCodeClean = dh.getMaDonHangCode().toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (dbCodeClean.equals(codeClean)) {
                    found = dh;
                    break;
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        if (found != null) {
            boolean isPaid = "DA_THANH_TOAN".equalsIgnoreCase(found.getTrangThaiThanhToan()) 
                          || "Đã thanh toán".equalsIgnoreCase(found.getTrangThaiThanhToan());
            
            response.put("paid", isPaid);
            response.put("status", found.getTrangThaiThanhToan());
            return ResponseEntity.ok(response);
        }
        
        response.put("paid", false);
        response.put("message", "Không tìm thấy mã đơn hàng");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/webhook-sepay")
    @Transactional
    public ResponseEntity<SepayResponse> receiveBankWebhook(@RequestBody SePayWebhookDto webhookData) {
        try {
            Double moneyReceived = 0.0;
            if (webhookData.getTransferAmount() != null && webhookData.getTransferAmount() > 0) {
                moneyReceived = webhookData.getTransferAmount();
            } else if (webhookData.getAmountIn() != null && webhookData.getAmountIn() > 0) {
                moneyReceived = webhookData.getAmountIn();
            }

            if (moneyReceived > 0) {
                String noiDung = webhookData.getContent();
                if (noiDung != null) {
                    Pattern pattern = Pattern.compile("VELORA-?\\d+", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(noiDung);

                    String codeTimDuoc = null;
                    if (matcher.find()) {
                        codeTimDuoc = matcher.group().toUpperCase().replaceAll("[^A-Z0-9]", "");
                    }

                    if (codeTimDuoc != null) {
                        List<DonHang> allOrders = donHangRepository.findAll();
                        DonHang donHangKhop = null;

                        for (DonHang dh : allOrders) {
                            if (dh.getMaDonHangCode() != null) {
                                String dbCodeClean = dh.getMaDonHangCode().toUpperCase().replaceAll("[^A-Z0-9]", "");
                                if (dbCodeClean.equals(codeTimDuoc)) {
                                    donHangKhop = dh;
                                    break;
                                }
                            }
                        }

                        if (donHangKhop != null) {
                            donHangKhop.setTrangThaiThanhToan("DA_THANH_TOAN");
                            donHangKhop.setTrangThaiDonHang("CHO_XU_LY");
                            donHangRepository.save(donHangKhop);

                            try {
                                donHangRepository.truSoLuongTonKhoTheoMaDon(donHangKhop.getMaDonHang());
                            } catch (Exception ex) {
                                System.out.println("⚠️ Lỗi trừ kho: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
            return ResponseEntity.ok(new SepayResponse(true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SepayResponse(false));
        }
    }

    @PatchMapping("/{id}/huy")
    @Transactional
    public ResponseEntity<?> huyDonHang(@PathVariable Integer id, @RequestParam(required = false) String lyDo) {
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        
        // 🔥 ĐÃ CHỈNH SỬA: Chặn không cho hủy nếu trạng thái khác CHO_XU_LY
        if (!"CHO_XU_LY".equalsIgnoreCase(donHang.getTrangThaiDonHang())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chỉ có thể hủy đơn hàng đang ở trạng thái chờ xử lý!");
        }

        donHang.setTrangThaiDonHang("DA_HUY");
        if (lyDo != null && !lyDo.trim().isEmpty()) {
            donHang.setLyDoHuyDon(lyDo.trim());
        }

        donHangRepository.save(donHang);
        return ResponseEntity.ok("Hủy đơn hàng thành công!");
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<?> capNhatTrangThai(
            @PathVariable Integer id, 
            @RequestParam String trangThaiMoi,
            @RequestParam(required = false) String trangThaiThanhToanMoi) {
        
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        donHang.setTrangThaiDonHang(trangThaiMoi);

        if (trangThaiThanhToanMoi != null && !trangThaiThanhToanMoi.trim().isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToanMoi);
        } else if (trangThaiMoi != null && ("Đã giao hàng".equalsIgnoreCase(trangThaiMoi.trim()) || "DA_GIAO".equalsIgnoreCase(trangThaiMoi.trim()))) {
            donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        }

        donHangRepository.save(donHang);
        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }

    /**
     * API 1: ĐẶT MUA NGAY 1 SẢN PHẨM
     */
    @PostMapping("/dat-ngay")
    @Transactional
    public ResponseEntity<?> datHangNhanh(@RequestBody DatNgayRequest payload) {
        try {
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0) payload.setMaNguoiDung(3); 
            if (payload.getMaSanPham() == null || payload.getMaSanPham() <= 0) payload.setMaSanPham(2); 

            DonHang donHang = new DonHang();
            donHang.setMaNguoiDung(payload.getMaNguoiDung());
            donHang.setMaDonHangCode(payload.getMaDonHangCode());
            donHang.setTongTien(BigDecimal.valueOf(payload.getTongTien()));
            donHang.setTenNguoiNhan(payload.getTenNguoiNhan());
            donHang.setSoDienThoaiGiaoHang(payload.getSoDienThoaiGiaoHang());
            donHang.setEmail(payload.getEmail()); // 🔥 BỔ SUNG: Lưu email
            donHang.setDiaChiGiaoHang(payload.getDiaChiGiaoHang());
            donHang.setPhuongThucThanhToan(payload.getPhuongThucThanhToan());
            donHang.setTrangThaiDonHang("CHO_XU_LY");
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");
            
            if (payload.getGhiChuDonHang() != null && !payload.getGhiChuDonHang().trim().isEmpty()) {
                donHang.setGhiChuDonHang(payload.getGhiChuDonHang().trim());
            }

            DonHang donHangDaLuu = donHangRepository.save(donHang);
            double giaLucMua = payload.getTongTien() / payload.getSoLuong();

            donHangRepository.insertChiTietDonHang(
                donHangDaLuu.getMaDonHang(), 
                payload.getMaSanPham(), 
                payload.getSoLuong(), 
                giaLucMua
            );

            donHangRepository.xoaSanPhamKhoiGioHang(payload.getMaNguoiDung(), payload.getMaSanPham());

            // 🔥 BỔ SUNG: Gửi mail thông báo
            guiEmailThongBaoDonHang(payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(), payload.getTongTien(), payload.getDiaChiGiaoHang());

            return ResponseEntity.ok("Đặt hàng kiệt tác thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API 2: ĐẶT HÀNG CHO TOÀN BỘ GIỎ HÀNG
     */
    @PostMapping("/dat-gio-hang")
    @Transactional
    public ResponseEntity<?> datHangTuGioHang(@RequestBody DatGioHangRequest payload) {
        try {
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0) payload.setMaNguoiDung(3);

            DonHang donHang = new DonHang();
            donHang.setMaNguoiDung(payload.getMaNguoiDung());
            donHang.setMaDonHangCode(payload.getMaDonHangCode());
            donHang.setTongTien(BigDecimal.valueOf(payload.getTongTien()));
            donHang.setTenNguoiNhan(payload.getTenNguoiNhan());
            donHang.setSoDienThoaiGiaoHang(payload.getSoDienThoaiGiaoHang());
            donHang.setEmail(payload.getEmail()); // 🔥 BỔ SUNG: Lưu email
            donHang.setDiaChiGiaoHang(payload.getDiaChiGiaoHang());
            donHang.setPhuongThucThanhToan(payload.getPhuongThucThanhToan());
            donHang.setTrangThaiDonHang("CHO_XU_LY");
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");

            if (payload.getGhiChuDonHang() != null && !payload.getGhiChuDonHang().trim().isEmpty()) {
                donHang.setGhiChuDonHang(payload.getGhiChuDonHang().trim());
            }

            DonHang donHangDaLuu = donHangRepository.save(donHang);

            // Chuyển chi tiết từ giỏ hàng sang chi tiết đơn hàng
            donHangRepository.chuyenGioHangSangChiTietDonHang(donHangDaLuu.getMaDonHang(), payload.getMaNguoiDung());

            // Xóa sạch giỏ hàng của người dùng trong SQL Server
            donHangRepository.xoaToanBoGioHangCuaUser(payload.getMaNguoiDung());

            // 🔥 BỔ SUNG: Gửi mail thông báo
            guiEmailThongBaoDonHang(payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(), payload.getTongTien(), payload.getDiaChiGiaoHang());

            return ResponseEntity.ok("Đặt hàng giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lý giỏ hàng: " + e.getMessage());
        }
    }

    // 🔥 BỔ SUNG: Hàm phụ trợ gửi Email
    private void guiEmailThongBaoDonHang(String emailKhach, String maCode, String tenKhach, Double tongTien, String diaChi) {
        try {
            String subject = "[VELORA BOUTIQUE] XÁC NHẬN ĐƠN HÀNG #" + maCode;
            String bodyKhach = "Kính chào quý khách " + tenKhach + ",\n\n"
                    + "Cảm ơn quý khách đã tin tưởng và lựa chọn tuyệt tác thời gian tại Velora.\n"
                    + "Thông tin đơn hàng của quý khách:\n"
                    + "- Mã đơn hàng: " + maCode + "\n"
                    + "- Tổng giá trị: " + String.format("%,.0f", tongTien) + " VND\n"
                    + "- Địa chỉ giao hàng: " + diaChi + "\n\n"
                    + "Chúng tôi đang tiến hành chuẩn bị đơn hàng và sẽ sớm giao đến quý khách.\n"
                    + "Trân trọng,\nĐội ngũ Velora.";

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                emailService.sendEmail(emailKhach, subject, bodyKhach);
            }

            String subjectAdmin = "[THÔNG BÁO CÓ ĐƠN HÀNG MỚI] #" + maCode;
            String bodyAdmin = "Hệ thống Velora vừa ghi nhận 1 đơn hàng mới:\n"
                    + "- Mã đơn hàng: " + maCode + "\n"
                    + "- Khách hàng: " + tenKhach + "\n"
                    + "- Email: " + emailKhach + "\n"
                    + "- Tổng tiền: " + String.format("%,.0f", tongTien) + " VND\n"
                    + "- Địa chỉ: " + diaChi + "\n"
                    + "Vui lòng vào trang Quản trị đơn hàng để kiểm tra và duyệt đơn.";

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, bodyAdmin);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận đơn hàng: " + e.getMessage());
        }
    }
}

// ================= DTO CLASSES =================

@Data
class DatNgayRequest {
    private Integer maNguoiDung;
    private String maDonHangCode;
    private Double tongTien;
    private String tenNguoiNhan;
    private String soDienThoaiGiaoHang;
    private String email; // 🔥 BỔ SUNG
    private String diaChiGiaoHang;
    private String phuongThucThanhToan;
    private Integer maSanPham;
    private Integer soLuong;
    private String ghiChuDonHang;
}

@Data
class DatGioHangRequest {
    private Integer maNguoiDung;
    private String maDonHangCode;
    private Double tongTien;
    private String tenNguoiNhan;
    private String soDienThoaiGiaoHang;
    private String email; // 🔥 BỔ SUNG
    private String diaChiGiaoHang;
    private String phuongThucThanhToan;
    private String ghiChuDonHang;
    private String maGiamGia;
}