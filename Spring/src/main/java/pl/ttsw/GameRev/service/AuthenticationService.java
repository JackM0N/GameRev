package pl.ttsw.GameRev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.AuthenticationResponse;
import pl.ttsw.GameRev.security.JWTService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public AuthenticationResponse register(WebsiteUser request) {
        WebsiteUser user = new WebsiteUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setJoinDate(LocalDate.now());
        user.setLastActionDate(LocalDateTime.now());
        user.setDescription(null);
        user.setIsBanned(false);
        user.setIsDeleted(false);
        user.setProfilepic(null);

        Role role = roleRepository.findByRoleName("User")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Collections.singletonList(role));
        user = websiteUserRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(WebsiteUser request) {
        WebsiteUser user;
        String login = request.getUsername() != null ? request.getUsername() : request.getEmail();

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                login,
                request.getPassword()
            )
        );

        user = websiteUserRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        String token = jwtService.generateToken(user);

        return new AuthenticationResponse(token);
    }
}
