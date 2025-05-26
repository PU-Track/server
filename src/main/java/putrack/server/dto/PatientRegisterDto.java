package putrack.server.dto;

import lombok.Data;

@Data
public class PatientRegisterDto {
    private String username;
    private String password;
    private String name;
    private Integer age;
    private String gender;
    private Integer weight;
    private String pushToken;
}