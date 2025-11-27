package com.library.libraryService.common.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("sendOtpEmail harus membentuk SimpleMailMessage dengan field yang benar dan memanggil mailSender.send()")
    void sendOtpEmail_success() {
        String to = "user@example.com";
        String otp = "123456";

        emailService.sendOtpEmail(to, otp);

        // Tangkap email yang dikirim
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getFrom()).isEqualTo("fidasLibrary@testmail.com");
        assertThat(msg.getTo()).containsExactly(to);
        assertThat(msg.getSubject()).isEqualTo("Fidas Library OTP Code");
        assertThat(msg.getText()).contains(otp);
        assertThat(msg.getText()).contains("Valid selama 5 menit.");
    }

    @Test
    @DisplayName("sendOtpEmail send email error")
    void sendOtpEmail_handlesException() {
        String to = "fida@test.com";
        String otp = "123456";

        doThrow(new RuntimeException("SMTP DOWN"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendOtpEmail(to, otp));

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
