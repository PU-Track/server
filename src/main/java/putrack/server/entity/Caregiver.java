package putrack.server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;



@Entity
@Setter @Getter
public class Caregiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer caregiverId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private Integer age;

    @Column(nullable = false, length = 1)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CaregiverRole role;

    @Column(length = 50)
    private String code;

    @Column(length = 50)
    private String pushToken;

    @ManyToMany(mappedBy = "caregivers")
    private List<Patient> patients;

    public void assignPatients(List<Patient> patients) {
        if (this.patients == null) {
            this.patients = new ArrayList<>();
        }
        this.patients.addAll(patients);
    }
}
