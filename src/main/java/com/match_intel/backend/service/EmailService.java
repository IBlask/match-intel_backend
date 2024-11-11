package com.match_intel.backend.service;

import com.match_intel.backend.entity.User;
import com.match_intel.backend.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service
public class EmailService {

    private final JavaMailSenderImpl mailSender;
    private final EmailProperties emailProperties;
    private final TemplateEngine templateEngine;


    @Autowired
    public EmailService(EmailProperties emailProperties, TemplateEngine templateEngine) {
        this.mailSender = new JavaMailSenderImpl();
        this.emailProperties = emailProperties;
        this.templateEngine = templateEngine;

        mailSender.setHost(emailProperties.getSmtpHost());
        mailSender.setPort(emailProperties.getSmtpPort());
        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }


    @Async
    public void sendEmail(String email, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailProperties.getUsername(), "Match Intel");
        helper.setTo(email);

        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }





    @Async
    public void sendEmailConfirmation(User user, String token) throws MessagingException, UnsupportedEncodingException {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("confirmationToken", token);

        String content = templateEngine.process("/emails/emailConfirmationTemplate", context);

        sendEmail(user.getEmail(), "Email confirmation", content);
    }
}