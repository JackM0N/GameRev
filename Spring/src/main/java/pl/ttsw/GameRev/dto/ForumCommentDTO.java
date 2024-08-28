package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumComment}
 */
@Data
@Getter
@Setter
public class ForumCommentDTO implements Serializable {
    Long id;
    Long forumPostId;
    SimplifiedUserDTO author;
    String content;
    LocalDateTime postDate;
    Boolean isDeleted;
    LocalDateTime deletedAt;

}