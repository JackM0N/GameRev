package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Principal principal) {
        String username = principal.getName();
        WebsiteUser user = websiteUserService.findByUsername(username);
        WebsiteUserDTO userDTO = websiteUserService.mapToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/edit-profile/{username}")
    public ResponseEntity<?> edit(@PathVariable String username, @RequestBody WebsiteUserDTO request, Principal principal) throws BadRequestException {
        String currentUsername = principal.getName();

        if (!currentUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own profile");
        }

        WebsiteUser user = websiteUserService.updateUserProfile(username, request);
        WebsiteUserDTO userDTO = websiteUserService.mapToDTO(user);
        return ResponseEntity.ok(userDTO);
    }
}
