package pl.ttsw.GameRev.controller;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EmailController {
    private GreenMail greenMail;

    public EmailController(GreenMail greenMail) {
        this.greenMail = greenMail;
    }

    @GetMapping("/emails")
    public List<String> getEmails() throws MessagingException, IOException {
        List<String> emails = new ArrayList<>();
        Message[] messages = greenMail.getReceivedMessages();
        for (Message message : messages) {
            emails.add(GreenMailUtil.getBody(message));
        }
        return emails;
    }

}
