package com.second.kirby.controller;

import com.second.kirby.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/robot/callback")
@RequiredArgsConstructor
@Slf4j
public class RobotCallbackController {

    private final CollectionService collectionService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @PostMapping("/collection")
    public ResponseEntity<Void> onCollectionUpdate(@RequestBody Map<String, Object> payload) {
        try {
            Long sessionId = ((Number) payload.get("sessionId")).longValue();
            Integer count = ((Number) payload.get("collectedCount")).intValue();
            collectionService.handleCountUpdate(sessionId, count);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("로봇 콜백 파싱 실패: payload={}", payload, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 로봇 일반 텔레메트리(공 수거 개수, 발포 개수, 서브 횟수, 발포 속도, 배터리 등)
    @PostMapping("/telemetry")
    public ResponseEntity<Void> onTelemetry(@RequestBody Map<String, Object> payload) {
        // 그대로 브로드캐스트하여 클라이언트가 시각화하도록 함
        try {
            messagingTemplate.convertAndSend("/topic/robot/telemetry", payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("텔레메트리 브로드캐스트 실패: payload={}", payload, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
