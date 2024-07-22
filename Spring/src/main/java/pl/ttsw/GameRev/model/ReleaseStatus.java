package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "release_status")
public class ReleaseStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_status_id", nullable = false)
    private Long id;

    @Column(name = "status_name", nullable = false)
    private String statusName;
}