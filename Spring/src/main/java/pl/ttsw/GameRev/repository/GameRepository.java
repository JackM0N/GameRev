package pl.ttsw.GameRev.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Game;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findGameByTitle(String title);
    Optional<Game> findGameById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
    Page<Game> findAll(Pageable pageable);
}
