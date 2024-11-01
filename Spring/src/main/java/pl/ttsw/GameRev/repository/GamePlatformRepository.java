package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.GamePlatform;

@Repository
public interface GamePlatformRepository extends JpaRepository<GamePlatform, Long>, JpaSpecificationExecutor<GamePlatform> {
    void deleteAllByGameId(Long id);
}
