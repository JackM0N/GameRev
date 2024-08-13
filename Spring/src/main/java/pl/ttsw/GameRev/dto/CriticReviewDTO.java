package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.ttsw.GameRev.enums.ReviewStatus;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link pl.ttsw.GameRev.model.CriticReview}
 */
@Data
@Getter
@Setter
public class CriticReviewDTO implements Serializable {
    Long id;
    String gameTitle;
    WebsiteUserDTO user;
    String content;
    LocalDate postDate;
    Integer score;
    ReviewStatus reviewStatus;
    WebsiteUserDTO statusChangedBy;
}