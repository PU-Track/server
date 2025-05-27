package putrack.server.dto;

import lombok.Data;
import putrack.server.entity.PatientStatus;

@Data
public class PatientDto {
    private Integer patientId;
    private String name;
    private Integer age;
    private Double weight;
    private Double height;
    private String gender;
    private PatientStatus status;
}
