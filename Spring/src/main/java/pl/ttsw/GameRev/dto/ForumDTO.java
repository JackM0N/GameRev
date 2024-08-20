package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
    String gameTitle;
    String forumName;
    Boolean isDeleted;
    Long parentForumId;
    List<WebsiteUserDTO> forumModeratorsDTO;
}