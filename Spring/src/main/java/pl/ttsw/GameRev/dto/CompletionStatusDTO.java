package pl.ttsw.GameRev.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.CompletionStatus}
 */
@Value
public class CompletionStatusDTO implements Serializable {
    Long id;
    String completionName;
}