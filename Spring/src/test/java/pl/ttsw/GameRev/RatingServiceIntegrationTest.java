package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.RatingDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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
    private GameRepository gameRepository;

    private WebsiteUser testUser;
    private WebsiteUser testUser2;
    private UserReview testUserReview;
    private Game game;

    @BeforeEach
    public void setup() {
        try {
            testUser = websiteUserRepository.findByUsername("testuser").get();
            testUser2 = websiteUserRepository.findByUsername("testadmin").get();
            game = gameRepository.findGameByTitle("Limbus Company").get();
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Test data not found");
        }
        testUserReview = new UserReview();
        testUserReview.setUser(testUser2);
        testUserReview.setGame(game);
        testUserReview.setContent("Great game!");
        testUserReview.setScore(7);
        testUserReview.setPostDate(LocalDate.now());
        testUserReview = userReviewRepository.save(testUserReview);
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testUpdateRating_CreateNewRating() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(testUserReview.getId());
        userReviewDTO.setOwnRatingIsPositive(true);

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNotNull(result);
        assertTrue(result.getIsPositive());

        Rating savedRating = ratingRepository.findByUserAndUserReview(testUser, testUserReview).orElseThrow(() -> new EntityNotFoundException("Rating not found"));
        assertEquals(true, savedRating.getIsPositive());
    }

    @Test
    @Transactional
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

        Rating updatedRating = ratingRepository.findByUserAndUserReview(testUser, testUserReview).orElseThrow(() -> new EntityNotFoundException("Rating not found"));
        assertEquals(false, updatedRating.getIsPositive());
    }

    @Test
    @Transactional
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
            ratingRepository.findById(finalInitialRating.getId()).orElseThrow(() -> new EmptyResultDataAccessException("Rating not found", 1));
        });
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    public void testUpdateRating_ReviewDoesNotExist() {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(999L);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            ratingService.updateRating(userReviewDTO);
        });

        assertEquals("User review not found", exception.getMessage());
    }
}
