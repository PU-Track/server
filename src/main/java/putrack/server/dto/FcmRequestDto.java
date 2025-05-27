package putrack.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmRequestDto {
    private String title;
    private String body;
}
