package pl.ttsw.GameRev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.UserGame}
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class UserGameDTO implements Serializable {
    Long id;
    GameDTO game;
    WebsiteUserDTO user;
    CompletionStatusDTO completionStatus;
    Boolean isFavourite;
}