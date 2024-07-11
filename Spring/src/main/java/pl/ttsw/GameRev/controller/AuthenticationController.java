package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.security.AuthenticationResponse;
import pl.ttsw.GameRev.service.AuthenticationService;
import pl.ttsw.GameRev.service.WebsiteUserService;

@RestController
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private WebsiteUserService websiteUserService;

    public AuthenticationController(AuthenticationService authenticationService, WebsiteUserService websiteUserService) {
        this.authenticationService = authenticationService;
        this.websiteUserService = websiteUserService;
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
