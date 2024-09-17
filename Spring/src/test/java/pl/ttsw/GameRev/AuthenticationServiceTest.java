package pl.ttsw.GameRev;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.AuthenticationResponse;
import pl.ttsw.GameRev.security.JWTService;
import pl.ttsw.GameRev.service.AuthenticationService;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockBean
    private WebsiteUserRepository websiteUserRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private RoleRepository roleRepository;

    @Test
    public void testRegister() {
        WebsiteUser request = new WebsiteUser();
        request.setUsername("testuser2");
        request.setPassword("password");
        request.setEmail("testuser2@example.com");
        request.setNickname("testnickname");

        WebsiteUser savedUser = new WebsiteUser();
        savedUser.setUsername("testuser2");

        Role userRole = new Role();
        userRole.setRoleName("User");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName("User")).thenReturn(Optional.of(userRole));
        when(websiteUserRepository.save(any(WebsiteUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.register(request);

        assertEquals("jwtToken", response.getToken());
        verify(websiteUserRepository).save(any(WebsiteUser.class));
    }

    @Test
    public void testAuthenticate() {
        WebsiteUser request = new WebsiteUser();
        request.setUsername("testuser2");
        request.setPassword("password");

        WebsiteUser user = new WebsiteUser();
        user.setUsername("testuser2");

        when(websiteUserRepository.findByUsernameOrEmail("testuser2", "testuser2")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
