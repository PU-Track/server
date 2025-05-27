package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import putrack.server.dto.CaregiverRegisterDto;
import putrack.server.entity.Caregiver;
import putrack.server.entity.Patient;
import putrack.server.repository.CaregiverRepository;
import putrack.server.repository.PatientRepository;

import java.util.List;

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
}
