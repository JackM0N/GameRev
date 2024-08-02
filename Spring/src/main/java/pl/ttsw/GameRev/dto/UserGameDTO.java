package pl.ttsw.GameRev.dto;

import lombok.*;
import pl.ttsw.GameRev.enums.CompletionStatus;

import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.UserGame}
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGameDTO implements Serializable {
    Long id;
    GameDTO game;
    WebsiteUserDTO user;
    CompletionStatus completionStatus;
    Boolean isFavourite;
}