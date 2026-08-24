package com.velora.website.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // SỬ DỤNG CONSTRUCTOR INJECTION
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        // KIỂM TRA NULL AN TOÀN
        if (mailSender == null) {
            System.err.println("❌ LỖI: JavaMailSender chưa được khởi tạo!");
            return;
        }

        if (to == null || to.trim().isEmpty()) {
            System.err.println("⚠️ BỎ QUA GỬI MAIL: Địa chỉ Email nhận bị trống.");
            return;
        }

        // Chạy luồng ngầm (Async) để gửi mail không làm đơ/treo giao dịch chính
        new Thread(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                // BẮT BUỘC: Khai báo đúng Email gửi trùng với spring.mail.username
                helper.setFrom("veloraclock@gmail.com", "VELORA CLOCK");
                helper.setTo(to.trim());
                helper.setSubject(subject);
                
                // 🔥 ĐỔI SANG HTML: Bật true để Gmail render giao diện thiệp Card Luxury
                helper.setText(body, true);

                mailSender.send(message);
                System.out.println("✅ Đã gửi email thành công tới: " + to);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi gửi mail tới " + to + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}