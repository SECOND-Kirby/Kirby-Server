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
}
