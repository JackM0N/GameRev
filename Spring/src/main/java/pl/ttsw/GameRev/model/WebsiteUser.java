package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "website_user")
public class WebsiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profilepic")
    private String profilepic;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "last_action_date", nullable = false)
    private LocalDateTime lastActionDate;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGame> userGames = new ArrayList<>();

    @ManyToMany(mappedBy = "forumModerators")
    private List<Forum> forums = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebsiteUser that = (WebsiteUser) o;

        return Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(profilepic, that.profilepic) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(email, that.email) &&
                Objects.equals(lastActionDate, that.lastActionDate) &&
                Objects.equals(description, that.description) &&
                Objects.equals(joinDate, that.joinDate) &&
                Objects.equals(isBanned, that.isBanned) &&
                Objects.equals(isDeleted, that.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, profilepic, nickname, email, lastActionDate, description, joinDate, isBanned, isDeleted, roles);
    }
}
