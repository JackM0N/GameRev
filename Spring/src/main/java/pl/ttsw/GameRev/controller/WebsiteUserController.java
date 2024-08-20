package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import pl.ttsw.GameRev.service.WebsiteUserService;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/user")
public class WebsiteUserController {
    private final WebsiteUserService websiteUserService;

    public WebsiteUserController(WebsiteUserService websiteUserService) {
        this.websiteUserService = websiteUserService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUsers(
            @RequestParam(value = "joinDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinDateFrom,
            @RequestParam(value = "joinDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinDateTo,
            @RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
            @RequestParam(value = "isBanned", required = false) Boolean isBanned,
            @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
            @RequestParam(value = "searchText", required = false) String searchText,
            Pageable pageable
    ) {
        Page<WebsiteUserDTO> users = websiteUserService
                .getAllWebsiteUsers(joinDateFrom, joinDateTo, isDeleted, isBanned, roleIds, searchText, pageable);
        if (users.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccount() {
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    //Admin endpoints
    @PutMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody WebsiteUserDTO userDTO) {
        boolean gotBanned = websiteUserService.banUser(userDTO);
        return ResponseEntity.ok(gotBanned);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @RequestBody WebsiteUserDTO websiteUserDTO) throws BadRequestException {
        WebsiteUserDTO userDTO = websiteUserService.updateWebsiteUser(id, websiteUserDTO);
        if (userDTO == null) {
            return ResponseEntity.badRequest().body("There was an error updating website user");
        }
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) throws BadRequestException {
        boolean isDeleted = websiteUserService.deleteWebsiteUser(id);
        if (!isDeleted) {
            return ResponseEntity.badRequest().body("There was an error deleting website user");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> editUserRole(
            @PathVariable Long id,
            @RequestBody RoleDTO roleDTO,
            @RequestParam boolean isAdded) throws BadRequestException {
        boolean changedRoles = websiteUserService.updateRoles(roleDTO, id, isAdded);
        if (!changedRoles) {
            return ResponseEntity.badRequest().body("There was an error updating this users roles");
        }
        return ResponseEntity.ok().build();
    }
}
