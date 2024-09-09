package pl.ttsw.GameRev.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link pl.ttsw.GameRev.model.WebsiteUser}
 */
@Data
@Setter
@Getter
public class WebsiteUserDTO implements Serializable {
    Long id;
    String username;
    String password;
    String profilepic;
    String nickname;
    String email;
    String lastActionDate;
    String description;
    LocalDate joinDate;
    Boolean isBanned;
    Boolean isDeleted;
    List<RoleDTO> roles;
}
