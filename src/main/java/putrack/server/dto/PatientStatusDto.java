package putrack.server.dto;

import lombok.Data;
import java.time.LocalDateTime;
import putrack.server.entity.PatientStatus;

@Data
public class PatientStatusDto {
    private PatientStatus status;
    private Double airTemp;
    private Double airHumid;
    private Double cushionTemp;
    private LocalDateTime postureStartTime;
}