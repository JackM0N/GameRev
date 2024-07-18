package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.RoleDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.IAuthenticationFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebsiteUserService {

    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAuthenticationFacade authenticationFacade;

    @Value("${profile.pics.directory}")
    private String profilePicsDirectory = "src/main/resources/static/profile_pics/";

    public WebsiteUserService(WebsiteUserRepository websiteUserRepository, PasswordEncoder passwordEncoder, IAuthenticationFacade authenticationFacade) {
        this.websiteUserRepository = websiteUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationFacade = authenticationFacade;
    }

    public List<WebsiteUserDTO> getAllWebsiteUsers() {
        List<WebsiteUser> websiteUsers = websiteUserRepository.findAll();
        for (WebsiteUser websiteUser : websiteUsers) {
            websiteUser.setPassword(null);
        }
        return websiteUsers.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public WebsiteUser findByUsername(String username) {
        return websiteUserRepository.findByUsername(username);
    }

    public WebsiteUser findByNickname(String nickname) {
        return websiteUserRepository.findByNickname(nickname);
    }

    public WebsiteUser updateUserProfile(String username, UpdateWebsiteUserDTO request) throws BadRequestException {
        WebsiteUser user = websiteUserRepository.findByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            user.setDescription(request.getDescription());
        }
        if (request.getProfilepic() != null && !request.getProfilepic().isEmpty()) {
            user.setProfilepic(request.getProfilepic());
        }
        if (request.getIsDeleted() != null) {
            user.setIsDeleted(request.getIsDeleted());
        }

        user = websiteUserRepository.save(user);

        return user;
    }

    public void uploadProfilePicture(ProfilePictureDTO profilePictureDTO) throws IOException {
        String username = profilePictureDTO.getUsername();
        MultipartFile file = profilePictureDTO.getProfilePicture();
        WebsiteUser user = websiteUserRepository.findByUsername(username);

        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            Path oldFilepath = Paths.get(user.getProfilepic());
            Files.deleteIfExists(oldFilepath);
        }

        String filename = username + "_" + file.getOriginalFilename();
        Path filepath = Paths.get(profilePicsDirectory, filename);
        Files.copy(file.getInputStream(), filepath);

        user.setProfilepic(filepath.toString());
        websiteUserRepository.save(user);
    }

    public byte[] getProfilePicture(String username) throws IOException {
        WebsiteUser user = websiteUserRepository.findByUsername(username);

        if (user.getProfilepic() == null) {
            throw new IOException("Users profile picture not found");
        }

        Path filepath = Paths.get(user.getProfilepic());
        return Files.readAllBytes(filepath);
    }

    public WebsiteUserDTO mapToDTO(WebsiteUser user) {
        WebsiteUserDTO dto = new WebsiteUserDTO();
        dto.setId(user.getId());
        dto.setPassword(user.getPassword());
        dto.setUsername(user.getUsername());
        dto.setProfilepic(user.getProfilepic());
        dto.setNickname(user.getNickname());
        dto.setDescription(user.getDescription());
        dto.setEmail(user.getEmail());
        dto.setJoinDate(user.getJoinDate());
        dto.setIsBanned(user.getIsBanned());
        dto.setIsDeleted(user.getIsDeleted());
        dto.setRoles(user.getRoles().stream()
                .map(role -> new RoleDTO(role.getId(), role.getRoleName()))
                .collect(Collectors.toList()));
        return dto;
    }

    public WebsiteUser getCurrentUser() {
        Authentication authentication = authenticationFacade.getAuthentication();
        String username = authentication.getName();
        if (websiteUserRepository.findByUsername(username) == null) {
            return null;
        }
        return websiteUserRepository.findByUsername(username);
    }
}
