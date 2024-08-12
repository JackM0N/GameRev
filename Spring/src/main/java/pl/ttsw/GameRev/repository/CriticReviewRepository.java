package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.model.CriticReview;

import java.util.Optional;

@Repository
public interface CriticReviewRepository extends JpaRepository<CriticReview, Long> {
    Optional<CriticReview> findByGameTitleAndReviewStatus(String gameTitle, ReviewStatus reviewStatus);
    Optional<CriticReview> findByGameTitle(String gameTitle);
    Page<CriticReview> findAll(Pageable pageable);
}
