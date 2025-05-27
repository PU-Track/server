package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.Caregiver;

import java.util.Optional;

public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {
    Optional<Caregiver> findByCode(String code);
}
