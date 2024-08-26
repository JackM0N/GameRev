package pl.ttsw.GameRev.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

@Service
@RequiredArgsConstructor
public class UserReviewService{
    private final WebsiteUserRepository websiteUserRepository;
    private final GameRepository gameRepository;
    private final UserReviewRepository userReviewRepository;
    private final RatingRepository ratingRepository;
    private final WebsiteUserService websiteUserService;
    private final UserReviewMapper userReviewMapper;

    public Page<UserReviewDTO> getUserReviewByGame(
            String title,
            LocalDate postDateFrom, LocalDate postDateTo,
            Integer scoreFrom, Integer scoreTo,
            Pageable pageable) {

        Specification<UserReview> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("game").get("title"), title)
        );

        if (postDateFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), postDateFrom));
        }
        if (postDateTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), postDateTo));
        }
        if (scoreFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), scoreFrom));
        }
        if (scoreTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), scoreTo));
        }

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
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

    public Page<UserReviewDTO> getUserReviewByUser(
            Long userId,
            LocalDate postDateFrom, LocalDate postDateTo,
            Integer scoreFrom, Integer scoreTo,
            Pageable pageable) throws BadRequestException {
        WebsiteUser wsUser = websiteUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Specification<UserReview> spec = filterCheck(postDateFrom, postDateTo, scoreFrom, scoreTo, wsUser);

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
        if (userReviews.getTotalElements() == 0) {
            throw new BadRequestException("This user didn't review any games yet");
        }
        return userReviews.map(userReviewMapper::toDto);
    }


    public Page<UserReviewDTO> getUserReviewByOwner(
            LocalDate postDateFrom, LocalDate postDateTo,
            Integer scoreFrom, Integer scoreTo,
            Pageable pageable) throws BadRequestException {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        if (currentUser == null) {
            throw new BadCredentialsException("You have to login first");
        }

        Specification<UserReview> spec = filterCheck(postDateFrom, postDateTo, scoreFrom, scoreTo, currentUser);

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
        if (userReviews.getTotalElements() == 0) {
            throw new BadRequestException("You haven't reviewed any games yet");
        }
        return userReviews.map(userReviewMapper::toDto);
    }

    public UserReviewDTO getUserReviewById(Long id) {
        Optional<UserReview> userReview = (userReviewRepository.findById(id));
        return userReview.map(userReviewMapper::toDto).orElse(null);
    }

    public Page<UserReviewDTO> getUserReviewsWithReports(
            LocalDate postDateFrom,
            LocalDate postDateTo,
            Integer scoreFrom,
            Integer scoreTo,
            Pageable pageable) {
        Specification<UserReview> spec = (root, query, builder) -> builder.isNotEmpty(root.get("reports"));

        if (postDateFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), postDateFrom));
        }
        if (postDateTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), postDateTo));
        }
        if (scoreFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), scoreFrom));
        }
        if (scoreTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), scoreTo));
        }

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
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
        WebsiteUser websiteUser = websiteUserRepository.findByUsername(userReviewDTO.getUserUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (websiteUser == null) {
            throw new BadRequestException("Your user could not be found");
        }
        if (websiteUser != websiteUserService.getCurrentUser()){
            throw new BadCredentialsException("You can only make reviews on your own behalf");
        }

        userReview.setUser(websiteUser);
        userReview.setGame(gameRepository.findGameByTitle(userReviewDTO.getGameTitle())
                .orElseThrow(() -> new BadRequestException("Game not found")));
        userReview.setContent(userReviewDTO.getContent());
        userReview.setScore(userReviewDTO.getScore());
        userReview.setPostDate(LocalDate.now());
        userReview.setPositiveRating(0);
        userReview.setNegativeRating(0);

        return userReviewMapper.toDto(userReviewRepository.save(userReview));
    }

    public UserReviewDTO updateUserReview(UserReviewDTO userReviewDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId())
                .orElseThrow(() -> new BadRequestException("User review not found"));

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

    public boolean deleteUserReviewByOwner(UserReviewDTO userReviewDTO) throws BadRequestException {
        WebsiteUser websiteUser = websiteUserRepository.findByUsername(userReviewDTO.getUserUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId())
                .orElseThrow(() -> new BadCredentialsException("User review not found"));

        if (websiteUser != websiteUserService.getCurrentUser()){
            throw new BadCredentialsException("You cant delete a review that doesn't belong to you");
        }

        if (userReview != null){
            userReviewRepository.deleteById(userReviewDTO.getId());
            return true;
        }
        return false;
    }

    public boolean deleteUserReviewById(Long id) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User review not found"));
        userReviewRepository.delete(userReview);
        return true;
    }

    private Specification<UserReview> filterCheck(LocalDate postDateFrom, LocalDate postDateTo, Integer scoreFrom, Integer scoreTo, WebsiteUser wsUser) {
        Specification<UserReview> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("user"), wsUser)
        );

        if (postDateFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), postDateFrom));
        }
        if (postDateTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), postDateTo));
        }
        if (scoreFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), scoreFrom));
        }
        if (scoreTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), scoreTo));
        }
        return spec;
    }
}
