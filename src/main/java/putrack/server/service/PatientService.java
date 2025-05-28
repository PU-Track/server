package putrack.server.service;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import putrack.server.dto.*;
import putrack.server.entity.AverageData;
import putrack.server.entity.Patient;
import putrack.server.entity.PatientStatus;
import putrack.server.entity.Alert;
import putrack.server.repository.AlertRepository;
import putrack.server.repository.AverageDataRepository;
import putrack.server.repository.PatientRepository;
import java.util.*;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final AverageDataRepository averageDataRepository;
    private final AlertRepository alertRepository;

    private final OpenAIClient client;

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

    @Transactional
    public WeekAverageDataDto getWeekAverageData(Integer patientId) {
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
        LocalDate lastMonday = thisMonday.minusWeeks(1);

        LocalDate thisWeekStart = thisMonday;
        LocalDate thisWeekEnd = today;

        LocalDate lastWeekStart = lastMonday;
        LocalDate lastWeekEnd = lastMonday.with(DayOfWeek.SUNDAY);

        List<AverageData> lastWeekData = averageDataRepository.findByPatientPatientIdAndDateBetween(
                patientId, lastWeekStart, lastWeekEnd
        );

        List<AverageData> thisWeekData = averageDataRepository.findByPatientPatientIdAndDateBetween(
                patientId, thisWeekStart, thisWeekEnd
        );

        List<AverageDataDto> lastWeekDto = lastWeekData.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        List<AverageDataDto> thisWeekDto = thisWeekData.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        WeekAverageDataDto result = new WeekAverageDataDto();
        result.setLastWeekData(lastWeekDto);
        result.setThisWeekData(thisWeekDto);

        return result;
    }

    @Transactional
    public List<AlertDto> getAlertForPatient(Integer patientId) {
        LocalDateTime now = LocalDateTime.now();

        List<Alert> alerts = alertRepository.findByPatient_PatientIdAndTimestampLessThanEqual(patientId, now);

        return alerts.stream().map(alert -> {
            AlertDto dto = new AlertDto();
            dto.setContent(alert.getContent());
            dto.setTimestamp(alert.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }

    private AverageDataDto convertToDto(AverageData entity) {
        AverageDataDto dto = new AverageDataDto();
        dto.setDate(entity.getDate());
        dto.setAirTemp(entity.getAirTemp());
        dto.setAirHumid(entity.getAirHumid());
        dto.setCushionTemp(entity.getCushionTemp());
        dto.setChangeInterval(entity.getChangeInterval());
        dto.setDayOfWeek(getDayOfWeekShort(entity.getDate()));
        return dto;
    }

    private String getDayOfWeekShort(LocalDate date) {
        return date.getDayOfWeek().name().substring(0, 3);
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

        String chatResponse = getChatResponse("재밌는 옛날 얘기 해줘");

        System.out.println("OpenAI Response: " + chatResponse);

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

    public String getChatResponse(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(ChatModel.GPT_4_1) // GPT-4 Turbo
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        return chatCompletion.choices().get(0).message().content().orElse("No response");
    }
}
