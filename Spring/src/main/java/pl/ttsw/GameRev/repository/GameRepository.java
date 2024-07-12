package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findGameByTitle(String title);
    Game findGameById(int id);
}
