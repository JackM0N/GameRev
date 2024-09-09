package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.filter.UserReviewFilter;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.UserReviewService;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserReviewServiceIntegrationTest {

    @Autowired
    private UserReviewService userReviewService;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TagRepository tagRepository;

    private Game game;
    private WebsiteUser testUser;
    private final Pageable pageable = PageRequest.ofSize(10);

    @BeforeEach
    public void setup() {
        testUser = websiteUserRepository.findByUsername("testuser").get();
        assertNotNull(testUser, "Test user should already exist in the database");

        game = createGameForTesting();
        game = gameRepository.save(game);
    }

    private Game createGameForTesting() {
        Game game = new Game();
        game.setTitle("Limbus Company 2");
        game.setDeveloper("Project Moon");
        game.setPublisher("Project Moon");
        game.setReleaseDate(LocalDate.now());
        game.setDescription("Nice game");
        game.setReleaseStatus(ReleaseStatus.RELEASED);
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
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        UserReviewDTO createdReview = userReviewService.createUserReview(userReviewDTO);
        assertNotNull(createdReview);
        assertEquals("Great game!", createdReview.getContent());


        Page<UserReviewDTO> userReviews = userReviewService.getUserReviewByGame(game.getTitle(), userReviewFilter, PageRequest.of(0, 10));
        assertFalse(userReviews.isEmpty());
        assertEquals(1, userReviews.getTotalElements());

        Page<UserReviewDTO> userReviewsByUser = userReviewService.getUserReviewByUser(testUser.getId(), userReviewFilter, pageable);
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
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        UserReviewDTO createdReview = userReviewService.createUserReview(userReviewDTO);
        assertNotNull(createdReview);

        boolean deleted = userReviewService.deleteUserReviewByOwner(createdReview);
        assertTrue(deleted);

        Page<UserReviewDTO> shouldBeDeleted = userReviewService.getUserReviewByUser(testUser.getId(), userReviewFilter, pageable);
        for (UserReviewDTO review : shouldBeDeleted.getContent()) {
            assertNotEquals(createdReview.getGameTitle(), review.getGameTitle());
        }
    }
}
