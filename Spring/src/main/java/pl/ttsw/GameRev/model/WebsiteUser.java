package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(name = "last_action_date")
    private String lastActionDate;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}