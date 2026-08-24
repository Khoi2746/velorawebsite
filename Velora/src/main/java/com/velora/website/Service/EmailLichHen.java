package com.velora.website.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class EmailLichHen {

    private final JavaMailSender mailSender;

    public EmailLichHen(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 1. Gửi email HTML cho Admin khi có lịch hẹn mới
    @Async
    public void sendNewBookingToAdmin(String adminEmail, String tenKhachHang, LocalDate ngayHen, String thoiGian,
            String soDienThoai, String tenSanPham) {
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
    public void sendStatusToCustomer(String customerEmail, Integer trangThai, LocalDate ngayHen, String thoiGian,
            String tenKhachHang) {
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
                    + "<p style='font-size: 15px; line-height: 1.6;'>Lịch hẹn xem đồng hồ của quý khách vào lúc <b>"
                    + thoiGian + "</b> ngày <b>" + ngayHen + "</b> " + statusText + "</p>"
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

    // 4. Gửi email HTML xác nhận ĐĂNG KÝ LỊCH HẸN THÀNH CÔNG đính kèm file PDF cho Khách hàng
    @Async
    public void sendBookingConfirmationToCustomer(String customerEmail, String tenKhachHang, LocalDate ngayHen,
            String thoiGian, String soDienThoai, String tenSanPham, Integer bookingId) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(customerEmail);
            helper.setSubject("Velora Clock - Đăng ký lịch hẹn tư vấn thành công");

            String htmlContent = "<div style='font-family: Arial; background-color: #26160d; color: #ffffff; max-width: 600px; margin: auto; border: 2px solid #d1aa68; padding: 30px; border-radius: 8px; text-align: center;'>"
                    + "<img src='https://i.postimg.cc/0jRpHvWJ/Velora-Icon.png' alt='Velora Clock' style='max-width: 150px; margin-bottom: 20px;' />"
                    + "<h2 style='color: #4CAF50;'>ĐĂNG KÝ LỊCH HẸN THÀNH CÔNG</h2>"
                    + "<p style='font-size: 16px;'>Kính chào <b>" + tenKhachHang + "</b>,</p>"
                    + "<p style='font-size: 15px; line-height: 1.6;'>Cảm ơn quý khách đã đăng ký lịch hẹn trải nghiệm đồng hồ tại <b>Velora Clock</b>. Yêu cầu của quý khách đã được ghi nhận thành công với thông tin sau:</p>"
                    + "<div style='background-color: #170d08; padding: 15px 25px; margin: 20px auto; border: 1px dashed #d1aa68; font-size: 15px; color: #ffffff; text-align: left; line-height: 1.8; border-radius: 6px;'>"
                    + "<b>Mã lịch hẹn:</b> #" + (bookingId != null ? bookingId : "N/A") + "<br/>"
                    + "<b>Họ và tên:</b> " + tenKhachHang + "<br/>"
                    + "<b>Số điện thoại:</b> " + soDienThoai + "<br/>"
                    + "<b>Sản phẩm quan tâm:</b> " + (tenSanPham != null ? tenSanPham : "Không chọn cụ thể") + "<br/>"
                    + "<b>Ngày hẹn:</b> " + ngayHen + "<br/>"
                    + "<b>Khung giờ:</b> " + thoiGian
                    + "</div>"
                    + "<p style='font-size: 14px; color: #d1aa68; font-weight: bold;'>📎 Tệp tin PDF phiếu xác nhận chi tiết đã được đính kèm trực tiếp trong Email này.</p>"
                    + "<p style='font-size: 14px; color: #aaa; margin-top: 10px;'>Đội ngũ chuyên viên Velora sẽ chủ động liên hệ qua số điện thoại để xác nhận chi tiết trước buổi hẹn.</p>"
                    + "<div style='background-color: #170d08; padding: 12px 25px; margin: 20px auto 0; border: 1px solid #d1aa68; display: inline-block; font-size: 14px; font-weight: bold; color: #d1aa68; letter-spacing: 1px;'>"
                    + "VELORA CLOCK - SANG TRỌNG & ĐẲNG CẤP"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);

            // Tạo và đính kèm file PDF
            byte[] pdfBytes = generateBookingPdf(tenKhachHang, ngayHen, thoiGian, soDienThoai, tenSanPham, bookingId);
            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment("PhieuXacNhanLichHen_Velora.pdf", new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm tạo tệp tin PDF xác nhận đặt lịch hẹn
    private byte[] generateBookingPdf(String tenKhachHang, LocalDate ngayHen, String thoiGian, String soDienThoai, String tenSanPham, Integer bookingId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL);

            Paragraph brand = new Paragraph("VELORA CLOCK", titleFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);

            Paragraph subBrand = new Paragraph("LUXURY & ADVISORY SERVICES", textFont);
            subBrand.setAlignment(Element.ALIGN_CENTER);
            document.add(subBrand);

            document.add(new Paragraph("\n"));

            Paragraph docTitle = new Paragraph("XAC NHAN DANG KY TU VAN LICH HEN", subTitleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(docTitle);

            Paragraph bookingCode = new Paragraph("Ma Lich Hen: #" + (bookingId != null ? bookingId : "VELORA"), headerFont);
            bookingCode.setAlignment(Element.ALIGN_CENTER);
            document.add(bookingCode);

            document.add(new Paragraph("\n----------------------------------------------------------------------------------------------------\n"));

            document.add(new Paragraph("THONG TIN KHACH HANG", headerFont));
            document.add(new Paragraph("Ho va ten: " + (tenKhachHang != null ? tenKhachHang : ""), textFont));
            document.add(new Paragraph("So dien thoai: " + (soDienThoai != null ? soDienThoai : ""), textFont));

            document.add(new Paragraph("\nTHONG TIN LICH HEN", headerFont));
            document.add(new Paragraph("Ngay hen: " + (ngayHen != null ? ngayHen.toString() : ""), textFont));
            document.add(new Paragraph("Khung gio: " + (thoiGian != null ? thoiGian : ""), textFont));
            document.add(new Paragraph("San pham quan tam: " + (tenSanPham != null ? tenSanPham : "Tu van chung"), textFont));

            document.add(new Paragraph("\n----------------------------------------------------------------------------------------------------\n"));
            document.add(new Paragraph("LUU Y DAND CHO KHACH HANG:", headerFont));
            document.add(new Paragraph("1. Vui long den dung gio hen de Velora chuan bi khong gian tu van tot nhat.", textFont));
            document.add(new Paragraph("2. Neu co thay doi lich trinh, vui long lien he hotline Velora truoc 2 gio.", textFont));

            document.add(new Paragraph("\n\nCam on quy khach da lua chon dich vu tu van cao cap cua Velora Services.", textFont));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}