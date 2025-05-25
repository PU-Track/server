package putrack.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer predictionId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private Integer predictionTime;
}
