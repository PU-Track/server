package putrack.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import putrack.server.dto.SensorDataDto;
import putrack.server.service.RealtimeService;
import java.util.concurrent.CompletableFuture;

//@RestController
//@RequestMapping("/realtime")
//public class RealtimeController {
//    @Autowired
//    private RealtimeService realtimeService;
//
//    @GetMapping("/{patient_id}")
//    public CompletableFuture<ResponseEntity<SensorDataDto>> getLatestSensorData(@PathVariable("patient_id") Integer patientId) {
//        return realtimeService.getLatestSensorDataByPatientId(patientId)
//                .thenApply(ResponseEntity::ok)
//                .exceptionally(ex -> ResponseEntity.badRequest().body(null));  // 혹은 별도 에러 DTO 사용 가능
//    }
//}
