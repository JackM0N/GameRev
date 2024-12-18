package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.RoleDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.filter.WebsiteUserFilter;
import pl.ttsw.GameRev.service.WebsiteUserService;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class WebsiteUserController {
    private final WebsiteUserService websiteUserService;

    @GetMapping("/list")
    public ResponseEntity<Page<WebsiteUserDTO>> getUsers(WebsiteUserFilter websiteUserFilter, Pageable pageable) {
        return ResponseEntity.ok( websiteUserService.getAllWebsiteUsers(websiteUserFilter, pageable));
    }

    @GetMapping("/account")
    public ResponseEntity<WebsiteUserDTO> getAccount() {
        return ResponseEntity.ok(websiteUserService.findByCurrentUser());
    }

    @GetMapping("/{nickname}")
    public ResponseEntity<WebsiteUserDTO> getUser(@PathVariable String nickname) {
        WebsiteUserDTO user = websiteUserService.findByNickname(nickname);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/edit-profile/{username}")
    public ResponseEntity<WebsiteUserDTO> editUserProfile(
            @PathVariable String username,
            @RequestBody UpdateWebsiteUserDTO request,
            Principal principal) throws BadRequestException
    {
        String currentUsername = principal.getName();

        if (!currentUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WebsiteUserDTO user = websiteUserService.updateUserProfile(username, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{username}/profile-picture")
    public ResponseEntity<WebsiteUserDTO> uploadProfilePicture(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        try {
            ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO();
            profilePictureDTO.setUsername(username);
            profilePictureDTO.setProfilePicture(file);
            websiteUserService.uploadProfilePicture(profilePictureDTO);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{nickname}/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String nickname) {
        try {
            byte[] image = websiteUserService.getProfilePicture(nickname);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nickname + "_profile_pic\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    //Admin endpoints
    @PutMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody WebsiteUserDTO userDTO) {
        return ResponseEntity.ok(websiteUserService.banUser(userDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<WebsiteUserDTO> editUser(@PathVariable Long id, @RequestBody WebsiteUserDTO websiteUserDTO) throws BadRequestException {
        WebsiteUserDTO userDTO = websiteUserService.updateWebsiteUser(id, websiteUserDTO);
        if (userDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<WebsiteUserDTO> deleteUser(@PathVariable Long id) throws BadRequestException {
        boolean isDeleted = websiteUserService.deleteWebsiteUser(id);
        if (!isDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> editUserRole(
            @PathVariable Long id,
            @RequestBody RoleDTO roleDTO,
            @RequestParam boolean isAdded) throws BadRequestException
    {
        boolean changedRoles = websiteUserService.updateRoles(roleDTO, id, isAdded);
        if (!changedRoles) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
