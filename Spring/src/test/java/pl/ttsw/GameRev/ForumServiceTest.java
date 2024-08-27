package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.filter.ForumFilter;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.service.ForumService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForumServiceTest {

    @Mock
    private ForumRepository forumRepository;

    @Mock
    private ForumMapper forumMapper;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ForumFilter forumFilter;

    @Mock
    private WebsiteUserService websiteUserService;

    @InjectMocks
    private ForumService forumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetForum_Success() {
        Long id = 1L;
        Pageable pageable = mock(Pageable.class);
        Forum forum = new Forum();
        forum.setId(id);
        ForumFilter forumFilter = new ForumFilter();

        when(forumRepository.findById(id)).thenReturn(Optional.of(forum));
        List<Forum> forumsList = new ArrayList<>();
        Page<Forum> forums = new PageImpl<>(forumsList);
        when(forumRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(forums);
        when(forumMapper.toDto(any(Forum.class))).thenReturn(new ForumDTO());
        when(websiteUserService.getCurrentUser()).thenThrow(new BadCredentialsException("You are not logged in"));

        Page<ForumDTO> result = forumService.getForum(id, forumFilter, pageable);

        assertNotNull(result);
        verify(forumRepository).findById(id);
        verify(forumRepository).findAll(any(Specification.class), eq(pageable));
        verify(forumMapper, times(1)).toDto(any(Forum.class));
    }

    @Test
    public void testGetForum_ForumNotFound() {
        Long id = 1L;
        Pageable pageable = mock(Pageable.class);
        ForumFilter forumFilter = new ForumFilter();
        when(forumRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> forumService.getForum(id, forumFilter, pageable));
    }

    @Test
    public void testCreateForum_Success() throws BadRequestException {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Limbus Company");
        forumDTO.setForumName("New Forum");
        forumDTO.setDescription("Description of the new forum");
        forumDTO.setParentForumId(1L);

        Game game = new Game();
        when(gameRepository.findGameByTitle(forumDTO.getGameTitle())).thenReturn(Optional.of(game));
        Forum parentForum = new Forum();
        when(forumRepository.findById(forumDTO.getParentForumId())).thenReturn(Optional.of(parentForum));
        Forum forum = new Forum();
        when(forumRepository.save(any(Forum.class))).thenReturn(forum);
        ForumDTO expectedForumDTO = new ForumDTO();
        when(forumMapper.toDto(any(Forum.class))).thenReturn(expectedForumDTO);

        ForumDTO result = forumService.createForum(forumDTO);

        assertNotNull(result);
        verify(gameRepository).findGameByTitle(forumDTO.getGameTitle());
        verify(forumRepository).findById(forumDTO.getParentForumId());
        verify(forumRepository).save(any(Forum.class));
        verify(forumMapper).toDto(forum);
    }

    @Test
    public void testCreateForum_GameNotFound() {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Gimbus Company");
        forumDTO.setParentForumId(1L);

        when(gameRepository.findGameByTitle(forumDTO.getGameTitle())).thenReturn(Optional.empty());
        BadRequestException exception = assertThrows(BadRequestException.class, () -> forumService.createForum(forumDTO));

        assertEquals("Game not found", exception.getMessage());

        verify(gameRepository).findGameByTitle(forumDTO.getGameTitle());
        verify(forumRepository, never()).save(any(Forum.class));
        verify(forumMapper, never()).toDto(any(Forum.class));
    }

    @Test
    public void testCreateForum_ParentForumNotFound() {
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Limbus Company");
        forumDTO.setParentForumId(999L);

        Game game = new Game();
        when(gameRepository.findGameByTitle(forumDTO.getGameTitle())).thenReturn(Optional.of(game));
        when(forumRepository.findById(forumDTO.getParentForumId())).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> forumService.createForum(forumDTO));
        assertEquals("Parent forum not found", exception.getMessage());

        verify(gameRepository).findGameByTitle(forumDTO.getGameTitle());
        verify(forumRepository).findById(forumDTO.getParentForumId());
        verify(forumRepository, never()).save(any(Forum.class));
        verify(forumMapper, never()).toDto(any(Forum.class));
    }

    @Test
    public void testUpdateForum_Success() throws BadRequestException {
        Long id = 1L;
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setGameTitle("Test Game");
        Forum forum = new Forum();
        Game game = new Game();
        when(forumRepository.findById(id)).thenReturn(Optional.of(forum));
        when(gameRepository.findGameByTitle(forumDTO.getGameTitle())).thenReturn(Optional.of(game));
        when(forumMapper.toDto(any())).thenReturn(forumDTO);
        when(forumRepository.save(any(Forum.class))).thenReturn(forum);

        ForumDTO result = forumService.updateForum(id, forumDTO);

        assertNotNull(result);
        verify(forumRepository).findById(id);
        verify(gameRepository).findGameByTitle(forumDTO.getGameTitle());
        verify(forumMapper).toDto(forum);
    }

    @Test
    public void testUpdateForum_ForumNotFound() {
        Long id = 1L;
        ForumDTO forumDTO = new ForumDTO();
        when(forumRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> forumService.updateForum(id, forumDTO));
        verify(forumRepository).findById(id);
    }

    @Test
    public void testDeleteForum_Success() throws BadRequestException {
        Long id = 1L;
        Forum forum = new Forum();
        forum.setId(id);
        forum.setIsDeleted(false);
        when(forumRepository.findById(id)).thenReturn(Optional.of(forum));

        boolean result = forumService.deleteForum(id, true);

        assertTrue(result);
        assertTrue(forum.getIsDeleted());
        verify(forumRepository).findById(id);
        verify(forumRepository).save(forum);
    }

    @Test
    public void testDeleteForum_ForumNotFound() {
        Long id = 1L;
        when(forumRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> forumService.deleteForum(id, true));
        verify(forumRepository).findById(id);
    }
}
