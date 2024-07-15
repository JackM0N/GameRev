package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserReviewService{
    private final WebsiteUserRepository websiteUserRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final WebsiteUserService websiteUserService;
    private final UserReviewRepository userReviewRepository;

    public UserReviewService(UserReviewRepository userReviewRepository, WebsiteUserRepository websiteUserRepository, GameRepository gameRepository, GameService gameService, WebsiteUserService websiteUserService) {
        this.userReviewRepository = userReviewRepository;
        this.websiteUserRepository = websiteUserRepository;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.websiteUserService = websiteUserService;
    }

    public List<UserReviewDTO> getUserReviewByGame(String title) {
        return mapListToDTO(userReviewRepository.findByGameTitle(title));
    }

    public List<UserReviewDTO> getUserReviewByUser(Long userId) {
        return mapListToDTO(userReviewRepository.findByUserId(userId));
    }

    public UserReview createUserReview(UserReviewDTO userReviewDTO) {
        UserReview userReview = new UserReview();
        userReview.setUser(websiteUserRepository.findByUsername(userReviewDTO.getUser().getUsername()));
        userReview.setGame(gameRepository.findGameByTitle(userReviewDTO.getGame().getTitle()));
        userReview.setContent(userReviewDTO.getContent());
        userReview.setPostDate(userReviewDTO.getPostDate());
        userReview.setScore(userReviewDTO.getScore());
        userReview.setPositiveRating(userReviewDTO.getPositiveRating());
        userReview.setNegativeRating(userReviewDTO.getNegativeRating());
        return userReviewRepository.save(userReview);
    }

    public UserReviewDTO mapToDTO(UserReview userReview) {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(userReview.getId());
        userReviewDTO.setGame(gameService.mapToDTO(userReview.getGame()));
        userReviewDTO.setUser(websiteUserService.mapToDTO(userReview.getUser()));
        userReviewDTO.setContent(userReview.getContent());
        userReviewDTO.setPostDate(userReview.getPostDate());
        userReviewDTO.setScore(userReview.getScore());
        userReviewDTO.setPositiveRating(userReview.getPositiveRating());
        userReviewDTO.setNegativeRating(userReview.getNegativeRating());
        return userReviewDTO;
    }

    public List<UserReviewDTO> mapListToDTO(List<UserReview> userReviewList) {
        List<UserReviewDTO> userReviewDTOList = new ArrayList<>();
        for (UserReview userReview : userReviewList) {
            userReviewDTOList.add(mapToDTO(userReview));
        }
        return userReviewDTOList;
    }
}
