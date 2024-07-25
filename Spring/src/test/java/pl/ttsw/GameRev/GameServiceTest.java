package pl.ttsw.GameRev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ReleaseStatusRepository statusRepository;

    @Mock
    private TagRepository tagRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Game createGameForTesting(){
        Game game = new Game();
        game.setId(1L);
        game.setTitle("Limbus Company");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        ReleaseStatus releaseStatus = new ReleaseStatus();
        releaseStatus.setId(1L);
        releaseStatus.setStatusName("Released");
        game.setReleaseStatus(releaseStatus);
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setTagName("RPG");
        game.setTags(new ArrayList<>(List.of(tag)));
        return game;
    }

    @Test
    public void testCreateGame() {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setTitle("Limbus Company");
        gameDTO.setDeveloper("Project Moon");
        gameDTO.setPublisher("Project Moon");
        gameDTO.setReleaseDate(LocalDate.now());
        gameDTO.setDescription("Nice game");

        ReleaseStatusDTO releaseStatusDTO = new ReleaseStatusDTO();
        releaseStatusDTO.setId(1L);
        releaseStatusDTO.setStatusName("Released");
        gameDTO.setReleaseStatus(releaseStatusDTO);

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setTagName("RPG");
        gameDTO.setTags(new ArrayList<>(List.of(tagDTO)));

        ReleaseStatus releaseStatus = new ReleaseStatus();
        releaseStatus.setId(1L);
        when(statusRepository.findReleaseStatusById(1L)).thenReturn(releaseStatus);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setTagName("RPG");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Game gameToReturn = createGameForTesting();
        when(gameRepository.save(any(Game.class))).thenReturn(gameToReturn);
        Game createdGame = gameService.createGame(gameDTO);

        assertNotNull(createdGame);
        assertEquals("Limbus Company", createdGame.getTitle());
        assertEquals("Project Moon", createdGame.getDeveloper());
        assertEquals("Project Moon", createdGame.getPublisher());
        assertEquals("Nice game", createdGame.getDescription());
        assertEquals(1L, createdGame.getReleaseStatus().getId());
        assertEquals("Released", createdGame.getReleaseStatus().getStatusName());
        assertEquals(1, createdGame.getTags().size());
        assertEquals(1L, createdGame.getTags().get(0).getId());
        assertEquals("RPG", createdGame.getTags().get(0).getTagName());

        verify(statusRepository, times(1)).findReleaseStatusById(1L);
        verify(tagRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    public void testGetGameByTitle() {
        Game game = createGameForTesting();
        GameDTO expectedGameDTO = gameService.mapToDTO(game);
        when(gameRepository.findGameByTitle("Limbus Company")).thenReturn(game);
        GameDTO actualGameDTO = gameService.getGameByTitle("Limbus Company");

        assertNotNull(actualGameDTO);
        assertEquals(expectedGameDTO.getTitle(), actualGameDTO.getTitle());
        assertEquals(expectedGameDTO.getDeveloper(), actualGameDTO.getDeveloper());
        assertEquals(expectedGameDTO.getPublisher(), actualGameDTO.getPublisher());
        assertEquals(expectedGameDTO.getReleaseDate(), actualGameDTO.getReleaseDate());
        assertEquals(expectedGameDTO.getDescription(), actualGameDTO.getDescription());
        assertEquals(expectedGameDTO.getReleaseStatus().getId(), actualGameDTO.getReleaseStatus().getId());
        assertEquals(expectedGameDTO.getReleaseStatus().getStatusName(), actualGameDTO.getReleaseStatus().getStatusName());
        assertEquals(expectedGameDTO.getTags().size(), actualGameDTO.getTags().size());
        assertEquals(expectedGameDTO.getTags().get(0).getId(), actualGameDTO.getTags().get(0).getId());
        assertEquals(expectedGameDTO.getTags().get(0).getTagName(), actualGameDTO.getTags().get(0).getTagName());

        verify(gameRepository, times(1)).findGameByTitle("Limbus Company");
    }

    @Test
    public void testGetGameById() {
        Game game = createGameForTesting();
        when(gameRepository.findGameById(1L)).thenReturn(game);
        GameDTO gameDTO = gameService.getGameById(1L);

        assertNotNull(gameDTO);
        assertEquals(1L, gameDTO.getId());
        verify(gameRepository, times(1)).findGameById(1L);
    }


    @Test
    public void testUpdateGame() {
        Game existingGame = createGameForTesting();
        GameDTO updateGameDTO = new GameDTO();
        updateGameDTO.setTitle("Limbus Company Updated");
        updateGameDTO.setDeveloper("Project Moon Updated");
        updateGameDTO.setPublisher("Project Moon Updated");
        updateGameDTO.setReleaseDate(LocalDate.of(2024, 1, 1));
        updateGameDTO.setDescription("Updated description");

        ReleaseStatusDTO updatedStatusDTO = new ReleaseStatusDTO();
        updatedStatusDTO.setId(1L);
        updatedStatusDTO.setStatusName("Updated");
        updateGameDTO.setReleaseStatus(updatedStatusDTO);

        TagDTO updatedTagDTO = new TagDTO();
        updatedTagDTO.setId(1L);
        updatedTagDTO.setTagName("Adventure");
        updateGameDTO.setTags(new ArrayList<>(List.of(updatedTagDTO)));

        Game updatedGame = createGameForTesting();
        updatedGame.setTitle("Limbus Company Updated");
        updatedGame.setDeveloper("Project Moon Updated");
        updatedGame.setPublisher("Project Moon Updated");
        updatedGame.setReleaseDate(LocalDate.of(2024, 1, 1));
        updatedGame.setDescription("Updated description");
        ReleaseStatus updatedStatus = new ReleaseStatus();
        updatedStatus.setId(1L);
        updatedStatus.setStatusName("Updated");
        updatedGame.setReleaseStatus(updatedStatus);
        Tag updatedTag = new Tag();
        updatedTag.setId(1L);
        updatedTag.setTagName("Adventure");
        updatedGame.setTags(new ArrayList<>(List.of(updatedTag)));

        when(gameRepository.findGameByTitle("Limbus Company")).thenReturn(existingGame);
        when(statusRepository.findById(1L)).thenReturn(Optional.of(updatedStatus));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(updatedTag));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        GameDTO resultGameDTO = gameService.updateGame("Limbus Company", updateGameDTO);
        assertNotNull(resultGameDTO);
        assertEquals("Limbus Company Updated", resultGameDTO.getTitle());
        assertEquals("Project Moon Updated", resultGameDTO.getDeveloper());
        assertEquals("Project Moon Updated", resultGameDTO.getPublisher());
        assertEquals(LocalDate.of(2024, 1, 1), resultGameDTO.getReleaseDate());
        assertEquals("Updated description", resultGameDTO.getDescription());
        assertEquals(1L, resultGameDTO.getReleaseStatus().getId());
        assertEquals("Updated", resultGameDTO.getReleaseStatus().getStatusName());
        assertEquals(1, resultGameDTO.getTags().size());
        assertEquals(1L, resultGameDTO.getTags().get(0).getId());
        assertEquals("Adventure", resultGameDTO.getTags().get(0).getTagName());

        verify(gameRepository, times(1)).findGameByTitle("Limbus Company");
        verify(statusRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }


    @Test
    public void testDeleteGame() {
        when(gameRepository.existsById(1L)).thenReturn(true);

        boolean result = gameService.deleteGame(1L);

        assertTrue(result);
        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteGameNotFound() {
        when(gameRepository.existsById(1L)).thenReturn(false);

        boolean result = gameService.deleteGame(1L);

        assertFalse(result);
        verify(gameRepository, never()).deleteById(1L);
    }
}
