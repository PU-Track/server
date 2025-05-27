package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import putrack.server.dto.PatientRegisterDto;
import putrack.server.dto.CaregiverRegisterDto;
import putrack.server.entity.Patient;
import putrack.server.entity.Caregiver;
import putrack.server.repository.PatientRepository;
import putrack.server.repository.CaregiverRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PatientRepository patientRepository;
    private final CaregiverRepository caregiverRepository;

    @Transactional
    public Patient registerPatient(PatientRegisterDto dto) {
        if (patientRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        String code = RandomStringUtils.random(8, true, false).toUpperCase();


        Patient patient = new Patient();
        patient.setUsername(dto.getUsername());
        patient.setPassword(dto.getPassword());
        patient.setName(dto.getName());
        patient.setAge(dto.getAge());
        patient.setGender(dto.getGender());
        patient.setWeight(dto.getWeight());
        patient.setPushToken(dto.getPushToken());
        patient.setCode(code);

        return patientRepository.save(patient);
    }

    @Transactional
    public Caregiver registerCaregiver(CaregiverRegisterDto dto) {

        String code = RandomStringUtils.random(4, true, false).toUpperCase();

        Caregiver caregiver = new Caregiver();
        caregiver.setName(dto.getName());
        caregiver.setAge(dto.getAge());
        caregiver.setGender(dto.getGender());
        caregiver.setRole(dto.getRole());
        caregiver.setCode(code);

        return caregiverRepository.save(caregiver);
    }

    @Transactional
    public void registerCaregiverPushToken(String code, String pushToken) {
        Caregiver caregiver = caregiverRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 간병인을 찾을 수 없습니다: " + code));

        caregiver.setPushToken(pushToken);
    }
}
