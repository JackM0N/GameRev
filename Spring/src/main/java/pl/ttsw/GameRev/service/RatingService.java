package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.RatingDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.RatingMapper;
import pl.ttsw.GameRev.model.Rating;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.repository.RatingRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import java.util.Optional;

@Service
public class RatingService {
    private final UserReviewRepository userReviewRepository;
    private final WebsiteUserService websiteUserService;
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public RatingService(RatingRepository ratingRepository, UserReviewRepository userReviewRepository,
                         WebsiteUserService websiteUserService, RatingMapper ratingMapper) {
        this.ratingRepository = ratingRepository;
        this.userReviewRepository = userReviewRepository;
        this.websiteUserService = websiteUserService;
        this.ratingMapper = ratingMapper;
    }

    public RatingDTO updateRating(UserReviewDTO userReviewDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(userReviewDTO.getId());
        Rating rating;
        if (userReview == null) {
            throw new BadRequestException("This review doesnt exist");
        }
        Optional <Rating> ratingOptional = ratingRepository.findByUserAndUserReview(websiteUserService.getCurrentUser(), userReview);

        if (userReviewDTO.getOwnRatingIsPositive() == null) {
            if (ratingOptional.isPresent()) {
                rating = ratingOptional.get();
                ratingRepository.delete(rating);
                return null;
            } else {
                throw new BadRequestException("Something went wrong");
            }
        } else {
            if (ratingOptional.isPresent()) {
                rating = ratingOptional.get();
                rating.setIsPositive(userReviewDTO.getOwnRatingIsPositive());
            } else {
                rating = new Rating();
                rating.setIsPositive(userReviewDTO.getOwnRatingIsPositive());
                rating.setUser(websiteUserService.getCurrentUser());
                rating.setUserReview(userReviewRepository.findById(userReviewDTO.getId()));
            }
        }
        return ratingMapper.toDto(ratingRepository.save(rating));
    }
}
