package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review_status")
public class ReviewStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_status_id", nullable = false)
    private Long id;

    @Column(name = "status_name", nullable = false)
    private String statusName;
}
