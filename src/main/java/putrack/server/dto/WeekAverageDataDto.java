package putrack.server.dto;

import lombok.Data;
import java.util.*;

@Data
public class WeekAverageDataDto {
    private List<AverageDataDto> lastWeekData;
    private List<AverageDataDto> thisWeekData;
}