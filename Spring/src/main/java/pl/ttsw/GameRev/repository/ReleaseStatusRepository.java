package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.ReleaseStatus;

@Repository
public interface ReleaseStatusRepository extends JpaRepository<ReleaseStatus, Long> {
}