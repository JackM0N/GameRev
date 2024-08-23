package pl.ttsw.GameRev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.*;
import pl.ttsw.GameRev.service.ForumPostService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ForumPostServiceTest {

    @Mock
    private ForumPostRepository forumPostRepository;

    @Mock
    private ForumRepository forumRepository;

    @Mock
    private ForumPostMapper forumPostMapper;

    @Mock
    private WebsiteUserRepository websiteUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private ForumModeratorRepository forumModeratorRepository;

    @InjectMocks
    private ForumPostService forumPostService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetForumPosts_Success() {
        Long forumId = 1L;
        Forum forum = new Forum();
        forum.setId(forumId);
        ForumPost forumPost = new ForumPost();
        forumPost.setForum(forum);
        forumPost.setAuthor(new WebsiteUser());

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(forum));
        when(forumPostRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(forumPost)));
        when(forumPostMapper.toDto(any())).thenReturn(new ForumPostDTO());

        Page<ForumPostDTO> result = forumPostService.getForumPosts(forumId, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(forumRepository).findById(forumId);
        verify(forumPostRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void testGetForumPosts_ForumNotFound() {
        Long forumId = 999L;

        when(forumRepository.findById(forumId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> forumPostService.getForumPosts(forumId, null, null, null, pageable));
        assertEquals("Forum not found", exception.getMessage());
    }

    @Test
    public void testCreateForumPost_Success() throws IOException {
        Long forumId = 1L;
        Forum forum = new Forum();
        forum.setId(forumId);
        WebsiteUser user = new WebsiteUser();
        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setForum(new ForumDTO());
        forumPostDTO.getForum().setId(forumId);
        forumPostDTO.setTitle("Test Title");
        forumPostDTO.setContent("Test Content");

        when(forumRepository.findById(forumId)).thenReturn(Optional.of(forum));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(forumPostRepository.save(any(ForumPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(forumPostMapper.toDto(any())).thenReturn(forumPostDTO);

        ForumPostDTO result = forumPostService.createForumPost(forumPostDTO, null);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(forumRepository).findById(forumId);
        verify(forumPostRepository).save(any(ForumPost.class));
    }

    @Test
    public void testCreateForumPost_ForumNotFound() {
        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setForum(new ForumDTO());
        forumPostDTO.getForum().setId(999L);

        when(forumRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> forumPostService.createForumPost(forumPostDTO, null));
        assertEquals("Forum not found", exception.getMessage());
    }

    @Test
    public void testUpdateForumPost_Success() throws IOException {
        Long postId = 1L;
        Long forumId = 2L;
        Forum forum = new Forum();
        forum.setId(forumId);
        WebsiteUser user = new WebsiteUser();

        ForumPost forumPost = new ForumPost();
        forumPost.setId(postId);
        forumPost.setForum(forum);
        forumPost.setAuthor(user);

        ForumPostDTO forumPostDTO = new ForumPostDTO();
        forumPostDTO.setForum(new ForumDTO());
        forumPostDTO.getForum().setId(forumId);
        forumPostDTO.setTitle("Updated Title");

        when(forumPostRepository.findById(postId)).thenReturn(Optional.of(forumPost));
        when(forumRepository.findById(forumId)).thenReturn(Optional.of(forum));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(forumPostRepository.save(any(ForumPost.class))).thenReturn(forumPost);
        when(forumPostMapper.toDto(any())).thenReturn(forumPostDTO);

        ForumPostDTO result = forumPostService.updateForumPost(postId, forumPostDTO, null);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(forumPostRepository).save(any(ForumPost.class));
    }

    @Test
    public void testUpdateForumPost_NoPermission() {
        Long postId = 1L;
        ForumPost forumPost = new ForumPost();
        forumPost.setId(postId);
        forumPost.setAuthor(new WebsiteUser());
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("Admin");
        Optional<Role> roles = Optional.of(adminRole);

        when(forumPostRepository.findById(postId)).thenReturn(Optional.of(forumPost));
        when(websiteUserService.getCurrentUser()).thenReturn(new WebsiteUser());
        when(roleRepository.findByRoleName("Admin")).thenReturn(roles);

        ForumPostDTO forumPostDTO = new ForumPostDTO();

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> forumPostService.updateForumPost(postId, forumPostDTO, null));
        assertEquals("You dont have permission to perform this action", exception.getMessage());
    }

    @Test
    public void testDeleteForumPost_Success() {
        Long postId = 1L;
        WebsiteUser user = new WebsiteUser();
        user.setRoles(Collections.emptyList());
        ForumPost forumPost = new ForumPost();
        forumPost.setId(postId);
        forumPost.setAuthor(user);

        when(forumPostRepository.findById(postId)).thenReturn(Optional.of(forumPost));
        when(websiteUserService.getCurrentUser()).thenReturn(user);
        when(forumModeratorRepository.existsByForumAndModerator(any(),any())).thenReturn(false);

        boolean result = forumPostService.deleteForumPost(postId);

        assertTrue(result);
        verify(forumPostRepository).delete(forumPost);
    }

    @Test
    public void testDeleteForumPost_NoPermission() {
        Long postId = 1L;
        ForumPost forumPost = new ForumPost();
        forumPost.setId(postId);
        WebsiteUser author = new WebsiteUser();
        author.setNickname("aa");
        forumPost.setAuthor(author);
        WebsiteUser otherUser = new WebsiteUser();
        otherUser.setNickname("bb");
        assertNotEquals(author, otherUser);

        when(forumPostRepository.findById(postId)).thenReturn(Optional.of(forumPost));
        when(websiteUserService.getCurrentUser()).thenReturn(otherUser);
        when(forumModeratorRepository.existsByForumAndModerator(any(),any())).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> forumPostService.deleteForumPost(postId));
        assertEquals("You dont have permission to perform this action", exception.getMessage());
    }
}
