package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.UserGame;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long>, JpaSpecificationExecutor<UserGame> {
    Page<UserGame> findByUserNickname(String nickname, Pageable pageable);
}
