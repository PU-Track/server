package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Integer>  {

}
