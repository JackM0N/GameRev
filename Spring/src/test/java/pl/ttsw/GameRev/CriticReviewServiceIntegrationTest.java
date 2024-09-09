package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.mapper.CriticReviewMapper;
import pl.ttsw.GameRev.model.CriticReview;
import pl.ttsw.GameRev.repository.CriticReviewRepository;
import pl.ttsw.GameRev.service.CriticReviewService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CriticReviewServiceIntegrationTest {

    @Autowired
    private CriticReviewService criticReviewService;

    @Autowired
    private CriticReviewRepository criticReviewRepository;

    @Autowired
    private CriticReviewMapper criticReviewMapper;

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testGetCriticReviewByTitle_Success() throws BadRequestException {
        CriticReviewDTO result = criticReviewService.getCriticReviewByTitle("Apex Legends");

        assertNotNull(result);
        assertEquals("Apex Legends", result.getGameTitle());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testGetCriticReviewByTitle_NotFound() {
        assertThrows(BadRequestException.class, () -> criticReviewService.getCriticReviewByTitle("Apex Legends 2"));

    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testCreateCriticReview_Success() throws BadRequestException {
        // all games already have reviews, and it is faster to delete existing review than to create new game
        CriticReview reviewToDelete = criticReviewRepository.findByGameTitle("Apex Legends").get();
        criticReviewRepository.delete(reviewToDelete);

        CriticReviewDTO criticReviewDTO = new CriticReviewDTO();
        criticReviewDTO.setGameTitle("Apex Legends");
        criticReviewDTO.setContent("Nice game");
        criticReviewDTO.setScore(9);

        CriticReviewDTO result = criticReviewService.createCriticReview(criticReviewDTO);

        assertNotNull(result);
        assertEquals("Apex Legends", result.getGameTitle());
        assertEquals(9, result.getScore());
        assertEquals("Nice game", result.getContent());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testUpdateCriticReview_Success() throws BadRequestException {
        CriticReview criticReview = criticReviewRepository.findByGameTitle("Apex Legends").get();
        criticReview.setScore(3);
        criticReview.setContent("Not so nice after update");

        CriticReviewDTO result = criticReviewService.updateCriticReview(criticReview.getId(), criticReviewMapper.toDto(criticReview));

        assertNotNull(result);
        assertEquals("Not so nice after update", result.getContent());
        assertEquals(3, result.getScore());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testUpdateCriticReview_NotFound() {
        CriticReviewDTO criticReviewDTO = new CriticReviewDTO();
        criticReviewDTO.setContent("Whatever");

        assertThrows(BadRequestException.class, () -> criticReviewService.updateCriticReview(999L, criticReviewDTO));
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testDeleteCriticReview_Success() throws BadRequestException {
        CriticReview reviewToDelete = criticReviewRepository.findByGameTitle("Apex Legends").get();
        boolean result = criticReviewService.deleteCriticReview(reviewToDelete.getId());

        assertTrue(result);
        assertFalse(criticReviewRepository.findById(reviewToDelete.getId()).isPresent());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testDeleteCriticReview_NotFound() {
        assertThrows(BadRequestException.class, () -> criticReviewService.deleteCriticReview(999L));
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testReviewCriticReview_Success() throws BadRequestException {
        CriticReview criticReview = criticReviewRepository.findByGameTitle("Limbus Company").get();
        CriticReviewDTO result = criticReviewService.reviewCriticReview(criticReview.getId(), ReviewStatus.APPROVED);

        assertNotNull(result);
        assertEquals(ReviewStatus.APPROVED, result.getReviewStatus());
        assertEquals("testuser", result.getStatusChangedBy().getNickname());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser")
    void testReviewCriticReview_NotFound() {
        assertThrows(BadRequestException.class, () -> criticReviewService.reviewCriticReview(999L, ReviewStatus.APPROVED));
    }
}
