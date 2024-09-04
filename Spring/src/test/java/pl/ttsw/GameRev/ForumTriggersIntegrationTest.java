package pl.ttsw.GameRev;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.repository.ForumCommentRepository;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.service.ForumCommentService;
import pl.ttsw.GameRev.service.ForumPostService;
import pl.ttsw.GameRev.service.ForumService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@ActiveProfiles("test")
public class ForumTriggersIntegrationTest {

    @Autowired
    private ForumCommentService forumCommentService;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Autowired
    private WebsiteUserService websiteUserService;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumMapper forumMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Test
    @Transactional
    @WithMockUser("testadmin")
    public void triggersTest() throws IOException {
        //INSERT

        // Test Forum postCount
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setForumName("Test Forum");
        forumDTO.setDescription("Test Description");
        forumDTO.setGameTitle("Limbus Company");
        forumDTO.setParentForumId(2L);

        ForumDTO savedForum = forumService.createForum(forumDTO);
        assertNotNull(savedForum);

        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setForum(savedForum);
        forumPostDTO.setTitle("Test Title");
        forumPostDTO.setContent("Test Content");

        ForumPostDTO savedPost = forumPostService.createForumPost(forumPostDTO, null);
        assertNotNull(savedPost);

        entityManager.flush(); // force hibernate to get updated data from db
        entityManager.clear(); // instead of local simulated db where there are no triggers that are tested here
        savedForum = forumMapper.toDto(forumRepository.findById(savedForum.getId()).get());
        assertNotNull(savedForum);
        assertEquals(1, savedForum.getPostCount());

        // Test ForumPost commentCount
        ForumCommentDTO forumCommentDTO = new ForumCommentDTO();
        forumCommentDTO.setForumPostId(savedPost.getId());
        forumCommentDTO.setContent("Test Content");

        ForumCommentDTO savedComment = forumCommentService.createForumComment(forumCommentDTO, null);
        assertNotNull(savedComment);

        entityManager.flush();
        entityManager.clear();
        savedPost = forumPostMapper.toDto(forumPostRepository.findById(savedPost.getId()).get());
        assertEquals(1, savedPost.getCommentCount());


        //UPDATE
        savedPost.setContent("Edited");
        savedPost = forumPostService.updateForumPost(savedPost.getId(), savedPost, null);
        assertNotNull(savedPost);

        savedComment.setContent("Edited");
        savedComment = forumCommentService.updateForumComment(savedComment.getId(), savedComment, null);
        assertNotNull(savedComment);

        entityManager.flush();
        entityManager.clear();
        savedForum = forumMapper.toDto(forumRepository.findById(savedForum.getId()).get());
        assertEquals(1, savedForum.getPostCount());
        savedPost = forumPostMapper.toDto(forumPostRepository.findById(savedPost.getId()).get());
        assertEquals(1, savedPost.getCommentCount());


        //DELETE-soft
        forumCommentService.deleteForumComment(savedComment.getId(), true);

        entityManager.flush();
        entityManager.clear();
        savedPost = forumPostMapper.toDto(forumPostRepository.findById(savedPost.getId()).get());
        assertEquals(0, savedPost.getCommentCount());
        assertFalse(forumCommentRepository.findById(savedComment.getId()).isEmpty());
        savedComment = forumCommentMapper.toDto(forumCommentRepository.findById(savedComment.getId()).get());
        assertTrue(savedComment.getIsDeleted());

        forumPostService.deleteForumPost(savedPost.getId(), true);

        entityManager.flush();
        entityManager.clear();
        savedForum = forumMapper.toDto(forumRepository.findById(savedForum.getId()).get());
        assertEquals(0, savedForum.getPostCount());
        assertFalse(forumPostRepository.findById(savedPost.getId()).isEmpty());
        savedPost = forumPostMapper.toDto(forumPostRepository.findById(savedPost.getId()).get());
        assertTrue(savedPost.getIsDeleted());


        //DELETE-hard
        forumCommentRepository.deleteById(savedComment.getId());

        entityManager.flush();
        entityManager.clear();
        savedPost = forumPostMapper.toDto(forumPostRepository.findById(savedPost.getId()).get());
        assertEquals(0, savedPost.getCommentCount());
        assertTrue(forumCommentRepository.findById(savedComment.getId()).isEmpty());

        forumPostRepository.deleteById(savedPost.getId());

        entityManager.flush();
        entityManager.clear();
        savedForum = forumMapper.toDto(forumRepository.findById(savedForum.getId()).get());
        assertEquals(0, savedForum.getPostCount());
        assertTrue(forumPostRepository.findById(savedPost.getId()).isEmpty());
    }
}
