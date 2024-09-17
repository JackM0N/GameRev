package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;
import pl.ttsw.GameRev.enums.CompletionStatus;
import java.util.List;

@Getter
@Setter
public class UserGameFilter {
    Boolean isFavourite;
    CompletionStatus completionStatus;
    List<Long> tagsIds;
}
