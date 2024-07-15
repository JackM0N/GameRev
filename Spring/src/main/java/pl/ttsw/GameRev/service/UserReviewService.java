package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserReviewService{
    private final WebsiteUserRepository websiteUserRepository;
    private final GameRepository gameRepository;
    private final UserReviewRepository userReviewRepository;

    public UserReviewService(UserReviewRepository userReviewRepository, WebsiteUserRepository websiteUserRepository, GameRepository gameRepository, GameService gameService, WebsiteUserService websiteUserService) {
        this.userReviewRepository = userReviewRepository;
        this.websiteUserRepository = websiteUserRepository;
        this.gameRepository = gameRepository;
    }

    public List<UserReviewDTO> getUserReviewByGame(String title) {
        List<UserReview> userReviews = userReviewRepository.findByGameTitle(title);
        return userReviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserReviewDTO> getUserReviewByUser(Long userId) {
        List<UserReview> userReviews = (userReviewRepository.findByUserId(userId));
        return userReviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserReviewDTO createUserReview(UserReviewDTO userReviewDTO) {
        UserReview userReview = new UserReview();
        userReview.setUser(websiteUserRepository.findByUsername(userReviewDTO.getUserUsername()));
        userReview.setGame(gameRepository.findGameByTitle(userReviewDTO.getGameTitle()));
        userReview.setContent(userReviewDTO.getContent());
        userReview.setPostDate(userReviewDTO.getPostDate());
        userReview.setScore(userReviewDTO.getScore());
        userReview.setPositiveRating(0);
        userReview.setNegativeRating(0);

        return mapToDTO(userReviewRepository.save(userReview));
    }

    public UserReviewDTO mapToDTO(UserReview userReview) {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(userReview.getId());
        userReviewDTO.setGameTitle(userReview.getGame().getTitle());
        userReviewDTO.setUserUsername(userReview.getUser().getUsername());
        userReviewDTO.setContent(userReview.getContent());
        userReviewDTO.setPostDate(userReview.getPostDate());
        userReviewDTO.setScore(userReview.getScore());
        userReviewDTO.setPositiveRating(userReview.getPositiveRating());
        userReviewDTO.setNegativeRating(userReview.getNegativeRating());
        return userReviewDTO;
    }
}
