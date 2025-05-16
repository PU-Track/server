package putrack.server.entitiy;
import jakarta.persistence.*;
import java.util.List;


@Entity
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

    @OneToMany(mappedBy = "patient")
    private List<Device> devices;

    @OneToMany(mappedBy = "patient")
    private List<Alert> alerts;

    @OneToMany(mappedBy = "patient")
    private List<Prediction> predictions;

    @ManyToMany
    @JoinTable(
            name = "PatientCaregiver",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "caregiver_id")
    )
    private List<Caregiver> caregivers;
}
