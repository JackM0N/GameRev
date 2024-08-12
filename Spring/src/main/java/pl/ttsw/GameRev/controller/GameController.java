package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.mail.internet.MimeMultipart;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.service.GameService;

import java.io.IOException;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("")
    public ResponseEntity<?> createGame(@RequestParam(value = "picture", required = false) MultipartFile picture,
                                        @RequestParam("game") String gameJson) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        GameDTO request = objectMapper.readValue(gameJson, GameDTO.class);
        GameDTO game = gameService.createGame(picture, request);
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
    public ResponseEntity<?> editGame(@RequestParam(value = "picture", required = false) MultipartFile picture,
                                      @PathVariable String title,
                                      @RequestParam("game") String gameJson) throws IOException {
        title = title.replaceAll("-"," ");
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        GameDTO request = objectMapper.readValue(gameJson, GameDTO.class);
        GameDTO updatedGame = gameService.updateGame(title, request, picture);
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
