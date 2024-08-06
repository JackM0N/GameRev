package pl.ttsw.GameRev.service;

import org.apache.commons.lang3.EnumUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final TagRepository tagRepository;
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository, TagRepository tagRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.tagRepository = tagRepository;
        this.gameMapper = gameMapper;
    }

    public Page<GameDTO> getAllGames(Pageable pageable) {
        Page<Game> games = gameRepository.findAll(pageable);
        return games.map(gameMapper::toDto);
    }

    public GameDTO getGameById(Long id) {
        return gameMapper.toDto(gameRepository.findGameById(id));
    }

    public GameDTO getGameByTitle(String title) {
        return gameMapper.toDto(gameRepository.findGameByTitle(title));
    }

    public Game createGame(GameDTO game) throws BadRequestException {
        Game newGame = new Game();
        newGame.setTitle(game.getTitle());
        newGame.setDeveloper(game.getDeveloper());
        newGame.setPublisher(game.getPublisher());
        newGame.setReleaseDate(game.getReleaseDate());
        newGame.setDescription(game.getDescription());
        newGame.setUsersScore(0.0f);

        ReleaseStatus releaseStatus = game.getReleaseStatus();
        if (releaseStatus == null || !EnumUtils.isValidEnumIgnoreCase(ReleaseStatus.class, releaseStatus.name())) {
                throw new BadRequestException("Release status not found");
        }

        newGame.setReleaseStatus(game.getReleaseStatus());

        List<Tag> tags = game.getTags().stream()
                .map(tagDTO -> tagRepository.findById(tagDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Invalid tag ID")))
                .collect(Collectors.toList());
        newGame.setTags(tags);

        return gameRepository.save(newGame);
    }

    public GameDTO updateGame(String title, GameDTO game) throws BadRequestException {
        Game updatedGame = gameRepository.findGameByTitle(title);
        if (game.getTitle() != null) {
            updatedGame.setTitle(game.getTitle());
        }
        if (game.getDeveloper() != null) {
            updatedGame.setDeveloper(game.getDeveloper());
        }
        if (game.getPublisher() != null) {
            updatedGame.setPublisher(game.getPublisher());
        }
        if (game.getReleaseDate() != null) {
            updatedGame.setReleaseDate(game.getReleaseDate());
        }
        if (game.getDescription() != null) {
            updatedGame.setDescription(game.getDescription());
        }
        if (game.getReleaseStatus() != null) {
            ReleaseStatus releaseStatus = game.getReleaseStatus();
            if (!EnumUtils.isValidEnumIgnoreCase(ReleaseStatus.class, releaseStatus.name())) {
                throw new BadRequestException("Release status not found");
            }
            updatedGame.setReleaseStatus(releaseStatus);
        }
        if (game.getTags() != null) {
            List<Tag> tags = game.getTags().stream()
                    .map(tagDTO -> tagRepository.findById(tagDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Invalid tag ID")))
                    .collect(Collectors.toList());
            updatedGame.setTags(tags);
        }
        return gameMapper.toDto(gameRepository.save(updatedGame));
    }

    public boolean deleteGame(Long id) {
        if (gameRepository.existsById(id)){
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
