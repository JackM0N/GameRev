package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.model.PasswordResetToken;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.PasswordResetTokenRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private WebsiteUserRepository websiteUserRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, WebsiteUserRepository websiteUserRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.websiteUserRepository = websiteUserRepository;
    }

    public String createPasswordResetToken(String email) {
        System.out.println(email);
        WebsiteUser user = websiteUserRepository.findByEmail(email);
        System.out.println(user);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plus(Duration.ofHours(1)));
        System.out.println(passwordResetToken);
        passwordResetTokenRepository.save(passwordResetToken);

        return "http://localhost:8080/password-reset/confirm?token="+token;
    }

    public PasswordResetToken validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null) {
            throw new RuntimeException("Token not found");
        }

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This token is expired");
        }
        return passwordResetToken;
    }

}
