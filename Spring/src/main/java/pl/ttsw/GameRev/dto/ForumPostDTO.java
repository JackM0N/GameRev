package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ForumPost}
 */
@Data
@Getter
@Setter
public class ForumPostDTO implements Serializable {
    Long id;
    ForumDTO forum;
    SimplifiedUserDTO author;
    String title;
    String content;
    LocalDateTime postDate;
    LocalDateTime lastResponseDate;
    Integer commentCount;
    Long views;
    Boolean isDeleted;
    LocalDateTime deletedAt;

}