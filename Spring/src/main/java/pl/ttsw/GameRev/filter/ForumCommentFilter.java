package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumCommentFilter {
    private Long userId;
    private String searchText;
}
