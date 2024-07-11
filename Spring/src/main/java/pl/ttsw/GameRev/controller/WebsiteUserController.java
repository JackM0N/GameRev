package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDto;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/user")
public class WebsiteUserController {
    private final WebsiteUserService websiteUserService;

    public WebsiteUserController(WebsiteUserService websiteUserService) {
        this.websiteUserService = websiteUserService;
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Principal principal) {
        String username = principal.getName();
        WebsiteUser user = websiteUserService.findByUsername(username);
        WebsiteUserDTO userDTO = websiteUserService.mapToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/edit-profile/{username}")
    public ResponseEntity<?> editUserProfile(@PathVariable String username, @RequestBody UpdateWebsiteUserDto request, Principal principal) throws BadRequestException {
        String currentUsername = principal.getName();

        if (!currentUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own profile");
        }

        WebsiteUser user = websiteUserService.updateUserProfile(username, request);
        WebsiteUserDTO userDTO = websiteUserService.mapToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/{username}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        try {
            ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO();
            profilePictureDTO.setUsername(username);
            profilePictureDTO.setProfilePicture(file);
            websiteUserService.uploadProfilePicture(profilePictureDTO);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading profile picture");
        }
    }

    @GetMapping("/{username}/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String username) {
        try {
            byte[] image = websiteUserService.getProfilePicture(username);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + username + "_profile_pic\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}
