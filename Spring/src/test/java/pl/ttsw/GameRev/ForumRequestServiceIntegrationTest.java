package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.ForumRequestRepository;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.service.ForumRequestService;
import pl.ttsw.GameRev.service.WebsiteUserService;
import org.springframework.security.test.context.support.WithMockUser;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ForumRequestServiceIntegrationTest {

    @Autowired
    private ForumRequestService forumRequestService;

    @Autowired
    private ForumRequestRepository forumRequestRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private WebsiteUserService websiteUserService;

    Pageable pageable = PageRequest.of(0, 10);

    @Test
    @Transactional
    public void testGetAllForumRequests_Success() {
        Boolean approved = true;

        Page<ForumRequestDTO> result = forumRequestService.getAllForumRequests(approved, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Transactional
    public void testGetForumRequestById_Success() {
        Long requestId = 1L; // assuming a request with ID 1 exists

        ForumRequestDTO result = forumRequestService.getForumRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
    }

    @Test
    @Transactional
    public void testGetForumRequestById_NotFound() {
        Long requestId = 999L;

        assertThrows(EntityNotFoundException.class, () -> forumRequestService.getForumRequestById(requestId));
    }

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void testCreateForumRequest_Success() throws BadRequestException {
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        forumRequestDTO.setForumName("New Forum Request");

        Game game = gameRepository.findById(1L).orElseThrow(() -> new RuntimeException("Game not found"));
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(game.getId());
        gameDTO.setTitle(game.getTitle());

        Forum parentForum = forumRepository.findById(1L).orElseThrow(() -> new RuntimeException("Parent forum not found"));
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setId(parentForum.getId());
        forumDTO.setForumName(parentForum.getForumName());
        forumDTO.setDescription(parentForum.getDescription());


        forumRequestDTO.setGame(gameDTO);
        forumRequestDTO.setParentForum(forumDTO);
        forumRequestDTO.setDescription("Desc");

        ForumRequestDTO createdRequest = forumRequestService.createForumRequest(forumRequestDTO);

        assertNotNull(createdRequest);
        assertEquals("New Forum Request", createdRequest.getForumName());
        assertEquals(game.getTitle(), createdRequest.getGame().getTitle());
        assertEquals(parentForum.getForumName(), createdRequest.getParentForum().getForumName());
    }

    @Test
    @Transactional
    public void testCreateForumRequest_ForumNameExists() {
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        forumRequestDTO.setForumName("Limbus Company");

        assertThrows(BadRequestException.class, () -> forumRequestService.createForumRequest(forumRequestDTO));
    }

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void testUpdateForumRequest_Success() {
        Long requestId = 1L;
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        forumRequestDTO.setForumName("Updated Forum Request");

        ForumRequestDTO updatedRequest = forumRequestService.updateForumRequest(requestId, forumRequestDTO);

        assertNotNull(updatedRequest);
        assertEquals("Updated Forum Request", updatedRequest.getForumName());
    }

    @Test
    @Transactional
    @WithMockUser("otherUser")
    public void testUpdateForumRequest_Forbidden() {
        Long requestId = 1L;
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        forumRequestDTO.setForumName("Updated Forum Request");

        assertThrows(RuntimeException.class, () -> forumRequestService.updateForumRequest(requestId, forumRequestDTO));
    }

    @Test
    @Transactional
    public void testApproveForumRequest_Success() {
        Long requestId = 1L;

        ForumRequestDTO result = forumRequestService.approveForumRequest(requestId, true);

        assertNotNull(result);
        assertTrue(result.getApproved());
    }

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void testDeleteForumRequest_Success() {
        Long requestId = 1L;

        boolean result = forumRequestService.deleteForumRequest(requestId);

        assertTrue(result);
        assertThrows(EntityNotFoundException.class, () -> forumRequestService.getForumRequestById(requestId));
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testDeleteForumRequest_Forbidden() {
        Long requestId = 1L;

        assertThrows(BadCredentialsException.class, () -> forumRequestService.deleteForumRequest(requestId));
    }
}
