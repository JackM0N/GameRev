package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.UserReview;

import java.util.Optional;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Long>, JpaSpecificationExecutor<UserReview> {
    Optional<UserReview> findById(Long id);
}
