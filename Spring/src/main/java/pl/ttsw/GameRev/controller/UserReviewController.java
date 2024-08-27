package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.RatingDTO;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
import pl.ttsw.GameRev.model.Rating;
import pl.ttsw.GameRev.service.RatingService;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.UserReviewService;
import java.time.LocalDate;

@RestController
@RequestMapping("/users-reviews")
@RequiredArgsConstructor
public class UserReviewController {
    private final UserReviewService userReviewService;
    private final RatingService ratingService;
    private final ReportService reportService;

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(userReviewService.getUserReviewById(id));
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getUserReviewByGame(
            @PathVariable String title,
            UserReviewFilter userReviewFilter,
            Pageable pageable) {
        title = title.replaceAll("-", " ");
        return ResponseEntity.ok(userReviewService.getUserReviewByGame(title, userReviewFilter, pageable));
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
    public ResponseEntity<?> deleteUserReview(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        boolean gotRemoved = userReviewService.deleteUserReviewByOwner(userReviewDTO);
        if (!gotRemoved) {
            return ResponseEntity.badRequest().body("There was an error deleting the user review");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Page<UserReviewDTO>> getUserReviewByUserId(
            @PathVariable Long id,
            UserReviewFilter userReviewFilter,
            Pageable pageable) throws BadRequestException {
        return ResponseEntity.ok(userReviewService.getUserReviewByUser(id, userReviewFilter, pageable));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<Page<UserReviewDTO>> getUserReviews(
            UserReviewFilter userReviewFilter,
            Pageable pageable) {
        return ResponseEntity.ok(userReviewService.getUserReviewByOwner(userReviewFilter, pageable));
    }

    @PutMapping("/add-rating")
    public ResponseEntity<RatingDTO> addUserReviewRating(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ratingService.updateRating(userReviewDTO));
    }

    @PutMapping("/report")
    public ResponseEntity<ReportDTO> reportUserReview(@RequestBody ReportDTO reportDTO) throws BadRequestException {
        if (reportDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.createReport(reportDTO));
    }
}
