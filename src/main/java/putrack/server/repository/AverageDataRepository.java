package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.AverageData;
import java.util.*;

import java.time.LocalDate;

public interface AverageDataRepository extends JpaRepository<AverageData, Integer> {
    List<AverageData> findByPatientPatientIdAndDateBetween(Integer patientId, LocalDate startDate, LocalDate endDate);
}
