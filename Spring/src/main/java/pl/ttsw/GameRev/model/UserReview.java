package pl.ttsw.GameRev.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_review")
public class UserReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_review_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private WebsiteUser user;

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "positive_rating")
    private Integer positiveRating;

    @Column(name = "negative_rating")
    private Integer negativeRating;

    @OneToMany(mappedBy = "userReview", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Rating> ratings;
}