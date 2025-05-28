package putrack.server.dto;

import lombok.Data;
import putrack.server.entity.CaregiverRole;

@Data
public class CaregiverDto {
    private String name;
    private Integer age;
    private String gender;
    private CaregiverRole role;
}
