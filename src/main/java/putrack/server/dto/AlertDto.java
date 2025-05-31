package putrack.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertDto {
    private String title;
    private String content;
    private LocalDateTime timestamp;
}
