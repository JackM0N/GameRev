package pl.ttsw.GameRev.service;

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

    public ForumRequestDTO createForumRequest(ForumRequestDTO forumRequestDTO) {
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
}
