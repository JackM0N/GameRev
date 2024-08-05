package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.UserGameDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserGameServiceIntegrationTest {
    private final Pageable pageable = PageRequest.ofSize(10);

    @Autowired
    private UserGameService userGameService;

    @Autowired
    private CompletionStatusRepository completionStatusRepository;

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
    private CompletionStatus completionStatus;
    private UserGame userGame;
    private UserGameDTO userGameDTO;

    @BeforeEach
    public void setUp() {
        testUser = websiteUserRepository.findByUsername("testuser");
        game = gameRepository.findGameByTitle("Limbus Company");
        completionStatus = completionStatusRepository.findById(1L).orElse(null);

        userGame = new UserGame();
        userGame.setUser(testUser);
        userGame.setGame(game);
        userGame.setCompletionStatus(completionStatus);
        userGame.setIsFavourite(false);
        userGame.setId(1L);
        userGameDTO = userGameMapper.toDto(userGame);

        assertNotNull(testUser);
        assertNotNull(game);
        assertNotNull(completionStatus);
        assertNotNull(userGameDTO);
    }

    @AfterEach
    public void tearDown() {
        Page<UserGame> gamesToDelete = userGameRepository.findByUserNickname("testuser", pageable);
        userGameRepository.deleteAll(gamesToDelete.getContent());
    }

    @Test
    public void testGetUserGameDTO_Success() throws BadRequestException {
        Pageable pageable = PageRequest.of(0, 10);
        WebsiteUser existingUser = websiteUserRepository.findById(4L).orElse(null);
        assertNotNull(existingUser);

        Page<UserGameDTO> result = userGameService.getUserGameDTO(existingUser.getNickname(), pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
    }

    @Test
    public void testGetUserGameDTO_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(BadRequestException.class, () -> userGameService.getUserGameDTO("nosuchuser", pageable));
    }

    @Test
    public void testGetUserGameDTO_EmptyLibrary() {
        Pageable pageable = PageRequest.of(0, 10);
        userGameRepository.findByUserNickname("testcritic", pageable);

        assertThrows(BadRequestException.class, () -> userGameService.getUserGameDTO("testcritic", pageable));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAddGameToUser_Success() throws BadRequestException {
        UserGame userGame = new UserGame();
        userGame.setUser(testUser);
        userGame.setGame(game);
        userGame.setCompletionStatus(completionStatus);
        userGame.setIsFavourite(false);

        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);
        UserGameDTO result = userGameService.addGameToUser(userGameDTO);

        assertNotNull(result);
        assertEquals("Limbus Company", result.getGame().getTitle());
    }

    @Test
    @WithMockUser()
    public void testAddGameToUser_NotLoggedIn() {
        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);

        assertThrows(BadCredentialsException.class, () -> userGameService.addGameToUser(userGameDTO));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateGame_NotOwner() {
        WebsiteUser anotherUser = websiteUserRepository.findById(4L).orElseThrow();
        anotherUser = websiteUserRepository.save(anotherUser);

        userGame.setUser(anotherUser);
        userGame = userGameRepository.save(userGame);

        UserGameDTO userGameDTO = userGameMapper.toDto(userGame);

        assertThrows(BadCredentialsException.class, () -> userGameService.updateGame(userGameDTO));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteGame_Success() throws BadRequestException {
        UserGameDTO savedGame = userGameService.addGameToUser(userGameDTO);

        boolean result = userGameService.deleteGame(savedGame.getId());

        assertTrue(result);
    }

    @Test
    @WithMockUser()
    public void testDeleteGame_NotLoggedIn() {
        assertThrows(BadCredentialsException.class, () -> userGameService.deleteGame(userGame.getId()));
    }
}
