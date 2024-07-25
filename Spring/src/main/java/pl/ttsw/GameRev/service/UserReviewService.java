package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.UserReviewMapper;
import pl.ttsw.GameRev.model.Rating;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.GameRepository;
import pl.ttsw.GameRev.repository.RatingRepository;
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
    private final RatingRepository ratingRepository;
    private final WebsiteUserService websiteUserService;
    private final UserReviewMapper userReviewMapper;

    public UserReviewService(UserReviewRepository userReviewRepository, WebsiteUserRepository websiteUserRepository, GameRepository gameRepository, RatingRepository ratingRepository, WebsiteUserService websiteUserService, UserReviewMapper userReviewMapper) {
        this.userReviewRepository = userReviewRepository;
        this.websiteUserRepository = websiteUserRepository;
        this.gameRepository = gameRepository;
        this.ratingRepository = ratingRepository;
        this.websiteUserService = websiteUserService;
        this.userReviewMapper = userReviewMapper;
    }

    public Page<UserReviewDTO> getUserReviewByGame(String title, Pageable pageable) {
        Page<UserReview> userReviews = userReviewRepository.findByGameTitle(title, pageable);
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        List<UserReviewDTO> userReviewDTOList = userReviews.stream().map(userReview -> {
            UserReviewDTO userReviewDTO = userReviewMapper.toDto(userReview);

            Optional<Rating> ratingOptional = ratingRepository.findByUserAndUserReview(currentUser, userReview);
            ratingOptional.ifPresentOrElse(rating -> {
                userReviewDTO.setOwnRatingIsPositive(rating.getIsPositive());
            }, () -> userReviewDTO.setOwnRatingIsPositive(null));

            return userReviewDTO;
        }).toList();

        return new PageImpl<>(userReviewDTOList, pageable, userReviews.getTotalElements());
    }

    public List<UserReviewDTO> getUserReviewByUser(Long userId) {
        List<UserReview> userReviews = (userReviewRepository.findByUserId(userId));
        return userReviews.stream()
                .map(userReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserReviewDTO getUserReviewById(Integer id) {
        Optional<UserReview> userReview = (userReviewRepository.findById(id));
        return userReview.map(userReviewMapper::toDto).orElse(null);
    }

    public Page<UserReviewDTO> getUserReviewsWithReports(Pageable pageable) {
        Page<UserReview> userReviews = userReviewRepository.findWithReports(pageable);
        return userReviews.map(userReview -> {
            UserReviewDTO userReviewDTO = userReviewMapper.toDto(userReview);
            long totalReports = userReview.getReports().size();
            long approvedReports = userReview.getReports().stream()
                    .filter(report -> report.getApproved() != null && report.getApproved())
                    .count();
            userReviewDTO.setTotalReports(totalReports);
            userReviewDTO.setApprovedReports(approvedReports);
            return userReviewDTO;
        });
    }

    public UserReviewDTO createUserReview(UserReviewDTO userReviewDTO) throws BadRequestException {
        UserReview userReview = new UserReview();
        WebsiteUser websiteUser = websiteUserRepository.findByUsername(userReviewDTO.getUserUsername());

        if (websiteUser == null) {
            throw new BadRequestException("Your user could not be found");
        }
        if (websiteUser != websiteUserService.getCurrentUser()){
            throw new BadCredentialsException("You can only make reviews on your own behalf");
        }

        userReview.setUser(websiteUserRepository.findByUsername(userReviewDTO.getUserUsername()));
        userReview.setGame(gameRepository.findGameByTitle(userReviewDTO.getGameTitle()));
        userReview.setContent(userReviewDTO.getContent());
        userReview.setScore(userReviewDTO.getScore());
        userReview.setPostDate(LocalDate.now());
        userReview.setPositiveRating(0);
        userReview.setNegativeRating(0);

        return userReviewMapper.toDto(userReviewRepository.save(userReview));
    }

    public UserReviewDTO updateUserReview(UserReviewDTO userReviewDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId());

        if (userReview == null) {
            throw new BadRequestException("This review doesn't exist");
        }
        if (userReview.getUser() != websiteUserService.getCurrentUser()) {
            throw new BadCredentialsException("You cant update this review");
        }

        userReview.setPostDate(LocalDate.now());
        if (userReviewDTO.getScore() != null){
            userReview.setScore(userReviewDTO.getScore());
        }
        if (userReviewDTO.getContent() != null){
            userReview.setContent(userReviewDTO.getContent());
        }

        return userReviewMapper.toDto(userReviewRepository.save(userReview));
    }

    public boolean deleteUserReviewByOwner(UserReviewDTO userReviewDTO) {
        WebsiteUser websiteUser = websiteUserRepository.findByUsername(userReviewDTO.getUserUsername());
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId());

        if (websiteUser != websiteUserService.getCurrentUser()){
            throw new BadCredentialsException("You cant delete a review that doesn't belong to you");
        }

        if (userReview != null){
            userReviewRepository.deleteById(Math.toIntExact(userReviewDTO.getId()));
            return true;
        }
        return false;
    }

    public boolean deleteUserReviewById(Long id) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(id);
        if (userReview == null){
            throw new BadRequestException("This review doesn't exist");
        }
        userReviewRepository.delete(userReview);
        return true;
    }
}
