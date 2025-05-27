package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.Caregiver;

public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {
    Caregiver findByCode(String code);
}
