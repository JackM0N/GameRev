package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.ReleaseStatus;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.ReleaseStatusRepository;
import pl.ttsw.GameRev.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final ReleaseStatusRepository statusRepository;
    private final TagRepository tagRepository;

    public GameService(GameRepository gameRepository, ReleaseStatusRepository statusRepository,
                       TagRepository tagRepository) {
        this.gameRepository = gameRepository;
        this.statusRepository = statusRepository;
        this.tagRepository = tagRepository;
    }

    public Game createGame(GameDTO game) {
        Game newGame = new Game();
        newGame.setTitle(game.getTitle());
        newGame.setDeveloper(game.getDeveloper());
        newGame.setPublisher(game.getPublisher());
        newGame.setReleaseDate(game.getReleaseDate());
        newGame.setDescription(game.getDescription());

        ReleaseStatus releaseStatus = statusRepository.findById(game.getReleaseStatus())
                .orElseThrow(() -> new RuntimeException("Invalid release status ID"));
        newGame.setReleaseStatus(releaseStatus);

        List<Tag> tags = tagRepository.findAllById(game.getTags());
        newGame.setTags(tags);

        return gameRepository.save(newGame);
    }

    public GameDTO getGameByTitle(String title) {
        return mapToDTO(gameRepository.findGameByTitle(title));
    }

    public GameDTO mapToDTO(Game game) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(game.getId());
        gameDTO.setTitle(game.getTitle());
        gameDTO.setDeveloper(game.getDeveloper());
        gameDTO.setPublisher(game.getPublisher());
        gameDTO.setReleaseDate(game.getReleaseDate());
        gameDTO.setReleaseStatus(game.getReleaseStatus().getId());
        gameDTO.setDescription(game.getDescription());

        List<Long> tagIds = game.getTags().stream().map(Tag::getId).collect(Collectors.toList());
        gameDTO.setTags(tagIds);

        return gameDTO;
    }
}
