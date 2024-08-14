package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.enums.CompletionStatus;
import pl.ttsw.GameRev.service.UserGameService;

import java.util.List;

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
            @RequestParam(value = "isFavourite", required = false) Boolean isFavourite,
            @RequestParam(value = "completionStatus", required = false) CompletionStatus completionStatus,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            Pageable pageable) throws BadRequestException {
        Page<UserGameDTO> userGameDTOS = userGameService.getUserGameDTO(isFavourite, completionStatus, tagIds, nickname, pageable);
        if (userGameDTOS.getTotalElements() == 0) {
            return ResponseEntity.badRequest().body("No games found with the given criteria");
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
