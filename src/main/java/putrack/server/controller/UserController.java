package putrack.server.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.*;
import putrack.server.entity.Caregiver;
import putrack.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "User API")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "간병인 등록", description = "새로운 간병인을 등록합니다. (role: CAREGIVER, DOCTOR, NURSE, NURSING_ASSISTANT, OTHER, gender: F, M)")
    @PostMapping("/caregiver/register")
    public CaregiverCodeDto registerCaregiver(@RequestBody CaregiverRegisterDto dto) {
        return userService.registerCaregiver(dto);
    }

    @Operation(summary = "간병인 정보", description = "간병인(유저)에 대한 정보를 반환합니다.")
    @GetMapping("/caregiver")
    public CaregiverDto getCaregiver(@Parameter(description = "간병인 코드") @RequestParam("code") String code) {
        return userService.getCaregiver(code);
    }

    @Operation(summary = "FCM 토큰 등록", description = "간병인에 대한 FCM 토큰을 등록합니다.")
    @PostMapping("/caregiver/register/token")
    public String registerCaregiverToken(
            @Parameter(description = "간병인 코드") @RequestParam("code") String code,
            @RequestBody PushTokenRequestDto dto) {
        userService.registerCaregiverPushToken(code, dto.getPushToken());
        return "FCM 토큰 등록 완료";
    }

    @Operation(summary = "환자 정보 리스트", description = "간병인에 대한 환자 정보 리스트")
    @GetMapping("/caregiver/{code}/patients")
    public PatientListDto getPatientsByCaregiverCode(@PathVariable("code") String code) {
        return userService.getPatientsByCaregiverCode(code);
    }
}