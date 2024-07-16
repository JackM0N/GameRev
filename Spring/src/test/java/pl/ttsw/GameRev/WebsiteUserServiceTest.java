package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class WebsiteUserServiceTest {

    @MockBean
    private WebsiteUserRepository websiteUserRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebsiteUserService websiteUserService;

    @Value("${profile.pics.directory}")
    private String profilePicsDirectory;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        try{
            Files.deleteIfExists(Path.of(profilePicsDirectory + "/testUser_newPic.jpg"));
            Files.deleteIfExists(Path.of(profilePicsDirectory + "/testUser_profilePic.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFindByUsername() {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");
        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);

        WebsiteUser result = websiteUserService.findByUsername("testUser");

        assertEquals("testUser", result.getUsername());
        verify(websiteUserRepository, times(1)).findByUsername("testUser");
    }

    @Test
    public void testUpdateUserProfile_Success() throws BadRequestException {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        UpdateWebsiteUserDto request = new UpdateWebsiteUserDto();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");
        request.setEmail("newEmail@test.com");

        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(websiteUserRepository.save(any(WebsiteUser.class))).thenReturn(user);

        WebsiteUser result = websiteUserService.updateUserProfile("testUser", request);

        assertEquals("newEmail@test.com", result.getEmail());
        assertEquals("newEncodedPassword", result.getPassword());
        verify(websiteUserRepository, times(1)).findByUsername("testUser");
        verify(passwordEncoder, times(1)).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(websiteUserRepository, times(1)).save(any(WebsiteUser.class));
    }

    @Test
    public void testUpdateUserProfile_PasswordMismatch() {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        UpdateWebsiteUserDto request = new UpdateWebsiteUserDto();
        request.setCurrentPassword("wrongPassword");

        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> {
            websiteUserService.updateUserProfile("testUser", request);
        });

        verify(websiteUserRepository, times(1)).findByUsername("testUser");
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
        verify(websiteUserRepository, times(0)).save(any(WebsiteUser.class));
    }

    @Test
    public void testUploadProfilePicture_Success() throws IOException {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");
        user.setProfilepic("oldPic.jpg");

        ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO();
        profilePictureDTO.setUsername("testUser");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("newPic.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Test Image Content".getBytes()));

        profilePictureDTO.setProfilePicture(file);

        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);

        websiteUserService.uploadProfilePicture(profilePictureDTO);

        Path newFilePath = Paths.get(profilePicsDirectory, "testUser_newPic.jpg");

        assertTrue(Files.exists(newFilePath));
        verify(websiteUserRepository, times(1)).findByUsername("testUser");
        verify(websiteUserRepository, times(1)).save(any(WebsiteUser.class));

        Files.deleteIfExists(newFilePath);
    }

    @Test
    public void testGetProfilePicture_Success() throws IOException {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");
        user.setProfilepic(profilePicsDirectory+"/testUser_profilePic.jpg");

        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);

        Path filePath = Paths.get(profilePicsDirectory, "testUser_profilePic.jpg");
        Files.createDirectories(filePath.getParent());
        if (!Files.exists(filePath)) {
//            System.out.println("File does not exist, creating");
            Files.createFile(filePath);
            Files.write(filePath, "Test Image Content".getBytes());
//            System.out.println("File created: "+ Files.exists(filePath));
        }else{
//            System.out.println("File already exists, skipping");
        }
        byte[] result = websiteUserService.getProfilePicture("testUser");

        assertArrayEquals("Test Image Content".getBytes(), result);

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetProfilePicture_NotFound() {
        WebsiteUser user = new WebsiteUser();
        user.setUsername("testUser");

        when(websiteUserRepository.findByUsername("testUser")).thenReturn(user);

        assertThrows(IOException.class, () -> {
            websiteUserService.getProfilePicture("testUser");
        });

        verify(websiteUserRepository, times(1)).findByUsername("testUser");
    }
}
