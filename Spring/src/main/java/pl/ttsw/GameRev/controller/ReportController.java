package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.UserReviewService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final UserReviewService userReviewService;
    private final ReportService reportService;

    public ReportController(UserReviewService userReviewService, ReportService reportService) {
        this.userReviewService = userReviewService;
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<?> reviewsWithReports(Pageable pageable) {
        Page<UserReviewDTO> userReviewDTOS = userReviewService.getUserReviewsWithReports(pageable);
        if (userReviewDTOS == null || userReviewDTOS.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userReviewDTOS);
    }
    
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> reviewReports(@PathVariable int reviewId, Pageable pageable) {
        UserReviewDTO userReviewDTO = userReviewService.getUserReviewById(reviewId);
        if (userReviewDTO == null) {
            return ResponseEntity.badRequest().body("This review doesn't exists");
        }

        Page<ReportDTO> reportDTOS = reportService.getReportsByReview(userReviewDTO,pageable);
        if (reportDTOS == null || reportDTOS.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reportDTOS);
    }
    
    @PutMapping("/approve")
    public ResponseEntity<?> approveReport(@RequestBody ReportDTO reportDTO) throws BadRequestException {
        if (reportDTO == null) {
            return ResponseEntity.badRequest().body("This report doesn't exists");
        }
        return ResponseEntity.ok(reportService.updateReport(reportDTO));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) throws BadRequestException {
        if (reviewId == null) {
            return ResponseEntity.badRequest().body("This review doesn't exists");
        }
        return ResponseEntity.ok(userReviewService.deleteUserReviewById(reviewId));
    }
}
