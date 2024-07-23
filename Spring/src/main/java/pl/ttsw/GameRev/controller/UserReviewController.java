package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.service.RatingService;
import pl.ttsw.GameRev.service.UserReviewService;
import java.util.List;

@RestController
@RequestMapping("/users-reviews")
public class UserReviewController {
    private final UserReviewService userReviewService;
    private final RatingService ratingService;

    public UserReviewController(UserReviewService userReviewService, RatingService ratingService) {
        this.userReviewService = userReviewService;
        this.ratingService = ratingService;
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
    public ResponseEntity<?> createUserReview(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error creating the user review");
        }
        return ResponseEntity.ok(userReviewService.createUserReview(userReviewDTO));
    }

    @PutMapping("")
    public ResponseEntity<?> updateUserReview(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
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

    @PutMapping("/add-rating")
    public ResponseEntity<?> addUserReviewRating(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error rating the user review");
        }
        return ResponseEntity.ok(ratingService.updateRating(userReviewDTO));
    }
}
