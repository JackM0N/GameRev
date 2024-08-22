package pl.ttsw.GameRev;

import jakarta.persistence.criteria.Join;
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
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.service.ForumService;

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

    @InjectMocks
    private ForumService forumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetForum_Success() throws BadRequestException {
        Long id = 1L;
        Long gameId = null;
        String searchText = null;
        Pageable pageable = mock(Pageable.class);
        Forum forum = new Forum();
        forum.setId(id);
        when(forumRepository.findById(id)).thenReturn(Optional.of(forum));
        List<Forum> forumsList = new ArrayList<>();
        Page<Forum> forums = new PageImpl<>(forumsList);
        when(forumRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(forums);
        when(forumMapper.toDto(any(Forum.class))).thenReturn(new ForumDTO());

        Page<ForumDTO> result = forumService.getForum(id, gameId, searchText, pageable);

        assertNotNull(result);
        verify(forumRepository).findById(id);
        verify(forumRepository).findAll(any(Specification.class), eq(pageable));
        verify(forumMapper, times(1)).toDto(any(Forum.class));
    }

    @Test
    public void testGetForum_ForumNotFound() throws BadRequestException {
        Long id = 1L;
        Pageable pageable = mock(Pageable.class);
        when(forumRepository.findById(id)).thenReturn(Optional.empty());

        Page<ForumDTO> result = forumService.getForum(id, null, null, pageable);

        assertNull(result);
        verify(forumRepository).findById(id);
    }

    @Test
    public void testCreateForum_Success() {
        ForumDTO forumDTO = new ForumDTO();
        Forum forum = new Forum();
        when(forumMapper.toEntity(forumDTO)).thenReturn(forum);
        when(forumRepository.save(forum)).thenReturn(forum);
        when(forumMapper.toDto(forum)).thenReturn(forumDTO);

        ForumDTO result = forumService.createForum(forumDTO);

        assertNotNull(result);
        verify(forumMapper).toEntity(forumDTO);
        verify(forumRepository).save(forum);
        verify(forumMapper).toDto(forum);
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
        when(forumMapper.toDto(any(Forum.class))).thenReturn(forumDTO);

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

        boolean result = forumService.deleteForum(id);

        assertTrue(result);
        assertTrue(forum.getIsDeleted());
        verify(forumRepository).findById(id);
        verify(forumRepository).save(forum);
    }

    @Test
    public void testDeleteForum_ForumNotFound() {
        Long id = 1L;
        when(forumRepository.findById(id)).thenReturn(Optional.empty());
        
        assertThrows(BadRequestException.class, () -> forumService.deleteForum(id));
        verify(forumRepository).findById(id);
    }
}
