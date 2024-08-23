package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.mapper.ForumRequestMapper;
import pl.ttsw.GameRev.model.ForumRequest;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.ForumRequestRepository;
import pl.ttsw.GameRev.repository.GameRepository;

@Service
public class ForumRequestService {
    private final ForumRequestRepository forumRequestRepository;
    private final GameRepository gameRepository;
    private final ForumRepository forumRepository;
    private final WebsiteUserService websiteUserService;
    private final ForumRequestMapper forumRequestMapper;

    public ForumRequestService(ForumRequestRepository forumRequestRepository, GameRepository gameRepository, ForumRepository forumRepository, WebsiteUserService websiteUserService, ForumRequestMapper forumRequestMapper) {
        this.forumRequestRepository = forumRequestRepository;
        this.gameRepository = gameRepository;
        this.forumRepository = forumRepository;
        this.websiteUserService = websiteUserService;
        this.forumRequestMapper = forumRequestMapper;
    }

    public Page<ForumRequestDTO> getAllForumRequests(Boolean approved, Pageable pageable) {
        Specification<ForumRequest> spec = (root, query, builder) -> builder.equal(root.get("approved"), approved);

        Page<ForumRequest> forumRequests = forumRequestRepository.findAll(spec, pageable);
        return forumRequests.map(forumRequestMapper::toDto);
    }

    public Page<ForumRequestDTO> getAllForumRequestsByUser(Boolean approved, Pageable pageable) {
        Specification<ForumRequest> spec = (root, query, builder) -> builder.equal(root.get("approved"), approved);
        spec = spec.and((root, query, builder) -> builder.equal(root.get("author"), websiteUserService.getCurrentUser()));

        Page<ForumRequest> forumRequests = forumRequestRepository.findAll(spec, pageable);
        return forumRequests.map(forumRequestMapper::toDto);
    }

    public ForumRequestDTO getForumRequestById(Long id) {
        ForumRequest forumRequest = forumRequestRepository.findById(id).orElse(null);
        if(forumRequest == null) {
            return null;
        }
        return forumRequestMapper.toDto(forumRequest);
    }


    public ForumRequestDTO createForumRequest(ForumRequestDTO forumRequestDTO) throws BadRequestException {
        if(forumRepository.existsForumByForumName(forumRequestDTO.getForumName())){
            throw new BadRequestException("Forum name already exists");
        }
        ForumRequest forumRequest = new ForumRequest();
        forumRequest.setForumName(forumRequestDTO.getForumName());
        forumRequest.setDescription(forumRequestDTO.getDescription());
        forumRequest.setGame(gameRepository.findById(forumRequestDTO.getGame().getId())
                .orElseThrow(() -> new RuntimeException("Game not found")));
        forumRequest.setParentForum(forumRepository.findById(forumRequestDTO.getParentForum().getId())
                .orElseThrow(() -> new RuntimeException("Parent forum not found")));
        forumRequest.setAuthor(websiteUserService.getCurrentUser());
        forumRequest = forumRequestRepository.save(forumRequest);
        return forumRequestMapper.toDto(forumRequest);
    }

    public ForumRequestDTO updateForumRequest(Long id, ForumRequestDTO forumRequestDTO) throws BadRequestException {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        if (forumRequestDTO.getGame().getId() != null){
            forumRequest.setGame(gameRepository.findById(forumRequestDTO.getGame().getId())
                    .orElseThrow(() -> new RuntimeException("Game not found")));
        }
        if (forumRequestDTO.getForumName() != null) {
            forumRequest.setForumName(forumRequestDTO.getForumName());
        }
        if (forumRequestDTO.getDescription() != null) {
            forumRequest.setDescription(forumRequestDTO.getDescription());
        }
        if (forumRequestDTO.getParentForum() != null) {
            forumRequest.setParentForum(forumRepository.findById(forumRequestDTO.getParentForum().getId())
                    .orElseThrow(() -> new RuntimeException("Parent forum not found")));
        }
        return forumRequestMapper.toDto(forumRequestRepository.save(forumRequest));
    }

    public ForumRequestDTO approveForumRequest(Long id, Boolean approval) throws BadRequestException {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        forumRequest.setApproved(approval);
        return forumRequestMapper.toDto(forumRequestRepository.save(forumRequest));
    }

    public boolean deleteForumRequest(Long id) {
        ForumRequest forumRequest = forumRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum request not found"));
        forumRequestRepository.delete(forumRequest);
        return true;
    }
}
