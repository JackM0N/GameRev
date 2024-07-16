package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.service.UserReviewService;

import java.util.List;

@RestController
@RequestMapping("/users-reviews")
public class UserReviewController {
    private final UserReviewService userReviewService;
    private final GameRepository gameRepository;

    public UserReviewController(UserReviewService userReviewService,
                                GameRepository gameRepository) {
        this.userReviewService = userReviewService;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserReviewById(@PathVariable Integer id) {
        UserReviewDTO userReviewDTO = userReviewService.getUserReviewById(id);
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There are no user reviews for this id");
        }
        return ResponseEntity.ok(userReviewDTO);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getUserReviewByGame(@PathVariable String title) {
        title = title.replaceAll("-", " ");
        List<UserReviewDTO> userReviewDTO = userReviewService.getUserReviewByGame(title);
        if (userReviewDTO == null || userReviewDTO.isEmpty()) {
            return ResponseEntity.badRequest().body("There are no user reviews for this title");
        }
        return ResponseEntity.ok(userReviewDTO);
    }

    @PostMapping("")
    public ResponseEntity<?> createUserReview(@RequestBody UserReviewDTO userReviewDTO) {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error creating the user review");
        }
        return ResponseEntity.ok(userReviewService.createUserReview(userReviewDTO));
    }

    @PutMapping("")
    public ResponseEntity<?> updateUserReview(@RequestBody UserReviewDTO userReviewDTO) {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error updating the user review");
        }
        return ResponseEntity.ok(userReviewService.updateUserReview(userReviewDTO));
    }
  
    @DeleteMapping("")
    public ResponseEntity<?> deleteUserReview(@RequestBody UserReviewDTO userReviewDTO) {
        boolean gotRemoved = userReviewService.deleteUserReview(userReviewDTO);
        if (!gotRemoved) {
            return ResponseEntity.badRequest().body("There was an error deleting the user review");
        }
        return ResponseEntity.ok().build();
    }
}
