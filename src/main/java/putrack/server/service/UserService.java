package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import putrack.server.dto.CaregiverRegisterDto;
import putrack.server.dto.PatientDto;
import putrack.server.entity.Caregiver;
import putrack.server.entity.Patient;
import putrack.server.repository.CaregiverRepository;
import putrack.server.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CaregiverRepository caregiverRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public Caregiver registerCaregiver(CaregiverRegisterDto dto) {

        String code = RandomStringUtils.random(4, true, false).toUpperCase();

        Caregiver caregiver = new Caregiver();
        caregiver.setName(dto.getName());
        caregiver.setAge(dto.getAge());
        caregiver.setGender(dto.getGender());
        caregiver.setRole(dto.getRole());
        caregiver.setCode(code);

        List<Patient> allPatients = patientRepository.findAll();
        caregiver.assignPatients(allPatients);

        return caregiverRepository.save(caregiver);
    }

    @Transactional
    public void registerCaregiverPushToken(String code, String pushToken) {
        Caregiver caregiver = caregiverRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 간병인을 찾을 수 없습니다: " + code));

        caregiver.setPushToken(pushToken);
    }

    @Transactional
    public List<PatientDto> getPatientsByCaregiverCode(String code) {
        Caregiver caregiver = caregiverRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("해당 코드의 간병인을 찾을 수 없습니다."));

        List<Patient> patients = caregiver.getPatients();

        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PatientDto convertToDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(patient.getPatientId());
        dto.setName(patient.getName());
        dto.setAge(patient.getAge());
        dto.setGender(patient.getGender());
        dto.setWeight(patient.getWeight());
        dto.setHeight(patient.getHeight());
        dto.setStatus(patient.getStatus());
        return dto;
    }
}
