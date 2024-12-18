package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Report;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserAndUserReview(WebsiteUser user, UserReview userReview);
    Page<Report> findAllByUserReviewIdAndApprovedIsNullOrApprovedIsTrue(Long userReviewId, Pageable pageable);
    Page<Report> findAll(Specification<Report> spec, Pageable pageable);
    void deleteAllByUserReviewId(Long userReviewId);
}
