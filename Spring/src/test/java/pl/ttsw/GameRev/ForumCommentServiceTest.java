package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumCommentFilter;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.mapper.ForumCommentMapperImpl;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.mapper.ForumPostMapperImpl;
import pl.ttsw.GameRev.model.ForumComment;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumCommentRepository;
import pl.ttsw.GameRev.repository.ForumModeratorRepository;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.service.ForumCommentService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForumCommentServiceTest {

    @Mock
    private ForumCommentRepository forumCommentRepository;

    @Mock
    private ForumPostRepository forumPostRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @InjectMocks
    private ForumCommentService forumCommentService;

    @Mock
    private ForumModeratorRepository forumModeratorRepository;

    @Spy
    private ForumCommentMapper forumCommentMapper = new ForumCommentMapperImpl();

    @Spy
    private ForumPostMapper forumPostMapper = new ForumPostMapperImpl();

    private ForumPost forumPost;
    private ForumComment forumComment;
    private WebsiteUser user;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new WebsiteUser();
        user.setNickname("testuser");
        user.setUsername("testuser");
        user.setId(1L);
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("USER");
        user.setRoles(Collections.singletonList(role));

        forumPost = new ForumPost();
        forumPost.setId(1L);
        forumPost.setViews(1L);

        forumComment = new ForumComment();
        forumComment.setId(1L);
        forumComment.setContent("Test comment");
        forumComment.setForumPost(forumPost);
        forumComment.setAuthor(user);
    }

    @Test
    void testGetForumCommentsByPost_Success() {
        when(forumPostRepository.findById(anyLong())).thenReturn(Optional.of(forumPost));
        when(forumCommentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(forumComment)));

        Page<ForumCommentDTO> result = forumCommentService.getForumCommentsByPost(
                1L, new ForumCommentFilter(), pageable);

        assertEquals(1, result.getTotalElements());
        verify(forumCommentMapper).toDto(forumComment);
    }

    @Test
    void testGetForumCommentsByPost_ForumPostNotFound() {
        when(forumPostRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                forumCommentService.getForumCommentsByPost(1L, new ForumCommentFilter(), pageable));
    }

    @Test
    void testGetOriginalPost_Success() {
        when(forumPostRepository.findById(anyLong())).thenReturn(Optional.of(forumPost));

        ForumPostDTO result = forumCommentService.getOriginalPost(1L);

        verify(forumPostMapper).toDto(forumPost);
        assertNotNull(result);
    }

    @Test
    void testGetOriginalPost_PostNotFound() {
        when(forumPostRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> forumCommentService.getOriginalPost(1L));
    }

    @Test
    void testCreateForumComment_Success() throws IOException {
        when(forumPostRepository.findById(anyLong())).thenReturn(Optional.of(forumPost));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(forumCommentRepository.save(any(ForumComment.class))).thenReturn(forumComment);

        ForumCommentDTO forumCommentDTO = new ForumCommentDTO();
        forumCommentDTO.setContent("Test comment");
        forumCommentDTO.setForumPostId(1L);

        ForumCommentDTO result = forumCommentService.createForumComment(forumCommentDTO, null);

        verify(forumCommentRepository, times(1)).save(any(ForumComment.class));
        assertEquals("Test comment", result.getContent());
    }

    @Test
    void testCreateForumComment_ForumPostNotFound() {
        when(forumPostRepository.findById(anyLong())).thenReturn(Optional.empty());

        ForumCommentDTO forumCommentDTO = new ForumCommentDTO();
        forumCommentDTO.setContent("Test comment");
        forumCommentDTO.setForumPostId(1L);

        assertThrows(RuntimeException.class, () -> forumCommentService.createForumComment(forumCommentDTO, null));
    }

    @Test
    void testUpdateForumComment_Success() throws IOException {
        when(forumCommentRepository.findById(anyLong())).thenReturn(Optional.of(forumComment));
        when(websiteUserService.getCurrentUser()).thenReturn(user);

        ForumCommentDTO forumCommentDTO = new ForumCommentDTO();
        forumCommentDTO.setContent("Updated content");

        ForumCommentDTO result = forumCommentService.updateForumComment(1L, forumCommentDTO, null);

        verify(forumCommentMapper).partialUpdateContent(forumCommentDTO, forumComment);
        assertEquals("Updated content", result.getContent());
    }

    @Test
    void testUpdateForumComment_Unauthorized() {
        when(forumCommentRepository.findById(anyLong())).thenReturn(Optional.of(forumComment));
        WebsiteUser otherUser = new WebsiteUser();
        otherUser.setId(2L);
        when(websiteUserService.getCurrentUser()).thenReturn(otherUser);

        ForumCommentDTO forumCommentDTO = new ForumCommentDTO();
        forumCommentDTO.setContent("Updated content");

        assertThrows(BadCredentialsException.class, () -> forumCommentService.updateForumComment(1L, forumCommentDTO, null));
    }

    @Test
    void testDeleteForumComment_Success() {
        when(forumCommentRepository.findById(anyLong())).thenReturn(Optional.of(forumComment));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(forumModeratorRepository.existsByForumAndModerator(any(), any())).thenReturn(false);

        boolean result = forumCommentService.deleteForumComment(1L, true);

        assertTrue(result);
        verify(forumCommentRepository).save(forumComment);
    }

    @Test
    void testDeleteForumComment_AsAdmin() {
        when(forumCommentRepository.findById(anyLong())).thenReturn(Optional.of(forumComment));
        when(forumModeratorRepository.existsByForumAndModerator(any(), any())).thenReturn(true);
        WebsiteUser admin = new WebsiteUser();
        admin.setId(2L);
        Role role = new Role();
        role.setRoleName("Admin");
        admin.setRoles(Collections.singletonList(role));
        when(websiteUserService.getCurrentUser()).thenReturn(admin);

        boolean result = forumCommentService.deleteForumComment(1L, true);

        assertTrue(result);
        verify(forumCommentRepository).save(forumComment);
    }

    @Test
    void testDeleteForumComment_Unauthorized() {
        when(forumCommentRepository.findById(anyLong())).thenReturn(Optional.of(forumComment));
        when(forumModeratorRepository.existsByForumAndModerator(any(), any())).thenReturn(false);
        WebsiteUser otherUser = new WebsiteUser();
        otherUser.setId(2L);
        when(websiteUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadCredentialsException.class, () -> forumCommentService.deleteForumComment(1L, true));
    }

}
