package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.RatingDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.RatingMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Rating;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.RatingRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.RatingService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RatingServiceIntegrationTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserReviewRepository userReviewRepository;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private GameRepository gameRepository;

    private WebsiteUser testUser;
    private WebsiteUser testUser2;
    private UserReview testUserReview;
    private Game game;

    @BeforeEach
    public void setup() {
        teardown();
        testUser = websiteUserRepository.findByUsername("testuser");
        testUser2 = websiteUserRepository.findByUsername("testadmin");
        game = gameRepository.findGameByTitle("Limbus Company");
        testUserReview = new UserReview();
        testUserReview.setUser(testUser2);
        testUserReview.setGame(game);
        testUserReview.setContent("Great game!");
        testUserReview.setScore(7);
        testUserReview.setPostDate(LocalDate.now());
        testUserReview = userReviewRepository.save(testUserReview);
    }

    @AfterEach
    public void teardown() {
        Optional<Rating> rating = ratingRepository.findByUserAndUserReview(testUser, testUserReview);
        if (rating.isPresent()) {
            ratingRepository.delete(rating.get());
        }
        if (testUserReview != null) {
            userReviewRepository.delete(testUserReview);
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRating_CreateNewRating() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(testUserReview.getId());
        userReviewDTO.setOwnRatingIsPositive(true);

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNotNull(result);
        assertTrue(result.getIsPositive());

        Rating savedRating = ratingRepository.findByUserAndUserReview(testUser, testUserReview)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found"));
        assertEquals(true, savedRating.getIsPositive());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRating_UpdateExistingRating() throws BadRequestException {
        Rating initialRating = new Rating();
        initialRating.setUser(testUser);
        initialRating.setUserReview(testUserReview);
        initialRating.setIsPositive(true);
        initialRating = ratingRepository.save(initialRating);

        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(testUserReview.getId());
        userReviewDTO.setOwnRatingIsPositive(false);

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNotNull(result);
        assertFalse(result.getIsPositive());

        Rating updatedRating = ratingRepository.findByUserAndUserReview(testUser, testUserReview)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found"));
        assertEquals(false, updatedRating.getIsPositive());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRating_DeleteRating() throws BadRequestException {
        Rating initialRating = new Rating();
        initialRating.setUser(testUser);
        initialRating.setUserReview(testUserReview);
        initialRating.setIsPositive(true);
        initialRating = ratingRepository.save(initialRating);

        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(testUserReview.getId());
        userReviewDTO.setOwnRatingIsPositive(null);

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNull(result);
        Rating finalInitialRating = initialRating;
        assertThrows(EmptyResultDataAccessException.class, () -> {
            ratingRepository.findById(finalInitialRating.getId())
                    .orElseThrow(() -> new EmptyResultDataAccessException("Rating not found", 1));
        });
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRating_ReviewDoesNotExist() {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(999L);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            ratingService.updateRating(userReviewDTO);
        });

        assertEquals("This review doesnt exist", exception.getMessage());
    }
}
