package pl.ttsw.GameRev.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.model.PasswordResetToken;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.PasswordResetTokenRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private WebsiteUserRepository websiteUserRepository;

    public String createPasswordResetToken(String email) {
        WebsiteUser user = websiteUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plus(Duration.ofHours(1)));
        passwordResetTokenRepository.save(passwordResetToken);

        return "http://localhost:8080/password-reset/confirm?token="+token;
    }

    public PasswordResetToken validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This token is expired");
        }
        return passwordResetToken;
    }
}
