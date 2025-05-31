package putrack.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.*;
import putrack.server.service.PatientService;
import java.util.*;

@RestController
@RequestMapping("/patient")
@Tag(name = "Patient", description = "Patient API")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @Operation(summary = "다음 체위 변경 시기", description = "다음 체위를 변경하는 시각을 반환합니다. (status가 sleeping일 시 알림 발송)")
    @PostMapping("/{patientId}/changeTime")
    public PredictedDateTimeDto predictChangeTime(@Parameter(description = "간병인 코드") @RequestParam("code") String code, @PathVariable("patientId") Integer patientId, @RequestBody PatientStatusDto dto) {
        return patientService.predictChangeTime(code, patientId, dto);
    }

    @Operation(summary = "지난주/이번주 평균 데이터", description = "지난주와 이번주의 평균 온습도, 방석 온도, 체위 변경 주기 데이터를 반환합니다.")
    @GetMapping("/{patientId}/averageData")
    public WeekAverageDataDto getWeekAverageData(@PathVariable("patientId") Integer patientId) {
        return patientService.getWeekAverageData(patientId);
    }

    @Operation(summary = "지금까지의 알림 목록", description = "현재 시간까지의 알림 목록")
    @GetMapping("/{patientId}/alert")
    public AlertListDto getAlertForPatient(@PathVariable("patientId") Integer patientId) {
        return patientService.getAlertForPatient(patientId);
    }

}
