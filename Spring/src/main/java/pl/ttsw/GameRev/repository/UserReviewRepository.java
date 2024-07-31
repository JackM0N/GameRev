package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Integer> {
    Page<UserReview> findByGameTitle(String title, Pageable pageable);
    Page<UserReview> findByUser(WebsiteUser websiteUser, Pageable pageable);
    UserReview findById(Long id);

    @Query("SELECT ur FROM UserReview ur WHERE ur.reports IS NOT EMPTY ")
    Page<UserReview> findWithReports(Pageable pageable);
}
