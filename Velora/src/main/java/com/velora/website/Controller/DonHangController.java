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
import org.springframework.jdbc.core.JdbcTemplate;
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

// =========================================================================
// [PHẦN 1: CẤU HÌNH CONTROLLER & DEPENDENCY INJECTION]
// =========================================================================
@RestController
@RequestMapping("/api/don-hang")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true", methods = { RequestMethod.GET,
        RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
@RequiredArgsConstructor
public class DonHangController {

    private final DonHangRepository donHangRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;

    // Bổ sung JdbcTemplate để chọc thẳng SQL xử lý tự động trừ mã giảm giá
    private final JdbcTemplate jdbcTemplate;

    private static final String ADMIN_EMAIL = "veloraclock@gmail.com";

    // =========================================================================
    // [PHẦN 2: CÁC API TRUY VẤN VÀ KIỂM TRA TRẠNG THÁI ĐƠN HÀNG]
    // =========================================================================

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

    // =========================================================================
    // [PHẦN 3: XỬ LÝ WEBHOOK TỰ ĐỘNG TỪ CỔNG THANH TOÁN NGÂN HÀNG (SEPAY)]
    // =========================================================================

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

                            try {
                                String ghiChuSePay = (donHangKhop.getGhiChuDonHang() != null
                                        && !donHangKhop.getGhiChuDonHang().trim().isEmpty())
                                                ? donHangKhop.getGhiChuDonHang().trim()
                                                : "Không có";

                                String subjectAdminSePay = "[SEPAY - ĐÃ NHẬN TIỀN] ĐƠN HÀNG #"
                                        + donHangKhop.getMaDonHangCode();
                                String contentAdminSePay = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> #"
                                        + donHangKhop.getMaDonHangCode() + "</p>"
                                        + "<p style='margin:0 0 6px 0;'><strong>• Số tiền nhận được:</strong> <span style='color: #27ae60; font-weight: bold;'>"
                                        + String.format("%,.0f", moneyReceived) + " VND</span></p>"
                                        + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> "
                                        + donHangKhop.getTenNguoiNhan() + " (" + donHangKhop.getEmail() + ")</p>"
                                        + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ nhận hàng:</strong> "
                                        + donHangKhop.getDiaChiGiaoHang() + "</p>"
                                        + "<p style='margin:0 0 6px 0; color: #cca15e;'><strong>• Ghi chú của khách:</strong> <em>"
                                        + ghiChuSePay + "</em></p>"
                                        + "<p style='margin:0;'><strong>• Trạng thái tiền:</strong> ĐÃ THANH TOÁN TỰ ĐỘNG QUA QR NGÂN HÀNG</p>";

                                String htmlAdminSePay = taoEmailHtmlVelora(
                                        "GIAO DỊCH CHUYỂN KHOẢN THÀNH CÔNG",
                                        "Thông báo Quản Trị Viên,",
                                        contentAdminSePay,
                                        "Đơn hàng đã được đối soát thành công. Vui lòng vào trang quản trị chuẩn bị kiệt tác để giao hàng.");

                                emailService.sendEmail(ADMIN_EMAIL, subjectAdminSePay, htmlAdminSePay);
                            } catch (Exception exMail) {
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

    // =========================================================================
    // [PHẦN 4: CẬP NHẬT TRẠNG THÁI & HỦY ĐƠN HÀNG]
    // =========================================================================

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
                || "HOAN_TAT".equalsIgnoreCase(trangThaiCu)
                || "DA_THANH_TOAN".equalsIgnoreCase(thanhToanCu);

        donHang.setTrangThaiDonHang(trangThaiMoi);

        if (lyDo != null && !lyDo.trim().isEmpty()) {
            donHang.setLyDoHuyDon(lyDo.trim());
        }

        if ("DA_HUY".equalsIgnoreCase(trangThaiMoi)) {
            if (!"CHO_XU_LY".equalsIgnoreCase(trangThaiCu)
                    && !"CHUAN_BI_HANG".equalsIgnoreCase(trangThaiCu)
                    && !"YEU_CAU_HUY".equalsIgnoreCase(trangThaiCu)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Chỉ được hủy đơn hàng ở trạng thái Chờ xử lý, Chuẩn bị hàng hoặc Đang chờ duyệt hủy!");
            }
        }

        if (trangThaiThanhToanMoi != null && !trangThaiThanhToanMoi.trim().isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToanMoi);
        } else if ("DA_GIAO".equalsIgnoreCase(trangThaiMoi) || "HOAN_TAT".equalsIgnoreCase(trangThaiMoi)) {
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

    @PatchMapping("/{id}/phuong-thuc-thanh-toan")
    @Transactional
    public ResponseEntity<?> capNhatPhuongThucThanhToan(@PathVariable Integer id, @RequestParam String phuongThucMoi) {
        Optional<DonHang> optionalDonHang = donHangRepository.findById(id);
        if (!optionalDonHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: Không tìm thấy đơn hàng cần cập nhật phương thức!");
        }
        if (phuongThucMoi == null || phuongThucMoi.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: Phương thức thanh toán không được để trống!");
        }

        DonHang donHang = optionalDonHang.get();
        donHang.setPhuongThucThanhToan(phuongThucMoi.trim());
        donHangRepository.save(donHang);

        return ResponseEntity.ok("Cập nhật phương thức thanh toán thành công!");
    }

    @PatchMapping("/{id}/huy")
    @Transactional
    public ResponseEntity<?> huyDonHangNhanh(@PathVariable Integer id, @RequestParam(required = false) String lyDo) {
        return capNhatTrangThaiHoacHuy(id, "DA_HUY", null, lyDo);
    }

    // =========================================================================
    // [PHẦN 5: NGHIỆP VỤ ĐẶT HÀNG (MUA NGAY & ĐẶT TỪ GIỎ HÀNG)]
    // =========================================================================

    @PostMapping("/dat-ngay")
    @Transactional
    public ResponseEntity<?> datHangNhanh(@RequestBody DatNgayRequest payload) {
        try {
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0)
                payload.setMaNguoiDung(3);
            if (payload.getMaSanPham() == null || payload.getMaSanPham() <= 0)
                payload.setMaSanPham(2);

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

            donHangRepository.insertChiTietDonHang(donHangDaLuu.getMaDonHang(), payload.getMaSanPham(),
                    payload.getSoLuong(), giaLucMua);
            donHangRepository.xoaSanPhamKhoiGioHang(payload.getMaNguoiDung(), payload.getMaSanPham());

            // 🔥 TĂNG LƯỢT DÙNG MÃ GIẢM GIÁ (Nếu có áp dụng voucher)
            if (payload.getMaGiamGia() != null && !payload.getMaGiamGia().trim().isEmpty()) {
                xuLyTangLuotDungVoucher(payload.getMaGiamGia().trim());
            }

            guiEmailXacNhanDatHang(
                    payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(),
                    payload.getTongTien(), payload.getDiaChiGiaoHang(), payload.getGhiChuDonHang());

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
            if (payload.getMaNguoiDung() == null || payload.getMaNguoiDung() <= 0)
                payload.setMaNguoiDung(3);

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

            // 🔥 TĂNG LƯỢT DÙNG MÃ GIẢM GIÁ (Nếu có áp dụng voucher)
            if (payload.getMaGiamGia() != null && !payload.getMaGiamGia().trim().isEmpty()) {
                xuLyTangLuotDungVoucher(payload.getMaGiamGia().trim());
            }

            guiEmailXacNhanDatHang(
                    payload.getEmail(), payload.getMaDonHangCode(), payload.getTenNguoiNhan(),
                    payload.getTongTien(), payload.getDiaChiGiaoHang(), payload.getGhiChuDonHang());

            return ResponseEntity.ok("Đặt hàng giỏ hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi xử lý giỏ hàng: " + e.getMessage());
        }
    }

    // =========================================================================
    // [PHẦN 6: HÀM NỘI BỘ (XỬ LÝ LOGIC NGẦM & THỐNG KÊ)]
    // =========================================================================

    /**
     * Hàm tự động cập nhật giảm số lượng hiển thị của voucher sau khi đặt hàng
     */
    private void xuLyTangLuotDungVoucher(String maCode) {
        try {
            // Tăng trong bảng ma_giam_gia (Kho mã)
            String sqlVoucher = "UPDATE ma_giam_gia SET so_luot_da_dung = so_luot_da_dung + 1 WHERE ma_code = ? AND so_luot_da_dung < gioi_han_su_dung";
            int affected = jdbcTemplate.update(sqlVoucher, maCode);

            // Nếu update thành công, đồng bộ luôn sang bảng Bài Viết Marketing
            if (affected > 0) {
                jdbcTemplate.update("UPDATE BaiVietMarketing SET SoLuotDaDung = SoLuotDaDung + 1 WHERE MaGiamGia = ?",
                        maCode);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật lượt dùng mã giảm giá: " + e.getMessage());
        }
    }

    /**
     * Tự động cộng dồn doanh thu ngày và tháng (Fix sạch lỗi NullPointerException)
     */
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
                    if (tenCacSanPham.length() > 0)
                        tenCacSanPham.append(", ");
                    tenCacSanPham.append(ct.getSanPham().getTenSanPham()).append(" (x").append(ct.getSoLuong())
                            .append(")");
                }
            }
        }
        if (soLuongSp == 0)
            soLuongSp = 1;

        // -----------------------------------------------------------
        // 1. CẬP NHẬT DOANH THU NGÀY (BẢO VỆ CHỐNG NULL)
        // -----------------------------------------------------------
        com.velora.website.Entity.DoanhThuNgay dtNgay = doanhThuNgayRepository.findByNgayChinhXac(ngayHienTai);
        if (dtNgay == null) {
            dtNgay = new com.velora.website.Entity.DoanhThuNgay();
            dtNgay.setNgay(ngayHienTai);
            dtNgay.setTongDoanhThu(BigDecimal.ZERO);
            dtNgay.setSoDonHangThanhCong(0);
            dtNgay.setSoSanPhamBanRa(0);
            dtNgay.setDanhSachSanPham("");
        }

        double currentTienNgay = dtNgay.getTongDoanhThu() != null ? dtNgay.getTongDoanhThu().doubleValue() : 0.0;
        int currentDonNgay = dtNgay.getSoDonHangThanhCong() != null ? dtNgay.getSoDonHangThanhCong() : 0;
        int currentSpNgay = dtNgay.getSoSanPhamBanRa() != null ? dtNgay.getSoSanPhamBanRa() : 0;

        dtNgay.setTongDoanhThu(BigDecimal.valueOf(currentTienNgay + tienDonHang.doubleValue()));
        dtNgay.setSoDonHangThanhCong(currentDonNgay + 1);
        dtNgay.setSoSanPhamBanRa(currentSpNgay + soLuongSp);

        String dsCu = dtNgay.getDanhSachSanPham() != null ? dtNgay.getDanhSachSanPham() : "";
        if (dsCu.isEmpty()) {
            dtNgay.setDanhSachSanPham(tenCacSanPham.toString());
        } else {
            dtNgay.setDanhSachSanPham(dsCu + ", " + tenCacSanPham.toString());
        }

        doanhThuNgayRepository.save(dtNgay);

        // -----------------------------------------------------------
        // 2. CẬP NHẬT DOANH THU THÁNG (BẢO VỆ CHỐNG NULL VÀ FIX LỖI GÕ NHẦM BIẾN)
        // -----------------------------------------------------------
        com.velora.website.Entity.DoanhThuThang dtThang = doanhThuThangRepository.findByThangVaNamChinhXac(thang, nam);
        if (dtThang == null) {
            dtThang = new com.velora.website.Entity.DoanhThuThang();
            dtThang.setThang(thang);
            dtThang.setNam(nam);
            dtThang.setTongDoanhThu(BigDecimal.ZERO);
            dtThang.setSoDonHangThanhCong(0);
            dtThang.setSoSanPhamBanRa(0); // 🔥 LỖI SAI BIẾN ĐÃ ĐƯỢC FIX TẠI ĐÂY
        }

        double currentTienThang = dtThang.getTongDoanhThu() != null ? dtThang.getTongDoanhThu().doubleValue() : 0.0;
        int currentDonThang = dtThang.getSoDonHangThanhCong() != null ? dtThang.getSoDonHangThanhCong() : 0;
        int currentSpThang = dtThang.getSoSanPhamBanRa() != null ? dtThang.getSoSanPhamBanRa() : 0;

        dtThang.setTongDoanhThu(BigDecimal.valueOf(currentTienThang + tienDonHang.doubleValue()));
        dtThang.setSoDonHangThanhCong(currentDonThang + 1);
        dtThang.setSoSanPhamBanRa(currentSpThang + soLuongSp);

        doanhThuThangRepository.save(dtThang);
    }

    /**
     * Dựng giao diện Email HTML phong cách Velora Luxury
     */
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
                + "      <h2 style='margin-top: 0; color: #cca15e; font-size: 17px; letter-spacing: 1.5px; text-transform: uppercase; border-bottom: 1px solid #2d1c12; padding-bottom: 12px; text-align: center;'>"
                + tieuDeChinh + "</h2>"
                + "      <p style='font-size: 14px; line-height: 1.6; color: #ffffff;'><strong>" + loiChao
                + "</strong></p>"
                + "      <div style='background-color: #24140d; border: 1px solid #3d2417; padding: 20px; margin: 20px 0; font-size: 14px; line-height: 1.8; color: #d6d0c7;'>"
                + noiDungChiTiet
                + "      </div>"
                + "      <p style='font-size: 13px; line-height: 1.6; color: #a39686; margin-top: 20px;'>"
                + ghiChuFooter + "</p>"
                + "    </div>"
                + "    <div style='padding: 20px; text-align: center; background-color: #120905; border-top: 1px solid #24140c; font-size: 11px; color: #706354; letter-spacing: 0.5px;'>"
                + "      <p style='margin: 0;'>© 2026 VELORA BOUTIQUE. MỌI QUYỀN ĐƯỢC BẢO LƯU.</p>"
                + "      <p style='margin: 4px 0 0;'>Hotline hỗ trợ: 1900 xxxx | TP. Hồ Chí Minh</p>"
                + "    </div>"
                + "  </div>"
                + "</body></html>";
    }

    private void guiEmailXacNhanDatHang(String emailKhach, String maCode, String tenKhach, Double tongTien,
            String diaChi, String ghiChu) {
        try {
            String ghiChuHienThi = (ghiChu != null && !ghiChu.trim().isEmpty()) ? ghiChu.trim() : "Không có";

            String subject = "[VELORA CLOCK] XÁC NHẬN ĐƠN HÀNG #" + maCode;
            String contentKhach = "<p style='margin:0 0 10px 0;'>Cảm ơn quý khách đã tin tưởng và lựa chọn tuyệt tác thời gian tại Velora Clock.</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> <span style='color: #cca15e;'>#"
                    + maCode + "</span></p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Tổng giá trị:</strong> <span style='color: #cca15e; font-weight: bold;'>"
                    + String.format("%,.0f", tongTien) + " VND</span></p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ nhận hàng:</strong> " + diaChi + "</p>"
                    + "<p style='margin:0;'><strong>• Ghi chú đơn hàng:</strong> <em>" + ghiChuHienThi + "</em></p>";

            String htmlKhach = taoEmailHtmlVelora(
                    "XÁC NHẬN ĐƠN HÀNG THÀNH CÔNG",
                    "Kính chào quý khách " + (tenKhach != null ? tenKhach : "") + ",",
                    contentKhach,
                    "Hệ thống đang tiến hành đóng gói kiệt tác và sẽ sớm bàn giao cho đơn vị vận chuyển.");

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                emailService.sendEmail(emailKhach, subject, htmlKhach);
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG MỚI CẦN XỬ LÝ #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn hàng:</strong> #" + maCode + "</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + " (" + emailKhach
                    + ")</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Tổng tiền:</strong> " + String.format("%,.0f", tongTien)
                    + " VND</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Địa chỉ:</strong> " + diaChi + "</p>"
                    + "<p style='margin:0; color: #cca15e;'><strong>• Ghi chú của khách:</strong> " + ghiChuHienThi
                    + "</p>";

            String htmlAdmin = taoEmailHtmlVelora(
                    "CÓ ĐƠN HÀNG MỚI CẦN DUYỆT",
                    "Thông báo Quản Trị Viên,",
                    contentAdmin,
                    "Vui lòng truy cập trang Quản Trị Hệ Thống để kiểm tra kho và xác nhận đơn hàng.");

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);
        } catch (Exception e) {
        }
    }

    private void guiEmailThongBaoHuyDon(DonHang donHang, String lyDo) {
        try {
            String emailKhach = donHang.getEmail();
            String maCode = donHang.getMaDonHangCode();
            String tenKhach = donHang.getTenNguoiNhan();
            String phuongThuc = donHang.getPhuongThucThanhToan();
            boolean isOnline = (phuongThuc != null && !phuongThuc.toUpperCase().contains("COD"));
            String ghiChuDon = (donHang.getGhiChuDonHang() != null && !donHang.getGhiChuDonHang().trim().isEmpty())
                    ? donHang.getGhiChuDonHang().trim()
                    : "Không có";

            if (emailKhach != null && !emailKhach.trim().isEmpty()) {
                String subjectKhach = "[VELORA CLOCK] THÔNG BÁO HỦY ĐƠN HÀNG #" + maCode;
                String contentKhach = "<p style='margin:0 0 10px 0;'>Yêu cầu hủy đơn hàng <strong>#" + maCode
                        + "</strong> của quý khách đã được hệ thống ghi nhận thành công.</p>"
                        + "<p style='margin:0 0 10px 0; color: #e6a200;'><strong>• Lý do hủy:</strong> "
                        + (lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Không có lý do cụ thể") + "</p>";

                if (isOnline) {
                    contentKhach += "<p style='margin:0; color: #2ecc71;'><strong>• Lưu ý hoàn tiền:</strong> Do đơn hàng thanh toán trực tuyến ("
                            + phuongThuc
                            + "), quý khách vui lòng vào mục <strong>'Đơn Hàng Của Bạn'</strong> trên website và nhấn <strong>'YÊU CẦU HOÀN TIỀN'</strong> để cung cấp số tài khoản nhận lại tiền.</p>";
                } else {
                    contentKhach += "<p style='margin:0;'>Đơn hàng COD đã được đóng thành công. Rất tiếc vì trải nghiệm chưa trọn vẹn lần này.</p>";
                }

                String htmlKhach = taoEmailHtmlVelora(
                        "THÔNG BÁO HỦY ĐƠN HÀNG",
                        "Kính chào quý khách " + tenKhach + ",",
                        contentKhach,
                        "Hân hạnh được phục vụ quý khách trong các trải nghiệm mua sắm tiếp theo.");

                emailService.sendEmail(emailKhach, subjectKhach, htmlKhach);
            }

            String subjectAdmin = "[QUẢN TRỊ] ĐƠN HÀNG ĐÃ BỊ HỦY #" + maCode;
            String contentAdmin = "<p style='margin:0 0 6px 0;'><strong>• Mã đơn:</strong> #" + maCode + "</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Khách hàng:</strong> " + tenKhach + "</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Thanh toán:</strong> " + phuongThuc + "</p>"
                    + "<p style='margin:0 0 6px 0;'><strong>• Ghi chú ban đầu:</strong> " + ghiChuDon + "</p>"
                    + "<p style='margin:0;'><strong>• Lý do hủy:</strong> " + (lyDo != null ? lyDo : "Không có")
                    + "</p>";

            if (isOnline) {
                contentAdmin += "<p style='margin:10px 0 0 0; color: #e74c3c; font-weight: bold;'>⚠️ Đơn thanh toán Online: Kế toán sẵn sàng tiếp nhận hồ sơ hoàn tiền khi khách gửi thông tin.</p>";
            }

            String htmlAdmin = taoEmailHtmlVelora(
                    "ĐƠN HÀNG ĐÃ BỊ HỦY",
                    "Thông báo Quản Trị Viên,",
                    contentAdmin,
                    "Dữ liệu trạng thái đơn hàng đã được cập nhật tự động trên hệ thống.");

            emailService.sendEmail(ADMIN_EMAIL, subjectAdmin, htmlAdmin);

        } catch (Exception e) {
        }
    }
}

// =========================================================================
// [PHẦN 7: CÁC DTO / REQUEST OBJECT NHẬN DỮ LIỆU TỪ CLIENT]
// =========================================================================

@Data
class DatNgayRequest {
    private Integer maNguoiDung; // ID người mua
    private String maDonHangCode; // Mã code đơn hàng (VD: VELORA1001)
    private Double tongTien; // Tổng tiền đơn hàng
    private String tenNguoiNhan; // Họ tên người nhận
    private String soDienThoaiGiaoHang; // SĐT giao hàng
    private String email; // Email nhận thông báo
    private String diaChiGiaoHang; // Địa chỉ nhận hàng
    private String phuongThucThanhToan; // COD, VNPAY, CHUYEN_KHOAN_QR
    private Integer maSanPham; // ID sản phẩm mua ngay
    private Integer soLuong; // Số lượng đặt
    private String ghiChuDonHang; // Ghi chú của khách
    private String maGiamGia; // Mã khuyến mãi voucher (nếu có)
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