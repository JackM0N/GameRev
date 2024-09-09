package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
import pl.ttsw.GameRev.mapper.UserReviewMapper;
import pl.ttsw.GameRev.mapper.UserReviewMapperImpl;
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

    @Spy
    private UserReviewMapper userReviewMapper = new UserReviewMapperImpl();

    @InjectMocks
    private UserReviewService userReviewService;

    private final Pageable pageable = PageRequest.ofSize(10);

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
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        when(userReviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userReviews);
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(ratingRepository.findByUserAndUserReview(currentUser, userReview)).thenReturn(Optional.empty());

        Page<UserReviewDTO> result = userReviewService.getUserReviewByGame(gameTitle, userReviewFilter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userReviewRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewMapper, times(1)).toDto(userReview);
        verify(ratingRepository, times(1)).findByUserAndUserReview(currentUser, userReview);
    }

    @Test
    void testGetUserReviewByUser() throws BadRequestException {
        Long userId = 1L;
        WebsiteUser currentUser = new WebsiteUser();
        UserReview userReview = new UserReview();
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        Page<UserReview> userReviews = new PageImpl<>(Collections.singletonList(userReview));
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        when(websiteUserRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userReviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userReviews);

        Page<UserReviewDTO> result = userReviewService.getUserReviewByUser(userId, userReviewFilter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userReviewDTO, result.getContent().get(0));

        verify(websiteUserRepository, times(1)).findById(userId);
        verify(userReviewRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(userReviewMapper, times(1)).toDto(userReview);
    }

    @Test
    void testGetUserReviewById() {
        Long reviewId = 1L;
        UserReview userReview = new UserReview();

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(userReview));

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
        Page<UserReview> userReviews = new PageImpl<>(Collections.singletonList(userReview));
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        when(userReviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userReviews);

        Page<UserReviewDTO> result = userReviewService.getUserReviewsWithReports(userReviewFilter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userReviewRepository, times(1)).findAll(any(Specification.class), eq(pageable));
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
        when(websiteUserRepository.findByUsername("testUser2")).thenReturn(Optional.of(websiteUser));
        when(websiteUserService.getCurrentUser()).thenReturn(websiteUser);
        when(gameRepository.findGameByTitle("Cimbus Lompany")).thenReturn(Optional.of(new Game()));
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(userReview);

        UserReviewDTO result = userReviewService.createUserReview(userReviewDTO);

        assertNotNull(result);
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

        when(userReviewRepository.findById(userReviewDTO.getId())).thenReturn(Optional.of(userReview));
        when(websiteUserService.getCurrentUser()).thenReturn(userReview.getUser());
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(userReview);

        UserReviewDTO result = userReviewService.updateUserReview(userReviewDTO);

        assertNotNull(result);
        verify(userReviewRepository, times(1)).findById(userReviewDTO.getId());
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewRepository, times(1)).save(any(UserReview.class));
        verify(userReviewMapper, times(1)).toDto(any(UserReview.class));
    }

    @Test
    void testDeleteUserReviewByOwner() throws BadRequestException {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(1L);
        userReviewDTO.setUserUsername("testUser2");

        WebsiteUser websiteUser = new WebsiteUser();
        UserReview userReview = new UserReview();
        userReview.setUser(websiteUser);

        when(websiteUserRepository.findByUsername("testUser2")).thenReturn(Optional.of(websiteUser));
        when(websiteUserService.getCurrentUser()).thenReturn(websiteUser);
        when(userReviewRepository.findById(userReviewDTO.getId())).thenReturn(Optional.of(userReview));

        boolean result = userReviewService.deleteUserReviewByOwner(userReviewDTO);

        assertTrue(result);
        verify(websiteUserService, times(1)).getCurrentUser();
        verify(userReviewRepository, times(1)).findById(userReviewDTO.getId());
        verify(userReviewRepository, times(1)).deleteById(userReviewDTO.getId());
    }

    @Test
    void testDeleteUserReviewById() throws BadRequestException {
        Long reviewId = 1L;
        UserReview userReview = new UserReview();

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(userReview));

        boolean result = userReviewService.deleteUserReviewById(reviewId);

        assertTrue(result);
        verify(userReviewRepository, times(1)).findById(reviewId);
        verify(userReviewRepository, times(1)).delete(userReview);
    }
}
