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
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.CompletionStatusDTO;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.mapper.UserGameMapper;
import pl.ttsw.GameRev.model.CompletionStatus;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserGame;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.CompletionStatusRepository;
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
    private CompletionStatusRepository completionStatusRepository;

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
    private CompletionStatus completionStatus;
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

        completionStatus = new CompletionStatus();
        completionStatus.setId(1L);
        completionStatus.setCompletionName("Finished");

        CompletionStatusDTO completionStatusDTO = new CompletionStatusDTO(1L, "Finished");

        userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setCompletionStatus(completionStatus);

        userGameDTO = new UserGameDTO(1L, gameDTO, userDTO, completionStatusDTO, false);
    }

    @Test
    public void testGetUserGameDTO_Success() throws BadRequestException {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserGame> userGamePage = new PageImpl<>(Collections.singletonList(userGame));

        when(websiteUserRepository.findByNickname("testuser")).thenReturn(user);
        when(userGameRepository.findByUserNickname("testuser", pageable)).thenReturn(userGamePage);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        Page<UserGameDTO> result = userGameService.getUserGameDTO("testuser", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetUserGameDTO_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(websiteUserRepository.findByNickname("nonexistent")).thenReturn(null);

        assertThrows(BadRequestException.class, () -> userGameService.getUserGameDTO("nonexistent", pageable));
    }

    @Test
    public void testGetUserGameDTO_EmptyLibrary() {
        Pageable pageable = PageRequest.of(0, 10);
        when(websiteUserRepository.findByNickname("testuser")).thenReturn(user);
        when(userGameRepository.findByUserNickname("testuser", pageable)).thenReturn(Page.empty());

        assertThrows(BadRequestException.class, () -> userGameService.getUserGameDTO("testuser", pageable));
    }

    @Test
    public void testAddGameToUser_Success() throws BadRequestException {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(user);
        when(gameRepository.findGameById(anyLong())).thenReturn(game);
        when(completionStatusRepository.findById(anyLong())).thenReturn(Optional.of(completionStatus));
        when(userGameRepository.save(any(UserGame.class))).thenReturn(userGame);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        UserGameDTO result = userGameService.addGameToUser(userGameDTO);

        assertNotNull(result);
        verify(userGameRepository, times(1)).save(any(UserGame.class));
    }

    @Test
    public void testAddGameToUser_NotLoggedIn() {
        when(websiteUserService.getCurrentUser()).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    public void testAddGameToUser_UserNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(null);

        assertThrows(BadRequestException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    public void testAddGameToUser_GameNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(websiteUserRepository.findByUsername(anyString())).thenReturn(user);
        when(gameRepository.findGameById(anyLong())).thenReturn(null);

        assertThrows(BadRequestException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    public void testUpdateGame_Success() throws BadRequestException {
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.of(userGame));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(completionStatusRepository.findById(anyLong())).thenReturn(Optional.of(completionStatus));
        when(userGameRepository.save(any(UserGame.class))).thenReturn(userGame);
        when(userGameMapper.toDto(any(UserGame.class))).thenReturn(userGameDTO);

        UserGameDTO result = userGameService.updateGame(userGameDTO);

        assertNotNull(result);
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
    public void testDeleteGame_NotLoggedIn() {
        when(websiteUserService.getCurrentUser()).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> userGameService.deleteGame(userGameDTO.getId()));
    }

    @Test
    public void testDeleteGame_GameNotFound() {
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(userGameRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userGameService.deleteGame(userGameDTO.getId()));
    }
}