package pl.ttsw.GameRev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "platform")
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "platform_id", nullable = false)
    private Long id;

    @Column(name = "platform_name", nullable = false)
    private String platformName;

}