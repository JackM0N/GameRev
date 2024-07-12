package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.service.GameService;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody GameDTO request) {
        Game game = gameService.createGame(request);
        if (game == null) {
            return ResponseEntity.badRequest().body("Game creation failed");
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{title}")
    public ResponseEntity<?> getGame(@PathVariable String title) {
        title = title.replaceAll("-"," ");
        GameDTO game = gameService.getGameByTitle(title);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameService.getGameByTitle(title));
    }
}
