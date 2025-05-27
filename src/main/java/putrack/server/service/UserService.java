package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import putrack.server.dto.CaregiverRegisterDto;
import putrack.server.entity.Caregiver;
import putrack.server.repository.CaregiverRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CaregiverRepository caregiverRepository;

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
