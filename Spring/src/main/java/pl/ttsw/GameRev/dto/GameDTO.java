package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.ttsw.GameRev.enums.ReleaseStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Game}
 */
@Data
@Getter
@Setter
public class GameDTO implements Serializable {
    Long id;
    String title;
    String developer;
    String publisher;
    LocalDate releaseDate;
    ReleaseStatus releaseStatus;
    String description;
    List<TagDTO> tags;
    Float usersScore;
}