package putrack.server.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import putrack.server.entity.Caregiver;
import putrack.server.repository.CaregiverRepository;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final CaregiverRepository caregiverRepository;

    public String sendMessage(String code, String title, String body) throws FirebaseMessagingException {

        Caregiver caregiver = caregiverRepository.findByCode(code).orElseThrow(() -> new IllegalArgumentException("해당 간병인을 찾을 수 없습니다: " + code));;
        if (caregiver == null || caregiver.getPushToken() == null) {
            throw new IllegalArgumentException("해당 code에 대한 pushToken이 존재하지 않습니다.");
        }

        String targetToken = caregiver.getPushToken();

        FirebaseApp fcmApp = FirebaseApp.getInstance("fcmApp");
        FirebaseMessaging messaging = FirebaseMessaging.getInstance(fcmApp);


        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(notification)
                .build();

        return messaging.send(message);
    }
}
