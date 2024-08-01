package pl.ttsw.GameRev.dto;

import lombok.*;

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
    CompletionStatusDTO completionStatus;
    Boolean isFavourite;
}