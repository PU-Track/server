package putrack.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.PatientStatusDto;
import putrack.server.dto.PredictedDateTimeDto;
import putrack.server.service.PatientService;

@RestController
@RequestMapping("/patient")
@Tag(name = "Patient", description = "Patient API")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @Operation(summary = "다음 체위 변경 시기", description = "다음 체위를 변경하는 시각을 반환합니다. (status가 sleeping일 시 알림 발송)")
    @PostMapping("/{patientId}/changeTime")
    public PredictedDateTimeDto predictChangeTime(@PathVariable("patientId") Integer patientId, @RequestBody PatientStatusDto dto) {
        return patientService.predictChangeTime(patientId, dto);
    }
}
