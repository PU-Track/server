package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.Alert;

import java.time.LocalDateTime;
import java.util.*;

public interface AlertRepository extends JpaRepository<Alert, Integer> {
    List<Alert> findByPatient_PatientIdAndTimestampLessThanEqual(Integer patientId, LocalDateTime now);

}
