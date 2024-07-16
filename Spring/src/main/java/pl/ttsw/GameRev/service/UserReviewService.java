package pl.ttsw.GameRev.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public UserReviewDTO getUserReviewById(Integer id) {
        Optional<UserReview> userReview = (userReviewRepository.findById(id));

        return userReview.map(this::mapToDTO).orElse(null);
    }

    public UserReviewDTO createUserReview(UserReviewDTO userReviewDTO) {
        UserReview userReview = new UserReview();
        userReview.setUser(websiteUserRepository.findByUsername(userReviewDTO.getUserUsername()));
        userReview.setGame(gameRepository.findGameByTitle(userReviewDTO.getGameTitle()));
        userReview.setContent(userReviewDTO.getContent());
        userReview.setScore(userReviewDTO.getScore());
        userReview.setPostDate(LocalDate.now());
        userReview.setPositiveRating(0);
        userReview.setNegativeRating(0);

        return mapToDTO(userReviewRepository.save(userReview));
    }

    public UserReviewDTO updateUserReview(UserReviewDTO userReviewDTO) {
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId());
        userReview.setPostDate(LocalDate.now());
        if (userReviewDTO.getScore() != null){
            userReview.setScore(userReviewDTO.getScore());
        }
        if (userReviewDTO.getContent() != null){
            userReview.setContent(userReviewDTO.getContent());
        }
        return mapToDTO(userReviewRepository.save(userReview));
    }

    public boolean deleteUserReview(Integer userReviewId) {
        if (userReviewRepository.existsById(userReviewId)){
            userReviewRepository.deleteById(userReviewId);
            return true;
        }
        return false;
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
