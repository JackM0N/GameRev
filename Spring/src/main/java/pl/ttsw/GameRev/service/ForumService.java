package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForumService {
    private final ForumRepository forumRepository;
    private final ForumMapper forumMapper;
    private final GameRepository gameRepository;

    public ForumService(ForumRepository forumRepository, ForumMapper forumMapper, GameRepository gameRepository) {
        this.forumRepository = forumRepository;
        this.forumMapper = forumMapper;
        this.gameRepository = gameRepository;
    }

    public Page<ForumDTO> getForum(Long id, Long gameId, String searchText , Pageable pageable) throws BadRequestException {
        Forum forum = forumRepository.findById(id).orElse(null);
        if (forum == null) {
            return null;
        }
        Specification<Forum> spec = (root, query, builder) -> builder.equal(root.get("parentForum"), forum);

        spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"),false));

        if (gameId != null) {
            spec = spec.and((root, query, builder) -> {
                Join<Forum, Game> gameJoin = root.join("game");
                return builder.equal(gameJoin.get("id"), gameId);
            });
        }
        if (searchText != null) {
            searchText = searchText.toLowerCase();
            String likePattern = "%" + searchText + "%";
            spec = spec.and((root, query, builder) ->  builder.like(builder.lower(root.get("forumName")), likePattern));
        }

        Page<Forum> forums = forumRepository.findAll(spec, pageable);
        List<Forum> forumList = new ArrayList<>(forums.getContent());

        forumList.add(0, forum);

        Page<Forum> forumPage = new PageImpl<>(forumList, pageable, forums.getTotalElements());

        return forumPage.map(f -> {
            ForumDTO dto = forumMapper.toDto(f);
            dto.setNumberOfPosts(f.getForumPosts().size());
            return dto;
        });
    }

    public ForumDTO createForum(ForumDTO forumDTO) {
        Forum forum = forumMapper.toEntity(forumDTO);
        forum.setIsDeleted(false);
        forum = forumRepository.save(forum);
        return forumMapper.toDto(forum);
    }

    public ForumDTO updateForum(Long id, ForumDTO forumDTO) throws BadRequestException {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Forum not found"));

        if (forumDTO.getGameTitle() != null) {
            Game game = gameRepository.findGameByTitle(forumDTO.getGameTitle())
                    .orElseThrow(() -> new BadRequestException("Game not found"));
            forum.setGame(game);
        }
        if (forumDTO.getForumName() != null) {
            forum.setForumName(forumDTO.getForumName());
        }
        if (forumDTO.getParentForumId() != null){
            Forum foundForum = forumRepository.findById(forumDTO.getParentForumId())
                    .orElseThrow(() -> new BadRequestException("Forum not found"));
            forum.setParentForum(foundForum);
        }
        return forumMapper.toDto(forum);
    }

    public boolean deleteForum(Long id) throws BadRequestException {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Forum not found"));
        forum.setIsDeleted(!forum.getIsDeleted());
        forumRepository.save(forum);
        return true;
    }
}
