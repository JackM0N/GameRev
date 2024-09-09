package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link pl.ttsw.GameRev.model.UserReview}
 */
@Data
@Setter
@Getter
public class UserReviewDTO implements Serializable {
    Long id;
    String gameTitle;
    String userUsername;
    String content;
    LocalDate postDate;
    Integer score;
    Integer positiveRating;
    Integer negativeRating;
    Boolean ownRatingIsPositive;
    Long totalReports;
    Long approvedReports;
}
