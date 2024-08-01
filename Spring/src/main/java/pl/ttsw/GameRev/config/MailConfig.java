package pl.ttsw.GameRev.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MailConfig {

    @Profile("!test")
    @Bean
    public GreenMail greenMail() {
        ServerSetup setup = new ServerSetup(3025, null, "smtp");
        GreenMail greenMail = new GreenMail(setup);
        greenMail.setUser("gamerev@example.com", "password123");
        greenMail.start();
        return greenMail;
    }

    @Profile("test")
    @Bean
    public GreenMail greenMailDisabled() {
        GreenMail greenMail = new GreenMail();
        return greenMail;
    }
}
