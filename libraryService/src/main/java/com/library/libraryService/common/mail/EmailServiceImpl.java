package com.library.libraryService.common.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("fidasLibrary@testmail.com");
            message.setTo(to);
            message.setSubject("Fidas Library OTP Code");
            message.setText("Kode OTP kamu adalah: " + otp + "\nValid selama 5 menit.");

            System.out.println("OTP EMAIL SENT TO " + to);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("GAGAL KIRIM EMAIL " + e.getMessage());
        }
    }
}
