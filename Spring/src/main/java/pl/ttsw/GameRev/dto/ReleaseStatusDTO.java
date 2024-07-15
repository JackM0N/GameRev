package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.ReleaseStatus}
 */
@Data
@Setter
@Getter
public class ReleaseStatusDTO implements Serializable {
    Long id;
    String statusName;
}