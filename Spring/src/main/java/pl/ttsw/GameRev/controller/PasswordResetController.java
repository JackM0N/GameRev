package pl.ttsw.GameRev.controller;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.model.PasswordResetToken;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.EmailService;
import pl.ttsw.GameRev.service.PasswordResetTokenService;

import java.io.IOException;

@RestController
@RequestMapping("/password-reset")
public class PasswordResetController {
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    public PasswordResetController(PasswordResetTokenService passwordResetTokenService, EmailService emailService, PasswordEncoder passwordEncoder, WebsiteUserRepository websiteUserRepository) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.websiteUserRepository = websiteUserRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) throws MessagingException, IOException {
        String resetUrl = passwordResetTokenService.createPasswordResetToken(email);
        emailService.sendEmail(email,"Password Reset Request", resetUrl);
        return ResponseEntity.ok("If this email is connected to an account, we are going to sent you an email. Please check your inbox");
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestParam String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.validatePasswordResetToken(token);
        if (passwordResetToken == null) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.validatePasswordResetToken(token);
        if (passwordResetToken == null) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }
        WebsiteUser user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        websiteUserRepository.save(user);

        return ResponseEntity.ok("Your password has been reset!");
    }
}
