package com.novalearn.novalearn.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("NovaLearn - Password Reset Request");

        message.setText("Hello,\n\n"
                + "You requested a password reset for your NovaLearn account.\n"
                + "Here is your 6-digit password reset code:\n\n"
                + "   " + token + "   \n\n"
                + "Please enter this code on the reset password page.\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nThe NovaLearn Team");

        mailSender.send(message);
    }
}
