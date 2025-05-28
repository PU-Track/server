package putrack.server.dto;

import lombok.Data;
import java.util.*;

@Data
public class PatientListDto {
    private List<PatientDto> patientList;
}
