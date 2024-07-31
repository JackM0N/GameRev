package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.security.AuthenticationResponse;
import pl.ttsw.GameRev.service.AuthenticationService;

@RestController
public class AuthenticationController {
    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody WebsiteUser request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody WebsiteUser request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
