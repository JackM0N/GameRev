package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
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
            UserReviewFilter userReviewFilter,
            Pageable pageable) {

        Specification<UserReview> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("game").get("title"), title)
        );

        spec = getUserReviewSpecification(userReviewFilter, spec);

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
            UserReviewFilter userReviewFilter,
            Pageable pageable) throws BadRequestException {
        WebsiteUser wsUser = websiteUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Specification<UserReview> spec = filterCheck(userReviewFilter, wsUser);

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
        return userReviews.map(userReviewMapper::toDto);
    }


    public Page<UserReviewDTO> getUserReviewByOwner(
            UserReviewFilter userReviewFilter,
            Pageable pageable) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        Specification<UserReview> spec = filterCheck(userReviewFilter, currentUser);

        Page<UserReview> userReviews = userReviewRepository.findAll(spec, pageable);
        return userReviews.map(userReviewMapper::toDto);
    }

    public UserReviewDTO getUserReviewById(Long id) {
        UserReview userReview = (userReviewRepository.findById(id))
                .orElseThrow(() -> new EntityNotFoundException("User review not found"));
        return userReviewMapper.toDto(userReview);
    }

    public Page<UserReviewDTO> getUserReviewsWithReports(UserReviewFilter userReviewFilter, Pageable pageable) {
        Specification<UserReview> spec = (root, query, builder) -> builder.isNotEmpty(root.get("reports"));

        spec = getUserReviewSpecification(userReviewFilter, spec);

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
        userReview.setUser(websiteUserService.getCurrentUser());
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
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId())
                .orElseThrow(() -> new BadRequestException("User review not found"));

        if (userReview.getUser() != websiteUserService.getCurrentUser()){
            throw new BadCredentialsException("You cant delete a review that doesn't belong to you");
        }

        userReviewRepository.deleteById(userReviewDTO.getId());
        return true;
    }

    public boolean deleteUserReviewById(Long id) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User review not found"));
        userReviewRepository.delete(userReview);
        return true;
    }

    private Specification<UserReview> filterCheck(UserReviewFilter userReviewFilter, WebsiteUser wsUser) {
        Specification<UserReview> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("user"), wsUser)
        );

        spec = getUserReviewSpecification(userReviewFilter, spec);
        return spec;
    }

    private Specification<UserReview> getUserReviewSpecification(UserReviewFilter userReviewFilter, Specification<UserReview> spec) {
        if (userReviewFilter.getPostDateFrom() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .greaterThanOrEqualTo(root.get("postDate"), userReviewFilter.getPostDateFrom()));
        }
        if (userReviewFilter.getPostDateTo() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .lessThanOrEqualTo(root.get("postDate"), userReviewFilter.getPostDateTo()));
        }
        if (userReviewFilter.getScoreFrom() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .greaterThanOrEqualTo(root.get("score"), userReviewFilter.getScoreFrom()));
        }
        if (userReviewFilter.getScoreTo() != null) {
            spec = spec.and((root, query, builder) -> builder
                    .lessThanOrEqualTo(root.get("score"), userReviewFilter.getScoreTo()));
        }
        return spec;
    }
}
