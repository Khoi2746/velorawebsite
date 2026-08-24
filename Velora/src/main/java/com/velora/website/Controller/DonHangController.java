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
        
        donHang.setTrangThaiDonHang(trangThaiMoi);

        if (lyDo != null && !lyDo.trim().isEmpty()) {
            donHang.setLyDoHuyDon(lyDo.trim());
        }

        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
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

        boolean isGiaoThanhCong = "DA_GIAO".equalsIgnoreCase(trangThaiMoi) || "HOAN_TAT".equalsIgnoreCase(trangThaiMoi);
        boolean isThanhToanXong = "DA_THANH_TOAN".equalsIgnoreCase(updatedDonHang.getTrangThaiThanhToan());

        if ((isGiaoThanhCong || isThanhToanXong) && !wasThanhCongTruocDo) {
            capNhatBangThongKe(updatedDonHang);
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
        
        int soLuongSp = 0;
        StringBuilder tenCacSanPham = new StringBuilder();
        
        if (donHang.getChiTietDonHangs() != null) {
            for (com.velora.website.Entity.ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
                soLuongSp += ct.getSoLuong();
                if (ct.getSanPham() != null) {
                    if (tenCacSanPham.length() > 0) tenCacSanPham.append(", ");
                    tenCacSanPham.append(ct.getSanPham().getTenSanPham()).append(" (x").append(ct.getSoLuong()).append(")");
                }
            }
        }
        if (soLuongSp == 0) soLuongSp = 1;

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
        
        String dsCu = dtNgay.getDanhSachSanPham() != null ? dtNgay.getDanhSachSanPham() : "";
        if (dsCu.isEmpty()) {
            dtNgay.setDanhSachSanPham(tenCacSanPham.toString());
        } else {
            dtNgay.setDanhSachSanPham(dsCu + ", " + tenCacSanPham.toString());
        }
        
        doanhThuNgayRepository.save(dtNgay);

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

    // ================= KHUNG TEMPLATE EMAIL VELORA LUXURY =================
    private String taoEmailHtmlVelora(String tieuDeChinh, String loiChao, String noiDungChiTiet, String ghiChuFooter) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='UTF-8'></head>"
            + "<body style='margin: 0; padding: 35px 15px; background-color: #0d0805; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
            + "  <div style='max-width: 540px; margin: 0 auto; background-color: #1a0f0a; border: 1px solid #4a3423; border-top: 4px solid #cca15e; box-shadow: 0 15px 40px rgba(0,0,0,0.85);'>"
            + "    <div style='padding: 35px 25px 20px; text-align: center; border-bottom: 1px solid #2d1c12;'>"
            + "      <h1 style='margin: 0; color: #cca15e; font-size: 24px; letter-spacing: 4px; font-weight: 700; text-transform: uppercase;'>VELORA CLOCK</h1>"
            + "      <p style='margin: 6px 0 0; color: #b5a99a; font-size: 11px; letter-spacing: 2px; text-transform: uppercase;'>Thế giới kiệt tác thời gian</p>"
            + "    </div>"
            + "    <div style='padding: 30px 32px; color: #dedede;'>"
            + "      <h2 style='margin-top: 0; color: #cca15e; font-size: 17px; letter-spacing: 1.5px; text-transform: uppercase; border-bottom: 1px solid #2d1c12; padding-bottom: 12px; text-align: center;'>" + tieuDeChinh + "</h2>"
            + "      <p style='font-size: 14px; line-height: 1.6; color: #ffffff;'><strong>" + loiChao + "</strong></p>"
            + "      <div style='background-color: #24140d; border: 1px solid #3d2417; padding: 20px; margin: 20px 0; font-size: 14px; line-height: 1.8; color: #d6d0c7;'>"
            +          noiDungChiTiet
            + "      </div>"
            + "      <p style='font-size: 13px; line-height: 1.6; color: #a39686; margin-top: 20px;'>" + ghiChuFooter + "</p>"
            + "    </div>"
            + "    <div style='padding: 20px; text-align: center; background-color: #120905; border-top: 1px solid #24140c; font-size: 11px; color: #706354; letter-spacing: 0.5px;'>"
            + "      <p style='margin: 0;'>© 2026 VELORA BOUTIQUE. MỌI QUYỀN ĐƯỢC BẢO LƯU.</p>"
            + "      <p style='margin: 4px 0 0;'>Hotline hỗ trợ: 1900 xxxx | TP. Hồ Chí Minh</p>"
            + "    </div>"
            + "  </div>"
            + "</body></html>";
    }

    private void guiEmailXacNhanDatHang(String emailKhach, String maCode, String tenKhach, Double tongTien, String diaChi) {
        try {
            String subject = "[VELORA CLOCK] XÁC NHẬN ĐƠN HÀNG #" + maCode;
            String contentKhach = "<p style='margin:0 0 10px 0;'>Cảm ơn quý khách đã tin tưởng và lựa chọn tuyệt tác thời gian tại Velora Clock.</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> <span style='color: #cca15e;'>#" + maCode + "</span></p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Tổng giá trị:</strong> <span style='color: #cca15e; font-weight: bold;'>" + String.format("%,.0f", tongTien) + " VND</span></p>"
                + "<p style='margin:0;'><strong>• Địa chỉ nhận hàng:</strong> " + diaChi + "</p>";
            
            String htmlKhach = taoEmailHtmlVelora(
                "XÁC NHẬN ĐƠN HÀNG THÀNH CÔNG", 
                "Kính chào quý khách " + (tenKhach != null ? tenKhach : "") + ",", 
                contentKhach, 
                "Hệ thống đang tiến hành đóng gói kiệt tác và sẽ sớm bàn giao cho đơn vị vận chuyển."
            );

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                emailService.sendEmail(emailKhach, subject, htmlKhach);
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG MỚI CẦN XỬ LÝ #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> #" + maCode + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + " (" + emailKhach + ")</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Tổng tiền:</strong> " + String.format("%,.0f", tongTien) + " VND</p>"
                + "<p style='margin:0;'><strong>• Địa chỉ:</strong> " + diaChi + "</p>";

            String htmlAdmin = taoEmailHtmlVelora(
                "CÓ ĐƠN HÀNG MỚI CẦN DUYỆT", 
                "Thông báo Quản Trị Viên,", 
                contentAdmin, 
                "Vui lòng truy cập trang Quản Trị Hệ Thống để kiểm tra kho và xác nhận đơn hàng."
            );

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);
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
                String contentKhach = "<p style='margin:0 0 10px 0;'>Yêu cầu hủy đơn hàng <strong>#" + maCode + "</strong> của quý khách đã được hệ thống ghi nhận thành công.</p>"
                    + "<p style='margin:0 0 10px 0; color: #e6a200;'><strong>• Lý do hủy:</strong> " + (lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Không có lý do cụ thể") + "</p>";
                
                if (isOnline) {
                    contentKhach += "<p style='margin:0; color: #2ecc71;'><strong>• Lưu ý hoàn tiền:</strong> Do đơn hàng thanh toán trực tuyến (" + phuongThuc + "), quý khách vui lòng vào mục <strong>'Đơn Hàng Của Bạn'</strong> trên website và nhấn <strong>'YÊU CẦU HOÀN TIỀN'</strong> để cung cấp số tài khoản nhận lại tiền.</p>";
                } else {
                    contentKhach += "<p style='margin:0;'>Đơn hàng COD đã được đóng thành công. Rất tiếc vì trải nghiệm chưa trọn vẹn lần này.</p>";
                }

                String htmlKhach = taoEmailHtmlVelora(
                    "THÔNG BÁO HỦY ĐƠN HÀNG", 
                    "Kính chào quý khách " + tenKhach + ",", 
                    contentKhach, 
                    "Hân hạnh được phục vụ quý khách trong các trải nghiệm mua sắm tiếp theo."
                );

                emailService.sendEmail(emailKhach, subjectKhach, htmlKhach);
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG ĐÃ BỊ HỦY #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn:</strong> #" + maCode + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + "</p>"
                + "<p style='margin:0 0 6px 0;'><strong>• Thanh toán:</strong> " + phuongThuc + "</p>"
                + "<p style='margin:0;'><strong>• Lý do:</strong> " + (lyDo != null ? lyDo : "Không có") + "</p>";

            if (isOnline) {
                contentAdmin += "<p style='margin:10px 0 0 0; color: #e74c3c; font-weight: bold;'>⚠️ Đơn thanh toán Online: Kế toán sẵn sàng tiếp nhận hồ sơ hoàn tiền khi khách gửi thông tin.</p>";
            }

            String htmlAdmin = taoEmailHtmlVelora(
                "ĐƠN HÀNG ĐÃ BỊ HỦY", 
                "Thông báo Quản Trị Viên,", 
                contentAdmin, 
                "Dữ liệu trạng thái đơn hàng đã được cập nhật tự động trên hệ thống."
            );

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);

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