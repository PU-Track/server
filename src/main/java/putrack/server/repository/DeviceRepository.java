package putrack.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import putrack.server.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

    Device findByPatient_PatientId(Integer patientId);
}


