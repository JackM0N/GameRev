package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import pl.ttsw.GameRev.model.ForumModerator;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Forum}
 */
@Data
@Getter
@Setter
public class ForumDTO implements Serializable {
    Long id;
    GameDTO game;
    String forumName;
    Boolean isDeleted;
    Long parentForumId;
    List<WebsiteUserDTO> forumModeratorsDTO;
    List<ForumPostDTO> forumPostsDTO;
}