package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.UserReviewMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.RatingRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.UserReviewService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserReviewServiceTest {

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private WebsiteUserRepository websiteUserRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private UserReviewMapper userReviewMapper;

    @InjectMocks
    private UserReviewService userReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserReviewByGame() {
        String gameTitle = "Cimbus Lompany";
        Pageable pageable = mock(Pageable.class);
        UserReview userReview = new UserReview();
        Page<UserReview> userReviews = new PageImpl<>(Collections.singletonList(userReview));
        WebsiteUser currentUser = new WebsiteUser();
        UserReviewDTO userReviewDTO = new UserReviewDTO();

        when(userReviewRepository.findByGameTitle(gameTitle, pageable)).thenReturn(userReviews);
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(userReviewMapper.toDto(userReview)).thenReturn(userReviewDTO);
        when(ratingRepository.findByUserAndUserReview(currentUser, userReview)).thenReturn(Optional.empty());

        Page<UserReviewDTO> result = userReviewService.getUserReviewByGame(gameTitle, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userReviewRepository, times(1)).findByGameTitle(gameTitle, pageable);
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewMapper, times(1)).toDto(userReview);
        verify(ratingRepository, times(1)).findByUserAndUserReview(currentUser, userReview);
    }

    @Test
    void testGetUserReviewByUser() {
        Long userId = 1L;
        UserReview userReview = new UserReview();
        UserReviewDTO userReviewDTO = new UserReviewDTO();

        when(userReviewRepository.findByUserId(userId)).thenReturn(Collections.singletonList(userReview));
        when(userReviewMapper.toDto(userReview)).thenReturn(userReviewDTO);

        List<UserReviewDTO> result = userReviewService.getUserReviewByUser(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userReviewRepository, times(1)).findByUserId(userId);
        verify(userReviewMapper, times(1)).toDto(userReview);
    }

    @Test
    void testGetUserReviewById() {
        Integer reviewId = 1;
        UserReview userReview = new UserReview();
        UserReviewDTO userReviewDTO = new UserReviewDTO();

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(userReview));
        when(userReviewMapper.toDto(userReview)).thenReturn(userReviewDTO);

        UserReviewDTO result = userReviewService.getUserReviewById(reviewId);

        assertNotNull(result);
        verify(userReviewRepository, times(1)).findById(reviewId);
        verify(userReviewMapper, times(1)).toDto(userReview);
    }

    @Test
    void testGetUserReviewsWithReports() {
        Pageable pageable = mock(Pageable.class);
        UserReview userReview = new UserReview();
        userReview.setReports(Collections.emptyList());
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        Page<UserReview> userReviews = new PageImpl<>(Collections.singletonList(userReview));

        when(userReviewRepository.findWithReports(pageable)).thenReturn(userReviews);
        when(userReviewMapper.toDto(userReview)).thenReturn(userReviewDTO);

        Page<UserReviewDTO> result = userReviewService.getUserReviewsWithReports(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userReviewRepository, times(1)).findWithReports(pageable);
        verify(userReviewMapper, times(1)).toDto(userReview);
    }

    @Test
    void testCreateUserReview() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setUserUsername("testUser2");
        userReviewDTO.setGameTitle("Cimbus Lompany");
        userReviewDTO.setContent("Nice game");
        userReviewDTO.setScore(7);

        UserReview userReview = new UserReview();
        WebsiteUser websiteUser = new WebsiteUser();
        when(websiteUserRepository.findByUsername("testUser2")).thenReturn(websiteUser);
        when(websiteUserService.getCurrentUser()).thenReturn(websiteUser);
        when(gameRepository.findGameByTitle("Cimbus Lompany")).thenReturn(new Game());
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(userReview);
        when(userReviewMapper.toDto(any(UserReview.class))).thenReturn(userReviewDTO);

        UserReviewDTO result = userReviewService.createUserReview(userReviewDTO);

        assertNotNull(result);
        verify(websiteUserRepository, times(1)).findByUsername("testUser2");
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(gameRepository, times(1)).findGameByTitle("Cimbus Lompany");
        verify(userReviewRepository, times(1)).save(any(UserReview.class));
        verify(userReviewMapper, times(1)).toDto(any(UserReview.class));
    }

    @Test
    void testUpdateUserReview() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setScore(8);
        userReviewDTO.setContent("Even nicer than I thought");

        UserReview userReview = new UserReview();
        userReview.setUser(new WebsiteUser());

        when(userReviewRepository.findById(userReviewDTO.getId())).thenReturn(userReview);
        when(websiteUserService.getCurrentUser()).thenReturn(userReview.getUser());
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(userReview);
        when(userReviewMapper.toDto(any(UserReview.class))).thenReturn(userReviewDTO);

        UserReviewDTO result = userReviewService.updateUserReview(userReviewDTO);

        assertNotNull(result);
        verify(userReviewRepository, times(1)).findById(userReviewDTO.getId());
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewRepository, times(1)).save(any(UserReview.class));
        verify(userReviewMapper, times(1)).toDto(any(UserReview.class));
    }

    @Test
    void testDeleteUserReviewByOwner() {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setUserUsername("testUser2");

        WebsiteUser websiteUser = new WebsiteUser();
        UserReview userReview = new UserReview();
        userReview.setUser(websiteUser);

        when(websiteUserRepository.findByUsername("testUser2")).thenReturn(websiteUser);
        when(websiteUserService.getCurrentUser()).thenReturn(websiteUser);
        when(userReviewRepository.findById(userReviewDTO.getId())).thenReturn(userReview);

        boolean result = userReviewService.deleteUserReviewByOwner(userReviewDTO);

        assertTrue(result);
        verify(websiteUserRepository, times(1)).findByUsername("testUser2");
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewRepository, times(1)).findById(userReviewDTO.getId());
        verify(userReviewRepository, times(1)).deleteById(Math.toIntExact(userReviewDTO.getId()));
    }

    @Test
    void testDeleteUserReviewById() throws BadRequestException {
        Long reviewId = 1L;
        UserReview userReview = new UserReview();

        when(userReviewRepository.findById(reviewId)).thenReturn(userReview);

        boolean result = userReviewService.deleteUserReviewById(reviewId);

        assertTrue(result);
        verify(userReviewRepository, times(1)).findById(reviewId);
        verify(userReviewRepository, times(1)).delete(userReview);
    }
}
