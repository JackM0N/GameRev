package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.dto.SimplifiedUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.filter.ForumFilter;
import pl.ttsw.GameRev.mapper.ForumMapper;
import pl.ttsw.GameRev.mapper.SimplifiedUserMapper;
import pl.ttsw.GameRev.mapper.WebsiteUserMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {
    private final ForumRepository forumRepository;
    private final ForumMapper forumMapper;
    private final GameRepository gameRepository;
    private final WebsiteUserRepository websiteUserRepository;
    private final WebsiteUserService websiteUserService;
    private final WebsiteUserMapper websiteUserMapper;
    private final SimplifiedUserMapper simplifiedUserMapper;

    public Page<ForumDTO> getForum(Long id, ForumFilter forumFilter, Pageable pageable) {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum not found"));
        Specification<Forum> spec = getForumSpecification(forumFilter, forum);

        try {
            WebsiteUser currentUser = websiteUserService.getCurrentUser();

            if(currentUser.getRoles().stream().noneMatch(role -> "Admin".equals(role.getRoleName()))){
                forumFilter.setIsDeleted(false);
            }
        }catch (Exception e){
            forumFilter.setIsDeleted(false);
        }


        Page<Forum> forums = forumRepository.findAll(spec, pageable);
        List<Forum> forumList = new ArrayList<>(forums.getContent());

        forumList.add(0, forum);

        Page<Forum> forumPage = new PageImpl<>(forumList, pageable, forums.getTotalElements());

        return forumPage.map(f -> {
            ForumDTO dto = forumMapper.toDto(f);
            dto.setTopPost(forumRepository.findTopPostForForum(dto.getId()));
            return dto;
        });
    }

    public List<SimplifiedUserDTO> getModeratorsForForum(Long id) {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum not found"));
        List<WebsiteUserDTO> forumModerators = forum.getForumModerators().stream().map(websiteUserMapper::toDto).toList();
        return forumModerators.stream().map(simplifiedUserMapper::toSimplifiedDto).toList();
    }

    public ForumDTO createForum(ForumDTO forumDTO) throws BadRequestException {
        Forum forum = forumMapper.toEntity(forumDTO);

        forum.setGame(gameRepository.findGameByTitle(forumDTO.getGameTitle())
                .orElseThrow(() -> new BadRequestException("Game not found")));
        forum.setParentForum(forumRepository.findById(forumDTO.getParentForumId())
                .orElseThrow(() -> new BadRequestException("Parent forum not found")));

        if (forumDTO.getForumModeratorsIds() != null) {
            List<WebsiteUser> moderators = websiteUserRepository.findAllById(forumDTO.getForumModeratorsIds());
            forum.setForumModerators(moderators);
        }

        forumRepository.save(forum);
        return forumMapper.toDto(forum);
    }

    public ForumDTO updateForum(Long id, ForumDTO forumDTO) throws BadRequestException {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Forum not found"));

        forumMapper.partialUpdate(forumDTO, forum);

        if (forumDTO.getGameTitle() != null) {
            Game game = gameRepository.findGameByTitle(forumDTO.getGameTitle())
                    .orElseThrow(() -> new BadRequestException("Game not found"));
            forum.setGame(game);
        }
        if (forumDTO.getParentForumId() != null){
            Forum foundForum = forumRepository.findById(forumDTO.getParentForumId())
                    .orElseThrow(() -> new BadRequestException("Parent forum not found"));
            forum.setParentForum(foundForum);
        }
        return forumMapper.toDto(forumRepository.save(forum));
    }

    public boolean deleteForum(Long id, Boolean isDeleted) throws BadRequestException {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Forum not found"));
        forum.setIsDeleted(isDeleted);
        forumRepository.save(forum);
        return true;
    }

    private static Specification<Forum> getForumSpecification(ForumFilter forumFilter, Forum forum) {
        Specification<Forum> spec = (root, query, builder) -> builder.equal(root.get("parentForum"), forum);

        spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"), forumFilter.getIsDeleted()));

        if (forumFilter.getGameId() != null) {
            spec = spec.and((root, query, builder) -> {
                Join<Forum, Game> gameJoin = root.join("game");
                return builder.equal(gameJoin.get("id"), forumFilter.getGameId());
            });
        }
        if (forumFilter.getSearchText() != null) {
            String likePattern = "%" + forumFilter.getSearchText().toLowerCase() + "%";
            spec = spec.and((root, query, builder) ->  builder.like(builder.lower(root.get("forumName")), likePattern));
        }
        return spec;
    }
}
