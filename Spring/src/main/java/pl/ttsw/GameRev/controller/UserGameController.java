package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.service.UserGameService;

@RestController
@RequestMapping("/library")
public class UserGameController {
    private final UserGameService userGameService;

    public UserGameController(UserGameService userGameService) {
        this.userGameService = userGameService;
    }

    @GetMapping("/{nickname}")
    public ResponseEntity<?> getUsersGames(
            @PathVariable String nickname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) throws BadRequestException {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserGameDTO> userGameDTOS = userGameService.getUserGameDTO(nickname,pageable);
        if (userGameDTOS.getTotalElements() == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userGameDTOS);
    }

    @PostMapping
    public ResponseEntity<?> addUserGame(@RequestBody UserGameDTO userGameDTO) throws BadRequestException {
        if (userGameDTO == null){
            return ResponseEntity.badRequest().body("There was an error when trying add this game to the library");
        }
        return ResponseEntity.ok(userGameService.addGameToUser(userGameDTO));
    }

    @PutMapping
    public ResponseEntity<?> updateUserGame(@RequestBody UserGameDTO userGameDTO) throws BadRequestException {
        if (userGameDTO == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userGameService.updateGame(userGameDTO));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserGame(@RequestBody UserGameDTO userGameDTO) throws BadRequestException {
        if (userGameDTO == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userGameService.deleteGame(userGameDTO));
    }
}
