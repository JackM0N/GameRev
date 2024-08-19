package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumPost}
 */
@Data
@Getter
@Setter
public class ForumPostDTO implements Serializable {
    Long id;
    ForumDTO forum;
    WebsiteUserDTO author;
    String title;
    String content;
    LocalDate postDate;
    String picture;
    List<ForumCommentDTO> forumComments;
}