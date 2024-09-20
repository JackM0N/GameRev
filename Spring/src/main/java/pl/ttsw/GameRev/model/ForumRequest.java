package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "forum_request")
public class ForumRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "forum_request_id", nullable = false)
    private Long id;

    @Column(name = "forum_name", nullable = false, length = 100)
    private String forumName;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_forum_id", nullable = false)
    private Forum parentForum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private WebsiteUser author;

    @Column(name = "approved")
    private Boolean approved;
}
