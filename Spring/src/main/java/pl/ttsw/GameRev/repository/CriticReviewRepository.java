package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.CriticReview;

import java.util.Optional;

@Repository
public interface CriticReviewRepository extends JpaRepository<CriticReview, Long> {
    Optional<CriticReview> findByGameTitleAndApprovedByIsNotNull(String gameTitle);
}
