package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.UserReview;

import java.util.List;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Integer> {
    List<UserReview> findByGameTitle(String title);
    List<UserReview> findByUserId(Long id);
    UserReview findByUserUsernameAndGameTitle(String username, String title);
    UserReview findById(Long id);
}
