package putrack.server.service;

import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessagingException;
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

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;


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
    private final FcmService fcmService;

    private final OpenAIClient client;

    @Transactional
    public PredictedDateTimeDto predictChangeTime(String code, Integer patientId, PatientStatusDto dto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        ;
        LocalDateTime predictedTime;

        if (dto.getStatus() == PatientStatus.LYING) {
            predictedTime = predictForLyingStatus(patientId, now);
        } else if (dto.getStatus() == PatientStatus.SLEEPING) {
            predictedTime = predictForSleepingStatus(code, patientId, now);
        } else if (dto.getStatus() == PatientStatus.SITTING) {
            Duration duration = Duration.between(dto.getPostureStartTime(), now);
            double elapsedTime = duration.toMillis() / 60000.0;

            predictedTime = predictForSittingStatus(now, dto.getAirTemp(), dto.getAirHumid(), 0.3, elapsedTime);
        } else {
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
                .map(data -> convertToDto(data, patientId))
                .collect(Collectors.toList());

        List<AverageDataDto> thisWeekDto = thisWeekData.stream()
                .map(data -> convertToDto(data, patientId))
                .collect(Collectors.toList());

        WeekAverageDataDto result = new WeekAverageDataDto();
        result.setLastWeekData(lastWeekDto);
        result.setThisWeekData(thisWeekDto);

        return result;
    }

    @Transactional
    public AlertListDto getAlertForPatient(Integer patientId) {
        LocalDateTime now = LocalDateTime.now();

        List<Alert> alerts = alertRepository.findByPatient_PatientIdAndTimestampLessThanEqual(patientId, now);

        List<AlertDto> alertDtos = alerts.stream().map(alert -> {
            AlertDto dto = new AlertDto();
            dto.setContent(alert.getContent());
            dto.setTimestamp(alert.getTimestamp());
            dto.setTitle(alert.getTitle());
            return dto;
        }).collect(Collectors.toList());

        AlertListDto alertListDto = new AlertListDto();
        alertListDto.setAlertList(alertDtos);

        return alertListDto;
    }

    private AverageDataDto convertToDto(AverageData entity, Integer patientId) {
        AverageDataDto dto = new AverageDataDto();
        dto.setDate(entity.getDate());
        dto.setAirTemp(entity.getAirTemp());
        dto.setAirHumid(entity.getAirHumid());
        dto.setCushionTemp(entity.getCushionTemp());
        dto.setChangeInterval(entity.getChangeInterval());
        dto.setDayOfWeek(getDayOfWeekShort(entity.getDate()));

        LocalDate date = entity.getDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay;

        if (date.equals(LocalDate.now())) {
            endOfDay = LocalDateTime.now();
        } else {
            endOfDay = date.atTime(LocalTime.MAX);
        }

        Optional<Alert> latestAlert = alertRepository.findTopByPatientPatientIdAndTimestampBetweenOrderByTimestampDesc(
                patientId, startOfDay, endOfDay
        );

        dto.setAlert(latestAlert.map(Alert::getContent).orElse("")); // 없으면 빈 문자열

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

    private LocalDateTime predictForSleepingStatus(String code, Integer patientId, LocalDateTime now) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("해당 환자를 찾을 수 없습니다."));

        LocalTime wakeUpTime = patient.getAverageWakeUpTime();

        LocalDateTime nextWakeUp = LocalDateTime.of(now.toLocalDate(), wakeUpTime);

        if (!nextWakeUp.isAfter(now)) {
            nextWakeUp = nextWakeUp.plusDays(1);
        }

        // 프롬프트 생성
        String prompt = makePrompt(patientId);
        System.out.println("prompt: " + prompt);

        // openai api 호출
        String chatResponse = getChatResponse(prompt);
        System.out.println("OpenAI Response: " + chatResponse);

        // 간병인에게 알림 전송
        sendAlertToCaregiver(code, patientId, chatResponse);

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
                .addSystemMessage("당신은 욕창 예방을 위한 환자 모니터링 시스템의 의료 데이터 분석 전문가입니다. 응답은 3~4문장으로 구성된 한 문단으로 작성하며, 각 문장은 '~입니다'로 끝나야 합니다. 분석은 간결하고 명확하며, 이상 징후와 건강 위험 요인을 포함해야 합니다.")
                .addUserMessage(prompt)
                .model(ChatModel.GPT_4_1) // GPT-4 Turbo
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        return chatCompletion.choices().get(0).message().content().orElse("No response");
    }

    public String makePrompt(Integer patientId) {
        StringBuilder promptBuilder = new StringBuilder();

        String todayData = getTodaySensorAveragesByDeviceId(1);
        String lastData = getLastThreeDaysData(patientId).trim();
        String patientData = getPatientData(patientId).trim();

        promptBuilder.append("다음은 욕창 예방을 위한 환자 모니터링 데이터입니다.\n")
                .append("각 데이터는 다음과 같습니다:\n")
                .append("- 오늘의 실시간 쿠션 온도 데이터\n")
                .append("- 최근 3일간의 평균 데이터\n")
                .append("- 환자 정보\n\n")
                .append("이 데이터를 기반으로 다음 항목을 간결하게 요약해 주세요:\n")
                .append("1. 오늘의 쿠션 온도 변화 패턴 요약\n")
                .append("2. 데이터에서 발견된 이상치 여부\n")
                .append("3. 예상되는 건강 위험 요소\n\n")
                .append("[오늘의 데이터]\n").append(todayData).append("\n\n")
                .append("[최근 3일간의 평균 데이터]\n").append(lastData).append("\n\n")
                .append("[환자 정보]\n").append(patientData);


        return promptBuilder.toString();
    }

    public String getTodaySensorAveragesByDeviceId(int deviceId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("data");
        StringBuilder resultBuilder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1); // latch 추가

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    System.out.println("데이터 없음");
                    resultBuilder.append("데이터 없음");
                    latch.countDown();
                    return;
                }

                Map<String, Object> allData = (Map<String, Object>) snapshot.getValue();
                if (allData == null) {
                    System.out.println("전체 데이터 없음");
                    resultBuilder.append("전체 데이터 없음");
                    latch.countDown();
                    return;
                }

                Map<String, Object> sensorDataMap = (Map<String, Object>) allData.get("sensor_data");
                Map<String, Object> tagDataMap = (Map<String, Object>) allData.get("tag_data");

                if (sensorDataMap == null || tagDataMap == null) {
                    System.out.println("sensor_data 또는 tag_data 없음");
                    resultBuilder.append("sensor_data 또는 tag_data 없음");
                    latch.countDown();
                    return;
                }

//                LocalDateTime now = LocalDateTime.parse("2025-05-31T23:59:00");
                LocalDateTime now = LocalDateTime.now();
                System.out.println("now: " + now);

                // tag들을 timestamp 기준으로 정렬
                List<Map<String, Object>> sortedTags = new ArrayList<>();
                for (Object rawTag : tagDataMap.values()) {
                    if (rawTag instanceof Map) {
                        sortedTags.add((Map<String, Object>) rawTag);
                    }
                }
                sortedTags.sort(Comparator.comparing(tag -> LocalDateTime.parse((String) tag.get("timestamp"))));

                for (int i = 0; i < sortedTags.size(); i++) {
                    Map<String, Object> tag = sortedTags.get(i);
                    String tagTimestamp = (String) tag.get("timestamp");
                    Number tagDeviceIdRaw = (Number) tag.get("device_id");

                    if (tagDeviceIdRaw == null) continue;
                    int tagDeviceId = tagDeviceIdRaw.intValue();
                    if (tagDeviceId != deviceId) continue;

                    LocalDateTime tagTime = LocalDateTime.parse(tagTimestamp);
                    if (tagTime.isAfter(now)) continue;

                    String intervalStr = "N/A";
                    if (i < sortedTags.size() - 1) {
                        Map<String, Object> nextTag = sortedTags.get(i + 1);
                        LocalDateTime nextTagTime = LocalDateTime.parse((String) nextTag.get("timestamp"));
                        long intervalMinutes = Duration.between(tagTime, nextTagTime).toMinutes();
                        intervalStr = intervalMinutes + "분";
                    }

                    double sumTemp = 0, sumHumid = 0, sumAirTemp = 0;
                    int count = 0;

                    for (String sensorKey : sensorDataMap.keySet()) {
                        Object rawSensor = sensorDataMap.get(sensorKey);
                        if (!(rawSensor instanceof Map)) continue;

                        Map<String, Object> sensor = (Map<String, Object>) rawSensor;
                        String sensorTimestamp = (String) sensor.get("timestamp");
                        Number sensorDeviceIdRaw = (Number) sensor.get("device_id");

                        if (sensorDeviceIdRaw == null) continue;
                        int sensorDeviceId = sensorDeviceIdRaw.intValue();
                        if (sensorDeviceId != deviceId) continue;

                        LocalDateTime sensorTime = LocalDateTime.parse(sensorTimestamp);
                        if (sensorTime.isAfter(tagTime) || sensorTime.isAfter(now)) continue;

                        Number cushionTemp = (Number) sensor.get("cushion_temp");
                        Number airHumid = (Number) sensor.get("air_humid");
                        Number airTemp = (Number) sensor.get("air_temp");

                        if (cushionTemp != null) sumTemp += cushionTemp.doubleValue();
                        if (airHumid != null) sumHumid += airHumid.doubleValue();
                        if (airTemp != null) sumAirTemp += airTemp.doubleValue();

                        count++;
                    }

                    if (count > 0) {
                        double avgTemp = sumTemp / count;
                        double avgHumid = sumHumid / count;
                        double avgAirTemp = sumAirTemp / count;

                        String resultLine = String.format("[%s] 기준 평균: cushion_temp=%.2f, air_humid=%.2f, air_temp=%.2f, 데이터 수=%d, interval=%s",
                                tagTimestamp, avgTemp, avgHumid, avgAirTemp, count, intervalStr);

                        System.out.println(resultLine);
                        resultBuilder.append(resultLine).append("\n");
                    } else {
                        String resultLine = String.format("[%s] 기준 데이터 없음, interval=%s", tagTimestamp, intervalStr);
                        System.out.println(resultLine);
                        resultBuilder.append(resultLine).append("\n");
                    }
                }

                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("데이터 읽기 실패: " + error.getMessage());
                resultBuilder.append("데이터 읽기 실패: ").append(error.getMessage()).append("\n");
                latch.countDown();
            }
        });

        try {
            latch.await(); // 💡 비동기 처리가 끝날 때까지 기다림
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return resultBuilder.toString();
    }

    public String getLastThreeDaysData(Integer patientId) {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        List<AverageData> recentData = averageDataRepository.findByPatientPatientIdAndDateBetween(
                patientId, threeDaysAgo, today.minusDays(1)  // 오늘은 제외
        );

        if (recentData.isEmpty()) {
            return "지난 3일간의 데이터 없음";
        }

        StringBuilder sb = new StringBuilder();
        for (AverageData data : recentData) {
            String line = String.format("%s 평균: cushion_temp=%.2f, air_humid=%.2f, air_temp=%.2f, interval=%.2f분",
                    data.getDate(),
                    data.getCushionTemp() != null ? data.getCushionTemp() : 0.0,
                    data.getAirHumid() != null ? data.getAirHumid() : 0.0,
                    data.getAirTemp() != null ? data.getAirTemp() : 0.0,
                    data.getChangeInterval() != null ? data.getChangeInterval() : 0.0
            );
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    public String getPatientData(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("해당 환자를 찾을 수 없습니다: " + patientId));

        return patient.toString();
    }

    public void sendAlertToCaregiver(String code, Integer patientId, String content) {
        Patient patient = patientRepository.getReferenceById(patientId);
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String title = String.format("%s님의 오늘 건강 리포트 (%s)", patient.getName(), formattedDate);

        try {
            fcmService.sendMessage(code, title, content);

            // db에 alert 저장

            Alert alert = new Alert();
            alert.setPatient(patient);
            alert.setTitle(title);
            alert.setContent(content);
            alert.setTimestamp(LocalDateTime.now());

            alertRepository.save(alert);

            System.out.println("-- 알림 전송 성공");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();

            // MessagingErrorCode 확인
            String errorCode = (e.getMessagingErrorCode() != null) ? e.getMessagingErrorCode().name() : "없음";
            System.out.println("MessagingErrorCode: " + errorCode);

            // HttpResponse 확인
            if (e.getHttpResponse() != null) {
                System.out.println("HTTP Status Code: " + e.getHttpResponse().getStatusCode());
                System.out.println("HTTP Response Body: " + e.getHttpResponse().getContent());
            } else {
                System.out.println("HttpResponse가 null입니다.");
            }

            System.out.println("-- 알림 전송 실패");
        }

    }
}
