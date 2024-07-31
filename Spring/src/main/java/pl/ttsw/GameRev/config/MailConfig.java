package pl.ttsw.GameRev.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    public GreenMail greenMail() {
        ServerSetup setup = new ServerSetup(3025, null, "smtp");
        GreenMail greenMail = new GreenMail(setup);
        greenMail.setUser("gamerev@example.com", "password123");
        greenMail.start();
        return greenMail;
    }
}
