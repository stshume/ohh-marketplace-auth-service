package za.co.ooh.marketplace.auth.domains.email.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class EmailService {

    //@Value("spring.mail.enabled")
    private final boolean mailEnabled = false;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String email, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setFrom("noreply@ooh-marketplace.co.za");
        mail.setReplyTo("noreply@ooh-marketplace.co.za");
        mail.setSubject(subject);
        mail.setSentDate(new Date());
        mail.setText(body);

        log.info("Send email to {}", mail.toString());
        if(mailEnabled) {
            mailSender.send(mail);
        }
    }
}
