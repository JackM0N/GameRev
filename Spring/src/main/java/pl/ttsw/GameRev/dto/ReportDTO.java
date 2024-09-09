package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Report}
 */
@Data
@Getter
@Setter
public class ReportDTO implements Serializable {
    Long id;
    UserReviewDTO userReview;
    WebsiteUserDTO user;
    Boolean approved;
    String content;
}
