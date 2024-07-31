package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.*;
import pl.ttsw.GameRev.service.UserReviewService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserReviewServiceIntegrationTest {

    private final Pageable pageable = PageRequest.ofSize(10);
    @Autowired
    private UserReviewService userReviewService;
    @Autowired
    private UserReviewRepository userReviewRepository;
    @Autowired
    private WebsiteUserRepository websiteUserRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private ReleaseStatusRepository releaseStatusRepository;
    @Autowired
    private TagRepository tagRepository;
    private Game game;
    private WebsiteUser testUser;

    @BeforeEach
    public void setup() {
        game = gameRepository.findGameByTitle("Limbus Company 2");
        if (game != null) {
            gameRepository.delete(game);
        }
        testUser = websiteUserRepository.findByUsername("testuser");
        assertNotNull(testUser, "Test user should already exist in the database");

        game = createGameForTesting();
        game = gameRepository.save(game);
    }

    @AfterEach
    public void teardown() {
        Page<UserReview> reviews = userReviewRepository.findByUser(testUser, pageable);
        for (UserReview review : reviews) {
            userReviewRepository.delete(review);
        }
        game = gameRepository.findGameByTitle("Limbus Company 2");
        if (game != null) {
            gameRepository.delete(game);
        }
    }

    private Game createGameForTesting() {
        Game game = new Game();
        game.setTitle("Limbus Company 2");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        game.setReleaseStatus(releaseStatusRepository.findReleaseStatusById(1L));
        game.setTags(List.of(tagRepository.findById(1L).orElseThrow(RuntimeException::new)));
        return game;
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testCreateAndRetrieveUserReview() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setUserUsername("testuser");
        userReviewDTO.setGameTitle(game.getTitle());
        userReviewDTO.setContent("Great game!");
        userReviewDTO.setScore(9);

        UserReviewDTO createdReview = userReviewService.createUserReview(userReviewDTO);
        assertNotNull(createdReview);
        assertEquals("Great game!", createdReview.getContent());

        Page<UserReviewDTO> userReviews = userReviewService.getUserReviewByGame(game.getTitle(), PageRequest.of(0, 10));
        assertFalse(userReviews.isEmpty());
        assertEquals(1, userReviews.getTotalElements());

        Page<UserReviewDTO> userReviewsByUser = userReviewService.getUserReviewByUser(testUser.getId(), pageable);
        assertFalse(userReviewsByUser.isEmpty());
        assertEquals(1, userReviewsByUser.getTotalElements());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testUpdateUserReview() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setUserUsername("testuser");
        userReviewDTO.setGameTitle(game.getTitle());
        userReviewDTO.setContent("Great game!");
        userReviewDTO.setScore(9);

        UserReviewDTO createdReview = userReviewService.createUserReview(userReviewDTO);
        assertNotNull(createdReview);

        createdReview.setContent("Awesome game!");
        createdReview.setScore(10);
        UserReviewDTO updatedReview = userReviewService.updateUserReview(createdReview);

        assertNotNull(updatedReview);
        assertEquals("Awesome game!", updatedReview.getContent());
        assertEquals(10, updatedReview.getScore());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testDeleteUserReview() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setUserUsername("testuser");
        userReviewDTO.setGameTitle(game.getTitle());
        userReviewDTO.setContent("Great game!");
        userReviewDTO.setScore(9);

        UserReviewDTO createdReview = userReviewService.createUserReview(userReviewDTO);
        assertNotNull(createdReview);

        boolean deleted = userReviewService.deleteUserReviewByOwner(createdReview);
        assertTrue(deleted);

        assertThrows(BadRequestException.class, () -> {
            userReviewService.getUserReviewByUser(testUser.getId(), pageable);
        });
    }
}
