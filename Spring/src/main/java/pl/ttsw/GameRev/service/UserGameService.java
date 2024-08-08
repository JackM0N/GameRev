package pl.ttsw.GameRev.service;

import org.apache.commons.lang3.EnumUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.mapper.UserGameMapper;
import pl.ttsw.GameRev.enums.CompletionStatus;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserGame;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserGameRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import java.util.Objects;

@Service
public class UserGameService {
    private final WebsiteUserService websiteUserService;
    private final UserGameMapper userGameMapper;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final WebsiteUserRepository websiteUserRepository;

    public UserGameService(GameRepository gameRepository,
                           UserGameRepository userGameRepository, WebsiteUserService websiteUserService,
                           UserGameMapper userGameMapper, WebsiteUserRepository websiteUserRepository) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.websiteUserService = websiteUserService;
        this.userGameMapper = userGameMapper;
        this.websiteUserRepository = websiteUserRepository;
    }

    public Page<UserGameDTO> getUserGameDTO(String nickname, Pageable pageable) throws BadRequestException {
        if (websiteUserRepository.findByNickname(nickname).isEmpty()) {
            throw new BadRequestException("This user doesn't exist");
        }
        Page<UserGame> userGame = userGameRepository.findByUserNickname(nickname, pageable);
        if (userGame == null || userGame.getTotalElements() == 0) {
            throw new BadRequestException("This users library is empty");
        }
        return userGame.map(userGameMapper::toDto);
    }

    public UserGameDTO addGameToUser(UserGameDTO userGameDTO) throws BadRequestException {
        WebsiteUser user = websiteUserService.getCurrentUser();
        if (user == null) {
            throw new BadCredentialsException("You have to login first");
        }

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
        userGame.setIsFavourite(false);

        user.getUserGames().add(userGame);
        game.getUserGames().add(userGame);

        return userGameMapper.toDto(userGameRepository.save(userGame));
    }

    public UserGameDTO updateGame(UserGameDTO userGameDTO) throws BadRequestException {
        UserGame userGame = userGameRepository.findById(userGameDTO.getId()).orElse(null);
        if (userGame == null) {
            throw new BadRequestException("Game not found");
        }

        WebsiteUser user = websiteUserService.getCurrentUser();
        if (user == null) {
            throw new BadCredentialsException("You have to login first");
        }

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
        if (user == null) {
            throw new BadCredentialsException("You have to login first");
        }

        UserGame userGame = userGameRepository.findById(id).orElse(null);
        if (userGame == null) {
            throw new BadRequestException("Game not found");
        }

        userGameRepository.delete(userGame);
        return true;
    }
}
