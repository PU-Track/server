package putrack.server.service;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FirebaseService {

    public void readData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    for (String key : data.keySet()) {
                        System.out.println(key + " → " + data.get(key));
                    }
                } else {
                    System.out.println("데이터 없음");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("데이터 읽기 실패: " + error.getMessage());
            }
        });
    }
}
