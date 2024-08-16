package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.repository.ForumRepository;

@Service
public class ForumService {
    private final ForumRepository forumRepository;
    private final ForumMapper forumMapper;

    public ForumService(ForumRepository forumRepository, ForumMapper forumMapper) {
        this.forumRepository = forumRepository;
        this.forumMapper = forumMapper;
    }

    public Page<ForumDTO> getForum(Long id, Long gameId, String searchText , Pageable pageable) {
        Specification<Forum> spec = (root, query, builder) -> builder.equal(root.get("id"), id);

        spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"),false));

        if (gameId != null) {
            spec = spec.and((root, query, builder) -> {
                Join<Forum, Game> gameJoin = root.join("game");
                return builder.equal(gameJoin.get("id"), gameId);
            });
        }
        if (searchText != null) {
            String likePattern = "%" + searchText + "%";
            spec = spec.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("forumName")), likePattern)
                    // TODO: Add searching for post titles
            ));
        }

        Page<Forum> forums = forumRepository.findAll(spec, pageable);
        return forums.map(forumMapper::toDto);
    }

    public ForumDTO createForum(ForumDTO forumDTO) {
        Forum forum = forumMapper.toEntity(forumDTO);
        forum = forumRepository.save(forum);
        return forumMapper.toDto(forum);
    }

    public ForumDTO updateForum(Long id, ForumDTO forumDTO) throws BadRequestException {
        Forum forum = forumRepository.findById(id).orElse(null);
        if (forum == null) {
            throw new BadRequestException("Forum not found");
        }
        forum = forumMapper.toEntity(forumDTO);
        forum = forumRepository.save(forum);
        return forumMapper.toDto(forum);
    }

    public boolean deleteForum(Long id) throws BadRequestException {
        Forum forum = forumRepository.findById(id).orElse(null);
        if (forum == null) {
            throw new BadRequestException("Forum not found");
        }
        forum.setIsDeleted(!forum.getIsDeleted());
        return true;
    }
}
