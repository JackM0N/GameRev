package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.UserGameDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserGameServiceIntegrationTest {

    @Autowired
    private UserGameService userGameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    private UserGameMapper userGameMapper;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    private WebsiteUser testUser;
    private Game game;
    private UserGame userGame;
    private UserGameDTO userGameDTO;
    private final Pageable pageable = PageRequest.ofSize(10);

    @BeforeEach
    public void setUp() {
        testUser = websiteUserRepository.findByUsername("testuser").get();
        game = gameRepository.findGameByTitle("Limbus Company").get();

        userGame = new UserGame();
        userGame.setUser(testUser);
        userGame.setGame(game);
        userGame.setCompletionStatus(CompletionStatus.IN_PROGRESS);
        userGame.setIsFavourite(false);
        userGameDTO = userGameMapper.toDto(userGame);

        assertNotNull(testUser);
        assertNotNull(game);
        assertNotNull(userGameDTO);
    }

    @Test
    @Transactional
    public void testGetUserGame_Success() throws BadRequestException {
        WebsiteUser existingUser = websiteUserRepository.findById(4L).orElse(null);
        assertNotNull(existingUser);
        UserGameFilter userGameFilter = new UserGameFilter();

        Page<UserGameDTO> result = userGameService.getUserGame(existingUser.getNickname(), userGameFilter, pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
    }

    @Test
    @Transactional
    public void testGetUserGameDTO_UserNotFound() {
        UserGameFilter userGameFilter = new UserGameFilter();
        assertThrows(BadRequestException.class, () -> userGameService.getUserGame("nosuchuser", userGameFilter, pageable));
    }

    @Test
    @Transactional
    public void testGetUserGame_EmptyLibrary() throws BadRequestException {
        userGameRepository.findByUserNickname("testcritic", pageable);
        UserGameFilter userGameFilter = new UserGameFilter();

        Page<UserGameDTO> result = userGameService.getUserGame("testcritic", userGameFilter, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testAddGameToUser_Success() throws BadRequestException {
        UserGame userGame = new UserGame();
        userGame.setUser(testUser);
        userGame.setGame(game);
        userGame.setCompletionStatus(CompletionStatus.IN_PROGRESS);
        userGame.setIsFavourite(false);

        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);
        UserGameDTO result = userGameService.addGameToUser(userGameDTO);

        assertNotNull(result);
        assertEquals("Limbus Company", result.getGame().getTitle());
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void testAddGameToUser_NotLoggedIn() {
        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);

        assertThrows(BadCredentialsException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testUpdateGame_NotOwner() {
        WebsiteUser anotherUser = websiteUserRepository.findById(6L).orElseThrow();
        anotherUser = websiteUserRepository.save(anotherUser);

        userGame.setUser(anotherUser);
        userGame = userGameRepository.save(userGame);

        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);

        assertThrows(BadCredentialsException.class, () -> userGameService.updateGame(userGameDTO));
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testDeleteGame_Success() throws BadRequestException {
        UserGameDTO savedGame = userGameService.addGameToUser(userGameDTO);

        boolean result = userGameService.deleteGame(savedGame.getId());

        assertTrue(result);
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void testDeleteGame_NotLoggedIn() {
        assertThrows(BadCredentialsException.class, () -> userGameService.deleteGame(userGame.getId()));
    }
}
