package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import pl.ttsw.GameRev.service.GameService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private TagRepository tagRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Game createGameForTesting() {
        Game game = new Game();
        game.setId(1L);
        game.setTitle("Limbus Company");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        game.setReleaseStatus(ReleaseStatus.RELEASED);
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setTagName("RPG");
        game.setTags(new ArrayList<>(List.of(tag)));
        return game;
    }

    @Test
    public void testGetAllGames() {
        Pageable pageable = PageRequest.of(0, 10);
        Game game = createGameForTesting();
        GameDTO gameDTO = new GameDTO();
        Page<Game> gamesPage = new PageImpl<>(Collections.singletonList(game));

        when(gameRepository.findAll(pageable)).thenReturn(gamesPage);
        when(gameMapper.toDto(game)).thenReturn(gameDTO);

        Page<GameDTO> result = gameService.getAllGames(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(gameRepository, times(1)).findAll(pageable);
        verify(gameMapper, times(1)).toDto(game);
    }

    @Test
    public void testCreateGame() throws BadRequestException {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setTitle("Limbus Company");
        gameDTO.setDeveloper("Project Moon");
        gameDTO.setPublisher("Project Moon");
        gameDTO.setReleaseDate(LocalDate.now());
        gameDTO.setDescription("Nice game");
        gameDTO.setReleaseStatus(ReleaseStatus.RELEASED);
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        gameDTO.setTags(Collections.singletonList(tagDTO));

        Game game = createGameForTesting();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(game.getTags().get(0)));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        when(gameMapper.toDto(any(Game.class))).thenReturn(gameDTO);

        GameDTO result = gameService.createGame(gameDTO);

        assertNotNull(result);
        verify(tagRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    public void testGetGameByTitle() throws BadRequestException {
        Game game = createGameForTesting();
        GameDTO gameDTO = new GameDTO();

        when(gameRepository.findGameByTitle("Limbus Company")).thenReturn(Optional.of(game));
        when(gameMapper.toDto(game)).thenReturn(gameDTO);

        GameDTO result = gameService.getGameByTitle("Limbus Company");

        assertNotNull(result);
        verify(gameRepository, times(1)).findGameByTitle("Limbus Company");
        verify(gameMapper, times(1)).toDto(game);
    }

    @Test
    void testUpdateGame() throws BadRequestException {
        Game game = createGameForTesting();
        GameDTO gameDTO = new GameDTO();
        gameDTO.setTitle("Updated Title");

        when(gameRepository.findGameByTitle("Limbus Company")).thenReturn(Optional.of(game));
        when(gameRepository.save(game)).thenReturn(game);
        when(gameMapper.toDto(game)).thenReturn(gameDTO);

        GameDTO result = gameService.updateGame("Limbus Company", gameDTO);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(gameRepository, times(1)).findGameByTitle("Limbus Company");
        verify(gameRepository, times(1)).save(game);
        verify(gameMapper, times(1)).toDto(game);
    }

    @Test
    public void testDeleteGame() {
        when(gameRepository.existsById(1L)).thenReturn(true);

        boolean result = gameService.deleteGame(1L);

        assertTrue(result);
        verify(gameRepository, times(1)).existsById(1L);
        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteGameNotExists() {
        when(gameRepository.existsById(1L)).thenReturn(false);

        boolean result = gameService.deleteGame(1L);

        assertFalse(result);
        verify(gameRepository, times(1)).existsById(1L);
        verify(gameRepository, times(0)).deleteById(1L);
    }
}
