package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.mapper.CriticReviewMapper;
import pl.ttsw.GameRev.mapper.CriticReviewMapperImpl;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.CriticReview;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.CriticReviewRepository;
import pl.ttsw.GameRev.service.CriticReviewService;
import pl.ttsw.GameRev.service.GameService;
import pl.ttsw.GameRev.service.WebsiteUserService;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CriticReviewServiceTest {

    @Mock
    private CriticReviewRepository criticReviewRepository;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private CriticReviewService criticReviewService;

    @Spy
    private CriticReviewMapper criticReviewMapper = new CriticReviewMapperImpl();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCriticReviewByTitle_Success() throws BadRequestException {
        CriticReview mockReview = new CriticReview();
        mockReview.setId(1L);
        mockReview.setGame(new Game());
        mockReview.setUser(new WebsiteUser());
        mockReview.setContent("Review content");
        mockReview.setScore(8);
        mockReview.setPostDate(LocalDate.now());
        mockReview.setReviewStatus(ReviewStatus.APPROVED);

        when(criticReviewRepository.findByGameTitleAndReviewStatus("Some Game", ReviewStatus.APPROVED))
                .thenReturn(Optional.of(mockReview));

        CriticReviewDTO result = criticReviewService.getCriticReviewByTitle("Some Game");

        assertNotNull(result);
        verify(criticReviewRepository, times(1)).findByGameTitleAndReviewStatus("Some Game", ReviewStatus.APPROVED);
    }

    @Test
    public void testGetCriticReviewByTitle_NotFound() {
        when(criticReviewRepository.findByGameTitleAndReviewStatus("Unknown Game", ReviewStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> criticReviewService.getCriticReviewByTitle("Unknown Game"));

        verify(criticReviewRepository, times(1)).findByGameTitleAndReviewStatus("Unknown Game", ReviewStatus.APPROVED);
    }

    @Test
    public void testCreateCriticReview_Success() throws BadRequestException {
        CriticReviewDTO criticReviewDTO = new CriticReviewDTO();
        criticReviewDTO.setGameTitle("Some Game");
        criticReviewDTO.setContent("Great review content");
        criticReviewDTO.setScore(9);

        WebsiteUser mockUser = new WebsiteUser();
        when(websiteUserService.getCurrentUser()).thenReturn(mockUser);

        Game mockGame = new Game();
        GameDTO mockGameDTO = new GameDTO();
        when(gameService.getGameByTitle("Some Game")).thenReturn(mockGameDTO);
        when(gameMapper.toEntity(mockGameDTO)).thenReturn(mockGame);

        CriticReview mockCriticReview = new CriticReview();
        when(criticReviewRepository.save(any(CriticReview.class))).thenReturn(mockCriticReview);

        CriticReviewDTO result = criticReviewService.createCriticReview(criticReviewDTO);

        assertNotNull(result);
        verify(criticReviewRepository, times(1)).save(any(CriticReview.class));
    }

    @Test
    public void testUpdateCriticReview_Success() throws BadRequestException {
        Long id = 1L;
        CriticReviewDTO criticReviewDTO = new CriticReviewDTO();
        criticReviewDTO.setScore(8);
        criticReviewDTO.setContent("Updated review content");
        WebsiteUser mockUser = new WebsiteUser();
        when(websiteUserService.getCurrentUser()).thenReturn(mockUser);
        CriticReview mockReview = new CriticReview();
        when(criticReviewRepository.findById(id)).thenReturn(Optional.of(mockReview));
        when(criticReviewRepository.save(any(CriticReview.class))).thenReturn(mockReview);

        CriticReviewDTO result = criticReviewService.updateCriticReview(id, criticReviewDTO);

        assertNotNull(result);
        verify(criticReviewRepository, times(1)).save(any(CriticReview.class));
    }

    @Test
    public void testUpdateCriticReview_NotFound() {
        Long id = 1L;
        CriticReviewDTO criticReviewDTO = new CriticReviewDTO();

        WebsiteUser mockUser = new WebsiteUser();
        when(websiteUserService.getCurrentUser()).thenReturn(mockUser);
        when(criticReviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> criticReviewService.updateCriticReview(id, criticReviewDTO));
    }

    @Test
    public void testReviewCriticReview_Success() throws BadRequestException {
        Long id = 1L;
        ReviewStatus status = ReviewStatus.APPROVED;
        WebsiteUser mockUser = new WebsiteUser();
        when(websiteUserService.getCurrentUser()).thenReturn(mockUser);
        CriticReview mockReview = new CriticReview();
        when(criticReviewRepository.findById(id)).thenReturn(Optional.of(mockReview));
        when(criticReviewRepository.save(any(CriticReview.class))).thenReturn(mockReview);

        CriticReviewDTO result = criticReviewService.reviewCriticReview(id, status);

        assertNotNull(result);
        verify(criticReviewRepository, times(1)).save(any(CriticReview.class));
    }

    @Test
    public void testReviewCriticReview_NotFound() {
        Long id = 1L;
        ReviewStatus status = ReviewStatus.APPROVED;

        WebsiteUser mockUser = new WebsiteUser();
        when(websiteUserService.getCurrentUser()).thenReturn(mockUser);
        when(criticReviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> criticReviewService.reviewCriticReview(id, status));
    }

    @Test
    public void testDeleteCriticReview_Success() throws BadRequestException {
        Long id = 1L;
        CriticReview mockReview = new CriticReview();
        when(criticReviewRepository.findById(id)).thenReturn(Optional.of(mockReview));

        boolean result = criticReviewService.deleteCriticReview(id);

        assertTrue(result);
        verify(criticReviewRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteCriticReview_NotFound() {
        Long id = 1L;
        when(criticReviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> criticReviewService.deleteCriticReview(id));
    }
}
