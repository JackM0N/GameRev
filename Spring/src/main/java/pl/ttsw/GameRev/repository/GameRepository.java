package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    public Game findGameByTitle(String title);
}
