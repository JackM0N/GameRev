package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.enums.CompletionStatus;
import pl.ttsw.GameRev.filter.UserGameFilter;
import pl.ttsw.GameRev.mapper.UserGameMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserGame;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserGameRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.UserGameService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameRepository userGameRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private UserGameMapper userGameMapper;

    @Mock
    private WebsiteUserRepository websiteUserRepository;

    @InjectMocks
    private UserGameService userGameService;

    private WebsiteUser user;
    private Game game;
    private UserGame userGame;
    private UserGameDTO userGameDTO;

    @BeforeEach
    public void setUp() {
        user = new WebsiteUser();
        user.setUsername("testuser");

        WebsiteUserDTO userDTO = new WebsiteUserDTO();
        userDTO.setUsername("testuser");

        game = new Game();
        game.setId(1L);

        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(1L);

        userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setCompletionStatus(CompletionStatus.IN_PROGRESS);

        userGameDTO = new UserGameDTO(1L, gameDTO, userDTO, CompletionStatus.IN_PROGRESS, false);
    }

    @Test
    public void testGetUserGame_Success() throws BadRequestException {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserGame> userGamePage = new PageImpl<>(Collections.singletonList(userGame));
        UserGameFilter userGameFilter = new UserGameFilter();

        when(websiteUserRepository.findByNickname("testuser")).thenReturn(Optional.ofNullable(user));
        when(userGameRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userGamePage);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        Page<UserGameDTO> result = userGameService.getUserGame("testuser", userGameFilter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetUserGameDTO_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(websiteUserRepository.findByNickname("nonexistent")).thenReturn(Optional.empty());
        UserGameFilter userGameFilter = new UserGameFilter();

        assertThrows(BadRequestException.class, () -> userGameService.getUserGame("nonexistent", userGameFilter, pageable));
    }

    @Test
    public void testGetUserGame_EmptyLibrary() throws BadRequestException {
        Pageable pageable = PageRequest.of(0, 10);
        when(websiteUserRepository.findByNickname("testuser")).thenReturn(Optional.ofNullable(user));
        when(userGameRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());
        UserGameFilter userGameFilter = new UserGameFilter();

        Page<UserGameDTO> result = userGameService.getUserGame("testuser", userGameFilter, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testAddGameToUser_Success() throws BadRequestException {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(user));
        when(gameRepository.findGameById(anyLong())).thenReturn(Optional.ofNullable(game));
        when(userGameRepository.save(any(UserGame.class))).thenReturn(userGame);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        UserGameDTO result = userGameService.addGameToUser(userGameDTO);

        assertNotNull(result);
        verify(userGameRepository, times(1)).save(any(UserGame.class));
    }

    @Test
    public void testAddGameToUser_UserNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    public void testAddGameToUser_GameNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(user));
        when(gameRepository.findGameById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    public void testUpdateGame_Success() throws BadRequestException {
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.of(userGame));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(userGameRepository.save(any(UserGame.class))).thenReturn(userGame);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        userGameDTO.setCompletionStatus(CompletionStatus.COMPLETED);
        UserGameDTO result = userGameService.updateGame(userGameDTO);

        assertNotNull(result);
        assertEquals(CompletionStatus.COMPLETED, result.getCompletionStatus());
        verify(userGameRepository, times(1)).save(any(UserGame.class));
    }

    @Test
    public void testUpdateGame_GameNotFound() {
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userGameService.updateGame(userGameDTO));
    }

    @Test
    public void testUpdateGame_NotLoggedIn() {
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.of(userGame));
        when(websiteUserService.getCurrentUser()).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> userGameService.updateGame(userGameDTO));
    }

    @Test
    public void testUpdateGame_NotOwner() {
        WebsiteUser anotherUser = new WebsiteUser();
        anotherUser.setUsername("anotheruser");

        when(userGameRepository.findById(anyLong())).thenReturn(Optional.of(userGame));
        when(websiteUserService.getCurrentUser()).thenReturn(anotherUser);

        assertThrows(BadCredentialsException.class, () -> userGameService.updateGame(userGameDTO));
    }

    @Test
    public void testDeleteGame_Success() throws BadRequestException {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.of(userGame));

        boolean result = userGameService.deleteGame(userGameDTO.getId());

        assertTrue(result);
        verify(userGameRepository, times(1)).delete(any(UserGame.class));
    }

    @Test
    public void testDeleteGame_GameNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userGameService.deleteGame(userGameDTO.getId()));
    }
}
