package pl.ttsw.GameRev;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.ReleaseStatus;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.ReleaseStatusRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import pl.ttsw.GameRev.service.GameService;
import pl.ttsw.GameRev.service.ReleaseStatusService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReleaseStatusRepository statusRepository;

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ReleaseStatusRepository releaseStatusRepository;
    @Autowired
    private ReleaseStatusService releaseStatusService;

    @BeforeEach
    public void setUp() {
        Game game = gameRepository.findGameByTitle("Limbus Company 2");
        if (game != null) {
            gameService.deleteGame(game.getId());
        }
        game = gameRepository.findGameByTitle("Limbus Company Updated");
        if (game != null) {
            gameService.deleteGame(game.getId());
        }
    }

    @AfterEach
    public void tearDown() {
        Game game = gameRepository.findGameByTitle("Limbus Company 2");
        if (game != null) {
            gameService.deleteGame(game.getId());
        }
        game = gameRepository.findGameByTitle("Limbus Company Updated");
        if (game != null) {
            gameService.deleteGame(game.getId());
        }
    }

    private Game createGameForTesting() {
        Game game = new Game();
        game.setTitle("Limbus Company 2");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        ReleaseStatus releaseStatus = releaseStatusRepository.findReleaseStatusById(1L);
        Tag tag = tagRepository.findById(1L).orElseThrow(RuntimeException::new);
        game.setReleaseStatus(releaseStatus);
        game.setTags(new ArrayList<>(List.of(tag)));
        return game;
    }

    @Test
    @Transactional
    public void testCreateGame() {
        // existing tags
        ReleaseStatus releaseStatus = releaseStatusRepository.findReleaseStatusById(1L);
        Tag tag = tagRepository.findById(1L).orElseThrow(RuntimeException::new);

        GameDTO gameDTO = new GameDTO();
        gameDTO.setTitle("Limbus Company 2");
        gameDTO.setDeveloper("Project Moon");
        gameDTO.setPublisher("Project Moon");
        gameDTO.setReleaseDate(LocalDate.now());
        gameDTO.setDescription("Nice game");

        ReleaseStatusDTO releaseStatusDTO = new ReleaseStatusDTO();
        releaseStatusDTO.setId(releaseStatus.getId());
        releaseStatusDTO.setStatusName(releaseStatus.getStatusName());
        gameDTO.setReleaseStatus(releaseStatusDTO);

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setTagName(tag.getTagName());
        gameDTO.setTags(new ArrayList<>(List.of(tagDTO)));

        Game createdGame = gameService.createGame(gameDTO);

        assertNotNull(createdGame);
        assertEquals("Limbus Company 2", createdGame.getTitle());
        assertEquals("Project Moon", createdGame.getDeveloper());
        assertEquals("Project Moon", createdGame.getPublisher());
        assertEquals("Nice game", createdGame.getDescription());
        assertEquals(releaseStatus.getId(), createdGame.getReleaseStatus().getId());
        assertEquals("Released", createdGame.getReleaseStatus().getStatusName());
        assertEquals(1, createdGame.getTags().size());
        assertEquals(tag.getId(), createdGame.getTags().get(0).getId());
        assertEquals("Singleplayer", createdGame.getTags().get(0).getTagName());
        System.out.println(createdGame.getId());
        gameService.deleteGame(createdGame.getId());
    }

    @Test
    @Transactional
    public void testGetGameByTitle() {
        Game game = createGameForTesting();
        gameRepository.save(game);

        GameDTO gameDTO = gameService.getGameByTitle("Limbus Company 2");

        assertNotNull(gameDTO);
        assertEquals(game.getTitle(), gameDTO.getTitle());
        assertEquals(game.getDeveloper(), gameDTO.getDeveloper());
        assertEquals(game.getPublisher(), gameDTO.getPublisher());
        assertEquals(game.getReleaseDate(), gameDTO.getReleaseDate());
        assertEquals(game.getDescription(), gameDTO.getDescription());
        assertEquals(game.getReleaseStatus().getId(), gameDTO.getReleaseStatus().getId());
        assertEquals(game.getReleaseStatus().getStatusName(), gameDTO.getReleaseStatus().getStatusName());
        assertEquals(game.getTags().size(), gameDTO.getTags().size());
        assertEquals(game.getTags().get(0).getId(), gameDTO.getTags().get(0).getId());
        assertEquals(game.getTags().get(0).getTagName(), gameDTO.getTags().get(0).getTagName());
    }

    @Test
    @Transactional
    public void testGetGameById() {
        Game game = createGameForTesting();
        game = gameRepository.save(game);

        GameDTO gameDTO = gameService.getGameById(game.getId());

        assertNotNull(gameDTO);
        assertEquals(game.getId(), gameDTO.getId());
    }

    @Test
    @Transactional
    public void testUpdateGame() {
        Game existingGame = createGameForTesting();
        existingGame = gameRepository.save(existingGame);

        ReleaseStatus updatedStatus = releaseStatusRepository.findReleaseStatusById(2L);
        Tag updatedTag = tagRepository.findById(2L).orElseThrow(RuntimeException::new);

        GameDTO updateGameDTO = new GameDTO();
        updateGameDTO.setTitle("Limbus Company Updated");
        updateGameDTO.setDeveloper("Project Moon Updated");
        updateGameDTO.setPublisher("Project Moon Updated");
        updateGameDTO.setReleaseDate(LocalDate.of(2024, 1, 1));
        updateGameDTO.setDescription("Updated description");

        ReleaseStatusDTO updatedStatusDTO = new ReleaseStatusDTO();
        updatedStatusDTO.setId(updatedStatus.getId());
        updatedStatusDTO.setStatusName(updatedStatus.getStatusName());
        updateGameDTO.setReleaseStatus(updatedStatusDTO);

        TagDTO updatedTagDTO = new TagDTO();
        updatedTagDTO.setId(updatedTag.getId());
        updatedTagDTO.setTagName(updatedTag.getTagName());
        updateGameDTO.setTags(new ArrayList<>(List.of(updatedTagDTO)));

        GameDTO resultGameDTO = gameService.updateGame("Limbus Company 2", updateGameDTO);

        assertNotNull(resultGameDTO);
        assertEquals("Limbus Company Updated", resultGameDTO.getTitle());
        assertEquals("Project Moon Updated", resultGameDTO.getDeveloper());
        assertEquals("Project Moon Updated", resultGameDTO.getPublisher());
        assertEquals(LocalDate.of(2024, 1, 1), resultGameDTO.getReleaseDate());
        assertEquals("Updated description", resultGameDTO.getDescription());
        assertEquals(updatedStatus.getId(), resultGameDTO.getReleaseStatus().getId());
        assertEquals("Early Access", resultGameDTO.getReleaseStatus().getStatusName());
        assertEquals(1, resultGameDTO.getTags().size());
        assertEquals(updatedTag.getId(), resultGameDTO.getTags().get(0).getId());
        assertEquals("Multiplayer", resultGameDTO.getTags().get(0).getTagName());
    }

    @Test
    @Transactional
    public void testDeleteGame() {
        Game game = createGameForTesting();
        game = gameRepository.save(game);

        boolean result = gameService.deleteGame(game.getId());

        assertTrue(result);
        assertFalse(gameRepository.existsById(game.getId().intValue()));
    }

    @Test
    @Transactional
    public void testDeleteGameNotFound() {
        boolean result = gameService.deleteGame(9999L);

        assertFalse(result);
    }
}
