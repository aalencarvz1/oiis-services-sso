package com.oiis.services.sso.services.mail;

import com.oiis.services.sso.controllers.mail.MailController;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailController mailController;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void sendEmail(String to, String subject, String text, String html) throws Exception {
        if (mailController.isValidEmail(to)) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html);
            helper.setFrom(mailFrom); // configure conforme seu SMTP
            mailSender.send(message);
        } else {
            throw new Exception("invalid mail");
        }
    }
}
