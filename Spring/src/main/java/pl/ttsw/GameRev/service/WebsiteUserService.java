package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.RoleDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.mapper.WebsiteUserMapper;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.IAuthenticationFacade;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

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

    public Page<WebsiteUserDTO> getAllWebsiteUsers(
            LocalDate joinDateFrom,
            LocalDate joinDateTo,
            Boolean isDeleted,
            Boolean isBanned,
            List<Long> roleIds,
            Pageable pageable) {
        Specification<WebsiteUser> spec = Specification.where((root, query, builder) -> builder.conjunction());

        if (joinDateFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("joinDate"), joinDateFrom));
        }
        if (joinDateTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("joinDate"), joinDateTo));
        }
        if (isDeleted != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"), isDeleted));
        }
        if (isBanned != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("isBanned"), isBanned));
        }
        if (roleIds != null && !roleIds.isEmpty()) {
            spec = spec.and((root, query, builder) -> {
                Join<WebsiteUser, Role> rolesJoin = root.join("roles");
                return rolesJoin.get("id").in(roleIds);
            });
        }
        Page<WebsiteUser> websiteUsers = websiteUserRepository.findAll(spec, pageable);
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

    public WebsiteUserDTO updateWebsiteUser(Long userId, WebsiteUserDTO websiteUserDTO) throws BadRequestException {
        WebsiteUser user = websiteUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        WebsiteUser currentUser = getCurrentUser();
        if (!currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get())){
            throw new BadCredentialsException("You dont have permission to perform this action");
        }

        if (websiteUserDTO.getUsername() != null && !websiteUserDTO.getUsername().isEmpty()) {
            user.setUsername(websiteUserDTO.getUsername());
        }
        if (websiteUserDTO.getPassword() != null && !websiteUserDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(websiteUserDTO.getPassword()));
        }
        if (websiteUserDTO.getProfilepic() != null && !websiteUserDTO.getProfilepic().isEmpty()) {
            user.setProfilepic(null);
        }
        if (websiteUserDTO.getEmail() != null && !websiteUserDTO.getEmail().isEmpty()) {
            user.setEmail(websiteUserDTO.getEmail());
        }
        if (websiteUserDTO.getNickname() != null && !websiteUserDTO.getNickname().isEmpty()) {
            user.setNickname(websiteUserDTO.getNickname());
        }
        if (websiteUserDTO.getDescription() != null && !websiteUserDTO.getDescription().isEmpty()) {
            user.setDescription(websiteUserDTO.getDescription());
        }
        if (websiteUserDTO.getIsDeleted() != null) {
            user.setIsDeleted(websiteUserDTO.getIsDeleted());
        }

        return websiteUserMapper.toDto(websiteUserRepository.save(user));
    }

    public boolean updateRoles(RoleDTO roleDTO, long userId, boolean isAdded) throws BadRequestException {
        WebsiteUser websiteUser = websiteUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        List<Role> roles = websiteUser.getRoles();
        Role role = roleRepository.findByRoleName(roleDTO.getRoleName())
                .orElseThrow(() -> new BadRequestException("Role not found"));
        if (websiteUser.getRoles().contains(role) && !isAdded){
            roles.remove(role);
            websiteUser.setRoles(roles);
            websiteUserRepository.save(websiteUser);
            return true;
        }
        if (!websiteUser.getRoles().contains(role) && isAdded) {
            roles.add(role);
            websiteUser.setRoles(roles);
            websiteUserRepository.save(websiteUser);
            return true;
        }
        return false;
    }

    public boolean banUser(WebsiteUserDTO userDTO) {
        WebsiteUser user = websiteUserRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        WebsiteUser currentUser = getCurrentUser();

        if (!currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get())){
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new BadCredentialsException("This user is deleted");
        }
        user.setIsBanned(userDTO.getIsBanned());
        websiteUserRepository.save(user);
        return user.getIsBanned();
    }

    public boolean deleteWebsiteUser(Long id) throws BadRequestException {
        WebsiteUser user = websiteUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        websiteUserRepository.delete(user);
        return true;
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
