package com.velora.website.Service;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.BaoHanhRepository;
import com.velora.website.Repository.NguoiDungRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BaoHanhService {

    private final BaoHanhRepository repo;
    private final NguoiDungRepository nguoiDungRepository;
    private final JavaMailSender mailSender;

    public BaoHanhService(BaoHanhRepository repo, NguoiDungRepository nguoiDungRepository, JavaMailSender mailSender) {
        this.repo = repo;
        this.nguoiDungRepository = nguoiDungRepository;
        this.mailSender = mailSender;
    }

    public BaoHanh saveRequest(BaoHanh baoHanh) {
        baoHanh.setTrangThai("CHO_XU_LY");
        return repo.save(baoHanh);
    }

    public List<BaoHanh> getAllRequests() {
        return repo.findAll();
    }

    public List<BaoHanh> getPendingRequests() {
        return repo.findByTrangThai("CHO_XU_LY");
    }

    public List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung) {
        return repo.findByMaNguoiDung(maNguoiDung);
    }

    // CẬP NHẬT TRẠNG THÁI VÀ GỬI EMAIL THÔNG BÁO LỊCH HẸN
    public BaoHanh updateStatus(Integer id, String trangThai, LocalDateTime thoiGianHen) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu bảo hành"));

        bh.setTrangThai(trangThai.trim().toUpperCase());
        
        if (thoiGianHen != null) {
            bh.setThoiGianHen(thoiGianHen);
        }

        BaoHanh updated = repo.save(bh);

        // NẾU TRẠNG THÁI LÀ "DA_TIEP_NHAN", GỬI EMAIL CHO KHÁCH
        if ("DA_TIEP_NHAN".equals(updated.getTrangThai()) && updated.getMaNguoiDung() != null) {
            sendEmailToCustomer(updated);
        }

        return updated;
    }

    private void sendEmailToCustomer(BaoHanh bh) {
        try {
            NguoiDung user = nguoiDungRepository.findById(bh.getMaNguoiDung()).orElse(null);
            if (user == null || user.getEmail() == null) return;

            String formattedTime = bh.getThoiGianHen() != null 
                ? bh.getThoiGianHen().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) 
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
                "Quý khách vui lòng mang theo đồng hồ và thông tin mã yêu cầu #" + bh.getMaBaoHanh() + " khi đến cửa hàng để được hỗ trợ tốt nhất.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Velora Clock."
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email thất bại: " + e.getMessage());
        }
    }

    public BaoHanh cancelRequest(Integer id) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
        if (!bh.getTrangThai().equals("CHO_XU_LY")) {
            throw new RuntimeException("Không thể hủy yêu cầu này.");
        }
        bh.setTrangThai("DA_HUY");
        return repo.save(bh);
    }
}