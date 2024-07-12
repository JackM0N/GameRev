package pl.ttsw.GameRev.dto;

import lombok.Value;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Role}
 */
@Value
public class RoleDTO implements Serializable {
    Long id;
    String roleName;
}