package pl.ttsw.GameRev.controller;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final GreenMail greenMail;

    @GetMapping("/emails")
    public String getEmails() {
        MimeMessage[] messages = greenMail.getReceivedMessages();
        StringBuilder emailContents = new StringBuilder();
        for (MimeMessage message : messages) {
            String content = GreenMailUtil.getBody(message);
            Document doc = Jsoup.parse(content);
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            String htmlContent = doc.html();
            emailContents.append("<div class='email'>").append(htmlContent).append("</div>");
        }
        return emailContents.toString();
    }
}
