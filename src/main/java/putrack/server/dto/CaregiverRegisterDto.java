package putrack.server.dto;

import lombok.Data;

@Data
public class CaregiverRegisterDto {
    private String username;
    private String password;
    private String name;
}