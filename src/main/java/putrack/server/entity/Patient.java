package putrack.server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Setter @Getter
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer patientId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false, length = 50)
    private String code;

    @OneToOne(mappedBy = "patient")
    private Device devices;

    @OneToMany(mappedBy = "patient")
    private List<Alert> alerts;

    @OneToMany(mappedBy = "patient")
    private List<Prediction> predictions;

    @Column(nullable = false, length = 50)
    private String pushToken;

    @ManyToMany
    @JoinTable(
            name = "PatientCaregiver",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "caregiver_id")
    )
    private List<Caregiver> caregivers;
}
