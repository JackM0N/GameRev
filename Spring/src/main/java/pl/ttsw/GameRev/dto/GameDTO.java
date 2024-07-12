package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import pl.ttsw.GameRev.model.ReleaseStatus;

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
    Long releaseStatus;
    String description;
    List<Long> tags;
}