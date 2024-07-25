package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Rating}
 */
@Data
@Setter
@Getter
public class RatingDTO implements Serializable {
    Long id;
    Boolean isPositive;
    WebsiteUserDTO user;
    UserReviewDTO userReview;
}