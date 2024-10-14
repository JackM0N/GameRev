package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.filter.GameFilter;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final TagRepository tagRepository;
    private final GameMapper gameMapper;

    @Value("${game.pics.directory}")
    private final String gamePicsDirectory = "../Pictures/game_pics";


    public Page<GameDTO> getAllGames(GameFilter gameFilter, Pageable pageable) {
        Specification<Game> spec = Specification.where(null);

        if (gameFilter.getFromDate() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .greaterThanOrEqualTo(root.get("releaseDate"), gameFilter.getFromDate()));
        }
        if (gameFilter.getToDate() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .lessThanOrEqualTo(root.get("releaseDate"), gameFilter.getToDate()));
        }
        if (gameFilter.getMinUserScore() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .greaterThanOrEqualTo(root.get("usersScore"), gameFilter.getMinUserScore()));
        }
        if (gameFilter.getMaxUserScore() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .lessThanOrEqualTo(root.get("usersScore"), gameFilter.getMaxUserScore()));
        }
        if (gameFilter.getReleaseStatuses() != null && !gameFilter.getReleaseStatuses().isEmpty()) {
            spec = spec.and((root, query, builder) -> root.get("releaseStatus").in(gameFilter.getReleaseStatuses()));
        }
        if (gameFilter.getTagIds() != null && !gameFilter.getTagIds().isEmpty()) {
            spec = spec.and((root, query, builder) -> {
                Join<Game, Tag> tagJoin = root.join("tags");
                return tagJoin.get("id").in(gameFilter.getTagIds());
            });
        }
        if (gameFilter.getSearchText() != null) {
            String likePattern = "%" + gameFilter.getSearchText().toLowerCase() + "%";

            spec = spec.and((root, query, builder) -> {
                Predicate titlePredicate = builder.like(builder.lower(root.get("title")), likePattern);
                Predicate developerPredicate = builder.like(builder.lower(root.get("developer")), likePattern);
                Predicate descriptionPredicate = builder.like(builder.lower(root.get("description")), likePattern);
                Predicate publisherPredicate = builder.like(builder.lower(root.get("publisher")), likePattern);

                return builder.or(titlePredicate, developerPredicate, descriptionPredicate, publisherPredicate);
            });
        }

        Page<Game> games = gameRepository.findAll(spec, pageable);
        return games.map(gameMapper::toDto);
    }

    public GameDTO getGameByTitle(String title) {
        title = title.replaceAll("-", " ");

        Game game = gameRepository.findGameByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));
        return gameMapper.toDto(game);
    }

    public GameDTO createGame(GameDTO game, MultipartFile picture) throws IOException {
        Game newGame = gameMapper.toEntity(game);

        Path filepath = null;

        try {
            if (picture != null && !picture.isEmpty()) {
                String filename = newGame.getTitle().replaceAll("\\s+", "_")
                        .toLowerCase() + "_" + picture.getOriginalFilename();
                filepath = Paths.get(gamePicsDirectory, filename);
                Files.copy(picture.getInputStream(), filepath);

                newGame.setPicture(filepath.toString());
            }

            List<Tag> tags = game.getTags().stream()
                    .map(tagDTO -> tagRepository.findById(tagDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Invalid tag ID")))
                    .collect(Collectors.toList());
            newGame.setTags(tags);

            return gameMapper.toDto(gameRepository.save(newGame));
        } catch (Exception e) {
            if (filepath != null && Files.exists(filepath)) {
                try {
                    Files.delete(filepath);
                } catch (IOException ioException) {
                    System.err.println("Failed to delete file after an error: " + filepath);
                }
            }
            throw e;
        }
    }

    public GameDTO updateGame(String title, GameDTO game, MultipartFile picture) throws IOException {
        Game updatedGame = gameRepository.findGameByTitle(title)
                .orElseThrow(() -> new BadRequestException("Game not found"));

        gameMapper.partialUpdate(game, updatedGame);

        if (game.getTags() != null) {
            List<Tag> tags = game.getTags().stream()
                    .map(tagDTO -> tagRepository.findById(tagDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Invalid tag ID")))
                    .collect(Collectors.toList());
            updatedGame.setTags(tags);
        }
        if (picture != null && !picture.isEmpty()) {
            String oldPicturePath = updatedGame.getPicture();
            if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
                Path oldFilepath = Paths.get(oldPicturePath);
                Files.deleteIfExists(oldFilepath);
            }

            String filename = updatedGame.getTitle().replaceAll("\\s+", "_")
                    .toLowerCase() + "_" + picture.getOriginalFilename();
            Path filepath = Paths.get(gamePicsDirectory, filename);
            Files.copy(picture.getInputStream(), filepath);

            updatedGame.setPicture(filepath.toString());
        }
        return gameMapper.toDto(gameRepository.save(updatedGame));
    }

    public boolean deleteGame(Long id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
