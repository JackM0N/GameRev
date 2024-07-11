package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UpdateWebsiteUserDto;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class WebsiteUserController {
    private final WebsiteUserService websiteUserService;

    public WebsiteUserController(WebsiteUserService websiteUserService) {
        this.websiteUserService = websiteUserService;
    }

    @GetMapping("/account/{username}")
    public ResponseEntity<?> getAccount(@PathVariable String username) {
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
}
