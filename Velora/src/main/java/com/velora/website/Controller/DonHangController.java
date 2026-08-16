package com.velora.website.Controller;

import com.velora.website.Entity.DonHang;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Repository.DoanhThuNgayRepository;
import com.velora.website.Repository.DoanhThuThangRepository;
import com.velora.website.Request.SepayResponse;
import com.velora.website.Request.SePayWebhookDto;
import com.velora.website.Service.EmailService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/don-hang")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}) 
@RequiredArgsConstructor 
public class DonHangController {

    private final DonHangRepository donHangRepository;
    private final EmailService emailService; 
    private final SimpMessagingTemplate messagingTemplate;
    
    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;

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

                            // Bơm tiền & Bắn sóng WebSocket
                            capNhatBangThongKe(donHangKhop);
                            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");

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

    /**
     * LUỒNG CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG CHÍNH THỨC
     */
    @PatchMapping("/{id}/trang-thai")
    @Transactional
    public ResponseEntity<?> capNhatTrangThaiHoacHuy(
            @PathVariable Integer id, 
            @RequestParam String trangThaiMoi,
            @RequestParam(required = false) String trangThaiThanhToanMoi,
            @RequestParam(required = false) String lyDo) {
        
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lỗi: Không tìm thấy đơn hàng!");
        }

        DonHang donHang = optionalDonHang.get();
        String trangThaiCu = donHang.getTrangThaiDonHang();
        String thanhToanCu = donHang.getTrangThaiThanhToan();

        boolean wasThanhCongTruocDo = "DA_GIAO".equalsIgnoreCase(trangThaiCu) 
                                   || "DA_THANH_TOAN".equalsIgnoreCase(thanhToanCu);
        
        // Cập nhật trạng thái mới
        donHang.setTrangThaiDonHang(trangThaiMoi);

        // 🔥 Luôn lưu lý do vào database nếu có truyền lên (Rất quan trọng cho việc khách hàng gửi YEU_CAU_HUY)
        if (lyDo != null && !lyDo.trim().isEmpty()) {
            donHang.setLyDoHuyDon(lyDo.trim());
        }

        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
            // 🔥 Bổ sung thêm "YEU_CAU_HUY" để Admin có thể duyệt hủy các đơn mà khách đã gửi yêu cầu
            if (!"CHO_XU_LY".equalsIgnoreCase(trangThaiCu) 
                && !"CHUAN_BI_HANG".equalsIgnoreCase(trangThaiCu) 
                && !"YEU_CAU_HUY".equalsIgnoreCase(trangThaiCu)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chỉ được hủy đơn hàng ở trạng thái Chờ xử lý, Chuẩn bị hàng hoặc Đang chờ duyệt hủy!");
            }
        }

        if (trangThaiThanhToanMoi != null && !trangThaiThanhToanMoi.trim().isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToanMoi);
        } else if ("DA_GIAO".equalsIgnoreCase(trangThaiMoi)) {
            donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        }

        DonHang updatedDonHang = donHangRepository.save(donHang);

        // ĐIỀU KIỆN KÍCH HOẠT DOANH THU & WEBSOCKET
        boolean isGiaoThanhCong = "DA_GIAO".equalsIgnoreCase(trangThaiMoi) || "HOAN_TAT".equalsIgnoreCase(trangThaiMoi);
        boolean isThanhToanXong = "DA_THANH_TOAN".equalsIgnoreCase(updatedDonHang.getTrangThaiThanhToan());

        if ((isGiaoThanhCong || isThanhToanXong) && !wasThanhCongTruocDo) {
            capNhatBangThongKe(updatedDonHang);
            System.out.println("🔥 Controller: Đã chốt sổ doanh thu đơn #" + updatedDonHang.getMaDonHangCode() + " và phát sóng WebSocket!");
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");
        }

        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
            guiEmailThongBaoHuyDon(updatedDonHang, lyDo);
        }

        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }

    @PatchMapping("/{id}/huy")
    @Transactional
    public ResponseEntity<?> huyDonHangNhanh(@PathVariable Integer id, @RequestParam(required = false) String lyDo) {
        return capNhatTrangThaiHoacHuy(id, "DA_HUY", null, lyDo);
    }

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
            donHang.setEmail(payload.getEmail()); 
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
            guiEmailXacNhanDatHang(payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(), payload.getTongTien(), payload.getDiaChiGiaoHang());

            return ResponseEntity.ok("Đặt hàng kiệt tác thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

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
            donHang.setEmail(payload.getEmail()); 
            donHang.setDiaChiGiaoHang(payload.getDiaChiGiaoHang());
            donHang.setPhuongThucThanhToan(payload.getPhuongThucThanhToan());
            donHang.setTrangThaiDonHang("CHO_XU_LY");
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");

            if (payload.getGhiChuDonHang() != null && !payload.getGhiChuDonHang().trim().isEmpty()) {
                donHang.setGhiChuDonHang(payload.getGhiChuDonHang().trim());
            }

            DonHang donHangDaLuu = donHangRepository.save(donHang);
            donHangRepository.chuyenGioHangSangChiTietDonHang(donHangDaLuu.getMaDonHang(), payload.getMaNguoiDung());
            donHangRepository.xoaToanBoGioHangCuaUser(payload.getMaNguoiDung());

            guiEmailXacNhanDatHang(payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(), payload.getTongTien(), payload.getDiaChiGiaoHang());

            return ResponseEntity.ok("Đặt hàng giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lý giỏ hàng: " + e.getMessage());
        }
    }

   private void capNhatBangThongKe(DonHang donHang) {
        LocalDate ngayHienTai = LocalDate.now();
        int thang = ngayHienTai.getMonthValue();
        int nam = ngayHienTai.getYear();

        BigDecimal tienDonHang = donHang.getTongTien();
        
        // 🔥 LOGIC MỚI: Lấy danh sách tên sản phẩm trong đơn hàng
        int soLuongSp = 0;
        StringBuilder tenCacSanPham = new StringBuilder();
        
        if (donHang.getChiTietDonHangs() != null) {
            for (com.velora.website.Entity.ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
                soLuongSp += ct.getSoLuong();
                if (ct.getSanPham() != null) {
                    // Cộng dồn tên sản phẩm vào chuỗi (Cách nhau bằng dấu phẩy)
                    if (tenCacSanPham.length() > 0) tenCacSanPham.append(", ");
                    tenCacSanPham.append(ct.getSanPham().getTenSanPham()).append(" (x").append(ct.getSoLuong()).append(")");
                }
            }
        }
        if (soLuongSp == 0) soLuongSp = 1; // Fallback an toàn

        // 1. CẬP NHẬT DOANH THU NGÀY
        com.velora.website.Entity.DoanhThuNgay dtNgay = doanhThuNgayRepository.findByNgayChinhXac(ngayHienTai);
        if (dtNgay == null) {
            dtNgay = new com.velora.website.Entity.DoanhThuNgay();
            dtNgay.setNgay(ngayHienTai);
            dtNgay.setTongDoanhThu(BigDecimal.ZERO);
            dtNgay.setSoDonHangThanhCong(0);
            dtNgay.setSoSanPhamBanRa(0);
            dtNgay.setDanhSachSanPham("");
        }
        
        dtNgay.setTongDoanhThu(dtNgay.getTongDoanhThu().add(tienDonHang));
        dtNgay.setSoDonHangThanhCong(dtNgay.getSoDonHangThanhCong() + 1);
        dtNgay.setSoSanPhamBanRa(dtNgay.getSoSanPhamBanRa() + soLuongSp);
        
        // Nối tên sản phẩm mới bán vào danh sách cũ của ngày hôm đó
        String dsCu = dtNgay.getDanhSachSanPham() != null ? dtNgay.getDanhSachSanPham() : "";
        if (dsCu.isEmpty()) {
            dtNgay.setDanhSachSanPham(tenCacSanPham.toString());
        } else {
            dtNgay.setDanhSachSanPham(dsCu + ", " + tenCacSanPham.toString());
        }
        
        doanhThuNgayRepository.save(dtNgay);

        // 2. CẬP NHẬT DOANH THU THÁNG (Giữ nguyên)
        com.velora.website.Entity.DoanhThuThang dtThang = doanhThuThangRepository.findByThangVaNamChinhXac(thang, nam);
        if (dtThang == null) {
            dtThang = new com.velora.website.Entity.DoanhThuThang();
            dtThang.setThang(thang);
            dtThang.setNam(nam);
            dtThang.setTongDoanhThu(BigDecimal.ZERO);
            dtThang.setSoDonHangThanhCong(0);
            dtThang.setSoSanPhamBanRa(0);
        }
                
        dtThang.setTongDoanhThu(dtThang.getTongDoanhThu().add(tienDonHang));
        dtThang.setSoDonHangThanhCong(dtThang.getSoDonHangThanhCong() + 1);
        dtThang.setSoSanPhamBanRa(dtThang.getSoSanPhamBanRa() + soLuongSp);
        doanhThuThangRepository.save(dtThang);
    }

    private void guiEmailXacNhanDatHang(String emailKhach, String maCode, String tenKhach, Double tongTien, String diaChi) {
        try {
            String subject = "[VELORA CLOCK] XÁC NHẬN ĐƠN HÀNG #" + maCode;
            String bodyKhach = "Kính chào Quý khách " + tenKhach + ",\n\n"
                    + "Cảm ơn Quý khách đã tin tưởng và lựa chọn tuyệt tác thời gian tại Velora Clock.\n"
                    + "Chúng tôi xin gửi thông tin xác nhận đơn hàng của Quý khách như sau:\n\n"
                    + "  - Mã đơn hàng: #" + maCode + "\n"
                    + "  - Tổng giá trị: " + String.format("%,.0f", tongTien) + " VND\n"
                    + "  - Địa chỉ nhận hàng: " + diaChi + "\n\n"
                    + "Hệ thống đang tiến hành đóng gói kiệt tác và sẽ bàn giao cho đơn vị vận chuyển trong thời gian sớm nhất.\n\n"
                    + "Trân trọng,\nĐội ngũ Vận Hành Velora Clock.";

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                emailService.sendEmail(emailKhach, subject, bodyKhach);
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG MỚI CẦN XỬ LÝ #" + maCode;
            String bodyAdmin = "Hệ thống vừa ghi nhận một giao dịch đặt hàng mới:\n\n"
                    + "  - Mã đơn: #" + maCode + "\n"
                    + "  - Khách hàng: " + tenKhach + " (" + emailKhach + ")\n"
                    + "  - Tổng tiền: " + String.format("%,.0f", tongTien) + " VND\n"
                    + "  - Địa chỉ: " + diaChi + "\n\n"
                    + "Vui lòng truy cập trang Quản Trị Hệ Thống để kiểm tra kho và tiến hành xác nhận đơn.";

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, bodyAdmin);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email đặt hàng: " + e.getMessage());
        }
    }

    private void guiEmailThongBaoHuyDon(DonHang donHang, String lyDo) {
        try {
            String emailKhach = donHang.getEmail();
            String maCode = donHang.getMaDonHangCode();
            String tenKhach = donHang.getTenNguoiNhan();
            String phuongThuc = donHang.getPhuongThucThanhToan();
            boolean isOnline = (phuongThuc != null && !phuongThuc.toUpperCase().contains("COD"));

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                String subjectKhach = "[VELORA CLOCK] THÔNG BÁO HỦY ĐƠN HÀNG #" + maCode;
                StringBuilder bodyKhach = new StringBuilder();
                bodyKhach.append("Kính chào Quý khách ").append(tenKhach).append(",\n\n");
                bodyKhach.append("Yêu cầu hủy đơn hàng #").append(maCode).append(" của Quý khách đã được hệ thống ghi nhận thành công.\n");
                bodyKhach.append("Lý do hủy: ").append(lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Không có lý do cụ thể").append("\n\n");

                if (isOnline) {
                    bodyKhach.append("Do đơn hàng này được thanh toán qua hình thức trực tuyến (").append(phuongThuc).append("), ")
                             .append("Quý khách vui lòng truy cập hệ thống website Velora Clock, vào mục 'Đơn Hàng Của Bạn', ")
                             .append("sau đó nhấn vào nút 'YÊU CẦU HOÀN TIỀN' để cung cấp thông tin tài khoản ngân hàng nhận lại tiền.\n\n");
                } else {
                    bodyKhach.append("Đơn hàng thanh toán theo hình thức COD đã được đóng tiến trình thành công. Rất tiếc vì trải nghiệm chưa trọn vẹn lần này.\n\n");
                }

                bodyKhach.append("Trân trọng,\nĐội ngũ Chăm Sóc Khách Hàng Velora Clock.");
                emailService.sendEmail(emailKhach, subjectKhach, bodyKhach.toString());
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG ĐÃ BỊ HỦY #" + maCode;
            StringBuilder bodyAdmin = new StringBuilder();
            bodyAdmin.append("Hệ thống thông báo đơn hàng #").append(maCode).append(" đã bị hủy bởi người dùng.\n\n");
            bodyAdmin.append("  - Khách hàng: ").append(tenKhach).append("\n");
            bodyAdmin.append("  - Hình thức thanh toán: ").append(phuongThuc).append("\n");
            bodyAdmin.append("  - Lý do hủy: ").append(lyDo != null ? lyDo : "Không có").append("\n");

            if (isOnline) {
                bodyAdmin.append("\n⚠️ LƯU Ý: Đơn hàng thanh toán Online. Yêu cầu bộ phận Kế toán chuẩn bị tiếp nhận hồ sơ hoàn tiền khi khách gửi thông tin số tài khoản.");
            }

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, bodyAdmin.toString());

        } catch (Exception e) {
            System.err.println("Lỗi gửi email quy trình hủy đơn: " + e.getMessage());
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
    private String email; 
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
    private String email; 
    private String diaChiGiaoHang;
    private String phuongThucThanhToan;
    private String ghiChuDonHang;
    private String maGiamGia;
}