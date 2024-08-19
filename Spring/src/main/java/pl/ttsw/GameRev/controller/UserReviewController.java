package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.service.RatingService;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.UserReviewService;
import java.time.LocalDate;

@RestController
@RequestMapping("/users-reviews")
public class UserReviewController {
    private final UserReviewService userReviewService;
    private final RatingService ratingService;
    private final ReportService reportService;

    public UserReviewController(UserReviewService userReviewService, RatingService ratingService, ReportService reportService) {
        this.userReviewService = userReviewService;
        this.ratingService = ratingService;
        this.reportService = reportService;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserReviewById(@PathVariable Long id) {
        UserReviewDTO userReviewDTO = userReviewService.getUserReviewById(id);
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There are no user reviews for this id");
        }
        return ResponseEntity.ok(userReviewDTO);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getUserReviewByGame(
            @PathVariable String title,
            @RequestParam(value = "postDateFrom", required = false) LocalDate postDateFrom,
            @RequestParam(value = "postDateTo", required = false) LocalDate postDateTo,
            @RequestParam(value = "scoreFrom", required = false) Integer scoreFrom,
            @RequestParam(value = "scoreTo", required = false) Integer scoreTo,
            Pageable pageable) {
        title = title.replaceAll("-", " ");
        Page<UserReviewDTO> userReviewDTO = userReviewService
                .getUserReviewByGame(title, postDateFrom, postDateTo, scoreFrom, scoreTo, pageable);
        if (userReviewDTO == null || userReviewDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    public ResponseEntity<?> deleteUserReview(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        boolean gotRemoved = userReviewService.deleteUserReviewByOwner(userReviewDTO);
        if (!gotRemoved) {
            return ResponseEntity.badRequest().body("There was an error deleting the user review");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getUserReviewByUserId(
            @PathVariable Long id,
            @RequestParam(value = "postDateFrom", required = false) LocalDate postDateFrom,
            @RequestParam(value = "postDateTo", required = false) LocalDate postDateTo,
            @RequestParam(value = "scoreFrom", required = false) Integer scoreFrom,
            @RequestParam(value = "scoreTo", required = false) Integer scoreTo,
            Pageable pageable) throws BadRequestException {
        Page<UserReviewDTO> userReviewDTOS = userReviewService
                .getUserReviewByUser(id, postDateFrom, postDateTo, scoreFrom, scoreTo, pageable);
        if (userReviewDTOS == null || userReviewDTOS.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(userReviewDTOS);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getUserReviews(
            @RequestParam(value = "postDateFrom", required = false) LocalDate postDateFrom,
            @RequestParam(value = "postDateTo", required = false) LocalDate postDateTo,
            @RequestParam(value = "scoreFrom", required = false) Integer scoreFrom,
            @RequestParam(value = "scoreTo", required = false) Integer scoreTo,
            Pageable pageable) throws BadRequestException {
        Page<UserReviewDTO> userReviewDTOS = userReviewService
                .getUserReviewByOwner( postDateFrom, postDateTo, scoreFrom, scoreTo, pageable);
        if (userReviewDTOS == null || userReviewDTOS.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(userReviewDTOS);
    }

    @PutMapping("/add-rating")
    public ResponseEntity<?> addUserReviewRating(@RequestBody UserReviewDTO userReviewDTO) throws BadRequestException {
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error rating the user review");
        }
        return ResponseEntity.ok(ratingService.updateRating(userReviewDTO));
    }

    @PutMapping("/report")
    public ResponseEntity<?> reportUserReview(@RequestBody ReportDTO reportDTO) throws BadRequestException {
        if (reportDTO == null){
            return ResponseEntity.badRequest().body("There was an error when trying to report this users review");
        }
        return ResponseEntity.ok(reportService.createReport(reportDTO));
    }
}
