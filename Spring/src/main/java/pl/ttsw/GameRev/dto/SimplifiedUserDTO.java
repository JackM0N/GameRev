package pl.ttsw.GameRev.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Data
@Setter
@Getter
public class SimplifiedUserDTO {
    Long id;
    String profilepic;
    String nickname;
    String lastActionDate;
    String description;
    LocalDate joinDate;
}
