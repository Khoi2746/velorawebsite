package com.velora.website.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailLichHen {

    private final JavaMailSender mailSender;

    public EmailLichHen(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 1. Gửi email HTML cho Admin khi có lịch hẹn mới
    @Async
    public void sendNewBookingToAdmin(String adminEmail, String tenKhachHang, LocalDate ngayHen, String thoiGian, String soDienThoai, String tenSanPham) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(adminEmail);
            helper.setSubject("Velora Clock - Có lịch hẹn xem đồng hồ mới");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: #d1aa68;'>LỊCH HẸN MỚI</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>Ban Quản Trị</b>,</p>"
                    + "<p style='font-size: 15px;'>Hệ thống vừa ghi nhận một yêu cầu đặt lịch hẹn mới từ khách hàng:</p>"
                    + "<div style='background-color: #170d08; padding: 15px 30px; margin: 20px auto; border: 1px dashed #d1aa68; display: inline-block; font-size: 15px; color: #ffffff; text-align: left; line-height: 1.8;'>"
                    + "<b>Khách hàng:</b> " + tenKhachHang + "<br/>"
                    + "<b>Số điện thoại:</b> " + soDienThoai + "<br/>"
                    + "<b>Sản phẩm:</b> " + (tenSanPham != null ? tenSanPham : "Không chọn cụ thể") + "<br/>"
                    + "<b>Thời gian:</b> " + thoiGian + " ngày " + ngayHen
                    + "</div>"
                    + "<p style='font-size: 14px; color: #aaa;'>Vui lòng truy cập hệ thống quản trị để kiểm tra và xác nhận.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // 2. Gửi email HTML cho Khách hàng khi Admin Xác nhận
    @Async
    public void sendStatusToCustomer(String customerEmail, Integer trangThai, LocalDate ngayHen, String thoiGian, String tenKhachHang) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(customerEmail);
            helper.setSubject("Velora Clock - Thông báo trạng thái lịch hẹn");

            boolean isConfirmed = (trangThai != null && trangThai == 1);
            String title = isConfirmed ? "XÁC NHẬN THÀNH CÔNG" : "ĐÃ HỦY LỊCH HẸN";
            String titleColor = isConfirmed ? "#4CAF50" : "#F44336"; 
            
            String statusText = isConfirmed 
                ? "đã được <b>XÁC NHẬN</b> thành công. Velora Clock rất hân hạnh được đón tiếp quý khách!" 
                : "đã bị <b>HỦY</b>.";

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: " + titleColor + ";'>" + title + "</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + tenKhachHang + "</b>,</p>"
                    + "<p style='font-size: 15px; line-height: 1.6;'>Lịch hẹn xem đồng hồ của quý khách vào lúc <b>" + thoiGian + "</b> ngày <b>" + ngayHen + "</b> " + statusText + "</p>"
                    + "<div style='background-color: #170d08; padding: 15px 30px; margin: 20px auto; border: 1px dashed #d1aa68; display: inline-block; font-size: 16px; font-weight: bold; color: #d1aa68; letter-spacing: 1px;'>"
                    + "CẢM ƠN QUÝ KHÁCH ĐÃ QUAN TÂM!"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #aaa; margin-top: 10px;'>Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ ngay với bộ phận CSKH của chúng tôi.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

  // 3. Gửi email HTML HỦY KÈM LÝ DO đồng bộ giao diện Velora
    @Async
    public void sendCancelReasonToCustomer(String email, String tenKhach, LocalDate ngayHen, String thoiGian, String lyDoHuy) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Velora Clock - Thông báo hủy lịch hẹn trải nghiệm");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: #F44336;'>THÔNG BÁO HỦY LỊCH HẸN</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + tenKhach + "</b>,</p>"
                    + "<p style='font-size: 15px; line-height: 1.6;'>Velora rất tiếc phải thông báo rằng lịch hẹn xem đồng hồ vào lúc <b>" + thoiGian + "</b> ngày <b>" + ngayHen + "</b> của Quý khách đã bị hủy.</p>"
                    + "<div style='background-color: #170d08; padding: 15px 25px; margin: 20px auto; border: 1px dashed #F44336; font-size: 14px; color: #ffffff; text-align: left; line-height: 1.8; border-radius: 6px;'>"
                    + "<b style='color: #F44336;'>Lý do hủy:</b> " + (lyDoHuy != null && !lyDoHuy.trim().isEmpty() ? lyDoHuy : "Chưa cập nhật cụ thể")
                    + "</div>"
                    + "<p style='font-size: 14px; color: #aaa; margin-top: 15px;'>Nếu có bất kỳ thắc mắc nào, Quý khách vui lòng liên hệ hotline hoặc phản hồi lại email này.</p>"
                    + "<div style='background-color: #170d08; padding: 12px 25px; margin: 20px auto 0; border: 1px solid #d1aa68; display: inline-block; font-size: 14px; font-weight: bold; color: #d1aa68; letter-spacing: 1px;'>"
                    + "VELORA CLOCK - SANG TRỌNG & ĐẲNG CẤP"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}