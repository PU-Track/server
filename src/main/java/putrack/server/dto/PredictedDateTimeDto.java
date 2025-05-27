package putrack.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PredictedDateTimeDto {
    private LocalDateTime currentDateTime;
    private LocalDateTime predictedDateTime;
}
