package pl.ttsw.GameRev.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);

        String htmlTemplate = loadEmailTemplate();
        htmlTemplate = htmlTemplate.replace("{link}", text);
        mimeMessageHelper.setText(htmlTemplate,true);

        mailSender.send(mimeMessage);
    }

    private String loadEmailTemplate() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("templates/email_template.html");
        return StreamUtils.copyToString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
    }
}
