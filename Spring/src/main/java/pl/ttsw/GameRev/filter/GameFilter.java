package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pl.ttsw.GameRev.enums.ReleaseStatus;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GameFilter {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private Float minUserScore;
    private Float maxUserScore;
    List<Long> tagIds;
    List<ReleaseStatus> releaseStatuses;
}
