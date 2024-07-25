package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "completion_status")
public class CompletionStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_status_id", nullable = false)
    private Long id;

    @Column(name = "completion_name", nullable = false)
    private String completionName;
}