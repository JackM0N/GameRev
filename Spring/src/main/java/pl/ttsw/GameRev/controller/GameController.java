package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.filter.GameFilter;
import pl.ttsw.GameRev.service.GameService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping
    public ResponseEntity<Page<GameDTO>> getAllGames(GameFilter gameFilter, Pageable pageable) {
        return ResponseEntity.ok(gameService.getAllGames(gameFilter, pageable));
    }

    @GetMapping("/{title}")
    public ResponseEntity<GameDTO> getGame(@PathVariable String title) {
        title = title.replaceAll("-"," ");
        return ResponseEntity.ok(gameService.getGameByTitle(title));
    }

    @PostMapping("/create")
    public ResponseEntity<GameDTO> createGame(@RequestParam(value = "picture", required = false) MultipartFile picture,
                                              @RequestParam("game") String gameJson) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        GameDTO request = objectMapper.readValue(gameJson, GameDTO.class);
        GameDTO game = gameService.createGame(request, picture);
        if (game == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(game);
    }

    @PutMapping("/edit/{title}")
    public ResponseEntity<GameDTO> editGame(@RequestParam(value = "picture", required = false) MultipartFile picture,
                                      @PathVariable String title,
                                      @RequestParam("game") String gameJson) throws IOException {
        title = title.replaceAll("-"," ");
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        GameDTO request = objectMapper.readValue(gameJson, GameDTO.class);
        GameDTO updatedGame = gameService.updateGame(title, request, picture);
        return ResponseEntity.ok(updatedGame);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        boolean gotRemoved = gameService.deleteGame(id);
        if (!gotRemoved) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
