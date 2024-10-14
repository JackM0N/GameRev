package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.mapper.ForumRequestMapper;
import pl.ttsw.GameRev.model.ForumRequest;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.ForumRequestRepository;
import pl.ttsw.GameRev.repository.GameRepository;

@Service
@RequiredArgsConstructor
public class ForumRequestService {
    private final ForumRequestRepository forumRequestRepository;
    private final GameRepository gameRepository;
    private final ForumRepository forumRepository;
    private final WebsiteUserService websiteUserService;
    private final ForumRequestMapper forumRequestMapper;

    public Page<ForumRequestDTO> getAllForumRequests(Boolean approved, Pageable pageable) {
        Page<ForumRequest> forumRequests;

        if (approved != null) {
            Specification<ForumRequest> spec = (root, query, builder) -> builder.equal(root.get("approved"), approved);

            forumRequests = forumRequestRepository.findAll(spec, pageable);
        } else {
            forumRequests = forumRequestRepository.findAll(pageable);
        }

        return forumRequests.map(forumRequestMapper::toDto);
    }

    public Page<ForumRequestDTO> getAllForumRequestsByOwner(Boolean approved, Pageable pageable) {
        Specification<ForumRequest> spec = (root, query, builder) -> builder.equal(root.get("author"), websiteUserService.getCurrentUser());
        if (approved != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("approved"), approved));
        }

        Page<ForumRequest> forumRequests = forumRequestRepository.findAll(spec, pageable);
        return forumRequests.map(forumRequestMapper::toDto);
    }

    public ForumRequestDTO getForumRequestById(Long id) {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum request not found"));
        return forumRequestMapper.toDto(forumRequest);
    }


    public ForumRequestDTO createForumRequest(ForumRequestDTO forumRequestDTO) throws BadRequestException {
        if (forumRepository.existsForumByForumName(forumRequestDTO.getForumName())) {
            throw new BadRequestException("A forum with this name already exists.");
        }

        ForumRequest forumRequest = forumRequestMapper.toEntity(forumRequestDTO);

        forumRequest.setGame(gameRepository.findById(forumRequestDTO.getGame().getId())
                .orElseThrow(() -> new RuntimeException("Game not found")));
        forumRequest.setParentForum(forumRepository.findById(forumRequestDTO.getParentForum().getId())
                .orElseThrow(() -> new RuntimeException("Parent forum not found")));
        forumRequest.setAuthor(websiteUserService.getCurrentUser());

        forumRequest = forumRequestRepository.save(forumRequest);
        return forumRequestMapper.toDto(forumRequest);
    }

    public ForumRequestDTO updateForumRequest(Long id, ForumRequestDTO forumRequestDTO) {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        boolean isAuthor = forumRequest.getAuthor().equals(currentUser);
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "Admin".equals(role.getRoleName()));
        if (isAuthor || isAdmin) {
            forumRequest = forumRequestMapper.partialUpdate(forumRequestDTO, forumRequest);

            if (forumRequestDTO.getGame() != null) {
                forumRequest.setGame(gameRepository.findById(forumRequestDTO.getGame().getId())
                        .orElseThrow(() -> new RuntimeException("Game not found")));
            }
            if (forumRequestDTO.getParentForum() != null) {
                forumRequest.setParentForum(forumRepository.findById(forumRequestDTO.getParentForum().getId())
                        .orElseThrow(() -> new RuntimeException("Parent forum not found")));
            }
            return forumRequestMapper.toDto(forumRequestRepository.save(forumRequest));
        } else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public ForumRequestDTO approveForumRequest(Long id, Boolean approval) {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        forumRequest.setApproved(approval);
        return forumRequestMapper.toDto(forumRequestRepository.save(forumRequest));
    }

    public boolean deleteForumRequest(Long id) {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        boolean isAuthor = forumRequest.getAuthor().equals(currentUser);
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "Admin".equals(role.getRoleName()));

        if (isAuthor || isAdmin) {
            forumRequestRepository.delete(forumRequest);
            return true;
        } else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }
}
