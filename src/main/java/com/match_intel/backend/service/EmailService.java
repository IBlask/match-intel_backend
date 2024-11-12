package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public void sendEmail(String emailRecipient, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(emailProperties.getUsername(), "Match Intel");
        } catch (UnsupportedEncodingException e) {
            log.warn("Encoding issue when setting email sender, defaulting to username", e);
            helper.setFrom(emailProperties.getUsername());
        }

        helper.setTo(emailRecipient);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }





    public void sendEmailConfirmationSafely(User user, EmailConfirmationToken token) throws MessagingException {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("confirmationToken", token.getToken());

        String content = templateEngine.process("/emails/emailConfirmationTemplate", context);

        sendEmail(user.getEmail(), "Email confirmation", content);
    }

    @Async
    public void sendEmailConfirmationAsync(User user, EmailConfirmationToken token) {
        try {
            sendEmailConfirmationSafely(user, token);
        } catch (Exception exception) {
            log.error(
                    String.format(
                            "Error while sending email confirmation to user %s with token %s: %s",
                            user.getId() != null ? user.getId() : "UNKNOWN",
                            token.getIdAsString() != null ? token.getIdAsString() : "UNKNOWN",
                            exception.getMessage()
                    ),
                    exception
            );
        }
    }
}