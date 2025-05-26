package putrack.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.CaregiverRegisterDto;
import putrack.server.dto.PatientRegisterDto;
import putrack.server.entity.Caregiver;
import putrack.server.entity.Patient;
import putrack.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "User (환자, 보호자) 등록 API")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "환자 등록", description = "새로운 환자를 등록합니다.")
    @PostMapping("/patient/register")
    public ResponseEntity<Patient> registerPatient(@RequestBody PatientRegisterDto dto) {
        Patient patient = userService.registerPatient(dto);
        return ResponseEntity.ok(patient);
    }

    @Operation(summary = "간병인 등록", description = "새로운 간병인을 등록합니다.")
    @PostMapping("/caregiver/register")
    public ResponseEntity<Caregiver> registerCaregiver(@RequestBody CaregiverRegisterDto dto) {
        Caregiver caregiver = userService.registerCaregiver(dto);
        return ResponseEntity.ok(caregiver);
    }
}