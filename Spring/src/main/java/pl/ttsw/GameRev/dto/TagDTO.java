package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Tag}
 */
@Data
@Getter
@Setter
public class TagDTO implements Serializable {
    Long id;
    String tagName;
    Integer priority;
}