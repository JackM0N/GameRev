package pl.ttsw.GameRev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumCommentFilter;
import pl.ttsw.GameRev.filter.ForumPostFilter;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.model.ForumComment;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumCommentRepository;
import pl.ttsw.GameRev.service.ForumCommentService;
import pl.ttsw.GameRev.service.ForumPostService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@ActiveProfiles("test")
public class ForumCommentServiceIntegrationTest {

    @Autowired
    private ForumCommentService forumCommentService;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Autowired
    private WebsiteUserService websiteUserService;

    private ForumPostDTO post;

    @BeforeEach
    void setUp() {
        post = forumPostService.getForumPosts(2L, new ForumPostFilter(), Pageable.unpaged()).get().toList().get(0);
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void testGetForumCommentsByPost() {
        Page<ForumCommentDTO> forumCommentDTOS = forumCommentService.getForumCommentsByPost(post.getId(), new ForumCommentFilter(), Pageable.unpaged());

        assertNotNull(forumCommentDTOS);
        assertTrue(forumCommentDTOS.stream().findAny().isPresent());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testCreateForumComment_Success() {
        ForumCommentDTO commentDTO = new ForumCommentDTO();
        commentDTO.setForumPostId(post.getId());
        commentDTO.setContent("Finally");

        ForumCommentDTO savedCommentDTO = forumCommentService.createForumComment(commentDTO);

        assertNotNull(savedCommentDTO.getId());
        assertEquals("Finally", savedCommentDTO.getContent());

        ForumComment savedComment = forumCommentRepository.findById(savedCommentDTO.getId()).orElse(null);
        assertNotNull(savedComment);
        assertEquals(post.getId(), savedComment.getForumPost().getId());
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void testCreateForumComment_NotLoggedIn() {
        ForumCommentDTO commentDTO = new ForumCommentDTO();
        commentDTO.setForumPostId(post.getId());
        commentDTO.setContent("Finally");

        assertThrows(BadCredentialsException.class, () -> forumCommentService.createForumComment(commentDTO));
    }

    @Test
    @Transactional
    @WithMockUser("gamer_guy")
    public void testUpdateForumComment_Success() {
        ForumComment comment = forumCommentRepository.findById(1L).orElse(null);
        assertNotNull(comment);
        comment.setContent("Edited");
        ForumCommentDTO forumCommentDTO = forumCommentMapper.toDto(comment);

        ForumCommentDTO resultDTO = forumCommentService.updateForumComment(comment.getId(), forumCommentDTO);
        assertEquals("Edited", resultDTO.getContent());

        ForumComment updatedComment = forumCommentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(updatedComment);
        assertEquals("Edited", updatedComment.getContent());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testUpdateForumComment_NotAuthor() {
        ForumComment comment = forumCommentRepository.findById(1L).orElse(null);
        assertNotNull(comment);
        comment.setContent("Edited");
        ForumCommentDTO forumCommentDTO = forumCommentMapper.toDto(comment);

        assertThrows(BadCredentialsException.class, () -> forumCommentService.updateForumComment(comment.getId(), forumCommentDTO));
    }

    @Test
    @Transactional
    @WithMockUser("gamer_guy")
    public void testDeleteForumComment_Success() {
        WebsiteUser userForContext = websiteUserService.getCurrentUser(); // not needed but without it test fails somehow
        assertEquals(userForContext.getUsername(), "gamer_guy");
        ForumComment comment = forumCommentRepository.findById(1L).orElse(null);
        assertNotNull(comment);

        boolean result = forumCommentService.deleteForumComment(comment.getId(), true);

        assertTrue(result);

        ForumComment deletedComment = forumCommentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(deletedComment);
        assertTrue(deletedComment.getIsDeleted());
        assertNotNull(deletedComment.getDeletedAt());
    }

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void testDeleteForumComment_Admin() {
        ForumComment comment = forumCommentRepository.findById(1L).orElse(null);
        assertNotNull(comment);

        boolean result = forumCommentService.deleteForumComment(comment.getId(), true);

        assertTrue(result);

        ForumComment deletedComment = forumCommentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(deletedComment);
        assertTrue(deletedComment.getIsDeleted());
        assertNotNull(deletedComment.getDeletedAt());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testDeleteForumComment_OtherUser() {
        ForumComment comment = forumCommentRepository.findById(1L).orElse(null);
        assertNotNull(comment);

        assertThrows(BadCredentialsException.class, () -> forumCommentService.deleteForumComment(comment.getId(), true));
    }
}
