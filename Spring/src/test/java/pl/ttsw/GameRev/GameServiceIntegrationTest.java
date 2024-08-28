package pl.ttsw.GameRev;

import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.filter.GameFilter;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import pl.ttsw.GameRev.service.GameService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TagRepository tagRepository;

    private Game createGameForTesting() {
        Game game = new Game();
        game.setTitle("Limbus Company 2");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        game.setReleaseStatus(ReleaseStatus.RELEASED);
        Tag tag = tagRepository.findById(1L).orElseThrow(RuntimeException::new);
        game.setTags(new ArrayList<>(List.of(tag)));
        return game;
    }

    @Test
    @Transactional
    public void testCreateGame() throws IOException {
        // existing tags
        Tag tag = tagRepository.findById(1L).orElseThrow(RuntimeException::new);

        GameDTO gameDTO = new GameDTO();
        gameDTO.setTitle("Limbus Company 2");
        gameDTO.setDeveloper("Project Moon");
        gameDTO.setPublisher("Project Moon");
        gameDTO.setReleaseDate(LocalDate.now());
        gameDTO.setDescription("Nice game");

        gameDTO.setReleaseStatus(ReleaseStatus.RELEASED);

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setTagName(tag.getTagName());
        gameDTO.setTags(new ArrayList<>(List.of(tagDTO)));

        GameDTO createdGame = gameService.createGame(gameDTO, null);

        assertNotNull(createdGame);
        assertEquals("Limbus Company 2", createdGame.getTitle());
        assertEquals("Project Moon", createdGame.getDeveloper());
        assertEquals("Project Moon", createdGame.getPublisher());
        assertEquals("Nice game", createdGame.getDescription());
        assertEquals(ReleaseStatus.RELEASED, createdGame.getReleaseStatus());
        assertEquals(1, createdGame.getTags().size());
        assertEquals(tag.getId(), createdGame.getTags().get(0).getId());
        assertEquals("Singleplayer", createdGame.getTags().get(0).getTagName());
        gameService.deleteGame(createdGame.getId());
    }

    @Test
    @Transactional
    public void testGetGameByTitle() throws BadRequestException {
        Game game = createGameForTesting();
        gameRepository.save(game);

        GameDTO gameDTO = gameService.getGameByTitle("Limbus Company 2");

        assertNotNull(gameDTO);
        assertEquals(game.getTitle(), gameDTO.getTitle());
        assertEquals(game.getDeveloper(), gameDTO.getDeveloper());
        assertEquals(game.getPublisher(), gameDTO.getPublisher());
        assertEquals(game.getReleaseDate(), gameDTO.getReleaseDate());
        assertEquals(game.getDescription(), gameDTO.getDescription());
        assertEquals(game.getReleaseStatus(), gameDTO.getReleaseStatus());
        assertEquals(game.getTags().size(), gameDTO.getTags().size());
        assertEquals(game.getTags().get(0).getId(), gameDTO.getTags().get(0).getId());
        assertEquals(game.getTags().get(0).getTagName(), gameDTO.getTags().get(0).getTagName());
    }

    @Test
    @Transactional
    public void testUpdateGame() throws IOException {
        Game existingGame = createGameForTesting();
        existingGame = gameRepository.save(existingGame);

        Tag updatedTag = tagRepository.findById(2L).orElseThrow(RuntimeException::new);

        GameDTO updateGameDTO = new GameDTO();
        updateGameDTO.setTitle("Limbus Company Updated");
        updateGameDTO.setDeveloper("Project Moon Updated");
        updateGameDTO.setPublisher("Project Moon Updated");
        updateGameDTO.setReleaseDate(LocalDate.of(2024, 1, 1));
        updateGameDTO.setDescription("Updated description");
        updateGameDTO.setReleaseStatus(ReleaseStatus.END_OF_SERVICE);

        TagDTO updatedTagDTO = new TagDTO();
        updatedTagDTO.setId(updatedTag.getId());
        updatedTagDTO.setTagName(updatedTag.getTagName());
        updateGameDTO.setTags(new ArrayList<>(List.of(updatedTagDTO)));

        GameDTO resultGameDTO = gameService.updateGame("Limbus Company 2", updateGameDTO, null);

        assertNotNull(resultGameDTO);
        assertEquals("Limbus Company Updated", resultGameDTO.getTitle());
        assertEquals("Project Moon Updated", resultGameDTO.getDeveloper());
        assertEquals("Project Moon Updated", resultGameDTO.getPublisher());
        assertEquals(LocalDate.of(2024, 1, 1), resultGameDTO.getReleaseDate());
        assertEquals("Updated description", resultGameDTO.getDescription());
        assertEquals(ReleaseStatus.END_OF_SERVICE, resultGameDTO.getReleaseStatus());
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
        assertFalse(gameRepository.existsById(game.getId()));
    }

    @Test
    @Transactional
    public void testDeleteGameNotFound() {
        boolean result = gameService.deleteGame(9999L);

        assertFalse(result);
    }


    @Test
    @Transactional
    public void testGetAllGamesWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        // Test filtering by date range
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        GameFilter dateFilter = new GameFilter();
        dateFilter.setFromDate(fromDate);
        dateFilter.setToDate(toDate);

        Page<GameDTO> result = gameService.getAllGames(dateFilter, pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream().allMatch(game -> game.getReleaseDate().isAfter(fromDate.minusDays(1)) && game.getReleaseDate().isBefore(toDate.plusDays(1))));

        // Test filtering by user score
        Float minUsersScore = 8.0f;
        Float maxUsersScore = 10.0f;
        GameFilter scoreFilter = new GameFilter();
        scoreFilter.setMinUserScore(minUsersScore);
        scoreFilter.setMaxUserScore(maxUsersScore);

        result = gameService.getAllGames(scoreFilter, pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream().allMatch(game -> game.getUsersScore() >= minUsersScore && game.getUsersScore() <= maxUsersScore));

        // Test filtering by tags
        List<Long> tagIds = List.of(1L);
        GameFilter tagFilter = new GameFilter();
        tagFilter.setTagIds(tagIds);

        result = gameService.getAllGames(tagFilter, pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream().allMatch(game -> game.getTags().stream().anyMatch(tag -> tagIds.contains(tag.getId()))));

        // Test filtering by release status
        List<ReleaseStatus> releaseStatuses = List.of(ReleaseStatus.RELEASED);
        GameFilter statusFilter = new GameFilter();
        statusFilter.setReleaseStatuses(releaseStatuses);

        result = gameService.getAllGames(statusFilter, pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream().allMatch(game -> releaseStatuses.contains(game.getReleaseStatus())));

        // Test filtering by date range, user score, tags, and release status
        GameFilter combinedFilter = new GameFilter();
        combinedFilter.setFromDate(fromDate);
        combinedFilter.setToDate(toDate);
        combinedFilter.setMinUserScore(minUsersScore);
        combinedFilter.setMaxUserScore(maxUsersScore);
        combinedFilter.setTagIds(tagIds);
        combinedFilter.setReleaseStatuses(releaseStatuses);

        result = gameService.getAllGames(combinedFilter, pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().stream().allMatch(game -> game.getReleaseDate().isAfter(fromDate.minusDays(1)) && game.getReleaseDate().isBefore(toDate.plusDays(1)) && game.getUsersScore() >= minUsersScore && game.getUsersScore() <= maxUsersScore && game.getTags().stream().anyMatch(tag -> tagIds.contains(tag.getId())) && releaseStatuses.contains(game.getReleaseStatus())));
    }

}
