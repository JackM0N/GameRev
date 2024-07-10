package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.RoleDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDto;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.security.AuthenticationResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WebsiteUserService {

    private final WebsiteUserRepository websiteUserRepository;
    private final PasswordEncoder passwordEncoder;

    public WebsiteUserService(WebsiteUserRepository websiteUserRepository, PasswordEncoder passwordEncoder) {
        this.websiteUserRepository = websiteUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public WebsiteUser findByUsername(String username) {
        return websiteUserRepository.findByUsername(username);
    }

    public WebsiteUser findByNickname(String nickname) {
        return websiteUserRepository.findByNickname(nickname);
    }

    public WebsiteUser updateUserProfile(String username, UpdateWebsiteUserDto request) throws BadRequestException {
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
        if (request.getProfilepic() != null && !request.getProfilepic().isEmpty()) {
            user.setProfilepic(request.getProfilepic());
        }
        if (request.getIsDeleted() != null) {
            user.setIsDeleted(request.getIsDeleted());
        }

        user = websiteUserRepository.save(user);

        return user;
    }

    public WebsiteUserDTO mapToDTO(WebsiteUser user) {
        WebsiteUserDTO dto = new WebsiteUserDTO();
        dto.setId(user.getId());
        dto.setPassword(user.getPassword());
        dto.setUsername(user.getUsername());
        dto.setProfilepic(user.getProfilepic());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setJoinDate(user.getJoinDate());
        dto.setIsBanned(user.getIsBanned());
        dto.setIsDeleted(user.getIsDeleted());
        dto.setRoles(user.getRoles().stream()
                .map(role -> new RoleDTO(role.getId(), role.getRoleName()))
                .collect(Collectors.toList()));
        return dto;
    }
}
