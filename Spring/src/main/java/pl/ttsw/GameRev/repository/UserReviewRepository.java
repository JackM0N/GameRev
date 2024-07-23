package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.UserReview;
import java.util.List;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Integer> {
    Page<UserReview> findByGameTitle(String title, Pageable pageable);
    List<UserReview> findByUserId(Long id);
    UserReview findById(Long id);
}
