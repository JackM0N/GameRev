package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Forum;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long>, JpaSpecificationExecutor<Forum> {
}
