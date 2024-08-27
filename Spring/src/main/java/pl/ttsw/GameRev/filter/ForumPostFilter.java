package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ForumPostFilter {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postDateTo;
    private String searchText;
}
