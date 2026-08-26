package com.tracker.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendSimpleMessage_ShouldSendEmail() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Text";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendSimpleMessage(to, subject, text);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}