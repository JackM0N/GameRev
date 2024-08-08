package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.mapper.WebsiteUserMapper;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.IAuthenticationFacade;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class WebsiteUserService {
    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAuthenticationFacade authenticationFacade;
    private final RoleRepository roleRepository;
    private final WebsiteUserMapper websiteUserMapper;

    @Value("${profile.pics.directory}")
    private String profilePicsDirectory = "../Pictures/profile_pics";

    public WebsiteUserService(WebsiteUserRepository websiteUserRepository, PasswordEncoder passwordEncoder, IAuthenticationFacade authenticationFacade, RoleRepository roleRepository, WebsiteUserMapper websiteUserMapper) {
        this.websiteUserRepository = websiteUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationFacade = authenticationFacade;
        this.roleRepository = roleRepository;
        this.websiteUserMapper = websiteUserMapper;
    }

    public Page<WebsiteUserDTO> getAllWebsiteUsers(Pageable pageable) {
        Page<WebsiteUser> websiteUsers = websiteUserRepository.findAll(pageable);
        for (WebsiteUser websiteUser : websiteUsers) {
            websiteUser.setPassword(null);
        }
        return websiteUsers.map(websiteUserMapper::toDto);
    }

    public WebsiteUserDTO findByCurrentUser() {
        WebsiteUser websiteUser = getCurrentUser();
        if (websiteUser == null) {
            throw new BadCredentialsException("You are not logged in");
        }
        return websiteUserMapper.toDto(websiteUser);
    }

    public WebsiteUserDTO findByNickname(String nickname) {
        WebsiteUser user = websiteUserRepository.findByNickname(nickname)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        user.setId(null);
        user.setUsername(null);
        user.setPassword(null);
        user.setIsDeleted(null);
        return websiteUserMapper.toDto(user);
    }

    public WebsiteUserDTO updateUserProfile(String username, UpdateWebsiteUserDTO request) throws BadRequestException {
        WebsiteUser user = getCurrentUser();

        if (user == null){
            throw new BadRequestException("You need to login first");
        }
        if (!username.equals(user.getUsername())){
            throw new BadCredentialsException("You can only edit your own profile");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Passwords do not match");
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

        return websiteUserMapper.toDto(user);
    }

    public boolean banUser(WebsiteUserDTO userDTO) {
        WebsiteUser user = websiteUserRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        WebsiteUser currentUser = getCurrentUser();

        if (!currentUser.getRoles().contains(roleRepository.findByRoleName("Admin"))){
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new BadCredentialsException("This user is deleted");
        }
        user.setIsBanned(userDTO.getIsBanned());
        websiteUserRepository.save(user);
        return user.getIsBanned();
    }

    public void uploadProfilePicture(ProfilePictureDTO profilePictureDTO) throws IOException {
        String username = profilePictureDTO.getUsername();
        MultipartFile file = profilePictureDTO.getProfilePicture();
        WebsiteUser user = websiteUserRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.equals(getCurrentUser())){
            throw new BadCredentialsException("You can only edit your own profile picture");
        }
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

    public byte[] getProfilePicture(String nickname) throws IOException {
        WebsiteUser user = websiteUserRepository.findByNickname(nickname)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.getProfilepic() == null) {
            throw new IOException("Users profile picture not found");
        }

        Path filepath = Paths.get(user.getProfilepic());
        return Files.readAllBytes(filepath);
    }

    public WebsiteUser getCurrentUser() {
        Authentication authentication = authenticationFacade.getAuthentication();
        String username = authentication.getName();
        return websiteUserRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("You are not logged in"));
    }
}
