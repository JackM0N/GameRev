package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.WebsiteUserService;
import pl.ttsw.GameRev.security.IAuthenticationFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebsiteUserServiceTest {

    @Mock
    private WebsiteUserRepository websiteUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IAuthenticationFacade authenticationFacade;

    @InjectMocks
    private WebsiteUserService websiteUserService;

    @Value("${profile.pics.directory}")
    private String profilePicsDirectory = "../Pictures/profile_pics/";
    private final String username = "testuser";

    private WebsiteUserDTO userDTO_old;
    private WebsiteUserDTO userDTO_new;
    private WebsiteUser user_new;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        userDTO_old = new WebsiteUserDTO();
        userDTO_old.setUsername(username);
        userDTO_old.setPassword("encodedPassword");
        userDTO_old.setNickname(username);
        userDTO_old.setEmail("email@test.com");

        userDTO_new = new WebsiteUserDTO();
        userDTO_new.setUsername(username + "2");
        userDTO_new.setPassword("encodedPassword");
        userDTO_new.setNickname(username + "2");
        userDTO_new.setEmail("email2@test.com");
        userDTO_new.setProfilepic("oldPic.jpg");

        user_new = new WebsiteUser();
        user_new.setUsername(username + "2");
        user_new.setPassword("encodedPassword");
        user_new.setNickname(username + "2");
        user_new.setEmail("email2@test.com");
        user_new.setProfilepic("oldPic.jpg");

        // lenient() - so mockito stops throwing UnnecessaryStubbingException for tests where it is not needed
        // it IS needed for some other tests here
        authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(username + "2");
        lenient().when(authenticationFacade.getAuthentication()).thenReturn(authentication);
    }

    @Test
    public void testFindByUsername() {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        WebsiteUserDTO result = websiteUserService.findByNickname(username + "2");
        assertEquals(username + "2", result.getUsername());
    }

    @Test
    public void testUpdateUserProfile_Success() throws BadRequestException {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(websiteUserRepository.save(any(WebsiteUser.class))).thenReturn(user_new);

        UpdateWebsiteUserDTO request = new UpdateWebsiteUserDTO();
        request.setCurrentPassword("currentPassword");
        request.setEmail("newEmail@test.com");

        WebsiteUserDTO result = websiteUserService.updateUserProfile(username + "2", request);
        assertEquals("newEmail@test.com", result.getEmail());
    }

    @Test
    public void testUpdateUserProfile_PasswordMismatch() {
        when(websiteUserRepository.findByUsername(username + "2")).thenReturn(user_new);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        UpdateWebsiteUserDTO request = new UpdateWebsiteUserDTO();
        request.setCurrentPassword("wrongPassword");

        assertThrows(BadCredentialsException.class, () -> {
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
        Files.createDirectories(Paths.get(profilePicsDirectory));
        websiteUserService.uploadProfilePicture(profilePictureDTO);

        Path newFilePath = Paths.get(profilePicsDirectory, "testuser2_newPic.jpg");

        assertTrue(Files.exists(newFilePath));
        Files.deleteIfExists(newFilePath);
    }

    @Test
    public void testGetProfilePicture_Success() throws IOException {
        user_new.setProfilepic(profilePicsDirectory + "/testuser2_profilePic.jpg");
        when(websiteUserRepository.findByNickname("nickname2")).thenReturn(user_new);

        Path filePath = Paths.get(profilePicsDirectory, "testuser2_profilePic.jpg");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "Test Image Content".getBytes());

        byte[] result = websiteUserService.getProfilePicture(username + "2");

        assertArrayEquals("Test Image Content".getBytes(), result);

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetProfilePicture_NotFound() {
        when(websiteUserRepository.findByNickname("nickname3")).thenReturn(user_new);

        assertThrows(IOException.class, () -> {
            websiteUserService.getProfilePicture("nickname3");
        });
    }
}
