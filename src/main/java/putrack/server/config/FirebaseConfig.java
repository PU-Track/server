package putrack.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Value("${firebase.credentials.path}")
    private String databaseCredentialsPath;

    @Value("${firebase.fcm.credentials.path}")
    private String fcmCredentialsPath;

    @PostConstruct
    public void init() throws IOException {
        // realtime firebase
        FileInputStream dbServiceAccount = new FileInputStream(databaseCredentialsPath);
        FirebaseOptions dbOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(dbServiceAccount))
                .setDatabaseUrl(databaseUrl)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(dbOptions);
            System.out.println("FirebaseApp (기본 DB) 초기화 완료: " + FirebaseApp.getInstance().getName());
        } else {
            System.out.println("FirebaseApp (기본 DB) 이미 초기화됨: " + FirebaseApp.getInstance().getName());
        }
    }
}