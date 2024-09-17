package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "forum")
public class Forum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "forum_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "forum_name", nullable = false, length = 100)
    private String forumName;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ColumnDefault("MISSING DESCRIPTION")
    @Column(name = "description", nullable = false)
    private String description;

    @ColumnDefault("0")
    @Column(name = "post_count", nullable = false)
    private Integer postCount;

    @Column(name = "last_post_date")
    private LocalDateTime lastPostDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_forum_id")
    private Forum parentForum;

    @OneToMany(mappedBy = "forum")
    private List<ForumPost> forumPosts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "forum_moderator",
            joinColumns = @JoinColumn(name = "forum_id"),
            inverseJoinColumns = @JoinColumn(name = "moderator_id")
    )
    private List<WebsiteUser> forumModerators = new ArrayList<>();
}
