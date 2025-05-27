package putrack.server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;


@Entity
@Setter @Getter
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer patientId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    @Enumerated(EnumType.STRING)
    @Column
    private PatientStatus status;

    @Column
    private Integer averageInterval;

    @Column
    private LocalTime averageWakeUpTime;

    @OneToOne(mappedBy = "patient")
    private Device devices;

    @OneToMany(mappedBy = "patient")
    private List<Alert> alerts;

    @OneToMany(mappedBy = "patient")
    private List<Prediction> predictions;

    @ManyToMany(mappedBy = "patients")
    private List<Caregiver> caregivers;
}
