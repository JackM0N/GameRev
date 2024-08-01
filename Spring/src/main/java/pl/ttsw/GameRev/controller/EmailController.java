package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EmailController {
    private GreenMail greenMail;

    public EmailController(GreenMail greenMail) {
        this.greenMail = greenMail;
    }

    @GetMapping("/emails")
    public List<String> getEmails() throws MessagingException, JsonProcessingException {
        MimeMessage[] messages = greenMail.getReceivedMessages();
        List<String> emailContents = new ArrayList<>();
        for (MimeMessage message : messages) {
            String content = GreenMailUtil.getBody(message);
            Document doc = Jsoup.parse(content);
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            String htmlContent = doc.html();
            emailContents.add(htmlContent);
        }
        return emailContents;
    }
}
