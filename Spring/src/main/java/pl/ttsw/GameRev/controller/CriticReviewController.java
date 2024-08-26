package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.filter.CriticReviewFilter;
import pl.ttsw.GameRev.service.CriticReviewService;

@RestController
@RequestMapping("/critics-reviews")
@RequiredArgsConstructor
public class CriticReviewController {
    private final CriticReviewService criticReviewService;

    @GetMapping("/list")
    public ResponseEntity<Page<CriticReviewDTO>> getAll(CriticReviewFilter criticReviewFilter, Pageable pageable){
        return ResponseEntity.ok(criticReviewService.getAllCriticReviews(criticReviewFilter, pageable));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CriticReviewDTO> getById(@PathVariable long id) {
        return ResponseEntity.ok(criticReviewService.getCriticReviewById(id));
    }

    @GetMapping("/{title}")
    public ResponseEntity<CriticReviewDTO> getByTitle(@PathVariable String title) throws BadRequestException {
        title = title.replaceAll("-", " ");
        CriticReviewDTO criticReviewDTO = criticReviewService.getCriticReviewByTitle(title);
        if (criticReviewDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(criticReviewDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<CriticReviewDTO> create(@RequestBody CriticReviewDTO criticReviewDTO) throws BadRequestException {
        if (criticReviewDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(criticReviewService.createCriticReview(criticReviewDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<CriticReviewDTO> update(@RequestBody CriticReviewDTO criticReviewDTO,
                                    @PathVariable long id) throws BadRequestException {
        if (criticReviewDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(criticReviewService.updateCriticReview(id,criticReviewDTO));
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<CriticReviewDTO> review(@RequestParam ReviewStatus reviewStatus,
                                    @PathVariable long id) throws BadRequestException {
        if (reviewStatus == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(criticReviewService.reviewCriticReview(id, reviewStatus));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) throws BadRequestException {
        boolean deleted = criticReviewService.deleteCriticReview(id);
        if (!deleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
