package putrack.server.service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import putrack.server.dto.SensorDataDto;
import putrack.server.entity.Device;
import putrack.server.repository.DeviceRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class RealtimeService {

    @Autowired
    private DeviceRepository deviceRepository;

    public CompletableFuture<SensorDataDto> getLatestSensorDataByPatientId(Integer patientId) {
        CompletableFuture<SensorDataDto> future = new CompletableFuture<>();

        Device device = deviceRepository.findByPatient_PatientId(patientId);
        if (device == null) {
            future.completeExceptionally(new RuntimeException("No device found for patientId: " + patientId));
            return future;
        }

        Integer targetDeviceId = device.getDeviceId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sensor_data");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());

                Map<String, Object> latestMap = null;
                long latestTimestamp = Long.MIN_VALUE;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> sensor = (Map<String, Object>) child.getValue();
                    Object deviceIdObj = sensor.get("device_id");
                    Object timestampObj = sensor.get("timestamp");

                    if (deviceIdObj != null && deviceIdObj.toString().equals(targetDeviceId.toString())) {
                        try {
                            long ts = parseTimestamp(timestampObj.toString());
                            if (ts > latestTimestamp) {
                                latestTimestamp = ts;
                                latestMap = sensor;
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid timestamp format: " + timestampObj);
                        }
                    }
                }

                if (latestMap != null) {
                    System.out.println("latestMap: " + latestMap);
                    SensorDataDto dto = new SensorDataDto(
                            Integer.valueOf(latestMap.get("device_id").toString()),
                            latestMap.get("timestamp").toString(),
                            latestMap.get("air_temp") != null ? Double.valueOf(latestMap.get("air_temp").toString()) : null,
                            latestMap.get("air_humid") != null ? Double.valueOf(latestMap.get("air_humid").toString()) : null,
                            latestMap.get("cushion_temp") != null ? Double.valueOf(latestMap.get("cushion_temp").toString()) : null
                    );
                    future.complete(dto);
                } else {
                    future.completeExceptionally(new RuntimeException("No sensor data found for device: " + targetDeviceId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    private long parseTimestamp(String iso8601) {
        // "2025-05-17T12:00:00" -> Unix timestamp (millis)
        return java.time.LocalDateTime
                .parse(iso8601)
                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();
    }
}
