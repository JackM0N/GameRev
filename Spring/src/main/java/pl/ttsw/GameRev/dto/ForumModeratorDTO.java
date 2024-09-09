package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumModerator}
 */
@Data
@Getter
@Setter
public class ForumModeratorDTO implements Serializable {
    Long id;
    ForumDTO forum;
    WebsiteUserDTO moderator;
}
