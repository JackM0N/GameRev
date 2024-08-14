package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.apache.commons.lang3.EnumUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.TagRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final TagRepository tagRepository;
    private final GameMapper gameMapper;

    @Value("${game.pics.directory}")
    private final String gamePicsDirectory = "../Pictures/game_pics";

    public GameService(GameRepository gameRepository, TagRepository tagRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.tagRepository = tagRepository;
        this.gameMapper = gameMapper;
    }

    public Page<GameDTO> getAllGames(
            LocalDate fromDate, LocalDate toDate,
            Float minUserScore, Float maxUserScore,
            List<Long> tagIds,
            List<ReleaseStatus> releaseStatuses,
            Pageable pageable
    ) {
        Specification<Game> spec = Specification.where(null);

        if (fromDate != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("releaseDate"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("releaseDate"), toDate));
        }
        if (minUserScore != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("userScore"), minUserScore));
        }
        if (maxUserScore != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("userScore"), maxUserScore));
        }
        if (releaseStatuses != null && !releaseStatuses.isEmpty()) {
            spec = spec.and((root, query, builder) -> root.get("releaseStatus").in(releaseStatuses));
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            spec = spec.and((root, query, builder) -> {
                Join<Game, Tag> tagJoin = root.join("tags");
                return tagJoin.get("id").in(tagIds);
            });
        }

        Page<Game> games = gameRepository.findAll(spec, pageable);
        return games.map(gameMapper::toDto);
    }

    public GameDTO getGameByTitle(String title) throws BadRequestException {
        Game game = gameRepository.findGameByTitle(title)
                .orElseThrow(() -> new BadRequestException("Game not found"));
        return gameMapper.toDto(game);
    }

    public GameDTO createGame(GameDTO game, MultipartFile picture) throws IOException {
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

        Path filepath = null;

        try {
            if (picture != null && !picture.isEmpty()) {
                String filename = newGame.getTitle().replaceAll("\\s+", "_").toLowerCase() + "_" + picture.getOriginalFilename();
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
                    System.err.println("Failed to delete file after an error: " + filepath.toString());
                }
            }
            throw e;
        }
    }

    public GameDTO updateGame(String title, GameDTO game, MultipartFile picture) throws IOException {
        Game updatedGame = gameRepository.findGameByTitle(title)
                .orElseThrow(() -> new BadRequestException("Game not found"));
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
        if (gameRepository.existsById(id)){
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
