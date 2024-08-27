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
import pl.ttsw.GameRev.service.GameService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestParam(value = "picture", required = false) MultipartFile picture,
                                        @RequestParam("game") String gameJson) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        GameDTO request = objectMapper.readValue(gameJson, GameDTO.class);
        GameDTO game = gameService.createGame(request, picture);
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

    @PutMapping("/edit/{title}")
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        boolean gotRemoved = gameService.deleteGame(id);
        if (!gotRemoved) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllGames(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "minUserScore", required = false) Float minUserScore,
            @RequestParam(value = "maxUserScore", required = false) Float maxUserScore,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "releaseStatuses", required = false) List<ReleaseStatus> releaseStatuses,
            Pageable pageable
    ) {
        Page<GameDTO> games = gameService.getAllGames(fromDate, toDate, minUserScore, maxUserScore, tagIds, releaseStatuses, pageable);
        if (games.getTotalElements() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(games);
    }
}
