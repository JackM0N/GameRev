package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.mapper.*;
import pl.ttsw.GameRev.model.*;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.ForumRequestRepository;
import pl.ttsw.GameRev.repository.GameRepository;

import jakarta.persistence.EntityNotFoundException;
import pl.ttsw.GameRev.service.ForumRequestService;
import pl.ttsw.GameRev.service.GameService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForumRequestServiceTest {

    @Mock
    private ForumRequestRepository forumRequestRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ForumRepository forumRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private GameService gameService;

    @Spy
    private ForumRequestMapper forumRequestMapper = new ForumRequestMapperImpl();

    @InjectMocks
    private ForumRequestService forumRequestService;

    @Test
    public void testGetAllForumRequests_Success() {
        Boolean approved = true;
        Pageable pageable = PageRequest.of(0, 10);
        ForumRequest forumRequest = new ForumRequest();
        Page<ForumRequest> page = new PageImpl<>(List.of(forumRequest));

        when(forumRequestRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();

        Page<ForumRequestDTO> result = forumRequestService.getAllForumRequests(approved, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(forumRequestRepository).findAll(any(Specification.class), eq(pageable));
        verify(forumRequestMapper).toDto(forumRequest);
    }

    @Test
    public void testGetForumRequestById_Success() {
        Long id = 1L;
        ForumRequest forumRequest = new ForumRequest();
        when(forumRequestRepository.findById(id)).thenReturn(Optional.of(forumRequest));

        ForumRequestDTO result = forumRequestService.getForumRequestById(id);

        assertNotNull(result);
        verify(forumRequestRepository).findById(id);
        verify(forumRequestMapper).toDto(forumRequest);
    }

    @Test
    public void testGetForumRequestById_NotFound() {
        Long id = 1L;
        when(forumRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> forumRequestService.getForumRequestById(id));
        verify(forumRequestRepository).findById(id);
    }

    @Test
    public void testCreateForumRequest_Success() throws BadRequestException {
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        Game game = new Game();
        game.setId(1L);
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(1L);

        Forum forum = new Forum();
        forum.setId(1L);
        ForumDTO forumDTO = new ForumDTO();
        forumDTO.setId(1L);

        forumRequestDTO.setForumName("Test Forum");
        forumRequestDTO.setGame(gameDTO);
        forumRequestDTO.setParentForum(forumDTO);

        ForumRequest forumRequest = new ForumRequest();
        WebsiteUser currentUser = new WebsiteUser();
        when(forumRepository.existsForumByForumName(forumRequestDTO.getForumName())).thenReturn(false);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(forumRepository.findById(1L)).thenReturn(Optional.of(forum));
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(forumRequestRepository.save(any(ForumRequest.class))).thenReturn(forumRequest);
        when(forumRequestMapper.toEntity(forumRequestDTO)).thenReturn(forumRequest);

        ForumRequestDTO result = forumRequestService.createForumRequest(forumRequestDTO);

        assertNotNull(result);
        verify(forumRepository).existsForumByForumName(forumRequestDTO.getForumName());
        verify(forumRequestMapper, times(2)).toEntity(forumRequestDTO);
        verify(gameRepository).findById(1L);
        verify(forumRepository).findById(1L);
        verify(websiteUserService).getCurrentUser();
        verify(forumRequestRepository).save(forumRequest);
        verify(forumRequestMapper).toDto(forumRequest);
    }

    @Test
    public void testCreateForumRequest_ForumNameExists() {
        ForumRequestDTO forumRequestDTO = new ForumRequestDTO();
        forumRequestDTO.setForumName("Test Forum");

        when(forumRepository.existsForumByForumName(forumRequestDTO.getForumName())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> forumRequestService.createForumRequest(forumRequestDTO));
        verify(forumRepository).existsForumByForumName(forumRequestDTO.getForumName());
    }

    @Test
    public void testDeleteForumRequest_Success() {
        Long id = 1L;
        ForumRequest forumRequest = mock(ForumRequest.class);
        WebsiteUser currentUser = new WebsiteUser();

        when(forumRequestRepository.findById(id)).thenReturn(Optional.of(forumRequest));
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(forumRequest.getAuthor()).thenReturn(currentUser);

        boolean result = forumRequestService.deleteForumRequest(id);

        assertTrue(result);
        verify(forumRequestRepository).findById(id);
        verify(forumRequestRepository).delete(forumRequest);
    }

    @Test
    public void testDeleteForumRequest_Forbidden() {
        Long id = 1L;
        ForumRequest forumRequest = mock(ForumRequest.class);
        WebsiteUser currentUser = mock(WebsiteUser.class);
        WebsiteUser otherUser = mock(WebsiteUser.class);
        when(forumRequestRepository.findById(id)).thenReturn(Optional.of(forumRequest));
        when(websiteUserService.getCurrentUser()).thenReturn(currentUser);
        when(forumRequest.getAuthor()).thenReturn(otherUser);

        assertThrows(BadCredentialsException.class, () -> forumRequestService.deleteForumRequest(id));
        verify(forumRequestRepository).findById(id);
    }
}