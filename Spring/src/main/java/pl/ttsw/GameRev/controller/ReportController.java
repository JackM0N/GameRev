package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.UserReviewService;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final UserReviewService userReviewService;
    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<Page<UserReviewDTO>> reviewsWithReports(UserReviewFilter userReviewFilter, Pageable pageable) {
        return ResponseEntity.ok(userReviewService.getUserReviewsWithReports(userReviewFilter, pageable));
    }
    
    @GetMapping("/{reviewId}")
    public ResponseEntity<Page<ReportDTO>> reviewReports(@PathVariable long reviewId, Pageable pageable) {
        UserReviewDTO userReviewDTO = userReviewService.getUserReviewById(reviewId);
        return ResponseEntity.ok(reportService.getReportsByReview(userReviewDTO, pageable));
    }
    
    @PutMapping("/approve")
    public ResponseEntity<ReportDTO> approveReport(@RequestBody ReportDTO reportDTO) throws BadRequestException {
        if (reportDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.updateReport(reportDTO));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) throws BadRequestException {
        if (reviewId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userReviewService.deleteUserReviewById(reviewId));
    }
}
