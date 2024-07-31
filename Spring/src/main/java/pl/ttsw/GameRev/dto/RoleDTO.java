package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Role}
 */
@Data
@Getter
@Setter
public class RoleDTO implements Serializable {
    Long id;
    String roleName;
}