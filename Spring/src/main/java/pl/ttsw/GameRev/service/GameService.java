package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.dto.TagDTO;
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

        ReleaseStatus releaseStatus = statusRepository.findById(game.getReleaseStatus().getId())
                .orElseThrow(() -> new RuntimeException("Invalid release status ID"));
        newGame.setReleaseStatus(releaseStatus);

        List<Tag> tags = game.getTags().stream()
                .map(tagDTO -> tagRepository.findById(tagDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Invalid tag ID")))
                .collect(Collectors.toList());
        newGame.setTags(tags);


        return gameRepository.save(newGame);
    }

    public GameDTO getGameByTitle(String title) {
        return mapToDTO(gameRepository.findGameByTitle(title));
    }

    public List<GameDTO> getAllGames() {
        List<Game> games = gameRepository.findAll();
        return games.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public GameDTO mapToDTO(Game game) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setId(game.getId());
        gameDTO.setTitle(game.getTitle());
        gameDTO.setDeveloper(game.getDeveloper());
        gameDTO.setPublisher(game.getPublisher());
        gameDTO.setReleaseDate(game.getReleaseDate());
        gameDTO.setDescription(game.getDescription());

        gameDTO.setReleaseStatus(mapReleaseStatusToDTO(game.getReleaseStatus()));

        gameDTO.setTags(game.getTags().stream()
                .map(this::mapTagToDTO)
                .collect(Collectors.toList()));

        return gameDTO;
    }

    private ReleaseStatusDTO mapReleaseStatusToDTO(ReleaseStatus releaseStatus) {
        ReleaseStatusDTO statusDTO = new ReleaseStatusDTO();
        statusDTO.setId(releaseStatus.getId());
        statusDTO.setStatusName(releaseStatus.getStatusName());
        return statusDTO;
    }

    private TagDTO mapTagToDTO(Tag tag) {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setTagName(tag.getTagName());
        tagDTO.setPriority(tag.getPriority());
        return tagDTO;
    }
}
