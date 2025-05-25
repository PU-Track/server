package putrack.server.entity;
import jakarta.persistence.*;
import java.util.List;



@Entity
public class Caregiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer caregiverId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "caregivers")
    private List<Patient> patients;
}
