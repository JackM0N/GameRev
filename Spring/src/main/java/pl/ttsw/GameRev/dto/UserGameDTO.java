package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import pl.ttsw.GameRev.model.Game;
import pl.ttsw.GameRev.model.WebsiteUser;

import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.UserGame}
 */
@Data
@Getter
@Setter
public class UserGameDTO implements Serializable {
    Long id;
    GameDTO game;
    WebsiteUserDTO user;
    CompletionStatusDTO completionStatus;
    Boolean isFavourite;
}