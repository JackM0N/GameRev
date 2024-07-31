package pl.ttsw.GameRev.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.CompletionStatus}
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class CompletionStatusDTO implements Serializable {
    Long id;
    String completionName;
}