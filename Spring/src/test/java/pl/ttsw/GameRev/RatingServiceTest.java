package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.ttsw.GameRev.dto.RatingDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.RatingMapper;
import pl.ttsw.GameRev.model.Rating;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.RatingRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.service.RatingService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class RatingServiceTest {

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateRating_CreateNewRating() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setOwnRatingIsPositive(true);

        UserReview userReview = new UserReview();
        WebsiteUser currentUser = new WebsiteUser();
        Rating rating = new Rating();

        when(userReviewRepository.findById(anyLong())).thenReturn(userReview);
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(ratingRepository.findByUserAndUserReview(any(), any())).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(new RatingDTO());

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNotNull(result);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    public void testUpdateRating_UpdateExistingRating() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setOwnRatingIsPositive(false);

        UserReview userReview = new UserReview();
        WebsiteUser currentUser = new WebsiteUser();
        Rating rating = new Rating();

        when(userReviewRepository.findById(anyLong())).thenReturn(userReview);
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(ratingRepository.findByUserAndUserReview(any(), any())).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(new RatingDTO());

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNotNull(result);
        assertEquals(false, rating.getIsPositive());
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    public void testUpdateRating_ReviewDoesNotExist() {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);

        when(userReviewRepository.findById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            ratingService.updateRating(userReviewDTO);
        });

        assertEquals("This review doesnt exist", exception.getMessage());
        verify(ratingRepository, never()).findByUserAndUserReview(any(), any());
    }

    @Test
    public void testUpdateRating_DeleteRating() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setOwnRatingIsPositive(null);

        UserReview userReview = new UserReview();
        WebsiteUser currentUser = new WebsiteUser();
        Rating rating = new Rating();

        when(userReviewRepository.findById(anyLong())).thenReturn(userReview);
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(ratingRepository.findByUserAndUserReview(any(), any())).thenReturn(Optional.of(rating));

        RatingDTO result = ratingService.updateRating(userReviewDTO);

        assertNull(result);
        verify(ratingRepository).delete(rating);
    }
}