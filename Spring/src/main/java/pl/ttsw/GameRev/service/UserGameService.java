package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.mapper.UserGameMapper;
import pl.ttsw.GameRev.mapper.WebsiteUserMapper;
import pl.ttsw.GameRev.model.CompletionStatus;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserGame;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserGameRepository;
import pl.ttsw.GameRev.repository.CompletionStatusRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

@Service
public class UserGameService {
    private final WebsiteUserService websiteUserService;
    private final UserGameMapper userGameMapper;
    private final CompletionStatusRepository completionStatusRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final WebsiteUserMapper websiteUserMapper;
    private final WebsiteUserRepository websiteUserRepository;

    public UserGameService(CompletionStatusRepository completionStatusRepository, GameRepository gameRepository, UserGameRepository userGameRepository, WebsiteUserService websiteUserService, UserGameMapper userGameMapper,
                           WebsiteUserMapper websiteUserMapper, WebsiteUserRepository websiteUserRepository) {
        this.completionStatusRepository = completionStatusRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.websiteUserService = websiteUserService;
        this.userGameMapper = userGameMapper;
        this.websiteUserMapper = websiteUserMapper;
        this.websiteUserRepository = websiteUserRepository;
    }

    public UserGameDTO addGameToUser(UserGameDTO userGameDTO) throws BadRequestException {
        WebsiteUser user = websiteUserService.getCurrentUser();
        if (user == null) {
            throw new BadCredentialsException("You have to login first");
        }

        WebsiteUser userFromDTO = websiteUserRepository.findByUsername(userGameDTO.getUser().getUsername());
        if (userFromDTO == null) {
            throw new BadRequestException("This user does not exist");
        }
        if (!userFromDTO.equals(user)) {
            throw new BadCredentialsException("You can only add games to your own library");
        }

        Game game = gameRepository.findGameById(userGameDTO.getGame().getId());
        if (game == null) {
            throw new BadRequestException("Game not found");
        }

        CompletionStatus completionStatus = completionStatusRepository
                .findById(userGameDTO.getCompletionStatus().getId())
                .orElseThrow(() -> new BadRequestException("Completion status not found"));

        UserGame userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setCompletionStatus(completionStatus);
        userGame.setIsFavourite(false);

        user.getUserGames().add(userGame);
        game.getUserGames().add(userGame);

        return userGameMapper.toDto(userGameRepository.save(userGame));
    }
}
