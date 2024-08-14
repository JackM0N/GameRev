package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.service.CriticReviewService;
import java.time.LocalDate;

@RestController
@RequestMapping("/critics-reviews")
public class CriticReviewController {
    private final CriticReviewService criticReviewService;

    public CriticReviewController(CriticReviewService criticReviewService) {
        this.criticReviewService = criticReviewService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll(
            @RequestParam(value = "gameTitle", required = false) String gameTitle,
            @RequestParam(value = "reviewStatus", required = false) ReviewStatus reviewStatus,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
    ) throws BadRequestException {
        gameTitle = gameTitle.replaceAll("-"," ");
        Page<CriticReviewDTO> criticReviewDTO = criticReviewService.getAllCriticReviews(gameTitle, reviewStatus, fromDate, toDate, pageable);
        if (criticReviewDTO.getTotalElements() == 0) {
            return ResponseEntity.badRequest().body("There are no critic reviews yet");
        }
        return ResponseEntity.ok(criticReviewDTO);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getById(@PathVariable long id) {
        CriticReviewDTO criticReviewDTO = criticReviewService.getCriticReviewById(id);
        if (criticReviewDTO == null) {
            return ResponseEntity.badRequest().body("There are no critic reviews for this id");
        }
        return ResponseEntity.ok(criticReviewDTO);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getByTitle(@PathVariable String title) throws BadRequestException {
        title = title.replaceAll("-", " ");
        CriticReviewDTO criticReviewDTO = criticReviewService.getCriticReviewByTitle(title);
        if (criticReviewDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(criticReviewDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CriticReviewDTO criticReviewDTO) throws BadRequestException {
        if (criticReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error creating this critic review");
        }
        return ResponseEntity.ok(criticReviewService.createCriticReview(criticReviewDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> update(@RequestBody CriticReviewDTO criticReviewDTO,
                                    @PathVariable long id) throws BadRequestException {
        if (criticReviewDTO == null) {
            return ResponseEntity.badRequest().body("There was an error editing this critic review");
        }
        return ResponseEntity.ok(criticReviewService.updateCriticReview(id,criticReviewDTO));
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<?> review(@RequestParam ReviewStatus reviewStatus,
                                    @PathVariable long id) throws BadRequestException {
        if (reviewStatus == null) {
            return ResponseEntity.badRequest().body("There was an error editing this critic review");
        }
        return ResponseEntity.ok(criticReviewService.reviewCriticReview(id, reviewStatus));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) throws BadRequestException {
        boolean deleted = criticReviewService.deleteCriticReview(id);
        if (!deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }
}
