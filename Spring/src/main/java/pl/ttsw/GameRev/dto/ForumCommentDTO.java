package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import pl.ttsw.GameRev.model.ForumPost;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumComment}
 */
@Data
@Getter
@Setter
public class ForumCommentDTO implements Serializable {
    Long id;
    ForumPostDTO forumPost;
    WebsiteUserDTO author;
    String content;
    LocalDate postDate;
}