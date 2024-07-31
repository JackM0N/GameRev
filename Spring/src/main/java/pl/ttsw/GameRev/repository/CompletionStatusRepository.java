package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ttsw.GameRev.model.CompletionStatus;

public interface CompletionStatusRepository extends JpaRepository<CompletionStatus, Long> {
    CompletionStatus findByCompletionName(String name);
}
