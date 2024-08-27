package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumFilter {
    private Boolean isDeleted;
    private Long gameId;
    private String searchText;
}
