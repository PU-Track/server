package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import putrack.server.dto.PatientStatusDto;
import putrack.server.dto.PredictedDateTimeDto;
import putrack.server.entity.Patient;
import putrack.server.entity.PatientStatus;
import putrack.server.repository.PatientRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    @Transactional
    public PredictedDateTimeDto predictChangeTime(Integer patientId, PatientStatusDto dto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);;
        LocalDateTime predictedTime;

        if (dto.getStatus() == PatientStatus.LYING) {
            predictedTime = predictForLyingStatus(patientId, now);
        } else if (dto.getStatus() == PatientStatus.SLEEPING) {
            predictedTime = predictForSleepingStatus(patientId, now);
        } else if (dto.getStatus() == PatientStatus.SITTING) {
            Duration duration = Duration.between(dto.getPostureStartTime(), now);
            double elapsedTime = duration.toMillis() / 60000.0;

            predictedTime = predictForSittingStatus(now, dto.getAirTemp(), dto.getAirHumid(), 0.3, elapsedTime);
        }
        else {
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

    private LocalDateTime predictForSleepingStatus(Integer patientId, LocalDateTime now) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("해당 환자를 찾을 수 없습니다."));

        LocalTime wakeUpTime = patient.getAverageWakeUpTime();

        LocalDateTime nextWakeUp = LocalDateTime.of(now.toLocalDate(), wakeUpTime);

        if (!nextWakeUp.isAfter(now)) {
            nextWakeUp = nextWakeUp.plusDays(1);
        }

        return nextWakeUp;
    }

    public LocalDateTime predictForSittingStatus(LocalDateTime now, double avgAirTemp, double avgAirHumid, double cushionSlope, double elapsedTime) {
        RestTemplate restTemplate = new RestTemplate();
        String PREDICT_URL = "http://52.79.250.81:8000/predict";

        // 1. 요청 Body 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("avg_air_temp", avgAirTemp);
        requestBody.put("avg_air_humid", avgAirHumid);
        requestBody.put("cushion_slope", cushionSlope);
        requestBody.put("elapsed_time", elapsedTime);

        // 2. 요청 HEADER 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 3. API 요청
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PREDICT_URL, requestEntity, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new RuntimeException("예측 API 호출 실패: " + responseEntity.getStatusCode());
        }

        String responseBody = responseEntity.getBody().trim();
        System.out.println("예측 응답: " + responseBody);

        // 4. JSON에서 "predicted_remaining_time" 값 추출
        ObjectMapper objectMapper = new ObjectMapper();
        double minutesToAdd;
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Object value = responseMap.get("predicted_remaining_time");
            if (value == null) {
                throw new RuntimeException("예측 결과 없음");
            }
            minutesToAdd = Double.parseDouble(value.toString());
        } catch (Exception e) {
            throw new RuntimeException("API 응답 파싱 실패", e);
        }

        // 5. now에 분 단위로 더하기
        LocalDateTime predictedTime = now.plusMinutes((long) minutesToAdd);

        return predictedTime;
    }
}
