package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ProfilePictureDTO;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
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

    @GetMapping("/list")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<WebsiteUserDTO> users = websiteUserService.getAllWebsiteUsers(pageable);
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccount() throws BadRequestException {
        WebsiteUserDTO user = websiteUserService.findByCurrentUser();
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{nickname}")
    public ResponseEntity<?> getUser(@PathVariable String nickname) {
        WebsiteUserDTO user = websiteUserService.findByNickname(nickname);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/edit-profile/{username}")
    public ResponseEntity<?> editUserProfile(@PathVariable String username, @RequestBody UpdateWebsiteUserDTO request, Principal principal) throws BadRequestException {
        String currentUsername = principal.getName();

        if (!currentUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own profile");
        }

        WebsiteUserDTO user = websiteUserService.updateUserProfile(username, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody WebsiteUserDTO userDTO) throws BadRequestException {
        boolean gotBanned = websiteUserService.banUser(userDTO);
        return ResponseEntity.ok(gotBanned);
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
            return ResponseEntity.status(404).body(null);
        }
    }
}
