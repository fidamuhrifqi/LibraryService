package com.library.libraryService.common.mail;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
}