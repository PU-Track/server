package putrack.server.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.FcmRequestDto;
import putrack.server.service.FcmService;

@RestController
@RequestMapping("/fcm")
@Tag(name = "FCM", description = "알람 전송 API")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/send")
    @Operation(summary = "알림 전송", description = "사용자에게 알림을 전송합니다.")
    public String sendNotification(@Parameter(description = "알림을 보낼 사용자 식별 코드") @RequestParam("code") String code, @RequestBody FcmRequestDto requestDto) {
        try {
            String response = fcmService.sendMessage(
                    code,
                    requestDto.getTitle(),
                    requestDto.getBody()
            );
            return "알림 전송 성공: " + response;
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

            return "알림 전송 실패";
        }
    }
}
