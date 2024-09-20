package pl.ttsw.GameRev;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        Optional<WebsiteUser> user = websiteUserRepository.findByUsername("testuser2");
        if (user.isPresent()) {
            websiteUserRepository.delete(user.get());
        }
    }

    @AfterEach
    public void tearDown() {
        Optional<WebsiteUser> user = websiteUserRepository.findByUsername("testuser2");
        if (user.isPresent()) {
            websiteUserRepository.delete(user.get());
        }
    }

    @Test
    public void testRegister() throws Exception {
        String userJson = "{ \"username\": \"testuser2\", \"password\": \"password\", \"email\": \"testuser2@example.com\", \"nickname\": \"testnickname2\" }";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testAuthenticate() throws Exception {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testuser2");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("testuser2@example.com");
        user.setNickname("testnickname2");
        user.setJoinDate(LocalDate.now());
        user.setLastActionDate(LocalDateTime.now());
        Role role = roleRepository.findByRoleName("User").get();
        user.setRoles(Collections.singletonList(role));
        websiteUserRepository.save(user);

        String loginJson = "{ \"username\": \"testuser2\", \"password\": \"password\" }";

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
