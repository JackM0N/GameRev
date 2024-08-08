package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.service.GameService;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("")
    public ResponseEntity<?> createGame(@RequestBody GameDTO request) throws BadRequestException {
        Game game = gameService.createGame(request);
        if (game == null) {
            return ResponseEntity.badRequest().body("Game creation failed");
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getGame(@PathVariable String title) throws BadRequestException {
        title = title.replaceAll("-"," ");
        GameDTO game = gameService.getGameByTitle(title);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameService.getGameByTitle(title));
    }

    @PutMapping("/{title}")
    public ResponseEntity<?> editGame(@PathVariable String title, @RequestBody GameDTO request) throws BadRequestException {
        title = title.replaceAll("-"," ");
        GameDTO updatedGame = gameService.updateGame(title, request);
        if (updatedGame == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedGame);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        boolean gotRemoved = gameService.deleteGame(id);
        if (!gotRemoved) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllGames(Pageable pageable) {
        Page<GameDTO> games = gameService.getAllGames(pageable);
        return ResponseEntity.ok(games);
    }
}
