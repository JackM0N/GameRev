package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.ForumComment;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long>, JpaSpecificationExecutor<ForumComment> {
}
