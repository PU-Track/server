package putrack.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FcmConfig {

    @Value("${firebase.fcm.credentials.path}")
    private String fcmCredentialsPath;

    @Bean
    public FirebaseApp fcmFirebaseApp() throws IOException {
        String fcmAppName = "fcmApp";

        if (FirebaseApp.getApps().stream().noneMatch(app -> app.getName().equals(fcmAppName))) {
            FileInputStream serviceAccount = new FileInputStream(fcmCredentialsPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options, fcmAppName);
        } else {
            return FirebaseApp.getInstance(fcmAppName);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp fcmFirebaseApp) {
        return FirebaseMessaging.getInstance(fcmFirebaseApp);
    }
}

