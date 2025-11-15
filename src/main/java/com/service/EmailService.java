package com.service;

import com.entity.NhanVien;
import io.github.cdimascio.dotenv.Dotenv;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;

public class EmailService {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String HOST = dotenv.get("SMTP_HOST", "smtp.gmail.com");
    private static final String USERNAME = dotenv.get("SMTP_USERNAME");
    private static final String PASSWORD = dotenv.get("SMTP_PASSWORD");

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static String generateActivationCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // tránh 0,1,O,I
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean sendActivationEmail(NhanVien nv, String activationCode) {
        if (nv == null || nv.getEmail() == null || nv.getEmail().isBlank()) {
            System.err.println("NhanVien hoặc email null");
            return false;
        }
        if (USERNAME == null || PASSWORD == null) {
            System.err.println("Chưa cấu hình SMTP_USERNAME / SMTP_PASSWORD trong .env");
            return false;
        }

        Session session = createSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(nv.getEmail()));
            message.setSubject("Kích hoạt tài khoản nhân viên");

            String content = "Xin chào " + nv.getTenNV() + ",\n\n" +
                    "Tài khoản của bạn tại KVStore đã được tạo.\n" +
                    "Mã nhân viên: " + nv.getMaNV() + "\n" +
                    "Mật khẩu kích hoạt: " + activationCode + "\n\n" +
                    "Vui lòng đăng nhập lần đầu bằng mật khẩu kích hoạt này và đổi sang mật khẩu mới.\n" +
                    "Trân trọng.";

            message.setText(content);
            Transport.send(message);
            System.out.println("Gửi mail kích hoạt thành công cho " + nv.getEmail());
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}

