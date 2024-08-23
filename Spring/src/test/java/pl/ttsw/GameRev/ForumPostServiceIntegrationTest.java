package pl.ttsw.GameRev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.mapper.WebsiteUserMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.ForumPostService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@ActiveProfiles("test")
@Transactional
public class ForumPostServiceIntegrationTest {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    private Forum testForum;
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private WebsiteUserMapper websiteUserMapper;

    @BeforeEach
    public void setUp() {
        testForum = forumRepository.findById(2L).get();
    }

    @Test
    public void testGetForumPosts_Success() {
        Page<ForumPostDTO> forumPosts = forumPostService.getForumPosts(testForum.getId(), null, null, null, Pageable.unpaged());

        assertNotNull(forumPosts);
        assertFalse(forumPosts.getContent().isEmpty());
        assertEquals("Update is finally here!", forumPosts.getContent().get(0).getTitle());
    }

    @Test
    @WithMockUser("testuser")
    public void testCreateForumPost_Success() throws Exception {
        ForumPostDTO forumPost = new ForumPostDTO();
        forumPost.setForum(forumMapper.toDto(testForum));
        forumPost.setTitle("New Post Title");
        forumPost.setContent("New Post Content");
        forumPost.setPostDate(LocalDateTime.now());

        ForumPostDTO createdPost = forumPostService.createForumPost(forumPost, null);

        assertNotNull(createdPost);
        assertEquals("New Post Title", createdPost.getTitle());

        Optional<ForumPost> forumPostOptional = forumPostRepository.findById(createdPost.getId());
        assertTrue(forumPostOptional.isPresent());
        assertEquals("New Post Title", forumPostOptional.get().getTitle());
    }

    @Test
    @WithMockUser("testuser")
    public void testUpdateForumPost_Success() throws Exception {
        ForumPostDTO forumPost = new ForumPostDTO();
        forumPost.setForum(forumMapper.toDto(testForum));
        forumPost.setTitle("New Post Title");
        forumPost.setContent("New Post Content");
        forumPost.setPostDate(LocalDateTime.now());

        ForumPostDTO createdPost = forumPostService.createForumPost(forumPost, null);

        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setTitle("Updated Title");
        forumPostDTO.setContent("Updated Content");

        ForumPostDTO updatedPost = forumPostService.updateForumPost(createdPost.getId(), forumPostDTO, null);

        assertNotNull(updatedPost);
        assertEquals("Updated Title", updatedPost.getTitle());

        Optional<ForumPost> forumPostOptional = forumPostRepository.findById(createdPost.getId());
        assertTrue(forumPostOptional.isPresent());
        assertEquals("Updated Title", forumPostOptional.get().getTitle());
    }

    @Test
    @WithMockUser("testuser")
    public void testDeleteForumPost_Success() throws IOException {
        ForumPostDTO forumPost = new ForumPostDTO();
        forumPost.setForum(forumMapper.toDto(testForum));
        forumPost.setTitle("New Post Title");
        forumPost.setContent("New Post Content");
        forumPost.setPostDate(LocalDateTime.now());

        ForumPostDTO createdPost = forumPostService.createForumPost(forumPost, null);

        boolean result = forumPostService.deleteForumPost(createdPost.getId());

        assertTrue(result);
        Optional<ForumPost> deletedPost = forumPostRepository.findById(createdPost.getId());
        assertFalse(deletedPost.isPresent());
    }

    @Test
    @WithMockUser("testuser")
    public void testDeleteForumPost_NoPermission() throws IOException {
        ForumPostDTO forumPost = new ForumPostDTO();
        forumPost.setForum(forumMapper.toDto(testForum));
        forumPost.setTitle("New Post Title");
        forumPost.setContent("New Post Content");
        forumPost.setPostDate(LocalDateTime.now());

        ForumPostDTO createdPost = forumPostService.createForumPost(forumPost, null);
        WebsiteUser otherUser = websiteUserRepository.findByUsername("testcritic").get();
        createdPost.setAuthor(websiteUserMapper.toDto(otherUser));
        forumPostService.updateForumPost(createdPost.getId(), createdPost, null);

        ForumPostDTO finalForumPost = createdPost;
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> forumPostService.deleteForumPost(finalForumPost.getId()));
        assertEquals("You dont have permission to perform this action", exception.getMessage());
    }
}
