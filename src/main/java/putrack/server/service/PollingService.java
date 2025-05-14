package putrack.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PollingService {
    private final FirebaseService firebaseService;

    @Scheduled(fixedRate = 60000)
    public void pollingFirebase() {
        System.out.println("Firebase 데이터 체크 중: " + LocalDateTime.now());
        firebaseService.readData();
    }
}
