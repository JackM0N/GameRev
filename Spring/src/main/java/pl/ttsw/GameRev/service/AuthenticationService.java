package pl.ttsw.GameRev.service;

import org.springframework.security.authentication.AuthenticationManager;
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
public class AuthenticationService {
    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final WebsiteUserService websiteUserService;

    public AuthenticationService(WebsiteUserRepository websiteUserRepository, PasswordEncoder passwordEncoder, JWTService jwtService, AuthenticationManager authenticationManager,
                                 RoleRepository roleRepository, WebsiteUserService websiteUserService) {
        this.websiteUserRepository = websiteUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.websiteUserService = websiteUserService;
    }

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

        Role role = roleRepository.findByRoleName("USER");
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

        user = websiteUserRepository.findByUsernameOrEmail(login, login);
        String token = jwtService.generateToken(user);
        websiteUserService.updateCurrentToken(user, token);

        return new AuthenticationResponse(token);
    }
}
