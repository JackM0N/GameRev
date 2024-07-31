package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.CompletionStatus}
 */
@Data
@Getter
@Setter
public class CompletionStatusDTO implements Serializable {
    Long id;
    String completionName;
}