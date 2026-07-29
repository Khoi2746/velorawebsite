package com.velora.website.Service;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.BaoHanhRepository;
import com.velora.website.Repository.NguoiDungRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BaoHanhService {

    private static final Logger log = LoggerFactory.getLogger(BaoHanhService.class);
    
    private static final String TRANG_THAI_CHO_XU_LY = "CHO_XU_LY";
    private static final String TRANG_THAI_DA_TIEP_NHAN = "DA_TIEP_NHAN";
    private static final String TRANG_THAI_DA_HUY = "DA_HUY";
    private static final String TRANG_THAI_YEU_CAU_DOI_LICH = "YEU_CAU_DOI_LICH";

    private final BaoHanhRepository repo;
    private final NguoiDungRepository nguoiDungRepository;
    private final JavaMailSender mailSender;

    public BaoHanhService(BaoHanhRepository repo, 
                          NguoiDungRepository nguoiDungRepository, 
                          JavaMailSender mailSender) {
        this.repo = repo;
        this.nguoiDungRepository = nguoiDungRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public BaoHanh saveRequest(BaoHanh baoHanh) {
        baoHanh.setTrangThai(TRANG_THAI_CHO_XU_LY);
        BaoHanh saved = repo.save(baoHanh);
        log.info("Đã tạo mới yêu cầu bảo hành ID: {} cho người dùng ID: {}", saved.getMaBaoHanh(), saved.getMaNguoiDung());
        return saved;
    }

    public List<BaoHanh> getAllRequests() {
        return repo.findAll();
    }

    public List<BaoHanh> getPendingRequests() {
        return repo.findByTrangThai(TRANG_THAI_CHO_XU_LY);
    }

    public List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung) {
        return repo.findByMaNguoiDungOrderByNgayGuiDesc(maNguoiDung);
    }

    @Transactional
    // Đã đổi kiểu dữ liệu của thoiGianHen thành String
    public BaoHanh updateStatus(Integer id, String trangThai, String thoiGianHen) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành với ID: " + id));

        String normalizedStatus = trangThai.trim().toUpperCase();
        bh.setTrangThai(normalizedStatus);
        
        if (thoiGianHen != null && !thoiGianHen.isBlank()) {
            bh.setThoiGianHen(thoiGianHen);
        }

        BaoHanh updated = repo.save(bh);
        log.info("Cập nhật trạng thái yêu cầu bảo hành ID: {} thành {}", id, normalizedStatus);

        if (TRANG_THAI_DA_TIEP_NHAN.equals(updated.getTrangThai()) && updated.getMaNguoiDung() != null) {
            sendEmailToCustomerAsync(updated);
        }

        return updated;
    }

    @Transactional
    public BaoHanh cancelRequest(Integer id) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành với ID: " + id));
        
        if (!TRANG_THAI_CHO_XU_LY.equals(bh.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu đang ở trạng thái chờ xử lý.");
        }
        
        bh.setTrangThai(TRANG_THAI_DA_HUY);
        BaoHanh cancelled = repo.save(bh);
        log.info("Đã hủy yêu cầu bảo hành ID: {}", id);
        return cancelled;
    }

    private void sendEmailToCustomerAsync(BaoHanh bh) {
        try {
            NguoiDung user = nguoiDungRepository.findById(bh.getMaNguoiDung()).orElse(null);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Không thể gửi email do không tìm thấy email của User ID: {}", bh.getMaNguoiDung());
                return;
            }

            // Do thoiGianHen giờ là String nên lấy trực tiếp, không cần format nữa
            String formattedTime = (bh.getThoiGianHen() != null && !bh.getThoiGianHen().isBlank()) 
                ? bh.getThoiGianHen() 
                : "Sớm nhất có thể trong giờ hành chính";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("[VELORA CLOCK] Xác Nhận Lịch Hẹn Bảo Hành Đơn Hàng #" + bh.getMaDonHangCode());
            message.setText(
                "Kính gửi Quý khách " + bh.getHoTen() + ",\n\n" +
                "Yêu cầu bảo hành cho dòng sản phẩm [" + bh.getLoaiSanPham() + "] của Quý khách đã được hệ thống Velora tiếp nhận.\n\n" +
                "LỊCH HẸN ĐẾN TRỰC TIẾP SHOP:\n" +
                "- Thời gian: " + formattedTime + "\n" +
                "- Địa điểm: Showroom Velora Clock, 123 Lê Lợi, TP.HCM\n\n" +
                "Quý khách vui lòng mang theo đồng hồ và mã yêu cầu #" + bh.getMaBaoHanh() + " khi đến cửa hàng.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Velora Clock."
            );

            mailSender.send(message);
            log.info("Đã gửi email lịch hẹn thành công tới: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Gửi email thất bại: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public BaoHanh requestReschedule(Integer id, String thoiGianMongMuon) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành ID: " + id));
        
        bh.setTrangThai(TRANG_THAI_YEU_CAU_DOI_LICH);
        
        // Cập nhật lại chuỗi thoiGianHen để lưu thẳng vào DB, giúp Admin thấy được yêu cầu của khách
        if (thoiGianMongMuon != null && !thoiGianMongMuon.isBlank()) {
            bh.setThoiGianHen("Khách yêu cầu đổi sang: " + thoiGianMongMuon);
        }
        
        // Vẫn set vào biến ảo nếu sau này Frontend cần lấy data trực tiếp từ Object không qua DB
        bh.setThoiGianKhachMongMuon(thoiGianMongMuon);
        
        BaoHanh updated = repo.save(bh);
        log.info("Khách hàng yêu cầu đổi lịch cho đơn bảo hành ID: {} với thời gian mong muốn: {}", id, thoiGianMongMuon);
        return updated;
    }
}