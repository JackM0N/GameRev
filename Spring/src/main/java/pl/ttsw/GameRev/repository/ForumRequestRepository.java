package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.ttsw.GameRev.model.ForumRequest;
import java.util.List;

public interface ForumRequestRepository extends JpaRepository<ForumRequest, Long>, JpaSpecificationExecutor<ForumRequest> {
    List<ForumRequest> findAllByGameId(Long gameId);
}
