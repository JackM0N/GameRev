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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.filter.ForumFilter;
import pl.ttsw.GameRev.service.ForumService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ForumServiceIntegrationTest {

    @Autowired
    private ForumService forumService;

    Pageable pageable = PageRequest.of(0, 10);

    @Test
    @Transactional
    @WithAnonymousUser
    public void testGetForum_Success() {
        Long forumId = 1L; // General
        ForumFilter forumFilter = new ForumFilter();

        Page<ForumDTO> result = forumService.getForum(forumId, forumFilter, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getTotalElements() >= 2); // General and Limbus, and whatever may be added later
        assertEquals("General", result.getContent().get(0).getForumName());
        assertEquals("Limbus Company", result.getContent().get(1).getForumName());
    }

    @Test
    @Transactional
    public void testGetForum_ForumNotFound() {
        Long forumId = 999L;
        ForumFilter forumFilter = new ForumFilter();

        assertThrows(EntityNotFoundException.class, () -> forumService.getForum(forumId, forumFilter, pageable));
    }

    @Test
    @Transactional
    public void testCreateForum_Success() throws BadRequestException {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Limbus Company");
        forumDTO.setForumName("New Forum");
        forumDTO.setDescription("Description of the new forum");
        forumDTO.setParentForumId(2L);

        ForumDTO createdForum = forumService.createForum(forumDTO);

        assertNotNull(createdForum);
        assertEquals("New Forum", createdForum.getForumName());
        assertEquals("Limbus Company", createdForum.getGameTitle());
        assertEquals("Description of the new forum", createdForum.getDescription());
        assertEquals(2L, createdForum.getParentForumId());
        assertEquals(0, createdForum.getPostCount());
        assertFalse(createdForum.getIsDeleted());
    }

    @Test
    @Transactional
    public void testCreateForum_GameNotFound() {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Gimbus Company");
        forumDTO.setForumName("New Forum");
        forumDTO.setDescription("Description of the new forum");
        forumDTO.setParentForumId(2L);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> forumService.createForum(forumDTO));
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    @Transactional
    public void testCreateForum_ParentForumNotFound() {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Limbus Company");
        forumDTO.setForumName("New Forum");
        forumDTO.setDescription("Description of the new forum");
        forumDTO.setParentForumId(999L);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> forumService.createForum(forumDTO));
        assertEquals("Parent forum not found", exception.getMessage());
    }

    @Test
    @Transactional
    public void testUpdateForum_Success() throws BadRequestException {
        Long forumId = 2L;
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setForumName("Updated Forum Name");

        ForumDTO updatedForum = forumService.updateForum(forumId, forumDTO);

        assertNotNull(updatedForum);
        assertEquals("Updated Forum Name", updatedForum.getForumName());
        assertEquals("Limbus Company", updatedForum.getGameTitle());
    }

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void testDeleteForum_Success() throws BadRequestException {
        Long forumId = 2L;
        ForumFilter forumFilter = new ForumFilter();
        forumFilter.setIsDeleted(true);

        boolean result = forumService.deleteForum(forumId, true);

        assertTrue(result);
        Page<ForumDTO> forums = forumService.getForum(1L, forumFilter, PageRequest.of(0, 10));
        assertTrue(forums.getTotalElements() >= 2);
        assertTrue(forums.getContent().get(1).getIsDeleted());
    }
}
