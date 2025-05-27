package putrack.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import putrack.server.dto.FcmRequestDto;
import putrack.server.service.FcmService;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/send")
    @Operation(summary = "알림 전송", description = "사용자에게 알림을 전송합니다.")
    public String sendNotification(@Parameter(description = "알림을 보낼 사용자 식별 코드") @RequestParam String code, @RequestBody FcmRequestDto requestDto) {
        try {
            String response = fcmService.sendMessage(
                    code,
                    requestDto.getTitle(),
                    requestDto.getBody()
            );
            return "알림 전송 성공: " + response;
        } catch (Exception e) {
            e.printStackTrace();
            return "알림 전송 실패: " + e.getMessage();
        }
    }
}
