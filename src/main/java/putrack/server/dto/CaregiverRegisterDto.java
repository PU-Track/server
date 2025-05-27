package putrack.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import putrack.server.entity.CaregiverRole;

@Data
public class CaregiverRegisterDto {
    private String name;
    private Integer age;
    private String gender;
    private CaregiverRole role;
}