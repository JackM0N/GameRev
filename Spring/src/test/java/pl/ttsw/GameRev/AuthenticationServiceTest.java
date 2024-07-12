package pl.ttsw.GameRev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import pl.ttsw.GameRev.model.*;
import pl.ttsw.GameRev.repository.*;
import pl.ttsw.GameRev.security.*;
import pl.ttsw.GameRev.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        // Arrange
        WebsiteUser request = new WebsiteUser();
        request.setUsername("testuser2");
        request.setPassword("password");
        request.setEmail("testuser2@example.com");
        request.setNickname("testnickname");

        WebsiteUser savedUser = new WebsiteUser();
        savedUser.setUsername("testuser2");

        Role userRole = new Role();
        userRole.setRoleName("USER");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName("USER")).thenReturn(userRole);
        when(websiteUserRepository.save(any(WebsiteUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.register(request);

        // Assert
        assertEquals("jwtToken", response.getToken());
        verify(websiteUserRepository).save(any(WebsiteUser.class));
    }

    @Test
    public void testAuthenticate() {
        // Arrange
        WebsiteUser request = new WebsiteUser();
        request.setUsername("testuser2");
        request.setPassword("password");

        WebsiteUser user = new WebsiteUser();
        user.setUsername("testuser2");

        when(websiteUserRepository.findByUsernameOrEmail("testuser2", "testuser2")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
