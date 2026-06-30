package com.velora.website.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            System.err.println("LỖI: JavaMailSender chưa được khởi tạo!");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Đã gửi email thành công tới: " + to);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}