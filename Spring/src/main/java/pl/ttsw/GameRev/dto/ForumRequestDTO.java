package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumRequest}
 */
@Data
@Getter
@Setter
public class ForumRequestDTO implements Serializable {
    Long id;
    String forumName;
    String description;
    GameDTO game;
    ForumDTO parentForum;
    SimplifiedUserDTO author;
    Boolean approved;
}
