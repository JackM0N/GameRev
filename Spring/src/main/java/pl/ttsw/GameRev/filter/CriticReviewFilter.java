package pl.ttsw.GameRev.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pl.ttsw.GameRev.enums.ReviewStatus;

import java.time.LocalDate;

@Getter
@Setter
public class CriticReviewFilter {
    private ReviewStatus reviewStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private Integer scoreFrom;
    private Integer scoreTo;
    private String searchText;
}
