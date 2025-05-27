package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import putrack.server.dto.PatientStatusDto;
import putrack.server.dto.PredictedDateTimeDto;
import putrack.server.entity.Patient;
import putrack.server.entity.PatientStatus;
import putrack.server.repository.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    @Transactional
    public PredictedDateTimeDto predictChangeTime(Integer patientId, PatientStatusDto dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime predictedTime;

        if (dto.getStatus() == PatientStatus.LYING) {
            predictedTime = predictForLyingStatus(patientId, now);
        } else {
            predictedTime = LocalDateTime.now();
        }

        PredictedDateTimeDto resDto = new PredictedDateTimeDto();
        resDto.setCurrentDateTime(now);
        resDto.setPredictedDateTime(predictedTime);

        return resDto;
    }

    private LocalDateTime predictForLyingStatus(Integer patientId, LocalDateTime now) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("해당 환자를 찾을 수 없습니다."));

        Integer averageInterval = patient.getAverageInterval();

        return now.plusMinutes(averageInterval);
    }

//    private LocalDateTime predictForSleepingStatus() {
//
//    }
//
//    private LocalDateTime predictForSittingStatus() {
//
//    }
}
