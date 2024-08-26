package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.enums.CompletionStatus;
import pl.ttsw.GameRev.service.UserGameService;
import java.util.List;

@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
public class UserGameController {
    private final UserGameService userGameService;

    @GetMapping("/{nickname}")
    public ResponseEntity<?> getUsersGames(
            @PathVariable String nickname,
            @RequestParam(value = "isFavourite", required = false) Boolean isFavourite,
            @RequestParam(value = "completionStatus", required = false) CompletionStatus completionStatus,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            Pageable pageable) throws BadRequestException {
        Page<UserGameDTO> userGameDTOS = userGameService.getUserGame(isFavourite, completionStatus, tagIds, nickname, pageable);
        if (userGameDTOS.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserGame(@PathVariable Long id) throws BadRequestException {
        return ResponseEntity.ok(userGameService.deleteGame(id));
    }
}
