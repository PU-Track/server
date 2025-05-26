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
        if (caregiverRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        String code = RandomStringUtils.random(8, true, false).toUpperCase();

        Caregiver caregiver = new Caregiver();
        caregiver.setUsername(dto.getUsername());
        caregiver.setPassword(dto.getPassword());
        caregiver.setName(dto.getName());
        caregiver.setPushToken(dto.getPushToken());
        caregiver.setCode(code);

        return caregiverRepository.save(caregiver);
    }
}
