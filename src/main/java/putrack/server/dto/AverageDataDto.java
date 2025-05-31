package putrack.server.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AverageDataDto {
    private String dayOfWeek;
    private LocalDate date;
    private Double airTemp;
    private Double airHumid;
    private Double cushionTemp;
    private Double changeInterval;
    private String alert;
}
