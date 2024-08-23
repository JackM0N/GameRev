package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ttsw.GameRev.model.ForumRequest;

public interface ForumRequestRepository extends JpaRepository<ForumRequest, Long> {
}