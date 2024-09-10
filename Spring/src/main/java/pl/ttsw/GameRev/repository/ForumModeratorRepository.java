package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumModerator;
import pl.ttsw.GameRev.model.WebsiteUser;

@Repository
public interface ForumModeratorRepository extends JpaRepository<ForumModerator, Long> {
    boolean existsByForumAndModerator(Forum forum, WebsiteUser moderator);
}
