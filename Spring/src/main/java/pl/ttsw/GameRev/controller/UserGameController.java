package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.filter.UserGameFilter;
import pl.ttsw.GameRev.service.UserGameService;

@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
public class UserGameController {
    private final UserGameService userGameService;

    @GetMapping("/{nickname}")
    public ResponseEntity<Page<UserGameDTO>> getUsersGames(
            @PathVariable String nickname,
            UserGameFilter userGameFilter,
            Pageable pageable) throws BadRequestException {
        return ResponseEntity.ok(userGameService.getUserGame(nickname, userGameFilter, pageable));
    }

    @PostMapping
    public ResponseEntity<UserGameDTO> addUserGame(@RequestBody UserGameDTO userGameDTO) throws BadRequestException {
        if (userGameDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userGameService.addGameToUser(userGameDTO));
    }

    @PutMapping
    public ResponseEntity<UserGameDTO> updateUserGame(@RequestBody UserGameDTO userGameDTO) throws BadRequestException {
        if (userGameDTO == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userGameService.updateGame(userGameDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserGame(@PathVariable Long id) throws BadRequestException {
        return ResponseEntity.ok(userGameService.deleteGame(id));
    }
}
