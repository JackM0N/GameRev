package pl.ttsw.GameRev.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.Role}
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO implements Serializable {
    Long id;
    String roleName;
}