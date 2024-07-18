package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDto;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebsiteUserServiceTest {

    @Mock
    private WebsiteUserRepository websiteUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private WebsiteUserService websiteUserService;

    @Value("${profile.pics.directory}") // this is not set in unit testing..?
    private String profilePicsDirectory = "src/main/resources/static/profile_pics/";
    private final String username = "testuser";

    private WebsiteUser user_old;
    private WebsiteUser user_new;

    @BeforeEach
    public void setUp() {
        user_old = new WebsiteUser();
        user_old.setUsername(username);
        user_old.setPassword("encodedPassword");
        user_old.setNickname("nickname");
        user_old.setEmail("email@test.com");

        user_new = new WebsiteUser();
        user_new.setUsername(username + "2");
        user_new.setPassword("encodedPassword");
        user_new.setNickname("nickname2");
        user_new.setEmail("email2@test.com");
        user_new.setProfilepic("oldPic.jpg");
    }

    @Test
    public void testFindByUsername() {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);

        WebsiteUser result = websiteUserService.findByUsername(username + "2");

        assertEquals(username + "2", result.getUsername());
    }

    @Test
    public void testUpdateUserProfile_Success() throws BadRequestException {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(websiteUserRepository.save(any(WebsiteUser.class))).thenReturn(user_new);

        UpdateWebsiteUserDto request = new UpdateWebsiteUserDto();
        request.setCurrentPassword("currentPassword");
        request.setEmail("newEmail@test.com");

        WebsiteUser result = websiteUserService.updateUserProfile(username + "2", request);
        assertEquals("newEmail@test.com", result.getEmail());
    }

    @Test
    public void testUpdateUserProfile_PasswordMismatch() {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        UpdateWebsiteUserDto request = new UpdateWebsiteUserDto();
        request.setCurrentPassword("wrongPassword");

        assertThrows(BadRequestException.class, () -> {
            websiteUserService.updateUserProfile(username + "2", request);
        });
    }

    @Test
    public void testUploadProfilePicture_Success() throws IOException {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);

        ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO();
        profilePictureDTO.setUsername(user_new.getUsername());

        MockMultipartFile file = new MockMultipartFile("newPic.jpg", "newPic.jpg", "image/jpeg", "Test Image Content".getBytes());
        profilePictureDTO.setProfilePicture(file);
        websiteUserService.uploadProfilePicture(profilePictureDTO);

        Path newFilePath = Paths.get(profilePicsDirectory, "testuser2_newPic.jpg");

        assertTrue(Files.exists(newFilePath));
        Files.deleteIfExists(newFilePath);
    }

    @Test
    public void testGetProfilePicture_Success() throws IOException {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        user_new.setProfilepic(profilePicsDirectory + "/testuser2_profilePic.jpg");

        Path filePath = Paths.get(profilePicsDirectory, "testuser2_profilePic.jpg");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "Test Image Content".getBytes());

        byte[] result = websiteUserService.getProfilePicture(username + "2");

        assertArrayEquals("Test Image Content".getBytes(), result);

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetProfilePicture_NotFound() {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);

        assertThrows(IOException.class, () -> {
            websiteUserService.getProfilePicture(username + "2");
        });
    }
}
