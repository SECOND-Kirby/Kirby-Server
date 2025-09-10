package com.second.kirby.controller;

import com.second.kirby.dto.response.RobotStatusResponse;
import com.second.kirby.service.RobotBridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final RobotBridgeService robotBridgeService;

    @MessageMapping("/robot/status")
    @SendTo("/topic/robot/status")
    public RobotStatusResponse getRobotStatus() {
        log.debug("WebSocket: 로봇 상태 요청");
        return robotBridgeService.getRobotStatus();
    }

    @MessageMapping("/training/subscribe")
    public void subscribeToTraining(Principal principal) {
        if (principal != null) {
            log.info("WebSocket: 훈련 구독 시작, user={}", principal.getName());
        }
    }
}