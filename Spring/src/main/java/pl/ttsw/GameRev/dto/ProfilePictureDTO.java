package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.io.Serializable;

/**
 * DTO for {@link pl.ttsw.GameRev.model.WebsiteUser}
 */
@Data
@Setter
@Getter
public class ProfilePictureDTO implements Serializable {
    String username;
    MultipartFile profilePicture;
}
