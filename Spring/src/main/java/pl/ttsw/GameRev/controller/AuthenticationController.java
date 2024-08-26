package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.security.AuthenticationResponse;
import pl.ttsw.GameRev.security.JWTService;
import pl.ttsw.GameRev.service.AuthenticationService;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private AuthenticationService authenticationService;
    private JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody WebsiteUser request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody WebsiteUser request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam("token") String token) {
        try {
            String refreshedToken = jwtService.refreshToken(token);
            return ResponseEntity.ok(new AuthenticationResponse(refreshedToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token cannot be refreshed, please log in again.");
        }
    }
}
