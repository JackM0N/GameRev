package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.WebsiteUser}
 */
@Data
@Setter
@Getter
public class UpdateWebsiteUserDTO implements Serializable {
    Long id;
    String currentPassword;
    String newPassword;
    String profilepic;
    String nickname;
    String email;
    String description;
    Boolean isDeleted;
}