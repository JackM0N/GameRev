package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.controller.AuthenticationController;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.AuthenticationService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WebsiteUserServiceIntegrationTest {

    private final String username = "testuser";
    @Autowired
    private WebsiteUserRepository websiteUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WebsiteUserService websiteUserService;
    @Value("${profile.pics.directory}")
    private String profilePicsDirectory;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp() {
        tearDown();
    }

    @AfterEach
    public void tearDown() {
        try {
            Files.deleteIfExists(Path.of(profilePicsDirectory + "/" + username + "2_newPic.jpg"));
            Files.deleteIfExists(Path.of(profilePicsDirectory + "/" + username + "2_profilePic.jpg"));
            Optional<WebsiteUser> user = websiteUserRepository.findByUsername(username + "2");
            if (user.isPresent()) {
                websiteUserRepository.delete(user.get());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private WebsiteUser copyForTesting() {
        WebsiteUser user_old = websiteUserRepository.findByUsername(username).get();
        WebsiteUser user_new = new WebsiteUser();
        user_new.setUsername(username + "2");
        user_new.setPassword(user_old.getPassword());
        user_new.setNickname(user_old.getNickname() + "2");
        user_new.setEmail(user_old.getEmail() + "2");
        user_new.setJoinDate(user_old.getJoinDate());
        user_new.setLastActionDate(user_old.getLastActionDate());
        return user_new;
    }

    @Test
    public void testFindByNickname() {
        WebsiteUser user = copyForTesting();
        websiteUserRepository.save(user);

        WebsiteUserDTO result = websiteUserService.findByNickname("testuser2");

        assertEquals("testuser2", result.getNickname());
    }

    @Test
    @WithMockUser(username = "testuser2")
    public void testUpdateUserProfile_Success() throws BadRequestException {
        WebsiteUser user = copyForTesting();
        user.setPassword(passwordEncoder.encode("currentPassword"));
        websiteUserRepository.save(user);

        UpdateWebsiteUserDTO request = new UpdateWebsiteUserDTO();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");
        request.setEmail("newEmail@test.com");

        WebsiteUserDTO result = websiteUserService.updateUserProfile("testuser2", request);

        assertEquals("newEmail@test.com", result.getEmail());
        assertTrue(passwordEncoder.matches("newPassword", result.getPassword()));
    }

    @Test
    @WithMockUser(username = "testuser2")
    public void testUpdateUserProfile_PasswordMismatch() {
        WebsiteUser user = copyForTesting();
        user.setPassword(passwordEncoder.encode("currentPassword"));
        websiteUserRepository.save(user);

        UpdateWebsiteUserDTO request = new UpdateWebsiteUserDTO();
        request.setCurrentPassword("wrongPassword");

        assertThrows(BadCredentialsException.class, () -> {
            websiteUserService.updateUserProfile("testuser2", request);
        });
    }

    @Test
    @WithMockUser(username = "testuser2")
    public void testUploadProfilePicture_Success() throws IOException {
        WebsiteUser user = copyForTesting();
        user.setProfilepic("oldPic.jpg");
        websiteUserRepository.save(user);

        ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO();
        profilePictureDTO.setUsername(user.getUsername());

        MultipartFile file = new MockMultipartFile("newPic.jpg", "newPic.jpg", "image/jpeg", "Test Image Content".getBytes());
        profilePictureDTO.setProfilePicture(file);

        websiteUserService.uploadProfilePicture(profilePictureDTO);

        Path newFilePath = Paths.get(profilePicsDirectory, "testuser2_newPic.jpg");

        assertTrue(Files.exists(newFilePath));
        Files.deleteIfExists(newFilePath);
    }

    @Test
    public void testGetProfilePicture_Success() throws IOException {
        WebsiteUser user = copyForTesting();
        user.setProfilepic(profilePicsDirectory + "/testuser2_profilePic.jpg");
        websiteUserRepository.save(user);

        Path filePath = Paths.get(profilePicsDirectory, "testuser2_profilePic.jpg");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "Test Image Content".getBytes());

        byte[] result = websiteUserService.getProfilePicture("testuser2");

        assertArrayEquals("Test Image Content".getBytes(), result);

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetProfilePicture_NotFound() {
        WebsiteUser user = copyForTesting();
        websiteUserRepository.save(user);

        assertThrows(IOException.class, () -> {
            websiteUserService.getProfilePicture("testuser2");
        });
    }
}
