package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.filter.UserGameFilter;
import pl.ttsw.GameRev.mapper.UserGameMapper;
import pl.ttsw.GameRev.enums.CompletionStatus;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.model.UserGame;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserGameRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserGameService {
    private final WebsiteUserService websiteUserService;
    private final UserGameMapper userGameMapper;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final WebsiteUserRepository websiteUserRepository;

    public Page<UserGameDTO> getUserGame(
            String nickname,
            UserGameFilter userGameFilter,
            Pageable pageable) throws BadRequestException {
        if (websiteUserRepository.findByNickname(nickname).isEmpty()) {
            throw new BadRequestException("This user doesn't exist");
        }

        Specification<UserGame> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("user").get("nickname"), nickname));

        if (userGameFilter.getIsFavourite() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .equal(root.get("isFavourite"), userGameFilter.getIsFavourite()));
        }
        if (userGameFilter.getCompletionStatus() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .equal(root.get("completionStatus"), userGameFilter.getCompletionStatus()));
        }
        if (userGameFilter.getTagsIds() != null && !userGameFilter.getTagsIds().isEmpty()) {
            spec = spec.and((root, query, builder) -> {
                Join<UserGame, Game> gameJoin = root.join("game");
                Join<Game, Tag> tags = gameJoin.join("tags");
                return tags.get("id").in(userGameFilter.getTagsIds());
            });
        }
        Page<UserGame> userGames = userGameRepository.findAll(spec, pageable);
        return userGames.map(userGameMapper::toDto);
    }

    public UserGameDTO addGameToUser(UserGameDTO userGameDTO) throws BadRequestException {
        WebsiteUser user = websiteUserService.getCurrentUser();

        WebsiteUser userFromDTO = websiteUserRepository.findByUsername(userGameDTO.getUser().getUsername())
                .orElseThrow(() -> new BadRequestException("This user doesn't exist"));
        if (userFromDTO == null) {
            throw new BadRequestException("This user does not exist");
        }
        if (!userFromDTO.equals(user)) {
            throw new BadCredentialsException("You can only add games to your own library");
        }

        Game game = gameRepository.findGameById(userGameDTO.getGame().getId())
                .orElseThrow(() -> new BadRequestException("This game doesn't exist"));

        CompletionStatus completionStatus = userGameDTO.getCompletionStatus();
        if (completionStatus == null || !EnumUtils.isValidEnumIgnoreCase(CompletionStatus.class, completionStatus.name())) {
            throw new BadRequestException("Completion status not found");
        }

        UserGame userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setCompletionStatus(completionStatus);
        userGame.setIsFavourite(userGameDTO.getIsFavourite());

        user.getUserGames().add(userGame);
        game.getUserGames().add(userGame);

        return userGameMapper.toDto(userGameRepository.save(userGame));
    }

    public UserGameDTO updateGame(UserGameDTO userGameDTO) throws BadRequestException {
        UserGame userGame = userGameRepository.findById(userGameDTO.getId())
                .orElseThrow(() -> new BadRequestException("This game doesn't exist"));
        WebsiteUser user = websiteUserService.getCurrentUser();

        if (!Objects.equals(userGame.getUser(), user)) {
            throw new BadCredentialsException("You can only update your own library");
        }

        if (userGame.getIsFavourite() != null) {
            userGame.setIsFavourite(userGameDTO.getIsFavourite());
        }
        if (userGame.getCompletionStatus() != null) {
            CompletionStatus completionStatus = userGameDTO.getCompletionStatus();
            if (completionStatus == null || !EnumUtils.isValidEnumIgnoreCase(CompletionStatus.class, completionStatus.name())) {
                throw new BadRequestException("Completion status not found");
            }
            userGame.setCompletionStatus(completionStatus);
        }
        return userGameMapper.toDto(userGameRepository.save(userGame));
    }

    public boolean deleteGame(Long id) throws BadRequestException {
        WebsiteUser user = websiteUserService.getCurrentUser();

        UserGame userGame = userGameRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("This game doesn't exist"));

        if (!Objects.equals(userGame.getUser(), user)) {
            throw new BadCredentialsException("You can only delete a game from your own library");
        }

        userGameRepository.delete(userGame);
        return true;
    }
}
